/* Created On: Jan 19, 2008 */
package edu.unl.act.rma.firm.core;

/**
 * @author Ian Cottingham
 *
 */
public interface BooleanOperable<T> {

	public void and(T object);
	public void or(T object);
	public void not(T object);
	
}
