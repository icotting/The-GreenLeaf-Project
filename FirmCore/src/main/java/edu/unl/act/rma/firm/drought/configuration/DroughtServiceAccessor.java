/* Created On Jul 26, 2007 */
package edu.unl.act.rma.firm.drought.configuration;

import java.rmi.RemoteException;

import edu.unl.act.rma.firm.core.ComponentInvocationHandler;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.drought.component.DroughtImpactQuery;
import edu.unl.act.rma.firm.drought.component.DroughtIndexQuery;
import edu.unl.act.rma.firm.drought.component.DroughtMonitorQuery;
import edu.unl.act.rma.firm.drought.component.SoilsDataQuery;

/**
 * @author Ian Cottingham
 *
 */
public class DroughtServiceAccessor {

	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, DroughtServiceAccessor.class);
	
	private static DroughtServiceAccessor instance;
	
	protected DroughtServiceAccessor() {
		super();
	}
	
	public static DroughtServiceAccessor getInstance() throws InstantiationException { 
		try { 
			return ( instance == null ) ? instance = new DroughtServiceAccessor() : instance;
		} catch ( RuntimeException re ) { 
			LOG.error("error getting the accessor instance", re);
			throw new InstantiationException("could not create an instance of this accessor");
		}
	}
	
	public DroughtImpactQuery getDroughtImpactQuery() throws RemoteException { 
		try {
			return ComponentInvocationHandler.generateProxy(DroughtImpactQuery.class);
		} catch ( Exception ne ) { 
			LOG.error("could not get the drought impact", ne);
			RuntimeException re = new RuntimeException();
			re.initCause(ne);
			throw re;
		}
	}
	
	public DroughtMonitorQuery getDroughtMonitorQuery() throws RemoteException { 
		try {
			return ComponentInvocationHandler.generateProxy(DroughtMonitorQuery.class);
		} catch ( Exception ne ) { 
			LOG.error("could not get the drought monitor query", ne);
			RuntimeException re = new RuntimeException();
			re.initCause(ne);
			throw re;
		}
	}
	
	/**
	 * @see DroughtIndexService#getDroughtIndexQuery()
	 */
	public DroughtIndexQuery getDroughtIndexQuery() throws RemoteException {
		try {
			return ComponentInvocationHandler.generateProxy(DroughtIndexQuery.class);
		} catch ( Exception ne ) { 
			LOG.error("could not get the DroughtIndexQuery", ne);
			RuntimeException re = new RuntimeException();
			re.initCause(ne);
			throw re;
		}
	}
	
	public SoilsDataQuery getSoilsDataQuery() throws RemoteException {
		try {
			return ComponentInvocationHandler.generateProxy(SoilsDataQuery.class);
		} catch ( Exception ne ) { 
			LOG.error("could not get the DroughtIndexQuery", ne);
			RuntimeException re = new RuntimeException();
			re.initCause(ne);
			throw re;
		}
	}
}
