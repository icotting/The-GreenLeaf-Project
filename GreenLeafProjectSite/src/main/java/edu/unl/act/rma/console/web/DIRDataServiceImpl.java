package edu.unl.act.rma.console.web;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import net.sourceforge.jtds.jdbcx.JtdsDataSource;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.configuration.ConfigurationException;
import edu.unl.act.rma.firm.core.configuration.jmx.Service;
import edu.unl.act.rma.firm.core.configuration.jmx.ServicePoint;

@Service(objectName="edu.unl.firm:type=DIRDataService", providerInterface=DIRDataServiceMBean.class)
public class DIRDataServiceImpl implements DIRDataServiceMBean, Serializable {
	private static final long serialVersionUID = 1L;
	
	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG, DIRDataServiceImpl.class);
	
	private String databaseHost;
	private String databasePort;
	private String databaseName;
	private String databaseUsername;
	private String databasePassword;

	private DIRDataService dirService;

	@Override
	public void buildDataset() throws RemoteException {
		run();
	}

	private void run() throws RemoteException {
		DataSourceTypes type = DataSourceTypes.SYSTEM;
		DataSource source = DataSourceInjector.injectDataSource(type);
		int start = Integer.MAX_VALUE;
		Connection connection = null;
		try {
			connection = source.getConnection();
			PreparedStatement statement = connection.prepareStatement("select max(legacy_id) from DroughtImpacts");
			ResultSet results = statement.executeQuery();
			if (results.next()) {
				start = results.getInt(1) + 1;
			}
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying data for type " + type.name(), sqe);
			throw new RemoteException("unable to query data from data base for type " + type.name());
		} finally {
			if (null != connection) {
				try {
					connection.close();
				} catch (SQLException e) {
					LOG.warn("SQL exception when closing the connection");
				}
			}
		}
		int end = 0;
		connection = null;
		try {
			DIRDataServiceManager manager = DIRDataServiceManager.getInstance();
			JtdsDataSource sqlSource = new JtdsDataSource();
			sqlSource.setServerName(manager.getDatabaseHost());
			sqlSource.setPortNumber(Integer.parseInt(manager.getDatabasePort()));
			sqlSource.setDatabaseName(manager.getDatabaseName());
			sqlSource.setUser(manager.getDatabaseUsername());
			sqlSource.setPassword(manager.getDatabasePassword());
			connection = sqlSource.getConnection();
			PreparedStatement statement = connection.prepareStatement("select max(ImpactID) from dbo.CseImpacts");
			ResultSet results = statement.executeQuery();
			if (results.next()) {
				end = results.getInt(1);
			}
		} catch ( InstantiationException ie ) { 
			LOG.error("could not get the dataset service manager", ie);
			return;
		} catch ( RemoteException re ) { 
			LOG.error("error reading db connection property", re);
			return;
		} catch ( SQLException sqe ) { 
			LOG.error("could not get the db connection", sqe);
			return;
		} catch ( RuntimeException re ) { 
			LOG.error("unknown error", re);
			return;
		} finally {
			if (null != connection) {
				try {
					connection.close();
				} catch (SQLException e) {
					LOG.warn("SQL exception when closing the connection");
				}
			}
		}
		if (start <= end) {
			dirService = new DIRDataService(start, end);
			dirService.start();
		}
	}
	
	public double getPercentComplete() {
		return (null != dirService ? dirService.getPercentComplete() : 0.0d);
	}
	
	@ServicePoint(defaultValue="torka.unl.edu")
	public String getDatabaseHost() throws ConfigurationException {
		return databaseHost;
	}
	
	public void setDatabaseHost(String host) throws ConfigurationException {
		databaseHost = host;
	}
	
	@ServicePoint(defaultValue="1451")
	public String getDatabasePort() throws ConfigurationException {
		return databasePort;
	}
	
	public void setDatabasePort(String port) throws ConfigurationException {
		databasePort = port;
	}
	
	@ServicePoint(defaultValue="CsePublicationDb")
	public String getDatabaseName() throws ConfigurationException {
		return databaseName;
	}
	
	public void setDatabaseName(String name) throws ConfigurationException {
		databaseName = name;
	}
	
	@ServicePoint(defaultValue="s-cseuser")
	public String getDatabaseUsername() throws ConfigurationException {
		return databaseUsername;
	}
	
	public void setDatabaseUsername(String username) throws ConfigurationException {
		databaseUsername = username;
	}
	
	@ServicePoint(defaultValue="f815+3W8D544fQ")
	public String getDatabasePassword() throws ConfigurationException {
		return databasePassword;
	}
	
	public void setDatabasePassword(String password) throws ConfigurationException {
		databasePassword = password;
	}
	
	public String getStatus() {
		return (null != dirService ? dirService.getStatus() : "Inactive");
	}
	
	public String getRunTime() {
		return (null != dirService ? dirService.getRunTime() : "");
	}
	
	private static final DateTimeFormatter D_FORMATTER = DateTimeFormat.forPattern("MMM dd, yyyy hh:mm a");
	
	public String getLastRun() { 
		if ( dirService == null || dirService.endTime() == null ) {
			return "Never";
		} else { 
			return D_FORMATTER.print(dirService.endTime());
		}
	}
}