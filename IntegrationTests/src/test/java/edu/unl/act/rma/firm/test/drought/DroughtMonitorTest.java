/* Created on: May 18, 2010 */
package edu.unl.act.rma.firm.test.drought;

import java.util.List;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.core.component.SpatialQuery;
import edu.unl.act.rma.firm.core.configuration.SpatialServiceAccessor;
import edu.unl.act.rma.firm.core.spatial.Layer;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.drought.DroughtMonitorArea;
import edu.unl.act.rma.firm.drought.component.DroughtMonitorQuery;
import edu.unl.act.rma.firm.drought.configuration.DroughtServiceAccessor;

/**
 * @author Ian Cottingham
 *
 */
public class DroughtMonitorTest extends TestCase {

	public void testStateQuery() throws Exception { 
		DroughtMonitorQuery query = DroughtServiceAccessor.getInstance().getDroughtMonitorQuery();
		DroughtMonitorArea desc = query.queryStateDM(USState.Nebraska, 
				new DateTime(2008,11,1,0,0,0,0,GregorianChronology.getInstance()));
			
		
		assertEquals("Incorrect D0 percentage", 9.98, desc.getD0(), 0.01);
		assertEquals("Incorrect D1 percentage", 0, desc.getD1(), 0.01);
		assertEquals("Incorrect D2 percentage", 0, desc.getD2(), 0.01);
		assertEquals("Incorrect D3 percentage", 0, desc.getD3(), 0.01);
		assertEquals("Incorrect D4 percentage", 0, desc.getD4(), 0.01);
		assertEquals("Incorrect Unclassified percentage", 90.01, desc.getUnclassified(), 0.01);

	}
	
	public void testCountyQuery() throws Exception { 
		DroughtMonitorQuery query = DroughtServiceAccessor.getInstance().getDroughtMonitorQuery();
		SpatialQuery spatial_query = SpatialServiceAccessor.getInstance().getSpatialQuery();
		
		USCounty county = spatial_query.getCountyForName("Travis", USState.Texas);
		
		DroughtMonitorArea desc = query.queryCountyDM(county, new DateTime(2009,5,10,0,0,0,0,GregorianChronology.getInstance()));
		
		System.out.println("\n\n -- Test County Query --\n\n");
		
		System.out.println(county);
		System.out.printf("D0 - %f D1 - %f D2 - %f D3 - %f D4 - %f Unclassifie - %f\n", desc.getD0(), 
				desc.getD1(), desc.getD2(), desc.getD3(), desc.getD4(), desc.getUnclassified());
		
		//TODO: update assertions once Mark has responded about the data issue found for Travis County TX

/*		
		assertEquals("Incorrect D0 percentage", 9.98, desc.getD0(), 0.01);
		assertEquals("Incorrect D1 percentage", 0, desc.getD1(), 0.01);
		assertEquals("Incorrect D2 percentage", 0, desc.getD2(), 0.01);
		assertEquals("Incorrect D3 percentage", 0, desc.getD3(), 0.01);
		assertEquals("Incorrect D4 percentage", 0, desc.getD4(), 0.01);
		assertEquals("Incorrect Unclassified percentage", 90.01, desc.getUnclassified(), 0.01);
		*/
	}
	
	public void testStateSequenceQuery() throws Exception { 
		System.out.println("\n\n -- Test State Sequence Query --\n\n");
		
		DroughtMonitorQuery query = DroughtServiceAccessor.getInstance().getDroughtMonitorQuery();
		SpatialQuery spatial_query = SpatialServiceAccessor.getInstance().getSpatialQuery();
		
		USCounty county = spatial_query.getCountyForName("Travis", USState.Texas);
		
		List<DroughtMonitorArea> descriptors = query.queryCountyDMSequence(county, new DateTime(2009,5,10,0,0,0,0,GregorianChronology.getInstance()), 
				new DateTime(2009,12,10,0,0,0,0,GregorianChronology.getInstance()));

		System.out.println(county);
		for ( DroughtMonitorArea desc : descriptors ) {
			System.out.printf("D0 - %f D1 - %f D2 - %f D3 - %f D4 - %f Unclassifie - %f\n", desc.getD0(), 
					desc.getD1(), desc.getD2(), desc.getD3(), desc.getD4(), desc.getUnclassified());
		}
	}
	
	public void testCountySequenceQuery() throws Exception { 
		System.out.println("\n\n -- Test County Sequence Query --\n\n");
		
		DroughtMonitorQuery query = DroughtServiceAccessor.getInstance().getDroughtMonitorQuery();
			
		List<DroughtMonitorArea> descriptors = query.queryStateDMSequence(USState.California, new DateTime(2009,5,10,0,0,0,0,GregorianChronology.getInstance()), 
				new DateTime(2009,12,10,0,0,0,0,GregorianChronology.getInstance()));
		
		for ( DroughtMonitorArea desc : descriptors ) {
			System.out.printf("D0 - %f D1 - %f D2 - %f D3 - %f D4 - %f Unclassifie - %f\n", desc.getD0(), 
					desc.getD1(), desc.getD2(), desc.getD3(), desc.getD4(), desc.getUnclassified());
		}
	}
	
	public void testNationalQuery() throws Exception { 
		System.out.println("\n\n -- Test National Query --\n\n");
		
		DroughtMonitorQuery query = DroughtServiceAccessor.getInstance().getDroughtMonitorQuery();
		DroughtMonitorArea desc = query.queryNationalDM(
				new DateTime(2008,11,1,0,0,0,0,GregorianChronology.getInstance()));
		
		System.out.printf("D0 - %f D1 - %f D2 - %f D3 - %f D4 - %f Unclassifie - %f\n", desc.getD0(), 
				desc.getD1(), desc.getD2(), desc.getD3(), desc.getD4(), desc.getUnclassified());

	}
	
	public void testNationalSequence() throws Exception { 
		System.out.println("\n\n -- Test National Sequence Query --\n\n");
		
		DroughtMonitorQuery query = DroughtServiceAccessor.getInstance().getDroughtMonitorQuery();
			
		List<DroughtMonitorArea> descriptors = query.queryNationalDMSequence(new DateTime(2009,5,10,0,0,0,0,GregorianChronology.getInstance()), 
				new DateTime(2009,12,10,0,0,0,0,GregorianChronology.getInstance()));
		
		for ( DroughtMonitorArea desc : descriptors ) {
			System.out.printf("D0 - %f D1 - %f D2 - %f D3 - %f D4 - %f Unclassifie - %f\n", desc.getD0(), 
					desc.getD1(), desc.getD2(), desc.getD3(), desc.getD4(), desc.getUnclassified());
		}
	}
	
	public void testMapLayerGeneration() throws Exception { 
		DroughtMonitorQuery query = DroughtServiceAccessor.getInstance().getDroughtMonitorQuery();
		Layer l = query.getDroughtMonitorLayerForDate(new DateTime(2010,6,1,0,0,0,0,GregorianChronology.getInstance()));
		l.setFillOpacity(1);
		System.out.println(l.toKml());
	}
}
