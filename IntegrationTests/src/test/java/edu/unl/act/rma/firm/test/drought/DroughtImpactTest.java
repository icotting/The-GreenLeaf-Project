/* Created On Dec 5, 2006 */
package edu.unl.act.rma.firm.test.drought;

import java.util.List;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.core.component.SpatialQuery;
import edu.unl.act.rma.firm.core.configuration.SpatialServiceAccessor;
import edu.unl.act.rma.firm.core.spatial.BoundingBox;
import edu.unl.act.rma.firm.core.spatial.SpatialReference;
import edu.unl.act.rma.firm.core.spatial.USCity;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.drought.DroughtImpactStatistics;
import edu.unl.act.rma.firm.drought.DroughtReportCategory;
import edu.unl.act.rma.firm.drought.ImpactBean;
import edu.unl.act.rma.firm.drought.component.DroughtImpactQuery;
import edu.unl.act.rma.firm.drought.configuration.DroughtServiceAccessor;

/**
 * @author Ian Cottingham
 *
 */
public class DroughtImpactTest extends TestCase {
	
	
	public void testCityQuery() throws Exception { 
		System.out.println("============= CITY TEST =============");
		SpatialQuery spatial_query = SpatialServiceAccessor.getInstance().getSpatialQuery();
		List<USCity> cities = spatial_query.getCitiesForName("Lincoln");
		USCity city =null;
		
		for ( USCity c : cities ) { 
			if ( c.getCounty().getState() == USState.Nebraska ) { 
				city = c;
				break;
			}
		}
		
		assertNotNull(city);
		
		DroughtImpactQuery impact_query = DroughtServiceAccessor.getInstance().getDroughtImpactQuery();
		List<ImpactBean> impacts = impact_query.loadAllImpacts(impact_query.queryAllImpactsForCity(city));
		
		for ( ImpactBean impact : impacts ) { 
			if ( impact == null ) { 
				continue;
			}
			int size = impact.getReports() == null ? 0: impact.getReports().size();
			System.out.printf("\n\n\nTitle: %s\nDescription: %s\nReport Count: %d\n", impact.getTitle(), impact.getSummary(), size);
			for ( SpatialReference ref : impact.getSpatialReferences() ) { 
				System.out.printf("Ref Type: %s\n", ref.getReferenceType());
			}
		}
		
		assertTrue("Invalid result size", impacts.size() > 0);
	}
	
	public void testAllCountyQuery() throws Exception { 
		System.out.println("============= COUNTY TEST =============");
		SpatialQuery spatial_query = SpatialServiceAccessor.getInstance().getSpatialQuery();
		List<USCounty> counties = spatial_query.getCountiesForName("Lancaster");
		
		USCounty county = null;
		for ( USCounty c : counties ) { 
			if ( c.getState() == USState.Nebraska ) { 
				county = c;
				break;
			}
		}
		
		assertNotNull(county);
		
		DroughtImpactQuery impact_query = DroughtServiceAccessor.getInstance().getDroughtImpactQuery();
		List<ImpactBean> impacts = impact_query.loadAllImpacts(impact_query.queryAllImpactsForCounty(county));
		
		
		for ( ImpactBean impact : impacts ) { 
			if ( impact == null ) { 
				continue;
			}
			int size = impact.getReports() == null ? 0: impact.getReports().size();
			System.out.printf("\n\n\nTitle: %s\nDescription: %s\nReport Count: %d\n", impact.getTitle(), impact.getSummary(), size);
			for ( SpatialReference ref : impact.getSpatialReferences() ) { 
				System.out.printf("Ref Type: %s\n", ref.getReferenceType());
			}
		}
		
		assertTrue("Invalid result size", impacts.size() > 0);
	}
	
	public void testAllStateQuery() throws Exception { 
		System.out.println("============= STATE TEST =============");
		DroughtImpactQuery impact_query = DroughtServiceAccessor.getInstance().getDroughtImpactQuery();		
		List<ImpactBean> impacts = impact_query.loadAllImpacts(impact_query.queryAllImpactsForState(USState.Nebraska));
		
		for ( ImpactBean impact : impacts ) { 
			if ( impact == null ) { 
				continue;
			}
			int size = impact.getReports() == null ? 0: impact.getReports().size();
			System.out.printf("\n\n\nTitle: %s\nDescription: %s\nReport Count: %d\n", impact.getTitle(), impact.getSummary(), size);
			for ( SpatialReference ref : impact.getSpatialReferences() ) { 
				System.out.printf("Ref Type: %s\n", ref.getReferenceType());
			}
		}
		
		assertTrue("Invalid result size", impacts.size() > 0);
	}
	
	public void testAllQuery() throws Exception { 
		System.out.println("============= ALL TEST =============");
		DroughtImpactQuery impact_query = DroughtServiceAccessor.getInstance().getDroughtImpactQuery();		
		
		DateTime end = new DateTime(System.currentTimeMillis());
		DateTime start = end.minusMonths(2);
		
		List<ImpactBean> impacts = impact_query.loadAllImpacts(impact_query.queryImpacts(start, end));
		
		for ( ImpactBean impact : impacts ) { 
			if ( impact == null ) { 
				continue;
			}
			int size = impact.getReports() == null ? 0: impact.getReports().size();
			System.out.printf("\n\n\nTitle: %s\nDescription: %s\nReport Count: %d\n", impact.getTitle(), impact.getSummary(), size);
			for ( SpatialReference ref : impact.getSpatialReferences() ) { 
				System.out.printf("Ref Type: %s\n", ref.getReferenceType());
			}
		}
		
		assertTrue("Invalid result size", impacts.size() > 0);
	}
	
	public void testDroughtServiceAccessor() throws Exception {
		DroughtServiceAccessor droughtServiceAccessor = DroughtServiceAccessor.getInstance();
		assertNotNull("DroughtServiceAccessor.getInstance()", droughtServiceAccessor);
		DroughtImpactQuery droughtImpactQuery = droughtServiceAccessor.getDroughtImpactQuery();
		assertNotNull("DroughtServiceAccessor.getDroughtImpactQuery()", droughtImpactQuery);
	}
	
	public void testUsStats() throws Exception { 
		DateTime now = new DateTime(System.currentTimeMillis());
		DateTime then = now.minusYears(200);
		
		DroughtServiceAccessor droughtServiceAccessor = DroughtServiceAccessor.getInstance();
		DroughtImpactQuery droughtImpactQuery = droughtServiceAccessor.getDroughtImpactQuery();
		
		DroughtImpactStatistics<USState> stats = droughtImpactQuery.lookupUSImpactStats(then, now);

		for ( USState state : stats ) {
			System.out.printf("State: %s, Reports: %d\n", state.name(), stats.getDivisionStatistics(state).getImpactCount());
			for ( DroughtReportCategory cat : stats.divisionCategories(state) ) { 
				System.out.printf("Category: %s, Reports: %d, Losses: %f\n", cat.getPrintName(), 
						stats.getDivisionCategoryCount(state, cat), stats.getDivisionCategoryLoss(state, cat));
			}
		}
	}
	
	public void testStateStats() throws Exception { 
		DateTime now = new DateTime(System.currentTimeMillis());
		DateTime then = now.minusYears(3);
		
		DroughtServiceAccessor droughtServiceAccessor = DroughtServiceAccessor.getInstance();
		DroughtImpactQuery droughtImpactQuery = droughtServiceAccessor.getDroughtImpactQuery();
		
		DroughtImpactStatistics<USCounty> stats = droughtImpactQuery.lookupStateImpactStats(USState.Nebraska, then, now);

		for ( USCounty county : stats ) {
			System.out.printf("County: %s, Reports: %d\n", county.toString(), stats.getDivisionStatistics(county).getImpactCount());
			for ( DroughtReportCategory cat : stats.divisionCategories(county) ) { 
				System.out.printf("Category: %s, Reports: %d, Losses: %f\n", cat.getPrintName(), 
						stats.getDivisionCategoryCount(county, cat), stats.getDivisionCategoryLoss(county, cat));
			}
		}
	}
	
	public void testStateStatsFromBounding() throws Exception { 
		DateTime end = new DateTime(2009,10,10,0,0,0,0,GregorianChronology.getInstance());
		DateTime start = end.minusWeeks(24);
		
		DroughtServiceAccessor droughtServiceAccessor = DroughtServiceAccessor.getInstance();
		DroughtImpactQuery droughtImpactQuery = droughtServiceAccessor.getDroughtImpactQuery();
		
		BoundingBox box = new BoundingBox(40.9069f, -97.0989f, 40.8f, -96.667f);
		
		DroughtImpactStatistics<USCounty> state_stats = droughtImpactQuery.lookupStateImpactStats(USState.Nebraska, start, end);
		DroughtImpactStatistics<USCounty> bounding_stats = droughtImpactQuery.lookupBoundedImpacts(box, start, end);

		USCounty county = SpatialServiceAccessor.getInstance().getSpatialQuery().getCountyForName("Lancaster", USState.Nebraska);

		assertEquals("Incorrect number of impact counts", state_stats.getDivisionImpactCount(county),
				bounding_stats.getDivisionImpactCount(county));
		assertEquals("Incorrect dominate county", state_stats.getDivisionStatistics(county).getDominantCategory(), 
				bounding_stats.getDivisionStatistics(county).getDominantCategory());
	}
}