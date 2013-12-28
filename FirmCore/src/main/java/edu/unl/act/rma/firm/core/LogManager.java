/* Created On: Sep 16, 2005 */
package edu.unl.act.rma.firm.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.logging.Level;

import edu.unl.act.rma.firm.core.component.LogDatabase;

/**
 * @author Ian Cottingham
 * 
 */
public class LogManager implements InvocationHandler {

	public static final java.util.logging.Logger BASE_LOG = java.util.logging.Logger
			.getLogger("javax.enterprise");

	private final Loggers appenderObject;
	private final Class clazz;

	protected LogManager(Loggers appenderObject, Class clazz) {
		this.appenderObject = appenderObject;
		this.clazz = clazz;
	}

	public static Logger getLogger(Loggers appenderObject, Class clazz) {
		return (Logger) Proxy.newProxyInstance(clazz.getClassLoader(),
				new Class[] { Logger.class }, new LogManager(appenderObject,
						clazz));
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

		String method_name = method.getName();
		if (method_name.equals("error") || method_name.equals("fatal")
				|| method_name.equals("warn") || method_name.equals("info")
				|| method_name.equals("debug")) {
			
			LogEntry entry = new LogEntry();
			entry.setAppender(appenderObject.name());
			entry.setTimeStamp(new Date(System.currentTimeMillis()));
			entry.setType(method_name.toUpperCase());
			entry.setClassName(clazz.getName());
			entry.setMessage((String)args[0]);
			
			Throwable t = null;
			if ( args.length == 2 ) { 
				t = (Throwable)args[1];
				StringWriter str = new StringWriter();
				t.printStackTrace(new PrintWriter(str));
				str.flush(); str.close();
				entry.setStackTrace(str.toString());
			}
			
			if (System.getProperty("edu.unl.firm.consoleLogger") != null) {
				System.out.println(entry.toString());
			} else {
				try {
					BeanInjector.getInstance().getSessionBean(LogDatabase.class).writeLog(entry);
				} catch ( Exception e ) { 
					BASE_LOG.log(Level.WARNING, "The following could not be written to the log db:");
					if ( args.length > 2 ) {
						BASE_LOG.log(Level.SEVERE, (String)args[0], (Throwable)args[1]);
					} else { 
						BASE_LOG.log(Level.SEVERE, (String)args[0]);
					}
				}
			}
			return null;
		} else {
			return method.invoke(this, args);
		}
	}
}
