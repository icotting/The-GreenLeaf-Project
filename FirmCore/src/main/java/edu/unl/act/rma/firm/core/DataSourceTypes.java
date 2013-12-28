/* Created On: Oct 24, 2005 */
package edu.unl.act.rma.firm.core;

import java.io.ObjectStreamException;

/**
 * @author Ian Cottingham
 *
 */
public enum DataSourceTypes {
	CLIMATIC,
	CLIMATIC_BUILD,
	SYSTEM, 
	SOIL,
	OCEANIC,
	STREAM_FLOW;
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
}
