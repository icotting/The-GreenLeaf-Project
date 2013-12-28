package edu.unl.act.rma.firm.test.drought;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.drought.DroughtImpactStatistics;
import edu.unl.act.rma.firm.drought.component.DroughtImpactQuery;
import edu.unl.act.rma.firm.drought.configuration.DroughtServiceAccessor;


public class ProfilingTest extends TestCase {

	public void testLargeStateQuery() throws Exception {
		DateTime end = new DateTime(2010,9,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime start = end.minusWeeks(24);
		
		DroughtServiceAccessor droughtServiceAccessor = DroughtServiceAccessor.getInstance();
		DroughtImpactQuery droughtImpactQuery = droughtServiceAccessor.getDroughtImpactQuery();
		
		DroughtImpactStatistics<USCounty> state_stats = droughtImpactQuery.lookupStateImpactStats(USState.Texas, start, end);
		
		System.out.printf("There were %d reports \n", 0);
		
	}
}
