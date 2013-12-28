/* Created On: Jun 13, 2006 */
package edu.unl.act.rma.firm.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;

import javax.naming.InitialContext;

/**
 * An invocation handler for ACL restricted component interfaces.  This class will generate 
 * proxies to the remote objects, facilitating ACL restriction checks on the client before
 * initiating the call.  The ACL functionlity extracts a user ID from the SystemRuntime object
 * 
 * @see edu.unl.firm.shared.SystemRuntime
 *  
 * @author Ian Cottingham 
 *
 */
public class ComponentInvocationHandler implements InvocationHandler {

	private static final Logger LOG = LogManager.getLogger(Loggers.SYSTEM_LOG, ComponentInvocationHandler.class);
	private static final InitialContext CONTEXT;
	
	static { 
		Properties env = new Properties();
		
		String provider_host = System.getProperty("edu.unl.firm.host");
		String provider_port = System.getProperty("edu.unl.firm.port");
		
		if ( provider_host == null ) { 
			provider_host = "localhost";
		}
		
		if ( provider_port == null ) { 
			provider_port = "3700";
		}
		
		env.setProperty("org.omg.CORBA.ORBInitialHost", provider_host);
		env.setProperty("org.omg.CORBA.ORBInitialPort", provider_port);
				
		try {
			CONTEXT = new InitialContext(env);
		} catch ( Exception e ) { 
			LOG.error("could not load the initial context", e);
			RuntimeException re = new RuntimeException();
			re.initCause(e);
			throw re;
		}
	}
	
	private Class clazz;
	private final Object backObject; 
	
	public static <T> T generateProxy(Class<T> type) { 
		return (T)Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, new ComponentInvocationHandler(type));
	}
	
	
	public ComponentInvocationHandler(Class clazz) { 
		this.clazz = clazz;
		String jndi_name = clazz.getName();	
		try {
			backObject = CONTEXT.lookup(jndi_name);
		} catch ( Exception e ) { 
			LOG.error("could not find the backing bean", e);
			RuntimeException re = new RuntimeException();
			re.initCause(e);
			throw re;
		}
	}

	/**
	 * This will apply token (or System user) restrictions only.  If this is deployed in a larger application
	 * framework making use of a FIRM profile, specific restriction logic must be applied elsewhere to obtain
	 * "current user" granularity in ACL calls.
	 */
	public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
		try {
			return method.invoke(backObject, args);
		} catch ( InvocationTargetException  ite ) {
			throw ite.getTargetException();
		} catch ( Throwable t) { 
			LOG.error("could not invoke the bean call", t);
			RuntimeException re = new RuntimeException();
			re.initCause(t);
			throw re;
		}
	}
	
}
