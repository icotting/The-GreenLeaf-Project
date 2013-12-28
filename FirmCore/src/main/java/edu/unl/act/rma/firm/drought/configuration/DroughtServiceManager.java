/* Created on: Apr 22, 2010 */
package edu.unl.act.rma.firm.drought.configuration;

import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.configuration.ConfigurationException;
import edu.unl.act.rma.firm.core.configuration.ConfigurationServer;
import edu.unl.act.rma.firm.core.configuration.CoreServiceManager;

/**
 * @author Ian Cottingham
 *
 */
public class DroughtServiceManager extends ConfigurationServer implements DroughtServiceMBean {

	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, CoreServiceManager.class);
	
	private static DroughtServiceManager instance;
	
	protected DroughtServiceManager() {
		super("edu.unl.firm:type=DroughtService", DroughtServiceImpl.class);
	}

	/**
	 * Returns the singleton system service manager.
	 * 
	 * @return The singleton manager
	 */
	public static DroughtServiceManager getInstance() throws InstantiationException {
		try { 
			return (instance == null) ? (instance = new DroughtServiceManager()) : instance;
		} catch ( RuntimeException re ) { 
			LOG.error("could not instantiate the manager", re);
			throw new InstantiationException("could not instantiate the manager");
		}
	}
	
	@Override
	public String getImporterStatus() throws ConfigurationException {
		return (String)get("ImporterStatus");
	}
	
	@Override
	public void importDroughtMonitorData(String startDate,
			String endDate) throws ConfigurationException {

		Object[] args = { startDate, endDate };
		this.invoke("importDroughtMonitorData", args);
		
	}

    @Override
    public void importAllDroughtMonitorData() throws ConfigurationException {
        Object[] args = new Object[0];
		this.invoke("importAllDroughtMonitorData", args);
    }
}
