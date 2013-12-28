/* Created on Nov 20, 2008 */
package edu.unl.act.rma.firm.core.component;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Remote;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.core.LogEntry;
import edu.unl.act.rma.firm.core.Loggers;

/**
 * 
 * @author Ian Cottingham
 *
 */
@Remote
public interface LogDatabase extends Serializable {

	public void writeLog(LogEntry entry);
	
	public List<LogEntry> getLogEntries();
	public List<LogEntry> getLogEntries(Loggers appender);
	public List<LogEntry> getLogEntries(Loggers appender, String type);
	public List<LogEntry> getLogEntries(Loggers appender, DateTime fromDate);
	public List<LogEntry> getLogEntries(Loggers appender, String type, DateTime fromDate);
	public List<LogEntry> getLogEntries(String type);
	public List<LogEntry> getLogEntries(String type, DateTime fromDate);
	public List<LogEntry> getLogEntries(DateTime fromDate);
	
	public void deleteEntry(LogEntry entry); 
	public void clearLog();
}
