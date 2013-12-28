package edu.unl.act.rma.firm.core;

import org.joda.time.DateTime;

/**
 * Defines the date/time options for a single report.  Individual tools may choose to use any, or none, of the attributes.
 * 
 * @author Jon Dokulil
 */
public class DateOptionsDTO extends DTOBase {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	
	protected static final String ATTR_START = "start";
	protected static final String ATTR_END = "end";
	protected static final String ATTR_PERIOD = "period";
	protected static final String ATTR_INTERVAL_LENGTH = "intervalLength";
	protected static final String ATTR_MULTIYEAR = "multiyear";
	protected static final String ATTR_CONTINUOUS = "continuous";
	
	/**
	 * @return The continuous.
	 */
	public boolean isContinuous() {
		Boolean b = (Boolean) get(ATTR_CONTINUOUS);
		return b == null ? false : b;
	}
	/**
	 * @param continuous The continuous to set.
	 */
	public void setContinuous(boolean continuous) {
		set(ATTR_CONTINUOUS, continuous);
	}
	/**
	 * @return The end.
	 */
	public DateTime getEnd() {
		return (DateTime) get(ATTR_END);
	}
	/**
	 * @param end The end to set.
	 */
	public void setEnd(DateTime end) {
		set(ATTR_END, end);
	}
	/**
	 * The interval length is the number of periods to include for a single calculation.  With <code>period</code> set to 
	 * WEEKLY and an <code>intervalLength</code> of 4 calculations should be run for 4 weeks at a time.
	 * 
	 * @return The intervalLength.
	 */
	public Integer getIntervalLength() {
		return (Integer) get(ATTR_INTERVAL_LENGTH);
	}
	/**
	 * @param intervalLength The intervalLength to set.
	 */
	public void setIntervalLength(Integer intervalLength) {
		set(ATTR_INTERVAL_LENGTH, intervalLength);
	}
	/**
	 * @return The period.
	 */
	public CalendarPeriod getPeriod() {
		return (CalendarPeriod) get(ATTR_PERIOD);
	}
	/**
	 * @param period The period to set.
	 */
	public void setPeriod(CalendarPeriod period) {
		set(ATTR_PERIOD, period);
	}
	/**
	 * @return The start.
	 */
	public DateTime getStart() {
		return (DateTime) get(ATTR_START);
	}
	/**
	 * @param start The start to set.
	 */
	public void setStart(DateTime start) {
		set(ATTR_START, start);
	}
	/**
	 * @return The multiyear.
	 */
	public boolean isMultiyear() {
		Boolean b = (Boolean) get(ATTR_MULTIYEAR);
		return b == null ? false : b;
	}
	/**
	 * @param multiyear The multiyear to set.
	 */
	public void setMultiyear(boolean multiyear) {
		set(ATTR_MULTIYEAR, multiyear);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (properties.containsKey(ATTR_START)) {
			sb.append("start=");
			sb.append(getStart());
		}
		if (properties.containsKey(ATTR_START) && properties.containsKey(ATTR_END)) {
			sb.append(", ");
		}
		
		if (properties.containsKey(ATTR_END)) {
			sb.append("end=");
			sb.append(getEnd());
		}
		return sb.toString();
	}
}
