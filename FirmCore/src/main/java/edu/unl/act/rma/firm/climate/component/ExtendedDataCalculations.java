/* Created On: Sep 12, 2005 */
package edu.unl.act.rma.firm.climate.component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.InvalidStateException;
import edu.unl.act.rma.firm.core.YearDataBuilder;
import edu.unl.act.rma.firm.core.configuration.CoreServiceManager;

/**
 * A delegate class used for abstracting computational logic away from the ClimateDataBean, extending the bean to provide computational climatic values
 * in addition to queried values.
 * 
 * @author Ian Cottingham
 */
public final class ExtendedDataCalculations {
	
	
	/**
	 * Generates a single "year" of data containing the per-period average for the historical period of record. 
	 * 
	 * @param stmt - the query statement for the period type being computed
	 * @param type
	 * @param period
	 * @param stationIds
	 * @return A single year CalendarDataCollection of all requested stations.
	 * 
	 * @throws SQLException
	 * @throws InvalidStateException
	 * @throws InvalidArgumentException
	 */
	protected static CalendarDataCollection computeAveragePeriodData(PreparedStatement stmt, DataType type, CalendarPeriod period, List<String> stationIds) throws SQLException, InvalidStateException, InvalidArgumentException {
		
		// weekly dates should ensure that the first value falls in the first week
		DateTime begin = ( period == CalendarPeriod.WEEKLY ) ? new DateTime(2000, 1, 3, 0, 0, 0, 0, GregorianChronology.getInstance()) :
			new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		DateTime end = new DateTime(2000, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		YearDataBuilder builder = new YearDataBuilder(begin, end, period, type);
		stmt.setString(2, type.name());
				
		ResultSet query_result = null;
		try {
			for ( String station : stationIds ) { 
				builder.openStation(station);
				stmt.setString(1, station);
				query_result = stmt.executeQuery();
				DateTime counter = begin.toDateTime();
				int pos = 0;
				
				if ( (counter.getWeekOfWeekyear() == 52) && (period == CalendarPeriod.WEEKLY) ) {
					counter = counter.plusWeeks(1);
				}
				
				while ( query_result.next() ) { 	
					switch ( period ) { 
						case DAILY:
							pos++; 
							if ( query_result.getInt(2) == counter.getDayOfMonth() ) {
								builder.add(query_result.getFloat(3));
							} else { 
								builder.add(DataType.MISSING);
							}
							counter = counter.plusDays(1);
							break;
						case WEEKLY: 
							pos++;
							if ( query_result.getInt(1) == counter.getWeekOfWeekyear() ) {
								builder.add(query_result.getFloat(2));
							} else { 
								builder.add(DataType.MISSING);
							}
							counter = counter.plusWeeks(1);
							break;
						case MONTHLY:
							pos ++;
							if ( query_result.getInt(1) == counter.getMonthOfYear() ) {
								builder.add(query_result.getFloat(2));
							} else { 
								builder.add(DataType.MISSING);
							}
							counter = counter.plusMonths(1);
							break;
						case ANNUALLY:
							pos++;
							builder.add(query_result.getFloat(1));
							break;
					}
				}
				
				while ( pos != period.getLength() ) { 
					builder.add(DataType.MISSING);
					pos++;
				}
				
				builder.writeStation();
			}
		} catch ( SQLException e1 ) { 
			throw e1;
		} catch ( InvalidStateException e2 ) {
			throw e2;
		} catch ( InvalidArgumentException e3 ) {
			throw e3;
		} finally { 
			try {
				query_result.close();
			} catch ( Exception e ) { 
				// do nothing
			}
		}
		
		CalendarDataCollection collection = builder.returnCollection();
		return collection;
	}
	
	public static CalendarDataCollection computeAveragePeriodData(CalendarDataCollection cdc) throws InvalidStateException, InvalidArgumentException { 
	
		float threshold;
		try {
			threshold = CoreServiceManager.getInstance().getDefaultThreshold();
		} catch (Exception e) {
			RuntimeException rt = new RuntimeException();
			rt.initCause(e);
			throw rt;
		}

		CalendarPeriod period = cdc.getCollectionPeriodType();
		
		// weekly dates should ensure that the first value falls in the first week
		DateTime begin = ( period == CalendarPeriod.WEEKLY ) ? new DateTime(2000, 1, 3, 0, 0, 0, 0, GregorianChronology.getInstance()) :
			new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		DateTime end = new DateTime(2000, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		YearDataBuilder builder = new YearDataBuilder(begin, end, period, cdc.getDataType());
		
		float years = (cdc.getEnd().getYear() - cdc.getBegin().getYear())+1;
		int pos_total = 0;
		
		switch ( period ) { 
		case ANNUALLY:
			pos_total = 1;
			break;
		case WEEKLY:
			pos_total = 52;
			break;
		case MONTHLY: 
			pos_total = 12;
			break;
		case DAILY:
			pos_total = 366;
			break;
		default:
			throw new InvalidStateException("no valid period was defined");
		}
		
		int max_missing = Math.round((1.0f - threshold) * years);
		
		for ( String station : cdc ) { 
			builder.openStation(station);
			int[] missing_count = new int[pos_total];
			int[] error_count = new int[pos_total];
			int[] range_count = new int[pos_total];
		
				float[] result = new float[pos_total];
				for ( float[] data : cdc.getStationData(station) ) {
					int len = data.length;
					if ( len != pos_total ) { 
						throw new InvalidStateException("the data array length did not match the period length. len = "+len+" pos_total="+pos_total);
					}
					for ( int i=0; i<len; i++ ) { 
						if ( data[i] == DataType.MISSING ) { 
							missing_count[i]++;
						} else if ( data[i] == DataType.ERROR_RESULT ) { 
							error_count[i]++;
						} else if ( data[i] == DataType.OUTSIDE_OF_RANGE ) { 
							range_count[i]++;
						} else {
							result[i] += data[i];
						}
					}
				}
				
				for ( int i=0; i<pos_total; i++ ) { 
					if ( missing_count[i] > max_missing ) { 
						builder.add(DataType.MISSING);
					} else if ( error_count[i] > max_missing ) { 
						builder.add(DataType.ERROR_RESULT);
					} else if ( range_count[i] > max_missing ) { 
						builder.add(DataType.OUTSIDE_OF_RANGE);
					} else if ( (missing_count[i] + error_count[i] + range_count[i]) > max_missing ) {
						builder.add(DataType.MISSING);
					} else { 
						builder.add((result[i] / years));
					}
				}
				
			builder.writeStation();
		}
		
		return builder.returnCollection();
	}
}
