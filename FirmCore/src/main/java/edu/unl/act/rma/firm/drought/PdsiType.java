package edu.unl.act.rma.firm.drought;

import java.io.ObjectStreamException;

/**
 * Type enumeration representing the possible types of PDSI results.
 * 
 * @author Jon Dokulil
 */
public enum PdsiType {
	PDSI("PDSI", 1),
	WPDSI("WPDSI", 2),
	PHDI("PHDI", 3),
	ZIND("ZIND", 4);
	
	private PdsiType(String name, int type) {
		this.name = name;
		this.type = type;
	}
	
	/**
	 * Gets the type name.
	 * @return The type name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Returns the internal <i>type</i>, used internally by the system.
	 * @return
	 */
	public int getType() {
		return this.type;
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
	
	private String name;
	private int type;
}
