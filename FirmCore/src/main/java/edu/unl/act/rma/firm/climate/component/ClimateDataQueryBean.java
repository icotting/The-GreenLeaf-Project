/* Created On: Sep 8, 2005 */
package edu.unl.act.rma.firm.climate.component;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.climate.ClimateQueryStrings;
import edu.unl.act.rma.firm.climate.TemperatureDayTypes;
import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarDataParser;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.InvalidStateException;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.TemporalPeriod;
import edu.unl.act.rma.firm.core.YearDataBuilder;
import edu.unl.act.rma.firm.core.configuration.CoreServiceManager;

/**
 * @see edu.unl.act.rma.firm.climate.component.ClimateDataQuery
 * 
 * @author Ian Cottingham
 */
@Stateless
@Local({LocalClimateDataQuery.class})
@Remote({ClimateDataQuery.class})
public class ClimateDataQueryBean implements LocalClimateDataQuery {
	
	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG, ClimateDataQueryBean.class);

	private DataSource source = DataSourceInjector.injectDataSource(DataSourceTypes.CLIMATIC);

	@EJB
	private ClimateMetaDataQuery metaDataQuery;

	@Override
	public CalendarDataCollection getAvailableData(List<String> stationIds,
			DataType type, CalendarPeriod period) throws RemoteException,
			InvalidArgumentException {
		
		switch ( period )  { 
		case DAILY:
			return this.getAvailableDailyData(stationIds, type);
		case WEEKLY:
			return this.getAvailableWeeklyData(stationIds, type);
		case MONTHLY:
			return this.getAvailableMonthlyData(stationIds, type);
		case ANNUALLY:
			return this.getAvailableAnnualData(stationIds, type);
		default:
			throw new InvalidArgumentException("An invalid calendar period was specified");
		}
	}

	@Override
	public CalendarDataCollection getDataNormals(List<String> stationIds,
			int startYear, int endYear, DataType type, CalendarPeriod period)
			throws RemoteException, InvalidArgumentException {

		switch ( period )  { 
		case DAILY:
			return this.getDailyDataNormals(stationIds, startYear, endYear, type);
		case WEEKLY:
			return this.getWeeklyDataNormals(stationIds, startYear, endYear, type);
		case MONTHLY:
			return this.getMonthlyDataNormals(stationIds, startYear, endYear, type);
		case ANNUALLY:
			return this.getAnnualDataNormals(stationIds, startYear, endYear, type);
		default:
			throw new InvalidArgumentException("An invalid calendar period was specified");
		}
	}

	@Override
	public CalendarDataCollection getHistoricalAverageData(
			List<String> stationIds, DataType type, CalendarPeriod period)
			throws RemoteException, InvalidArgumentException {
		switch ( period )  { 
		case DAILY:
			return this.getHistoricalAverageDailyData(stationIds, type);
		case WEEKLY:
			return this.getHistoricalAverageWeeklyData(stationIds, type);
		case MONTHLY:
			return this.getHistoricalAverageMonthlyData(stationIds, type);
		case ANNUALLY:
			return this.getHistoricalAverageAnnualData(stationIds, type);
		default:
			throw new InvalidArgumentException("An invalid calendar period was specified");
		}
	}

	@Override
	public CalendarDataCollection getPeriodData(List<String> stationIds,
			DateTime firstDate, DateTime lastDate, DataType type,
			CalendarPeriod period) throws RemoteException,
			InvalidArgumentException {
		switch ( period )  { 
		case DAILY:
			return this.getPeriodDailyData(stationIds, firstDate, lastDate, type);
		case WEEKLY:
			return this.getPeriodWeeklyData(stationIds, firstDate, lastDate, type);
		case MONTHLY:
			return this.getPeriodMonthlyData(stationIds, firstDate, lastDate, type);
		case ANNUALLY:
			return this.getPeriodAnnualData(stationIds, firstDate, lastDate, type);
		default:
			throw new InvalidArgumentException("An invalid calendar period was specified");
		}
	}

	/**
	 * @see edu.unl.act.rma.firm.climate.component.ClimateDataQuery#getPercentageNormal(java.util.List, edu.unl.firm.common.DataType, edu.unl.firm.common.CalendarPeriod, org.joda.time.DateTime)
	 */
	 public CalendarDataCollection getPercentageNormal(List<String> stationIds,DataType type,CalendarPeriod summary,DateTime start, DateTime end) throws RemoteException, InvalidArgumentException, InvalidStateException{
		
		/* round of the dates */
		start = new DateTime(start.getYear(),1,1,0,0,0,0,GregorianChronology.getInstance());
		end = new DateTime(end.getYear(),12,31,0,0,0,0,GregorianChronology.getInstance());
		
		CalendarDataCollection all_avgdata = null;
		CalendarDataCollection all_data = null;
		
		switch(summary) {
			case DAILY:
				all_avgdata = getHistoricalAverageDailyData(stationIds,type);
				all_data = getPeriodDailyData(stationIds,start,end,type);
				break;
			case WEEKLY:
				all_avgdata = getHistoricalAverageWeeklyData(stationIds,type);
				all_data = getPeriodWeeklyData(stationIds,start,end,type);
				break;
			case MONTHLY:
				all_avgdata = getHistoricalAverageMonthlyData(stationIds,type);
				all_data = getPeriodMonthlyData(stationIds,start,end,type);
				break;
		}
		
		/* the data range may be different than the request for weekly, so we use those values 
		 * (this happens when the first couple of days in a year are still in the 52nd week of
		 * the previous year) */
		YearDataBuilder builder = new YearDataBuilder(all_data.getBegin(), all_data.getEnd(), summary, type);
		for ( String station : all_avgdata ) {
			builder.openStation(station);
			if ( all_data.getStationData(station) == null ) { 
				continue;
			} else { 
				/* the average data has only a single row, which is used against all years for a comparison */
				float[] avg = all_avgdata.getStationData(station).iterator().next();
				for ( float[] d : all_data.getStationData(station) ) { 
					for ( int i=0; i<d.length; i++ ) { 
						if ( d[i] == DataType.MISSING ) { 
							builder.add(DataType.MISSING);
						} else if ( d[i] == DataType.OUTSIDE_OF_RANGE ) { 
							builder.add(DataType.OUTSIDE_OF_RANGE);
						} else if ( d[i] == DataType.OUTSIDE_OF_REQUEST_RANGE ) { 
							builder.add(DataType.OUTSIDE_OF_REQUEST_RANGE);
						} else if ( d[i] == DataType.ERROR_RESULT ) {
							LOG.warn("An error result was found when computing % normal for "+station+" on "+type.name());
							builder.add(DataType.ERROR_RESULT);
						} else {
							builder.add(d[i] / avg[i]);
						}
					}
				}
			}
			
			builder.writeStation();
		}
		
		return builder.returnCollection();
	}
		
	/**
	 * @throws InvalidStateException 
	 * @see ClimateDataQuery#getFrostFreePeriod(List, int, int)
	 */
	 public CalendarDataCollection getFrostFreePeriod(List<String> stations, int fromYear, int endingYear) throws RemoteException, InvalidArgumentException {
		
		
		Float threshold = null;
		try {
			threshold = CoreServiceManager.getInstance().getDefaultThreshold();
		} catch (RemoteException jme) {
			LOG.error("error getting the default threshold value", jme);
			throw new RemoteException("exception getting the default threshold value");
		} catch (InstantiationException ie) {
			LOG.error("error instantiating the data service", ie);
			throw new RemoteException("could not instantiate the data service");
		}
		int max_missing = (int) ((1.0f - threshold) * CalendarPeriod.DAILY.getLength());
		
		DateTime from = new DateTime(fromYear, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime ending = new DateTime(endingYear, 12 ,31 ,0, 0, 0, 0, GregorianChronology.getInstance());

		// correct for 53-week years
		if (from.getWeekOfWeekyear() == 53) {
			from = from.plusWeeks(1);
		}
		if (ending.getWeekOfWeekyear() == 53) {
			ending = ending.minusWeeks(1);
		}
		
		YearDataBuilder builder = new YearDataBuilder(from, ending, CalendarPeriod.ANNUALLY, DataType.FFP);
		CalendarDataCollection low_temps = getPeriodDailyData(stations, from, ending, DataType.LOW_TEMP);
		
		try {
			for (String station_id : stations) {
				builder.openStation(station_id);
	
				// iterate through each station's data
				year_loop:
				for (float[] year_of_data : low_temps.getStationData(station_id)) {
					int longest_ffp_value = -9999;
					int counter = 0;
					int missing = 0;
					
					// iterate through one year of temperature data
					for (float d : year_of_data) {
						// check to see if missing has maxed out
						if (missing >= max_missing) {
							builder.add(DataType.MISSING);
							continue year_loop;
						}
						
						// if we hit an invalid data point or the temp <= 28 then reset the counter and continue on
						if (d == DataType.MISSING || d == DataType.NONEXISTANT || d == DataType.OUTSIDE_OF_RANGE || d == DataType.ERROR_RESULT) {
							missing++;
							counter++;
							continue;
						} else if (d > 28.0) {
							counter++;
							continue;
						}
						
						// this is only reached when the temp got below 28
						if (counter > longest_ffp_value) {
							longest_ffp_value = counter;
						}
						counter = 0;
					}
					
					builder.add(longest_ffp_value == -9999 ? 0 : longest_ffp_value);
				}
				
				builder.writeStation();
			}
		} catch (InvalidStateException ise) {
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		}
		
		return builder.returnCollection();
	}

	/**
	 * @see ClimateDataQuery#getGrowingDegreeDays(List, CalendarPeriod, DateTime, DateTime)
	 */
	 public CalendarDataCollection getGrowingDegreeDays(java.util.List<String> stations, CalendarPeriod period, DateTime starting, DateTime ending, 
			float minBaseTemp, float maxBaseTemp) throws RemoteException, InvalidStateException, InvalidArgumentException {
		
		// validate the period requested
		switch (period) {
		case DAILY:
		case WEEKLY:
		case MONTHLY:
		case ANNUALLY:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for period " + period.name());	
		}
				
		int years = ending.getYear() - starting.getYear() + 1;
		DateTime gdd_start = new DateTime(starting.getYear(), 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime gdd_end = new DateTime(ending.getYear(), 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		YearDataBuilder builder = new YearDataBuilder(starting, ending, period, DataType.GDD);
		
		// data is queried from the start of the first year to the end of the last, to ensure validation checks work correctly
		CalendarDataCollection low_temp = getPeriodDailyData(stations, gdd_start, gdd_end, DataType.LOW_TEMP);
		CalendarDataCollection high_temp = getPeriodDailyData(stations, gdd_start, gdd_end, DataType.HIGH_TEMP);
		
		for (String station : low_temp) {
			
			float[][] low_data = low_temp.getDataMatrix(station);
			float[][] high_data = high_temp.getDataMatrix(station);
			int periods = CalendarPeriod.DAILY.getLength();
			float[] gdd_data = new float[years * periods];
			
			// calculate all GDD values for the station
			for (int i=0; i<years; i++) {				
				for (int j=0; j<periods; j++) {
					
					float low = low_data[i][j];
					float high = high_data[i][j];

					// validate data values
					if (low == DataType.MISSING || low == DataType.NONEXISTANT || high == DataType.MISSING || high == DataType.NONEXISTANT) {
						gdd_data[i*periods + j] = DataType.MISSING;
					} else if (low == DataType.ERROR_RESULT || high == DataType.ERROR_RESULT) {
						gdd_data[i*periods + j] = DataType.ERROR_RESULT;
					} else if (low == DataType.OUTSIDE_OF_RANGE || high == DataType.OUTSIDE_OF_RANGE) {
						gdd_data[i*periods + j] = DataType.OUTSIDE_OF_RANGE;
					} else if (high < minBaseTemp || low > maxBaseTemp) {
						gdd_data[i*periods + j] = 0.0f;
					} else {	
						//calculate the daily GDD value.  If the daily high is less than the minBaseTemp then daily GDD = 0
						high = Math.min(high, maxBaseTemp);
						low = Math.max(low, minBaseTemp);
						gdd_data[i*periods + j] = ((low + high) / 2f) - (float)minBaseTemp;
					}
					
				}
			}
			
			// add all GDD values to the builder
			CalendarDataParser parser = new CalendarDataParser(gdd_data, starting.getYear(), 1, 1);
			
			// skip ahead in the parser to the start of the request
			int starting_doy = starting.getDayOfYear() - 1;
			while (parser.getDayOfYear() < starting_doy) {
				parser.nextDay();
			}
			
			// after the GDD values are calculated, put them into the builder
			builder.openStation(station);
			if (period == CalendarPeriod.DAILY) {
				while (parser.hasNextDay() && !builder.isLimitReached()) {
					float val = parser.nextDay();
					if ( val != DataType.MISSING && val != DataType.ERROR_RESULT && val != DataType.NONEXISTANT && val != DataType.OUTSIDE_OF_RANGE && val < 0 ) { 
						val = 0;
					}
					builder.add(val);
				}
			} else if (period == CalendarPeriod.WEEKLY) {
				while (parser.hasNextWeek() && !builder.isLimitReached()) {
					float val = parser.nextWeekSum();
					if ( val != DataType.MISSING && val != DataType.ERROR_RESULT && val != DataType.NONEXISTANT && val != DataType.OUTSIDE_OF_RANGE && val < 0 ) { 
						val = 0;
					}
					builder.add(val);
				}
			} else if (period == CalendarPeriod.MONTHLY) {
				while (parser.hasNextMonth() && !builder.isLimitReached()) {
					float val = parser.nextMonthSum();
					if ( val != DataType.MISSING && val != DataType.ERROR_RESULT && val != DataType.NONEXISTANT && val != DataType.OUTSIDE_OF_RANGE && val < 0 ) { 
						val = 0;
					}
					builder.add(val);
				}
			} else if ( period == CalendarPeriod.ANNUALLY ) {
				while ( parser.hasNextYear() && !builder.isLimitReached() ) {
					int annual_gdd = 0;
					int actual_count = 0;
					
					int missing_count = 0;
					int error_count = 0;
					int out_count = 0;
					
					double val = 0;
					for ( int i=0; i<12; i++ ) {
						val = parser.nextMonthSum();
						if ( val == DataType.MISSING ) { 
							missing_count++;
						} else if ( val == DataType.OUTSIDE_OF_RANGE ) { 
							out_count++;
						} else if ( val == DataType.ERROR_RESULT ) { 
							error_count++;
						} else {
							actual_count++;
							annual_gdd += val;
						}						
					}
					if ( annual_gdd != DataType.MISSING && annual_gdd != DataType.ERROR_RESULT && annual_gdd != DataType.NONEXISTANT && annual_gdd != DataType.OUTSIDE_OF_RANGE && annual_gdd < 0 ) { 
						annual_gdd = 0;
					}
					
					if ( actual_count > 10 ) {
						builder.add(annual_gdd);
					} else if ( missing_count >= error_count && missing_count >= out_count ) { 
						builder.add(DataType.MISSING);
					} else if ( out_count >= missing_count ) { 
						builder.add(DataType.OUTSIDE_OF_RANGE);
					} else { 
						builder.add(DataType.ERROR_RESULT);
					}
				}
			}
			
			builder.writeStation();
		}
			
		return builder.returnCollection();
	}
	
	/**
	 * @throws InvalidStateException 
	 * @see ClimateDataQuery#getTemperatureDays(List, DateTime, DateTime, TemperatureDayTypes)
	 */
	 public CalendarDataCollection getTemperatureDays(List<String> stationIds, DateTime starting, DateTime ending, CalendarPeriod period, TemperatureDayTypes type, int tempRef) 
	throws RemoteException, InvalidStateException, InvalidArgumentException {
		
		// validate the period requested		
		switch (period) {
		case WEEKLY:
		case MONTHLY:
		case ANNUALLY:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for period " + period.name());	
		}
		
		Float threshold = null;
		try {
			threshold = CoreServiceManager.getInstance().getDefaultThreshold();
		} catch (RemoteException jme) {
			LOG.error("error getting the default threshold value", jme);
			throw new RemoteException("exception getting the default threshold value");
		} catch (InstantiationException ie) {
			LOG.error("error instantiating the data service", ie);
			throw new RemoteException("could not instantiate the data service");
		}
		int max_missing = (int) ((1.0f - threshold) * CalendarPeriod.DAILY.getLength());

		DataType data_type = null;
		switch (type) {
		case HIGH_GREATER_THAN:
		case HIGH_LESS_THAN:
			data_type = DataType.HIGH_TEMP;
			break;
		case LOW_GREATER_THAN:
		case LOW_LESS_THAN:
			data_type = DataType.LOW_TEMP;
		}

		DateTime clean_start = new DateTime(starting.getYear(), 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime clean_end = new DateTime(ending.getYear(), 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		int periods = CalendarPeriod.DAILY.getLength();
		int years = ending.getYear() - starting.getYear() + 1;
		CalendarDataCollection data = getPeriodDailyData(stationIds, clean_start, clean_end, data_type);
		
		YearDataBuilder builder = new YearDataBuilder(clean_start, clean_end, period, DataType.GDD);
		
		for (String station : data) {
			float[][] temp_data = data.getDataMatrix(station);
			float[] temp_days = new float[years * periods];
			
			int index = 0;
			
			// put binary values into temp_days array
			for (int i=0; i<years; i++) {

				int missing_count = 0;
				int error_count = 0;
				for (int j=0; j<periods; j++) {
					
					// if we are over the limit for our invalid counts, fill in the period with DataType.MISSING values
					if (missing_count >= max_missing) {
						temp_days[index++] = DataType.MISSING;
						continue;
					} else if (error_count >= max_missing) {
						temp_days[index++] = DataType.ERROR_RESULT;
						continue;
					}

					float temp = temp_data[i][j];
					if (temp == DataType.ERROR_RESULT) {
						temp_days[index++] = DataType.ERROR_RESULT;
						error_count++;
						continue;
					} else if (temp == DataType.MISSING) {
						temp_days[index++] = DataType.MISSING;
						missing_count++;
						continue;
					} else if (temp == DataType.OUTSIDE_OF_RANGE) {
						temp_days[index++] = DataType.OUTSIDE_OF_RANGE;
						continue;
					} else if (temp == DataType.NONEXISTANT) {
						// Skip NONEXISTANT days without incrementing index.  CalendarDataParser does not expect to be parsing over data
						// that includes NONEXISTANT days, so we do not include those days in our data array.
						continue;
					}
					
					
					switch(type) {
					case HIGH_GREATER_THAN:
						temp_days[index++] = temp > tempRef ? 1 : 0;
						break;
					case HIGH_LESS_THAN:
						temp_days[index++] = temp < tempRef ? 1 : 0;
						break;
					case LOW_GREATER_THAN:
						temp_days[index++] = temp > tempRef ? 1 : 0;
						break;
					case LOW_LESS_THAN:
						temp_days[index++] = temp < tempRef ? 1 : 0;
						break;
					}
				}
			}
			
			// summarize the temp_days array using a calendar data parser
			CalendarDataParser parser = new CalendarDataParser(temp_days, starting.getYear(), 1, 1);
			builder.openStation(station);
			if (period == CalendarPeriod.WEEKLY) {
				while (parser.hasNextWeek() && !builder.isLimitReached()) {
					builder.add(parser.nextWeekSum());
				}
			} else if (period == CalendarPeriod.MONTHLY) {
				while (parser.hasNextMonth() && !builder.isLimitReached()) {
					builder.add(parser.nextMonthSum());
				}
			} else if (period == CalendarPeriod.ANNUALLY) {
				while (parser.hasNextYear() && !builder.isLimitReached()) {
					// sum the years worth of monthly sums
					float sum = 0.0f;
					while (parser.hasNextMonth()) {
						if (parser.getMonthOfYear() == 12) {
							builder.add(sum);
							sum = 0.0f;
						}
						sum += parser.nextMonthSum();
					}
					
					// add the final sum value to the builder
					if (sum != 0.0) {
						builder.add(sum);
					}
				}
			}
			builder.writeStation();
		}
		
		return builder.returnCollection();
	}
	
	 public CalendarDataCollection getAnnualExtremeTemp(List<String> stationIds,int firstYear, int lastYear ,DataType type) throws RemoteException, InvalidStateException, InvalidArgumentException {

		DateTime start = new DateTime(firstYear, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(lastYear, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		YearDataBuilder builder = new YearDataBuilder(start, end, CalendarPeriod.ANNUALLY, DataType.FFP);

		CalendarDataCollection data = getPeriodDailyData(stationIds, start,end, type);
		
		for ( String station : stationIds ) {
			builder.openStation(station);
			/*Set the iterator depending on the TemperatureDayTypes*/
			float temp= 0 ; 
			for(float [] tempcnt : data.getStationData(station)) {
				/*Setting the intial value to start comparing with . The check for missing data and non-existent data is done as it will cause a problem when calculation
				 * lowest temperature and it will always return -99 or -100 */
				for(int j = 0 ; j<366; j++){
					temp = tempcnt[j];
					if((temp!=DataType.MISSING)&&(temp!=DataType.NONEXISTANT) && (temp != DataType.ERROR_RESULT) && (temp != DataType.OUTSIDE_OF_RANGE)){
						break;
					}
				}
				for(int cntofdays=0; cntofdays<366;cntofdays++){
					if((tempcnt[cntofdays]!=DataType.MISSING)&&(tempcnt[cntofdays]!=DataType.NONEXISTANT) 
							&& (tempcnt[cntofdays] != DataType.ERROR_RESULT) && (tempcnt[cntofdays] != DataType.OUTSIDE_OF_RANGE)){
						switch(type){
							case HIGH_TEMP:
								if(tempcnt[cntofdays]>temp){
									temp = tempcnt[cntofdays];
								}
								break;
							case LOW_TEMP:
								if(tempcnt[cntofdays]<temp){
									temp = tempcnt[cntofdays];
								}
								break; 
						}
					}
				}
				builder.add(temp); 
			}
			builder.writeStation();
		}
		return builder.returnCollection(); 
	}
	 
	public CalendarDataCollection getAnnualDataNormals(List<String> stationIds, int startYear, int endYear, DataType type) throws RemoteException, InvalidArgumentException {
		DateTime startDate = new DateTime(startYear, 1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime endDate = new DateTime(endYear, 12,31,0,0,0,0,GregorianChronology.getInstance());
		
		CalendarDataCollection period_data = getPeriodAnnualData(stationIds, startDate, endDate, type);
		
		try {
			return ExtendedDataCalculations.computeAveragePeriodData(period_data);
		} catch ( InvalidStateException ise ) { 
			LOG.error("could not compute data normals, the object state is invalid", ise);
			RemoteException re = new RemoteException();
			re.initCause(ise);
			
			throw re;
		}
	}

	public CalendarDataCollection getFrostFreePeriodNormals(List<String> stationIds, int startYear, int endYear) throws RemoteException, InvalidArgumentException {		
		CalendarDataCollection period_data = getFrostFreePeriod(stationIds, startYear, endYear);
		
		try {
			return ExtendedDataCalculations.computeAveragePeriodData(period_data);
		} catch ( InvalidStateException ise ) { 
			LOG.error("could not compute data normals, the object state is invalid", ise);
			RemoteException re = new RemoteException();
			re.initCause(ise);
			
			throw re;
		}
	}

	public CalendarDataCollection getGrowingDegreeDayNormals(List<String> stationIds, CalendarPeriod summary, int startYear, int endYear, float minDailyTemp, float maxDailyTemp) throws RemoteException, InvalidArgumentException, InvalidStateException {
		// validate the period requested
		switch (summary) {
		case DAILY:
		case WEEKLY:
		case MONTHLY:
		case ANNUALLY:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for period " + summary.name());	
		}
		
		DateTime startDate = new DateTime(startYear, 1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime endDate = new DateTime(endYear, 12,31,0,0,0,0,GregorianChronology.getInstance());
		
		CalendarDataCollection period_data = getGrowingDegreeDays(stationIds, summary, startDate, endDate, minDailyTemp, maxDailyTemp);
		
		try {
			return ExtendedDataCalculations.computeAveragePeriodData(period_data);
		} catch ( InvalidStateException ise ) { 
			LOG.error("could not compute data normals, the object state is invalid", ise);
			RemoteException re = new RemoteException();
			re.initCause(ise);
			
			throw re;
		}
	}

	/**
	 * A method for populating an entire station result with missing values over the temporal period
	 * requested.
	 * 
	 * @param builder the <i>PeriodOrderedDataBuilder</i> containing the results from the caller method, 
	 * this object is updated in this method.
	 * 
	 * @throws InvalidStateException
	 */
	private void emptyResult(YearDataBuilder builder, DateTime from, boolean isDaily) throws InvalidStateException { 
		/* populate the entire range of expected values with the missing value */
		if ( isDaily ) {
			// when the builder is doing daily data, February 29th needs to be accounted for
			DateTime clock = new DateTime(from);
			while (! builder.isLimitReached()) {
				if (!clock.year().isLeap() && clock.getDayOfYear() == 59) {
					builder.add(DataType.NONEXISTANT);
					if ( builder.isLimitReached() ) {
						break;
					}
				}
				
				builder.add(DataType.MISSING);
				clock = clock.plusDays(1);
			}
		} else {
			// for weekly, monthly, and annually there are no special cases to account for
			while ( ! builder.isLimitReached() ) {
				builder.add(DataType.MISSING);
			}
		}
		
		builder.writeStation();
	}
	
	/**
	 * This method is used to populate the outlying regions of a data request with missing values.  Queries to the object can be made which 
	 * fall outside of the range of data for a station, this logic is invoked for such stations, allowing a uniform temporal result to be 
	 * returned for stations which do not have a uniform data range. 
	 * 
	 * @param begin_year
	 * @param end_year
	 * @param begin_period
	 * @param end_period
	 * @param type
	 * @param builder
	 * @throws InvalidStateException
	 */
	private void missingPopulate(int begin_year, int end_year, int begin_period, int end_period, CalendarPeriod type, YearDataBuilder builder) throws InvalidStateException {
		while ( true ) {
			while ( begin_period < end_period ) { 
				builder.add(DataType.OUTSIDE_OF_RANGE);
				begin_period++;
			}
		
			while ( begin_year < end_year ) { 
				while ( begin_period <= type.getLength() ) { 
					builder.add(DataType.OUTSIDE_OF_RANGE);
					begin_period++;
				}
				
				begin_year++;
				begin_period = 1;
			} 
			
			if ( begin_year == end_year && begin_period==end_period )
				return;
		}
	}
	
	private CalendarDataCollection getDailyDataNormals(List<String> stationIds, int startYear, int endYear, DataType type) throws RemoteException, InvalidArgumentException {
		switch ( type ) {
		case PRECIP:
			break;
		case LOW_TEMP:
			break;
		case HIGH_TEMP:
			break;
		case NORMAL_TEMP:
			break;
		case WSAVG_3M:
			break;
		case SOILTAVG_10:
			break;
		case RHAVG:
			break;
		case SOLARRAD_LANGLEY:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for type "+type.name());		
		}
		
		DateTime startDate = new DateTime(startYear, 1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime endDate = new DateTime(endYear, 12,31,0,0,0,0,GregorianChronology.getInstance());
		
		CalendarDataCollection period_data = getPeriodDailyData(stationIds, startDate, endDate, type);
		
		try {
			return ExtendedDataCalculations.computeAveragePeriodData(period_data);
		} catch ( InvalidStateException ise ) { 
			LOG.error("could not compute data normals, the object state is invalid", ise);
			RemoteException re = new RemoteException();
			re.initCause(ise);
			
			throw re;
		}
	}

	private CalendarDataCollection getMonthlyDataNormals(List<String> stationIds, int startYear, int endYear, DataType type) throws RemoteException, InvalidArgumentException {
		/* enforces a specific set of types which can be queried */
		switch ( type ) {
		case PRECIP:
			break;
		case NORMAL_TEMP:
			break;
		case HIGH_TEMP:
			break;
		case LOW_TEMP:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for type "+type.name());		
		}
		
		DateTime startDate = new DateTime(startYear, 1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime endDate = new DateTime(endYear, 12,31,0,0,0,0,GregorianChronology.getInstance());
		
		CalendarDataCollection period_data = getPeriodMonthlyData(stationIds, startDate, endDate, type);
		
		try {
			return ExtendedDataCalculations.computeAveragePeriodData(period_data);
		} catch ( InvalidStateException ise ) { 
			LOG.error("could not compute data normals, the object state is invalid", ise);
			RemoteException re = new RemoteException();
			re.initCause(ise);
			
			throw re;
		}
	}

	private CalendarDataCollection getWeeklyDataNormals(List<String> stationIds, int startYear, int endYear, DataType type) throws RemoteException, InvalidArgumentException {
		/* enforces a specific set of types which can be queried */
		switch ( type ) {
		case PRECIP:
			break;
		case NORMAL_TEMP:
			break;
		case HIGH_TEMP:
			break;
		case LOW_TEMP:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for type "+type.name());		
		}
		
		DateTime startDate = new DateTime(startYear, 1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime endDate = new DateTime(endYear, 12,31,0,0,0,0,GregorianChronology.getInstance());
		
		CalendarDataCollection period_data = getPeriodWeeklyData(stationIds, startDate, endDate, type);
		
		try {
			return ExtendedDataCalculations.computeAveragePeriodData(period_data);
		} catch ( InvalidStateException ise ) { 
			LOG.error("could not compute data normals, the object state is invalid", ise);
			RemoteException re = new RemoteException();
			re.initCause(ise);
			
			throw re;
		}
	}
	
	 private CalendarDataCollection getPeriodAnnualData(List<String> stationIds, DateTime firstDate, DateTime lastDate, DataType type) 
	throws RemoteException, InvalidArgumentException {
		
	switch ( type ) {
		case PRECIP:
			break;
		case NORMAL_TEMP:
			break;
		case HIGH_TEMP:
			break;
		case LOW_TEMP:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for type "+type.name());		
		}
		Connection conn = null;
		YearDataBuilder builder = new YearDataBuilder(firstDate, lastDate, CalendarPeriod.ANNUALLY, type);
		int first_year = firstDate.getYear();
		int last_year = lastDate.getYear();		
		
		/* adjust for 53rd week */
		int first_week = firstDate.getWeekOfWeekyear();
		int last_week = lastDate.getWeekOfWeekyear();
		int begin_month = firstDate.getMonthOfYear();
		int end_month = lastDate.getMonthOfYear();		
						
		if ( first_week == 53 ) {
			if ( begin_month == 1 ) {
				first_week = 1;
			} else {
				first_week = 52; 
			}
		} else if ( first_week == 52 && begin_month == 1 ) {
			first_week = 1;
		} else if ( first_week == 1 && begin_month == 12 ) {
			first_week = 52;
		}
		
		if ( last_week == 53 ) {
			if ( end_month == 1 ) {
				last_week = 1;
			} else {
				last_week = 52; 
			}
		} else if ( last_week == 52 && end_month == 1 ) {
			last_week = 1;
		} else if ( last_week == 1 && end_month == 12 ) { 
			last_week = 52;
		}
		
		ResultSet query_result = null;
		PreparedStatement stmt = null;
		try { 
			conn = source.getConnection();
			// updated ANNUAL_BASE query  jira FARM-445
			if(type==DataType.PRECIP){
				stmt = conn.prepareStatement(ClimateQueryStrings.ANNUAL_BASE.getQueryString());
			} else if ( type == DataType.HIGH_TEMP ) {
				stmt = conn.prepareStatement(ClimateQueryStrings.ANNUAL_BASE_AVG.getQueryString());
			} else if ( type == DataType.LOW_TEMP ) {
				stmt = conn.prepareStatement(ClimateQueryStrings.ANNUAL_BASE_AVG.getQueryString());
			}else{	//type==DataType.NORMAL_TEMP
				stmt = conn.prepareStatement(ClimateQueryStrings.ANNUAL_BASE_AVG.getQueryString());
			}
			stmt.setString(2, type.name());
			stmt.setString(11, type.name());
			
			for ( String station : stationIds ) { 
				builder.openStation(station);
				stmt.setString(1, station);
				stmt.setInt(3, first_year);
				stmt.setInt(4, first_year);
				stmt.setInt(5, first_week);
				stmt.setInt(6, last_year);
				stmt.setInt(7, last_year);
				stmt.setInt(8, last_week);
				stmt.setInt(9, DataType.WEEKS_IN_YEAR_THRESHOLD);
				

				stmt.setInt(10, first_year);	//start of the year range
				stmt.setInt(11, last_year); //end of the range of years
	
				query_result = stmt.executeQuery();
				
				if ( !query_result.next() ) {
					emptyResult(builder, firstDate, false);
					continue;
				}
				do { 
					builder.add(query_result.getFloat(1));
				} while ( query_result.next() );
				
				query_result.last();
				builder.writeStation();
			}
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying data for type "+type.name(), sqe);
			throw new RemoteException("unable to query data from data base for type "+type.name());
		} catch (  InvalidStateException ise ) { 
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally { 
			try {
				if ( query_result != null ) {
					query_result.close();
				}
				
				if ( stmt != null ) {
					stmt.close();
				}
				
				if ( conn != null ) {
					conn.close();
				}
			} catch ( SQLException sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
		return builder.returnCollection();
	}

	 private CalendarDataCollection getPeriodDailyData(List<String> stationIds, DateTime firstDate, DateTime lastDate, DataType type) 
	throws RemoteException, InvalidArgumentException {
		
		/* enforces a specific set of types which can be queried */
		switch ( type ) {
		case PRECIP:
			break;
		case LOW_TEMP:
			break;
		case HIGH_TEMP:
			break;
		case NORMAL_TEMP:
			break;
		case WSAVG_3M:
			break;
		case SOILTAVG_10:
			break;
		case RHAVG:
			break;
		case SOLARRAD_LANGLEY:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for type "+type.name());		
		}
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet query_result = null;
		YearDataBuilder builder = new YearDataBuilder(firstDate, lastDate, CalendarPeriod.DAILY, type);
		try { 
			conn = source.getConnection();
			stmt = conn.prepareStatement(ClimateQueryStrings.DAILY_BASE.getQueryString());
			stmt.setString(2, type.name());
			stmt.setInt(3, firstDate.getYear());
			stmt.setInt(4, firstDate.getYear());
			stmt.setInt(5, firstDate.getMonthOfYear());
			stmt.setInt(6, firstDate.getMonthOfYear());
			stmt.setInt(7, firstDate.getDayOfMonth());		
			stmt.setInt(8, lastDate.getYear());
			stmt.setInt(9, lastDate.getYear());
			stmt.setInt(10, lastDate.getMonthOfYear());
			stmt.setInt(11, lastDate.getMonthOfYear());
			stmt.setInt(12, lastDate.getDayOfMonth());
						
			for ( String station : stationIds ) { 
				builder.openStation(station);
				stmt.setString(1, station);
				query_result = stmt.executeQuery();
				
				if ( !query_result.next() ) {
					emptyResult(builder, firstDate, true);
					continue;
				}
				
				/* the query actual is the first date for the specific station on which data is reported this is the starting boundry */
				DateTime queryActual = new DateTime(query_result.getInt(3), query_result.getInt(2), query_result.getInt(1), 0, 0, 0, 0, GregorianChronology.getInstance());
				int real_day = firstDate.getDayOfYear();
				int actual_day = queryActual.getDayOfYear();
						
				/* if the year is not a leap year and the date is past 02/28
				 * the counter is incremented by 1 to adjust for the NON-EXISTANT
				 * value which the builder puts in the array (NOTE: all FIRM years have 366 days)
				 */
				if ( !(firstDate.year().isLeap()) && (real_day > 59) )
					real_day++;
				
				if ( !(queryActual.year().isLeap()) && (actual_day > 59) )
					actual_day++;
				
				missingPopulate(firstDate.getYear(), queryActual.getYear(), real_day, actual_day, CalendarPeriod.DAILY, builder);
				
				do { 
					builder.add(query_result.getFloat(4));
				} while ( query_result.next() );
				
				query_result.last();
				
				/* the ending data boundry for this station */
				queryActual = new DateTime(query_result.getInt(3), query_result.getInt(2), query_result.getInt(1), 0, 0, 0, 0, GregorianChronology.getInstance());
				real_day = lastDate.getDayOfYear();
				actual_day = queryActual.getDayOfYear();
				if ( !(lastDate.year().isLeap()) && (real_day > 59) )
					real_day++;
				
				if ( !(queryActual.year().isLeap()) && (actual_day > 59) )
					actual_day++;
				
				missingPopulate(queryActual.getYear(), lastDate.getYear(), actual_day, real_day, CalendarPeriod.DAILY, builder);
				builder.writeStation();
			}
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying data for type "+type.name(), sqe);
			throw new RemoteException("unable to query data from data base for type "+type.name());
		} catch (  InvalidStateException ise ) { 
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally { 
			try {
				if ( query_result != null ) {
					query_result.close();
				} 
				
				if ( stmt != null ) { 
					stmt.close();
				}
				
				if ( conn != null ) {
					conn.close();
				}
			} catch ( SQLException sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
		
		return builder.returnCollection();
	}

	 private CalendarDataCollection getPeriodMonthlyData(List<String> stationIds, DateTime firstDate, DateTime lastDate, DataType type) 
	throws RemoteException, InvalidArgumentException {
		/* enforces a specific set of types which can be queried */
		switch ( type ) {
		case PRECIP:
			break;
		case HIGH_TEMP:
			break;
		case LOW_TEMP:
			break;
		case NORMAL_TEMP:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for type "+type.name());		
		}
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet query_result = null;
		YearDataBuilder builder = new YearDataBuilder(firstDate, lastDate, CalendarPeriod.MONTHLY, type);
		try { 
			conn = source.getConnection();
			stmt = conn.prepareStatement(ClimateQueryStrings.MONTHLY_BASE.getQueryString());
			stmt.setString(2, type.name());
			stmt.setInt(3, firstDate.getYear());
			stmt.setInt(4, firstDate.getYear());
			stmt.setInt(5, firstDate.getMonthOfYear());
			stmt.setInt(6, lastDate.getYear());
			stmt.setInt(7, lastDate.getYear());
			stmt.setInt(8, lastDate.getMonthOfYear());
						
			for ( String station : stationIds ) { 
				builder.openStation(station);
				stmt.setString(1, station);
				query_result = stmt.executeQuery();
				
				if ( !query_result.next() ) { 
					emptyResult(builder, firstDate, false);
					continue;
				}
				
				missingPopulate(firstDate.getYear(), query_result.getInt(2), firstDate.getMonthOfYear(), query_result.getInt(1), CalendarPeriod.MONTHLY, builder);
				
				do { 
					builder.add(query_result.getFloat(3));
				} while ( query_result.next() );
				
				query_result.last();
				missingPopulate(query_result.getInt(2), lastDate.getYear(), query_result.getInt(1), lastDate.getMonthOfYear(), CalendarPeriod.MONTHLY, builder);
				
				
				builder.writeStation();
			}
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying data for type "+type.name(), sqe);
			throw new RemoteException("unable to query data from data base for type "+type.name());
		} catch ( InvalidStateException ise ) { 
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally { 
			try {
				if ( query_result != null ) {
					query_result.close();
				}
				
				if ( stmt != null ) {
					stmt.close();
				}
				
				if ( conn != null ) {
					conn.close();
				}
			} catch ( SQLException sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
		
		return builder.returnCollection();
	}

	 private CalendarDataCollection getPeriodWeeklyData(List<String> stationIds, DateTime firstDate, DateTime lastDate, DataType type) 
	throws RemoteException, InvalidArgumentException {
		
		/* enforces a specific set of types which can be queried */
		switch ( type ) {
		case PRECIP:
			break;
		case HIGH_TEMP:
			break;
		case LOW_TEMP:
			break;
		case NORMAL_TEMP:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for type "+type.name());		
		}
		
		Connection conn = null;
		YearDataBuilder builder = new YearDataBuilder(firstDate, lastDate, CalendarPeriod.WEEKLY, type);

		// make sure that the SQL query is using the values from the builder as it might change them
		int first_year = builder.getBegin().getYear();
		int last_year = builder.getEnd().getYear();		
		
		/* adjust for 53rd week
		 * this is done by using the values that were computed by the builder 
		 * when it determined the period ranges based on week calculations
		 */
		int first_week = builder.getPeriodStartValue();
		int last_week = builder.getPeriodEndValue();	
						
		PreparedStatement stmt = null;
		ResultSet query_result = null;
		try { 
			conn = source.getConnection();
			stmt = conn.prepareStatement(ClimateQueryStrings.WEEKLY_BASE.getQueryString());
			stmt.setString(2, type.name());
			stmt.setInt(3, first_year);
			stmt.setInt(4, first_year);
			stmt.setInt(5, first_week);
			stmt.setInt(6, last_year);
			stmt.setInt(7, last_year);
			stmt.setInt(8, last_week);
						
			for ( String station : stationIds ) { 
				builder.openStation(station);
				stmt.setString(1, station);
				query_result = stmt.executeQuery();
				
				if ( !query_result.next() ) { 
					emptyResult(builder, firstDate, false);
					continue;
				}
				
				missingPopulate(first_year, query_result.getInt(2), first_week, query_result.getInt(1), CalendarPeriod.WEEKLY, builder);
				
				do { 
					builder.add(query_result.getFloat(3));
				} while ( query_result.next() );
				
				query_result.last();
				missingPopulate(query_result.getInt(2), last_year, query_result.getInt(1), last_week, CalendarPeriod.WEEKLY, builder);
				
				builder.writeStation();
			}
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying data for type "+type.name(), sqe);
			throw new RemoteException("unable to query data from data base for type "+type.name());
		} catch ( InvalidStateException ise ) { 
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally { 
			try {
				if ( query_result != null ) { 
					query_result.close();
				}
				
				if ( stmt != null ) {
					stmt.close();
				}
				
				if ( conn != null ) {
					conn.close();
				}
			} catch ( SQLException sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
			
		}
		
		return builder.returnCollection();
	}

	 private CalendarDataCollection getAvailableAnnualData(List<String> stationIds, DataType type) throws RemoteException, InvalidArgumentException {
		return null;
	}

	 private CalendarDataCollection getAvailableDailyData(List<String> stationIds, DataType type) throws RemoteException, InvalidArgumentException {
		if ( type != null ) { 
			type = DataType.valueOf(type.name());
		}
		
		/* enforces a specific set of types which can be queried */
		switch ( type ) {
		case PRECIP:
			break;
		case LOW_TEMP:
			break;
		case HIGH_TEMP:
			break;
		case NORMAL_TEMP:
			break;
		case WSAVG_3M:
			break;
		case SOILTAVG_10:
			break;
		case RHAVG:
			break;
		case SOLARRAD_LANGLEY:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for type "+type.name());		
		}
		
		TemporalPeriod longest_period = null;
		try { 
			longest_period = metaDataQuery.getLongestPeriod(stationIds, CalendarPeriod.DAILY);
		} catch ( RemoteException jme ) { 
			LOG.error("could not get ClimateMetaDataObject", jme);
			throw new RemoteException("could not get ClimateMetaDataObject");
		} catch ( RuntimeException ie ) { 
			LOG.error("could not instantiate a DataServiceAccessor", ie);
			throw new RemoteException("could not instantiate a DataServiceAccessor");
		}
		
		return this.getPeriodDailyData(stationIds, longest_period.getStart(), longest_period.getEnd(), type);
	}

	 private CalendarDataCollection getAvailableMonthlyData(List<String> stationIds, DataType type) throws RemoteException, InvalidArgumentException {
		
		/* enforces a specific set of types which can be queried */
		switch ( type ) {
		case PRECIP:
			break;
		case HIGH_TEMP:
			break;
		case LOW_TEMP:
			break;
		case NORMAL_TEMP:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for type "+type.name());		
		}
		
		TemporalPeriod longest_period = null;
		try { 
			longest_period = metaDataQuery.getLongestPeriod(stationIds, CalendarPeriod.MONTHLY);
		} catch ( RemoteException jme ) { 
			LOG.error("could not get ClimateMetaDataObject", jme);
			throw new RemoteException("could not get ClimateMetaDataObject");
		} catch ( RuntimeException ie ) { 
			LOG.error("could not instantiate a DataServiceAccessor", ie);
			throw new RemoteException("could not instantiate a DataServiceAccessor");
		}
		
		return this.getPeriodMonthlyData(stationIds, longest_period.getStart(), longest_period.getEnd(), type);
	}

	 private CalendarDataCollection getAvailableWeeklyData(List<String> stationIds, DataType type) throws RemoteException, InvalidArgumentException {
		
		/* enforces a specific set of types which can be queried */
		switch ( type ) {
		case PRECIP:
			break;
		case HIGH_TEMP:
			break;
		case LOW_TEMP:
			break;
		case NORMAL_TEMP:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for type "+type.name());		
		}

		TemporalPeriod longest_period = null;
		try { 
			longest_period = metaDataQuery.getLongestPeriod(stationIds, CalendarPeriod.WEEKLY);
		} catch ( RemoteException jme ) { 
			LOG.error("could not get ClimateMetaDataObject", jme);
			throw new RemoteException("could not get ClimateMetaDataObject");
		} catch ( RuntimeException ie ) { 
			LOG.error("could not instantiate a DataServiceAccessor", ie);
			throw new RemoteException("could not instantiate a DataServiceAccessor");
		}
				
		return getPeriodWeeklyData(stationIds, longest_period.getStart(), longest_period.getEnd(), type);
	}	
	
	 private CalendarDataCollection getHistoricalAverageAnnualData(List<String> stationIds, DataType type) throws RemoteException, InvalidArgumentException {
		
		/* enforces a specific set of types which can be queried */
		switch ( type ) {
		case PRECIP:
			break;
		case NORMAL_TEMP:
			break;
		case HIGH_TEMP:
			break;
		case LOW_TEMP:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for type "+type.name());		
		}
			
		Connection conn = null;
		PreparedStatement stmt = null;
		try { 
			conn = source.getConnection();

			stmt = conn.prepareStatement(ClimateQueryStrings.AVERAGE_DAILY_BASE.getQueryString());
			CalendarDataCollection cdc = ExtendedDataCalculations.computeAveragePeriodData(stmt, type, CalendarPeriod.DAILY, stationIds);
			
			DateTime begin = new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
			DateTime end = new DateTime(2000, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
			YearDataBuilder builder = new YearDataBuilder(begin, end, CalendarPeriod.ANNUALLY, type);
			
			for ( String station : cdc ) {
				builder.openStation(station);
				float avg = 0;
				for ( float d : cdc.getDataMatrix(station)[0] ) { 
					avg += d;
				}
				builder.add(avg/366);
				builder.writeStation();
			}
			
			return builder.returnCollection();
			
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying data for type "+type.name(), sqe);
			throw new RemoteException("unable to query data from data base for type "+type.name());
		} catch ( InvalidStateException ise ) { 
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally { 
			try {
				stmt.close();
				conn.close();
			} catch ( SQLException sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
	}

	 private CalendarDataCollection getHistoricalAverageDailyData(List<String> stationIds, DataType type) throws RemoteException, InvalidArgumentException {
		/* enforces a specific set of types which can be queried */
		switch ( type ) {
		case PRECIP:
			break;
		case NORMAL_TEMP:
			break;
		case HIGH_TEMP:
			break;
		case LOW_TEMP: 
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for type "+type.name());		
		}
			
		Connection conn = null;
		PreparedStatement stmt = null;
		try { 
			conn = source.getConnection();
			stmt = conn.prepareStatement(ClimateQueryStrings.AVERAGE_DAILY_BASE.getQueryString());
			return ExtendedDataCalculations.computeAveragePeriodData(stmt, type, CalendarPeriod.DAILY, stationIds);
			
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying data for type "+type.name(), sqe);
			throw new RemoteException("unable to query data from data base for type "+type.name());
		} catch ( InvalidStateException ise ) { 
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally { 
			try {
				stmt.close();
				conn.close();
			} catch ( SQLException sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
	}

	 private CalendarDataCollection getHistoricalAverageMonthlyData(List<String> stationIds, DataType type) throws RemoteException, InvalidArgumentException {
		/* enforces a specific set of types which can be queried */
		switch ( type ) {
		case PRECIP:
			break;
		case HIGH_TEMP:
			break;
		case LOW_TEMP:
			break;
		case NORMAL_TEMP:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for type "+type.name());		
		}
			
		PreparedStatement stmt = null;
		Connection conn = null;
		try { 
			conn = source.getConnection();
			stmt = conn.prepareStatement(ClimateQueryStrings.AVERAGE_MONTHLY_BASE.getQueryString());
			return ExtendedDataCalculations.computeAveragePeriodData(stmt, type, CalendarPeriod.MONTHLY, stationIds);
			
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying data for type "+type.name(), sqe);
			throw new RemoteException("unable to query data from data base for type "+type.name());
		} catch ( InvalidStateException ise ) { 
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally { 
			try {
				stmt.close();
				conn.close();
			} catch ( SQLException sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
	}

	 private CalendarDataCollection getHistoricalAverageWeeklyData(List<String> stationIds, DataType type) throws RemoteException, InvalidArgumentException {
		
		/* enforces a specific set of types which can be queried */
		switch ( type ) {
		case PRECIP:
			break;
		case HIGH_TEMP:
			break;
		case LOW_TEMP:
			break;
		case NORMAL_TEMP:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for type "+type.name());		
		}
		
		PreparedStatement stmt = null;
		Connection conn = null;
		try { 
			conn = source.getConnection();
			stmt = conn.prepareStatement(ClimateQueryStrings.AVERAGE_WEEKLY_BASE.getQueryString());
			return ExtendedDataCalculations.computeAveragePeriodData(stmt, type, CalendarPeriod.WEEKLY, stationIds);
			
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying data for type "+type.name(), sqe);
			throw new RemoteException("unable to query data from data base for type "+type.name());
		} catch ( InvalidStateException ise ) { 
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally { 
			try {
				stmt.close();
				conn.close();
			} catch ( SQLException sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
	}
}
