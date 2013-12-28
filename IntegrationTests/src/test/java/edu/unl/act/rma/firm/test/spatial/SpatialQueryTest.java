/* Created on: May 27, 2010 */
package edu.unl.act.rma.firm.test.spatial;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import edu.unl.act.rma.firm.core.component.SpatialQuery;
import edu.unl.act.rma.firm.core.configuration.SpatialServiceAccessor;
import edu.unl.act.rma.firm.core.spatial.BoundingBox;
import edu.unl.act.rma.firm.core.spatial.Layer;
import edu.unl.act.rma.firm.core.spatial.Region;
import edu.unl.act.rma.firm.core.spatial.Style;
import edu.unl.act.rma.firm.core.spatial.USCity;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;

/**
 * @author Ian Cottingham
 *
 */
public class SpatialQueryTest extends TestCase {

	public void testCityNameQuery() throws Exception { 
		SpatialQuery query = SpatialServiceAccessor.getInstance().getSpatialQuery();
		USCity city = query.getCityForName("Houston", USState.Texas);
		
		assertEquals("invalid city", "Houston", city.getName());
	}
	
	public void testStateRegion() throws Exception { 
		SpatialQuery query = SpatialServiceAccessor.getInstance().getSpatialQuery();
		Region r = query.getRegionForState(USState.Nebraska);
		
		assertEquals("invalid region", USState.Nebraska, r.getState());
		assertEquals("invalid polygon", 1, r.getPolygon().size());
	}
	
	public void testCountyRegion() throws Exception { 
		SpatialQuery query = SpatialServiceAccessor.getInstance().getSpatialQuery();
	
		USCounty county = query.getCountyForName("Travis", USState.Texas);
		
		Region r = query.getRegionForCounty(county);
		
		assertEquals("invalid state", USState.Texas, r.getState());
		assertEquals("invalid county", "Travis", r.getCounty().getName());
		assertEquals("invalid polygon", 1, r.getPolygon().size());
	}
	
	public void testMultipleStateRegions() throws Exception { 
		SpatialQuery query = SpatialServiceAccessor.getInstance().getSpatialQuery();
		ArrayList<USState> states = new ArrayList<USState>();
		
		states.add(USState.New_York);
		states.add(USState.California);
		states.add(USState.Michigan);
		
		List<Region> regions = query.getRegionsForStates(states);
		
		assertEquals("incorrect region count", states.size(), regions.size());
		
		for ( int i=0; i<states.size(); i++ ) {
			assertEquals("invalid state", states.get(i), regions.get(i).getState());
		}
	}
	
	public void testMultipleCountyRegions() throws Exception { 
		SpatialQuery query = SpatialServiceAccessor.getInstance().getSpatialQuery();
	
		List<USCounty> counties = new ArrayList<USCounty>();
		counties.add(query.getCountyForName("Travis", USState.Texas));
		counties.add(query.getCountyForName("Lancaster", USState.Nebraska));
		counties.add(query.getCountyForName("Douglas", USState.Nebraska));
		counties.add(query.getCountyForName("Otoe", USState.Nebraska));
		counties.add(query.getCountyForName("Westchester", USState.New_York));
		counties.add(query.getCountyForName("Fairfield", USState.Connecticut));
		
		List<Region> regions = query.getRegionsForCounties(counties);
		
		assertEquals("incorrect region count",  counties.size(), regions.size());
		
		for ( int i=0; i<counties.size(); i++ ) {
			assertEquals("invalid state", counties.get(i), regions.get(i).getCounty());
		}
	}
	
	public void testKmlGeneration() throws Exception { 
		SpatialQuery query = SpatialServiceAccessor.getInstance().getSpatialQuery();
		
		List<USCounty> counties = new ArrayList<USCounty>();
		counties.add(query.getCountyForName("Travis", USState.Texas));
		counties.add(query.getCountyForName("Lancaster", USState.Nebraska));
		counties.add(query.getCountyForName("Douglas", USState.Nebraska));
		counties.add(query.getCountyForName("Otoe", USState.Nebraska));
		counties.add(query.getCountyForName("Westchester", USState.New_York));
		counties.add(query.getCountyForName("Fairfield", USState.Connecticut));
		
		ArrayList<USState> states = new ArrayList<USState>();
		
		states.add(USState.Tennessee);
		states.add(USState.Washington);
		states.add(USState.California);
		states.add(USState.Hawaii);
		states.add(USState.Alaska);
		
		List<Region> state_regions = query.getRegionsForStates(states);
		List<Region> county_regions = query.getRegionsForCounties(counties);
		
		Style state_style = new Style();
		state_style.setId("stateStyle");
		state_style.setLabelScale("2.000000");
		state_style.setLineWidth("2");
		state_style.setPolyColorBlue("37");
		state_style.setPolyColorGreen("e4");
		state_style.setPolyColorRed("84");
		state_style.setPolyOutline(true);
		state_style.setPolyFill(true);
		
		Style county_style = new Style();
		county_style.setId("countyStyle");
		county_style.setLabelScale("2.000000");
		county_style.setLineWidth("1");
		county_style.setPolyColorBlue("d5");
		county_style.setPolyColorGreen("81");
		county_style.setPolyColorRed("36");
		county_style.setPolyOutline(true);
		county_style.setPolyFill(true);
		
		Layer layer = new Layer();
		
		for ( Region sr : state_regions ) {
			sr.setDescription("This is the state "+sr.getState().name());
			sr.setStyle(state_style);
			layer.addRegion(sr);
		}
		
		for ( Region cr : county_regions ) { 
			cr.setDescription("This is the county polygon for "+cr.getCounty()+" County "+cr.getState().name());
			cr.setStyle(county_style);
			layer.addRegion(cr);
		}
		
		System.out.println(layer.toKml());
		System.out.flush();
	}
	
	public void testStateBoundingQuery() throws Exception { 
		BoundingBox region = new BoundingBox(42.36f, -101.36f, 40.267f, -98.746f);

		SpatialQuery query = SpatialServiceAccessor.getInstance().getSpatialQuery();
		Set<USState> states = query.getStatesByRegion(region);
	
		assertEquals("Invalid state results", 1, states.size());
		assertEquals("Invalid state results", USState.Nebraska, states.toArray()[0]);
	}
	
	public void testCountyBoundingQuery() throws Exception { 
		BoundingBox region = new BoundingBox(42.36f, -101.36f, 40.267f, -98.746f);

		SpatialQuery query = SpatialServiceAccessor.getInstance().getSpatialQuery();
		Set<USCounty> counties = query.getCountiesByRegion(region);
	
		for ( USCounty county : counties ) { 
			System.out.println(county.getName()+", "+county.getState());
		}
	}
}
