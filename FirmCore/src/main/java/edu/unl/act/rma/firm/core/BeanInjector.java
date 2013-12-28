/* Created on: Jul 17, 2008 */
package edu.unl.act.rma.firm.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Ian Cottingham
 *
 */
public class BeanInjector {

	private static final BeanInjector INSTANCE = new BeanInjector();
	protected static final Logger LOG = Logger.getLogger("system.out");
	
	private final InitialContext initialContext;
	
	private BeanInjector() { 
		try {
			this.initialContext = new InitialContext();
		} catch ( NamingException ne ) { 
			LOG.log(Level.SEVERE, "an error occured loading the initial context", ne);
			throw new RuntimeException("could not create the bean injector");
		}
	}
	
	public static BeanInjector getInstance() { 
		return INSTANCE;
	}
	
	/**
	 * Returns an instance of a session bean of the given type.
	 * 
	 * @param sessionClass The type of session bean requested
	 * @return A session bean instance, if one exists
	 * @throws NamingException If there are no session beans of the given type bound
	 */
	public <beanType> beanType getSessionBean(Class<beanType> sessionClass) throws NamingException {
		String jndi_name = sessionClass.getName();	
		return (beanType) this.initialContext.lookup(jndi_name);
	}
	
	public <beanType> beanType getLocalSessionBean(Class<beanType> sessionClass, Class baseType) throws NamingException { 
		String jndi_name = baseType.getName();	
		return (beanType) this.initialContext.lookup(jndi_name);
	}
}
