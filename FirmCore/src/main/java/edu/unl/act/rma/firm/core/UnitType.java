/* Created On Aug 27, 2007 */
package edu.unl.act.rma.firm.core;

import java.io.ObjectStreamException;

/**
 * @author Ian Cottingham
 *
 */
public enum UnitType {

	ENGLISH, 
	METRIC;
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
}
