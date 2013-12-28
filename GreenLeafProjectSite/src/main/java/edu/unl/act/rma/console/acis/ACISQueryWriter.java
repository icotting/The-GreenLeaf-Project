/* Created On: Jun 13, 2005 */
package edu.unl.act.rma.console.acis;

import java.rmi.server.UID;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.joda.time.DateTime;

import edu.unl.act.rma.console.web.IDMap;
import edu.unl.act.rma.firm.climate.WeatherStationNetwork;
import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.Task;
import edu.unl.act.rma.firm.core.TaskState;
import edu.unl.act.rma.firm.core.spatial.USState;

/**
 * @author Ian Cottingham
 *
 */
public class ACISQueryWriter extends Task {
	
	private Logger LOG = LogManager.getLogger(Loggers.SYSTEM_LOG, ACISQueryWriter.class);
	
	private static final int ATTEMPTS = 10;
	private static final long WAIT_TIME = 600000l;
	
	public final String DESCRIPTION = "A task to query acis servers for station data and write meta data to db";
	private static final int APROX_STATIONS = 500;
	
	/* the internal ID of the UCAN data source */
	private static final int UCAN_SOURCE_ID = 2;
	
	private final String stationInsert = "INSERT INTO station (station_name, state, county, climate_div, " +
											"abs_start_date, abs_end_Date, station_id) Values(?,?,?,?,?,?,?)";
	private final String locationInsert = "INSERT INTO absolute_location (latitude,longitude,elevation, location_id) VALUES (?,?,?,?)";
	private final String locationStationInsert = "INSERT INTO location_station_link (location_id,station_id, start_date, end_date) VALUES (?,?,?,?)";
	private final String networkStationInsert = "INSERT INTO network_station_link (station_id, network_id, network_type_id, ucan_id) VALUES (?,?,?,?)";
	private final String variableInsert = "INSERT INTO variable (variable_id, variable_name, station_id, start_date, end_date, source_type_id) VALUES(?,?,?,?,?,"+UCAN_SOURCE_ID+")";

	private final String stationQuery = "SELECT nsl.network_id, s.station_id FROM network_station_link nsl INNER JOIN station s ON nsl.station_id = s.station_id WHERE s.abs_end_date = '9999-12-31' AND s.state = ?";
	private final String updateStation = "UPDATE station SET abs_end_date = ? WHERE station_id = ?";	
	private final String variableSelection = "SELECT v.variable_name, v.variable_id FROM variable v INNER JOIN station s ON v.station_id = s.station_id WHERE s.station_id = ?";
	private final String variableUpdate = "UPDATE variable SET end_date = ? WHERE variable_id = ?";
	
	private static final DataSource source = DataSourceInjector.injectDataSource(DataSourceTypes.CLIMATIC_BUILD);
		
	private BlockingQueue<Collection<ACISResult>> queue;
	private Pattern stateExpression;
	private BuildType buildType;
	private DateTime lastRun;
	private String currentState;
	private ACISDataBuilder builder;
	
	/* this reference is for updating units of complete work, all data should be passed to the object
	 * through the provided queue
	 */ 
	private ACISDataWriter dbuilder;
	
	public ACISQueryWriter(ACISDataBuilder builder, BlockingQueue<Collection<ACISResult>> queue, Pattern regex, BuildType buildType, DateTime lastRun) { 
		super("Ucan Query Writer");
		this.queue = queue;
		this.stateExpression = regex;
		this.buildType = buildType;
		this.lastRun = lastRun;
		this.builder = builder;
	}
		
	public ACISQueryWriter(ACISDataBuilder builder, BlockingQueue<Collection<ACISResult>> queue, Pattern regex, BuildType buildType, DateTime lastRun, Semaphore s) {
		super("Ucan Query Writer", s, false, true);
		this.queue = queue;
		this.stateExpression = regex;
		this.buildType = buildType;
		this.lastRun = lastRun;
		this.builder = builder;
	}
	
	
	public void setDataWriter(ACISDataWriter dbuilder) { 
		this.dbuilder = dbuilder;
	}
	
	public void logic() throws Exception {

		totalUnits = USState.getStatesByRegex(stateExpression).size()*2*APROX_STATIONS;
		dbuilder.addToTotalUnits(totalUnits);
		
		Collection<USState> states = USState.getStatesByRegex(stateExpression);
		
		if ( states.size() > 0 ) { // there will be data to be written so the data writer should be started
			builder.startDataWriter();
		}
		
		for ( USState state : states ) {
			if ( halted ) { 
				break;
			}
			currentState = state.name();
			
			for ( WeatherStationNetwork network : WeatherStationNetwork.values() ) { 
				if ( halted ) { 
					break;
				}
				
				if ( !(network.isAcisNetwork()) ) { 
					continue; // this network cannot be processed by this code
				}
				processNetwork(state, network);
			}
		}
		
		if ( halted ) { 
			taskState = TaskState.HALTED;
			LOG.info("The data writer process has been halted");
			currentState = "Process halted";
		}
		
		releaseSemaphore();
	}
	
	public void aquireLogic() throws Exception { }
	
	private void processNetwork(USState state, WeatherStationNetwork network) throws Exception { 
		LOG.info("querying "+network.name()+" stations for "+state.name());
		taskStatus = "["+currentState+" Process]: querying "+network.name()+" stations for "+state.name();
		
		boolean try_again = true;
		Collection<ACISResult> result = null;
		int try_count = 0;
		
		while ( try_again ) {
			try {
				result = ACISResultFactory.getInstance(lastRun, buildType).queryNetworkStationsByState(state, network);
				try_again = false;
			} catch ( Exception e ) { 
				if ( ++try_count < ATTEMPTS ) {
					LOG.warn("caught exception communicating with acis, will wait and try again.  Attempt: "+try_count);
					try_again = true;
					ACISResultFactory.release();
					Thread.sleep(WAIT_TIME);
				} else { 
					try_again = false;
					throw e;
				}
			}
		} 
		
		int result_count = result.size();
		if ( result_count > 0 ) {

			this.totalUnits -= APROX_STATIONS - result_count;
			dbuilder.subtractFromTotalUnits(APROX_STATIONS - result_count);
			
			LOG.info("querying "+network.name()+" variables for "+state.name()+" stations");
			taskStatus = "["+currentState+" Process]: querying "+network.name()+" variables for "+state.name()+" stations";
			addVariables(result);
			
			if ( buildType == BuildType.BUILD ) { 
				LOG.info("writing "+network.name()+" station meta data for "+state.name());
				taskStatus = "["+currentState+" Process]: writing "+network.name()+" station meta data for "+state.name();
				writeStationMetaData(result, network);
			}
			
			taskStatus = "["+currentState+" Process]: waiting for queue to free";
			taskState = TaskState.WAITING;
			queue.put(result);
			taskState = TaskState.RUNNING;
		} else { 
			this.totalUnits -= APROX_STATIONS;
			dbuilder.subtractFromTotalUnits(APROX_STATIONS);
		}
	}

	/**
	 * Filters out stations that are not already in the database or which do not have an ending date of 9999-12-31. 
	 * This results in only stations for which data was originally built and which have (as of the build or last update)
	 * current reporting records being updated.
	 * 
	 * @param result
	 * @param state
	 * @throws SQLException
	 */
	/*
	 * Change log Ben Kutsch 03-26-06
	 * I changed the HashMap values component to a List from String, I also changed the Hasmap traversing to remove prevoulsy visted stations.
	 * This way multiple stations can be stored in the List for on NetworkID and further each station is removed from the list after it has been
	 * added to the filteredResults object
	 * 
	 */
	private Collection<ACISResult> filterStations(Collection<ACISResult> result, USState state) throws SQLException { 
		Connection c = null;
		ArrayList<ACISResult> filteredResults = new ArrayList<ACISResult>();
		try { 
			c = source.getConnection();
			PreparedStatement stmt = c.prepareStatement(stationQuery);
			stmt.setString(1, state.name());
			HashMap<String, List<String>> stations = new HashMap<String, List<String>>();
			List<String> l=null;
			ResultSet rs = stmt.executeQuery();
			while ( rs.next() ) {
				
				/* changed so muliple ststion IDs can be linked to one networkID */				
				if (!stations.containsKey(rs.getString(1))){
					stations.put(rs.getString(1), l=new ArrayList<String>());
					l.add(rs.getString(2));
				}
				else{
					l=stations.get(rs.getString(1));
					l.add(rs.getString(2));
				}
			}
			String s_id=null;
			for ( ACISResult res : result ) {
				l=stations.get(res.getValue(ACISMetaField.NETWORKID));

				if (l!=null && !l.isEmpty())
					s_id = l.get(0);
				else
					s_id=null;
				
 				if ( ( s_id == null) ) {
					short[] end = (short[])res.getValue(ACISMetaField.ENDDATE);
					LOG.info("Skipping update of station "+res.getValue(ACISMetaField.NETWORKID)+" ending on "+end[0]+"."+end[1]+"."+end[2]);
				} else {
					/* add the generated id for the station for later updates */
					res.setStationID(s_id);
					filteredResults.add(res);
					l.remove(0);	
				}
			}
			
		} catch ( SQLException sqe ) { 
			LOG.error("Error querying stations to filter", sqe);
			throw sqe;
		} finally { 
			c.close();
		}
		
		return filteredResults;
	}
	
	private void addVariables(Collection<ACISResult> results) throws ACISAccessException {
		this.totalUnits += results.size() * ACISVariableEnumeration.values().length;
		
		boolean try_again = true;
		int try_count = 0;
		
		try {		
			for ( ACISResult result : results ) { 
				try_again = true;
;				while ( try_again ) {
					try {
						ACISResultFactory.getInstance(lastRun, buildType).addAvailableVariables(result, this);
						try_again = false;
					} catch ( ACISAccessException e ) { 
						if ( ++try_count < ATTEMPTS ) {
							LOG.info("caught exception communicating with acis, will wait and try again.  Attempt: "+try_count);
							try_again = true;
							ACISResultFactory.release();
							Thread.sleep(WAIT_TIME);
						} else { 
							try_again = false;
							throw e;
						}
					}
				} 
			}
		} catch ( ACISAccessException e ) { 
			throw e;
		} catch ( Exception e ) { 
			LOG.error("unknown exception adding variables", e);
			throw new ACISAccessException(e.getMessage());
		} 
	}
	
	private void writeStationMetaData(Collection<ACISResult> results, WeatherStationNetwork network) throws SQLException, ACISAccessException { 
		Connection conn = null;
		try {
			conn = source.getConnection();
			conn.setAutoCommit(false);		
			
			PreparedStatement station_insert = conn.prepareStatement(stationInsert);
			PreparedStatement location_insert = conn.prepareStatement(locationInsert);
			PreparedStatement location_station_insert = conn.prepareStatement(locationStationInsert);
			PreparedStatement network_station_insert = conn.prepareStatement(networkStationInsert);
			
			int count = 0;
			String station_id = null;
			String location_id = null;
			
			for ( ACISResult result : results ) {			
				station_id = String.valueOf(IDMap.getInstance().getID((String)result.getValue(ACISMetaField.NETWORKID)));
				location_id = new UID().toString();
				
				station_insert.setString(1, (String)result.getValue(ACISMetaField.STATION_NAME));
				station_insert.setString(2, (String)result.getValue(ACISMetaField.STATE));
				station_insert.setString(3, (String)result.getValue(ACISMetaField.COUNTY));
				station_insert.setString(4, (String)result.getValue(ACISMetaField.CLIMATE_DIVISION));
				
				short[] u_start = (short[])result.getValue(ACISMetaField.STARTDATE);
				String start_date = u_start[0]+"/"+u_start[1]+"/"+u_start[2];
				
				short[] u_end = (short[])result.getValue(ACISMetaField.ENDDATE);
				String end_date = u_end[0]+"/"+u_end[1]+"/"+u_end[2];
				
				station_insert.setString(5, start_date);
				station_insert.setString(6, end_date);
				station_insert.setString(7, station_id);
				
				try {
					double lat = (Double)result.getValue(ACISMetaField.LATITUDE);
					location_insert.setFloat(1, Math.round(lat*1000.00)/1000.0f);
				} catch ( SQLException sqe ) { 
					LOG.warn("could not parse out latitude value for station "+station_id);
					location_insert.setDouble(1, 0);
				}
				
				try {
					double lon = (Double)result.getValue(ACISMetaField.LONGITUDE);
					location_insert.setFloat(2, Math.round(lon*1000.0)/1000.0f);
				} catch ( SQLException sqe ) { 
					LOG.warn("could not parse out longitude value for station "+station_id);
					location_insert.setDouble(2, 0);
				}
				
				try {
					double elevation = (Double)result.getValue(ACISMetaField.ELEVATION);
					location_insert.setFloat(3, Math.round(elevation*1000.0)/1000.0f);
				} catch ( SQLException sqe ) { 
					LOG.warn("could not parse out elevation value for station "+station_id);
					location_insert.setDouble(3, 0);
				}
				
				location_insert.setString(4, location_id);
				
				location_station_insert.setString(1, location_id);
				location_station_insert.setString(2, station_id);
				location_station_insert.setString(3, start_date);
				location_station_insert.setString(4, end_date);
				
				network_station_insert.setString(1, station_id);
				network_station_insert.setString(2, (String)result.getValue(ACISMetaField.NETWORKID));
				network_station_insert.setInt(3, network.getNetworkID());
				network_station_insert.setInt(4, (Integer)result.getValue(ACISMetaField.ACIS_ID));
				
				station_insert.execute();
				location_insert.execute();
				location_station_insert.execute();
				network_station_insert.execute();
				writeStationVariables(result, station_id, conn);
				
				if ( (++count % 100) == 1 )
					conn.commit();
			
				unitsComplete++;
			}
			
			conn.commit();
		} catch ( SQLException sqe ) { 
			LOG.error("error writing meta data");
			conn.rollback();
			throw sqe; 
		} catch ( ACISAccessException uae ) { 
			LOG.error("error getting variable dates", uae);
			conn.rollback();
			throw uae;
		} finally { 
			if ( conn != null ) {
				conn.close();
			}
		}
	}
	
	private void updateStationEnd(Collection<ACISResult> results) throws SQLException { 
		Connection conn = null;
		try {
			conn = source.getConnection();
			conn.setAutoCommit(false);		
			
			PreparedStatement update = conn.prepareStatement(updateStation);
			
			int count = 0;			
			for ( ACISResult result : results ) { 
				short[] u_end = (short[])result.getValue(ACISMetaField.ENDDATE);
				String end_date = u_end[0]+"/"+u_end[1]+"/"+u_end[2];
				update.setString(1, end_date);
				update.setString(2, (String)result.getValue(ACISMetaField.STATIONID));
				
				if ( (++count % 100) == 1 )
					conn.commit();
			}
			
			conn.commit();
			
		} catch ( SQLException sqe ) { 
			LOG.error("error updating station end date");
			conn.rollback();
			throw sqe; 
		} finally { 
			if ( conn != null ) {
				conn.close();
			}
		}
	}
	
	private void updateVariableEnd(Collection<ACISResult> results) throws SQLException {
		Connection conn = null;
		try {
			conn = source.getConnection();
			conn.setAutoCommit(false);		
			
			PreparedStatement select = conn.prepareStatement(variableSelection);
			PreparedStatement update = conn.prepareStatement(variableUpdate);
			
			int count = 0;			
			for ( ACISResult result : results ) {
				ACISVariableResult vars = result.getAvailableVariables();
				select.setString(1, (String)result.getValue(ACISMetaField.STATIONID));
				ResultSet variables = select.executeQuery();
				while ( variables.next() ) { 
					ACISVariableEnumeration var = ACISVariableEnumeration.valueOf(variables.getString(1));
					
					/* if the variable was skipped due to data ranges, the variable will be ommitted */
					if ( !vars.contains(var) )
						continue;
					
					String var_id = variables.getString(2);
					vars.updateVariableID(var, var_id);
					
					short[] end_date = vars.getEndDate(var);
					update.setString(1, end_date[0]+"/"+end_date[1]+"/"+end_date[2]);
					update.setString(2, var_id);
					update.execute();
				}
								
				if ( (++count % 100) == 1 )
					conn.commit();
			}
			
			conn.commit();
			
		} catch ( SQLException sqe ) { 
			LOG.error("error updating station end date");
			conn.rollback();
			throw sqe; 
		} finally { 
			if ( conn != null ) {
				conn.close();
			}
		}
	}
	
	/* this method takes a connection as an argument as it is called as part of a transaction on that connection */
	private void writeStationVariables(ACISResult res, String station_id, Connection c) throws SQLException, ACISAccessException { 
		PreparedStatement variable_insert = c.prepareStatement(variableInsert);
		ACISVariableResult variables = res.getAvailableVariables();
		
		for ( ACISVariableEnumeration var : variables.variables() ) {
			variable_insert.setString(1, variables.getVariableId(var));
			variable_insert.setString(2, var.name());
			variable_insert.setString(3, station_id);
			
			short[] start_date = variables.getStartDate(var);
			variable_insert.setString(4, start_date[0]+"/"+start_date[1]+"/"+start_date[2]);
			
			short[] end_date = variables.getEndDate(var);
			variable_insert.setString(5, end_date[0]+"/"+end_date[1]+"/"+end_date[2]);
			
			variable_insert.execute();
		}
	}
}
