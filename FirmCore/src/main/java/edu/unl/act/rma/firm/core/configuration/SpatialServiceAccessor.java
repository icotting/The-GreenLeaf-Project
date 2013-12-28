/* Created On Jul 26, 2007 */
package edu.unl.act.rma.firm.core.configuration;

import java.rmi.RemoteException;

import edu.unl.act.rma.firm.core.ComponentInvocationHandler;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.component.SpatialQuery;

/**
 * @author Ian Cottingham
 *
 */
public class SpatialServiceAccessor {

	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, SpatialServiceAccessor.class);
	
	private static SpatialServiceAccessor instance;
	
	protected SpatialServiceAccessor() {

	}
	
	public static SpatialServiceAccessor getInstance() throws InstantiationException { 
		try { 
			return ( instance == null ) ? instance = new SpatialServiceAccessor() : instance;
		} catch ( RuntimeException re ) { 
			LOG.error("error getting the accessor instance", re);
			throw new InstantiationException("could not create an instance of this accessor");
		}
	}
	
	/**
	 * @see SpatialService#newMapQuery()
	 */
	public SpatialQuery getSpatialQuery() throws RemoteException {
		try {
			return ComponentInvocationHandler.generateProxy(SpatialQuery.class);
		} catch ( Exception ne ) { 
			LOG.error("could not get the SpatialQuery", ne);
			RuntimeException re = new RuntimeException();
			re.initCause(ne);
			throw re;
		}
	}
}
