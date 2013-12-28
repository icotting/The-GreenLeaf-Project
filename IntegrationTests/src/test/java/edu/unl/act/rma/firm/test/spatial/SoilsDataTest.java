/* Created On: Sep 12, 2005 */
package edu.unl.act.rma.firm.test.spatial;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

import javax.management.JMException;

import junit.framework.TestCase;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.drought.component.SoilsDataQuery;
import edu.unl.act.rma.firm.drought.configuration.DroughtServiceAccessor;
import edu.unl.act.rma.firm.test.StationList;

/**
 * @author Ian Cottingham
 *
 */
public class SoilsDataTest extends TestCase {
	

	public static final String ASHLAND_ID = StationList.ASHLAND_ID.getStationID();
	
	public void testAWCValue() throws InstantiationException, JMException, RemoteException { 
		SoilsDataQuery sdo = DroughtServiceAccessor.getInstance().getSoilsDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		Map<String, Float> awcs = sdo.getWaterHoldingCapacity(stations);
		assertEquals("correct AWC value returned", 11.26f, awcs.get(ASHLAND_ID));			
	}
	
	public void testCSM() throws InstantiationException, JMException, RemoteException { 
		SoilsDataQuery sdo = DroughtServiceAccessor.getInstance().getSoilsDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		
		DateTime date = new DateTime(1990, 12, 1, 0, 0, 0, 0);
		Map<String, Float> csms = sdo.getCurrentSoilMoisture(stations, date);
		assertEquals("correct CSM value returned", 10.151f, csms.get(ASHLAND_ID), 0.001);
	}
}
