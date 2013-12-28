/* Created On Jul 26, 2007 */
package edu.unl.act.rma.firm.climate.configuration;

import java.rmi.RemoteException;

import edu.unl.act.rma.firm.climate.component.ClimateDataQuery;
import edu.unl.act.rma.firm.climate.component.ClimateMetaDataQuery;
import edu.unl.act.rma.firm.climate.component.ClimateSpatialExtension;
import edu.unl.act.rma.firm.climate.component.OceanicDataQuery;
import edu.unl.act.rma.firm.core.ComponentInvocationHandler;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.streamflow.component.StreamFlowDataQuery;
import edu.unl.act.rma.firm.streamflow.component.StreamFlowMetaDataQuery;
import edu.unl.act.rma.firm.streamflow.component.StreamFlowSpatialExtension;

/**
 * @author Ian Cottingham
 *
 */
public class ClimateServiceAccessor {

	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, ClimateServiceAccessor.class);
	
	private static ClimateServiceAccessor instance;
	
	protected ClimateServiceAccessor() {
		super();
	}
	
	public static ClimateServiceAccessor getInstance() throws InstantiationException { 
		try { 
			return ( instance == null ) ? instance = new ClimateServiceAccessor() : instance;
		} catch ( RuntimeException re ) { 
			LOG.error("error getting the accessor instance", re);
			throw new InstantiationException("could not create an instance of this accessor");
		}
	}
	
	/**
	 * @see DataService#getClimateDataQuery()
	 */
	public ClimateDataQuery getClimateDataQuery() throws RemoteException {
		try {
			return ComponentInvocationHandler.generateProxy(ClimateDataQuery.class);
		} catch ( Exception ne ) { 
			LOG.error("could not get the ClimateDataQuery", ne);
			RuntimeException re = new RuntimeException();
			re.initCause(ne);
			throw re;
		}
	}

	/**
	 * @see DataService#getClimateMetaDataQuery()
	 */
	public ClimateMetaDataQuery getClimateMetaDataQuery() throws RemoteException {
		try {
			return ComponentInvocationHandler.generateProxy(ClimateMetaDataQuery.class);
		} catch ( Exception ne ) { 
			LOG.error("could not get the ClimateMetaDataQuery", ne);
			RuntimeException re = new RuntimeException();
			re.initCause(ne);
			throw re;
		}
	}
		
	/**
	 * @see DataService#OceanicDataQuery()
	 */
	public OceanicDataQuery getOceanicDataQuery() throws RemoteException {
		try {
			return ComponentInvocationHandler.generateProxy(OceanicDataQuery.class);
		} catch ( Exception ne ) { 
			LOG.error("could not get the OceanicDataQuery", ne);
			RuntimeException re = new RuntimeException();
			re.initCause(ne);
			throw re;
		}
	}
	
	public StreamFlowDataQuery getStreamFlowDataQuery() throws RemoteException {
		try {
			return ComponentInvocationHandler.generateProxy(StreamFlowDataQuery.class);
		} catch ( Exception ne ) { 
			LOG.error("could not get the StreamFlowDataQuery", ne);
			RuntimeException re = new RuntimeException();
			re.initCause(ne);
			throw re;
		}
	}
	
	public StreamFlowMetaDataQuery getStreamFlowMetaDataQuery() throws RemoteException {
		try {
			return ComponentInvocationHandler.generateProxy(StreamFlowMetaDataQuery.class);
		} catch ( Exception ne ) { 
			LOG.error("could not get the StreamFlowMetaDataQuery", ne);
			RuntimeException re = new RuntimeException();
			re.initCause(ne);
			throw re;
		}
	}
	
	public ClimateSpatialExtension getSpatialExtension() throws RemoteException {
		try {
			return ComponentInvocationHandler.generateProxy(ClimateSpatialExtension.class);
		} catch ( Exception ne ) { 
			LOG.error("could not get the ClimateSpatialExtension", ne);
			RuntimeException re = new RuntimeException();
			re.initCause(ne);
			throw re;
		}
	}
	
	public StreamFlowSpatialExtension getStreamFlowSpatialExtension() throws RemoteException {
		try {
			return ComponentInvocationHandler.generateProxy(StreamFlowSpatialExtension.class);
		} catch ( Exception ne ) { 
			LOG.error("could not get the StreamFlowSpatialExtension", ne);
			RuntimeException re = new RuntimeException();
			re.initCause(ne);
			throw re;
		}
	}
}
