/* Created on: Feb 09, 2010 */
package edu.unl.act.rma.console.beans;

import java.rmi.RemoteException;
import java.text.NumberFormat;

import javax.inject.Named;

import edu.unl.act.rma.console.web.DIRDataServiceManager;
import edu.unl.act.rma.console.web.NWSDataServiceManager;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.configuration.ConfigurationException;


/**
 * @author Ian Cottingham
 *
 */
@Named("dataBean")
public class DataStatusBean {
	
	private static final NumberFormat N_FORMAT = NumberFormat.getPercentInstance();
	
	private Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, DataStatusBean.class);
	private DIRDataServiceManager dirService;
	private NWSDataServiceManager nwsService;
	
	public DataStatusBean() { 
		try { 
			dirService = DIRDataServiceManager.getInstance();
			nwsService = NWSDataServiceManager.getInstance();	
		} catch ( Exception e ) { 
			LOG.error("could not connect to the configuration servers", e);
			RuntimeException re = new RuntimeException("could not load the vm info bean");
			re.initCause(e);
			throw re;
		}
	}
	
	public String getDirBuilderStatus() { 
		try {
			return dirService.getStatus();
		} catch ( ConfigurationException ce ) { 
			LOG.error("could not get the configuration value", ce);
			return "Error getting value";
		}
	}
	
	public String getDirBuilderProgress() { 
		try {
			double val = dirService.getPercentComplete();
			return N_FORMAT.format(val);
		} catch ( ConfigurationException ce ) { 
			LOG.error("could not get the configuration value", ce);
			return "Error getting value";
		}	
	}
	
	public String getDirBuilderRuntime() { 
		try {
			return dirService.getRunTime();
		} catch ( ConfigurationException ce ) { 
			LOG.error("could not get the configuration value", ce);
			return "Error getting value";
		}
	}
	
	public String getDirBuilderLastRun() { 
		try {
			return dirService.getLastRun();
		} catch ( ConfigurationException ce ) { 
			LOG.error("could not get the configuration value", ce);
			return "Error getting value";
		}
	}
	
	public String getAcisDataWriterStatus() {
		try {
			return nwsService.getDataWriterStatusString();
		} catch ( RemoteException ce ) { 
			LOG.error("could not get the configuration value", ce);
			return "Error getting value";
		}
	}
	
	public String getAcisDataWriterProgress() { 
		try {
			return nwsService.getDataWriterProgress();
		} catch ( RemoteException ce ) { 
			LOG.error("could not get the configuration value", ce);
			return "Error getting value";
		}
	}
	
	public String getAcisQueryWriterStatus() { 
		try {
			return nwsService.getQueryWriterStatusString();
		} catch ( RemoteException ce ) { 
			LOG.error("could not get the configuration value", ce);
			return "Error getting value";
		}
	}
	
	public String getAcisQueryWriterProgress() { 
		try {
			return nwsService.getQueryWriterProgress();
		} catch ( RemoteException ce ) { 
			LOG.error("could not get the configuration value", ce);
			return "Error getting value";
		}
	}
	
	public String getAcisBuilderRuntime() { 
		try {
			return nwsService.getRunTime();
		} catch ( RemoteException ce ) { 
			LOG.error("could not get the configuration value", ce);
			return "Error getting value";
		}
	}
}
