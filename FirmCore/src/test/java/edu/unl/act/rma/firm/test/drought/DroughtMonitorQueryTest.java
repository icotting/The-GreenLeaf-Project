/* Created on: Jul 19, 2010 */
package edu.unl.act.rma.firm.test.drought;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.drought.component.DroughtMonitorQueryBean;

/**
 * @author Ian Cottingham
 *
 */
public class DroughtMonitorQueryTest extends TestCase {

	public void testDateRoundThisWeek() throws Exception { 

		DateTime now = new DateTime(System.currentTimeMillis());
		
		DateTime last_tuesday = now.minusWeeks(1).minusDays(now.getDayOfWeek() - DateTimeConstants.TUESDAY);
		DateTime this_tuesday =  now.minusDays(now.getDayOfWeek() - DateTimeConstants.TUESDAY);
				
		DateTime date;

		date = now.minusWeeks(1).minusDays(now.getDayOfWeek() - DateTimeConstants.FRIDAY);
		assertEquals("invalid date for Friday", last_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
		date = now.minusWeeks(1).minusDays(now.getDayOfWeek() - DateTimeConstants.SATURDAY);
		assertEquals("invalid date for Saturday", last_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
		date = now.minusWeeks(1).minusDays(now.getDayOfWeek() - DateTimeConstants.SUNDAY);
		assertEquals("invalid date for Sunday", last_tuesday, DroughtMonitorQueryBean.roundDMDate(date));		
		
		date = now.minusDays(now.getDayOfWeek() - DateTimeConstants.MONDAY);
		assertEquals("invalid date for Monday", last_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
		date = now.minusDays(now.getDayOfWeek() - DateTimeConstants.TUESDAY);
		assertEquals("invalid date for Tuesday", last_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
		date = now.minusDays(now.getDayOfWeek() - DateTimeConstants.WEDNESDAY);
		assertEquals("invalid date for Wednesday", last_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
		date = now.minusDays(now.getDayOfWeek() - DateTimeConstants.THURSDAY);
		assertEquals("invalid date for Thursday", this_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
		date = now.minusDays(now.getDayOfWeek() - DateTimeConstants.FRIDAY);
		assertEquals("invalid date for Friday", this_tuesday, DroughtMonitorQueryBean.roundDMDate(date));

		date = now.minusDays(now.getDayOfWeek() - DateTimeConstants.SATURDAY);
		assertEquals("invalid date for Saturday", this_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
		date = now.minusDays(now.getDayOfWeek() - DateTimeConstants.SUNDAY);
		assertEquals("invalid date for Sunday", this_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
	}
	
	public void testDateRoundPastWeek() throws Exception { 

		DateTime last_tuesday = new DateTime(2010,6,29,0,0,0,0,GregorianChronology.getInstance());
		DateTime this_tuesday = new DateTime(2010,7,6,0,0,0,0,GregorianChronology.getInstance());
		
		DateTime date;
		
		date = new DateTime(2010,7,2,0,0,0,0,GregorianChronology.getInstance());
		assertEquals("invalid date for Friday", last_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
		date = new DateTime(2010,7,3,0,0,0,0,GregorianChronology.getInstance());
		assertEquals("invalid date for Saturday", last_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
		date = new DateTime(2010,7,4,0,0,0,0,GregorianChronology.getInstance());
		assertEquals("invalid date for Sunday", last_tuesday, DroughtMonitorQueryBean.roundDMDate(date));		
		
		date = new DateTime(2010,7,5,0,0,0,0,GregorianChronology.getInstance());
		assertEquals("invalid date for Monday", last_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
		date = new DateTime(2010,7,6,0,0,0,0,GregorianChronology.getInstance());
		assertEquals("invalid date for Tuesday", this_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
		date = new DateTime(2010,7,7,0,0,0,0,GregorianChronology.getInstance());
		assertEquals("invalid date for Wednesday", this_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
		date = new DateTime(2010,7,8,0,0,0,0,GregorianChronology.getInstance());
		assertEquals("invalid date for Thursday", this_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
		date = new DateTime(2010,7,9,0,0,0,0,GregorianChronology.getInstance());
		assertEquals("invalid date for Friday", this_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
		date = new DateTime(2010,7,10,0,0,0,0,GregorianChronology.getInstance());
		assertEquals("invalid date for Saturday", this_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
		date = new DateTime(2010,7,11,0,0,0,0,GregorianChronology.getInstance());
		assertEquals("invalid date for Sunday", this_tuesday, DroughtMonitorQueryBean.roundDMDate(date));
		
	}
}
