/* Created On: Sep 11, 2005 */
package edu.unl.act.rma.firm.core;

import org.joda.time.DateTime;


/**
 * @author Ian Cottingham
 *
 */
public final class TemporalPeriod extends DTOBase {

	public static final TemporalPeriod EMPTY_PERIOD = new TemporalPeriod((Long)null, null);
	
	private static final long serialVersionUID = 2L;
	
	private DateTime start;
	private DateTime end;
	
	public TemporalPeriod() { }
	
	public TemporalPeriod(DateTime start, DateTime end) { 
		this.start = start; 
		this.end = end;
	}
	
	public TemporalPeriod(Long start, Long end) { 
		if ( start == null || start == -1 || end == null || end == -1 ) {
			return;
		} else { 
			this.start = new DateTime(start);
			this.end = new DateTime(end);
		}
	}
	
	/**
	 * 
	 * @return the period end date, if there is no valid end date then the current instant is returned
	 */
	public DateTime getEnd() {
		return (end != null) ? end : new DateTime(System.currentTimeMillis());
	}

	/**
	 * 
	 * @return the period start date, if there is no valid start date then the current instant is returned
	 */
	public DateTime getStart() {
		return (start != null) ? start : new DateTime(System.currentTimeMillis());
	}

	public boolean valid() {
		return ( start == null | end == null ) ? false : true;		
	}
	
	public int getYears() { 
		return ( start == null | end == null ) ? 0 : ( end.getYear() - start.getYear() ) + 1;
	}
	
	public TemporalPeriod concatenate(TemporalPeriod period) {
		if (!this.valid()) { 
			return period;
		}
		
		DateTime start = (this.start.isBefore(period.start)) ? period.start : this.start;
		DateTime end = (this.end.isAfter(period.end)) ? period.end : this.end;
		return new TemporalPeriod(start, end);
	}
	
	public TemporalPeriod union(TemporalPeriod period) {
		if (!this.valid()) { 
			return period;
		}
		
		DateTime start = (this.start.isBefore(period.start)) ? this.start : period.start;
		DateTime end = (this.end.isAfter(period.end)) ? this.end : period.end;
		return new TemporalPeriod(start, end);
	}
	
	public boolean contains(DateTime time) {
		if ( !this.valid() ) { 
			return false;
		}
		
		return ( !(time.isBefore(start)) && !(time.isAfter(end)) );
	}
	
	public boolean contains(TemporalPeriod period) { 
		if ( !this.valid() || !period.valid() ) { 
			return false;
		}
		
		return ( contains(period.getStart()) && contains(period.getEnd()) );
	}
	
	public void setStart(DateTime start) {
		this.start = start;
	}

	public void setEnd(DateTime end) {
		this.end = end;
	}

	@Override
	public String toString() {
		return new StringBuffer("start=").append(start).append(",end=").append(end).toString();
	}
}
