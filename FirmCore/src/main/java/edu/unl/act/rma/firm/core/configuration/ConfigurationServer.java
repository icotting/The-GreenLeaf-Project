/* Created On: Aug 2, 2006 */
package edu.unl.act.rma.firm.core.configuration;

import java.io.IOException;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.configuration.jmx.ServerRuntime;

/**
 * @author Ian Cottingham
 *
 */
public class ConfigurationServer {
	
	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, ConfigurationServer.class);
	private static final MBeanServer serverConnection;	
	
	static {
		serverConnection = ServerRuntime.getInstance().getMBeanServer();
	}
	
	private ObjectName objectName;
	
	protected ConfigurationServer(String objectName, Class clazz) {		
		super();
		
		if ( !ServerRuntime.getInstance().isDeployed(clazz) ) { 
			ServerRuntime.getInstance().deploy(clazz);
		}
		
		try {
			this.objectName = new ObjectName(objectName);
		} catch ( MalformedObjectNameException mfe ) {
			LOG.error("invalid object name specified", mfe);
			throw new RuntimeException(objectName+" is not a valid ObjectName");
		}
	}
	
	protected ConfigurationServer(String objectName, Class clazz, NotificationListener listener) { 
		this(objectName, clazz);
		try {
			serverConnection.addNotificationListener(this.objectName, listener, null, null);
		} catch ( Exception e ) { 
			RuntimeException re = new RuntimeException();
			re.initCause(e);
			throw re;
		}
	}
	
	public ConfigurationServer(ObjectName name) { 
		this.objectName = name;
	}
	
	public MBeanInfo getInfo() throws InstanceNotFoundException, IntrospectionException, IOException, ReflectionException {
		return serverConnection.getMBeanInfo(objectName);
	}
	
	public Object get(String attributeName) throws ConfigurationException {
		try {
			return serverConnection.getAttribute(objectName, attributeName);
		} catch ( InstanceNotFoundException inf ) { 
			LOG.error("could not find instance for object name", inf);
			throw new ConfigurationException("could not find instance for object name "+objectName.getCanonicalName());
		} catch ( ReflectionException re ) { 
			LOG.error( "could not get the attribute", re);
			throw new ConfigurationException("the attribute "+attributeName+" could not be read");
		} catch ( AttributeNotFoundException nfe ) { 
			LOG.error( "no attribute", nfe);
			throw new ConfigurationException("no such attribute "+attributeName);
		} catch ( MBeanException mbe ) { 
			LOG.error( "unknown mbean exception", mbe);
			throw new ConfigurationException("unknown mbean exception: "+mbe.getMessage());
		} catch ( RuntimeException re ) { 
			LOG.error( "unknown exception", re);
			throw new ConfigurationException(re.getMessage());
		}
	}
	
	public void set(String attributeName, Object value) throws ConfigurationException {
		try {
			Attribute attr = new Attribute(attributeName, value);
			
			serverConnection.setAttribute(objectName, attr);
		} catch ( InstanceNotFoundException inf ) { 
			LOG.error( "could not find instance for object name", inf);
			throw new ConfigurationException("could not find instance for object name "+objectName.getCanonicalName());
		} catch ( ReflectionException re ) { 
			LOG.error( "could not get the attribute", re);
			throw new ConfigurationException("the attribute "+attributeName+" could not be read");
		} catch ( InvalidAttributeValueException iave ) { 
			LOG.error( "the provided attribute value is invalid", iave);
			throw new ConfigurationException("an invalid attribute value was provided for "+attributeName);
		} catch ( AttributeNotFoundException nfe ) { 
			LOG.error( "no attribute", nfe);
			throw new ConfigurationException("no such attribute "+attributeName);
		} catch ( MBeanException mbe ) { 
			LOG.error( "unknown mbean exception", mbe);
			throw new ConfigurationException("unknown mbean exception: "+mbe.getMessage());
		}
	}
	
	public Object invoke(String operationName, Object[] args) throws ConfigurationException { 
		try {			
			int len = args.length;
			String[] sigs = new String[len];
			for ( int i=0; i<len; i++ ) { 
				sigs[i] = args[i].getClass().getName();
			}
			return serverConnection.invoke(objectName, operationName, args, sigs);
		} catch ( InstanceNotFoundException inf ) { 
			LOG.error( "could not find instance for object name", inf);
			throw new ConfigurationException("could not find instance for object name "+objectName.getCanonicalName());
		} catch ( ReflectionException re ) { 
			LOG.error( "could not get the attribute", re);
			throw new ConfigurationException("the attribute "+operationName+" could not be read");
		} catch ( MBeanException mbe ) { 
			LOG.error( "unknown mbean exception", mbe);
			throw new ConfigurationException("unknown mbean exception: "+mbe.getMessage());
		}
	}
}
