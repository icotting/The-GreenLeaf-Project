package edu.unl.act.rma.console.web;

import java.io.Serializable;
import java.rmi.RemoteException;

import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.configuration.jmx.Service;

@Service(objectName="edu.unl.firm:type=OceanicDataService", providerInterface=OceanicDataServiceMBean.class)
public class OceanicDataServiceImpl implements OceanicDataServiceMBean, Serializable {
	private static final long serialVersionUID = 1L;
	
	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG, OceanicDataServiceImpl.class);

	private double percentComplete = 0.0d;

	public void run() throws RemoteException {
		Thread thread = new Thread() {
			public void run() {
				OceanicDataService oceanicService = new OceanicDataService();
				percentComplete = 0.0d;
				LOG.info("Creating oceanic tables...");
				oceanicService.create_tables();
				percentComplete = 0.5d;
				LOG.info("Loading oceanic data into database...");
				oceanicService.loaddata();
				percentComplete = 1.0d;
				LOG.info("Oceanic operations complete.");
			}
		};
		thread.start();
	}
	
	public double getPercentComplete() {
		return percentComplete;
	}
}