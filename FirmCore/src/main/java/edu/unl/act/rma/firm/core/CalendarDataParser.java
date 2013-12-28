/* Created On: Aug 25, 2005 */
package edu.unl.act.rma.firm.core;

import java.text.DecimalFormat;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.format.DateTimeFormat;


/**
 * The calendar data parser partitions daily data into monthly or weekly blocks, allowing 
 * individual period values, summations or averages to be returned.  This class functions as
 * an iterator, implementing next*() methods for retriving values in temporal order based
 * on a calendar; <code>DateTime</code>.
 * 
 * @author Ian Cottingham
 */
public final class CalendarDataParser {

	private static final DecimalFormat FORMAT = new DecimalFormat("#.###");
	private static final float DISCARD_THRESHHOLD = 0.80f;
	private static final Chronology CHRONOLOGY = GregorianChronology.getInstance();	
	
	private float[] data;
	private CalendarIterator iterator;
	
	private int dataPosition;
	private int weekPosition;
	private int monthPosition;
	private boolean nonExistant;
	private boolean nonExistantActual;
	
	private float[] weekData;
	private float[] monthData;
	private float dayData;
	private DateTime endOfPeriod;
	
	private boolean periodContainsNonExistant;
	private boolean firstMonth;
	private boolean firstWeek;
	
	/**
	 * Default constructor 
	 * 
	 * @param data the array of daily values for the period being parsed
	 * @param year the year on which the first (0) value of the array occurs
	 * @param month the month on which the first (0) value of the array occurs
	 * @param day the day on which the first (0) value of the array occurs
	 */
	public CalendarDataParser(float[] data, int year, int month, int day) {
		this.data = data;
		this.iterator = new CalendarIterator(year, month, day);

		endOfPeriod = iterator.myTime.plusDays(data.length);
		
		dataPosition = 0;
		
		firstMonth = true;
		firstWeek = true;
		
		/* fill out the current week and month, this will add MISSING values to all the days back from the current */
		newMonth();
		newWeek();
	}
	
	/**
	 * Checks if all days in the current <code>DateTime</code> month have been parsed. 
	 *  
	 * @return true if all values for the current month have been parsed
	 */
	public boolean hasMonthData() { 
		if ( !(iterator.isLastDayOFMonth()) ) { 
			return false;
		} else {
			if ( tomorrowNotExist() ) {
				return (nonExistant && !nonExistantActual) ? true : false;
			} else { 
				return true;
			}
		}
	}

	/**
	 * Checks if all days in the current <code>DateTime</code> week have been parsed. 
	 *  
	 * @return true if all values for the current week have been parsed
	 */
	public boolean hasWeekData() { 
		if ( !(iterator.isLastDayOfWeek()) ) { 
			return false;
		} else {
			if ( tomorrowNotExist() ) {
				return (nonExistant && !nonExistantActual) ? true : false;
			} else { 
				return true;
			}
		}
	}

	/**
	 * Checks if another day can be parsed from the data
	 * 
	 * @return true if the data contains another day's value
	 */
	public boolean hasNextDay() { 
		return ( dataPosition < data.length ) ? true : false;
	}
	
	/**
	 * Checks if another month can be parsed from the data (i.e. MAX(days_in_current_month))
	 * 
	 * @return true if MAX(days_in_current_month) more values can be parsed from the data
	 */
	public boolean hasNextMonth() { 
		return iterator.isBeforePlusMonths(endOfPeriod, 1);
	}
	
	/**
	 * Checks if another week can be parsed from the data (i.e. 7)
	 * 
	 * @return true if 7 more values can be parsed from the data
	 */
	public boolean hasNextWeek() { 
		return iterator.isBeforePlusWeeks(endOfPeriod, 1);
	}
	
	/**
	 * Checks if another year can be parsed from the data (i.e 366)
	 * 
	 * @return true if 366 more values can be parsed from the data
	 */
	public boolean hasNextYear() { 
		return iterator.isBeforePlusYears(endOfPeriod, 1);
	}
	
	/**
	 * Parses the next day from the daily data, adding the value to the current array of monthly and weekly
	 * data.  This function will clear the previous month or week if the day being parsed begins a new period
	 * 
	 * @return the value of the next day contained in the data
	 */
	public float nextDay() {
		if ( nonExistantActual ) {
			nonExistantActual = false;
			this.periodContainsNonExistant = true;
			return DataType.NONEXISTANT;
		} else if ( tomorrowNotExist() && !(nonExistant) ) {
			nonExistant = true;
			if ( iterator.myTime == iterator.startTime ) {
				nonExistantActual = true;
			} else {
				this.periodContainsNonExistant = true;
				return DataType.NONEXISTANT;
			}
		} else { 
			nonExistant = false;
		}
			
		if ( dataPosition != 0)
			iterator.nextDay();
		
		dayData = data[dataPosition++];
		
		if ( iterator.isFirstDayOfMonth() ) {
			if ( firstMonth ) {
				firstMonth = false;
			}
			newMonth();
		}
		
		if ( iterator.isFirstDayOfWeek() ) {
			if ( firstWeek ) { 
				firstWeek = false;
			}
			newWeek();
		}
		
		monthData[monthPosition++] = weekData[weekPosition++] = dayData;
		
		return dayData;
	}
	
	/**
	 * 
	 * @return the value parsed for the current day
	 */
	public float day() { 
		return dayData;
	}
	
	/**
	 * Advances to the next month and returns the sum of all values for the previous month
	 * 
	 * @return the sum of all previous month values
	 */
	public float nextMonthSum() { 
		incrementMonth();
		return monthSum();
	}
	
	/**
	 * Advances to the next month and returns the average of all values for the previous month
	 * 
	 * @return the average of all previous month values
	 */
	public float nextMonthAverage() { 
		incrementMonth();
		return monthAverage();
	}
	
	/**
	 * Advances to the next month and returns all values for the previous month
	 * 
	 * @return all previous month values
	 */
	public float[] nextMonth() { 
		incrementMonth();
		return month();
	}
	
	/**
	 * 
	 * @return the sum of all of the values for the current month
	 */
	public float monthSum() { 
		return sum(monthData);
	}
	
	/**
	 * 
	 * @return the average of all of the values for the current month
	 */
	public float monthAverage() { 
		return average(monthData);
	}
	
	/**
	 * 
	 * @return all of the values for the current month
	 */
	public float[] month() { 
		return monthData;
	}
	
	
	/**
	 * Advances to the next week and returns the sum of all values for the previous week
	 * 
	 * @return the sum of all previous week values
	 */
	public float nextWeekSum() { 
		incrementWeek();
		return weekSum();
	}

	/**
	 * Advances to the next week and returns the average of all values for the previous week
	 * 
	 * @return the average of all previous week values
	 */
	public float nextWeekAverage() { 
		incrementWeek();
		return weekAverage();
	}
	
	/**
	 * Advances to the next week and returns all values for the previous week
	 * 
	 * @return all previous week values
	 */	
	public float[] nextWeek() { 
		incrementWeek();
		return week();
	}
	
	/**
	 * 
	 * @return the sum of all the values for the current week
	 */
	public float weekSum() { 
		return sum(trimWeekData());
	}
	
	/**
	 * 
	 * @return the average of all the values for the current week
	 */
	public float weekAverage() { 
		return average(trimWeekData());
	}

	/**
	 * 
	 * @return all the values of the current week
	 */
	public float[] week() { 
		return trimWeekData();
	}
	
	/**
	 * 
	 * @return the number (1 - 52) of the current week in the current year, represented by the <code>DateTime</code> object.
	 */
	public int getWeekOfYear() { 
		return iterator.getWeekOfYear();
	}
	
	/**
	 * overrides functionality for the <code>DateTime</code> class, allowing for Feburary 29 to be returned as the last day
	 * of the month for all years
	 *  
	 * @return the number (1 - 31) of the current day of the current month as represented by the <code>DateTime</code> object.
	 */
	public int getDayOfMonth() { 
		if (tomorrowNotExist() && nonExistant)
			return 29;
		else
			return iterator.getDayOfMonth();
	}
	
	/**
	 * overrides functionality for the <code>DateTime</code> class, adjusting day count to account for Feb. 29 of 
	 * non-leap years.
	 *  
	 * @return the number (1 - 366) of the current day of the current year as represented by the <code>DateTime</code> object.
	 */
	public int getDayOfYear() {
		if ( !(iterator.myTime.year().isLeap()) && iterator.getDayOfYear() > 59  ) {
			return iterator.getDayOfYear() + 1;
		} else
			return iterator.getDayOfYear();
	}
	
	/**
	 * 
	 * @return the number (1 - 12) of the current month in the current year as represented by the <code>DateTime</code> object.
	 */	
	public int getMonthOfYear() { 
		return iterator.getMonthOfYear();
	}
	
	/**
	 * Overrides the functionality of the <code>DateTime</code> class, allowing for the previous year to be returned if the 52nd week
	 * of that year ends in Janurary of the next year.
	 * 
	 * @param period the period being considered when requesting the year
	 * @return the Gregorian year number of the current year as represented by the <code>DateTime</code> object.
	 */
	public int getYear(CalendarPeriod period) {
		/* this check is to ensure that the 52nd week of the previous year is actually recorded in the pervious year */
		if ( (period == CalendarPeriod.WEEKLY) && (iterator.getWeekOfYear() == 52) && (iterator.getMonthOfYear() == 1) ) { 
			return iterator.getYear()-1;
		} else 
			return iterator.getYear();
	}
	
	/**
	 * This function is used to enforce the standard of 366 values per year without overriding the 
	 * calendar.
	 * 
	 * @return boolean value indicating 02/29 in a non-leap year.  
	 */
	private boolean tomorrowNotExist() { 
		if ( (iterator.getMonthOfYear() == 2) && (iterator.getDayOfMonth() == 28) && 
				(iterator.myTime.plusDays(1).getDayOfMonth() != 29) ) {
			return true;
		} else {
			return false;
		}
	}
		
	/**
	 * Clears the current month's parsed data.  This method will prepopulate with missing values should 
	 * the current day not fall on the first day of the month.
	 *
	 */
	private void newMonth() { 
		monthData = new float[iterator.getMaximumDayOfMonth()];
		for ( int i=0; i<iterator.getDayOfMonth(); i++ ) { 
			monthData[i] = DataType.MISSING;
			monthPosition = i;
		}
	}
	
	/**
	 * Clears the current week's parsed data.  This method will prepopulate with missing values should 
	 * the current day not fall on the first day of the week.
	 *
	 */
	private void newWeek() { 
		weekData = new float[iterator.getMaximumDayOfWeek()];
		for ( int i=0; i<iterator.getDayOfWeek(); i++ ) {
			weekData[i] = DataType.MISSING;
			weekPosition = i;
		}
	}
	
	/**
	 * parse days to the first day of the next month.
	 *
	 */
	private void incrementMonth() {
		periodContainsNonExistant = false;
		nextDay();
		while ( !hasMonthData() ) {
			nextDay();
		}
	}
	
	/**
	 * parse days to the first day of the next week
	 *
	 */
	private void incrementWeek() {
		periodContainsNonExistant = false;
		nextDay();
		while ( !hasWeekData() ) {
			nextDay();
		}
	}
	
	/**
	 * 
	 * @param values some set of data
	 * @return the sum of argument data
	 */
	private float sum(float[] values) { 
		float sum_value = 0;
		float missing_count = 0;
		float noexistant_count = 0; 
		float range_count = 0;
		float error_count = 0;
		
		for ( int i=0; i<values.length; i++ ) {
			if ( values[i] == DataType.NONEXISTANT ) {
				noexistant_count++;
				continue;
			} else if ( values[i] == DataType.OUTSIDE_OF_RANGE ) {
				range_count++;
				continue;
			} else if ( values[i] == DataType.MISSING ) {
				missing_count++;
				continue;
			} else if ( values[i] == DataType.ERROR_RESULT ) { 
				error_count++;
				continue;
			} else {
				sum_value += values[i];
			}
		}

		int max_missing = (int) ((1.0f - DISCARD_THRESHHOLD) * values.length);
		
		if ( missing_count > max_missing ) {
			sum_value = DataType.MISSING;
		} else if ( noexistant_count > max_missing ) {
			sum_value = -DataType.NONEXISTANT;
		} else 	if ( range_count > max_missing ) {
			sum_value = DataType.OUTSIDE_OF_RANGE;
		} else if ( error_count > max_missing ) { 
			sum_value = DataType.ERROR_RESULT;
		} else if ( ((missing_count+noexistant_count+range_count+error_count) > max_missing) ) { 
			sum_value = DataType.MISSING;
		}
		
		return Float.parseFloat(FORMAT.format(sum_value));
	}
	
	/**
	 * 
	 * @param values some set of data
	 * @return the average value contained in the set
	 */
	private float average(float[] values) { 
		float sum_value = 0;
		float missing_count = 0;
		float non_exist_vals = (this.periodContainsNonExistant) ? 1 : 0;
		
		for ( int i=0; i<values.length; i++ ) {
			if ( values[i] == DataType.NONEXISTANT ) {
				continue;
			} else if ( values[i] == DataType.OUTSIDE_OF_RANGE ) {
				continue;
			} else if ( values[i] != DataType.MISSING ) {
				sum_value += values[i];
			} else { 
				missing_count++;
			}
		}
		
		float avg = -99;
		if ( (missing_count / values.length) < DISCARD_THRESHHOLD ) {
			avg = (sum_value / (values.length - (missing_count+non_exist_vals)));
		}
		
		return Float.parseFloat(FORMAT.format(avg));
	}
	
	/**
	 * Special function used in the case of week #53.  The system treats week 52 in these cases as a 14 day week, 
	 * in order to normalize the week for calculations, values on each day mod 7 are averaged, creating a weighted
	 * 7 day week. 
	 * 
	 * @return the weighted values for a 7 day normalized week
	 */
	private float[] trimWeekData() { 
		if ( weekData.length == 14 ) {
			float[] trimmed_values = new float[7];
			for ( int i=0; i<7; i++ ) { 
				if ( weekData[i] == DataType.MISSING ) 
					trimmed_values[i] = weekData[i+7];
				else if ( weekData[i+7] == DataType.MISSING )
					trimmed_values[i] = weekData[i];
				else 
					trimmed_values[i] = (weekData[i] + weekData[i+7]) / 2f;
			}
			
			return trimmed_values;
		} else {
			return weekData;
		}
	}
	
	/**
	 *
	 * This class 'extends' the functionality of org.joda.time.DateTime to allow for minor modification
	 * of some of the calendar functionality for FIRM data operations. Because the operations function on 
	 * the standard chronology (Gregorian), a seperate chronology need not be created.
	 * 
	 * This Calendar is designed to be a placeholder for calculations of time periods while stepping through
	 * data arrays.  If general calendar algebra is required this class should not be used, rather a org.joda.time.DateTime
	 * should be used.
	 *
	 */
	private class CalendarIterator {
		/*
		 * the localized instance of a DateTime for this class
		 */
		protected DateTime myTime;
		protected DateTime startTime;
		
		public CalendarIterator(int year, int month, int day) { 
			myTime = new DateTime(year, month, day, 0,0,0,0, CHRONOLOGY);
			startTime = myTime;
		}
		
		public CalendarIterator(DateTime dateTime) throws IllegalArgumentException {
			if ( dateTime.getChronology() != CHRONOLOGY )
				throw new IllegalArgumentException("argument date time must be of Chronology type "+CHRONOLOGY.getClass().getName());
			
			myTime = dateTime;
		}

		/**
		 * FIRM requires that all years contain 52 weeks, for this reason calling this function on the last
		 * day of week 52 in a year with 53 weeks will return false.  The net effect of this is that the last
		 * week of a 53 week year will have 14 days.
		 * 
		 * @return boolean indicating if the current day is the last day of the current week
		 */
		public boolean isLastDayOfWeek() {
			/* override the default chronology functionality forcing all years into 52 weeks. 
			 * FIRM tools require even 52 week years, code working with 'FIRM' weeks must account
			 * for this, commonly assuming that the last week of a 53 week year contains 14 days.
			 */
			if ( myTime.getDayOfWeek() == myTime.dayOfWeek().getField().getMaximumValue(myTime.toDate().getTime()) ) {
				return myTime.getWeekOfWeekyear() == 53 ? false : true; 
			} else {
				return false;
			}
		}
		
		public boolean isFirstDayOfMonth() { 
			return (myTime.dayOfMonth().getField().getMinimumValue(myTime.toDate().getTime()) == myTime.getDayOfMonth()) ? true : false;
		}
		
		public boolean isFirstDayOfWeek() { 
			return (myTime.dayOfWeek().getField().getMinimumValue(myTime.toDate().getTime()) == myTime.getDayOfWeek()) ? true : false;
		}
		
		/**
		 * 
		 * @return boolean indication if the current day is the last day of the current month 
		 */
		public boolean isLastDayOFMonth() { 
			if ( myTime.getDayOfMonth() == myTime.dayOfMonth().getField().getMaximumValue(myTime.toDate().getTime()) )
				return true;
			else
				return false;
		}
		
		/**
		 * 
		 * @return the number of days in the current year
		 */
		public int getMaximumDayOfYear() {
			/* every year in the FIRM system has 366 days, with 02.29 having a value of -100 in non-leap years */
			return 366; 
		}
		
		/**
		 * 
		 * @return the number of days in the current month
		 */
		public int getMaximumDayOfMonth() {
			/* in the FIRM system, Feburary always will have 29 days, with the value of day 29 being -100 in non-leap years */
			if ( myTime.getMonthOfYear() == 2 ) 
				return 29;
			else
				return myTime.dayOfMonth().getField().getMaximumValue(myTime.toDate().getTime());
		}
		
		/**
		 * Due to the FIRM requirement of 52 week years, this function will return a value of 14 if the 
		 * current week represented by this Calendar object is the 52nd or 53rd.  For non-53 week years
		 * this function will return 7 for week 52
		 * 
		 * @return the number of days in the current week
		 */
		public int getMaximumDayOfWeek() { 
			int last_week = myTime.minusWeeks(1).getWeekOfWeekyear();
			int this_week = myTime.getWeekOfWeekyear();
			
			if ( this_week == 53 || ((this_week == 1) && (last_week == 53)) )
				return 14;
			else 
				return myTime.dayOfWeek().getField().getMaximumValue();	
		}
		
		/**
		 * Due to FIRM requirements, week 53 is reported as week 1
		 * 
		 * @return current week of the year
		 */
		public int getWeekOfYear() { 
			int week = myTime.getWeekOfWeekyear();
			if ( week == 53 )
				week = 1;
			
			return week;
		}
			
		public int getMonthOfYear() { 
			return myTime.getMonthOfYear();
		}
		
		public int getDayOfYear() { 
			return myTime.getDayOfYear();
		}
		
		public int getDayOfMonth() { 
			return myTime.getDayOfMonth();
		}
		
		public int getYear() { 
			return myTime.getYear();
		}
		
		public int getDayOfWeek() { 
			int last_week = myTime.minusWeeks(1).getWeekOfWeekyear();
			int this_week = myTime.getWeekOfWeekyear();
			
			if ( (this_week == 1) && (last_week == 53) )
				return myTime.getDayOfWeek() *2;
			else
				return myTime.getDayOfWeek();
		}
		
		public int nextDay() {
			DateTime tmp = myTime.plusDays(1);
			int days_between = new Period(myTime, tmp, PeriodType.days()).getDays();
			myTime = tmp;
			
			return days_between;
		}
		
		public String toString() { 
			return myTime.toString(DateTimeFormat.mediumDate());
		}
				
		public boolean isBeforePlusDays(DateTime dt, int days) {
			DateTime hypo_time = myTime.plusDays(days);
			return (hypo_time.isBefore(dt) || hypo_time.isEqual(dt));
		}
		
		public boolean isBeforePlusMonths(DateTime dt, int months) {
			DateTime hypo_time = myTime.plusMonths(months);
			return (hypo_time.isBefore(dt) || hypo_time.isEqual(dt));
		}
		
		public boolean isBeforePlusWeeks(DateTime dt, int weeks) {
			DateTime hypo_time = myTime.plusWeeks(weeks);
			return (hypo_time.isBefore(dt) || hypo_time.isEqual(dt));
		}

		public boolean isBeforePlusYears(DateTime dt, int years) {
			DateTime hypo_time = myTime.plusYears(years);
			return (hypo_time.isBefore(dt) || hypo_time.isEqual(dt));
		}
	}
	
	public boolean isFirstWeek() { 
		return hasWeekData() && firstWeek;
	}
	
	public boolean isFirstMonth() { 
		return hasMonthData() && firstMonth;
	}
	
	@Override
	public String toString() {
		return getYear(CalendarPeriod.DAILY) + "." + getMonthOfYear() + "." + getDayOfMonth();
	}
}

