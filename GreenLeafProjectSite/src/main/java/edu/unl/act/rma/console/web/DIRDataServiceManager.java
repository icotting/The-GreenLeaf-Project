package edu.unl.act.rma.console.web;

import edu.unl.act.rma.firm.core.configuration.ConfigurationException;
import edu.unl.act.rma.firm.core.configuration.ConfigurationServer;

public class DIRDataServiceManager extends ConfigurationServer {
	private static DIRDataServiceManager instance;
	
	public static DIRDataServiceManager getInstance() throws InstantiationException { 
		if ( instance == null ) {
			try {
				instance = new DIRDataServiceManager();
			} catch ( RuntimeException re ) { 
				throw new InstantiationException("could not instantiate the manager");
			}
		}
		
		return instance;
	}
	
	protected DIRDataServiceManager() { 
		super("edu.unl.firm:type=DIRDataService", DIRDataServiceImpl.class);
	}
	
	public double getPercentComplete() throws ConfigurationException {
		return  Double.parseDouble(get("PercentComplete").toString());
	}
	
	public String getStatus() throws ConfigurationException {
		return (String)get("Status");
	}
	
	public String getRunTime() throws ConfigurationException {
		return (String)get("RunTime");
	}
	
	public void run() throws ConfigurationException {
		Object[] args = {};
		this.invoke("run", args);
	}
	
	public String getDatabaseHost() throws ConfigurationException {
		return (String)get("DatabaseHost");
	}
	
	public void setDatabaseHost(String host) throws ConfigurationException {
		set("DatabaseHost", host);
	}
	
	public String getDatabasePort() throws ConfigurationException {
		return (String)get("DatabasePort");
	}
	
	public void setDatabasePort(String port) throws ConfigurationException {
		set("DatabasePort", port);
	}
	
	public String getDatabaseName() throws ConfigurationException {
		return (String)get("DatabaseName");
	}
	
	public void setDatabaseName(String name) throws ConfigurationException {
		set("DatabaseName", name);
	}
	
	public String getDatabaseUsername() throws ConfigurationException {
		return (String)get("DatabaseUsername");
	}
	
	public void setDatabaseUsername(String username) throws ConfigurationException {
		set("DatabaseUsername", username);
	}
	
	public String getDatabasePassword() throws ConfigurationException {
		return (String)get("DatabasePassword");
	}
	
	public void setDatabasePassword(String password) throws ConfigurationException {
		set("DatabasePassword", password);
	}
	
	public String getLastRun() throws ConfigurationException { 
		return (String)get("LastRun");
	}
}