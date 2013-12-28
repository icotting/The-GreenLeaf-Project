/* Created On: Jun 13, 2005 */
package edu.unl.act.rma.console.acis;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import org.joda.time.DateTime;

import edu.unl.act.rma.console.web.NWSDataServiceManager;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;

/**
 * @author Ian Cottingham
 *
 */
public class ACISDataBuilder {

	private Logger LOG = LogManager.getLogger(Loggers.SYSTEM_LOG, ACISDataBuilder.class);
	
	private Thread query_thread;
	private Thread data_thread;
	
	public BuildMonitor runBuild(String tmpPath, BuildType type, DateTime runFrom, String stateRegex, int months) {
		
		tmpPath += "/sqlTmp/";
		File f = new File(tmpPath);
		f.mkdir();
		
		BuildMonitor monitor = null;
		try {
			/* adjust the calendar to run from k months prior to the given date */
			runFrom = runFrom.minusMonths(months);
			
			/* adjust to the first day of the month */
			runFrom = runFrom.minusDays((runFrom.getDayOfMonth() - runFrom.dayOfMonth().getField().getMinimumValue()));
			
	        final Pattern regex = Pattern.compile(stateRegex);
	        NWSDataServiceManager manager = NWSDataServiceManager.getInstance();
	        final BlockingQueue <Collection<ACISResult>> queue = new ArrayBlockingQueue<Collection<ACISResult>>(manager.getMaxQueueSize());
	        
	        Semaphore s = new Semaphore(1, true);
	        ACISQueryWriter query = new ACISQueryWriter(this, queue, regex, type, runFrom, s);
	        ACISDataWriter data = new ACISDataWriter(tmpPath, queue, type, s);
	        
	        //TODO: there should be some kind of pipe to pass worked units or some more elegant way of getting the query writer a data writer ... 
	        //perhaps the query writer should spawn a data writer for a more explicit binding
	        
	        /* set the data writer so that its worked units can be updated */
	        query.setDataWriter(data);
	        
	        query_thread = new Thread(query);
	        data_thread = new Thread(data);
	        
	        query_thread.start();
	    	        
	        monitor = new BuildMonitor(queue, query, data, type);
		} catch ( Exception e ) { 
			LOG.error("unknown error", e);
		}
		
		return monitor;
	}
	
	protected void startDataWriter() { 
		data_thread.start();
	}
}
