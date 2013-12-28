/* Created On: Aug 2, 2006 */
package edu.unl.act.rma.firm.core.configuration;

import java.rmi.RemoteException;

/**
 * @author Ian Cottingham
 *
 */
public class ConfigurationException extends RemoteException {

	private static final long serialVersionUID = 1L;

	public ConfigurationException(String msg) { 
		super(msg);
	}
}
