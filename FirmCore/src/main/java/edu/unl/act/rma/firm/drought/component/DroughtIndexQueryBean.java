/* Created On: Sep 30, 2005 */
package edu.unl.act.rma.firm.drought.component;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.climate.configuration.ClimateServiceAccessor;
import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DTOCollection;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.InvalidStateException;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.StationMetaDataType;
import edu.unl.act.rma.firm.core.YearDataBuilder;
import edu.unl.act.rma.firm.core.configuration.ConfigurationException;
import edu.unl.act.rma.firm.drought.NsmCompleteDTO;
import edu.unl.act.rma.firm.drought.NsmSummaryDTO;
import edu.unl.act.rma.firm.drought.PdsiType;
import edu.unl.act.rma.firm.drought.configuration.DroughtServiceAccessor;
import edu.unl.act.rma.firm.drought.index.KeetchByramDroughtIndex;
import edu.unl.act.rma.firm.drought.index.NewhallSimulationModel;
import edu.unl.act.rma.firm.drought.index.PalmerDroughtSeverityIndex;
import edu.unl.act.rma.firm.drought.index.StandardizedPrecipitationIndex;

/**
 * @author Ian Cottingham
 * 
 */
@Stateless
@Remote( { DroughtIndexQuery.class })
@Local( { LocalDroughtIndexQuery.class })
public class DroughtIndexQueryBean implements DroughtIndexQuery {

	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG,
			DroughtIndexQueryBean.class);

	@Override
	public CalendarDataCollection computeScPdsi(List<String> stations,
			PdsiType type, int step, CalendarPeriod period, DateTime ending,
			int fromYear) throws RemoteException, InvalidArgumentException {

		if (ending == null) {
			throw new InvalidArgumentException("null end date");
		}
		if (ending.getYear() < fromYear) {
			throw new InvalidArgumentException("start date (" + fromYear
					+ ") is after end date (" + ending.toString() + ")");
		}
		switch (period) {
		case WEEKLY:
			if (step != 1 && step != 2 && step != 4 && step != 13) {
				throw new InvalidArgumentException(step
						+ " is not a valid weekly step for the scPDSI");
			}
			break;
		case MONTHLY:
			if (step != 1) {
				throw new InvalidArgumentException(step
						+ " is not a valid monthly step for the scPDSI");
			}
			break;
		default:
			throw new InvalidArgumentException(period.name()
					+ " is an invalid period type for the PDSI");
		}
		if (period != CalendarPeriod.WEEKLY && period != CalendarPeriod.MONTHLY) {
			throw new InvalidArgumentException(
					"The PDSI can only be computed over weekly or monthly periods");
		}

		CalendarDataCollection precip_data;
		CalendarDataCollection temp_data;
		CalendarDataCollection all_normals;
		Map<String, Object> all_latitudes = null;
		Map<String, Float> all_awcs = null;
		if (fromYear == -1) {
			try {
				fromYear = ClimateServiceAccessor.getInstance()
						.getClimateMetaDataQuery().getLongestPeriod(stations,
								period).getStart().getYear();
			} catch (Exception e) {
				LOG.error("could not compute the station interval", e);
				RemoteException re = new RemoteException();
				re.initCause(e);
				throw re;
			}
		}
		int diff = 0;
		DateTime precip_start = new DateTime(fromYear, 1, 1, 0, 0, 0, 0,
				GregorianChronology.getInstance());
		if ((diff = (ending.getYear() - fromYear)) < 30) {
			precip_start = precip_start.minusYears(30 - diff);
		}

		try {
			precip_data = ClimateServiceAccessor.getInstance()
					.getClimateDataQuery().getPeriodData(stations,
							precip_start, ending, DataType.PRECIP, period);
			temp_data = ClimateServiceAccessor.getInstance()
					.getClimateDataQuery().getPeriodData(stations,
							precip_start, ending, DataType.NORMAL_TEMP, period);
			all_normals = ClimateServiceAccessor.getInstance()
					.getClimateDataQuery().getHistoricalAverageData(stations,
							DataType.NORMAL_TEMP, period);
			all_latitudes = ClimateServiceAccessor.getInstance()
					.getClimateMetaDataQuery().getMetaData(stations,
							StationMetaDataType.LATITUDE, CalendarPeriod.DAILY)
					.extractType(StationMetaDataType.LATITUDE);
			all_awcs = DroughtServiceAccessor.getInstance().getSoilsDataQuery()
					.getWaterHoldingCapacity(stations);
		} catch (Exception e) {
			LOG.error("could not get the precip data", e);
			RemoteException re = new RemoteException();
			re.initCause(e);
			throw re;
		}
		DateTime act_ending = new DateTime(ending.getYear(), 12, 31, 0, 0, 0, 0);
		YearDataBuilder builder = new YearDataBuilder(ending.minusYears(diff),
				act_ending, period, DataType.scPDSI);

		PalmerDroughtSeverityIndex pdsi = new PalmerDroughtSeverityIndex(
				fromYear);

		float[][] pdsi_data = null;
		try {
			for (String str : precip_data) {
				pdsi.setData(precip_data.getDataMatrix(str), temp_data
						.getDataMatrix(str), all_normals.getDataMatrix(str)[0],
						(Float) all_latitudes.get(str), (Float) all_awcs
								.get(str));
				if (period == CalendarPeriod.WEEKLY) {
					pdsi_data = pdsi.weeklyPDSI(step);
				} else {
					pdsi_data = pdsi.scMonthlyPDSI();
				}
				DateTime date_pos = ending.minusYears(diff);

				int x_pos = ((period == CalendarPeriod.WEEKLY) ? date_pos
						.getWeekOfWeekyear() : date_pos.getMonthOfYear()) - 1;

				// if the week of the year is 52 but the month is Jan. then the
				// x_pos should have been 1
				if (date_pos.getWeekOfWeekyear() == 52
						&& date_pos.getMonthOfYear() == 1) {
					x_pos = 0;
				}

				int y_pos = (fromYear - precip_start.getYear());

				builder.openStation(str);
				while (true) {
					try {
						builder.add(pdsi_data[y_pos][x_pos++]);
						if (x_pos >= period.getLength()) {
							y_pos++;
							x_pos = 0;
						}
						if (y_pos >= pdsi_data.length) {
							break;
						}
					} catch (ArrayIndexOutOfBoundsException ae) {
						LOG
								.error(
										"The computation state was invalid -- see the debug log for details",
										ae);
						LOG.debug("date_pos=" + date_pos.toString());
						LOG.debug("fromYear=" + fromYear);
						LOG.debug("ending=" + ending.toString());
						LOG.debug("x_pos=" + x_pos);
						LOG.debug("y_pos=" + y_pos);
						LOG.debug("period len=" + period.getLength());
						LOG.debug("Builder state="
								+ builder.getExpectedValues() + "/"
								+ builder.getPeriodStartIndex() + "/"
								+ builder.getPeriodEndIndex());
						LOG.debug("pdsi_size" + pdsi_data.length);
						RemoteException re = new RemoteException(
								"An error occured reading the data array");
						re.initCause(ae);
						throw re;
					}

					date_pos = (period == CalendarPeriod.WEEKLY) ? date_pos
							.plusWeeks(1) : date_pos.plusMonths(1);
				}
				builder.writeStation();
			}
		} catch (InvalidStateException ise) {
			LOG.error("an error occured writing the PDSI data to the builder",
					ise);
			RemoteException re = new RemoteException();
			re.initCause(ise);
		}

		return builder.returnCollection();
	}

	@Override
	public CalendarDataCollection computeSpi(List<String> stations,
			CalendarPeriod period, int step, DateTime ending, int fromYear)
			throws RemoteException, InvalidArgumentException {

		if (period != CalendarPeriod.WEEKLY && period != CalendarPeriod.MONTHLY) {
			throw new InvalidArgumentException(
					"The SPI can only be computed over weekly or monthly periods");
		}

		if (ending == null) {
			throw new InvalidArgumentException("null end date");
		}
		if (ending.getYear() < fromYear) {
			throw new InvalidArgumentException("start date (" + fromYear
					+ ") is after end date (" + ending.toString() + ")");
		}

		CalendarDataCollection precip_data;

		if (fromYear == -1) {
			try {
				fromYear = ClimateServiceAccessor.getInstance()
						.getClimateMetaDataQuery().getLongestPeriod(stations,
								period).getStart().getYear();
			} catch (Exception e) {
				LOG.error("could not compute the station interval", e);
				RemoteException re = new RemoteException();
				re.initCause(e);
				throw re;
			}
		}

		int diff = 0;
		DateTime precip_start = new DateTime(fromYear, 1, 1, 0, 0, 0, 0,
				GregorianChronology.getInstance());
		if ((diff = (ending.getYear() - fromYear)) < 30) {
			precip_start = precip_start.minusYears(30 - diff);
		}

		try {
			precip_data = ClimateServiceAccessor.getInstance()
					.getClimateDataQuery().getPeriodData(stations,
							precip_start, ending, DataType.PRECIP, period);
		} catch (Exception e) {
			LOG.error("could not get the precip data", e);
			RemoteException re = new RemoteException();
			re.initCause(e);
			throw re;
		}
		DateTime act_ending = new DateTime(ending.getYear(), 12, 31, 0, 0, 0, 0);
		YearDataBuilder builder = new YearDataBuilder(ending.minusYears(diff),
				act_ending, period, DataType.SPI);

		StandardizedPrecipitationIndex spi = new StandardizedPrecipitationIndex(
				fromYear);

		float[][] spi_data;
		try {
			for (String str : precip_data) {
				spi.setData(precip_data.getDataMatrix(str));
				spi_data = spi.computeSpi(step);
				DateTime date_pos = ending.minusYears(diff);

				int x_pos = ((period == CalendarPeriod.WEEKLY) ? date_pos
						.getWeekOfWeekyear() : date_pos.getMonthOfYear()) - 1;

				// if the week of the year is 52 but the month is Jan. then the
				// x_pos should have been 1
				if (date_pos.getWeekOfWeekyear() == 52
						&& date_pos.getMonthOfYear() == 1) {
					x_pos = 0;
				}

				int y_pos = (fromYear - precip_start.getYear());

				builder.openStation(str);
				while (true) {
					try {
						builder.add(spi_data[y_pos][x_pos++]);
						if (x_pos >= period.getLength()) {
							y_pos++;
							x_pos = 0;
						}
						if (y_pos >= spi_data.length) {
							break;
						}
					} catch (ArrayIndexOutOfBoundsException ae) {
						LOG
								.error(
										"The computation state was invalid -- see the debug log for details",
										ae);
						LOG.debug("date_pos=" + date_pos.toString());
						LOG.debug("fromYear=" + fromYear);
						LOG.debug("ending=" + ending.toString());
						LOG.debug("x_pos=" + x_pos);
						LOG.debug("y_pos=" + y_pos);
						LOG.debug("period len=" + period.getLength());
						LOG.debug("Builder state="
								+ builder.getExpectedValues() + "/"
								+ builder.getPeriodStartIndex() + "/"
								+ builder.getPeriodEndIndex());
						LOG.debug("spi_size" + spi_data.length);
						RemoteException re = new RemoteException(
								"An error occured reading the data array");
						re.initCause(ae);
						throw re;
					}

					date_pos = (period == CalendarPeriod.WEEKLY) ? date_pos
							.plusWeeks(1) : date_pos.plusMonths(1);
				}
				builder.writeStation();
			}
		} catch (InvalidStateException ise) {
			LOG.error("an error occured writing the SPI data to the builder",
					ise);
			RemoteException re = new RemoteException();
			re.initCause(ise);
		}

		return builder.returnCollection();
	}

	@Override
	public CalendarDataCollection computeRangeKBDI(List<String> stations,
			DateTime firstday, DateTime lastday) throws RemoteException,
			InvalidArgumentException {
		try {
			return new KeetchByramDroughtIndex().computeKBDI(stations,
					firstday, lastday);
		} catch (ConfigurationException jme) {
			LOG.error("exception obtaining KBDI object", jme);
			throw new RemoteException("could not get a KBDI object");
		} catch (RemoteException re) {
			LOG.error("exception running newhall", re);
			throw new RemoteException("exception running the kbdi");
		}
	}

	@Override
	public DTOCollection<NsmCompleteDTO> computeCompleteNewhall(
			List<String> stations, DateTime start, DateTime end)
			throws RemoteException, InvalidArgumentException {
		try {

			if (start == null) {
				throw new InvalidArgumentException("null start date");
			}
			if (end == null) {
				throw new InvalidArgumentException("null end date");
			}
			if (end.getYear() < start.getYear()) {
				throw new InvalidArgumentException("start date (" + start
						+ ") is after end date (" + end.toString() + ")");
			}

			CalendarDataCollection precip_data;
			CalendarDataCollection temp_data;
			Map<String, Object> all_latitudes = null;
			Map<String, Float> all_awcs = null;
			int fromYear = start.getYear();
			if (fromYear == -1) {
				try {
					fromYear = ClimateServiceAccessor.getInstance()
							.getClimateMetaDataQuery().getLongestPeriod(
									stations, CalendarPeriod.MONTHLY)
							.getStart().getYear();
				} catch (Exception e) {
					LOG.error("could not compute the station interval", e);
					RemoteException re = new RemoteException();
					re.initCause(e);
					throw re;
				}
			}

			DateTime precip_start = new DateTime(fromYear, 1, 1, 0, 0, 0, 0,
					GregorianChronology.getInstance());

			try {
				precip_data = ClimateServiceAccessor.getInstance()
						.getClimateDataQuery().getPeriodData(stations,
								precip_start, end, DataType.PRECIP,
								CalendarPeriod.MONTHLY);
			} catch (Exception e) {
				LOG.error("could not get the precip data", e);
				RemoteException re = new RemoteException();
				re.initCause(e);
				throw re;
			}
			try {
				temp_data = ClimateServiceAccessor.getInstance()
						.getClimateDataQuery().getPeriodData(stations,
								precip_start, end, DataType.NORMAL_TEMP,
								CalendarPeriod.MONTHLY);
			} catch (Exception e) {
				LOG.error("could not get the temperature data", e);
				RemoteException re = new RemoteException();
				re.initCause(e);
				throw re;
			}

			try {
				all_latitudes = ClimateServiceAccessor.getInstance()
						.getClimateMetaDataQuery().getMetaData(stations,
								StationMetaDataType.LATITUDE,
								CalendarPeriod.MONTHLY).extractType(
								StationMetaDataType.LATITUDE);
			} catch (InstantiationException e) {
				LOG.error("an error occured getting the latitudes", e);
				RemoteException re = new RemoteException();
				re.initCause(e);
			}
			try {
				all_awcs = DroughtServiceAccessor.getInstance()
						.getSoilsDataQuery().getWaterHoldingCapacity(stations);
			} catch (InstantiationException e) {
				LOG.error("an error occured getting the awc", e);
				RemoteException re = new RemoteException();
				re.initCause(e);

			}
			NewhallSimulationModel nsm = new NewhallSimulationModel(
					precip_start.getYear(), end.getYear());
			DTOCollection<NsmCompleteDTO> collection = new DTOCollection<NsmCompleteDTO>();

			try {
				// Iterate over all stations
				for (String station : stations) {
					NsmCompleteDTO dto = null;
					if (all_latitudes.get(station) == null
							|| all_awcs.get(station) == null) {
						continue;
					}
					nsm.setData(precip_data.getDataMatrix(station), temp_data
							.getDataMatrix(station), (Float) all_latitudes
							.get(station), (Float) all_awcs.get(station));

					dto = nsm.computeCompleteNsm();
					collection.add(station, dto);

				}
			} catch (InvalidStateException ise) {
				LOG.error("exception running complete newhall", ise);
				throw new RemoteException(
						"exception running the complete newhall");
			}
			return collection;
		} catch (RemoteException re) {
			LOG.error("exception running complete newhall", re);
			throw new RemoteException("exception running the complete newhall");
		} catch (InvalidArgumentException iae) {
			LOG.error("exception running complete newhall", iae);
			throw new RemoteException("exception running the complete newhall");
		}
	}

	@Override
	public DTOCollection<NsmSummaryDTO> computeSummaryNewhall(
			List<String> stations, DateTime start, DateTime end)
			throws RemoteException, InvalidArgumentException {
		try {
			if (start == null) {
				throw new InvalidArgumentException("null start date");
			}
			if (end == null) {
				throw new InvalidArgumentException("null end date");
			}
			if (end.getYear() < start.getYear()) {
				throw new InvalidArgumentException("start date (" + start
						+ ") is after end date (" + end.toString() + ")");
			}

			CalendarDataCollection precip_data;
			CalendarDataCollection temp_data;
			Map<String, Object> all_latitudes = null;
			Map<String, Float> all_awcs = null;
			int fromYear = start.getYear();
			if (fromYear == -1) {
				try {
					fromYear = ClimateServiceAccessor.getInstance()
							.getClimateMetaDataQuery().getLongestPeriod(
									stations, CalendarPeriod.MONTHLY)
							.getStart().getYear();
				} catch (Exception e) {
					LOG.error("could not compute the station interval", e);
					RemoteException re = new RemoteException();
					re.initCause(e);
					throw re;
				}
			}

			try {
				precip_data = ClimateServiceAccessor.getInstance()
						.getClimateDataQuery().getPeriodData(stations, start,
								end, DataType.PRECIP, CalendarPeriod.MONTHLY);
			} catch (Exception e) {
				LOG.error("could not get the precip data", e);
				RemoteException re = new RemoteException();
				re.initCause(e);
				throw re;
			}
			try {
				temp_data = ClimateServiceAccessor.getInstance()
						.getClimateDataQuery().getPeriodData(stations, start,
								end, DataType.NORMAL_TEMP,
								CalendarPeriod.MONTHLY);
			} catch (Exception e) {
				LOG.error("could not get the temperature data", e);
				RemoteException re = new RemoteException();
				re.initCause(e);
				throw re;
			}

			try {
				all_latitudes = ClimateServiceAccessor.getInstance()
						.getClimateMetaDataQuery().getMetaData(stations,
								StationMetaDataType.LATITUDE,
								CalendarPeriod.MONTHLY).extractType(
								StationMetaDataType.LATITUDE);
			} catch (InstantiationException e) {
				LOG.error("an error occured getting the latitudes", e);
				RemoteException re = new RemoteException();
				re.initCause(e);
			}
			try {
				all_awcs = DroughtServiceAccessor.getInstance()
						.getSoilsDataQuery().getWaterHoldingCapacity(stations);
			} catch (InstantiationException e) {
				LOG.error("an error occured getting the awc", e);
				RemoteException re = new RemoteException();
				re.initCause(e);

			}
			NewhallSimulationModel nsm = new NewhallSimulationModel(start
					.getYear(), end.getYear());
			DTOCollection<NsmSummaryDTO> collection = new DTOCollection<NsmSummaryDTO>();

			// Iterate over all stations
			try {
				for (String station : stations) {
					NsmSummaryDTO dto = null;
					if (all_latitudes.get(station) == null
							|| all_awcs.get(station) == null) {
						continue;
					}
					nsm.setData(precip_data.getDataMatrix(station), temp_data
							.getDataMatrix(station), (Float) all_latitudes
							.get(station), (Float) all_awcs.get(station));

					dto = nsm.computeSummaryNsm();
					collection.add(station, dto);
				}
			} catch (InvalidStateException iae) {
				LOG.error("exception running complete newhall", iae);
				throw new RemoteException(
						"exception running the complete newhall");
			}
			return collection;

		} catch (RemoteException re) {
			LOG.error("exception running complete newhall", re);
			throw new RemoteException("exception running the complete newhall");
		} catch (InvalidArgumentException iae) {
			LOG.error("exception running complete newhall", iae);
			throw new RemoteException("exception running the complete newhall");
		}

	}
}
