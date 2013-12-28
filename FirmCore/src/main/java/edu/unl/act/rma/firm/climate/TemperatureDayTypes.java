package edu.unl.act.rma.firm.climate;

import java.io.ObjectStreamException;

/**
 * Type enumeration representing the temperature days request types available to the system.
 */
public enum TemperatureDayTypes {

	HIGH_GREATER_THAN,
	HIGH_LESS_THAN, 
	LOW_GREATER_THAN,
	LOW_LESS_THAN;

	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
	
}
