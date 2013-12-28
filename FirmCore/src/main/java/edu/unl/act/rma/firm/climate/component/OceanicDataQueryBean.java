/* Created On: Sep 8, 2005 */
package edu.unl.act.rma.firm.climate.component;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.climate.OceanicMetaDataType;
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
import edu.unl.act.rma.firm.core.YearDataBuilder;



/**
 * @see edu.unl.act.rma.firm.climate.component.OceanicDataQuery
 * 
 * @author Ben Kutsch
 */
@Stateless
@Local({LocalOceanicDataQuery.class})
@Remote({OceanicDataQuery.class})
public class OceanicDataQueryBean implements LocalOceanicDataQuery{

	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG, OceanicDataQueryBean.class);
	
	private DataSource source = DataSourceInjector.injectDataSource(DataSourceTypes.OCEANIC);
	private static final String SQL_BASE = " FROM oceanic_variable, type where oceanic_variable.type_id=type.type_id ";
	
	private static final String MONTHLY_BASE=("select month, year, value from monthly m inner join oceanic_variable v on m.variable_id = v.variable_id" +
			" where v.name = ?" +
			" and (year>? or (year=? and (month>=? ))) and (year< ? or (year=? and (month<=?))) order by year, month");
	private static final String WEEKLY_BASE=("select week, year, value from weekly m inner join oceanic_variable v on m.variable_id = v.variable_id" +
			" where v.name = ?" +
			" and (year>? or (year=? and (week>=? ))) and (year< ? or (year=? and (week<=?))) order by year, week");

	
	public CalendarDataCollection getOceanicMonthlyValues(DataType variableID, DateTime firstDate, DateTime lastDate) throws RemoteException, InvalidArgumentException{
	
		if ( lastDate.isBefore(firstDate) ) { 
			throw new InvalidArgumentException("the lastDate argument cannot occur before the firstDate argument");
		}
		
		switch ( variableID ) {
		case SOI_ANOMALY:
			break;
		case SOI_STANDARD:
			break;
		case MEI:
			break;
		case NAM_PC1:
			break;
		case PNA_PC2:
			break;
		case PC3:
			break;
		case NAO:
			break;
		case ONI:
			break;
		case NPI:
			break;
		case PDO:
			break;
		case JMASST:
			break;
		case AMO:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for variable  "+variableID.getName());
		}
		
		
		Connection conn = null;
		YearDataBuilder builder = new YearDataBuilder(firstDate, lastDate, CalendarPeriod.MONTHLY, variableID);
		try { 
			conn = source.getConnection();
			PreparedStatement stmt = conn.prepareStatement(MONTHLY_BASE);
			stmt.setString(1, variableID.name());
			stmt.setInt(2, firstDate.getYear());
			stmt.setInt(3, firstDate.getYear());
			stmt.setInt(4, firstDate.getMonthOfYear());
			stmt.setInt(5, lastDate.getYear());
			stmt.setInt(6, lastDate.getYear());
			stmt.setInt(7, lastDate.getMonthOfYear());

			builder.openStation(variableID.name());
			stmt.setString(1, variableID.name());
			ResultSet query_result = stmt.executeQuery();
			
			if ( !query_result.next() ) { 
				emptyResult(builder, firstDate);
			}
			
			missingPopulate(firstDate.getYear(), query_result.getInt(2), firstDate.getMonthOfYear(), query_result.getInt(1), CalendarPeriod.MONTHLY, builder);
			
			do { 
				builder.add(query_result.getFloat(3));
			} while ( query_result.next() );
			
			query_result.last();
			missingPopulate(query_result.getInt(2), lastDate.getYear(), query_result.getInt(1), lastDate.getMonthOfYear(), CalendarPeriod.MONTHLY, builder);
			
			
			builder.writeStation();

		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying data for type "+variableID.name(), sqe);
			throw new RemoteException("unable to query data from data base for type "+variableID.name());
		} catch ( InvalidStateException ise ) { 
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally { 
			try {
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
	
	public CalendarDataCollection getOceanicWeeklyValues(DataType variableID, DateTime firstDate, DateTime lastDate) throws RemoteException, InvalidArgumentException{
		
		if ( lastDate.isBefore(firstDate) ) { 
			throw new InvalidArgumentException("the lastDate argument cannot occur before the firstDate argument");
		}
		
		switch ( variableID ) {
		case RMM1:
			break;
		case RMM2:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for variable  "+variableID.getName());
		}
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
		
		Connection conn = null;
		YearDataBuilder builder = new YearDataBuilder(firstDate, lastDate, CalendarPeriod.WEEKLY, variableID);
		try { 
			conn = source.getConnection();
			PreparedStatement stmt = conn.prepareStatement(WEEKLY_BASE);
			stmt.setString(1, variableID.name());
			stmt.setInt(2, first_year);
			stmt.setInt(3, first_year);
			stmt.setInt(4, first_week);
			stmt.setInt(5, last_year);
			stmt.setInt(6, last_year);
			stmt.setInt(7, last_week);

			builder.openStation(variableID.name());
			stmt.setString(1, variableID.name());
			ResultSet query_result = stmt.executeQuery();
			
			if ( !query_result.next() ) { 
				emptyResult(builder, firstDate);
			}
			
			missingPopulate(firstDate.getYear(), query_result.getInt(2), first_week, query_result.getInt(1), CalendarPeriod.WEEKLY, builder);
			
			do { 
				builder.add(query_result.getFloat(3));
			} while ( query_result.next() );
			
			query_result.last();
			missingPopulate(query_result.getInt(2), lastDate.getYear(), query_result.getInt(1), last_week, CalendarPeriod.WEEKLY, builder);
			
			
			builder.writeStation();

		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying data for type "+variableID.name(), sqe);
			throw new RemoteException("unable to query data from data base for type "+variableID.name());
		} catch ( InvalidStateException ise ) { 
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally { 
			try {
				conn.close();
			} catch ( SQLException sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
		
		return builder.returnCollection();
	}
	


	
	
	public MetaDataCollection getOceanicMetaData(DataType variableID) throws RemoteException, InvalidArgumentException{

		Connection conn = null;
		
		Map<String,Map<OceanicMetaDataType, Object>> oceanic_data = new HashMap<String, Map<OceanicMetaDataType, Object>>();
		try { 
			conn = source.getConnection();

			String types = "";
			for ( OceanicMetaDataType type : OceanicMetaDataType.values() ) {
				types += ", "+type.getFieldName();
			}
			types = types.substring(2);
			
			PreparedStatement stmt = conn.prepareStatement("select "+types+SQL_BASE+"and oceanic_variable.name = ?");
			

			stmt.setString(1, variableID.name());
			ResultSet ocean_query_result = stmt.executeQuery();		
			
			HashMap<OceanicMetaDataType, Object> station_meta_data = new HashMap<OceanicMetaDataType, Object>();
			ResultSetMetaData res_meta = ocean_query_result.getMetaData();
			int field_count = res_meta.getColumnCount();
			OceanicMetaDataType current_type = null;
			
			while ( ocean_query_result.next() ) { 
				for ( int i=1; i<=field_count; i++ ) { 
					current_type = OceanicMetaDataType.valueOf(res_meta.getColumnName(i).toUpperCase());
					station_meta_data.put(current_type, getTypedValue(ocean_query_result, current_type, i));
				}
			}
			
			oceanic_data.put(variableID.name(), station_meta_data);

			
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying meta data", sqe);
			throw new RemoteException("unable to query metadata from datasource");
		} finally { 
			try {
				if ( conn != null ) {
					conn.close();
				}
			} catch ( SQLException sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}

		return new MetaDataCollection<OceanicMetaDataType>(oceanic_data);
	}
	
	
	public DateTime getOceanicStartDate(DataType variableID) throws RemoteException, InvalidArgumentException{
		String sql=null;
		switch ( variableID ) {
		case SOI_ANOMALY:
		case SOI_STANDARD:
		case MEI:
		case NAM_PC1:
		case PNA_PC2:
		case PC3:
		case NAO:
		case ONI:
		case NPI:
		case PDO:
		case JMASST:
		case AMO:
			sql="SELECT month,year FROM monthly m INNER JOIN oceanic_variable v ON m.variable_id = v.variable_id WHERE v.name = '"+variableID.name()+"' ORDER BY year ASC, month ASC LIMIT 1";
			break;
		case RMM1:
		case RMM2:
			sql="SELECT month,year FROM weekly m INNER JOIN oceanic_variable v ON m.variable_id = v.variable_id WHERE v.name = '"+variableID.name()+"' ORDER BY year ASC, month ASC LIMIT 1";
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for variable  "+variableID.getName());
		}
		
		Connection conn = null;
		int month = 0;
		int year = 0;
		DateTime date=null;
		try{
			conn = source.getConnection();
			ResultSet rs=conn.createStatement().executeQuery(sql);
			while (rs.next()){
				month = rs.getInt("month");
				year = rs.getInt("year");
				date= new DateTime(year+"-"+month+"-01");
			}

			if(date==null){
				LOG.error("No date found for the given varaibleID: "+ variableID.name());
				throw new RemoteException("unable to query date for given variableID");
			}
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying meta data", sqe);
			throw new RemoteException("unable to query date: sql exception");
		} finally { 
			try {
				if ( conn != null ) { 
					conn.close();
				}
			} catch ( SQLException sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
		
		return date;
	}
	
	
	public DateTime getOceanicEndDate(DataType variableID) throws RemoteException, InvalidArgumentException{
		String sql = null;
		switch ( variableID ) {
		case SOI_ANOMALY:
		case SOI_STANDARD:
		case MEI:
		case NAM_PC1:
		case PNA_PC2:
		case PC3:
		case NAO:
		case ONI:
		case NPI:
		case PDO:
		case JMASST:
		case AMO:
			sql="SELECT month,year FROM monthly m INNER JOIN oceanic_variable v ON m.variable_id = v.variable_id WHERE v.name = '"+variableID.name()+"' ORDER BY year DESC, month DESC LIMIT 1";
			break;
		case RMM1:
		case RMM2:
			sql="SELECT month,year FROM weekly m INNER JOIN oceanic_variable v ON m.variable_id = v.variable_id WHERE v.name = '"+variableID.name()+"' ORDER BY year DESC, month DESC LIMIT 1";
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for variable  "+variableID.getName());
		}
		
	
		Connection conn = null;
		int month = 0;
		int year = 0;
		int day =0;
		DateTime date=null;
		try{
			conn = source.getConnection();
			ResultSet rs=conn.createStatement().executeQuery(sql);
			while (rs.next()){
				month = rs.getInt("month");
				year = rs.getInt("year");
				date= new DateTime(year+"-"+month+"-01");
				day = date.dayOfMonth().getMaximumValue();
				date= new DateTime(year+"-"+month+"-"+day);
			}

			if(date==null){
				LOG.error("No date found for the given varaibleID: "+ variableID.name());
				throw new RemoteException("unable to query date for given variableID");
			}
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying meta data", sqe);
			throw new RemoteException("unable to query date: sql exception");
		} finally { 
			try {
				if ( conn != null ) {
					conn.close();
				}
			} catch ( SQLException sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
		
		return date;
	}

	/**
	 * A method used to parse out meta data values from a JDBC ResultSet.  The logic pairs enumerated meta-data types with
	 * the column headers of the ResultSet.
	 * 
	 * @param meta_query_result
	 * @return A map of enumerated meta types to object values for the query
	 * @throws SQLException
	 * @throws RemoteException
	 *//*
	private HashMap<String, Object> getOcenaicMetaDataFromQuery(ResultSet oceanic_query_result) throws InvalidArgumentException, SQLException, RemoteException { 
		
		HashMap<String, Object> station_meta_data = new HashMap<String, Object>();
		ResultSetMetaData res_meta = oceanic_query_result.getMetaData();
		int field_count = res_meta.getColumnCount();
		OceanicMetaDataType current_type = null;
		
		while ( oceanic_query_result.next() ) { 
			for ( int i=1; i<=field_count; i++ ) { 
				current_type = OceanicMetaDataType.valueOf(res_meta.getColumnName(i).toUpperCase());
				station_meta_data.put(current_type.name(), getTypedValue(oceanic_query_result, current_type, i));
			}
		}
		
		return station_meta_data;
	}
	
	
	/**
	 * A method for overriding some meta values from the ResultSet.  In most cases the actual value is returned.  Special cases
	 * are accounted for:
	 * 
	 * 
	 * @param meta_query_result
	 * @param current_type
	 * @param i
	 * @return
	 * @throws SQLException
	 * @throws RemoteException
	 */
	private Object getTypedValue(ResultSet meta_query_result, OceanicMetaDataType current_type, int i) 
	throws InvalidArgumentException, SQLException, RemoteException { 

		switch ( current_type ) { 
			case VARIABLE_ID:
				return meta_query_result.getString(i);
			case NAME:
				return meta_query_result.getString(i);
			case START_DATE:
				return new DateTime(meta_query_result.getDate(i));
			case END_DATE:
				return new DateTime(meta_query_result.getDate(i));
			case TYPE:
				return meta_query_result.getString(i);
			default:
				throw new RemoteException("invalid oceanic meta data type found in result set");
		}
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
	
	/**
	 * A method for populating an entire station result with missing values over the temporal period
	 * requested.
	 * 
	 * @param builder the <i>PeriodOrderedDataBuilder</i> containing the results from the caller method, 
	 * this object is updated in this method.
	 * 
	 * @throws InvalidStateException
	 */
	private void emptyResult(YearDataBuilder builder, DateTime from) throws InvalidStateException { 
	//	 populate the entire range of expected values with the missing value 
		DateTime clock = new DateTime(from);
		while (! builder.isLimitReached()) {
			if (!clock.year().isLeap() && clock.getDayOfYear() == 59) {
				builder.add(DataType.NONEXISTANT);
			}
			
			builder.add(DataType.MISSING);
			clock = clock.plusDays(1);
		}
		
		builder.writeStation();
	}
	
}