/* Created On Jul 24, 2007 */
package edu.unl.act.rma.firm.core;

import java.io.ObjectStreamException;

/**
 * @author Ian Cottingham
 * 
 *
 */
public enum BundleField {

	FIRST_NAME,
	LAST_NAME,
	EMAIL_ADDRESS,
	INSTITUTION,
	PRACTICE,
	STREET_ADDRESS,
	CITY,
	STATE,
	ZIP,
	PHONE, 
	PASSWORD;
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
}
