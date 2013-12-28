package edu.unl.act.rma.console.web;

import java.rmi.RemoteException;

import edu.unl.act.rma.firm.core.configuration.ConfigurationException;

public interface DIRDataServiceMBean {
	public void buildDataset() throws RemoteException;
	
	public double getPercentComplete();
	
	public String getStatus();
	
	public String getRunTime();
	
	public String getDatabaseHost() throws ConfigurationException;
	public void setDatabaseHost(String host) throws ConfigurationException;
	
	public String getDatabasePort() throws ConfigurationException;
	public void setDatabasePort(String port) throws ConfigurationException;
	
	public String getDatabaseName() throws ConfigurationException;
	public void setDatabaseName(String name) throws ConfigurationException;
	
	public String getDatabaseUsername() throws ConfigurationException;
	public void setDatabaseUsername(String username) throws ConfigurationException;
	
	public String getDatabasePassword() throws ConfigurationException;
	public void setDatabasePassword(String password) throws ConfigurationException;
	
	public String getLastRun() throws ConfigurationException;
}