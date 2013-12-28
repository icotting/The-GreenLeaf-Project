package edu.unl.act.rma.firm.drought;

import java.io.ObjectStreamException;

/**
 * Enumeration of the various soil moisture regimes used by the Newhall (NSM) component.
 * 
 * @author Jon Dokulil
 */
public enum SoilMoistureRegime {
	ARIDIC("ARIDIC", 0),
	XERIC("XERIC", 1),
	USTIC("USTIC", 2),
	PERUDIC("PERUDIC", 3),
	UDIC("UDIC", 4),
	UNDEFINED("UNDEFINED", 5);
	
	private SoilMoistureRegime(String name, int index) {
		this.prettyPrint = name;
		this.index = index;
	}
	
	/**
	 * @return The regime's name
	 */
	public String getName() {
		return this.prettyPrint;
	}
	
	/**
	 * Returns the NSM library's index of this regime.  This attribute is only used by the 
	 * NSM component to interpret the results of the NSM native library.
	 * 
	 * @return The NSM native library's index representing this regime
	 */
	public int getIndex() {
		return this.index;
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
	
	private String prettyPrint;
	private int index;
}
