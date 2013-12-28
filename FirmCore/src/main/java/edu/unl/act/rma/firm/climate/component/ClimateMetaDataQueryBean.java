/* Created On: Sep 11, 2005 */
package edu.unl.act.rma.firm.climate.component;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.climate.VariableFilter;
import edu.unl.act.rma.firm.climate.VariableMetaData;
import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.InvalidStateException;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.MetaDataCollection;
import edu.unl.act.rma.firm.core.StationMetaDataType;
import edu.unl.act.rma.firm.core.StationSearchTerms;
import edu.unl.act.rma.firm.core.TemporalPeriod;

/**
 * @see edu.unl.act.rma.firm.climate.component.ClimateMetaDataQuery
 * 
 * @author Ian Cottingham
 *
 */
@Stateless
@Remote({ClimateMetaDataQuery.class})
public class ClimateMetaDataQueryBean implements ClimateMetaDataQuery {

	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG, ClimateMetaDataQueryBean.class);

	private static int ALLOWABLE_DAYS = 366;
	
	private static final String SQL_BASE = " from station, location_station_link, network_station_link, absolute_location, network_type, source_type where" +
			" absolute_location.location_id = location_station_link.location_id and location_station_link.station_id = station.station_id and network_type.network_type_id" +
			" = network_station_link.network_type_id and network_station_link.station_id = station.station_id ";
	
	private static final String VARIABLE_BASE = " select variable_name from variable where station_id = ?";
	
	private static final float LAT_LONG_DELTA = 0.1f;
	
	private DataSource source = DataSourceInjector.injectDataSource(DataSourceTypes.CLIMATIC); 

	@EJB(name="ClimateDataQuery", beanInterface=ClimateDataQuery.class)
	private ClimateDataQuery dataQuery;
		
	@EJB(name="ClimateSpatialExtension", beanInterface=ClimateSpatialExtension.class)
	private ClimateSpatialExtension spatialExtension;
	
	@WebMethod
	public MetaDataCollection<StationMetaDataType> getAllMetaData(List<String> stations, CalendarPeriod period) throws InvalidArgumentException, RemoteException {		
		Connection conn = null;
		
		Map<String,Map<StationMetaDataType, Object>> station_data = new HashMap<String, Map<StationMetaDataType, Object>>();
		
		PreparedStatement stmt = null;
		ResultSet meta_query_result = null;
		try { 
			conn = source.getConnection();

			String types = "";
			for ( StationMetaDataType type : StationMetaDataType.values() ) {
				types += ", "+type.getFieldName();
			}
			types = types.substring(2);
			
			stmt = conn.prepareStatement("select "+types+SQL_BASE+"and station.station_id = ?");
			
			for ( String station : stations ) { 
				stmt.setString(1, station);
				meta_query_result = stmt.executeQuery();			
				station_data.put(station, getMetaDataFromQuery(meta_query_result, period));
			}
			
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying meta data", sqe);
			throw new RemoteException("unable to query metadata from datasource");
		} catch ( Exception e ) { 
			LOG.error("unknown exception creating meta result", e);
			RemoteException re = new RemoteException();
			re.initCause(e);
			throw re;
		} finally { 
			try {
				if ( meta_query_result != null ) { 
					meta_query_result.close();
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

		return new MetaDataCollection<StationMetaDataType>(station_data);
	}

	@WebMethod
	public MetaDataCollection<StationMetaDataType> getMetaData(List<String> stations, StationMetaDataType field, CalendarPeriod period) throws InvalidArgumentException, RemoteException {
		
		Connection conn = null;
		Map<String, Object> meta_datum = new HashMap<String, Object>();
		
		MetaDataCollection<StationMetaDataType> ret = new MetaDataCollection<StationMetaDataType>();
		PreparedStatement stmt = null;
		ResultSet meta_query_result = null;
		try { 
			conn = source.getConnection();
			
			stmt = conn.prepareStatement("select "+field.getFieldName()+SQL_BASE+"and station.station_id = ?");
			
			for ( String station : stations ) { 
				stmt.setString(1, station);
				meta_query_result = stmt.executeQuery();
				
				while ( meta_query_result.next() ) {
					meta_datum.put(station, getTypedValue(meta_query_result, field, 1, period));
				}
			}
			
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying meta data", sqe);
			throw new RemoteException("unable to query metadata from datasource");
		} catch ( RuntimeException t ) { 
			LOG.error("unknown exception", t);
			throw new RemoteException("could not query meta data");
		} finally { 
			try {
				if ( meta_query_result != null ) { 
					meta_query_result.close();
				}
				
				if ( stmt != null ) { 
					stmt.close();
				}
				
				if ( conn != null ) {
					conn.close();
				}
			} catch ( Exception sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
		
		try {
			ret.importMap(field, meta_datum);
		} catch ( InvalidStateException ise ) { 
			LOG.error("could not import the map", ise);
			throw new RemoteException("the meta data collection is at an invalid state");
		}
		return ret;
	}
	
	@WebMethod
	public MetaDataCollection<StationMetaDataType> findStations(StationSearchTerms terms) throws RemoteException {
		Connection conn = null;
		ArrayList<String> station_ids = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		PreparedStatement city_stmt = null;
		ResultSet rs1 = null;
		try { 
			conn = source.getConnection();
			
			if ( terms.getStationID() != null ) {
				stmt = conn.prepareStatement("SELECT station_id FROM network_station_link WHERE network_id = ?");
				stmt.setString(1, terms.getStationID());
				rs = stmt.executeQuery();
				while (rs.next()) {
					String id = rs.getString(1);
					station_ids.add(id);
				}
			} else if ( terms.getZipCode() != null ) {	
				station_ids.addAll(spatialExtension.getStationsByZipCode(terms.getZipCode(), 10));				
			} else if ( terms.getCounty() != null ) {
				stmt = conn.prepareStatement("SELECT station_id FROM station WHERE UPPER(county) LIKE ?" + 
						(terms.getState() == null ? "" : " AND STATE = ?"));
				stmt.setString(1, terms.getCounty().toUpperCase() + "%");
				if ( terms.getState() != null ) {
					stmt.setString(2, terms.getState().name());
				}
				rs = stmt.executeQuery();
				while (rs.next()) {
					String id = rs.getString(1);
					if (! station_ids.contains(id)) {
						station_ids.add(id);
					}
				}
			} else if ( terms.getCity() != null ) {
				
				stmt = conn.prepareStatement("SELECT al.latitude, al.longitude FROM absolute_location al INNER JOIN location_station_link lsl ON " +
						"al.location_id = lsl.location_id INNER JOIN station s ON s.station_id = lsl.station_id WHERE UPPER(s.station_name) LIKE ?" + 
						(terms.getState() == null ? "" : " AND STATE = ?"));
				city_stmt = conn.prepareStatement("SELECT s.station_id FROM station s INNER JOIN location_station_link lsl ON " +
						"s.station_id = lsl.station_id INNER JOIN absolute_location al ON lsl.location_id = al.location_id WHERE al.latitude > ? AND " +
						"al.latitude < ? AND al.longitude > ? AND al.longitude < ?");
				stmt.setString(1, "%" + terms.getCity().toUpperCase() + "%");
				if ( terms.getState() != null ) {
					stmt.setString(2, terms.getState().name());
				}
				rs1 = stmt.executeQuery();
				while ( rs1.next() ) {
					float latitude = rs1.getFloat(1);
					float longitude = rs1.getFloat(2);

					// now for each station found by name, search in the same area (by lat/long)
					city_stmt.setFloat(1, latitude - LAT_LONG_DELTA);
					city_stmt.setFloat(2, latitude + LAT_LONG_DELTA);
					city_stmt.setFloat(3, longitude - LAT_LONG_DELTA);
					city_stmt.setFloat(4, longitude + LAT_LONG_DELTA);
					ResultSet rs2 = city_stmt.executeQuery();
					while ( rs2.next() ) {
						String id = rs2.getString(1);
						if ( ! station_ids.contains(id) ) {
							station_ids.add(id);
						}
					}
				}
				
			} else if ( terms.getState() != null ) {
				for (String id : spatialExtension.getStationsForState(terms.getState())) {
					if (! station_ids.contains(id)) {
						station_ids.add(id);
					}
				}
			}
			
			if ( terms.getStationName() != null ) {
				stmt = conn.prepareStatement("SELECT station_id FROM station WHERE UPPER(station_name) LIKE ?");
				stmt.setString(1, "%" + terms.getStationName().toUpperCase() + "%");
				rs = stmt.executeQuery();
				while ( rs.next() ) {
					String id = rs.getString(1);
					if ( ! station_ids.contains(id) ) {
						station_ids.add(id);
					}
				}
			}

		} catch (SQLException sqe) { 
			LOG.error("sql exception querying meta data", sqe);
			throw new RemoteException("unable to query metadata from datasource");
		} finally { 
			try {
				if ( rs != null ) { 
					rs.close();
				}
				
				if ( rs1 != null ) { 
					rs1.close();
				}
				
				if ( city_stmt != null ) { 
					city_stmt.close();
				}
				
				if ( stmt != null ) { 
					stmt.close();
				}
				
				if ( conn != null ) {
					conn.close();
				}
			} catch (SQLException sqe2) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
		
		MetaDataCollection<StationMetaDataType> mdc = null;
		try {
			mdc = getMetaData(station_ids, StationMetaDataType.STATION_NAME, CalendarPeriod.DAILY);
			mdc.importMap(StationMetaDataType.NETWORK_ID, getMetaData(station_ids, StationMetaDataType.NETWORK_ID, CalendarPeriod.DAILY).extractType(StationMetaDataType.NETWORK_ID));
			mdc.importMap(StationMetaDataType.LATITUDE, getMetaData(station_ids, StationMetaDataType.LATITUDE, CalendarPeriod.DAILY).extractType(StationMetaDataType.LATITUDE));
			mdc.importMap(StationMetaDataType.LONGITUDE, getMetaData(station_ids, StationMetaDataType.LONGITUDE, CalendarPeriod.DAILY).extractType(StationMetaDataType.LONGITUDE));
			mdc.importMap(StationMetaDataType.NETWORK_ID, getMetaData(station_ids, StationMetaDataType.NETWORK_ID, CalendarPeriod.DAILY).extractType(StationMetaDataType.NETWORK_ID));
			mdc.importMap(StationMetaDataType.COUNTY, getMetaData(station_ids, StationMetaDataType.COUNTY, CalendarPeriod.DAILY).extractType(StationMetaDataType.COUNTY));
			mdc.importMap(StationMetaDataType.STATE, getMetaData(station_ids, StationMetaDataType.STATE, CalendarPeriod.DAILY).extractType(StationMetaDataType.STATE));
			mdc.importMap(StationMetaDataType.ABS_START_DATE, getMetaData(station_ids, StationMetaDataType.ABS_START_DATE, CalendarPeriod.DAILY).extractType(StationMetaDataType.ABS_START_DATE));
			mdc.importMap(StationMetaDataType.ABS_END_DATE, getMetaData(station_ids, StationMetaDataType.ABS_END_DATE, CalendarPeriod.DAILY).extractType(StationMetaDataType.ABS_END_DATE));
			mdc.importMap(StationMetaDataType.NETWORK_NAME, getMetaData(station_ids, StationMetaDataType.NETWORK_NAME, CalendarPeriod.DAILY).extractType(StationMetaDataType.NETWORK_NAME));
		} catch (InvalidArgumentException iae) {
			LOG.error("error collecting metadata", iae);
			throw new RemoteException("unable to query metadata for station criteria");
		} catch (InvalidStateException ise) {
			LOG.error("error collecting metadata", ise);
			throw new RemoteException("unable to query metadata for station criteria");
		}
		
		return mdc;
	}

	@WebMethod
	public TemporalPeriod getLongestPeriod(List<String> stations, CalendarPeriod period) throws RemoteException, InvalidArgumentException {
		
		if ( stations.size() < 1 ) { 
			return new TemporalPeriod(-1l, -1l);
		}
		
		DateTime begin = new DateTime(System.currentTimeMillis());
		DateTime end = new DateTime(new DateTime(1800, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance()).getMillis());
		Map<String, Object> beginTimes = getMetaData(stations, StationMetaDataType.START_DATE, period).extractType(StationMetaDataType.START_DATE);
		Map<String, Object> endTimes = getMetaData(stations, StationMetaDataType.END_DATE, period).extractType(StationMetaDataType.END_DATE);
		
		DateTime begin_time = null, end_time = null;
		for ( String station : beginTimes.keySet() ) { 
			begin_time = (DateTime)beginTimes.get(station);
			end_time = (DateTime)endTimes.get(station);
			
			if ( begin_time.isBefore(begin) )
				begin = begin_time;
			
			if ( end_time.isAfter(end) ) { 
				end = end_time;
			}
		}
		
		return new TemporalPeriod(begin, end);
	}	
	
	@WebMethod
	public HashMap<String, Map<DataType, VariableMetaData>> getVariableMetaData(List<String> stations) throws RemoteException {
		
		Connection conn = null;

		HashMap<String, Map<DataType, VariableMetaData>> ret = new HashMap<String, Map<DataType, VariableMetaData>>();
		PreparedStatement stmt = null;
		PreparedStatement var_stmt = null;
		ResultSet query_result = null;
		ResultSet variables = null;
		
		try {			
			conn = source.getConnection();
			
			stmt = conn.prepareStatement("select start_date, end_date, missing_percent from variable inner join station on variable.station_id = " +
					"station.station_id where station.station_id = ? and variable.variable_name = ?");
			
			var_stmt = conn.prepareStatement(VARIABLE_BASE);
						
			for ( String station : stations ) {
				var_stmt.setString(1, station);
				variables = var_stmt.executeQuery();
				Map<DataType, VariableMetaData> station_result = new HashMap<DataType, VariableMetaData>();
				
				while ( variables.next() ) {
					DataType variable;
					try {
						variable = DataType.valueOf(variables.getString(1));
					} catch ( Exception e ) { 
						LOG.warn("The type "+variables.getString(1)+" is not valid, skipping");
						continue;
					}
					
					stmt.setString(2, variable.name());
					stmt.setString(1, station);
					query_result = stmt.executeQuery();
					if ( query_result.next() ) {
						station_result.put(variable, new VariableMetaData(query_result.getDate(1).getTime(), query_result.getDate(2).getTime(), variable.name(), query_result.getFloat(3)));
					}
				}
				
				ret.put(station, station_result);
			}
		} catch ( SQLException sqe ) { 
			LOG.error("error accessing database", sqe);
			throw new RemoteException("error accessing database");
		} finally { 
			try {
				if ( variables != null ) { 
					variables.close();
				}
				
				if ( query_result != null ) { 
					query_result.close();
				}
				
				if ( stmt != null ) { 
					stmt.close();
				}
				
				if ( var_stmt != null ) { 
					var_stmt.close();
				}
				conn.close();
			} catch ( Exception s ) { /* do nothing */ }
		}
		
		return ret;
	}
	
	/**
	 * A method used to parse out meta data values from a JDBC ResultSet.  The logic pairs enumerated meta-data types with
	 * the column headers of the ResultSet.
	 * 
	 * @param meta_query_result
	 * @return A map of enumerated meta types to object values for the query
	 * @throws SQLException
	 * @throws RemoteException
	 */
	private HashMap<StationMetaDataType, Object> getMetaDataFromQuery(ResultSet meta_query_result, CalendarPeriod period) throws InvalidArgumentException, SQLException, RemoteException { 

		
		HashMap<StationMetaDataType, Object> station_meta_data = new HashMap<StationMetaDataType, Object>();
		ResultSetMetaData res_meta = meta_query_result.getMetaData();
		int field_count = res_meta.getColumnCount();
		StationMetaDataType current_type = null;
		
		while ( meta_query_result.next() ) { 
			for ( int i=1; i<=field_count; i++ ) { 
				current_type = StationMetaDataType.valueOf(res_meta.getColumnName(i).toUpperCase());
				station_meta_data.put(current_type, getTypedValue(meta_query_result, current_type, i, period));
			}
		}
		
		return station_meta_data;
	}
	
	/**
	 * A method for overriding some meta values from the ResultSet.  In most cases the actual value is returned.  Special cases
	 * are accounted for:
	 * 
	 * 9999 - used to indicate that the station has current data (i.e. the station continues to report values).  In this case
	 * the current system time is returned rather than 9999.
	 * 
	 * @param meta_query_result
	 * @param current_type
	 * @param i
	 * @return
	 * @throws SQLException
	 * @throws RemoteException
	 */
	private Object getTypedValue(ResultSet meta_query_result, StationMetaDataType current_type, int i, CalendarPeriod period) 
	throws InvalidArgumentException, SQLException, RemoteException { 
		
		switch ( current_type ) { 
			case NETWORK_ID:
				return meta_query_result.getString(i);
			case STATION_ID:
				return meta_query_result.getString(i);
			case STATION_NAME:
				return meta_query_result.getString(i);
			case STATE:
				return meta_query_result.getString(i);
			case COUNTY:
				return meta_query_result.getString(i);
			case LONGITUDE:
				return meta_query_result.getFloat(i);
			case LATITUDE:
				return meta_query_result.getFloat(i);
			case ELEVATION:
				return meta_query_result.getFloat(i);
			case CLIMATE_DIV:
				return meta_query_result.getInt(i);
			case ABS_START_DATE:
				if ( meta_query_result.getString(i).indexOf("9999") > -1 )
					return getEndingDate(period);
				else
					return new DateTime(meta_query_result.getDate(i));
			case ABS_END_DATE:
				if ( meta_query_result.getString(i).indexOf("9999") > -1 )
					return getEndingDate(period);
				else
					return new DateTime(meta_query_result.getDate(i));
			case START_DATE:
				if ( meta_query_result.getString(i).indexOf("9999") > -1 )
					return getEndingDate(period);
				else
					return new DateTime(meta_query_result.getDate(i));
			case END_DATE:
				if ( meta_query_result.getString(i).indexOf("9999") > -1 )
					return getEndingDate(period);
				else
					return new DateTime(meta_query_result.getDate(i));
			case NETWORK_NAME:
				return meta_query_result.getString(i);
			default:
				throw new RemoteException("invalid meta data type found in result set");
		}
	}
	
	@WebMethod
	public DateTime getEndingDate(CalendarPeriod period) throws InvalidArgumentException, RemoteException {
		
		switch ( period ) {
		case DAILY:
		case WEEKLY:
		case MONTHLY:
			break;
		default:
			period = CalendarPeriod.DAILY;
		}
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try { 
			conn = source.getConnection();
			
			stmt = conn.prepareStatement("SELECT date FROM buildmeta WHERE name = ?");
			stmt.setString(1, period.name());
			rs = stmt.executeQuery();
			if (rs.next()) {
				return new DateTime(rs.getDate(1));
			} else {
				throw new RemoteException("no ending date found for period " + period);
			}
			
		} catch ( SQLException sqle ) {
			LOG.error("exception querying from buildmeta table", sqle);
			throw new RemoteException("could not query ending date");
		} catch ( RuntimeException t ) { 
			LOG.error("unknown exception", t);
			throw new RemoteException("could not query ending date");
		} finally { 
			try {
				if ( rs != null ) { 
					rs.close();
				}
				
				if ( stmt != null ) {
					stmt.close();
				}
					
				if ( conn != null ) {
					conn.close();
				}
			} catch ( Exception sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
	}
	
	@WebMethod
	public List<String> filterStations(List<String> stations, List<VariableFilter> filters, TemporalPeriod overallPeriod, float overallTolerance, boolean actualTolerance) 
	throws RemoteException {		
		ArrayList<String> filtered_stations = new ArrayList<String>();
		HashMap<VariableFilter, Map<String, VariableMetaData>> meta_data = new HashMap<VariableFilter, Map<String, VariableMetaData>>();
		HashMap<VariableFilter, CalendarDataCollection> variable_data = new HashMap<VariableFilter, CalendarDataCollection>();
		
		// calculate the earliest start date and the latest end date over which to query data, to ensure enough data is queried to validate tolerance constraints
		TemporalPeriod whole_period = overallPeriod;
		for ( VariableFilter filter : filters ) {
			if (filter.getValidPeriod() != null) {
				whole_period = whole_period.union(filter.getValidPeriod());
			}
		}
		
		Map<String, Map<DataType, VariableMetaData>> var_data = getVariableMetaData(stations);
		for ( VariableFilter filter : filters ) { 
			
			meta_data.put(filter, extractType(filter.getVariableType(), var_data));
			if ( actualTolerance ) { 
				try {
					variable_data.put(filter, dataQuery.getPeriodData(stations, whole_period.getStart(), whole_period.getEnd(), filter.getVariableType(), CalendarPeriod.WEEKLY));
				} catch ( InvalidArgumentException ive ) { 
					LOG.error("an invalid argument was used in the station filter", ive);
					throw new RemoteException("and invalid argument prevented the fileter from running");
				}
			}
		}
		
		Map<String, VariableMetaData> meta = null;
		VariableMetaData m_data = null;
		CalendarDataCollection cdc = null;
		
		float missing_count = 0;
		float actual_count = 0;
		
		station_loop:
		for ( String station : stations ) { 
			
			/* apply all filters to the station */
			for ( VariableFilter filter : meta_data.keySet() ) { 
				meta = meta_data.get(filter);
				m_data = meta.get(station);
				
				/* check to see if the station has this variable */
				if ( m_data == null ) {
					continue station_loop;
				}
				
				/* first check against the overall date constraints */
				if ( !m_data.getVariablePeriod().contains(overallPeriod)) {
					int days_after = new Period(m_data.getVariablePeriod().getEnd(), overallPeriod.getEnd(), PeriodType.days()).getDays();
					int days_before = new Period(overallPeriod.getStart(), m_data.getVariablePeriod().getStart(), PeriodType.days()).getDays();
					
					/* the date ranges are close enough to allow a threshhold check */
					if ( (days_after > ALLOWABLE_DAYS) || (days_before > ALLOWABLE_DAYS) ) {
						continue station_loop;
					}
				}
				
				/* then check against the individual variable's date constraints */
				if ( filter.getValidPeriod() != null ) { 
					if ( !(m_data.getVariablePeriod().contains(filter.getValidPeriod())) ) { 
						
						int days_after = new Period(m_data.getVariablePeriod().getEnd(), filter.getValidPeriod().getEnd(), PeriodType.days()).getDays();
						int days_before = new Period(filter.getValidPeriod().getStart(), m_data.getVariablePeriod().getStart(), PeriodType.days()).getDays();
						
						/* the date ranges are close enough to allow a threshhold check */
						if ( (days_after > ALLOWABLE_DAYS) || (days_before > ALLOWABLE_DAYS) ) {
							continue station_loop;
						}
					}
				}
				
				/* now check the amount of reporting data, this only happens if actualTolerance is false.  */
				if ( !(actualTolerance) && filter.getMissingTolerance() != -1f ) { 
					if ( m_data.getMissingPercent() > filter.getMissingTolerance() ) { 
						continue station_loop;
					}
				} else if ( !(actualTolerance) && overallTolerance != -1f ) {
					/* in case there is an overall tolerance that all variables must meet, check that the variable meets the requirement */
					if ( m_data.getMissingPercent() > overallTolerance ) {
						continue station_loop;
					}
					
				} else if ( filter.getMissingTolerance() != -1f || overallTolerance != -1 ) { 
					/* in this case, there is a tolerance requirement, and actual filter should be used */
					cdc = variable_data.get(filter);
					missing_count = 0;
					actual_count = 0;
					for ( float[] d : cdc.getStationData(station) ) { 
						for ( float d2 : d ) { 
							actual_count++;
							if ( d2 == DataType.MISSING || d2 == DataType.ERROR_RESULT ) { 
								missing_count++;
							}
						}
					}
					
					float missing = missing_count / actual_count;
					
					/* apply the variable's missing tolerance constraint, if it exists */
					if ( filter.getMissingTolerance() != -1f && missing > filter.getMissingTolerance() ) {
						continue station_loop;
					}
					
					/* apply the overall missing tolerance constraint, if it exists */
					if ( overallTolerance != -1f && missing > overallTolerance ) {
						continue station_loop;
					}
				}
			}
			
			/* if the execution gets here, then all filters passed so the station is added to the filtered list */
			filtered_stations.add(station);
		}
				
		return filtered_stations;
	}	

	private Map<String, VariableMetaData> extractType(DataType type, Map<String, Map<DataType, VariableMetaData>> map) { 
		Map<String, VariableMetaData> ret = new HashMap<String, VariableMetaData>();
		
		for ( String str : map.keySet() ) { 
			ret.put(str, map.get(str).get(type));
		}
		
		return ret;
	}
	
	@WebMethod
	public HashMap<String, List<Interval>> getIntervalGaps(List<String> stations, DataType type) throws RemoteException {

		HashMap <String, List<Interval>> resultMap = new HashMap<String, List<Interval>>();

		// Have to iterate over each station since they could have differing available data windows
		for (String str : stations) {
			Vector <String> thisStation = new Vector<String>();
			thisStation.add(str);	
			CalendarDataCollection dataCol = null;
			try {
				dataCol = dataQuery.getAvailableData(thisStation, type, CalendarPeriod.MONTHLY);

			} catch ( InvalidArgumentException ive ) { 
				LOG.error("an invalid argument was used in the station filter", ive);
				throw new RemoteException("and invalid argument prevented the filter from running");
			}
			
			float dataMatrix [][] = dataCol.getDataMatrix(str);
			DateTime dataStart = dataCol.getBegin();				
			DateTime gapStart = null;
			DateTime gapEnd = null;
			List <Interval> missingPeriods = new ArrayList<Interval>();
			
			boolean insideGap = false;			 // If true, We are currently inside of missing data segment			
			for (int year = 0; year < dataMatrix.length; year++) {
				int currentYear = dataStart.year().get() + year;
				for (int month = 0; month < dataMatrix[0].length; month++) {
					if (dataMatrix [year][month] == DataType.MISSING) {
						if (!insideGap) {
							insideGap = true;
							gapStart = new DateTime(currentYear, month + 1, 1, 0, 0, 0, 0);		
						}										
					} 
					else {
						if (insideGap) {
							gapEnd = new DateTime(currentYear, month + 1, 1, 0, 0, 0, 0);							
							Interval i = new Interval(gapStart, gapEnd);
							missingPeriods.add(i);
							insideGap = false;
						}
					} 
				} // End for day					
			} // End for year			
			resultMap.put(str, missingPeriods);
		} // End for station id string
		return resultMap;		
	}

	@WebMethod
	public boolean isValidStation(String stationId) throws RemoteException {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try { 
			conn = source.getConnection();
			
			stmt = conn.prepareStatement("select s.station_name from station s where s.station_id = ?");
			
			stmt.setString(1, stationId);
			return stmt.executeQuery().next();
			
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying meta data", sqe);
			throw new RemoteException("unable to query metadata from datasource");
		} finally { 
			try {
				if ( stmt != null ) {
					stmt.close();
				}
				
				if ( conn != null ) {
					conn.close();
				}
			} catch ( Exception sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
	}

	@WebMethod
	public List<String> removeInvalidStations(List<String> stations) throws RemoteException {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try { 
			conn = source.getConnection();
			
			stmt = conn.prepareStatement("select s.station_name from station s where s.station_id = ?");
			ArrayList<String> valid_ids = new ArrayList<String>();
			
			for ( String stationId : stations ) {
				stmt.setString(1, stationId);
				if ( stmt.executeQuery().next() ) {
					valid_ids.add(stationId);
				}
			}
			
			return valid_ids; 
			
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying meta data", sqe);
			throw new RemoteException("unable to query metadata from datasource");
		} finally { 
			try {
				if ( stmt != null ) { 
					stmt.close();
				}
				
				if ( conn != null ) {
					conn.close();
				}
			} catch ( Exception sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
	}	
}
