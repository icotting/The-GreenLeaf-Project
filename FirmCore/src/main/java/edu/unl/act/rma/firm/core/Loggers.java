/* Created On: Sep 16, 2005 */
package edu.unl.act.rma.firm.core;

import java.io.ObjectStreamException;

/**
 * @author Ian Cottingham
 *
 */
public enum Loggers {
	SYSTEM_LOG("edu.unl.firm.system"), 
	COMPONENT_LOG("edu.unl.firm.component"), 
	SERVICE_LOG("edu.unl.firm.service"), 
	APPLICATION_LOG("edu.unl.firm.application");
	
	private String appender;
	
	private Loggers(String str) {
		appender = str;
	}
	
	public String getAppender() { 
		return appender;
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
}
