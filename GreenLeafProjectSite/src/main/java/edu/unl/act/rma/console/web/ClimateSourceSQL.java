/* Created On: Aug 23, 2005 */
package edu.unl.act.rma.console.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;

/**
 * @author Ian Cottingham
 *
 */
public class ClimateSourceSQL {
	
	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, DIRDataServiceImpl.class);
	
	private static final String CREATE_DAILY = "CREATE TABLE `daily` (`daily_id` int(11) " +
			"NOT NULL auto_increment,`variable_id` varchar(64) default NULL,`year` int(11) NOT " +
			"NULL default '0',`month_num` int(11) NOT NULL default '0', `day_num` int(11) NOT NULL default " +
			"'0', `value` float(20,3) default NULL, `flag` varchar(10) default NULL, `backup_value` float(20,3) " +
			"default NULL, PRIMARY KEY  (`daily_id`), KEY `idx_var_yr_mnth_day` (`variable_id`,`year`,`month_num`,`" +
			"day_num`) ) ENGINE=MyISAM DEFAULT CHARSET=latin1 MAX_ROWS=4294967295 AVG_ROW_LENGTH=38;";
	
	private static final String CREATE_WEEKLY = "CREATE TABLE `weekly` (`weekly_id` int(11) NOT NULL auto_increment," +
			"`variable_id` varchar(64) default NULL,`year` int(11) default NULL,`week_num` int(11) default NULL,`value` " +
			"float(20,3) default NULL,PRIMARY KEY  (`weekly_id`),KEY `idx_variable` (`variable_id`),KEY `idx_date` (`year`," +
			"`week_num`)) ENGINE=MyISAM DEFAULT CHARSET=latin1;";
	
	private static final String CREATE_MONTHLY = "CREATE TABLE `monthly` (`monthly_id` int(11) NOT NULL auto_increment," +
			"`variable_id` varchar(64) default NULL,`year` int(11) default NULL,`month_num` int(11) default NULL,`value` " +
			"float(20,3) default NULL,PRIMARY KEY  (`monthly_id`),KEY `idx_variable` (`variable_id`),KEY `idx_date` (`year`," +
			"`month_num`)) ENGINE=MyISAM DEFAULT CHARSET=latin1;";

	public static final String CREATE_ABSOLUTE_LOCATION = "CREATE TABLE `absolute_location` (`latitude` float(10,4) NOT NULL default '0.0000',"+
		"`longitude` float(10,4) NOT NULL default '0.0000'," +
		"`elevation` float(10,4) default NULL," +
		"`location_id` varchar(64) NOT NULL default ''," +
		"PRIMARY KEY  (`location_id`)," +
		"KEY `idx_lat_long` (`latitude`,`longitude`)" +
		") ENGINE=MyISAM DEFAULT CHARSET=latin1";
	
	public static final String CREATE_LOCATION_LINK = "CREATE TABLE `location_station_link` (" +
			"`link_id` int(11) NOT NULL auto_increment," +
			"`location_id` varchar(64) NOT NULL default '0'," +
			"`station_id` varchar(64) NOT NULL default '0'," +
			"`start_date` date NOT NULL default '0000-00-00'," +
			"`end_date` date default NULL," +
			"PRIMARY KEY  (`link_id`)," +
			"KEY `idx_location` (`location_id`)," +
			"KEY `idx_station` (`station_id`)" +
			") ENGINE=MyISAM DEFAULT CHARSET=latin1";
	
	public static final String CREATE_NETWORK_LINK = "CREATE TABLE `network_station_link` (" +
			"`network_station_link_id` int(11) NOT NULL auto_increment," +
			"`network_id` varchar(20) NOT NULL default ''," +
			"`network_type_id` int(11) NOT NULL default '0'," +
			"`station_id` varchar(64) NOT NULL default '0'," +
			"`ucan_id` int(11) NOT NULL default '0'," +
			"PRIMARY KEY  (`network_station_link_id`)," +
			"KEY `idx_network` (`network_id`)," +
			"KEY `idx_station` (`station_id`)," +
			"KEY `idx_network_type` (`network_type_id`)" +
			") ENGINE=MyISAM DEFAULT CHARSET=latin1";
	
	public static final String CREATE_NETWORK_TYPE = "CREATE TABLE `network_type` (" +
			"`network_type_id` int(11) NOT NULL auto_increment," +
			"`network_name` varchar(50) NOT NULL default ''," +
			"PRIMARY KEY  (`network_type_id`)" +
			") ENGINE=MyISAM DEFAULT CHARSET=latin1";
	
	public static final String CREATE_SOURCE_TYPE = "CREATE TABLE `source_type` (" +
			"`source_type_id` tinyint(4) NOT NULL auto_increment," +
			"`source_name` varchar(20) NOT NULL default ''," +
			"PRIMARY KEY  (`source_type_id`)" +
			") ENGINE=MyISAM DEFAULT CHARSET=latin1";
	
	public static final String CREATE_STATION = "CREATE TABLE `station` (" +
			"`station_name` varchar(50) default NULL," +
			"`state` varchar(15) default NULL," +
			"`county` varchar(50) default NULL," +
			"`climate_div` varchar(50) default NULL," +
			"`abs_start_date` date default NULL," +
			"`abs_end_date` date default NULL," +
			"`default_state` char(1) default NULL," +
			"`source_key` int(11) NOT NULL default '0'," +
			"`station_id` varchar(64) NOT NULL default ''," +
			"PRIMARY KEY  (`station_id`)" +
			") ENGINE=MyISAM DEFAULT CHARSET=latin1";
	
	public static final String CREATE_VARIABLE = "CREATE TABLE `variable` (" +
			"`variable_id` varchar(64) NOT NULL default ''," +
			"`variable_name` varchar(20) default NULL," +
			"`station_id` varchar(64) NOT NULL default '0'," +
			"`start_date` date default NULL," +
			"`end_date` date default NULL," +
			"`source_type_id` int(11) NOT NULL default '0'," +
			"`missing_percent` float NOT NULL default '0',"+
			"PRIMARY KEY  (`variable_id`)," +
			"KEY `idx_station` (`station_id`)," +
			"KEY `idx_source` (`source_type_id`)" +
			") ENGINE=MyISAM DEFAULT CHARSET=latin1";
	
	private static final String CREATE_HELP_UNION = "CREATE TABLE `help_union` (`ID` int(11) NOT NULL auto_increment,`value`int(11) NOT NULL,PRIMARY KEY " +
			"(`ID`)) ENGINE=MyISAM DEFAULT CHARSET=latin1";
	
	private static final String CREATE_HELP_UNION_YEAR = "CREATE TABLE `help_union_year` (`ID` int(11) NOT NULL auto_increment,`month_num` int(11) NOT NULL," +
			"`day_num` int(11) NOT NULL,`value` int(11) NOT NULL default '-99',PRIMARY KEY  (`ID`)) ENGINE=MyISAM DEFAULT CHARSET=latin1";

	public static final String CREATE_BUILD_META = "CREATE TABLE `buildmeta` (`metaid` int(11) NOT NULL auto_increment,`name` varchar(64) " +
			"default NULL, `date` date default NULL, PRIMARY KEY (`metaid`) ) ENGINE=MyISAM DEFAULT CHARSET=latin1; ";
		
	private static DataSource source = DataSourceInjector.injectDataSource(DataSourceTypes.CLIMATIC_BUILD);
	
	public static void createSchemas() throws SQLException { 
		Connection c = null;
		StringBuffer union_daily = new StringBuffer();
		StringBuffer union_weekly = new StringBuffer();
		StringBuffer union_monthly = new StringBuffer();
		
		try { 
			c = source.getConnection();

			/* requires that a climatic database exist */
			c.createStatement().execute("DROP TABLE IF EXISTS absolute_location");
			c.createStatement().execute("DROP TABLE IF EXISTS location_station_link");
			c.createStatement().execute("DROP TABLE IF EXISTS network_station_link");
			c.createStatement().execute("DROP TABLE IF EXISTS network_type");
			c.createStatement().execute("DROP TABLE IF EXISTS source_type");
			c.createStatement().execute("DROP TABLE IF EXISTS station");
			c.createStatement().execute("DROP TABLE IF EXISTS variable");
			c.createStatement().execute("DROP TABLE IF EXISTS daily");
			c.createStatement().execute("DROP TABLE IF EXISTS weekly");
			c.createStatement().execute("DROP TABLE IF EXISTS monthly");
			c.createStatement().execute("DROP TABLE IF EXISTS buildmeta");
			c.createStatement().execute("DROP TABLE IF EXISTS buildmeta");
			c.createStatement().execute("DROP TABLE IF EXISTS help_union");
			c.createStatement().execute("DROP TABLE IF EXISTS help_union_year");
			
			c.createStatement().execute(CREATE_ABSOLUTE_LOCATION);
			c.createStatement().execute(CREATE_LOCATION_LINK);
			c.createStatement().execute(CREATE_NETWORK_LINK);
			c.createStatement().execute(CREATE_NETWORK_TYPE);
			c.createStatement().execute(CREATE_SOURCE_TYPE);
			c.createStatement().execute(CREATE_STATION);
			c.createStatement().execute(CREATE_VARIABLE);
			c.createStatement().execute(CREATE_BUILD_META);
			c.createStatement().execute(CREATE_HELP_UNION);
			c.createStatement().execute(CREATE_HELP_UNION_YEAR);
			
			c.createStatement().execute("INSERT INTO source_type (source_type_id, source_name) VALUES (2, 'ACIS')");
			c.createStatement().execute("INSERT INTO network_type (network_type_id, network_name) VALUES(1, 'COOP')");
			c.createStatement().execute("INSERT INTO network_type (network_type_id, network_name) VALUES(2, 'AWDN')");
			
			c.createStatement().execute(CREATE_DAILY);
			c.createStatement().execute(CREATE_WEEKLY);
			c.createStatement().execute(CREATE_MONTHLY);
			
			PreparedStatement help_statement = c.prepareStatement("insert into `help_union` values(?,-99)");
			for ( int i=1; i<53; i++ ) { 
				help_statement.setInt(1, i);
				help_statement.execute();
			}
			
			help_statement = c.prepareStatement("insert into `help_union_year` values(?, ?, ?, -99)");
			DateTime dt = new DateTime(2000,1,1,0,0,0,0,GregorianChronology.getInstance());
			
			for ( int i=1; i<367; i++ ) { 
				help_statement.setInt(1, i);
				help_statement.setInt(2, dt.getMonthOfYear());
				help_statement.setInt(3, dt.getDayOfMonth());
				
				help_statement.execute();
				
				dt = dt.plusDays(1);
			}
			
		} catch ( SQLException sqe ) { 
			throw sqe;
		} catch ( Exception e ) { 
			LOG.error("unknown exception", e);
			throw new RuntimeException(e.getMessage());
		} finally {
			if ( c != null ) { 
				c.close();
			}
		}
	}
}
