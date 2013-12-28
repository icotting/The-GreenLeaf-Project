/* Created On: Jul 15, 2005 */
package edu.unl.act.rma.firm.core;

import java.io.ObjectStreamException;

/**
 * Type enumration representing the possible temporal periods available to the system. 
 *  
 * @author Ian Cottingham
 */
public enum CalendarPeriod {

	DAILY(366, "Day"), 
	WEEKLY(52, "Week"), 
	MONTHLY(12, "Month"), 
	ANNUALLY(1, "Year") { };
	
	private CalendarPeriod(int l, String prettyPrint) { 
		this.length = l;
		this.prettyPrint = prettyPrint;
	}
	
	int length;
	int milisecondsInPeriod;
	String prettyPrint;
	
	/**
	 * 
	 * @return the length of the period in days
	 */
	public int getLength() { 
		return length;
	}

	public String getPrettyPrint() {
		return prettyPrint;
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
}
