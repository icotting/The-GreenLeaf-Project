/* Created On: Sep 7, 2005 */
package edu.unl.act.rma.firm.test.core;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataClass;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.InvalidStateException;
import edu.unl.act.rma.firm.core.YearDataBuilder;

/**
 * @author Ian Cottingham
 *
 */
public class PeriodOrderedDataBuilderTest extends TestCase {

	private static final DateTime start = new DateTime(1978, 11, 22, 0, 0, 0, 0, GregorianChronology.getInstance()); 
	private static final DateTime end = new DateTime(1982, 8, 4, 0, 0, 0, 0, GregorianChronology.getInstance());
	
	static { 
		System.setProperty("edu.unl.firm.consoleLogger", "true"); // force any log statements to the console
	}
	
	public void testDailyPopulation() throws InvalidStateException, InvalidArgumentException { 
		YearDataBuilder daily = new YearDataBuilder(start, end, CalendarPeriod.DAILY, DataType.UNKNOWN);
		daily.openStation("test_station");
		
		assertEquals("expects the correct number of data points", 1355, daily.getExpectedValues());
	}
	
	public void testWeeklyPeriod() throws InvalidStateException, InvalidArgumentException { 
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		weekly.openStation("test_station");
		
		assertEquals("expects the correct number of data points", 193, weekly.getExpectedValues());
	}
	
	public void testMonthlyPeriod() throws InvalidStateException, InvalidArgumentException { 
		YearDataBuilder monthly = new YearDataBuilder(start, end, CalendarPeriod.MONTHLY, DataType.UNKNOWN);
		monthly.openStation("test_station");
		
		assertEquals("expects the correct number of data points", 46, monthly.getExpectedValues());
	}
	
	public void testWeeklyFor53WeekYear() throws InvalidStateException, InvalidArgumentException { 
		DateTime my_start = new DateTime(1959, 11, 22, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime my_end = new DateTime(1995, 8, 4, 0, 0, 0, 0, GregorianChronology.getInstance());		
		YearDataBuilder weekly = new YearDataBuilder(my_start, my_end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		weekly.openStation("test_station");
		
		assertEquals("expects the correct number of data points", 1857, weekly.getExpectedValues());
	}
	
	public void testDataPopulation() throws InvalidStateException, InvalidArgumentException { 
		YearDataBuilder daily = new YearDataBuilder(start, end, CalendarPeriod.DAILY, DataType.UNKNOWN);
		daily.openStation("test station");
		
		for ( int i=0; i<1355; i++ ) {
			daily.add(i);
		}
		
		daily.writeStation();
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		weekly.openStation("test station");
		
		for ( int i=0; i<193; i++ ) {
			weekly.add(i);
		}
		
		weekly.writeStation();
		
		YearDataBuilder monthly = new YearDataBuilder(start, end, CalendarPeriod.MONTHLY, DataType.UNKNOWN);
		monthly.openStation("test station");
		
		for ( int i=0; i<46; i++ ) {
			monthly.add(i);
		}
		
		monthly.writeStation();
	}
	
	public void testPartialWrite() throws InvalidArgumentException, InvalidStateException {
		YearDataBuilder monthly = new YearDataBuilder(start, end, CalendarPeriod.MONTHLY, DataType.UNKNOWN);
		monthly.openStation("test station");
		
		for ( int i=0; i<30; i++ ) {
			monthly.add(i);
		}
		
		boolean exc = false;
		monthly.writeStation();
		
		CalendarDataCollection cdc = monthly.returnCollection();
		outter:
		for ( String str : cdc ) { 
			for ( float[] f : cdc.getStationData(str) ) { 
				for ( int i=0; i<f.length; i++ ) { 
					if ( (f[i] != DataType.ERROR_RESULT) && (f[i] != DataType.OUTSIDE_OF_RANGE) && f[i] != DataType.OUTSIDE_OF_REQUEST_RANGE ) { 
						exc = true;
						break outter;
					}
				}
			}
		}
		
		assertFalse("did not allow partial station to be written", exc);
	}
	
	public void testForNoAdditions() throws InvalidArgumentException, InvalidStateException {
		YearDataBuilder monthly = new YearDataBuilder(new DateTime(1978, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance()), new DateTime(1978, 12,31, 0, 0, 0, 0, GregorianChronology.getInstance()), 
				CalendarPeriod.MONTHLY, DataType.UNKNOWN);
		
		monthly.openStation("test station");
		assertEquals("expects a full year of values", 12, monthly.getExpectedValues());
		for ( int i=0; i<12; i++ ) 
			monthly.add(i);
		
		monthly.writeStation();
		
		monthly = new YearDataBuilder(new DateTime(1978, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance()), new DateTime(1979, 12,31, 0, 0, 0, 0, GregorianChronology.getInstance()), 
				CalendarPeriod.MONTHLY, DataType.UNKNOWN);
		
		monthly.openStation("test station");
		assertEquals("expects a full year of values", 24, monthly.getExpectedValues());
		
		monthly = new YearDataBuilder(new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance()), new DateTime(2005, 12,31, 0, 0, 0, 0, GregorianChronology.getInstance()), 
				CalendarPeriod.MONTHLY, DataType.UNKNOWN);
		
		monthly.openStation("test station");
		assertEquals("expects a full year of values", 72, monthly.getExpectedValues());
	}
	
	public void testAnnualValuePopulation() throws InvalidArgumentException, InvalidStateException { 
		YearDataBuilder yearly = new YearDataBuilder(start, end, CalendarPeriod.ANNUALLY, DataType.UNKNOWN);
		yearly.openStation("test_station");
		
		assertEquals("expects the correct number of data points", 5, yearly.getExpectedValues());
	}
	
	public void testendsOn53Week() throws InvalidArgumentException, InvalidStateException {
		DateTime start = new DateTime(2000, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2004, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		weekly.openStation("test");
		
		assertEquals("expects the correct number of weeks", 209, weekly.getExpectedValues());
		for ( int i=0; i<209; i++ ) { 
			weekly.add(i);
		}
		weekly.writeStation();
	}
	
	public void testEnd53() throws InvalidArgumentException, InvalidStateException {
		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		System.out.println(start.getWeekOfWeekyear());
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		weekly.openStation("test");
		
		assertEquals("expects the correct number of weeks", 313, weekly.getExpectedValues());
		for ( int i=0; i<312; i++) { 
			weekly.add(i);
		}
		weekly.writeStation();
	}
	
	public void teststartsOn53Week() throws InvalidArgumentException, InvalidStateException {
		DateTime start = new DateTime(2004, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2008, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		weekly.openStation("test");
		
		assertEquals("expects the correct number of weeks", 209, weekly.getExpectedValues());
		for ( int i=0; i<209; i++) { 
			weekly.add(i);
		}
		weekly.writeStation();
	}
	
	public void testcheckCentury() throws InvalidArgumentException, InvalidStateException {
		DateTime start = new DateTime(1900, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());

		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		weekly.openStation("test");
		
		assertEquals("expects the correct number of weeks", 5512, weekly.getExpectedValues());
		for ( int i=0; i<5461; i++) { 
			weekly.add(i);
		}
		weekly.writeStation();
	}
	
	public void testStart53End53() throws InvalidArgumentException, InvalidStateException {
		DateTime start = new DateTime(1937, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		weekly.openStation("test");
		
		assertEquals("expects the correct number of weeks", 3589, weekly.getExpectedValues());
		for ( int i=0; i<3588; i++) { 
			weekly.add(i);
		}
		weekly.writeStation();
	}
		
	public void testDailyAcrossBrokenYears() throws InvalidArgumentException, InvalidStateException {
		DateTime start = new DateTime(2003, 12, 21, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 1, 15, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.DAILY, DataType.UNKNOWN);
		assertEquals("correct number of expected values", 392, weekly.getExpectedValues());
		
		weekly.openStation("test");

		for ( int i=0; i<392; i++) { 
			weekly.add(i);
		}
		weekly.writeStation();
	}
	
	public void testWeeklyAcrossBrokenYears() throws InvalidArgumentException, InvalidStateException {
		DateTime start = new DateTime(2003, 12, 21, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 1, 15, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		assertEquals("correct number of expected values", 56, weekly.getExpectedValues());
		
		weekly.openStation("test");

		for ( int i=0; i<56; i++) { 
			weekly.add(i);
		}
		weekly.writeStation();
	}
	
	public void testMonthly() throws InvalidArgumentException, InvalidStateException {
		DateTime start = new DateTime(2003, 12, 21, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 1, 15, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.MONTHLY, DataType.UNKNOWN);
		assertEquals("correct number of expected values", 14, weekly.getExpectedValues());
		
		weekly.openStation("test");

		for ( int i=0; i<14; i++) { 
			weekly.add(i);
		}
		weekly.writeStation();
	}
	
	public void testMultiyearWeeklyStarts53() throws InvalidArgumentException, InvalidStateException {
		DateTime start = new DateTime(1892, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		
		weekly.openStation("test");

		for ( int i=0; i<5928; i++) { 
			weekly.add(i);
		}
		weekly.writeStation();
	}
	
	public void testResultToString() {
		DateTime start = new DateTime(1892, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		assertNotNull("null toString() on YearDataBuilder result", weekly.returnCollection().toString());
	}
	
	public void testAddOverExpected() throws Exception { 
		DateTime start = new DateTime(2005,1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime end = start.plusYears(1);
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		weekly.openStation("my_station");
		for ( int i=0; i<100; i++ ) { 
			weekly.add(i);
		}
		weekly.writeStation();
	}
	
	public void testFixedPoint() throws Exception { 
		DateTime start = new DateTime(2000,1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime end = start.plusYears(6);
		
		DateTime check_point_one = start.plusWeeks(4);
		float val_one = 10;
		DateTime check_point_two = start.plusWeeks(20);
		float val_two = 22;
		DateTime check_point_three = start.plusWeeks(67);
		float val_three = 35;
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		weekly.openStation("my_station");
		int len = weekly.getExpectedValues();
		
		DateTime pos = start;
		for ( int i=0; i<len; i++ ) {
			if ( pos.isEqual(check_point_one) ) { 
				weekly.add(val_one);
			} else if ( pos.isEqual(check_point_two) ) { 
				weekly.add(val_two);
			} else if ( pos.isEqual(check_point_three) ) { 
				weekly.add(val_three);
			} else { 
				weekly.add(0);
			}
			pos = pos.plusWeeks(1);
		}
		weekly.writeStation();
		
		CalendarDataCollection col = weekly.returnCollection();
		
		col.setFixedPoint(check_point_one);
		assertEquals("incorrect value", val_one, col.getFixedDateStationData("my_station"));
		
		col.setFixedPoint(check_point_two);
		assertEquals("incorrect value", val_two, col.getFixedDateStationData("my_station"));
		
		col.setFixedPoint(check_point_three);
		assertEquals("incorrect value", val_three, col.getFixedDateStationData("my_station"));
	}
	
	public void testFixedPointNoDate() throws Exception { 
		DateTime start = new DateTime(2000,1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime end = start.plusYears(6);
		Period period = new Period(start, end, PeriodType.weeks());
				
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		weekly.openStation("my_station");
		int len = period.getWeeks();
		
		for ( int i=0; i<len; i++ ) {
			weekly.add(i);		
		}
		weekly.writeStation();
		
		CalendarDataCollection col = weekly.returnCollection();
		try {
			assertEquals("incorrect value", 20, col.getFixedDateStationData("my_station"));
			fail("allowed call");
		} catch ( Exception e ) { 
			// this should happen
		}
	}
	
	public void testAverage() throws Exception { 
		DateTime start = new DateTime(2000,1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime end = start.plusYears(1).minusDays(1);
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN, true, true);
		for ( int i=0; i<10; i++ ) { 
			weekly.openStation(i+" values");
			for (int j=0; j<weekly.getExpectedValues(); j++ ) {
				weekly.add(i);
			}
			weekly.writeStation();
		}
		
		CalendarDataCollection data_col = weekly.returnCollection();
		CalendarDataCollection averages = data_col.collectionAverage();
		
		for ( String str : averages ) {
			DateTime pos = start;
			while ( pos.isBefore(end) ) {
				float val = averages.getStationDataForDate(str, pos);
				assertEquals("invalid value match", 4.5f, val);
				pos = pos.plusWeeks(1);
			}
		}
	}
	
	public void testAverageEndExclude() throws Exception { 
		DateTime start = new DateTime(2000,1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime end = start.plusYears(1).minusDays(1);
		
		YearDataBuilder monthly = new YearDataBuilder(start, end, CalendarPeriod.MONTHLY, DataType.UNKNOWN, true, false);
		for ( int i=0; i<10; i++ ) { 
			monthly.openStation(i+" values");
			for (int j=0; j<(11); j++ ) {
				monthly.add(i);
			}
			monthly.writeStation();
		}
		
		CalendarDataCollection data_col = monthly.returnCollection();
		CalendarDataCollection averages = data_col.collectionAverage();
		
		for ( String str : averages ) { 
			for ( float[] data : averages.getStationData(str) ) { 
				for ( float val : data ) { 
					assertEquals("incorrect value", 4.5f, val);
				}
			}
		}
	}
	
	public void testAveragePartial() throws Exception { 
		DateTime start = new DateTime(2000,8,22,0,0,0,0,GregorianChronology.getInstance());
		DateTime end = start.plusWeeks(16);
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN, true, true);
		for ( int i=0; i<10; i++ ) { 
			weekly.openStation(i+" values");
			for (int j=0; j<(17); j++ ) {
				weekly.add(i);
			}
			weekly.writeStation();
		}
		
		CalendarDataCollection data_col = weekly.returnCollection();
		CalendarDataCollection averages = data_col.collectionAverage();
		
		for ( String str : averages ) { 
			for ( float[] data : averages.getStationData(str) ) { 
				DateTime pos = new DateTime(2000,1,1,0,0,0,0,GregorianChronology.getInstance());
				for ( float val : data ) { 
					if ( pos.isAfter(start) && pos.isBefore(end) ) {
						assertEquals("incorrect value", 4.5f, val);
					}
					pos = pos.plusWeeks(1);
				}
			}
		}
	}
	
	public void testSum() throws Exception { 
		DateTime start = new DateTime(2000,1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime end = start.plusYears(1).minusDays(1);
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.MONTHLY, DataType.UNKNOWN, true, true);
		for ( int i=0; i<10; i++ ) { 
			weekly.openStation(i+" values");
			for (int j=0; j<(12); j++ ) {
				weekly.add(i);
			}
			weekly.writeStation();
		}
		
		CalendarDataCollection data_col = weekly.returnCollection();
		CalendarDataCollection averages = data_col.collectionSum();
		
		for ( String str : averages ) { 
			for ( float[] data : averages.getStationData(str) ) { 
				for ( float val : data ) { 
					assertEquals("incorrect value", 45f, val);
				}
			}
		}
	}
	
	public void testSumEndExclude() throws Exception { 
		DateTime start = new DateTime(2000,1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime end = start.plusYears(1).minusDays(1);
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.MONTHLY, DataType.UNKNOWN, true, false);
		for ( int i=0; i<10; i++ ) { 
			weekly.openStation(i+" values");
			for (int j=0; j<(11); j++ ) {
				weekly.add(i);
			}
			weekly.writeStation();
		}
		
		CalendarDataCollection data_col = weekly.returnCollection();
		CalendarDataCollection averages = data_col.collectionSum();
		
		for ( String str : averages ) { 
			for ( float[] data : averages.getStationData(str) ) { 
				for ( float val : data ) { 
					assertEquals("incorrect value", 45f, val);
				}
			}
		}
	}
	
	public void testSumPartial() throws Exception { 
		DateTime start = new DateTime(2000,1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime end = start.plusWeeks(16);
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.MONTHLY, DataType.UNKNOWN, true, true);
		for ( int i=0; i<10; i++ ) { 
			weekly.openStation(i+" values");
			for (int j=0; j<(4); j++ ) {
				weekly.add(i);
			}
			weekly.writeStation();
		}
		
		CalendarDataCollection data_col = weekly.returnCollection();
		CalendarDataCollection averages = data_col.collectionSum();
		
		for ( String str : averages ) { 
			for ( float[] data : averages.getStationData(str) ) { 
				DateTime pos = new DateTime(2000,1,3,0,0,0,0,GregorianChronology.getInstance());
				for ( float val : data ) { 
					if ( pos.isAfter(start) && pos.isBefore(end) ) {
						assertEquals("incorrect value", 45f, val);
					}
					pos = pos.plusMonths(1);
				}
			}
		}
	}
	
	public void testFixedPointInvalidDate() throws Exception { 
		DateTime start = new DateTime(2000,1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime end = start.plusYears(6);
		Period period = new Period(start, end, PeriodType.weeks());
		DateTime fixed_point_past = new DateTime(1980,1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime fixed_point_future = new DateTime(2010,1,1,0,0,0,0,GregorianChronology.getInstance());
		 
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		weekly.openStation("my_station");
		int len = period.getWeeks();
		
		for ( int i=0; i<len; i++ ) {
			weekly.add(1);		
		}
		weekly.writeStation();
		
		CalendarDataCollection col = weekly.returnCollection();
		try {
			col.setFixedPoint(fixed_point_past);
			fail("allowed call");
		} catch ( Exception e ) { 
			// this should happen
		}
		
		try {
			col.setFixedPoint(fixed_point_future);
			fail("allowed call");
		} catch ( Exception e ) { 
			// this should happen
		}
	}
	
	public void testFilter() throws InvalidArgumentException, InvalidStateException {
		DateTime start = new DateTime(1892, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		YearDataBuilder weekly = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		
		weekly.openStation("test");

		for ( int i=0; i<weekly.getExpectedValues(); i++) { 
			weekly.add(i);
		}
		weekly.writeStation();
		
		CalendarDataCollection cdc = weekly.returnCollection();
		cdc.attachClass(new DataClass() {

			public float classifyValue(float value) {
				if ( value == DataType.MISSING || value == DataType.ERROR_RESULT || value == DataType.NONEXISTANT 
						|| value == DataType.OUTSIDE_OF_RANGE || value == DataType.OUTSIDE_OF_REQUEST_RANGE ) { 
					return -1;
				}
				
				if ( value < 1000 ) { 
					return 1;
				} else if ( value < 2000 ) { 
					return 2;
				} else if ( value < 3000 ) { 
					return 3;
				} else if ( value < 4000 ) { 
					return 4;
				} else { 
					return 5;
				}
			}
		
		});
		
		int one = 0;
		int two = 0; 
		int three = 0; 
		int four = 0; 
		int five = 0;
		
		for ( float[] f : cdc.getStationData("test") ) {
			for ( float value : f ) { 
				if ( value == -1 ) { 
					// do nothing
				} else if ( value == 1 ) { 
					one++;
				} else if ( value == 2 ) { 
					two++;
				} else if ( value == 3 ) { 
					three++;
				} else if ( value == 4 ) { 
					four++;
				} else { 
					five++;
				}
			}
		}
		
		assertEquals("invalid count", 1000, one);
		assertEquals("invalid count", 1000, two);
		assertEquals("invalid count", 1000, three);
		assertEquals("invalid count", 1000, four);
		assertEquals("invalid count", 1929, five);
	}
	
	public void testPartialDateFunctions() throws Exception { 
		float[] data_array = {1.3f, 4f, 2.4f, 8.5f, 4.6f };
		
		DateTime end = new DateTime(2004,1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime start = end.minusWeeks(4);
		
		YearDataBuilder builder = new YearDataBuilder(start, end, CalendarPeriod.WEEKLY, DataType.UNKNOWN);
		builder.openStation("TestStation");
		for ( float f : data_array ) { 
			builder.add(f);
		}
		builder.writeStation();
		
		CalendarDataCollection col = builder.returnCollection();
		
		DateTime pos = start;
		int i = 0;
		while ( pos.isBefore(end.plusWeeks(1)) ) {
			float val = col.getStationDataForDate("TestStation", pos);
			assertEquals("invalid value match", data_array[i], val);
			i++; pos = pos.plusWeeks(1);
		}
	}
}