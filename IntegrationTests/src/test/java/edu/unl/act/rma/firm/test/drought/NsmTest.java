/* Created On: Sep 7, 2005 */
package edu.unl.act.rma.firm.test.drought;

import java.util.ArrayList;
import java.util.List;
 
import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.climate.VariableFilter;
import edu.unl.act.rma.firm.climate.component.ClimateSpatialExtension;
import edu.unl.act.rma.firm.climate.configuration.ClimateServiceAccessor;
import edu.unl.act.rma.firm.core.DTOCollection;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.TemporalPeriod;
import edu.unl.act.rma.firm.core.component.SpatialQuery;
import edu.unl.act.rma.firm.core.spatial.USRegion;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.drought.NsmCompleteDTO;
import edu.unl.act.rma.firm.drought.NsmSummaryDTO;
import edu.unl.act.rma.firm.drought.SoilMoistureRegime;
import edu.unl.act.rma.firm.drought.SoilMoistureRegimeSubdivision;
import edu.unl.act.rma.firm.drought.SoilTemperatureRegime;
import edu.unl.act.rma.firm.drought.component.DroughtIndexQuery;
import edu.unl.act.rma.firm.drought.configuration.DroughtServiceAccessor;
import edu.unl.act.rma.firm.test.StationList;


/**
 * @author jdokulil
 *
 */
public class NsmTest extends TestCase {

	private DroughtIndexQuery query;	
	public static final String STATION_ID = StationList.AGATE.getStationID();
	private static final String STATION_ID_1 = StationList.BIRMINGHAM.getStationID();
	
	// variables used in testThreadedCalls
	private int threadCount = 3;
	private boolean threadedFailure;
	
	@Override
	protected void setUp() throws Exception {
		
		this.query = DroughtServiceAccessor.getInstance()
		.getDroughtIndexQuery();
	}
	
	public void testEmptyStationList() throws Exception {
		DateTime start = new DateTime(1893, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime ending = new DateTime(2006, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		ArrayList<String> stations = new ArrayList<String>();
		DTOCollection<NsmSummaryDTO> results = this.query.computeSummaryNewhall(stations, start, ending);
		assertNotNull("returned null results", results);
	}
	
	public void testBadDates() throws Exception {
		DateTime start = new DateTime(2006, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime ending = new DateTime(2007, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_ID);

		try {
			this.query.computeSummaryNewhall(stations, start, ending);
		} catch (InvalidArgumentException se) {
			// test case passed
		} catch ( Exception e ) { 
			throw e;
		}
		
		try {
			this.query.computeCompleteNewhall(stations, start, ending);
		} catch (InvalidArgumentException se) {
			// test case passed
		} catch ( Exception e ) { 
			throw e;
		}
	}
	
	
	public void testSummaryBadValues() throws Exception {
		DateTime start = new DateTime(1990, 1, 1, 0, 0, 0, 0);
		DateTime end = new DateTime(2005, 1, 1, 0, 0, 0, 0);
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_ID);

		// get data
		DTOCollection<NsmSummaryDTO> results = this.query.computeSummaryNewhall(stations, start, end);
		NsmSummaryDTO dto = results.get(STATION_ID);

		int undefined_years_count = 0;
		for (Integer year : dto.years()) {
			NsmSummaryDTO.DataSet ds = dto.getData(year);
			
			if (ds.getRegime() == SoilMoistureRegimeSubdivision.UNDEFINED) {
				undefined_years_count++;

				assertEquals("non-zero awb value in an undefined year", 0.0f, ds.getAwb());
				assertEquals("non-zero bio8 value in an undefined year", 0, ds.getBio8());
				assertEquals("non-zero medum days value in an undefined year", 0, ds.getMediumDays());
			}
		}

		assertEquals("incorrect number of UNDEFINED years", 1, undefined_years_count);
	}
	
	
	public void testCompleteBadValues() throws Exception {
		DateTime start = new DateTime(1990, 1, 1, 0, 0, 0, 0);
		DateTime end = new DateTime(2005, 1, 1, 0, 0, 0, 0);
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_ID);

		// get data
		DTOCollection<NsmCompleteDTO> results = this.query.computeCompleteNewhall(stations, start, end);
		NsmCompleteDTO dto = results.get(STATION_ID);
		
		int undefined_years_count = 0;
		for (Integer year : dto.years()) {
			NsmCompleteDTO.DataSet ds = dto.getData(year);
			
			if (ds.getMoistureRegimeSubdivision() == SoilMoistureRegimeSubdivision.UNDEFINED) {
				undefined_years_count++;
				
				for (float val : ds.getMonthlyEvapotranspirations()) {
					assertEquals("non-zero monthly evapotranspiration calendar value in an unedfine year", 0.0f, val);
				}
				assertEquals("non-zero most consecutive moist days value in an undefined year", 0, ds.getMostConsecutiveMoistDays());
				assertEquals("non-zero total dry days value in an undefined year", 0, ds.getTotalDryDays());
				assertEquals("non-zero total moist days above 5 value in an undefined year", 0, ds.getTotalMoistDaysAbove5());
			}
		}
		
		assertEquals("incorrect number of UNDEFINED years", 1, undefined_years_count);
	}
	
	
	public void testSummary1() throws Exception {
		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0);
		DateTime end = new DateTime(2005, 1, 1, 0, 0, 0, 0);
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_ID);

		// get data
		DTOCollection<NsmSummaryDTO> results = this.query.computeSummaryNewhall(stations, start, end);
		
		// validate nsm results
		assertEquals("wrong number of summary results", 1, results.size());
		NsmSummaryDTO dto = results.get(STATION_ID);
		assertEquals("wrong number of years of results", 6, dto.size());

		// validate averages
		assertEquals("incorrect average dry days", 130.0f, dto.getDryDaysAvg());
		assertEquals("incorrect average bio8", 54.0f, dto.getBio8Avg());
		assertEquals("incorrect average evapotranspiration", 635.545f, dto.getEvapotranspirationAvg(), 1.0);
		
		// validate regime frequencies
		for (NsmSummaryDTO.RegimeSummary summary : dto.getRegimes()) {
			if (summary.getRegime() == SoilMoistureRegime.XERIC) {
				assertEquals("incorrect XERIC frequency", 0.0f, summary.getFrequency(), 0.1);
				assertEquals("incorrect XERIC count", 0, summary.getCount());
			} else if (summary.getRegime() == SoilMoistureRegime.ARIDIC) {
				assertEquals("incorrect ARIDIC frequency", 66.6f, summary.getFrequency(), 0.1);
				assertEquals("incorrect ARIDIC count", 4, summary.getCount());
			} else if (summary.getRegime() == SoilMoistureRegime.USTIC) {
				assertEquals("incorrect USTIC frequency", 33.33f, summary.getFrequency(), 0.1);
				assertEquals("incorrect USTIC count", 2, summary.getCount());
			}
		}
		
		for (NsmSummaryDTO.RegimeSubdivisionSummary summary : dto.getSubdivisions()) {
			if (summary.getRegime() == SoilMoistureRegimeSubdivision.TYPIC_TEMPUSTIC) {
				assertEquals("incorrect TYPIC ARIDIC frequency", 33.3f, summary.getFrequency(), 0.1);
				assertEquals("incorrect TYPIC ARIDIC count", 2, summary.getCount());
			} else if (summary.getRegime() == SoilMoistureRegimeSubdivision.EXTREME_ARIDIC) {
				assertEquals("incorrect EXTREME ARIDIC frequency", 16.6f, summary.getFrequency(), 0.1);
				assertEquals("incorrect EXTREME ARIDIC count", 1, summary.getCount());
			} else if (summary.getRegime() == SoilMoistureRegimeSubdivision.WET_TEMPUSTIC) {
				assertEquals("incorrect WET TEMPUSTIC frequency", 0.0f, summary.getFrequency(), 0.01);
				assertEquals("incorrect WET TEMPUSTIC count", 0, summary.getCount());
			}
		}
		
		// validate some of the yearly data
		NsmSummaryDTO.DataSet dataset = dto.getData(2000);
		assertEquals("incorrect regime", SoilMoistureRegimeSubdivision.WEAK_ARIDIC, dataset.getRegime());
		assertEquals("incorrect precipitation", 456.0f, dataset.getPrecipitation(), 0.5);
		assertEquals("incorrect MSD", -273.443f, dataset.getMsd(), 0.5);
		assertEquals("incorrect number of moist days", 62, dataset.getMoistDays());
		
		dataset = dto.getData(2002);
		assertEquals("incorrect regime", SoilMoistureRegimeSubdivision.TYPIC_ARIDIC, dataset.getRegime());
		assertEquals("incorrect precipitation", 221.0f, dataset.getPrecipitation(), 0.5);
		assertEquals("incorrect MSD", -319.2f, dataset.getMsd(), 0.5);
		assertEquals("incorrect number of dry days", 179, dataset.getDryDays());
		assertEquals("incorrect number of moist days", 0, dataset.getMoistDays());
		assertEquals("incorrect bio8 value", 26, dataset.getBio8());
	}

	public void testComplete1() throws Exception {
		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0);
		DateTime end = new DateTime(2005, 1, 1, 0, 0, 0, 0);
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(STATION_ID);

		// get data
		DTOCollection<NsmCompleteDTO> results = this.query.computeCompleteNewhall(stations, start, end);
		
		// validate nsm results
		assertEquals("wrong number of summary results", 1, results.size());
		NsmCompleteDTO dto = results.get(STATION_ID);
		assertEquals("wrong number of years of results", 6, dto.size());
		
		NsmCompleteDTO.DataSet data = dto.getData(2003);
		assertEquals("incorrect temperature regime", SoilTemperatureRegime.MESIC, data.getTemperatureRegime());
		assertEquals("incorrect moisture regime", SoilMoistureRegimeSubdivision.TYPIC_TEMPUSTIC, data.getMoistureRegimeSubdivision());
		assertEquals("incorrect total dry days", 112, data.getTotalDryDays());
		assertEquals("incorrect most consecutive moist days", 185, data.getMostConsecutiveMoistDays());
		assertEquals("incorrect [2][2] slant value", 0.016f, data.getSoilMoistureSlants().get(9), 0.01);
	}
	
	
	public void testCompleteDTOValidity() throws Exception {
		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0);
		DateTime end = new DateTime(2005, 1, 1, 0, 0, 0, 0);
		
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		List<String> stations = sq.getStationsForState(USState.Rhode_Island);
	
		
		DTOCollection<NsmCompleteDTO> results = this.query.computeCompleteNewhall(stations, start, end);
		for (String station : results.stations()) {
			NsmCompleteDTO dto = results.get(station);
			for (Integer year : dto.years()) {
				NsmCompleteDTO.DataSet ds = dto.getData(year);

				assertEquals("invalid size of soil moisture calendar", 360, ds.getSoilMoistureCalendar().size());
				assertEquals("invalid size of soil moisture slant", 64, ds.getSoilMoistureSlants().size());
			}
		}
	}
	
	public void testMidwestSummary() throws Exception {
		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0);
		DateTime end = new DateTime(2005, 1, 1, 0, 0, 0, 0);
		
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		List<String> stations = sq.getStationsForGeographicRegion(USRegion.MIDWEST);

		// get data
		query.computeSummaryNewhall(stations, start, end);
	}
	
	public void testThreadedCalls() throws Exception {
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
					
					query.computeSummaryNewhall(filtered_stations, starting, ending);
					
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
				fail("Threaded NSM calls failed");
			}
			Thread.sleep(10000);
		}
	}
}
