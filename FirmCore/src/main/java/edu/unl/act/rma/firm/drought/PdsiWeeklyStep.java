/**
 * 
 */
package edu.unl.act.rma.firm.drought;

import java.io.ObjectStreamException;

import edu.unl.act.rma.firm.core.CalendarPeriod;

/**
 * Type enumeration representing the possible PDSI weekly step values.
 * 
 * @author Jon Dokulil
 */
public enum PdsiWeeklyStep {
	ONE_WEEK("weekly", 1),
	TWO_WEEK("2-week weekly", 2),
	FOUR_WEEK("4-week weekly", 4),
	THIRTEEN_WEEK("13-week weekly", 13);
	
	/**
	 * Gets the 'pretty-print' version of the step.
	 * @return The step
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Gets the length, in weeks, of the step.
	 * @return The step length
	 */
	public int getStep() {
		return this.step;
	}
	
	/**
	 * Gets the number of values that will occur in one year.
	 * @return The number of values of this step per year
	 */
	public int getValuesPerYear() {
		return CalendarPeriod.WEEKLY.getLength() / this.step;
	}
	
	private PdsiWeeklyStep(String name, int step) {
		this.name = name;
		this.step = step;
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
	
	private String name;
	private int step;
}
