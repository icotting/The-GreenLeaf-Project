/* Created On: Aug 31, 2005 */
package edu.unl.act.rma.console.web;

import java.rmi.RemoteException;

import edu.unl.act.rma.firm.core.configuration.ConfigurationException;

/**
 * @author Ian Cottingham
 *
 */
public interface NWSDataServiceMBean {
	public void run(String stateExpression) throws RemoteException;

	public String getRunTime() throws RemoteException;
	public String getDataWriterStatus() throws RemoteException;
	public String getQueryWriterStatus() throws RemoteException;

	public String getQueryWriterStatusString() throws RemoteException;
	public String getDataWriterStatusString() throws RemoteException;
	public String getQueryWriterProgress() throws RemoteException;
	public String getDataWriterProgress() throws RemoteException;
	public int getQueueSize() throws RemoteException;
	public int getRemainingQueueCapacity() throws RemoteException;
	
	public void setTemporaryPath(String value) throws ConfigurationException;
	public String getTemporaryPath() throws ConfigurationException;
	public String getBrokerIORPath() throws RemoteException;
	public void setBrokerIORPath(String brokerIORPath) throws ConfigurationException;
	public String getAcisUser() throws RemoteException;
	public void setAcisUser(String acisUser) throws ConfigurationException;
	public String getAcisPassword() throws RemoteException;
	public void setAcisPassword(String acisPassword) throws ConfigurationException;
	public Integer getMaxQueueSize() throws ConfigurationException;
	public void setMaxQueueSize(Integer size) throws ConfigurationException;
}