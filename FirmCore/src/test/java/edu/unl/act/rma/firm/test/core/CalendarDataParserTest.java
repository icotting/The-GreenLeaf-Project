/* Created On: Sep 8, 2005 */
package edu.unl.act.rma.firm.test.core;

import junit.framework.TestCase;
import edu.unl.act.rma.firm.core.CalendarDataParser;

/**
 * @author Ian Cottingham
 *
 */
public class CalendarDataParserTest extends TestCase {

	public void testNonLeapYearWeek9() { 
		float[] f = new float[366];
		for ( int i=0; i<366; i++ )
			f[i] = 1;
		
		/* a non-leap year calendar */
		CalendarDataParser parser = new CalendarDataParser(f, 2005, 01, 02);
		
		int week_num = 0;
		while ( parser.hasNextDay() ) {
			parser.nextDay();
			if ( parser.hasWeekData() ) {
				assertTrue("correctly parses past the non-existant 02/29", week_num != parser.getWeekOfYear());
				week_num = parser.getWeekOfYear();
			}
		}
	}
	
	public void testNonLeapYearWeek9Sum() { 
		float[] f = new float[366];
		for ( int i=0; i<366; i++ )
			f[i] = i;
		
		/* a non-leap year calendar */
		CalendarDataParser parser = new CalendarDataParser(f, 2005, 01, 01);
		
		while ( parser.hasNextDay() ) {
			parser.nextDay();
			if ( parser.hasWeekData() ) {
				if ( parser.getWeekOfYear() == 8 )
					assertEquals("correctly sums the week before the non-existant day week", 378.0f, parser.weekSum());
				
				if ( parser.getWeekOfYear() == 9 )
					assertEquals("correctly sums the week of the non-existant day", 427.0f, parser.weekSum());
			}
		}
	}
	
	public void testRangeOfYearsWeekly() {
		for ( int years=1850; years<2006; years++ ) { 
			float[] f = new float[366];
			for ( int i=0; i<366; i++ )
				f[i] = 1;
	
			/* a non-leap year calendar */
			CalendarDataParser parser = new CalendarDataParser(f, years, 01, 02);
			int week_num = 0;
			while ( parser.hasNextDay() ) {
				parser.nextDay();
				if ( parser.hasWeekData() ) {
					assertTrue("correctly parses past weeks of non-leap years", week_num != parser.getWeekOfYear());
					week_num = parser.getWeekOfYear();
				}
			}
		}
	}
	
	public void testRangeOfYearsMonthly() {
		for ( int years=1850; years<2006; years++ ) { 
			float[] f = new float[366];
			for ( int i=0; i<366; i++ )
				f[i] = 1;
	
			/* a non-leap year calendar */
			CalendarDataParser parser = new CalendarDataParser(f, years, 01, 02);
			int month_num = 0;
			while ( parser.hasNextDay() ) {
				parser.nextDay();
				if ( parser.hasMonthData() ) {
					assertTrue("correctly parses past months of non-leap years", month_num != parser.getMonthOfYear());
					month_num = parser.getMonthOfYear();
				}
			}
		}
	}
	
	public void testBeginsMonthEndingFeb() { 
		float[] f = new float[366];
		for ( int i=0; i<366; i++ )
			f[i] = i;
		
		/* a non-leap year calendar */
		CalendarDataParser parser = new CalendarDataParser(f, 2003, 2, 28);
		
		int month_num = 1;
		while ( parser.hasNextDay() ) {
			parser.nextDay();
			if ( parser.hasMonthData() ) {
				assertTrue("months increment correctly", month_num != parser.getMonthOfYear());
				month_num = parser.getMonthOfYear();
			}
		}
	}
	
	public void testBeginsWeekEndingFeb() { 
		float[] f = new float[766];
		for ( int i=0; i<766; i++ )
			f[i] = 1;
		
		CalendarDataParser parser = new CalendarDataParser(f, 1954, 2, 28);
			
		int week_num = 7;
		while ( parser.hasNextDay() ) {
			parser.nextDay();
			if ( parser.hasWeekData() ) {
				assertTrue("months increment correctly", week_num != parser.getWeekOfYear());
				week_num = parser.getWeekOfYear();
			}
		}
	}
	
	public void testFirstWeekCheck() { 
		float[] f = new float[20];
		for ( int i=0; i<20; i++ )
			f[i] = 1;
		
		CalendarDataParser parser = new CalendarDataParser(f, 2006, 3, 1);
		boolean seen_week = false;
		int day_count = 0;
		while ( parser.hasNextDay() ) { 
			parser.nextDay(); day_count++;
			if ( parser.hasWeekData() ) {
				if ( !seen_week ) { 
					assertTrue("does not rgister the first week", parser.isFirstWeek());
					assertEquals("incorrect parsed days", 5, day_count);
					seen_week = true;
				} else {
					assertFalse("still registers a first week", parser.isFirstWeek());
				}
			}
		}
	}
	
	public void testFirstWeekCheckFirstDay() { 
		float[] f = new float[20];
		for ( int i=0; i<20; i++ )
			f[i] = 1;
		
		CalendarDataParser parser = new CalendarDataParser(f, 2006, 3, 6);
		boolean seen_week = false;
		int day_count = 0;
		while ( parser.hasNextDay() ) { 
			parser.nextDay(); day_count++;
			if ( parser.hasWeekData() ) {
				if ( !seen_week ) { 
					assertFalse("registers the first week", parser.isFirstWeek());
					assertEquals("incorrect parsed days", 7, day_count);
					seen_week = true;
				} else {
					assertFalse("registers a first week", parser.isFirstWeek());
				}
			}
		}
	}
	
	public void testFirstMonthCheck() { 
		float[] f = new float[120];
		for ( int i=0; i<120; i++ )
			f[i] = 1;
		
		CalendarDataParser parser = new CalendarDataParser(f, 2006, 2, 27);
		boolean seen_month = false;
		int day_count = 0;
		while ( parser.hasNextDay() ) { 
			parser.nextDay(); day_count++;
			if ( parser.hasMonthData() ) {
				if ( !seen_month ) { 
					assertTrue("does not rgister the first month", parser.isFirstMonth());
					assertEquals("incorrect parsed days", 3, day_count);
					seen_month = true;
				} else {
					assertFalse("still registers a first month", parser.isFirstMonth());
				}
			}
		}
	}
	
	public void testFirstMonthCheckFirstDay() { 
		float[] f = new float[120];
		for ( int i=0; i<120; i++ )
			f[i] = 1;
		
		CalendarDataParser parser = new CalendarDataParser(f, 2006, 3, 1);
		boolean seen_month = false;
		int day_count = 0;
		while ( parser.hasNextDay() ) { 
			parser.nextDay(); day_count++;
			if ( parser.hasMonthData() ) {
				if ( !seen_month ) { 
					assertFalse("registers the first month", parser.isFirstMonth());
					assertEquals("incorrect parsed days", 31, day_count);
					seen_month = true;
				} else {
					assertFalse("still registers a first month", parser.isFirstMonth());
				}
			}
		}
	}
	
	public void testEmptyToString() {
		CalendarDataParser parser = new CalendarDataParser(new float[0], 2006, 4, 1);
		assertNotNull("null toString() on an empty parser", parser.toString());
	}
	
	public void testToString() {
		float[] f = new float[120];
		for ( int i=0; i<120; i++ )
			f[i] = 1;

		CalendarDataParser parser = new CalendarDataParser(f, 2006, 3, 1);
		assertNotNull("null toString() on parser", parser.toString());
		while (parser.hasNextDay()) {
			parser.nextDay();
		}

		assertNotNull("null toString() on an completed parser", parser.toString());
	}
	
	public void testNonExistantAverage() { 
		float[] data = new float[29];
		for ( int i=0; i<28; i++ ) { 
			data[i] = 2;
		}
		
		CalendarDataParser parser = new CalendarDataParser(data, 2007,2,1);
		
		assertEquals("invalid average", 2f, parser.nextMonthAverage());
	}
}
