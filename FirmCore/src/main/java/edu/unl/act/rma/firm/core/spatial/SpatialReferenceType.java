/* Created On Feb 13, 2007 */
package edu.unl.act.rma.firm.core.spatial;

import java.io.ObjectStreamException;

/**
 * @author Ian Cottingham
 *
 */
public enum SpatialReferenceType {

	CUSTOM, 
	US,
	US_COUNTY,
	US_STATE,
	US_CITY;
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
}
