/* Created On: Aug 4, 2006 */
package edu.unl.act.rma.console.web;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.unl.act.rma.firm.core.configuration.CoreServiceImpl;
import edu.unl.act.rma.firm.core.configuration.jmx.ServerRuntime;
import edu.unl.act.rma.firm.drought.configuration.DroughtServiceImpl;

/**
 * @author Ian Cottingham
 *
 * @web.listener
 */
public class Deployer implements ServletContextListener {

	private static final Logger LOG = Logger.getLogger("javax.enterprise.system");
		
	public void contextDestroyed(ServletContextEvent arg0) {}

	public void contextInitialized(ServletContextEvent arg0) {
		System.setProperty("FIRM_VERBOSE", "true");
		System.setProperty("sun.rmi.server.exceptionTrace", "true");
				
		LOG.info("Deploying configuration objects");
		try {
			ServerRuntime.getInstance().deploy(CoreServiceImpl.class);
			ServerRuntime.getInstance().deploy(DIRDataServiceImpl.class);
			ServerRuntime.getInstance().deploy(OceanicDataServiceImpl.class);
			ServerRuntime.getInstance().deploy(NWSDataServiceImpl.class);
			ServerRuntime.getInstance().deploy(StreamFlowDataServiceImpl.class);
			ServerRuntime.getInstance().deploy(DroughtServiceImpl.class);
		} catch ( Exception e ) {
			LOG.log(Level.SEVERE, "An error occured deploying the configuration objects", e);
		}
		
		ServletContext ctx = arg0.getServletContext();
		
		List<String> service_names = new ArrayList<String>();

		service_names.add("edu.unl.firm:type=CoreService");
		service_names.add("edu.unl.firm:type=SpatialService");
		service_names.add("edu.unl.firm:type=DIRDataService");
		service_names.add("edu.unl.firm:type=OceanicDataService");
		service_names.add("edu.unl.firm:type=NWSDataService");
		service_names.add("edu.unl.firm:type=StreamFlowDataService");
		service_names.add("edu.unl.firm:type=DroughtService");
		
		ctx.setAttribute("SERVICE_NAMES", service_names);
	}
}
