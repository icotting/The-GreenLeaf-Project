package edu.unl.act.rma.console.web;

import java.io.Serializable;
import java.rmi.RemoteException;

import edu.unl.act.rma.console.usgs.USGSDataWriter;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.configuration.jmx.Service;
import edu.unl.act.rma.firm.core.configuration.jmx.ServicePointParameter;
import edu.unl.act.rma.firm.core.spatial.USState;

@Service(objectName = "edu.unl.firm:type=StreamFlowDataService", providerInterface = StreamFlowServiceMBean.class)
public class StreamFlowDataServiceImpl implements StreamFlowServiceMBean,
		Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG,
			StreamFlowDataServiceImpl.class);

	private double percentComplete = 0.0d;
	private StreamFlowDataService streamFlowService;

	@Override
	public double getPercentComplete() {
		return percentComplete;
	}

	@Override
	public void createTables() throws RemoteException {
		streamFlowService = new StreamFlowDataService();
		streamFlowService.createTables();
	}

	@Override
	public void buildStations(
			@ServicePointParameter(name = "stateCode", description = "Postal code for state (leave blank to load stations for all states") final String stateCode)
			throws RemoteException {
		new Thread() {
			public void run() {
				streamFlowService = new StreamFlowDataService();
				try {
					if (stateCode.equals("")) {
						streamFlowService.buildStations();
					} else {
						USState state = USState.fromPostalCode(stateCode);
						streamFlowService.buildStations(state);
					}
				} catch (RemoteException e) {
					LOG.error("could not build stations", e);
				}
			}
		}.start();
	}

	@Override
	public void loadData(
			@ServicePointParameter(name = "stateCode", description = "Postal code for state") final String stateCode)
			throws RemoteException {
		new Thread() {
			public void run() {
				USGSDataWriter writer = new USGSDataWriter();
				if (stateCode.equals("")) {
					for (USState state : USState.values()) {
						writer.loadData(state);
					}
				} else {
					writer.loadData(USState.fromPostalCode(stateCode));
				}
			}
		}.start();
	}
	
}
