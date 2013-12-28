/* Created On: Sep 7, 2005 */
package edu.unl.act.rma.firm.test.drought;

import java.util.ArrayList;
import java.util.Arrays;
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
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.drought.component.DroughtIndexQuery;
import edu.unl.act.rma.firm.drought.configuration.DroughtServiceAccessor;
import edu.unl.act.rma.firm.test.StationList;

/**
 * @author Ian Cottingham
 * 
 */
public class SpiTest extends TestCase {
	private DroughtIndexQuery query;

	public static final String STATION_1_ID = StationList.ASHLAND_ID
			.getStationID();
	public static final String STATION_2_ID = StationList.AKRON4E
			.getStationID();
	public static final String STATION_3_ID = StationList.AKRON_WASHINGTON
			.getStationID();
	public static final String STATION_4_ID = StationList.DEL_NORTE
			.getStationID();
	public static final String STATION_5_ID = StationList.CALERA.getStationID();
	public static final String STATION_6_ID = StationList.COCHRANE
			.getStationID();

	// variables used in testThreadedSpiCalls
	private int threadCount = 3;
	private boolean threadedFailure;

	@Override
	protected void setUp() throws Exception {
		this.query = DroughtServiceAccessor.getInstance()
				.getDroughtIndexQuery();
	}

	public void testFullRangeAkronWeeklySteps() throws Exception {
		DateTime ending = new DateTime(2007, 12, 31, 0, 0, 0, 0,
				GregorianChronology.getInstance());
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(StationList.AKRON4E.getStationID());

		CalendarDataCollection data = query.computeSpi(stations,
				CalendarPeriod.WEEKLY, 2, ending, -1);

		data = query.computeSpi(stations, CalendarPeriod.WEEKLY, 4, ending, -1);
		data = query.computeSpi(stations, CalendarPeriod.WEEKLY, 8, ending, -1);
		data = query
				.computeSpi(stations, CalendarPeriod.WEEKLY, 12, ending, -1);
		data = query
				.computeSpi(stations, CalendarPeriod.WEEKLY, 26, ending, -1);
		data = query
				.computeSpi(stations, CalendarPeriod.WEEKLY, 52, ending, -1);
	}

	public void testFullRangeAkronMonthlySteps() throws Exception {
		assertTrue("remove this and complete the method", true);
	}

	// bad data station
	public void testPartialRangeCochraneWeeklySteps() throws Exception {
		assertTrue("remove this and complete the method", true);
	}

	public void testPartialRangeCochraneMonthlySteps() throws Exception {
		assertTrue("remove this and complete the method", true);
	}

	public void testEmptyStationList() throws Exception {
		int starting = 1893;
		DateTime ending = new DateTime(2006, 12, 31, 0, 0, 0, 0,
				GregorianChronology.getInstance());
		ArrayList<String> stations = new ArrayList<String>();

		CalendarDataCollection results = this.query.computeSpi(stations,
				CalendarPeriod.MONTHLY, 1, ending, starting);
		assertNotNull("returned null results", results);
		for (String station : results) {
			assertNotNull("null data", results.getDataMatrix(station));
		}
	}

	public void testBadDates() throws Exception {
		DateTime ending = new DateTime(2000, 12, 31, 0, 0, 0, 0,
				GregorianChronology.getInstance());
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_2_ID);

		try {
			this.query.computeSpi(stations, CalendarPeriod.MONTHLY, 5, ending,
					2000);
		} catch (InvalidArgumentException se) {
			// test case passed
		} catch (Exception e) {
			throw e;
		}
	}

	public void testBadDatesWithEmptyStationsList() throws Exception {
		int starting = 2006;
		DateTime ending = new DateTime(2000, 12, 31, 0, 0, 0, 0,
				GregorianChronology.getInstance());
		ArrayList<String> stations = new ArrayList<String>();

		try {
			this.query.computeSpi(stations, CalendarPeriod.WEEKLY, 52, ending,
					starting);
		} catch (InvalidArgumentException se) {
			// test case passed
		} catch (Exception e) {
			throw e;
		}
	}

	public void testMultiyearWeeklySPIending2006_2() throws Exception {
		DateTime date = new DateTime(2006, 8, 5, 0, 0, 0, 0);
		int start = 2004;

		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_1_ID);

		// get data
		CalendarDataCollection results = this.query.computeSpi(stations,
				CalendarPeriod.WEEKLY, 4, date, start);
		float[][] data = results.getDataMatrix(STATION_1_ID);
		assertEquals("not enough data returned", 3, data.length);
		assertNotNull("first year of results is null", data[0]);
		assertNotNull("second year of results is null", data[1]);
		assertNotNull("third year of results is null", data[2]);
	}

	public void testVerifyFullResults() throws Exception {
		DateTime ending = new DateTime(2006, 12, 31, 0, 0, 0, 0,
				GregorianChronology.getInstance());
		int starting = 2006;
		CalendarDataCollection cdc1 = this.query.computeSpi(Arrays
				.asList(new String[] { STATION_6_ID, STATION_5_ID }),
				CalendarPeriod.MONTHLY, 1, ending, starting);
		CalendarDataCollection cdc2 = this.query.computeSpi(Arrays
				.asList(new String[] { STATION_5_ID, STATION_6_ID }),
				CalendarPeriod.MONTHLY, 1, ending, starting);

		for (String station : cdc1) {
			float[][] results1 = cdc1.getDataMatrix(station);
			float[][] results2 = cdc2.getDataMatrix(station);

			assertEquals("unequal number of results for the same station: "
					+ station, results1.length, results2.length);
			for (int i = 0; i < results1.length; i++) {
				for (int j = 0; j < CalendarPeriod.MONTHLY.getLength(); j++) {
					assertEquals("invalid result", results1[i][j],
							results2[i][j]);
				}
			}
		}
	}

	public void testValidateAlabamaResults() throws Exception {
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance()
				.getSpatialExtension();
		List<String> stations = sq.getStationsForState(USState.Alabama);

		DateTime today = new DateTime(System.currentTimeMillis(),
				GregorianChronology.getInstance());
		int starting = today.getYear();
		int month = today.getMonthOfYear();

		CalendarDataCollection results = this.query.computeSpi(stations,
				CalendarPeriod.WEEKLY, 1, today, starting);

	}

	public void testThreadedSpiCalls() throws Exception {
		threadedFailure = false;

		Runnable r = new Runnable() {
			public void run() {
				DateTime ending = new DateTime(2002, 12, 31, 0, 0, 0, 0);
				DateTime starting = new DateTime(1998, 1, 1, 0, 0, 0, 0);
				TemporalPeriod period = new TemporalPeriod(starting, ending);

				try {
					ClimateSpatialExtension sq = ClimateServiceAccessor
							.getInstance().getSpatialExtension();
					List<String> stations = sq
							.getStationsForState(USState.Nebraska);

					VariableFilter filter = new VariableFilter();
					filter.setValidPeriod(period);
					filter.setVariableType(DataType.PRECIP);
					filter.setMissingTolerance(0.2f);
					ArrayList<VariableFilter> filters = new ArrayList<VariableFilter>();
					filters.add(filter);

					List<String> filtered_stations = ClimateServiceAccessor
							.getInstance().getClimateMetaDataQuery()
							.filterStations(stations, filters, period, 0.2f,
									true);

					// get data
					query.computeSpi(filtered_stations, CalendarPeriod.MONTHLY,
							1, ending, -1);

				} catch (Throwable t) {
					t.printStackTrace();
					threadedFailure = true;
				} finally {
					threadCount--;
				}
			}
		};

		int len = threadCount;
		for (int i = 0; i < len; i++) {
			Thread t = new Thread(r);
			t.start();
		}

		while (threadCount > 0) {
			if (threadedFailure) {
				fail("Threaded SPI calls failed");
			}
			Thread.sleep(10000);
		}
	}
}