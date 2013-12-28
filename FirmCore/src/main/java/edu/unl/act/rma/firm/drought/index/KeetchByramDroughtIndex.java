package edu.unl.act.rma.firm.drought.index;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.climate.component.ClimateDataQuery;
import edu.unl.act.rma.firm.climate.configuration.ClimateServiceAccessor;
import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.InvalidStateException;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.PeriodOrderedDataBuilder;
import edu.unl.act.rma.firm.core.YearDataBuilder;
import edu.unl.act.rma.firm.core.configuration.ConfigurationException;
import edu.unl.act.rma.firm.drought.component.SoilsDataQuery;
import edu.unl.act.rma.firm.drought.configuration.DroughtServiceAccessor;

/**
 * @author Ian Cottingham
 * @author Laura Meerkatz
 */
public class KeetchByramDroughtIndex {

	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG,
			KeetchByramDroughtIndex.class);

	public CalendarDataCollection computeKBDI(List<String> stations,
			DateTime firstday, DateTime lastday) throws RemoteException,
			InvalidArgumentException {
		ClimateDataQuery dquery = null;
		SoilsDataQuery squery = null;

		try {
			dquery = ClimateServiceAccessor.getInstance().getClimateDataQuery();
			squery = DroughtServiceAccessor.getInstance().getSoilsDataQuery();
		} catch (InstantiationException ie) {
			LOG.error("error getting service accessor", ie);
			throw new RemoteException("could not get a service accessor");
		} catch (ConfigurationException je) {
			LOG.error("could not get query object", je);
			throw new RemoteException("could not get query object");
		}

		CalendarDataCollection precip = null;
		CalendarDataCollection high_temp = null;
		CalendarDataCollection avg_precip = null;
		Map<String, Float> csm_data = null;
		Map<String, Float> awc_data = null;

		int abs_first_year = 0;
		int req_first_year = firstday.getYear();

		try {
			DateTime leadin = firstday.minusMonths(6);
			abs_first_year = leadin.getYear();
			precip = dquery.getPeriodData(stations, leadin, lastday,
					DataType.PRECIP, CalendarPeriod.DAILY);
			high_temp = dquery.getPeriodData(stations, leadin, lastday,
					DataType.HIGH_TEMP, CalendarPeriod.DAILY);
			avg_precip = dquery.getHistoricalAverageData(stations,
					DataType.PRECIP, CalendarPeriod.ANNUALLY);
			csm_data = squery.getCurrentSoilMoisture(stations, leadin);
			awc_data = squery.getWaterHoldingCapacity(stations);
		} catch (RemoteException re) {
			LOG.error("error getting data variables", re);
			throw new RemoteException("error getting data variables");
		} catch (RuntimeException t) {
			LOG.error("unknown error getting data variables", t);
			throw new RemoteException("error getting data variables");
		}

		YearDataBuilder builder = new YearDataBuilder(firstday, lastday,
				CalendarPeriod.DAILY, DataType.KBDI);

		try {
			int data_pos = 1;
			station_point: for (String station : stations) {
				try {
					builder.openStation(station);
					data_pos = 1;
				} catch (InvalidStateException ise) {
					LOG.error("could not open station", ise);
					throw new RemoteException("station write error");
				}
				boolean missing = false;
				if (csm_data.get(station) == DataType.MISSING
						|| awc_data.get(station) == DataType.MISSING) {
					missing = true;
				}
				if (missing) {
					LOG
							.info("station "
									+ station
									+ " does not have a CSM or ASM value, it will be skipped");
					fillStation(station, builder, DataType.MISSING);
				}

				// float yd = 800 * ( 1 - (csm_data.get(station) /
				// awc_data.get(station)) );
				float yd = 0;
				float[][] precip_matrix = precip.getDataMatrix(station);
				float[][] temp_matrix = high_temp.getDataMatrix(station);
				boolean write_val = false;
				float runningTotal = 0;

				/*
				 * this will track the position in the data request to push into
				 * the request year
				 */
				for (int year = 0; year < precip_matrix.length; year++) {
					for (int period = 0; period < precip_matrix[year].length; period++) {
						if ((year + abs_first_year >= req_first_year)) {
							// in range of the builder
							if ((data_pos >= builder.getPeriodStartIndex() && data_pos <= builder
									.getPeriodEndIndex())) {
								write_val = true; // in range of request
							}
							data_pos++;
						}

						try {
							if ((precip_matrix[year][period] == DataType.OUTSIDE_OF_RANGE || temp_matrix[year][period] == DataType.OUTSIDE_OF_RANGE)) {
								if (write_val) {
									builder.add(DataType.OUTSIDE_OF_RANGE);
								}
							} else if ((precip_matrix[year][period] == DataType.MISSING || temp_matrix[year][period] == DataType.MISSING)) {
								if (write_val) {
									builder.add(DataType.MISSING);
								}
							} else if ((precip_matrix[year][period] == DataType.ERROR_RESULT || temp_matrix[year][period] == DataType.ERROR_RESULT)) {
								if (write_val) {
									builder.add(DataType.ERROR_RESULT);
								}
							} else {
								float r = avg_precip.getDataMatrix(station)[0][0];
								float nr = precip_matrix[year][period];

								float nr1 = nr;
								if (runningTotal == 0.0f) {
									nr = nr - 0.2f;
									nr = (nr > 0.0f) ? nr : 0.0f;
								} else if ((runningTotal > 0.0f)
										&& (runningTotal <= 0.2f)) {
									nr = nr + runningTotal - 0.2f;
									nr = (nr > 0.0f) ? nr : 0.0f;
								}
								if (nr1 == 0.0f) {
									runningTotal = 0.0f;
								} else {
									runningTotal += nr1;
								}

								if (nr > 0) {
									float yd_adjusted = 0f;
									yd_adjusted = yd - (100f * nr);
									yd = yd_adjusted;
								}

								float df = (float) (((800.0f - yd) * ((0.968f * Math
										.exp(0.0486f * temp_matrix[year][period])) - 8.3f)) / (1 + (10.88f * Math
										.exp(-0.0441f * r)))) * .001f;

								df = (df < 0) ? 0 : df;

								/* compute and add the KBDI value */
								float kbdi = 0;

								kbdi = yd + df;

								if (kbdi > 800) {
									kbdi = 800;
								}
								if (kbdi < 0) {
									kbdi = 0;
								}
								if (write_val) {
									builder.add(kbdi); // only add a KBDI in the
									// range we want
								}
								yd = kbdi;
							}
						} catch (InvalidStateException ise) {
							LOG
									.error("could not add value to builder, station "
											+ station);
							fillStation(station, builder, DataType.ERROR_RESULT);
							continue station_point;
						}

						write_val = false;
					}
				}

				try {
					/* finish up the out of range values */
					while (!builder.isLimitReached()) {
						builder.add(DataType.OUTSIDE_OF_RANGE);
					}

					builder.writeStation();
				} catch (InvalidStateException iEx) {
					LOG.error("could not write station " + station, iEx);
					fillStation(station, builder, DataType.ERROR_RESULT);
				}
			}
		} catch (Throwable t) {
			LOG.error("unknown critical error when computing KBDI", t);
			throw new RemoteException("error computing KBDI");
		}

		return builder.returnCollection();
	}

	public float[][] compute(int station_start_year, float[][] precip_data,
			float[][] high_temp_data, float avg_precip, float csm, float awc, DateTime firstday,
			DateTime lastday) throws RemoteException, InvalidArgumentException {

		DateTime leadin = firstday.minusMonths(6);

		// daily precip
		float[][] precip = selectDataRange(station_start_year, precip_data,
				leadin, lastday);
		// daily high temp
		float[][] high_temp = selectDataRange(station_start_year,
				high_temp_data, leadin, lastday);

		if (csm == DataType.MISSING || awc == DataType.MISSING) {
			LOG
					.info("station does not have a CSM or ASM value, it will be skipped");
		}

		float yd = 0;
		float runningTotal = 0;
		float[][] kbdi_data = new float[precip.length][precip[0].length];

		for (int year = 0; year < precip.length; year++) {
			for (int period = 0; period < precip[year].length; period++) {
				if ((precip[year][period] == DataType.OUTSIDE_OF_RANGE || high_temp[year][period] == DataType.OUTSIDE_OF_RANGE)) {
					kbdi_data[year][period] = DataType.OUTSIDE_OF_RANGE;
				} else if ((precip[year][period] == DataType.MISSING || high_temp[year][period] == DataType.MISSING)) {
					kbdi_data[year][period] = DataType.MISSING;
				} else if ((precip[year][period] == DataType.ERROR_RESULT || high_temp[year][period] == DataType.ERROR_RESULT)) {
					kbdi_data[year][period] = DataType.ERROR_RESULT;
				} else {
					float r = avg_precip;
					float nr = precip[year][period];

					float nr1 = nr;
					if (runningTotal == 0.0f) {
						nr = nr - 0.2f;
						nr = (nr > 0.0f) ? nr : 0.0f;
					} else if ((runningTotal > 0.0f) && (runningTotal <= 0.2f)) {
						nr = nr + runningTotal - 0.2f;
						nr = (nr > 0.0f) ? nr : 0.0f;
					}
					if (nr1 == 0.0f) {
						runningTotal = 0.0f;
					} else {
						runningTotal += nr1;
					}

					if (nr > 0) {
						float yd_adjusted = 0f;
						yd_adjusted = yd - (100f * nr);
						yd = yd_adjusted;
					}

					float df = (float) (((800.0f - yd) * ((0.968f * Math
							.exp(0.0486f * high_temp[year][period])) - 8.3f)) / (1 + (10.88f * Math
							.exp(-0.0441f * r)))) * .001f;

					df = (df < 0) ? 0 : df;

					/* compute and add the KBDI value */
					float kbdi = 0;

					kbdi = yd + df;

					if (kbdi > 800) {
						kbdi = 800;
					}
					if (kbdi < 0) {
						kbdi = 0;
					}
					kbdi_data[year][period] = kbdi;
					yd = kbdi;
				}
			}
		}

		return kbdi_data;
	}

	private float[][] selectDataRange(int dataStartYear, float[][] data,
			DateTime startDate, DateTime endDate) {
		int period_length = data[0].length;
		int years = endDate.getYear() - startDate.getYear() + 1;
		float[][] periodData = new float[years][period_length];
		int shift = startDate.getYear() - dataStartYear;
		for (int i = 0; i < periodData.length; i++) {
			for (int j = 0; j < periodData[i].length; j++) {
				if ((i == 0) && (j < startDate.getDayOfYear())) {
					periodData[i][j] = DataType.OUTSIDE_OF_REQUEST_RANGE;
				} else if ((i == periodData.length - 1)
						&& (j > endDate.getDayOfYear())) {
					periodData[i][j] = DataType.OUTSIDE_OF_REQUEST_RANGE;
				} else {
					periodData[i][j] = data[i + shift][j];
				}
			}
		}
		return periodData;
	}

	

	/**
	 * fill out stations with a flag value, typically some kind of error or
	 * missing result. Calling this method for a station will resultin the
	 * station containing no data other than the flag value provided, any data
	 * already added to the builder will be discarded.
	 * 
	 * @param station
	 *            - the station to fill
	 * @param builder
	 *            - the builder fulfilling this request
	 * @param value
	 *            - the flag value to fill with
	 * 
	 * @throws RemoteException
	 * @throws InvalidArgumentException
	 */
	private void fillStation(String station, PeriodOrderedDataBuilder builder,
			float value) throws RemoteException, InvalidArgumentException {
		builder.disposeStation();
		try {
			builder.openStation(station);
			while (!builder.isLimitReached()) {
				builder.add(value);
			}
			builder.writeStation();
		} catch (InvalidStateException isEx) {
			LOG.error("could not open station for error result", isEx);
			throw new RemoteException(
					"unrecoverable error while populating station " + station);
		}
	}
}
