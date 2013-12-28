/* Created On: Jun 16, 2006 */
package edu.unl.act.rma.firm.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import edu.unl.act.rma.firm.core.configuration.CoreServiceManager;

/**
 * @author Ian Cottingham 
 *
 */
public class DataSourceInjector implements InvocationHandler {

	private final Logger LOG = LogManager.getLogger(Loggers.SYSTEM_LOG, DataSourceInjector.class);
	private static InitialContext INITIAL_CONTEXT;
	
	static {
		try { 
			INITIAL_CONTEXT = new InitialContext();
		} catch ( NamingException ne ) { 
			ne.printStackTrace();
		}
	}
	
	private DataSourceTypes type;
	private String currentName;
	private DataSource source;
	
	public static DataSource injectDataSource(DataSourceTypes type) { 
		return (DataSource)Proxy.newProxyInstance(DataSource.class.getClassLoader(), new Class[] { DataSource.class }, new DataSourceInjector(type));
	}
	
	protected DataSourceInjector(DataSourceTypes type) { 
		this.type = DataSourceTypes.valueOf(type.name());
	}
	
	public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {

		CoreServiceManager manager = CoreServiceManager.getInstance();
		String tmp = "";
		currentName = (currentName == null) ? "" : currentName;
		
		switch ( type ) { 
		case CLIMATIC:
			if ( currentName != (tmp = manager.getClimaticDSJNDIName()) ) {
				currentName = tmp;
				source = null;
			}
			break;
		case CLIMATIC_BUILD:
			if( currentName != (tmp = manager.getClimaticBuildDSJNDIName()) ) {
				currentName = tmp;
				source = null;
			}
			break;
		case SOIL:
			if ( currentName != (tmp = manager.getSoilsDSJNDIName()) ) {
				currentName = tmp;
				source = null;
			}
			break;
		case OCEANIC:
			if ( currentName != (tmp = manager.getOceanicDSJNDIName()) ) {
				currentName = tmp;
				source = null;
			}
			break;
		case STREAM_FLOW:
			if ( currentName != (tmp = manager.getStreamFlowDSJNDIName()) ) {
				currentName = tmp;
				source = null;
			}
			break;
		case SYSTEM:
			if ( currentName != (tmp = manager.getSystemDSJNDIName()) ) {
				currentName = tmp;
				source = null;
			}
			break;
		default:
			LOG.warn(type.name()+" is an unknown log type");
		}
		
		if ( source == null ) { 
			source = getDataSource(tmp);
		}

		try {
			return arg1.invoke(source, arg2);
		} catch ( Exception e ) { 
			e.printStackTrace();
			LOG.error("injection error", e);
			throw new RuntimeException("could not inject the data source");
		}
	}
	
	/**
	 * Returns the DataSource registered to the given JNDI name, or throws a <code>NamingException</code> 
	 * if a DataSource cannot be found.
	 * 
	 * @param jndiName The JNDI name of the DataSource requested
	 * @return A DataSource
	 * @throws NamingException If the DataSource could not be found
	 */
	private DataSource getDataSource(String jndiName) throws NamingException {
		DataSource source = (DataSource)INITIAL_CONTEXT.lookup(jndiName);
		if ( source == null ) {
			throw new NamingException("could not find named datasource "+jndiName);
		}
		
		return source;
	}

}
