/* Created On: Jul 26, 2005 */
package edu.unl.act.rma.console.acis;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.w3c.dom.Node;

import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.Task;
import edu.unl.act.rma.firm.core.TaskState;

/**
 * @author Ian Cottingham
 *
 */
public class BuildMonitor {

	private Logger LOG = LogManager.getLogger(Loggers.SYSTEM_LOG, BuildMonitor.class);
	
	private BlockingQueue<Collection<ACISResult>> queue;
    private ACISQueryWriter queryWriter;
    private ACISDataWriter dataWriter;
    private DateTime startTime;
    private DateTime aproxEndTime;
    private BuildType type;
    
    public BuildMonitor(BlockingQueue<Collection<ACISResult>> queue, ACISQueryWriter query, ACISDataWriter data, BuildType type) { 
    		this.queue = queue;
    		this.queryWriter = query; 
    		this.dataWriter = data;
    		startTime = new DateTime(System.currentTimeMillis());
    		this.type = type;
    		aproxEndTime = null;
    }
	
    public int queueSize() { return queue.size(); }
    public int remainingQueueCapacity() { return queue.remainingCapacity(); }
    
    public Node dataWriterXML() throws ParserConfigurationException, TransformerConfigurationException, TransformerException { 
    		return dataWriter.export(); 
    	}
    
    public Task getDataWriter() { 
    	return dataWriter;
    }
    
    public Task getQueryWriter() { 
    	return queryWriter;
    }
    
    public Node queryWriterXML() throws ParserConfigurationException, TransformerConfigurationException, TransformerException { 
    		return queryWriter.export(); 
    	}
    
    public Period getRuntime() { 
    		if ( aproxEndTime != null )
    			return new Period(startTime, aproxEndTime, PeriodType.seconds());
    		else if ( (queryWriter.getTaskState() == TaskState.COMPLETE) && (dataWriter.getTaskState() == TaskState.COMPLETE) ) {
    			return new Period(startTime, new DateTime(dataWriter.getLastRun()), PeriodType.seconds());
    		} else {
    			return new Period(startTime, new DateTime(System.currentTimeMillis()), PeriodType.seconds());
    		}
    }
    
    public BuildType getType() { 
    		return type;
    }
    
    public void haltBuild() { 
    	LOG.info("Executing a halt operation on the data build");
    	queryWriter.haltTask();
    	dataWriter.haltTask();
    }
    
}
