/* Created On: Jun 13, 2005 */
package edu.unl.act.rma.console.acis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import javax.sql.DataSource;

import edu.unl.act.rma.firm.core.CalendarDataParser;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.Task;
import edu.unl.act.rma.firm.core.TaskState;

/**
 * @author Ian Cottingham
 *
 */
public class ACISDataWriter extends Task {

	private Logger LOG = LogManager.getLogger(Loggers.SYSTEM_LOG, ACISDataWriter.class);	
	
	public final String DESCRIPTION = "A task to write station data to db";
		
	private static final String dailyInsert = "LOAD DATA LOCAL INFILE ? INTO TABLE daily FIELDS TERMINATED BY ',' LINES TERMINATED BY '|' (variable_id, year, month_num, day_num, value)";
	private static final String weeklyInsert = "LOAD DATA LOCAL INFILE ? INTO TABLE weekly FIELDS TERMINATED BY ',' LINES TERMINATED BY '|' (variable_id, year, week_num, value)";
	private static final String monthlyInsert = "LOAD DATA LOCAL INFILE ? INTO TABLE monthly FIELDS TERMINATED BY ',' LINES TERMINATED BY '|' (variable_id, year, month_num, value)";
	
	private static final String checkVariable ="SELECT distinct variable_id from Variable where variable_id=?";
	
	private static final String missingUpdate = "UPDATE variable SET missing_percent = ? WHERE variable_id = ?";
	
	private static final DataSource source = DataSourceInjector.injectDataSource(DataSourceTypes.CLIMATIC_BUILD);
		
	private PreparedStatement dailyInsertStatement;
	private PreparedStatement weeklyInsertStatement;
	private PreparedStatement monthlyInsertStatement;
	private PreparedStatement variableUpdate;
	
	private Connection dbConnection;
	
	private String tmpDataPath;
	private BlockingQueue<Collection<ACISResult>> queue;
	private boolean finished;
	private BuildType buildType;
	
	private String currentState;
	
	public ACISDataWriter(String tmpDataPath, BlockingQueue<Collection<ACISResult>> queue, BuildType buildType) { 
		super("Ucan Data Writer");
		
		taskStatus = "Removing table indices";
		try {
			this.clearIndex();
		} catch ( Exception e ) { 
			LOG.warn("An error occurred removing the table indices", e);
		}
		
		taskStatus = "Initialized and waiting...";
		this.tmpDataPath = tmpDataPath;
		this.queue = queue;
		this.finished = false;
		this.buildType = buildType;
	}
	
	public ACISDataWriter(String tmpDataPath, BlockingQueue<Collection<ACISResult>> queue, BuildType buildType, Semaphore s) { 
		super("Ucan Data Writer", s, true, false);
		
		taskStatus = "Removing table indices";
		try {
			this.clearIndex();
		} catch ( Exception e ) { 
			LOG.warn("An error occurred removing the table indices", e);
		}
		
		taskStatus = "Initialized and waiting...";
		this.tmpDataPath = tmpDataPath;
		this.queue = queue;
		this.finished = false;
		this.buildType = buildType;
	}

	public void logic() throws Exception { 
		Collection<ACISResult> network_result = null;
		boolean is_first = true;
		while ( !finished || (queue.size() > 0) ) { 
			if ( halted ) { 
				break;
			}

			taskState = TaskState.WAITING;
			taskStatus = "waiting for result data";
			network_result = queue.take();
			taskStatus = "";
			taskState = TaskState.RUNNING;
			
			is_first = true;
						
			for ( ACISResult acis_result : network_result ) {				
				if ( is_first) { 
					currentState = acis_result.getState().name();
					LOG.info("writing "+acis_result.getNetwork().name()+" variable data for "+acis_result.getState().name());
					is_first = false;
				}

				writeDailyData(acis_result);
				unitsComplete++;
			}
			
			network_result = null;
		}
		
		if ( halted ) {
			taskState = TaskState.HALTED;
			LOG.info("The data writer process has been halted");
			currentState = "Process halted";
		} else {
			taskStatus = "Updating the meta data";
			createMetaData();
			
			taskStatus = "Indexing the database";
			index();
		}
	}
	
	private void createMetaData() throws SQLException { 
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
		Connection c = null;
		
		try {
			c = source.getConnection();
			
			/* clear out the old values */
			c.createStatement().execute("delete from buildmeta");
			
			PreparedStatement in_stmt = c.prepareStatement("insert into buildmeta (name, date) VALUES (?,?)");
			
			{
				ResultSet rs = c.createStatement().executeQuery("SELECT max(end_date) FROM variable");
				rs.next();
				String daily_str = rs.getString(1);
				
				Calendar cal = new GregorianCalendar();
				cal.set(Calendar.YEAR, Integer.parseInt(daily_str.substring(0,4)));
				cal.set(Calendar.MONTH, Integer.parseInt(daily_str.substring(5,7)) - 1);
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(daily_str.substring(8)));
	
				in_stmt.setString(1, "DAILY");
				in_stmt.setString(2, formatter.format(cal.getTime()));
				in_stmt.execute();
			}
			
			{
				PreparedStatement stmt = c.prepareStatement("SELECT max(year) year FROM weekly WHERE value<>-99");
				PreparedStatement stmt2 = c.prepareStatement("SELECT max(week_num) week_num FROM weekly WHERE value <>-99 and year=?");
				
				ResultSet rs = stmt.executeQuery();
				int year = 0;
				if ( rs.next() ) 
					year = rs.getInt(1);
				else 
					throw new SQLException("no data found");
				
				stmt.close();
				stmt2.setInt(1, year);
				rs = stmt2.executeQuery();
				if ( rs.next() ) {
					Calendar cal = new GregorianCalendar();
					cal.set(Calendar.YEAR, year);
					cal.set(Calendar.DAY_OF_YEAR, rs.getInt(1)*7);
					
					in_stmt.setString(1, "WEEKLY");
					in_stmt.setString(2, formatter.format(cal.getTime()));
					in_stmt.execute();
				}
				
				stmt2.close();
			}
			
			{
				PreparedStatement stmt = c.prepareStatement("SELECT max(year) year FROM monthly WHERE value<>-99");
				PreparedStatement stmt2 = c.prepareStatement("SELECT max(month_num) FROM monthly WHERE value <>-99 and year=?");
				
				ResultSet rs = stmt.executeQuery();
				int year = 0;
				if ( rs.next() ) 
					year = rs.getInt(1);
				else 
					throw new SQLException("no data found");
				
				stmt.close();
				stmt2.setInt(1, year);
				rs = stmt2.executeQuery();
				if ( rs.next() ) {
					
					int month = rs.getInt(1);
					
					Calendar cal = new GregorianCalendar(year, month-1, 1);
					cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
					
					in_stmt.setString(1, "MONTHLY");
					in_stmt.setString(2, formatter.format(cal.getTime()));
					in_stmt.execute();
				}
				
				stmt2.close();
			}
		} catch ( SQLException sqe ) { 
			throw sqe; 
		} finally {
			if ( c != null ) { 
				c.close();
			}
		}
	}
	
	public void aquireLogic() throws Exception {
		finished = true;
	}

	/*
	 * Change-log:
	 * 03-30-06 Ben Kutsch
	 * I added a check to see if variable_ids exist in the database before wrtitng the data. This avoids orphaned data.
	 * 09-06-07 Ben Kutsch
	 * I added High and Low Temp to the weekly and monthly summarization as to task FARM-661
	 */
	private void writeDailyData(ACISResult result) throws ACISAccessException, SQLException { 
		try {
			initDbConnection();
			ACISVariableResult variables = result.getAvailableVariables();
	
			for ( ACISVariableEnumeration variable : variables.variables() ) {
				short[] start_date = variables.getStartDate(variable);
				short[] end_date = variables.getEndDate(variable);
				
				float missing_percent;				
				taskStatus = "["+currentState+" Process]: writing "+variable.name()+" data for station "+result.getValue(ACISMetaField.NETWORKID)+" for period "+
							start_date[0]+"/"+start_date[1]+"/"+start_date[2]+" to "+end_date[0]+"/"+end_date[1]+"/"+end_date[2];
				
				
				if(checkVars(variables.getVariableId(variable))){
					CalendarDataParser iterator = new CalendarDataParser(variables.getData(variable), start_date[0], start_date[1], start_date[2]);		
					
					if ( (variable == ACISVariableEnumeration.NORMAL_TEMP) || (variable == ACISVariableEnumeration.PRECIP) 
							|| (variable == ACISVariableEnumeration.HIGH_TEMP)  ||(variable == ACISVariableEnumeration.LOW_TEMP) ) {
						missing_percent = insertAndSummarizeDaily(variables.getVariableId(variable), variable, iterator);
						executeStatements(true);
					} else {
						missing_percent = insertStationDaily(variables.getVariableId(variable), iterator);
						executeStatements(false);
					}
					
					if ( this.buildType == BuildType.BUILD ) { 
						variableUpdate.setFloat(1, missing_percent);
						variableUpdate.setString(2, variables.getVariableId(variable));
						variableUpdate.execute();
					}
					
					dbConnection.commit();
				}
				else
					LOG.info("No META data: skipping "+variable.name()+" data for station "+result.getValue(ACISMetaField.NETWORKID));
				
			}
		} catch ( SQLException sqe ) { 
			throw sqe;
		} catch ( IOException ioe ) { 
			LOG.error("IO exception on daily write", ioe);
			throw new ACISAccessException(ioe.getMessage());
		} catch ( Exception e ) { 
			LOG.error("unknown exception on daily write", e);
			throw new ACISAccessException(e.getMessage());
		} finally { 
			if ( dbConnection != null ) {
				/* clean out the statement map */
				if ( dailyInsertStatement != null ) {
					dailyInsertStatement.close();
				}
				
				if ( weeklyInsertStatement != null ) {
					weeklyInsertStatement.close();
				}
				
				if ( monthlyInsertStatement != null ) { 
					monthlyInsertStatement.close();
				}
				
				dbConnection.close();	
				dbConnection = null;
			}
		}
	} //TODO: need to rollback the connection when an error occurs

	private void initDbConnection() throws Exception { 
		dbConnection = source.getConnection();
		dbConnection.setAutoCommit(false);
		/* generate the required prepared statements */
		
		dailyInsertStatement = dbConnection.prepareStatement(dailyInsert);
		weeklyInsertStatement = dbConnection.prepareStatement(weeklyInsert);
		monthlyInsertStatement = dbConnection.prepareStatement(monthlyInsert);
		variableUpdate = dbConnection.prepareStatement(missingUpdate);
	}
	
	private void executeStatements(boolean summarize) throws SQLException { 
		
		/* Even though we no longer use the Period00 - Period99 database structure we still
		 * write the file to the disk with this construct.  This allows for smaller files to 
		 * be read and inserted into the database at a single time.  It also complicated logic
		 * somewhat to remove the file writer convention and had the potential to increase
		 * databuild errors.  If it isn't broken, then don't fix it.
		 */
		
		try {			
			dailyInsertStatement.setObject(1, this.tmpDataPath+"daily.99");
			dailyInsertStatement.execute();
			
			if ( summarize ) {				
				weeklyInsertStatement.setObject(1, this.tmpDataPath+"weekly.99");
				weeklyInsertStatement.execute();
			
				monthlyInsertStatement.setObject(1, this.tmpDataPath+"monthly.99");
				monthlyInsertStatement.execute();
			}
			
			int retries = 0;
			for ( int dec=0; dec<10; dec++ ) {				
				for ( int years = 0; years<5; years++ ) {
					String str = String.valueOf(dec)+String.valueOf(years);
						try {
						dailyInsertStatement.setObject(1, this.tmpDataPath+"daily."+str);
						dailyInsertStatement.execute();
						
						if ( summarize ) {						
							weeklyInsertStatement.setObject(1, this.tmpDataPath+"weekly."+str);
							weeklyInsertStatement.execute();
						
							monthlyInsertStatement.setObject(1, this.tmpDataPath+"monthly."+str);
							monthlyInsertStatement.execute();
						}
						
						dbConnection.commit();
						retries = 0;
						
					} catch ( Exception ce ) {
						if ( retries < 5 ) {
							retries++; 
							LOG.error("A db communication error occured, will retry in 5 min.  Attempt #"+retries);
							Thread.sleep((1000L*60)*5);
							
							try { 
								initDbConnection(); // reconnect to the database
							} catch ( Exception e ) { /* do nothing, this will be caught in another attempt */ }
							
							years--; // decrement the year and try again
							continue;
						} else { 
							RuntimeException re = new RuntimeException("Db communication has failed after 5 attempts to reconnect");
							re.initCause(ce);
							throw re;
						}
					}
				}
			}
		} catch ( SQLException sqe ) { 
			throw sqe;
		} catch ( InterruptedException ise ) { 
			RuntimeException re = new RuntimeException("Could not hold the thread to wait for a db recovery error");
			re.initCause(ise); 
			throw re;
		}
	}
			
	private float insertStationDaily(String variable_id, CalendarDataParser iterator) throws SQLException, IOException {
		ACISDataFileMapper mapper = new ACISDataFileMapper(this.tmpDataPath, variable_id);
		float total = 0, missing = 0;
		while ( iterator.hasNextDay() ) {
			float value = iterator.nextDay();
			
			if ( value == ACISResultFactory.ACIS_MISSING ) { 
				missing++;
			}
			total++;
			mapper.writeDaily(iterator.getYear(CalendarPeriod.DAILY), iterator.getMonthOfYear(), iterator.getDayOfMonth(), value);
		}
		
		mapper.cleanUp();
		return (missing/total);
	}
	
	private float insertAndSummarizeDaily(String variable_id, ACISVariableEnumeration var, CalendarDataParser iterator) throws SQLException, IOException {
		
		ACISDataFileMapper mapper = new ACISDataFileMapper(this.tmpDataPath, variable_id);
		float total = 0, missing = 0;
		while ( iterator.hasNextDay() ) {
			float value = iterator.nextDay();
			
			if ( value == ACISResultFactory.ACIS_MISSING ) { 
				missing++;
			}
			total++;
			
			mapper.writeDaily(iterator.getYear(CalendarPeriod.DAILY), iterator.getMonthOfYear(), iterator.getDayOfMonth(), value);
			
			if ( iterator.hasWeekData() ) {
				if ( var == ACISVariableEnumeration.PRECIP ) 
					mapper.writeWeekly(iterator.getYear(CalendarPeriod.WEEKLY), iterator.getWeekOfYear(), iterator.weekSum());
				else 
					mapper.writeWeekly(iterator.getYear(CalendarPeriod.WEEKLY), iterator.getWeekOfYear(), iterator.weekAverage());
			}
			
			if ( iterator.hasMonthData() ) {
				if ( var == ACISVariableEnumeration.PRECIP ) 
					mapper.writeMonthly(iterator.getYear(CalendarPeriod.MONTHLY), iterator.getMonthOfYear(), iterator.monthSum());
				else 
					mapper.writeMonthly(iterator.getYear(CalendarPeriod.MONTHLY), iterator.getMonthOfYear(), iterator.monthAverage());
			}
		}
		
		mapper.cleanUp();
		return (missing/total);
	}
	
	/*
	 * Change-log
	 * 03-30-2006 Ben Kutsch
	 * I added this function to check if variables exsit in the database.
	 */
	private boolean checkVars(String variable_id) throws SQLException,Exception{
		PreparedStatement stmt = null;
		String s=checkVariable;
		try{
			stmt=dbConnection.prepareStatement(s);
			stmt.setString(1,variable_id);
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next())
				return true;
			else
				return false;
			
		} catch ( SQLException sqe ) { 
			throw sqe;
		} catch ( Exception e ) { 
			LOG.error("unknown exception checking variables", e);
			throw new ACISAccessException(e.getMessage());
		} finally { 
			if ( stmt != null ) {
				stmt.close();
				stmt = null;
			}
		}
	}
	
	private void clearIndex() throws Exception { 
		Connection conn = source.getConnection();
		conn.createStatement().execute("ALTER TABLE daily DROP INDEX idx_var_yr_mnth_day");
		conn.createStatement().execute("ALTER TABLE weekly DROP INDEX idx_variable");
		conn.createStatement().execute("ALTER TABLE weekly DROP INDEX idx_date");
		conn.createStatement().execute("ALTER TABLE monthly DROP INDEX idx_variable");
		conn.createStatement().execute("ALTER TABLE monthly DROP INDEX idx_date");		
	}
	
	private void index() throws Exception {  
		Connection conn = source.getConnection();
		conn.createStatement().execute("ALTER TABLE daily ADD INDEX idx_var_yr_mnth_day (variable_id,year,month_num,day_num)");
		conn.createStatement().execute("ALTER TABLE weekly ADD INDEX idx_variable (variable_id)");
		conn.createStatement().execute("ALTER TABLE weekly ADD INDEX idx_date (year,week_num)");		
		conn.createStatement().execute("ALTER TABLE monthly ADD INDEX idx_variable (variable_id)");
		conn.createStatement().execute("ALTER TABLE monthly ADD INDEX idx_date (year,month_num)");			
	}
}
