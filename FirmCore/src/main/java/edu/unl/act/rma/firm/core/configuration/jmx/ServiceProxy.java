/* Created On: Jul 18, 2006 */
package edu.unl.act.rma.firm.core.configuration.jmx;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * @author Ian Cottingham
 *
 */
public class ServiceProxy implements InvocationHandler, Serializable {

	private static final long serialVersionUID = 1L;

	private ObjectName obj;
	
	public ServiceProxy(ObjectName obj) { 
		this.obj = obj;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		String name = method.getName();
		
		if ( name.startsWith("get") ) { 
			name = name.substring(3);
			return server.getAttribute(obj, name);
		} else if ( name.startsWith("is") ) {
			name = name.substring(2);
			return server.getAttribute(obj, name);
		} else if ( name.startsWith("set") ) { 
			name = name.substring(3);
			Attribute attr = new Attribute(name, args[0]);
			server.setAttribute(obj, attr);
			return void.class;
		} else { 
			int len = (args == null ) ? 0 : args.length;
			String[] types = new String[len];
			for ( int i=0; i<len; i++ ) { 
				types[i] = args[i].getClass().getName();
			}
			return server.invoke(obj, name, args, types);
		}
	}
}
