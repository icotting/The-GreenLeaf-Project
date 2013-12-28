package edu.unl.act.rma.console.web;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;

public class StreamFlowSourceSQL {
	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG,
			StreamFlowDataServiceImpl.class);

	private static final String CREATE_DAILY = "CREATE TABLE `daily` ("
			+ "`daily_id` varchar(64) NOT NULL default '',"
			+ "`variable_id` varchar(64) NOT NULL default '',"
			+ "`year` int(11) NOT NULL default '0',"
			+ "`month_num` int(11) NOT NULL default '0', "
			+ "`day_num` int(11) NOT NULL default '0',"
			+ "`value` double(8,2) default NULL,"
			+ "`qualification_code` varchar(20) default '',"
			+ "`status` varchar(20) default '',"
			+ "PRIMARY KEY  (`daily_id`), "
			+ "KEY `idx_variable_id` (`variable_id`)"
			+ ") ENGINE=MyISAM DEFAULT CHARSET=latin1 MAX_ROWS=4294967295 AVG_ROW_LENGTH=38;";

	private static final String CREATE_WEEKLY = "CREATE TABLE `weekly` "
			+ "(`weekly_id` varchar(64) NOT NULL,"
			+ "`variable_id` varchar(64) default NULL,"
			+ "`year` int(11) default NULL,"
			+ "`week_num` int(11) default NULL,"
			+ "`value` float(20,3) default NULL,"
			+ "PRIMARY KEY  (`weekly_id`),"
			+ "KEY `idx_variable` (`variable_id`),"
			+ "KEY `idx_date` (`year`,`week_num`)) "
			+ "ENGINE=MyISAM DEFAULT CHARSET=latin1;";

	private static final String CREATE_MONTHLY = "CREATE TABLE `monthly` " +
			"(`monthly_id` varchar(64) NOT NULL," +
			"`variable_id` varchar(64) default NULL," +
			"`year` int(11) default NULL," +
			"`month_num` int(11) default NULL," +
			"`value` float(20,3) default NULL," +
			"PRIMARY KEY  (`monthly_id`)," +
			"KEY `idx_variable` (`variable_id`)," +
			"KEY `idx_date` (`year`,`month_num`)) " +
			"ENGINE=MyISAM DEFAULT CHARSET=latin1;";

	public static final String CREATE_STATIONS = "CREATE TABLE `station` ("
			+ "`station_id` varchar(64) NOT NULL default '',"
			+ "`station_name` varchar(50) default NULL,"
			+ "`agency` varchar(12) default NULL,"
			+ "`state` varchar(15) default NULL,"
			+ "`county` varchar(50) default NULL,"
			+ "`latitude` float(10,6) default NULL,"
			+ "`longitude` float(10,6) default NULL,"
			+ "`coord_accuracy` varchar(12) default NULL,"
			+ "`lat_long_datum` varchar(12) default NULL,"
			+ "`elevation` float(10,6) default NULL,"
			+ "`elev_accuracy` varchar(12) default NULL,"
			+ "`elev_datum` varchar(12) default NULL,"
			+ "`hydrologic_unit` varchar(12) default NULL,"
			+ "`drain_area` float(10,6) default NULL,"
			+ "`contrib_drain_area` float(10,6) default NULL,"
			+ "`start_date` date default NULL,"
			+ "`end_date` date default NULL," + "PRIMARY KEY (`station_id`)"
			+ ") ENGINE=MyISAM DEFAULT CHARSET=latin1";

	public static final String CREATE_VARIABLE = "CREATE TABLE `variable` ("
			+ "`variable_id` varchar(64) NOT NULL default '',"
			+ "`variable_name` varchar(20) default NULL,"
			+ "`station_id` varchar(64) NOT NULL default '', "
			+ "`start_date` date default NULL,"
			+ "`end_date` date default NULL,"
			+ "`missing_percent` float(10,6) default NULL,"
			+ "PRIMARY KEY  (`variable_id`),"
			+ "KEY `idx_station_id` (`station_id`)"
			+ ") ENGINE=MyISAM DEFAULT CHARSET=latin1";

	private static DataSource source = DataSourceInjector
			.injectDataSource(DataSourceTypes.STREAM_FLOW);

	public static void createSchemas() throws SQLException {
		Connection c = null;

		try {
			c = source.getConnection();

			/* requires that a streamflow database exist */
			c.createStatement().execute("DROP TABLE IF EXISTS station");
			c.createStatement().execute("DROP TABLE IF EXISTS variable");
			c.createStatement().execute("DROP TABLE IF EXISTS daily");
			c.createStatement().execute("DROP TABLE IF EXISTS weekly");
			c.createStatement().execute("DROP TABLE IF EXISTS monthly");

			c.createStatement().execute(CREATE_STATIONS);
			c.createStatement().execute(CREATE_VARIABLE);
			c.createStatement().execute(CREATE_DAILY);
			c.createStatement().execute(CREATE_WEEKLY);
			c.createStatement().execute(CREATE_MONTHLY);
			
		} catch (SQLException sqe) {
			throw sqe;
		} catch (Exception e) {
			LOG.error("unknown exception", e);
			throw new RuntimeException(e.getMessage());
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}
}
