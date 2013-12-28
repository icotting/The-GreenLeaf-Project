/* Created on Nov 20, 2008 */
package edu.unl.act.rma.firm.core.component;

import static edu.unl.act.rma.firm.core.LogManager.BASE_LOG;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.logging.Level;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.sql.DataSource;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogEntry;
import edu.unl.act.rma.firm.core.Loggers;

/**
 * 
 * @author Ian Cottingham
 *
 */
@Stateless
@Remote({LogDatabase.class})
public class LogDatabaseBean implements LogDatabase {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The peristence manager, used to manage the bound entity beans. */
	@PersistenceContext(unitName="FirmCorePU", type=PersistenceContextType.TRANSACTION)
	private transient EntityManager manager;
	
	private DataSource source = DataSourceInjector.injectDataSource(DataSourceTypes.SYSTEM);
	
	
	@Override
	public void writeLog(LogEntry entry) {
		try {
			manager.persist(entry);
			manager.flush();
		} catch ( Exception e ) { 
			RuntimeException re = new RuntimeException("Could not write the log entry");
			re.initCause(e);
			throw re;
		}
	}


	@Override
	public void clearLog() {
		Connection conn;
		PreparedStatement stmt;
		try { 
			conn = source.getConnection();
			stmt = conn.prepareStatement("delete from logentries");
		} catch ( Exception e ) { 
			BASE_LOG.log(Level.SEVERE, "could not get a db connection", e);
			RuntimeException re = new RuntimeException("could not clear the log");
			re.initCause(e);
			throw re;
		}
		
		try {
			stmt.execute();
		} catch ( Exception e ) { 
			BASE_LOG.log(Level.SEVERE, "an sql error occured", e);
			RuntimeException re = new RuntimeException("could not clear the log");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception e) { /* don't care */ }
		}
	}


	@Override
	public void deleteEntry(LogEntry entry) {
		try {
			manager.remove(manager.find(LogEntry.class, entry.getId()));
			manager.flush();
		} catch ( Exception e ) { 
			BASE_LOG.log(Level.SEVERE, "could not delete the log database entry", e);
		}
	}


	@Override
	public List<LogEntry> getLogEntries(Loggers appender, DateTime fromDate) {
		String query_str = "select le from LogEntry as le where le.appender =:appender and le.timeStamp >=:date order by le.timeStamp desc";
		try { 
			return manager.createQuery(query_str).setParameter("appender", appender.name()).setParameter("date", fromDate.toDate()).getResultList();
		} catch ( Exception e ) { 
			BASE_LOG.log(Level.SEVERE, "could not return log entries", e);
			RuntimeException re = new RuntimeException("error querying log database");
			re.initCause(e);
			throw re;
		}
	}


	@Override
	public List<LogEntry> getLogEntries(DateTime fromDate) {
		String query_str = "select le from LogEntry as le where le.timeStamp >=:date order by le.timeStamp desc";
		try { 
			return manager.createQuery(query_str).setParameter("date", fromDate.toDate()).getResultList();
		} catch ( Exception e ) { 
			BASE_LOG.log(Level.SEVERE, "could not return log entries", e);
			RuntimeException re = new RuntimeException("error querying log database");
			re.initCause(e);
			throw re;
		}
	}


	@Override
	public List<LogEntry> getLogEntries(Loggers appender, String type,
			DateTime fromDate) {
		String query_str = "select le from LogEntry as le where le.appender =:appender and le.type =:type and le.timeStamp >=:date order by le.timeStamp desc";
		try { 
			return manager.createQuery(query_str).setParameter("appender", appender.name()).setParameter("type", type).setParameter("date", fromDate.toDate()).getResultList();
		} catch ( Exception e ) { 
			BASE_LOG.log(Level.SEVERE, "could not return log entries", e);
			RuntimeException re = new RuntimeException("error querying log database");
			re.initCause(e);
			throw re;
		}
	}


	@Override
	public List<LogEntry> getLogEntries(Loggers appender, String type) {
		String query_str = "select le from LogEntry as le where le.appender =:appender and le.type =:type order by le.timeStamp desc";
		try { 
			return manager.createQuery(query_str).setParameter("appender", appender.name()).setParameter("type", type).getResultList();
		} catch ( Exception e ) { 
			BASE_LOG.log(Level.SEVERE, "could not return log entries", e);
			RuntimeException re = new RuntimeException("error querying log database");
			re.initCause(e);
			throw re;
		}
	}


	@Override
	public List<LogEntry> getLogEntries(Loggers appender) {
		String query_str = "select le from LogEntry as le where le.appender =:appender order by le.timeStamp desc";
		try { 
			return manager.createQuery(query_str).setParameter("appender", appender.name()).getResultList();
		} catch ( Exception e ) { 
			BASE_LOG.log(Level.SEVERE, "could not return log entries", e);
			RuntimeException re = new RuntimeException("error querying log database");
			re.initCause(e);
			throw re;
		}
	}


	@Override
	public List<LogEntry> getLogEntries(String type, DateTime fromDate) {
		String query_str = "select le from LogEntry as le where le.type =:type and le.timeStamp >=:date order by le.timeStamp desc";
		try { 
			return manager.createQuery(query_str).setParameter("type", type).setParameter("date", fromDate.toDate()).getResultList();
		} catch ( Exception e ) { 
			BASE_LOG.log(Level.SEVERE, "could not return log entries", e);
			RuntimeException re = new RuntimeException("error querying log database");
			re.initCause(e);
			throw re;
		}
	}


	@Override
	public List<LogEntry> getLogEntries(String type) {
		String query_str = "select le from LogEntry as le where le.type =:type order by le.timeStamp desc";
		try { 
			return manager.createQuery(query_str).setParameter("type", type).getResultList();
		} catch ( Exception e ) { 
			BASE_LOG.log(Level.SEVERE, "could not return log entries", e);
			RuntimeException re = new RuntimeException("error querying log database");
			re.initCause(e);
			throw re;
		}
	}


	@Override
	public List<LogEntry> getLogEntries() {
		String query_str = "select le from LogEntry as le order by le.timeStamp desc";
		try { 
			return manager.createQuery(query_str).getResultList();
		} catch ( Exception e ) { 
			BASE_LOG.log(Level.SEVERE, "could not return log entries", e);
			RuntimeException re = new RuntimeException("error querying log database");
			re.initCause(e);
			throw re;
		}
	}	
	
	
}
