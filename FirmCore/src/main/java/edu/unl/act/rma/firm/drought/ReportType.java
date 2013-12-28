/* Created On Feb 22, 2007 */
package edu.unl.act.rma.firm.drought;

import java.io.ObjectStreamException;

/**
 * @author Ian Cottingham
 *
 */
public enum ReportType {
	
	MEDIA("Media Report"), 
	USER("User Report"),
	IMPACT("Impact Report"),
	SEARCH_RESULT("Search Results");
	
	private String print;
	
	private ReportType(String str) { 
		this.print = str;
	}
	
	public String print() { 
		return this.print;
	}

	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
}
