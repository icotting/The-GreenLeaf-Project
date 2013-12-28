/* Created on Nov 25, 2008 */
package edu.unl.act.rma.firm.test.core;

import junit.framework.TestCase;
import edu.unl.act.rma.firm.core.BeanInjector;
import edu.unl.act.rma.firm.core.LogEntry;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.component.LogDatabase;

/**
 * 
 * @author Ian Cottingham
 *
 */
public class LogDataBaseTest extends TestCase {

	private LogDatabase db;
	
	public void setUp() throws Exception { 
		this.db = BeanInjector.getInstance().getSessionBean(LogDatabase.class);
	}
	
	public void testCreateLog() throws Exception { 
		int prev_count = db.getLogEntries().size();
		LogEntry entry = new LogEntry();
		entry.setAppender(Loggers.COMPONENT_LOG.name());
		entry.setMessage("Test case log entry");
		entry.setType("DEBUG");
		
		db.writeLog(entry);
		
		assertEquals("did not update the log db", prev_count+1, db.getLogEntries().size());
	}
	
	public void testQuery() throws Exception { 
		int prev_count = db.getLogEntries("DEBUG").size();
		int prev_other_count = db.getLogEntries("ERROR").size();
		LogEntry entry = new LogEntry();
		entry.setAppender(Loggers.COMPONENT_LOG.name());
		entry.setMessage("Test case log entry");
		entry.setType("DEBUG");
		
		db.writeLog(entry);
		
		assertEquals("did not update the log db", prev_count+1, db.getLogEntries("DEBUG").size());
		assertEquals("incorrect count", prev_other_count, db.getLogEntries("ERROR").size());		
	}
	
	public void testRemote() throws Exception { 
		int prev_count = db.getLogEntries("DEBUG").size();

		LogEntry entry = db.getLogEntries("DEBUG").get(0);
		db.deleteEntry(entry);
		
		assertEquals("did not update the log db", prev_count-1, db.getLogEntries("DEBUG").size());
	}
	
	public void testClearLog() throws Exception { 
		db.clearLog();
		assertEquals("log was not cleared", 0, BeanInjector.getInstance().getSessionBean(LogDatabase.class).getLogEntries().size());
	}
}
