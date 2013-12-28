/* Created On: Aug 31, 2005 */
package edu.unl.act.rma.console.web;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.management.BadAttributeValueExpException;

import edu.unl.act.rma.console.acis.BuildMonitor;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.configuration.ConfigurationException;
import edu.unl.act.rma.firm.core.configuration.jmx.JMXException;
import edu.unl.act.rma.firm.core.configuration.jmx.Service;
import edu.unl.act.rma.firm.core.configuration.jmx.ServicePoint;
import edu.unl.act.rma.firm.core.configuration.jmx.ServicePointParameter;

/**
 * @author Ian Cottingham
 *
 */
@Service(objectName="edu.unl.firm:type=NWSDataService", providerInterface=NWSDataServiceMBean.class)
public class NWSDataServiceImpl implements NWSDataServiceMBean, Serializable {

	private static final long serialVersionUID = 1L;
	
	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG, NWSDataServiceImpl.class);
		
	private Integer maxQueueSize;
	private String temporaryPath;
	private String brokerIORPath;
	private String acisUser;
	private String acisPassword;
	private NWSDataService dataService;
	private BuildMonitor monitor;

	public void run(@ServicePointParameter(name="stateExpression", description="Regular Expression for state") 
			final String stateExpression) throws RemoteException {

			dataService = new NWSDataService();
			LOG.info("Creating climate schema...");
			try {
				dataService.createClimateSchemas();
			} catch (JMXException e) {
				LOG.error("Error creating climate schema.", e);
				return;
			}
			
			dataService.runACISBuild(stateExpression);
			monitor = dataService.getMonitor();
	}
	
	
	@ServicePoint(defaultValue="/tmp/")
	public String getTemporaryPath() {
		return this.temporaryPath;
	}
	
	public void setTemporaryPath(String value) {
		this.temporaryPath = value;
	}
	
	@ServicePoint(defaultValue="4")
	public Integer getMaxQueueSize() throws ConfigurationException {
		return this.maxQueueSize;
	}

	public void setMaxQueueSize(Integer size) throws ConfigurationException {
		this.maxQueueSize = size;
	}

	@ServicePoint(defaultValue="autumn")
	public String getAcisPassword() {
		return acisPassword;
	}

	public void setAcisPassword(String acisPassword) {
		this.acisPassword = acisPassword;
	}

	@ServicePoint(defaultValue="csuser")
	public String getAcisUser() {
		return acisUser;
	}

	public void setAcisUser(String acisUser) {
		this.acisUser = acisUser;
	}

	@ServicePoint(defaultValue="ndmc3.unl.edu/broker.ior")
	public String getBrokerIORPath() {
		return brokerIORPath;
	}

	public void setBrokerIORPath(String brokerIORPath) {
		this.brokerIORPath = brokerIORPath;
	}
	
	public String getRunTime(){
		try {
			return (null != dataService ? dataService.getRunTime() : "");
		} catch (BadAttributeValueExpException e) {
			return "Error in getting RunTime";
		}
	}
	
	public String getDataWriterStatus(){
		try {
			return (null != dataService ? dataService.getDataWriterStatus() : "");
		} catch (JMXException e) {
			return "Error in getting DataWriterStatus";
		} catch (BadAttributeValueExpException e) {
			return "Error in getting DataWriterStatus";
		}
	}
	
	public String getQueryWriterStatus(){
		try {
			return (null != dataService ? dataService.getQueryWriterStatus() : "");
		} catch (BadAttributeValueExpException e) {
			return "Error in getting QueryWriterStatus";
		}
	}


	@Override
	public String getDataWriterProgress() throws RemoteException {
		return (monitor == null) ? "" : monitor.getDataWriter().percentComplete();
	}


	@Override
	public String getDataWriterStatusString() throws RemoteException {
		return (monitor == null) ? "Inactive" : monitor.getDataWriter().getTaskStatus();
	}


	@Override
	public String getQueryWriterProgress() throws RemoteException {
		return (monitor == null) ? "" : monitor.getQueryWriter().percentComplete();
	}


	@Override
	public String getQueryWriterStatusString() throws RemoteException {
		return (monitor == null) ? "Inactive" : monitor.getQueryWriter().getTaskStatus();
	}


	@Override
	public int getQueueSize() throws RemoteException {
		return (dataService == null) ? 0 : dataService.getQueueSize();
	}


	@Override
	public int getRemainingQueueCapacity() throws RemoteException {
		return (dataService == null) ? 0 : dataService.getQueueCapacity() - dataService.getQueueSize(); 
	}
	
	
}