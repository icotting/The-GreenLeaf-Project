/* Created On: Aug 2, 2006 */
package edu.unl.act.rma.firm.core.configuration.jmx;

import java.rmi.RemoteException;

/**
 * @author Ian Cottingham
 *
 */
public class JMXException extends RemoteException {

	private static final long serialVersionUID = 1L;

	public JMXException(String msg) { 
		super(msg);
	}
}
