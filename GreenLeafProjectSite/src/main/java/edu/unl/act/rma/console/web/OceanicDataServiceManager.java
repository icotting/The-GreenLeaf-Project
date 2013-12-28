package edu.unl.act.rma.console.web;

import edu.unl.act.rma.firm.core.configuration.ConfigurationException;
import edu.unl.act.rma.firm.core.configuration.ConfigurationServer;

public class OceanicDataServiceManager extends ConfigurationServer {
	public static OceanicDataServiceManager instance;
	
	public static OceanicDataServiceManager getInstance() {
		if (null == instance) {
			instance = new OceanicDataServiceManager();
		}
		return instance;
	}

	protected OceanicDataServiceManager() { 
		super("edu.unl.firm:type=OceanicDataService", OceanicDataServiceImpl.class);
	}
	
	public double getPercentComplete() throws ConfigurationException {
		return  Double.parseDouble(get("PercentComplete").toString());
	}
	
	public void run() throws ConfigurationException {
		Object[] args = {};
		this.invoke("run", args);
	}
}