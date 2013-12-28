/* Created on Nov 20, 2008 */
package edu.unl.act.rma.firm.core;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * @author Ian Cottingham
 *
 */
@Entity
@Table(name="LogEntries")
@TableGenerator(name="LOG_ENTRY_GEN", table="GENERATOR_TABLE")
@XStreamAlias("LogEntry")
public class LogEntry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final DateFormat TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.FULL);
	private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);	
	
	private int id;
	private String type;
	private String appender;
	private String className;
	private String message;
	private String stackTrace;
	private Date timeStamp;
	
	public LogEntry() { 
		this.timeStamp = new Date(System.currentTimeMillis());
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="LOG_ENTRY_GEN")
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getAppender() {
		return appender;
	}
	
	public void setAppender(String appender) {
		this.appender = appender;
	}
	
	public String getClassName() {
		return className;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	@Column(columnDefinition="longtext")
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	@Column(columnDefinition="longtext")
	public String getStackTrace() {
		return stackTrace;
	}
	
	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date getTimeStamp() {
		return timeStamp;
	}
	
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public String toString() {
		StringWriter str_writer = new StringWriter();
		PrintWriter out = new PrintWriter(str_writer);
		
		out.printf("[%s|%s] registered on %s at %s in class %s: %s\n", this.type, this.appender, DATE_FORMAT.format(this.timeStamp), TIME_FORMAT.format(this.timeStamp), this.className, this.message);
		if ( this.stackTrace != null ) { 
			out.printf("-- Begin Stack Trace --\n%s\n--End STack Trace--", this.stackTrace);
		}
		
		return str_writer.toString();
	}	
	
	public boolean isShowStackTrace() { 
		return ((stackTrace != null) && !(stackTrace.trim().equals("")));
	}
	
}
