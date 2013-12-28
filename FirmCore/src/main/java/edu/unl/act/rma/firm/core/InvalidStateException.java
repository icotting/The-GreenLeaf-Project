/* Created On: Sep 7, 2005 */
package edu.unl.act.rma.firm.core;

import java.io.Serializable;

/**
 * @author Ian Cottingham
 *
 */
public class InvalidStateException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;

	public InvalidStateException(String str) { super(str); }
}
