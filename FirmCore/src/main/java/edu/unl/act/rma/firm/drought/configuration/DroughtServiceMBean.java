/* Created on: Apr 22, 2010 */
package edu.unl.act.rma.firm.drought.configuration;

import edu.unl.act.rma.firm.core.configuration.ConfigurationException;

/**
 * @author Ian Cottingham
 *
 */
public interface DroughtServiceMBean {

	
	public void importDroughtMonitorData(String startDate, String endDate) throws ConfigurationException;
	public void importAllDroughtMonitorData() throws ConfigurationException;

	public String getImporterStatus() throws ConfigurationException;
}
