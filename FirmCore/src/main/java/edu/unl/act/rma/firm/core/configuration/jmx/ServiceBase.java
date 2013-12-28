/* Created On: Jul 13, 2006 */
package edu.unl.act.rma.firm.core.configuration.jmx;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ReflectionException;

/**
 * @author Ian Cottingham
 *
 * This class is used as the base object for a constructed MBean from some interface to expose
 * managed operations and some implementing class.  This class provides a convenient way to 
 * create MBeans from standard POJOs through annotated resources.  
 * 
 */
public class ServiceBase<T> extends NotificationBroadcasterSupport implements DynamicMBean, Serializable {
	
	private static final Logger LOG = Logger.getLogger("edu.unl.larc.jmx.server.log");

	private static final long serialVersionUID = 1L;
	
	protected static final List<String> RESTRICTED_METHODS;

	static { 
		Method[] restricted_methods = Object.class.getDeclaredMethods();
		RESTRICTED_METHODS = new ArrayList<String>();
		for ( Method m : restricted_methods ) { 
			RESTRICTED_METHODS.add(m.getName());
		}
	}

	protected T backObject;
	private HashMap<String, String> types;
	protected HashMap<String, Method> get_read;
	protected HashMap<String, Method> is_read;
	protected HashMap<String, Method> write;
	protected ArrayList<String> notificationSetters;
	
	private int sequenceNumber = 0;
	private HashMap<String, Method> op;	
	private MBeanInfo info;
	protected Class clazz;
	private Service annotation;
	private List<String> transientAttributes;
	
	@ProxyConstructor
	public ServiceBase(T backObject, Service annotation) {
		this.backObject = backObject;
		this.clazz = backObject.getClass();
		this.notificationSetters = new ArrayList<String>();
		this.types = new HashMap<String, String>();
		this.get_read = new HashMap<String, Method>();
		this.is_read = new HashMap<String, Method>();
		this.write = new HashMap<String, Method>();
		this.op = new HashMap<String, Method>();
		this.annotation = annotation;
		this.transientAttributes = new ArrayList<String>();
		
		Method[] methods = clazz.getMethods();
		
		for ( Method method : methods ) { 
			ServicePoint service_point = (ServicePoint)method.getAnnotation(ServicePoint.class);
			String name = method.getName();
				
			boolean notify = (service_point != null && service_point.sendChangeNotification());
			
			if ( RESTRICTED_METHODS.contains(name) || method.getAnnotation(Excluded.class) != null ) { 
				continue;
			}
		
			if ( name.startsWith("get") ) { 
				name = name.substring(3);
				if ( notify ) { notificationSetters.add(name); }
				get_read.put(name, method);
				if ( types.get(name) == null ) { 
					types.put(name, method.getReturnType().getName());
				}
			} else if ( name.startsWith("is") ) {
				name = name.substring(2);
				if ( notify ) { notificationSetters.add(name); }
				is_read.put(name, method);
				if ( types.get(name) == null ) { 
					types.put(name, method.getReturnType().getName());
				}
			} else if ( name.startsWith("set") ) {
				name = name.substring(3);
				if ( notify ) { notificationSetters.add(name); }
				write.put(name, method);
				if ( types.get(name) == null ) { 
					/* a setter should have a single argument only */
					types.put(name, method.getParameterTypes()[0].getName());
				}
				if ( service_point != null && service_point.isTransient() ) { 
					transientAttributes.add(name);
				}
			} else { 
				op.put(name, method);
			}
		}
		
		
		if ( this.annotation.persistent() ) {
			try {
				PersistenceFactory.importObject(this);
			} catch ( Exception e ) { 
				LOG.log(Level.SEVERE, "error importing object", e);
			}
		}
		
		this.info = constructInfo();
	}
	
	public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
		Method point = get_read.get(attribute);
		if ( point == null ) {
			point = is_read.get(attribute);
			if ( point == null ) {
				throw new AttributeNotFoundException("the attribute "+attribute+" was not found as a getter for this MBean");
			}
		}
		
		Object obj;
		try {
			obj = point.invoke(backObject, new Object[0]);
		} catch ( IllegalAccessException iae ) { 
			throw new ReflectionException(iae, "the getter operation is "+point.getName()+" is not accessible");
		} catch ( InvocationTargetException ite ) {
			throw new ReflectionException(ite, "the getter operation is "+point.getName()+" could not be invoked on target of type"+backObject.getClass().getName());
		}
		
		return obj;
	}

	public AttributeList getAttributes(String[] attributes) {
		AttributeList list = new AttributeList();
		
		for ( String str : attributes ) { 
			try {
				list.add(new Attribute(str, getAttribute(str)));
			} catch ( Exception e ) { 
				LOG.log(Level.WARNING, "could not get value for point "+str, e);
			}
		}
		
		return list;
	}

	public MBeanInfo getMBeanInfo() {
		return info;
	}
	
	public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
		Method m = op.get(actionName);
		if ( m == null ) { 
			throw new ReflectionException(new NullPointerException(), actionName+" is not a valid operation on the MBean type "+backObject.getClass().getName());
		}
		
		try {
			return m.invoke(backObject, params);
		} catch ( Exception e ) { 
			throw new MBeanException(e, "could not invoke the operation "+actionName+" on MBean type "+backObject.getClass().getName());
		}
	}

	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		setAttribute(attribute, true);
	}

	private void setAttribute(Attribute attribute, boolean persist) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		Method point = write.get(attribute.getName());
		if ( point == null ) { 
			throw new AttributeNotFoundException("the attribute "+attribute+" was not found as a setter for this MBean");
		}
		
		/* a change notification should be sent */
		if ( notificationSetters.contains(attribute.getName()) ) { 
			// get the old value
			Object old = this.getAttribute(attribute.getName());
	        Notification n = 
	            new AttributeChangeNotification(this, 
						    sequenceNumber++, 
						    System.currentTimeMillis(), 
						    "The "+attribute.getName()+" attribute was changed", 
						    attribute.getName(), 
						    attribute.getValue().getClass().getName(), 
						    old, 
						    attribute.getValue()); 
	 
	        sendNotification(n); 
		}
		
		try {
			point.invoke( backObject, new Object[] {attribute.getValue()} );
		} catch ( IllegalAccessException iae ) { 
			throw new ReflectionException(iae, "the setter operation is "+point.getName()+" is not accessible");
		} catch ( InvocationTargetException ite ) {
			throw new ReflectionException(ite, "the setter operation is "+point.getName()+" could not be invoked on target of type"+backObject.getClass().getName());
		}
		
		if ( this.annotation.persistent() && persist && !transientAttributes.contains(attribute)) {
			try {
				PersistenceFactory.exportObject(this);
			} catch ( Exception e ) { 
				LOG.log(Level.SEVERE, "error exporting object", e);
			}
		}
	}
	
	public AttributeList setAttributes(AttributeList attributes) {
		AttributeList list = new AttributeList();
		for ( Object obj : attributes ) { 
			if ( !(obj instanceof Attribute ) ) { 
				throw new RuntimeException("an attribute in the list was not of type Attribute");
			}
			Attribute attr = (Attribute)obj;
			try {
				setAttribute(attr, false);
				list.add(attr);
			} catch ( Exception e ) { 
				LOG.log(Level.WARNING, "could not get value for point "+attr.getName(), e);
			}
		}
		
		if ( this.annotation.persistent() ) {
			try {
				PersistenceFactory.exportObject(this);
			} catch ( Exception e ) { 
				LOG.log(Level.SEVERE, "error exporting object", e);
			}
		}
		
		return list;		
	}
		
	private MBeanInfo constructInfo() { 
		
		ArrayList<MBeanAttributeInfo> infos = new ArrayList<MBeanAttributeInfo>();
		for ( String key : types.keySet() ) { 
			boolean is_isreadable = ( is_read.get(key) != null );
			boolean is_readable = ( get_read.get(key) != null );
			boolean is_writeable = ( write.get(key) != null );
			
			infos.add(new MBeanAttributeInfo(key, types.get(key), "a service point attribute", (is_isreadable || is_readable), is_writeable, is_isreadable)); 
		}
		
		ArrayList<MBeanConstructorInfo> constructor_infos = new ArrayList<MBeanConstructorInfo>();
		for ( Constructor con : clazz.getConstructors() ) { 
			if ( con.getAnnotation(ProxyConstructor.class) == null ) { 
				constructor_infos.add(new MBeanConstructorInfo("service constructor", con));
			}
		}
		
		ArrayList<MBeanOperationInfo> operation_infos = new ArrayList<MBeanOperationInfo>();
		for ( String key : op.keySet() ) { 
			Method method = op.get(key);
			ServicePoint service_point = (ServicePoint)method.getAnnotation(ServicePoint.class);
			String description = ( service_point == null ) ? "" : service_point.description();
			
			Class[] args = method.getParameterTypes();
			Annotation[][] param_annos = method.getParameterAnnotations();
			ArrayList<MBeanParameterInfo> param_info = new ArrayList<MBeanParameterInfo>();
			
			for ( int i=0; i<args.length; i++ ) { 
				ServicePointParameter param_anno = null;
				for ( Annotation test : param_annos[i] ) { 
					if ( test instanceof ServicePointParameter ) {
						param_anno = (ServicePointParameter)test;
						break;
					}
				}
				
				String name = ( param_anno != null ) ? param_anno.name() : "arg"+i;
				String desc = ( param_anno != null ) ? param_anno.description() : "a parameter";
				
				param_info.add(new MBeanParameterInfo(name, args[i].getName(), desc));
			}

			operation_infos.add(new MBeanOperationInfo(key, description, param_info.toArray(new MBeanParameterInfo[param_info.size()]), 
					method.getReturnType().getName(), MBeanOperationInfo.UNKNOWN));
		}
				
		return new MBeanInfo(clazz.getName(), "a service proxied MBean", infos.toArray(new MBeanAttributeInfo[infos.size()]), 
				constructor_infos.toArray(new MBeanConstructorInfo[constructor_infos.size()]), 
				operation_infos.toArray(new MBeanOperationInfo[operation_infos.size()]), 
				new MBeanNotificationInfo[0]);
	}
	
    @Override 
    public MBeanNotificationInfo[] getNotificationInfo() { 
        String[] types = new String[] { 
            AttributeChangeNotification.ATTRIBUTE_CHANGE 
        }; 
        
        String name = AttributeChangeNotification.class.getName(); 
        String description = "An attribute of this MBean has changed"; 
        
        MBeanNotificationInfo info = new MBeanNotificationInfo(types, name, description); 
        return new MBeanNotificationInfo[] {info}; 
    } 
 
	
	@Retention(value=RetentionPolicy.RUNTIME)
	@Target(value=ElementType.CONSTRUCTOR)
	@interface ProxyConstructor { };
	
	
}
