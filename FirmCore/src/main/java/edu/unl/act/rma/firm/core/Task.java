/* Created On: Jun 8, 2005 */
package edu.unl.act.rma.firm.core;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An abstraction of a threaded management process.  Extension of this class allows for 
 * dependency structure and process meta-data.
 * 
 * @author Ian Cottingham
 */
public abstract class Task implements Runnable {		
	
	private final Logger log = LogManager.getLogger(Loggers.SYSTEM_LOG, DataSourceInjector.class);
	
	private Semaphore semaphore;
	private boolean autoRelease;
	private boolean autoAquire; //this indicates that the semaphore should be aquired BEFORE the constructor returns
	
	protected long startTime;
	protected String taskName;
	protected long lastRun;
	protected Exception taskError;
	
	protected String taskStatus;
	protected TaskState taskState;
	
	protected float totalUnits;
	protected float unitsComplete;
	protected boolean halted;
	
	protected abstract void logic() throws Exception;
	
	/** this method contains the logic to be executed when the optional
	 * semaphore is aquired.
	 * 
	 * @throws Exception
	 */
	protected abstract void aquireLogic() throws Exception;
	
	public Task(String name) { 
		this.taskName = name;
		taskState = TaskState.NEVERRUN;
		taskStatus = "";
		totalUnits = 0;
		unitsComplete = 0;
	}
	
	
	/** optional constructor for multi-task interaction events, a permit on 
	 * the semaphore is aquired at construction time, so the order of object
	 * construction matters when using this constructor.
	 * 
	 * 
	 * @param name
	 * @param semaphore
	 * @param autoRelease - if this is set to true, the class must explicitly release the semiphore
	 */
	public Task(String name, Semaphore semaphore, boolean autoRelease, boolean autoAquire) { 
		this(name);
		this.semaphore = semaphore;
		aquireSemaphore();
	}
	
	/**
	 * aquires a lock to control order of exit for multiple tasks with dependencies
	 *
	 */
	private void aquireSemaphore() {
		try { 
			if ( autoAquire )
				semaphore.acquire();
		} catch ( InterruptedException e) { 
			log.error("error aquiring the semaphore", e);
		}
		
		new Thread() { 
			public void run() {
				try {
					if ( !autoAquire )
						semaphore.acquire();
	
					aquireLogic();
	
					if ( autoRelease )
						semaphore.release();
				} catch ( Exception e ) { 
					log.error("Error in aquiring or releasing the semiphore", e);
				}
			}
		}.start();
	}

	/**
	 * release the semaphore, indicating that the task is complete, allowing dependant tasks to exit when complete
	 *
	 */
	public void releaseSemaphore() { 
		this.semaphore.release();
	}
	
	/** 
	 * generic runner method allowing defined logic to execute in a thread <code>java.lang.Runnable</code>
	 */
	public void run() {
		startTime = System.currentTimeMillis();
		try {				
			taskState = TaskState.RUNNING;
			logic();
			taskState = TaskState.COMPLETE;
			lastRun = System.currentTimeMillis();
			DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
			taskStatus = "Process completed on: "+df.format(lastRun);
		} catch ( Exception e ) {
			taskState = TaskState.ERROR;
			taskStatus = "[ERROR]: "+e.getMessage();
			taskError = e;
			log.error("unknown task error", e);
		}
	}
	
	private static NumberFormat FORMAT = NumberFormat.getPercentInstance();
	
	/**
	 * 
	 * @return the stringified percentage of completion of the current task
	 */
	public String percentComplete() {
		return FORMAT.format(unitsComplete / totalUnits);
	}
	
	/**
	 * 
	 * @return the current state of the task
	 */
	public TaskState getTaskState() { 
		return taskState;
	}
	
	/**
	 * 
	 * @return true if an exception has been thrown during execution of the task logic
	 */
	public boolean isError() { 
		return ( taskError == null ) ? false : true;
	}
	
	/**
	 * 
	 * @return an exception thrown by lower level logic code
	 */
	public Exception getTaskError() { 
		return taskError;
	}
	
	/**
	 * 
	 * @return a description of the current work being done by the task
	 */
	public String getTaskStatus() { 
		return taskStatus;
	}
	
	/**
	 * 
	 * @return the total time in miliseconds for which the task executed
	 */
	public long getRunTime() { 
		if ( (taskState == TaskState.RUNNING) || (taskState == TaskState.WAITING) ) 
			return ( System.currentTimeMillis() - startTime);
		else
			return ( lastRun - startTime );
	}
	
	/**
	 * 
	 * @return the name of the task
	 */
	public String getTaskName() {
		return taskName;
	}
	
	/**
	 * 
	 * @return the time in milliseconds since the task weas started
	 */
	public long getLastRun() {
		return lastRun;
	}
		
	/** 
	 * generates an XML representation of the task object 
	 *
	 */
	public Node export() throws ParserConfigurationException, TransformerConfigurationException, TransformerException { 
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document d = db.newDocument();
		d.setXmlStandalone(true);
		Element root = d.createElement("ManagementTask");
		root.setAttribute("taskName", this.getTaskName());
		try { 
			root.setAttribute("taskDescription", (String)this.getClass().getField("DESCRIPTION").get(this));
		} catch ( Exception e ) { 
			log.warn("could not get task description, please implement tasks with a brief description");
		}

		Element tmp = d.createElement("State");
		tmp.appendChild(d.createTextNode(this.getTaskState().name()));
		root.appendChild(tmp);
		
		tmp = d.createElement("Status");
		tmp.appendChild(d.createTextNode(this.getTaskStatus()));
		root.appendChild(tmp);
				
		tmp = d.createElement("Progress");
		tmp.appendChild(d.createTextNode(this.percentComplete()));
		root.appendChild(tmp);
		
		tmp = d.createElement("Runtime");
		tmp.appendChild(d.createTextNode(String.valueOf(this.getRunTime())));
		root.appendChild(tmp);
		
		tmp = d.createElement("Lastrun");
		tmp.appendChild(d.createTextNode(String.valueOf(this.getLastRun())));
		root.appendChild(tmp);
				
		return root;
	}
	
	public void addToTotalUnits(float total) { 
		this.totalUnits += total;
	}
	
	public void subtractFromTotalUnits(float total) { 
		this.totalUnits -= total;
	}
	
	public void incrementWorkedUnits(float units) { 
		this.unitsComplete += units;
	}
	
	public void setStatus(String status) {
		this.taskStatus = status;
	}
	
	@Override
	public String toString() {
		return taskName == null ? "" : taskName;
	}
	
	public void haltTask() { 
		this.halted = true;
	}
}
