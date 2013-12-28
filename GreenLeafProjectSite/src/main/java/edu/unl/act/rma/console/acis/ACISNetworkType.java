/* Created On: May 23, 2005 */
package edu.unl.act.rma.console.acis;

import java.io.ObjectStreamException;

/**
 * @author Ian Cottingham
 *
 */
public enum ACISNetworkType {

	COOP("coop", 1), 
	AWDN("awdn", 2){ };
	
	private String acisString;
	private int networkID;
	
	private ACISNetworkType(String str, int type) { 
		acisString = str;
		networkID = type;
	}
	
	public String getUcanString() { 
		return acisString;
	}
	
	public int getNetworkID() { 
		return networkID;
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
}
