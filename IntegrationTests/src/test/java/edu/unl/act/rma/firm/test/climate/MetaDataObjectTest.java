/* Created On: Sep 12, 2005 */
package edu.unl.act.rma.firm.test.climate;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.management.JMException;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.climate.VariableFilter;
import edu.unl.act.rma.firm.climate.component.ClimateMetaDataQuery;
import edu.unl.act.rma.firm.climate.component.ClimateSpatialExtension;
import edu.unl.act.rma.firm.climate.configuration.ClimateServiceAccessor;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.InvalidStateException;
import edu.unl.act.rma.firm.core.MetaDataCollection;
import edu.unl.act.rma.firm.core.StationMetaDataType;
import edu.unl.act.rma.firm.core.StationSearchTerms;
import edu.unl.act.rma.firm.core.TemporalPeriod;
import edu.unl.act.rma.firm.core.spatial.BoundingBox;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.test.StationList;

/**
 * @author Ian Cottingham
 *
 */
public class MetaDataObjectTest extends TestCase {
	
	public static final String ASHLAND_ID=StationList.ASHLAND_ID.getStationID();
	public static final String RI_ID = StationList.PROVIDENCE_ID.getStationID();
		
	public void testStationMeta() throws InstantiationException, InvalidArgumentException, JMException, RemoteException { 
		ClimateServiceAccessor accessor = ClimateServiceAccessor.getInstance();
		ClimateMetaDataQuery cmdo = accessor.getClimateMetaDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		
		Map<String, Object> data = cmdo.getMetaData(stations, StationMetaDataType.STATION_NAME, CalendarPeriod.MONTHLY).extractType(StationMetaDataType.STATION_NAME);
		assertEquals("contains the correct number of results", 1, data.size());
		assertEquals("contains the correct station name", "ASHLAND 2", (String)data.get(ASHLAND_ID));
	}
	
	public void testStationSelectionByState() throws InstantiationException, JMException, RemoteException { 
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		
		List<String> stations = sq.getStationsForState(USState.Nebraska);
		
		assertTrue("contains the correct number of results", stations.size() > 500);
	}
	
	public void testAllMetaDataMultipleStation() throws InstantiationException, InvalidArgumentException, JMException, RemoteException {
		ClimateServiceAccessor accessor = ClimateServiceAccessor.getInstance();
		ClimateMetaDataQuery cmdo = accessor.getClimateMetaDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		stations.add(RI_ID);
		
		MetaDataCollection<StationMetaDataType> meta_data = cmdo.getAllMetaData(stations, CalendarPeriod.MONTHLY);
		
		for ( String station : meta_data ) { 
			Map m = meta_data.getStationMetaData(station);
			
			if ( station.equals(ASHLAND_ID)) { 
				assertEquals("check for correct Ashland name", "ASHLAND 2", m.get(StationMetaDataType.STATION_NAME));
			} else {
				assertEquals("check for correct Providence name", "PROVIDENCE T F GREEN AP", m.get(StationMetaDataType.STATION_NAME));
			}
		}
	}
	
	public void testDailyPeriod() throws InstantiationException, JMException, InvalidArgumentException, RemoteException { 
		ClimateServiceAccessor accessor = ClimateServiceAccessor.getInstance();
		ClimateMetaDataQuery cmdo = accessor.getClimateMetaDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		stations.add(RI_ID);
		
		TemporalPeriod period = cmdo.getLongestPeriod(stations, CalendarPeriod.DAILY);
		DateTime ending = cmdo.getEndingDate(CalendarPeriod.DAILY);
		
		assertEquals("correct start month", 1, period.getStart().getMonthOfYear());
		assertEquals("correct start day", 1, period.getStart().getDayOfMonth());
		assertEquals("correct start year", 1893, period.getStart().getYear());
		
		assertTrue("correct end month", ending.getMonthOfYear() >= period.getEnd().getMonthOfYear());
		assertTrue("correct end day", ending.getDayOfMonth() >= period.getEnd().getDayOfMonth());
		assertTrue("correct end year", ending.getYear() >= period.getEnd().getYear());
	}
	
	public void testWeeklyPeriod() throws InstantiationException, JMException, InvalidArgumentException, RemoteException { 
		ClimateServiceAccessor accessor = ClimateServiceAccessor.getInstance();
		ClimateMetaDataQuery cmdo = accessor.getClimateMetaDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		stations.add(RI_ID);
		
		TemporalPeriod period = cmdo.getLongestPeriod(stations, CalendarPeriod.WEEKLY);
		DateTime ending = cmdo.getEndingDate(CalendarPeriod.WEEKLY);
		
		assertEquals("correct start month", 1, period.getStart().getMonthOfYear());
		assertEquals("correct start day", 1, period.getStart().getDayOfMonth());
		assertEquals("correct start year", 1893, period.getStart().getYear());
		
		assertTrue("correct end month", ending.getMonthOfYear() >= period.getEnd().getMonthOfYear());
		assertTrue("correct end day", ending.getDayOfMonth() >= period.getEnd().getDayOfMonth());
		assertTrue("correct end year", ending.getYear() >= period.getEnd().getYear());
	}
	
	public void testMonthlyPeriod() throws InstantiationException, JMException, InvalidArgumentException, RemoteException { 
		ClimateServiceAccessor accessor = ClimateServiceAccessor.getInstance();
		ClimateMetaDataQuery cmdo = accessor.getClimateMetaDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		stations.add(RI_ID);
		
		TemporalPeriod period = cmdo.getLongestPeriod(stations, CalendarPeriod.MONTHLY);
		DateTime ending = cmdo.getEndingDate(CalendarPeriod.MONTHLY);
		
		assertEquals("correct start month", 1, period.getStart().getMonthOfYear());
		assertEquals("correct start day", 1, period.getStart().getDayOfMonth());
		assertEquals("correct start year", 1893, period.getStart().getYear());
		
		assertTrue("correct end month", ending.getMonthOfYear() >= period.getEnd().getMonthOfYear());
		assertTrue("correct end day", ending.getDayOfMonth() >= period.getEnd().getDayOfMonth());
		assertTrue("correct end year", ending.getYear() >= period.getEnd().getYear());
	}
	
	public void testStationFilter() throws InstantiationException, JMException, RemoteException {
		ClimateServiceAccessor accessor = ClimateServiceAccessor.getInstance();
		ClimateMetaDataQuery query = accessor.getClimateMetaDataQuery();
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		
		TemporalPeriod period = new TemporalPeriod(new DateTime(1949,1,1,0,0,0,0,GregorianChronology.getInstance()),
				new DateTime(1980,12,31,0,0,0,0,GregorianChronology.getInstance()));
		
		VariableFilter high_filter = new VariableFilter();
		high_filter.setValidPeriod(period);
		high_filter.setVariableType(DataType.HIGH_TEMP);
		high_filter.setMissingTolerance(0.20f);
		
		VariableFilter low_filter = new VariableFilter();
		low_filter.setValidPeriod(period);
		low_filter.setVariableType(DataType.LOW_TEMP);
		low_filter.setMissingTolerance(0.20f);

		VariableFilter precip_filter = new VariableFilter();
		precip_filter.setValidPeriod(period);
		precip_filter.setVariableType(DataType.PRECIP);
		precip_filter.setMissingTolerance(0.20f);
		
		List<VariableFilter> filters = new ArrayList<VariableFilter>();
		filters.add(precip_filter);
		
		List<String> unfiltered_stations = sq.getStationsForState(USState.Rhode_Island);
		List<String> filtered_stations = query.filterStations(unfiltered_stations, filters, period, 0.20f, false);
		
		assertEquals("incorrect station count", 18, unfiltered_stations.size());
		assertEquals("incorrect filtered count", 3, filtered_stations.size());		
	}
	
	public void testStationFilterActualTolerance() throws InstantiationException, JMException, RemoteException {
		ClimateServiceAccessor accessor = ClimateServiceAccessor.getInstance();
		ClimateMetaDataQuery query = accessor.getClimateMetaDataQuery();
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		
		TemporalPeriod period = new TemporalPeriod(new DateTime(2000,1,1,0,0,0,0,GregorianChronology.getInstance()),
				new DateTime(2005,12,31,0,0,0,0,GregorianChronology.getInstance()));
		
		VariableFilter temp_filter = new VariableFilter();
		temp_filter.setValidPeriod(period);
		temp_filter.setVariableType(DataType.NORMAL_TEMP);
		temp_filter.setMissingTolerance(0.1f);

		VariableFilter precip_filter = new VariableFilter();
		precip_filter.setValidPeriod(period);
		precip_filter.setVariableType(DataType.PRECIP);
		precip_filter.setMissingTolerance(0.1f);
		
		List<VariableFilter> filters = new ArrayList<VariableFilter>();
		filters.add(precip_filter);
		filters.add(temp_filter);
		
		List<String> unfiltered_stations = sq.getStationsForState(USState.Nebraska);
		List<String> filtered_stations = query.filterStations(unfiltered_stations, filters, period, 0.1f, true);
		
		assertEquals("incorrect station count", 594, unfiltered_stations.size());
		assertEquals("incorrect filtered count", 177, filtered_stations.size());		
	}
	
	public void testStationFilterVariableTolerance() throws InstantiationException, JMException, RemoteException {
		ClimateServiceAccessor accessor = ClimateServiceAccessor.getInstance();
		ClimateMetaDataQuery query = accessor.getClimateMetaDataQuery();
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		
		TemporalPeriod period = new TemporalPeriod(new DateTime(1998,1,1,0,0,0,0,GregorianChronology.getInstance()),
				new DateTime(2005,12,31,0,0,0,0,GregorianChronology.getInstance()));
		
		VariableFilter temp_filter = new VariableFilter();
		temp_filter.setValidPeriod(period);
		temp_filter.setVariableType(DataType.NORMAL_TEMP);
		temp_filter.setMissingTolerance(0.1f);

		VariableFilter precip_filter = new VariableFilter();
		precip_filter.setValidPeriod(period);
		precip_filter.setVariableType(DataType.PRECIP);
		precip_filter.setMissingTolerance(0.005f);
		
		List<VariableFilter> filters = new ArrayList<VariableFilter>();
		filters.add(precip_filter);
		filters.add(temp_filter);
		
		List<String> unfiltered_stations = sq.getStationsForState(USState.Rhode_Island);
		List<String> filtered_stations = query.filterStations(unfiltered_stations, filters, period, 0.1f, true);
		
		assertEquals("incorrect station count", 18, unfiltered_stations.size());
		assertEquals("incorrect filtered count", 3, filtered_stations.size());
	}
	
	/**
	 * In this test case the individual variable tolerances are higher than the overall tolerance, so they should not effect it
	 * 
	 * @throws InstantiationException
	 * @throws JMException
	 * @throws RemoteException
	 */
	public void testStationFilterVariablePeriod() throws InstantiationException, JMException, RemoteException {
		ClimateServiceAccessor accessor = ClimateServiceAccessor.getInstance();
		ClimateMetaDataQuery query = accessor.getClimateMetaDataQuery();
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		
		TemporalPeriod overall_period = new TemporalPeriod(new DateTime(2000,1,1,0,0,0,0,GregorianChronology.getInstance()),
				new DateTime(2005,12,31,0,0,0,0,GregorianChronology.getInstance()));
		TemporalPeriod temp_period = new TemporalPeriod(new DateTime(1980,1,1,0,0,0,0,GregorianChronology.getInstance()),
				new DateTime(2005,12,31,0,0,0,0,GregorianChronology.getInstance()));
		TemporalPeriod precip_period = new TemporalPeriod(new DateTime(1990,1,1,0,0,0,0,GregorianChronology.getInstance()),
				new DateTime(2005,12,31,0,0,0,0,GregorianChronology.getInstance()));
		
		VariableFilter temp_filter = new VariableFilter();
		temp_filter.setValidPeriod(temp_period);
		temp_filter.setVariableType(DataType.NORMAL_TEMP);
		temp_filter.setMissingTolerance(0.2f);

		VariableFilter precip_filter = new VariableFilter();
		precip_filter.setValidPeriod(precip_period);
		precip_filter.setVariableType(DataType.PRECIP);
		precip_filter.setMissingTolerance(0.2f);
		
		List<VariableFilter> filters = new ArrayList<VariableFilter>();
		filters.add(precip_filter);
		filters.add(temp_filter);
		
		List<String> unfiltered_stations = sq.getStationsForState(USState.Rhode_Island);
		List<String> filtered_stations = query.filterStations(unfiltered_stations, filters, overall_period, 0.1f, true);
		
		assertEquals("incorrect station count", 18, unfiltered_stations.size());
		assertEquals("incorrect filtered count", 3, filtered_stations.size());	
	}
	
	public void testInvalidLongestPeriod() throws InstantiationException, InvalidArgumentException, JMException, RemoteException { 
		ClimateServiceAccessor accessor = ClimateServiceAccessor.getInstance();
		ClimateMetaDataQuery query = accessor.getClimateMetaDataQuery();
		
		List<String> stations = new ArrayList<String>();
		
		TemporalPeriod period = query.getLongestPeriod(stations, CalendarPeriod.MONTHLY);
		
		assertFalse("the period was valid", period.valid());
	}
	
	public void testEmptyCollectionToString() {
		MetaDataCollection collection = new MetaDataCollection();
		assertNotNull("null empty collection toString()", collection.toString());
	}
	
	public void testResltToString() throws JMException, InstantiationException, InvalidArgumentException, InvalidStateException, RemoteException {
		ClimateServiceAccessor accessor = ClimateServiceAccessor.getInstance();
		ClimateMetaDataQuery cmdo = accessor.getClimateMetaDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		
		MetaDataCollection collection = cmdo.getMetaData(stations, StationMetaDataType.STATION_NAME, CalendarPeriod.MONTHLY);
		assertNotNull("null collection toString()", collection.toString());
		
		collection.startStation(ASHLAND_ID);
		assertNotNull("null collection toString()", collection.toString());
	}
	
	public void testStationSearchByCounty() throws InstantiationException, JMException, RemoteException { 
		ClimateMetaDataQuery cmdq = ClimateServiceAccessor.getInstance().getClimateMetaDataQuery();
		
		StationSearchTerms terms = new StationSearchTerms();
		terms.setCounty("Madison");

		MetaDataCollection<StationMetaDataType> mdc = cmdq.findStations(terms);
		int count = 0;
		for ( String s : mdc ) {
			count++;
			assertNotNull("null station name", mdc.getStationMetaData(s).get(StationMetaDataType.STATION_NAME));
		}
		
		System.out.println("Madison county returned: " + count);
	}
	
	public void testStationSearchByCountyState() throws InstantiationException, JMException, RemoteException { 
		ClimateMetaDataQuery cmdq = ClimateServiceAccessor.getInstance().getClimateMetaDataQuery();
		
		StationSearchTerms terms = new StationSearchTerms();
		terms.setCounty("Madison");
		terms.setState(USState.Georgia);

		MetaDataCollection<StationMetaDataType> mdc = cmdq.findStations(terms);
		int count = 0;
		for ( String s : mdc ) {
			count++;
			assertNotNull("null station name", mdc.getStationMetaData(s).get(StationMetaDataType.STATION_NAME));
		}
		
		System.out.println("Madison county, Georgia returned: " + count);
	}
	
	public void testStationSearchByCityState() throws InstantiationException, JMException, RemoteException { 
		ClimateMetaDataQuery cmdq = ClimateServiceAccessor.getInstance().getClimateMetaDataQuery();
		
		StationSearchTerms terms = new StationSearchTerms();
		terms.setCity("Ashland");
		terms.setState(USState.Nebraska);

		MetaDataCollection<StationMetaDataType> mdc = cmdq.findStations(terms);
		boolean found = false;
		int count = 0;
		for ( String s : mdc ) {
			count++;
			if (s.equals(ASHLAND_ID)) {
				found = true;
			}
			assertNotNull("null station name", mdc.getStationMetaData(s).get(StationMetaDataType.STATION_NAME));
			assertNotNull("null network id", mdc.getStationMetaData(s).get(StationMetaDataType.NETWORK_ID));
		}

		System.out.println("Ashland, NE returned: " + count);
		assertTrue("ASHLAND 2 was not returned", found);
	}
	
	public void testStationSearchByCity() throws InstantiationException, JMException, RemoteException { 
		ClimateMetaDataQuery cmdq = ClimateServiceAccessor.getInstance().getClimateMetaDataQuery();
		
		StationSearchTerms terms = new StationSearchTerms();
		terms.setCity("Ashland");

		MetaDataCollection<StationMetaDataType> mdc = cmdq.findStations(terms);
		boolean found = false;
		int count = 0;
		for ( String s : mdc ) {
			count++;
			if (s.equals(ASHLAND_ID)) {
				found = true;
			}
			assertNotNull("null station name", mdc.getStationMetaData(s).get(StationMetaDataType.STATION_NAME));
			assertNotNull("null network id", mdc.getStationMetaData(s).get(StationMetaDataType.NETWORK_ID));
		}

		System.out.println("Ashland (city) returned: " + count);
		assertTrue("ASHLAND 2 was not returned", found);
	}
	
	public void testStationSearchByStationName() throws InstantiationException, JMException, RemoteException { 
		ClimateMetaDataQuery cmdq = ClimateServiceAccessor.getInstance().getClimateMetaDataQuery();
		
		StationSearchTerms terms = new StationSearchTerms();
		terms.setStationName("Ashland");

		MetaDataCollection<StationMetaDataType> mdc = cmdq.findStations(terms);
		boolean found = false;
		int count = 0;
		for ( String s : mdc ) {
			count++;
			if ( s.equals(ASHLAND_ID) ) {
				found = true;
			}
			assertNotNull("null station name", mdc.getStationMetaData(s).get(StationMetaDataType.STATION_NAME));
			assertNotNull("null network id", mdc.getStationMetaData(s).get(StationMetaDataType.NETWORK_ID));
			assertNotNull("null network id", mdc.getStationMetaData(s).get(StationMetaDataType.NETWORK_ID));
			assertNotNull("null state", mdc.getStationMetaData(s).get(StationMetaDataType.STATE));
			assertNotNull("null end date", mdc.getStationMetaData(s).get(StationMetaDataType.ABS_END_DATE));
			assertNotNull("null start date", mdc.getStationMetaData(s).get(StationMetaDataType.ABS_START_DATE));
		}
		
		System.out.println("by station name found: " + count);
		assertTrue("ASHLAND 2 was not returned", found);
	}
	
	public void testGetIntervalGaps() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		
		ClimateMetaDataQuery query = ClimateServiceAccessor.getInstance().getClimateMetaDataQuery();
		ArrayList <String> stationList = new ArrayList<String>();
		stationList.add(ASHLAND_ID);
	
		Map <String, List<Interval>> resultMap = query.getIntervalGaps(stationList, DataType.PRECIP);
		
		// Check Ashland Station
		List <Interval> intervalList = resultMap.get(ASHLAND_ID);
		assertNotNull("null missing data list", intervalList);
		
		Interval interval = intervalList.get(1);		// Second gap spans two different years
		assertNotNull("null missing data", interval);
		
		assertEquals("incorrect number of gaps", "36", String.valueOf(intervalList.size()));
		
		DateTime start = interval.getStart();
		DateTime end = interval.getEnd();
				
		assertEquals("start year not equal", "1893", start.year().getAsString());
		assertEquals("start month not equal", "4", start.monthOfYear().getAsString());
		assertEquals("end year not equal", "1893", end.year().getAsString());
		assertEquals("end month not equal", "5", end.monthOfYear().getAsString());
		
		//Changed to not use RI station as data changed with Jan 2007 build, now using Ashland gap 4
		assertNotNull("null missing data list", intervalList);
		
			
		interval = intervalList.get(4);					// Only one year gap
		assertNotNull("null missing data", interval);
		
		
		start = interval.getStart();
		end = interval.getEnd();
		
		assertEquals("start year not equal", "1894", start.year().getAsString());
		assertEquals("start month not equal", "12", start.monthOfYear().getAsString());
		assertEquals("end year not equal", "1895", end.year().getAsString());
		assertEquals("end month not equal", "1", end.monthOfYear().getAsString());
			
	}
	
	public void testStationValidation() throws Exception { 
		ClimateMetaDataQuery query = ClimateServiceAccessor.getInstance().getClimateMetaDataQuery();
		List <String> stationList = new ArrayList<String>();
		stationList.add(StationList.ASHLAND_ID.getStationID());
		stationList.add(StationList.ARTHUR.getStationID());
		stationList.add(StationList.HAVELOCK.getStationID());
		stationList.add("545898543");
		stationList.add("some ID");
		
		stationList = query.removeInvalidStations(stationList);
		assertEquals("invalid removeal list size", stationList.size(), 3);
		
		assertFalse("incorrect validation result", query.isValidStation("test station"));
		assertTrue("incorrect validation result", query.isValidStation(StationList.ASHLAND2_ID.getStationID()));
	}
	
	public void testStationsForDefinedRegion() throws InstantiationException, JMException, RemoteException { 
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		BoundingBox boundingBox = new BoundingBox(43f, -104f, 39f, -95f);
		
		List<String> stations = sq.getStationsForDefinedRegion(boundingBox);
		
		assertTrue("contains the correct number of results", stations.size() > 500);
	}
}
