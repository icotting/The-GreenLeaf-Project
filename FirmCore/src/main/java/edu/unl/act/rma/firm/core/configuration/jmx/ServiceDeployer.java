/* Created On: Jul 14, 2006 */
package edu.unl.act.rma.firm.core.configuration.jmx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.jfree.util.Log;

/**
 * @author Ian Cottingham
 *
 */
public class ServiceDeployer {
	
	private static final String PERSISTENCE_PATH_PROPERTY = "edu.unl.firm.configuration.persist.dir";
	
	private static Map<Class, Object> deployedMBeans;
	private static List<ObjectName> objectNames;	
	
	protected static final String persistencePath;
	
	static { 
		deployedMBeans = new HashMap<Class, Object>();
		objectNames = new ArrayList<ObjectName>();

		String the_path = System.getProperty(PERSISTENCE_PATH_PROPERTY);

		persistencePath = ( the_path == null ) ? System.getProperty("java.io.tmpdir") : the_path;
	}
	
	//TODO: deal with logging
	
	/**
	 * Deploys a class, creating an instance of the managed bean.
	 * 
	 */
	protected static void deploy(Class clazz) { 
		if ( (deployedMBeans.get(clazz) != null) ) { 
			return;
		}
		
		try {
			if ( clazz.getAnnotation(Service.class) == null ) { 
				throw new RuntimeException("invalid class: "+clazz.getName());
			}
			
			Service service = (Service)clazz.getAnnotation(Service.class);
			ServiceProvider provider = (ServiceProvider)clazz.getAnnotation(ServiceProvider.class);
			
			String object_name = service.objectName();
			Class public_type = ( provider == null ) ? null : provider.providerInterface();
			
			createBean(service.providerInterface(), clazz, public_type, object_name, null, service);
		} catch ( Exception e ) { 
			e.printStackTrace();
		}
	}
	
	public static boolean isDeployed(Class clazz) { 
		return deployedMBeans.containsKey(clazz);
	}
	
	/**
	 * Deploy a class, using the argument object as the managed bean instance
	 * 
	 * @param obj
	 */
	protected static void deploy(Object obj) { 
		Class clazz = obj.getClass();
		if ( (deployedMBeans.get(clazz) != null) ) { 
			return;
		}
		
		try {
			if ( clazz.getAnnotation(Service.class) == null ) { 
				throw new RuntimeException("invalid class: "+clazz.getName());
			}
			
			Service service = (Service)clazz.getAnnotation(Service.class);
			ServiceProvider provider = (ServiceProvider)clazz.getAnnotation(ServiceProvider.class);
			
			String object_name = service.objectName();
			Class public_type = ( provider == null ) ? null : provider.providerInterface();
			
			createBean(service.providerInterface(), clazz, public_type, object_name, obj, service);
		} catch ( Exception e ) { 
			e.printStackTrace();
		}
	}
	
	private static <T, IMPL, PUB> void createBean(Class<T> interfaceType, Class<IMPL> implType, Class<PUB> publicType, final String objectName, IMPL base, Service annotation) throws IllegalAccessException, 
		InstantiationException, IllegalArgumentException {		
		
		if ( !(interfaceType.isAssignableFrom(implType)) ) { 
			throw new IllegalArgumentException(implType.getName()+" is not assignable from "+interfaceType.getName()); 
		}
		
		if ( base == null ) {
			base = (IMPL)implType.newInstance();
		}
		
		T pri = (T)base;
		ServiceBase service_object = new ServiceBase<T>(pri, annotation);
		
		deployedMBeans.put(implType, service_object);
	
		try {
			ObjectName object_name = new ObjectName(objectName);
			if ( ServerRuntime.server.isRegistered(object_name) ) {
				ServerRuntime.server.unregisterMBean(object_name);
			}
			
			ServerRuntime.server.registerMBean(service_object, object_name);
			objectNames.add(object_name);
		} catch ( InstanceAlreadyExistsException iae ) { 
			iae.printStackTrace();
		} catch ( MalformedObjectNameException mone ) { 
			mone.printStackTrace();
		} catch ( MBeanRegistrationException mre ) { 
			mre.printStackTrace();
		} catch ( NotCompliantMBeanException ncme ) { 
			ncme.printStackTrace();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	protected static void undeployAll() { 
		try {
			for ( ObjectName name : objectNames ) { 
				ServerRuntime.server.unregisterMBean(name);
			}
		} catch ( Exception e ) {
			//TODO: log this
			e.printStackTrace();
		}
	}
}
