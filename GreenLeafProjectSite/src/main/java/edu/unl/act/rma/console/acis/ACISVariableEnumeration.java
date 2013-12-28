/* Created On: May 25, 2005 */
package edu.unl.act.rma.console.acis;

import java.io.ObjectStreamException;


/**
 * @author Ian Cottingham
 *
 */
public enum ACISVariableEnumeration {

	HIGH_TEMP(1), 
	LOW_TEMP(2), 
	NORMAL_TEMP(3), 
	PRECIP(4), 
	RHAVG(71), 
	SOILTAVG_10(69), 
	WSAVG_3M(12), 
	SOLARRAD_LANGLEY(70), 
	WIND_VECTOR_DIR_3M(78) { 
		
	};
	
	private int acisID;
	public static final int COUNT = 9;
	
	private ACISVariableEnumeration(int var) { 
		this.acisID = var;
	}
	
	public int getUcanID() { 
		return acisID; 
	}
	
	public static short[] getUcanIDs() {
		ACISVariableEnumeration[] types = ACISVariableEnumeration.values(); 
		short[] ids = new short[types.length];
		for ( int i=0; i<types.length; i++ )
			ids[i] = (short)types[i].getUcanID();
		
		return ids;
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
}
