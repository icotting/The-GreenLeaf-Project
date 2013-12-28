/* Created On: Apr 27, 2006 */
package edu.unl.act.rma.firm.climate;

import java.io.Serializable;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.core.TemporalPeriod;

/**
 * @author Ian Cottingham 
 *
 */
public class VariableMetaData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	
	private final DateTime startDate;
	private final DateTime endDate;
	private final String name;
	private final float missingPercent;
	
	public VariableMetaData(long startDate, long endDate, String name, float missingPercent) { 
		this.startDate = new DateTime(startDate);
		this.endDate = new DateTime(endDate);
		this.name = name;
		this.missingPercent = missingPercent;
	}

	public DateTime getEndDate() {
		return endDate;
	}

	public float getMissingPercent() {
		return missingPercent;
	}

	public String getName() {
		return name;
	}

	public DateTime getStartDate() {
		return startDate;
	}
	
	public TemporalPeriod getVariablePeriod() { 
		return new TemporalPeriod(startDate.getMillis(), endDate.getMillis());
	}
	
	@Override
	public String toString() {
		return "name="+ name +",start="+ startDate +",end="+ endDate +",missing="+ missingPercent;
	}
}
