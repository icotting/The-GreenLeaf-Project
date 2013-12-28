/* Created On: Jun 11, 2005 */
package edu.unl.act.rma.firm.core.spatial;

import java.io.ObjectStreamException;

/**
 * A list of the regions defined by FIRM for the United States
 * 
 * @author Ian Cottingham
 */
public enum USRegion {

	NORTHEAST, 
	SOUTHEAST, 
	SOUTH, 
	MIDWEST, 
	PLAINS, 
	MOUNTIAN, 
	SOUTHWEST, 
	NORTHWEST;
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
	
}
