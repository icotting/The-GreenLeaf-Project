/* Created On: Sep 23, 2005 */
package edu.unl.act.rma.console.web;

import java.rmi.RemoteException;

import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.configuration.ConfigurationException;
import edu.unl.act.rma.firm.core.configuration.ConfigurationServer;

/**
 * @author Ian Cottingham
 *
 */
public class NWSDataServiceManager extends ConfigurationServer implements NWSDataServiceMBean {

	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, NWSDataServiceManager.class);
	
	private static NWSDataServiceManager instance;
	
	protected NWSDataServiceManager() {
		super("edu.unl.firm:type=NWSDataService", NWSDataServiceImpl.class);
	}
	
	public static NWSDataServiceManager getInstance() throws InstantiationException {
		try { 
			return (instance == null) ? (instance = new NWSDataServiceManager()) : instance;
		} catch ( RuntimeException re ) { 
			LOG.error("could not instantiate the manager", re);
			throw new InstantiationException("could not instantiate the manager");
		}
	}
	
	public String getRunTime() throws ConfigurationException {
		return (String)get("RunTime");
	}
	
	public String getDataWriterStatus() throws ConfigurationException {
		return (String)get("DataWriterStatus");
	}
	
	public String getQueryWriterStatus() throws ConfigurationException {
		return (String)get("QueryWriterStatus");
	}
	
	public void run(String stateExpression) throws ConfigurationException {
		Object[] args = { stateExpression };
		this.invoke("run", args);
	}

	public String getTemporaryPath() throws ConfigurationException {
		return (String)get("TemporaryPath");
	}

	public void setTemporaryPath(String value) throws ConfigurationException { 
		set("TemporaryPath", value);
	}

	public Integer getMaxQueueSize() throws ConfigurationException {
		return (Integer)get("MaxQueueSize");
	}

	public void setMaxQueueSize(Integer size) throws ConfigurationException {
		set("MaxQueueSize", size);
	}
	
	public String getBrokerIORPath() throws ConfigurationException {
		return (String)get("BrokerIORPath");
	}
	
	public String getAcisUser() throws ConfigurationException {
		return (String)get("AcisUser");
	}
	
	public String getAcisPassword() throws ConfigurationException {
		return (String)get("AcisPassword");
	}
	
	public void setBrokerIORPath(String brokerIORPath) throws ConfigurationException {		
		set("BrokerIORPath", brokerIORPath);
	}
	
	public void setAcisUser(String acisUser) throws ConfigurationException {		
		set("AcisUser", acisUser);
	}
	
	public void setAcisPassword(String acisPassword) throws ConfigurationException {		
		set("AcisPassword", acisPassword);
	}

	@Override
	public String getDataWriterProgress() throws RemoteException {
		return (String)get("DataWriterProgress");
	}

	@Override
	public String getDataWriterStatusString() throws RemoteException {
		return (String)get("DataWriterStatusString");
	}

	@Override
	public String getQueryWriterProgress() throws RemoteException {
		return (String)get("QueryWriterProgress");
	}

	@Override
	public String getQueryWriterStatusString() throws RemoteException {
		return (String)get("QueryWriterStatusString");
	}

	@Override
	public int getQueueSize() throws RemoteException {
		return (Integer)get("QueueSize");
	}

	@Override
	public int getRemainingQueueCapacity() throws RemoteException {
		return (Integer)get("RemainingQueueCapacity");
	}
}
