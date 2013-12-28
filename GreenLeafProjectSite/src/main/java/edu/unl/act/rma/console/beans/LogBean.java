/* Created on Nov 25, 2008 */
package edu.unl.act.rma.console.beans;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.core.BeanInjector;
import edu.unl.act.rma.firm.core.LogEntry;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.component.LogDatabase;

/**
 * 
 * @author Ian Cottingham
 *
 */
@ManagedBean(name="logBean")
@SessionScoped
public class LogBean {

	private LogDatabase logDb;
	
	private List<LogEntry> currentEntries;
	private String type = "ALL";
	private String appender = "ALL";
	private DateTime fromDate;
	private String dateDisplay;
	private String appenderDisplay;
	private String typeDisplay;
	
	public LogBean() throws Exception { 
		this.logDb = BeanInjector.getInstance().getSessionBean(LogDatabase.class);
		this.typeDisplay = "all";
		this.appenderDisplay = "all appenders";
		entireHistory();
		update();
	}

	public List<LogEntry> getCurrentEntries() {
		return currentEntries;
	}
	
	public void clear() { 
		logDb.clearLog();
		update();
	}
	
	public void update() { 
		if ( this.type.equals("ALL") && appender.equals("ALL") ) {
			this.currentEntries = logDb.getLogEntries(this.fromDate);
		} else { 
			if ( !(type.equals("ALL")) && appender.equals("ALL") ) {
				this.currentEntries = logDb.getLogEntries(this.type, this.fromDate);
			} else if ( type.equals("ALL") && !(appender.equals("ALL")) ) { 
				this.currentEntries = logDb.getLogEntries(Loggers.valueOf(appender), this.fromDate);
			} else { 
				this.currentEntries = logDb.getLogEntries(Loggers.valueOf(appender), this.type, this.fromDate);
			}
			
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
		if ( this.type.equals("ALL") ) { 
			this.typeDisplay = "all";
		} else { 
			this.typeDisplay = this.type.toLowerCase();
		}
	}

	public String getAppender() {
		return appender;
	}

	public void setAppender(String appender) {
		this.appender = appender;
		if ( this.appender.equals("ALL") ) { 
			this.appenderDisplay = "all appenders";
		} else { 
			this.appenderDisplay = "the "+this.appender+" appender";
		}
	}	
	
	public void pastDay() { 
		this.fromDate = new DateTime(System.currentTimeMillis()).minusDays(1);
		dateDisplay = "past day";
	}

	public void pastWeek() { 
		this.fromDate = new DateTime(System.currentTimeMillis()).minusWeeks(1);
		dateDisplay = "past week";
	}
	
	public void pastTwoWeeks() { 
		this.fromDate = new DateTime(System.currentTimeMillis()).minusWeeks(2);
		dateDisplay = "past two weeks";
	}
	
	public void pastMonth() { 
		this.fromDate = new DateTime(System.currentTimeMillis()).minusMonths(1);
		dateDisplay = "past month";
	}
	
	public void entireHistory() { 
		this.fromDate = new DateTime(2000,1,1,0,0,0,0,GregorianChronology.getInstance()); // cannot be earlier than 2007 actually
		dateDisplay = "entire log history";
	}
	
	public void customDate(int year, int month, int day){
		this.fromDate = new DateTime(year,month,day,0,0,0,0,GregorianChronology.getInstance());
		dateDisplay = "custom date";
	}

	public String getFromString() {
		return dateDisplay;
	}

	public String getDateDisplay() {
		return dateDisplay;
	}

	public String getAppenderDisplay() {
		return appenderDisplay;
	}

	public String getTypeDisplay() {
		return typeDisplay;
	}
}
