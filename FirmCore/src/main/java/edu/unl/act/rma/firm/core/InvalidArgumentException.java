/* Created On: Sep 7, 2005 */
package edu.unl.act.rma.firm.core;

import java.io.Serializable;


/**
 * @author Ian Cottingham
 *
 */
public class InvalidArgumentException extends Exception implements Serializable {

	private static final long serialVersionUID = 2L;

	public InvalidArgumentException(String str) { super(str); }
}
