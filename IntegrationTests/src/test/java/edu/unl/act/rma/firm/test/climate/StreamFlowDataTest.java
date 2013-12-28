package edu.unl.act.rma.firm.test.climate;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.management.JMException;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.climate.configuration.ClimateServiceAccessor;
import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.InvalidStateException;
import edu.unl.act.rma.firm.core.TemporalPeriod;
import edu.unl.act.rma.firm.streamflow.component.StreamFlowDataQuery;
import edu.unl.act.rma.firm.streamflow.component.StreamFlowMetaDataQuery;
import edu.unl.act.rma.firm.streamflow.component.StreamFlowSpatialExtension;

public class StreamFlowDataTest extends TestCase {
	public static final String alabama1="02339495";
	public static final String alabama2="02342933";
	public static final String rhode1="01115670";
	public static final String rhode2="01117000";
	
	public void testdailyStreamData1() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		StreamFlowDataQuery obj = manager.getStreamFlowDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(alabama1);
		DateTime start = new DateTime(2006, 9, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2006, 9, 5, 0, 0, 0, 0, GregorianChronology.getInstance());
		int missing_count= 0; 
		int range_count = 0; 
		int data_count = 0; 
		CalendarDataCollection coll = obj.getStreamFlowPeriodData(stations, start, end, DataType.DISCHARGE_MEAN, CalendarPeriod.DAILY);
		for ( String station : coll ) { 
			int count = 0; 
			for ( float[] year : coll.getStationData(station) ) { 
				count++;
				for ( int i=0; i<year.length; i++ ) { 
					if ( year[i] == DataType.MISSING )
						missing_count++;
					else if ( year[i] == DataType.OUTSIDE_OF_REQUEST_RANGE ) { 
						range_count++;
					} else {
						data_count++;
						if(station.equals(alabama1)){
							if((count==1)&&(i==244)){
								assertEquals("contains correct value",28.0f,year[i], 0.1f);
							}
							if((count==1)&&(i==245)){
								assertEquals("contains correct value",31.0f,year[i], 0.1f);
							}
							if((count==1)&&(i==248)){
								assertEquals("contains correct value",13.0f,year[i], 0.1f);
							}
						}
							
						}
					}
				}
			}
		assertEquals("contains valid number of values", 5, data_count);
		assertEquals("contains valid missing values", 0, missing_count);	
		assertEquals("contains valid outside range values", 361, range_count);	
	}
	
	public void testdailyStreamData2() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		StreamFlowDataQuery obj = manager.getStreamFlowDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(alabama2);
		DateTime start = new DateTime(2005, 3 , 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 9, 5, 0, 0, 0, 0, GregorianChronology.getInstance());
		int missing_count= 0; 
		int range_count = 0; 
		int data_count = 0; 
		CalendarDataCollection coll = obj.getStreamFlowPeriodData(stations, start, end, DataType.DISCHARGE_MEAN, CalendarPeriod.DAILY);
		for ( String station : coll ) { 
			int count = 0; 
			for ( float[] year : coll.getStationData(station) ) { 
				count++;
				for ( int i=0; i<year.length; i++ ) { 
					if ( year[i] == DataType.MISSING )
						missing_count++;
					else if ( year[i] == DataType.OUTSIDE_OF_REQUEST_RANGE ) { 
						range_count++;
					} else {
						data_count++;
						if(station.equals(alabama2)){
									
							if((count==1)&&(i==60)){
								assertEquals("contains correct value",141.0f,year[i], 0.1f);
							}
							if((count==1)&&(i==106)){
								assertEquals("contains correct value",74.0f,year[i], 0.1f);
							}
							if((count==1)&&(i==144)){
								assertEquals("contains correct value",18.0f,year[i], 0.1f);
							}
							if((count==1)&&(i==203)){
								assertEquals("contains correct value",52.0f,year[i], 0.1f);
							}
						}
							
						}
					}
				}
			}
		assertEquals("contains valid number of values", 189, data_count);
		assertEquals("contains valid missing values", 0, missing_count);	
		assertEquals("contains valid outside range values", 177, range_count);	
	}
	
	public void testAvailableDailyData() throws InstantiationException, JMException, RemoteException, InvalidArgumentException {		
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		StreamFlowDataQuery obj = manager.getStreamFlowDataQuery();
		StreamFlowSpatialExtension sq = ClimateServiceAccessor.getInstance().getStreamFlowSpatialExtension();
		List<String> stations = new ArrayList<String>();
		stations.add(rhode2);
		DateTime start = new DateTime(2005,3 , 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 9, 5, 0, 0, 0, 0, GregorianChronology.getInstance());
		StreamFlowMetaDataQuery meta = manager.getStreamFlowMetaDataQuery();
		TemporalPeriod longest_period = meta.getLongestPeriod(stations,
				CalendarPeriod.DAILY, DataType.DISCHARGE_MEAN);
		System.out.println(longest_period);
		
		CalendarDataCollection coll = obj.getDataNormals(stations, start.getYear(), end.getYear(), DataType.DISCHARGE_MEAN, CalendarPeriod.WEEKLY);
		for (float[] year : coll.getStationData(rhode2)) {
			for (int i = 0; i < year.length; i++) {
				System.out.print(year[i] + "\t");
			}
			System.out.println("");
		}
	}
}
