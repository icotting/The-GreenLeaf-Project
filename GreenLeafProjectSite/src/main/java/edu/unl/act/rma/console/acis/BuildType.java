/* Created On: Jul 20, 2005 */
package edu.unl.act.rma.console.acis;

import java.io.ObjectStreamException;

/**
 * @author Ian Cottingham
 *
 */
public enum BuildType {

	BUILD { };
		
	public static BuildType fromString(String str) { 
		if ( str.equals("build") )
			return BUILD;

		throw new RuntimeException("unknown build type");
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
}
