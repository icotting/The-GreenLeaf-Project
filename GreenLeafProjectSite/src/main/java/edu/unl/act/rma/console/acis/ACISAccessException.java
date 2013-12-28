/* Created On: May 25, 2005 */
package edu.unl.act.rma.console.acis;

import java.io.IOException;

/**
 * @author Ian Cottingham
 *
 */
public class ACISAccessException extends IOException { 

	private static final long serialVersionUID = 1L;

	public ACISAccessException(String str) { super(str); }
}
