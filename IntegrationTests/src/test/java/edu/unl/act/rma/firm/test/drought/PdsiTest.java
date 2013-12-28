/* Created On: Sep 7, 2005 */
package edu.unl.act.rma.firm.test.drought;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.climate.VariableFilter;
import edu.unl.act.rma.firm.climate.component.ClimateSpatialExtension;
import edu.unl.act.rma.firm.climate.configuration.ClimateServiceAccessor;
import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.TemporalPeriod;
import edu.unl.act.rma.firm.core.component.SpatialQuery;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.drought.PdsiType;
import edu.unl.act.rma.firm.drought.PdsiWeeklyStep;
import edu.unl.act.rma.firm.drought.component.DroughtIndexQuery;
import edu.unl.act.rma.firm.drought.configuration.DroughtServiceAccessor;
import edu.unl.act.rma.firm.drought.index.PalmerDroughtSeverityIndex;
import edu.unl.act.rma.firm.test.StationList;


/**
 * @author jdokulil
 *
 */
public class PdsiTest extends TestCase {
	private DroughtIndexQuery query;	
	public static final String STATION_ID = StationList.AGATE.getStationID();
	public static final String ASHLAND_ID = StationList.ASHLAND_ID.getStationID();
	public static final String EAST_WALLINGFORD = StationList.EAST_WALLINGFORD.getStationID();
	public static final String AKRON = StationList.AKRON.getStationID();

	// variables used in testThreadedSpiCalls
	private int threadCount = 3;
	private boolean threadedFailure;
	
	@Override
	protected void setUp() throws Exception {
		this.query = DroughtServiceAccessor.getInstance()
							.getDroughtIndexQuery();
	}
	

	public void testVerifyFullResults() throws Exception {
		DateTime ending = new DateTime(2006, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		int starting = 2006;
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		List<String> stations = sq.getStationsForState(USState.Alabama);
		CalendarDataCollection cdc1 = query.computeScPdsi(stations, PdsiType.PDSI, 1, CalendarPeriod.MONTHLY, ending, starting);
		CalendarDataCollection cdc2 = query.computeScPdsi(stations, PdsiType.PDSI, 1, CalendarPeriod.MONTHLY, ending, starting);
		for (String station : cdc1) {
			float[][] results1 = cdc1.getDataMatrix(station);
			float[][] results2 = cdc2.getDataMatrix(station);
			
			assertEquals("unequal number of results for the same station: " + station, results1.length, results2.length);
			for (int i=0; i<results1.length; i++) {
				for (int j=0; j<CalendarPeriod.MONTHLY.getLength(); j++) {
					assertEquals("invalid result", results1[i][j], results2[i][j]);
				}
			}
		}
	}
	
	public void testEmptyStationList() throws Exception {
		int starting = 1893;
		DateTime ending = new DateTime(2006, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		ArrayList<String> stations = new ArrayList<String>();

		CalendarDataCollection results = query.computeScPdsi(stations, PdsiType.PDSI, 1, CalendarPeriod.MONTHLY, ending, starting);
		assertNotNull("returned null results", results);
		for (String station : results) {
			assertNotNull("null data", results.getDataMatrix(station));
		}
	}
	
	public void testBadDates() throws Exception {
		int starting = 2006;
		DateTime ending = new DateTime(2000, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_ID);

		try {
			CalendarDataCollection results = this.query.computeScPdsi(stations, PdsiType.PDSI, 1, CalendarPeriod.MONTHLY, ending, starting);
			fail("PDSI should have failed when given invalid start/end dates");
		} catch (InvalidArgumentException se) {
			// test case passed
		} catch ( Exception e ) { 
			throw e;
		}
	}
	

	public void testBadDatesWithEmptyStationsList() throws Exception {
		int starting = 2006;
		DateTime ending = new DateTime(2000, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		ArrayList<String> stations = new ArrayList<String>();

		try {
			CalendarDataCollection results = query.computeScPdsi(stations, PdsiType.PDSI, 1, CalendarPeriod.MONTHLY, ending, starting);
			fail("PDSI should have failed when given invalid start/end dates");
		} catch (InvalidArgumentException se) {
			// test case passed
		} catch ( Exception e ) { 
			throw e;
		}
	}

	public void testVermont() throws Exception {
		int starting = 1948;
		DateTime ending = new DateTime(2006, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());

		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		List<String> stations = sq.getStationsForState(USState.Vermont);
		
		CalendarDataCollection results =  query.computeScPdsi(stations, PdsiType.PDSI, 1, CalendarPeriod.MONTHLY, ending, starting);
		assertNotNull("returned null results", results);
	}
	
	public void testEastWallingford() throws Exception {
		int starting = 1948;
		DateTime ending = new DateTime(2006, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());

		ArrayList<String> stations = new ArrayList<String>();
		stations.add(EAST_WALLINGFORD);

		CalendarDataCollection results =  query.computeScPdsi(stations, PdsiType.PDSI, 1, CalendarPeriod.MONTHLY, ending, starting);
		assertNotNull("returned null results", results);
	}
	
	public void testMultiyearContinuousWeeklySPIending2006() throws Exception {
		DateTime ending = new DateTime(2006, 8, 5, 0, 0, 0, 0);
		int starting = 2005;
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);

		// get data
		CalendarDataCollection results = query.computeScPdsi(stations, PdsiType.PDSI, 1, CalendarPeriod.WEEKLY, ending, starting);
		float[][] data = results.getDataMatrix(ASHLAND_ID);
		assertEquals("not enough data returned", 2, data.length);
		assertNotNull("first year of results is null", data[0]);
		assertNotNull("second year of results is null", data[1]);
	}
	
	
	public void testOneWeekStepPDSI() throws Exception {
		int values = CalendarPeriod.WEEKLY.getLength();
		DateTime ending = new DateTime(2001, 1, 1, 0, 0, 0, 0);
		int starting=2001;
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_ID);
		
		CalendarDataCollection results =  query.computeScPdsi(stations, PdsiType.PDSI, 1, CalendarPeriod.WEEKLY, ending, starting);
		float[][] data = results.getDataMatrix(STATION_ID);
		
		// validate pdsi results
		assertEquals("incorrect number of PDSI results", 1, data.length);
		assertEquals("incorrect number of PDSI results", values, data[0].length);
	}
	
	
	public void testTwoWeekStepPDSI() throws Exception {
		PdsiWeeklyStep step = PdsiWeeklyStep.TWO_WEEK;
		int values = CalendarPeriod.WEEKLY.getLength();
		DateTime ending = new DateTime(2000, 1, 1, 0, 0, 0, 0);
		int starting=2000;
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_ID);
		
		CalendarDataCollection results = query.computeScPdsi(stations, PdsiType.PDSI, step.getStep(), CalendarPeriod.WEEKLY, ending, starting);
		float[][] data = results.getDataMatrix(STATION_ID);
		
		// validate pdsi results
		assertEquals("incorrect number of PDSI results", 1, data.length);
		assertEquals("incorrect number of PDSI results", values, data[0].length);
		int bad_values = 0;
		for (int i=0; i<values; i++) {
			if (data[0][i] == DataType.MISSING) {
				bad_values++;
			}
		}
		assertEquals("incorrect number of -99 values", 50, bad_values);
	}
	
	public void testFourWeekStepPDSI() throws Exception {
		PdsiWeeklyStep step = PdsiWeeklyStep.FOUR_WEEK;
		int values = CalendarPeriod.WEEKLY.getLength();
		DateTime ending = new DateTime(2000, 1, 1, 0, 0, 0, 0);
		int starting=2000;
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_ID);
		
		CalendarDataCollection results = query.computeScPdsi(stations, PdsiType.PDSI, step.getStep(), CalendarPeriod.WEEKLY, ending, starting);
		float[][] data = results.getDataMatrix(STATION_ID);
		
		// validate pdsi results
		assertEquals("incorrect number of PDSI results", 1, data.length);
		assertEquals("incorrect number of PDSI results", values, data[0].length);
		int bad_values = 0;
		for (int i=0; i<values; i++) {
			if (data[0][i] == DataType.MISSING) {
				bad_values++;
			}
		}
		assertEquals("incorrect number of -99 values", 48, bad_values);
	}
	
	
	public void testThirteenWeekStepPDSI() throws Exception {
		PdsiWeeklyStep step = PdsiWeeklyStep.THIRTEEN_WEEK;
		int values = CalendarPeriod.WEEKLY.getLength();
		DateTime ending = new DateTime(2000, 1, 1, 0, 0, 0, 0);
		int starting=2000;
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_ID);
		
		CalendarDataCollection results =  query.computeScPdsi(stations, PdsiType.PDSI, step.getStep(), CalendarPeriod.WEEKLY, ending, starting);
		float[][] data = results.getDataMatrix(STATION_ID);
		
		// validate pdsi results
		assertEquals("incorrect number of PDSI results", 1, data.length);
		assertEquals("incorrect number of PDSI results", values, data[0].length);
		int bad_values = 0;
		for (int i=0; i<values; i++) {
			if (data[0][i] == DataType.MISSING) {
				bad_values++;
			}
		}
		assertEquals("incorrect number of -99 values", 52-13, bad_values);
	}
	

	public void testMonthlyPDSI() throws Exception {
		DateTime ending = new DateTime(2000, 1, 1, 0, 0, 0, 0);
		int starting=2000;
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_ID);
		
		CalendarDataCollection results = query.computeScPdsi(stations, PdsiType.PDSI,1, CalendarPeriod.MONTHLY, ending, starting);
		
		// validate pdsi results
		float[][] data = results.getDataMatrix(STATION_ID);
		assertEquals("incorrect number of PDSI results", 1, data.length);
		assertEquals("incorrect PDSI value", 2.736, data[0][0], 0.1);
	}
	
	
	public void testMultiyearWeeklyPDSI() throws Exception {
		int fromYear = 2000;
		DateTime end = new DateTime(2004, 2, 7, 0, 0, 0, 0);
		
		// get data
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_ID);
		
		CalendarDataCollection results = query.computeScPdsi(stations, PdsiType.PDSI,1, CalendarPeriod.WEEKLY, end, fromYear);
		
		float[][] data = results.getDataMatrix(STATION_ID);
		// validate pdsi results
		assertEquals("incorrect number of years of PDSI results", 5, data.length);
		for (int i=0; i<5; i++) {
			assertEquals("incorrect number of years of PDSI results", 52, data[i].length);
		}
		assertEquals("incorrect first PDSI value", 3.262, data[0][5], 0.1);
		assertEquals("incorrect middle PDSI value", -1.50, data[2][5], 0.1);
		assertEquals("incorrect last PDSI value", -1.0690, data[4][5], 0.1);
	}
	
	
	public void testMultiyearMonthlyPDSI() throws Exception {
		int fromYear = 2000;
		DateTime end = new DateTime(2004, 12, 31, 0, 0, 0, 0);
		
		// get data
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_ID);
		
		CalendarDataCollection results = query.computeScPdsi(stations, PdsiType.PDSI,1, CalendarPeriod.MONTHLY, end, fromYear);
		
		// validate pdsi results
		float[][] data = results.getDataMatrix(STATION_ID);
		assertEquals("incorrect number of years of PDSI results", 5, data.length);
		for (int i=0; i<data.length; i++) {
			assertEquals("incorrect number of periods of PDSI data at period: " + i, 12, data[i].length);
		}
		assertEquals("incorrect first PDSI value", -0.12, data[0][11], 0.1);
		assertEquals("incorrect middle PDSI value", -3.148, data[2][11], 0.1);
		assertEquals("incorrect last PDSI value", 1.25, data[4][11], 0.1);
	}
	
	
	public void testContinuousMultiyearWeeklyPDSI() throws Exception {
		int fromYear = 2000;
		DateTime end = new DateTime(2004, 12, 31, 0, 0, 0, 0);
		
		// get data
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_ID);
		
		CalendarDataCollection results = query.computeScPdsi(stations, PdsiType.PDSI,1, CalendarPeriod.WEEKLY, end, fromYear);
		float[][] data = results.getDataMatrix(STATION_ID);
		// validate pdsi results
		int len = CalendarPeriod.WEEKLY.getLength();
		assertEquals("incorrect number of years of PDSI results", 5, data.length);
		for (int i=0; i<5; i++) {
			assertEquals("incorrect number of years of PDSI results", len, data[i].length);
		}
		assertEquals("incorrect first PDSI value", 4.098, data[0][51], 0.1);
		assertEquals("incorrect middle PDSI value", -1.38, data[2][0], 0.1);
		assertEquals("incorrect middle PDSI value", -3.62, data[2][26], 0.1);
		assertEquals("incorrect middle PDSI value", 0.4219, data[2][51], 0.1);
		assertEquals("incorrect last PDSI value", 1.68, data[4][50], 0.1);
	}
	

	public void testContinuousMultiyearMonthlyPDSI() throws Exception {
		int fromYear = 2000;
		DateTime end = new DateTime(2004, 12, 31, 0, 0, 0, 0);
		
		// get data
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_ID);
		
		CalendarDataCollection results = query.computeScPdsi(stations, PdsiType.PDSI,1, CalendarPeriod.MONTHLY, end, fromYear);
		
		// validate pdsi results
		int len = CalendarPeriod.MONTHLY.getLength();
		float[][] data = results.getDataMatrix(STATION_ID);
		assertEquals("incorrect number of years of PDSI results", 5, data.length);
		for (int i=0; i<data.length; i++) {
			assertEquals("incorrect number of periods of PDSI data at period: " + i, len, data[i].length);
		}
		assertEquals("incorrect first PDSI value", -0.12, data[0][11], 0.1);
		assertEquals("incorrect middle PDSI value", -3.50, data[2][5], 0.1);
		assertEquals("incorrect last PDSI value", 1.25, data[4][11], 0.1);
	}
	

	public void testAkron() throws Exception {
		PdsiWeeklyStep step = PdsiWeeklyStep.FOUR_WEEK;
		int values = CalendarPeriod.WEEKLY.getLength();
		DateTime date = new DateTime(2005, 9, 28, 0, 0, 0, 0);
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(AKRON);
		
		CalendarDataCollection results = query.computeScPdsi(stations, PdsiType.PDSI,step.getStep(), CalendarPeriod.WEEKLY, date, date.getYear());
		float[][] data = results.getDataMatrix(AKRON);
		
		// validate pdsi results
		assertEquals("incorrect number of PDSI results", 1, data.length);
		assertEquals("incorrect number of PDSI results", values, data[0].length);
		int bad_values = 0;
		for (int i=0; i<values; i++) {
			if ((data[0][i] == DataType.MISSING) ||(data[0][i] == DataType.OUTSIDE_OF_REQUEST_RANGE)){
				bad_values++;
			}
		}
		assertTrue("incorrect number of -99 values: " + bad_values, bad_values >= (step.getValuesPerYear() * (step.getStep() - 1)));
	}
		
	public void testAllOfColorado() throws Exception {
		DateTime date = new DateTime(2005, 9, 28, 0, 0, 0, 0);
		
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		List<String> stations = sq.getStationsForState(USState.Colorado);
		// get data
		long start = System.nanoTime();
		CalendarDataCollection results = query.computeScPdsi(stations, PdsiType.PDSI,PdsiWeeklyStep.FOUR_WEEK.getStep(), CalendarPeriod.WEEKLY, date, date.getYear());
		long end = System.nanoTime();
		
		System.out.println("All of Colorado took " + (end - start) + " nanoseconds");
		for (Iterator<String> i = results.iterator(); i.hasNext();) {
			String station = i.next();

			float[][] data = results.getDataMatrix(station);
			assertEquals("incorrect number of SPI results", 1, data.length);
		}
	}
	
	public void testAllOfColoradoContMultiyearMonthly() throws Exception {
		int fromYear = 2000;
		DateTime ending = new DateTime(2004, 12, 31, 0, 0, 0, 0);
		
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		List<String> stations = sq.getStationsForState(USState.Colorado);
		
		// get data
		long start = System.nanoTime();
		CalendarDataCollection results = query.computeScPdsi(stations, PdsiType.PDSI,1, CalendarPeriod.MONTHLY, ending, fromYear);
		long end = System.nanoTime();
		
		System.out.println("All of Colorado took " + (end - start) + " nanoseconds");
		for (Iterator<String> i = results.iterator(); i.hasNext();) {
			String station = i.next();

			float[][] data = results.getDataMatrix(station);
			assertEquals("incorrect number of SPI results", 5, data.length);
		}
	}

	public void testAllOfNebraska() throws Exception {
		DateTime date = new DateTime(2005, 8, 5, 0, 0, 0, 0);
		
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		List<String> stations = sq.getStationsForState(USState.Nebraska);
		// get data
		long start = System.nanoTime();
		CalendarDataCollection results = query.computeScPdsi(stations, PdsiType.PDSI,PdsiWeeklyStep.ONE_WEEK.getStep(), CalendarPeriod.WEEKLY, date, date.getYear());
		long end = System.nanoTime();
		
		System.out.println("All of Nebraska took " + (end - start) + " nanoseconds");
		for (Iterator<String> i = results.iterator(); i.hasNext();) {
			String station = i.next();

			float[][] data = results.getDataMatrix(station);
			assertEquals("incorrect number of SPI results", 1, data.length);
		}
	}
	
	public void testThreadedPdsiCalls() throws Exception {
		threadedFailure = false;
		
		Runnable r = new Runnable() {
			public void run() {
				DateTime ending = new DateTime(2002, 12, 31, 0, 0, 0, 0);
				DateTime starting = new DateTime(1998, 1, 1, 0, 0, 0, 0);
				TemporalPeriod period = new TemporalPeriod(starting, ending);
				
				try {
					ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
					List<String> stations = sq.getStationsForState(USState.Nebraska);
					VariableFilter filter = new VariableFilter();
					filter.setValidPeriod(period);
					filter.setVariableType(DataType.PRECIP);
					filter.setMissingTolerance(0.2f);
					ArrayList<VariableFilter> filters = new ArrayList<VariableFilter>();
					filters.add(filter);
					
					List<String> filtered_stations = ClimateServiceAccessor.getInstance().getClimateMetaDataQuery().filterStations(stations, filters, period, 0.2f, true);
					query.computeScPdsi(filtered_stations, PdsiType.PDSI,1, CalendarPeriod.MONTHLY, ending, starting.getYear());
					
				} catch (Throwable t) {
					t.printStackTrace();
					threadedFailure = true;
				} finally {
					threadCount--;
				}
			}
		};
		
		int len = threadCount;
		for (int i=0; i<len; i++) {
			Thread t = new Thread(r);
			t.start();
		}
		
		while (threadCount > 0) {
			if (threadedFailure) {
				fail("Threaded PDSI calls failed");
			}
			Thread.sleep(10000);
		}
	}
	
}