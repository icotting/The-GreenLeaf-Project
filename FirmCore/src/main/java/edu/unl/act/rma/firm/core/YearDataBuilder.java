/* Created On: Sep 7, 2005 */
package edu.unl.act.rma.firm.core;

import java.security.InvalidParameterException;

import org.joda.time.DateTime;


/**
 * @author Ian Cottingham
 *
 */
public final class YearDataBuilder extends PeriodOrderedDataBuilder {
 
	private static final long serialVersionUID = 1L;

	public YearDataBuilder(DateTime begin, DateTime end, CalendarPeriod period, DataType type) { 
		super(begin, end, period, type);
	}
			
	public YearDataBuilder(DateTime begin, DateTime end, CalendarPeriod period, DataType type, boolean beginInclusive, boolean endInclusive) {
		super(begin, end, period, type, beginInclusive, endInclusive);
	}
	
	@Override
	public CalendarDataCollection returnCollection() {
		return new CalendarDataCollection(this.begin, this.end, CalendarPeriod.ANNUALLY, this.periodType, this.dataType, this.results);
	}
	
	@Override
	protected void buildResultStorage() throws InvalidArgumentException, InvalidStateException {
		boolean endsNoExist = false;
		boolean startsNoExist = false;
		
		expectedValues = 0;
		switch (periodType) {
			case DAILY:
				periodStart = begin.getDayOfYear();				
				periodEnd = end.getDayOfYear();
		
				/* in a nonleap year add an extra day for the non-existant 29th of Feb.
				 * if the period starts before the 59th day, then it can be safely assumed
				 * that the incoming data accounts for this */
				if ( !(begin.year().isLeap()) && (begin.getDayOfYear() > 59) )
					periodStart++;
				
				/* in a nonleap year add an extra day for the non-existant 29th of Feb.
				 * if the period ends after the 59th day, then it can be safely assumed
				 * that the incoming data accounts for this */
				if ( !(end.year().isLeap()) && (end.getDayOfYear() > 59) )
					periodEnd++;
				
				break;
			case WEEKLY:
				periodStart = begin.getWeekOfWeekyear();				
				periodEnd = end.getWeekOfWeekyear();
				int begin_month = begin.getMonthOfYear();
				int end_month = end.getMonthOfYear();		
								
				if ( periodStart == 53 ) {
					if ( begin_month == 1 ) {
						periodStart = 52;
						begin = begin.minusDays(begin.getDayOfWeek() - 1); // shift the end date into the previous year
					} else {
						periodStart = 52; 
					}
				} else if ( periodStart == 52 && begin_month == 1 ) {
					periodStart = 52;
					begin = begin.minusDays(begin.getDayOfWeek() - 1); // shift the end date into the previous year
				} else if ( periodStart == 1 && begin_month == 12 ) {
					periodStart = 52;
				}
				
				if ( periodEnd == 53 ) {
					if ( end_month == 1 ) {
						periodEnd = 52;
						end = end.plusDays(end.getDayOfWeek() - 6); // shift the end date into the next year
					} else {
						periodEnd = 52;
					}
				} else if ( periodEnd == 52 && end_month == 1 ) {
					periodEnd = 52;
					end = end.plusDays(end.getDayOfWeek() - 6); // shift the end date into the next year
				} else if ( periodEnd == 1 && end_month == 12 ) { 
					periodEnd = 52;
				}

				break;
			case MONTHLY:
				periodStart = begin.getMonthOfYear();
				periodEnd = end.getMonthOfYear();

				break;
			case ANNUALLY: 
				break;
			default:
				throw new InvalidParameterException(periodType.name()+" period not supported");
			
		}
		
		if ( !endInclusive )
			periodEnd--;
		
		if ( !beginInclusive )
			periodStart++;
				

		int num_full = (end.getYear() - begin.getYear()) - 1; // the number of full years between the first year and last year
		expectedValues = num_full * periodType.getLength(); // add the number of values for these years to the expected count
			
		// add the number of periods from the beginning and ending years to the expected count
		expectedValues += (periodType.getLength() - periodStart) + 1;
		expectedValues += periodEnd; 
		
		int additions = (endsNoExist || startsNoExist) ? 2 : 1;
		stationResults = new float[(end.getYear() - begin.getYear())+additions][(periodType.getLength())];
	}
}
