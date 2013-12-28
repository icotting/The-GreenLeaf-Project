/* Created On: Sep 16, 2005 */
package edu.unl.act.rma.firm.test.core;

import junit.framework.TestCase;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;

/**
 * @author Ian Cottingham
 *
 */
public class LoggingTest extends TestCase {
		
	static { 
		System.setProperty("edu.unl.firm.consoleLogger", "true");
	}
	
	public void testSystemLogging() { 
		Logger log = LogManager.getLogger(Loggers.SYSTEM_LOG, this.getClass());
		
		log.debug("log entry for debug mode");
		log.debug("log entry with throwable", new Exception("debug entry"));
		
		log.info("information log test");
		log.info("information log test with throwable", new Exception("info entry"));
		
		log.warn("warn log test");
		log.warn("warn log test with throwable", new Exception("warn entry"));
		
		log.fatal("fatal log test");
		log.fatal("fatal log test with throwable", new Exception("fatal entry"));
		
		log.error("error log test");
		log.error("error log test with throwable", new Exception("error entry"));
	}
	
	public void testComponentLogging() { 
		Logger log = LogManager.getLogger(Loggers.COMPONENT_LOG, this.getClass());
		
		log.debug("log entry for debug mode");
		log.debug("log entry with throwable", new Exception("debug entry"));
		
		log.info("information log test");
		log.info("information log test with throwable", new Exception("info entry"));
		
		log.warn("warn log test");
		log.warn("warn log test with throwable", new Exception("warn entry"));
		
		log.fatal("fatal log test");
		log.fatal("fatal log test with throwable", new Exception("fatal entry"));
		
		log.error("error log test");
		log.error("error log test with throwable", new Exception("error entry"));
	}
		
	public void testServiceLogginf() { 
		Logger log = LogManager.getLogger(Loggers.SERVICE_LOG, this.getClass());

		log.debug("log entry for debug mode");
		log.debug("log entry with throwable", new Exception("debug entry"));
		
		log.info("information log test");
		log.info("information log test with throwable", new Exception("info entry"));
		
		log.warn("warn log test");
		log.warn("warn log test with throwable", new Exception("warn entry"));
		
		log.fatal("fatal log test");
		log.fatal("fatal log test with throwable", new Exception("fatal entry"));
		
		log.error("error log test");
		log.error("error log test with throwable", new Exception("error entry"));
	}
	
}
