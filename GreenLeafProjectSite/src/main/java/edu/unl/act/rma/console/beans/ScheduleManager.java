/* Created On Sep 21, 2010 */
package edu.unl.act.rma.console.beans;

import javax.ejb.Schedule;
import javax.ejb.Startup;
import javax.ejb.Stateless;

import edu.unl.act.rma.console.web.DIRDataServiceManager;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;


/**
 * @author Ian Cottingham
 *
 */
@Stateless
@Startup
public class ScheduleManager {

	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, ScheduleManager.class);
	
	@Schedule(dayOfWeek="6", hour="7", minute="0")
	public void updateDir() {
		try {
			DIRDataServiceManager manager = DIRDataServiceManager.getInstance();
			manager.run();
		} catch ( Exception e ) { 
			LOG.error("An error occurred when running the DIR update", e);
		}
	}
}
