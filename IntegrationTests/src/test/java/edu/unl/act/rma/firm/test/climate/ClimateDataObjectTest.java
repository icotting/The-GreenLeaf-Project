/* Created On: Sep 11, 2005 */
package edu.unl.act.rma.firm.test.climate;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.management.JMException;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.climate.TemperatureDayTypes;
import edu.unl.act.rma.firm.climate.VariableMetaData;
import edu.unl.act.rma.firm.climate.component.ClimateDataQuery;
import edu.unl.act.rma.firm.climate.component.ClimateMetaDataQuery;
import edu.unl.act.rma.firm.climate.component.ClimateSpatialExtension;
import edu.unl.act.rma.firm.climate.configuration.ClimateServiceAccessor;
import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.InvalidStateException;
import edu.unl.act.rma.firm.core.MetaDataCollection;
import edu.unl.act.rma.firm.core.StationMetaDataType;
import edu.unl.act.rma.firm.core.TemporalPeriod;
import edu.unl.act.rma.firm.core.YearDataBuilder;
import edu.unl.act.rma.firm.core.spatial.Site;
import edu.unl.act.rma.firm.core.spatial.SiteList;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.test.StationList;



/**
 * <b>NOTE:</b> This test case must be executed with a valid FIRM_HOME system variable set, 
 * the value provided for FIRM_HOME must reference a running JBoss server.
 * 
 * @author Ian Cottingham
 *
 */
public class ClimateDataObjectTest extends TestCase {
	
	public static final String ASHLAND_ID = StationList.ASHLAND_ID.getStationID();
	public static final String PROVIDENCE_ID = StationList.PROVIDENCE_ID.getStationID();
	public static final String INCOMPLETE_ID = StationList.INCOMPLETE_ID.getStationID();				
	public static final String SAINT_BERNARD = StationList.SAINT_BERNARD.getStationID();
	public static final String ADAMS_BEACH = StationList.ADAMS_BEACH.getStationID();		
	public static final String ASHLAND2_ID = StationList.ASHLAND2_ID.getStationID();			
	public static final String ARTHUR = StationList.ARTHUR.getStationID();			
	public static final String DANBURY = StationList.DANBURY.getStationID();
	public static final String KRAMER = StationList.KRAMER.getStationID();		
	public static final String KEARNEY4NE = StationList.KEARNEY4NE.getStationID();
	public static final String COCHRANE = StationList.COCHRANE.getStationID();
	
	public void testgetPercentageNormaldaily1() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(PROVIDENCE_ID);
		stations.add(ASHLAND_ID);
		DateTime start = new DateTime(2002, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2003, 1, 15, 0, 0, 0, 0, GregorianChronology.getInstance());
		int missing_count= 0; 
		int range_count = 0; 
		int data_count = 0; 
		
		CalendarDataCollection coll = obj.getPercentageNormal(stations, DataType.PRECIP,  CalendarPeriod.DAILY ,start, end);
		for ( String station : coll ) { 
			int count = 0; 
			for ( float[] year : coll.getStationData(station) ) { 
				count++;
				for ( int i=0; i<year.length; i++ ) { 
					if ( year[i] == DataType.MISSING )
						missing_count++;
					else if ( year[i] == DataType.OUTSIDE_OF_RANGE ) { 
						range_count++;
					} else {
						data_count++;
						if(station.equals(PROVIDENCE_ID)){
							if((count==2)&&(i==0)){
								assertEquals("contains correct value",4.0f , year[i], 0.1f);
							}
							if((count==2)&&(i==14)){
								assertEquals("contains correct value",0.0f , year[i], 0.1f);
							}
							if((count==1)&&(i==365)){
								assertEquals("contains correct value",0.88f , year[i], 0.1f);
							}
						}
						if(station.equals(ASHLAND_ID)){
							if((count==2)&&(i==0)){
								assertEquals("contains correct value",0.0f , year[i], 0.1f);
							}
							if((count==2)&&(i==14)){
								assertEquals("contains correct value",0.0f , year[i], 0.1f);
							}
							if((count==1)&&(i==365)){
								assertEquals("contains correct value",0.0f, year[i], 0.1f);
							}
						}
					}
				}
			}
		}
	}
	
	public void testgetPercentageNormaldaily2() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(PROVIDENCE_ID);
		DateTime start = new DateTime(2003,4 , 15, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2003, 6, 15, 0, 0, 0, 0, GregorianChronology.getInstance());
		int missing_count= 0; 
		int range_count = 0; 
		int data_count = 0; 
		
		CalendarDataCollection coll = obj.getPercentageNormal(stations, DataType.HIGH_TEMP,  CalendarPeriod.DAILY ,start, end);
		for ( String station : coll ) { 
			int count = 0; 
			for ( float[] year : coll.getStationData(station) ) { 
				count++;
				for ( int i=0; i<year.length; i++ ) { 
					if ( year[i] == DataType.MISSING )
						missing_count++;
					else if ( year[i] == DataType.OUTSIDE_OF_RANGE ) { 
						range_count++;
					} else {
						data_count++;
						if(station.equals(PROVIDENCE_ID)){
							if((count==1)&&(i==106)){
								assertEquals("contains correct value",1.5199999809265137f, year[i], 0.1f);
							}
							if((count==1)&&(i==111)){
								assertEquals("contains correct value",1.0f, year[i], 0.1f);
							}
							if((count==1)&&(i==153)){
								assertEquals("contains correct value",0.9800000190734863f, year[i], 0.1f);
							}
						}
					}
				}
			}
			assertEquals("contains valid number of values", 366, data_count);
			assertEquals("contains valid missing values", 0, missing_count);	
		}
	}
	
	public void testgetPercentageNormalmonthly1() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(PROVIDENCE_ID);
		DateTime start = new DateTime(2003,4 , 15, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2004, 12, 15, 0, 0, 0, 0, GregorianChronology.getInstance());
		int missing_count= 0; 
		int range_count = 0; 
		int data_count = 0; 
		
		CalendarDataCollection coll = obj.getPercentageNormal(stations, DataType.NORMAL_TEMP,  CalendarPeriod.MONTHLY ,start, end);
		for ( String station : coll ) { 
			int count = 0; 
			for ( float[] year : coll.getStationData(station) ) { 
				count++;
				for ( int i=0; i<year.length; i++ ) { 
					if ( year[i] == DataType.MISSING ){
						missing_count++;
					}else if ( year[i] == DataType.OUTSIDE_OF_RANGE ) { 
						range_count++;
					} else {
						data_count++;
						if(station.equals(PROVIDENCE_ID)){
							if((count==1)&&(i==6)){
								assertEquals("contains correct value",1.0099999904632568f, year[i], 0.1f);
							}
							if((count==1)&&(i==11)){
								assertEquals("contains correct value",1.090000033378601f, year[i], 0.1f);
							}
							if((count==2)&&(i==5)){
								assertEquals("contains correct value",0.9900000095367432f, year[i], 0.1f);
							}
							if((count==2)&&(i==10)){
								assertEquals("contains correct value",0.9900000095367432f, year[i], 0.1f);
							}
						}
					}
				}
			}
			assertEquals("contains valid number of values", 24, data_count);
			assertEquals("contains valid missing values", 0, missing_count);	
		}
	}
	
	
	public void testgetPercentageNormalmonthly2() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(PROVIDENCE_ID);
		DateTime start = new DateTime(2003,6 , 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 2, 5, 0, 0, 0, 0, GregorianChronology.getInstance());
		int missing_count= 0; 
		int range_count = 0; 
		int data_count = 0; 
		
		CalendarDataCollection coll = obj.getPercentageNormal(stations, DataType.PRECIP,  CalendarPeriod.MONTHLY ,start, end);
		for ( String station : coll ) { 
			int count = 0; 
			for ( float[] year : coll.getStationData(station) ) { 
				count++;
				for ( int i=0; i<year.length; i++ ) { 
					if ( year[i] == DataType.MISSING )
						missing_count++;
					else if ( year[i] == DataType.OUTSIDE_OF_RANGE ) { 
						range_count++;
					} else {
						data_count++;
						if(station.equals(PROVIDENCE_ID)){
							if((count==1)&&(i==5)){
								assertEquals("contains correct value",1.75f, year[i], 0.1f);
							}
							if((count==1)&&(i==6)){
								assertEquals("contains correct value",1.25f, year[i], 0.1f);
							}
							if((count==2)&&(i==5)){
								assertEquals("contains correct value",0.46000000834465027f, year[i], 0.1f);
							}
							if((count==2)&&(i==10)){
								assertEquals("contains correct value",0.9700000286102295f, year[i], 0.1f);
							}
							if((count==3)&&(i==0)){
								assertEquals("contains correct value",1.190000057220459f, year[i], 0.1f);
							}
							if((count==3)&&(i==1)){
								assertEquals("contains correct value",0.9700000286102295f, year[i], 0.1f);
							}
						}
					}
				}
			}
			assertEquals("contains valid number of values", 36, data_count);
			assertEquals("contains valid missing values", 0, missing_count);	
		}
	}
	
	public void testgetPercentageNormalweekly1() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(PROVIDENCE_ID);
		DateTime start = new DateTime(2003,6 , 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 2, 5, 0, 0, 0, 0, GregorianChronology.getInstance());
		int missing_count= 0; 
		int range_count = 0; 
		int data_count = 0; 
		
		CalendarDataCollection coll = obj.getPercentageNormal(stations, DataType.PRECIP,  CalendarPeriod.WEEKLY ,start, end);
		for ( String station : coll ) { 
			int count = 0; 
			for ( float[] year : coll.getStationData(station) ) { 
				count++;
				for ( int i=0; i<year.length; i++ ) { 
					if ( year[i] == DataType.MISSING )
						missing_count++;
					else if ( year[i] == DataType.OUTSIDE_OF_RANGE ) { 
						range_count++;
					} else {
						data_count++;
						if(station.equals(PROVIDENCE_ID)){
							if((count==1)&&(i==21)){
								assertEquals("contains correct value",2.9800000190734863f,year[i], 0.1f);
							}
							if((count==1)&&(i==30)){
								assertEquals("contains correct value",1.559999942779541f,year[i], 0.1f);
							}
							if((count==2)&&(i==23)){
								assertEquals("contains correct value",0.0f, year[i], 0.1f);
							}
							if((count==2)&&(i==37)){
								assertEquals("contains correct value",2.75f, year[i], 0.1f);
							}
							if((count==3)&&(i==0)){
								assertEquals("contains correct value",1.350000023841858f, year[i], 0.1f);
							}
							if((count==3)&&(i==3)){
								assertEquals("contains correct value",0.18000000715255737f, year[i], 0.1f);
							}
							
						}
					}
				}
			}
			assertEquals("contains valid number of values", 156, data_count);
			assertEquals("contains valid missing values", 0, missing_count);	
		}
	}
	
	public void testgetPercentageNormalweekly2() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(PROVIDENCE_ID);
		DateTime start = new DateTime(2002,6 , 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2002, 12, 5, 0, 0, 0, 0, GregorianChronology.getInstance());
		int missing_count= 0; 
		int range_count = 0; 
		int data_count = 0; 
		
		CalendarDataCollection coll = obj.getPercentageNormal(stations, DataType.NORMAL_TEMP ,  CalendarPeriod.WEEKLY ,start, end);
		for ( String station : coll ) { 
			int count = 0; 
			for ( float[] year : coll.getStationData(station) ) { 
				count++;
				for ( int i=0; i<year.length; i++ ) { 
					if ( year[i] == DataType.MISSING )
						missing_count++;
					else if ( year[i] == DataType.OUTSIDE_OF_RANGE ) { 
						range_count++;
					} else {
						data_count++;
						if(station.equals(PROVIDENCE_ID)){
							if((count==1)&&(i==24)){
								assertEquals("contains correct value",1.0f,year[i], 0.1f);
							}
							if((count==1)&&(i==30)){
								assertEquals("contains correct value",1.1100000143051147f,year[i], 0.1f);
							}
							if((count==1)&&(i==23)){
								assertEquals("contains correct value",0.8999999761581421f, year[i], 0.1f);
							}
							if((count==1)&&(i==41)){
								assertEquals("contains correct value",0.949999988079071f, year[i], 0.1f);
							}
							if((count==1)&&(i==48)){
								assertEquals("contains correct value",0.75f, year[i], 0.1f);
							}
						}
					}
				}
			}
			assertEquals("contains valid number of values", 52, data_count);
			assertEquals("contains valid missing values", 0, missing_count);	
			
		}
		
	}
		
	public void testWeeklyPrePostRequired2() throws InstantiationException, JMException, RemoteException, InvalidArgumentException {			
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(PROVIDENCE_ID);
		DateTime start = new DateTime(1878, 5, 3, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		DateTime end = new DateTime(2005, 9, 15, 0, 0, 0, 0, GregorianChronology.getInstance());
	
		obj.getPeriodData(stations, start, end, DataType.PRECIP, CalendarPeriod.WEEKLY);
	}
	
	public void testWeeklyPrePostRequired() throws InstantiationException, JMException, RemoteException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(PROVIDENCE_ID);
		DateTime start = new DateTime(1878, 5, 3, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 9, 15, 0, 0, 0, 0, GregorianChronology.getInstance());
				
		obj.getPeriodData(stations, start, end, DataType.PRECIP, CalendarPeriod.WEEKLY);
	}
	
	public void testDailyPrePostRequired() throws InstantiationException, JMException, RemoteException, InvalidArgumentException {	
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(PROVIDENCE_ID);
		DateTime start = new DateTime(1878, 5, 3, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		DateTime end = new DateTime(2005, 9, 15, 0, 0, 0, 0, GregorianChronology.getInstance());
	
		obj.getPeriodData(stations, start, end, DataType.PRECIP, CalendarPeriod.DAILY);
	}
	
	public void testWeeklyData() throws InstantiationException, JMException, RemoteException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(INCOMPLETE_ID);
		DateTime start = new DateTime(1944, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(1950, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		int data_count =0 , missing_count = 0, range_count = 0;
	
		CalendarDataCollection coll = obj.getPeriodData(stations, start, end, DataType.PRECIP, CalendarPeriod.WEEKLY);
		for ( String station : coll ) { 
			for ( float[] year : coll.getStationData(station) ) { 
				for ( int i=0; i<52; i++ ) { 
					if ( year[i] == DataType.MISSING )
						missing_count++;
					else if ( year[i] == DataType.OUTSIDE_OF_RANGE || year[i] == DataType.OUTSIDE_OF_REQUEST_RANGE ) { 
						range_count++;
					} else {
						data_count++;
					}
				}
			}
		}
		
		assertEquals("contains valid number of outside range values", 95, range_count);
		assertEquals("contains valid number of values", 289, data_count);
		assertEquals("contains valid missing values", 32, missing_count);	
	}
	
	public void testGDDdaily() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateDataQuery obj = ClimateServiceAccessor.getInstance().getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ARTHUR);
		DateTime start = new DateTime(2000, 5, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2000, 7, 20, 0, 0, 0, 0, GregorianChronology.getInstance());
		
        	CalendarDataCollection result = obj.getGrowingDegreeDays(stations, CalendarPeriod.DAILY, start, end, 50, 86);
        	float[][] data = result.getDataMatrix(ARTHUR);
        	
        	assertEquals("incorrect 05/01/2000 value", 8.0, data[0][121], 0.1);
        	assertEquals("incorrect 06/08/2000 value", 23.3, data[0][159], 0.1);
        	assertEquals("incorrect 07/19/2000 value", 19.3, data[0][200], 0.1);
	}

	public void testGDDdailyMultiyear() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateDataQuery obj = ClimateServiceAccessor.getInstance().getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ARTHUR);
		DateTime start = new DateTime(2000, 5, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2003, 7, 20, 0, 0, 0, 0, GregorianChronology.getInstance());
		
        	CalendarDataCollection result = obj.getGrowingDegreeDays(stations, CalendarPeriod.DAILY, start, end, 50, 86);
        	float[][] data = result.getDataMatrix(ARTHUR);
        	
        	assertEquals("incorrect 05/01/2000 value", 8.0, data[0][121], 0.1);
        	assertEquals("incorrect 06/08/2000 value", 23.3, data[0][159], 0.1);
        	assertEquals("incorrect 07/19/2000 value", 19.3, data[0][200], 0.1);
        	assertEquals("incorrect 03/14/2001 value", 4.35, data[1][74], 0.1);
        	assertEquals("incorrect 06/01/2001 value", 14.57, data[1][153], 0.1);
	}
	
	public void testGDDweekly() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateDataQuery obj = ClimateServiceAccessor.getInstance().getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		DateTime start = new DateTime(2000, 1, 8, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2000, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
        	CalendarDataCollection result = obj.getGrowingDegreeDays(stations, CalendarPeriod.WEEKLY, start, end, 50, 86);
        	float[][] data = result.getDataMatrix(ASHLAND_ID);
        	
        	assertEquals("incorrect week 1 value", 1.5, data[0][0], 0.1);
        	assertEquals("incorrect week 33 value", 157.5, data[0][32], 0.1);
        	assertEquals("incorrect week 44 value", 62.5, data[0][43], 0.1);
	}
	
	public void testGDDWeeklyMultiStation() throws Exception {
		ClimateDataQuery obj = ClimateServiceAccessor.getInstance().getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		stations.add(PROVIDENCE_ID);
		stations.add(SAINT_BERNARD);
		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2001, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());

		CalendarDataCollection result = obj.getGrowingDegreeDays(stations, CalendarPeriod.WEEKLY, start, end, 50, 86);
		
		// assert constants for all stations
		for (String station : stations) {
			float[][] data = result.getDataMatrix(station);
			assertEquals("not enough years of data", 3, data.length);
			assertEquals("not enough week values", CalendarPeriod.WEEKLY.getLength(), data[0].length);
		}
		
		// assert missing values for Providence
		int missing_count = 0;
		int error_count = 0;
		int outside_range_count = 0;
		int valid_count = 0;
		
		for (float[] row : result.getDataMatrix(PROVIDENCE_ID)) {
			for (float d : row) {
				if (d == DataType.MISSING || d == DataType.NONEXISTANT) {
					missing_count++;
				} else if (d == DataType.ERROR_RESULT) {
					error_count++;
				} else if (d == DataType.OUTSIDE_OF_RANGE || d == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
					outside_range_count++;
				} else {
					valid_count++;
				}
			}
		}
			
		assertEquals("wrong number of missing values", 1, missing_count);
		assertEquals("wrong number of error values", 0, error_count);
		assertEquals("wrong number of outside range values", 51, outside_range_count);
		assertEquals("wrong number of valid values", 104, valid_count);
		
	    float[][] data = result.getDataMatrix(ASHLAND_ID);
	    assertEquals("incorrect week 2 value", 1.5, data[1][0], 0.1);
	    assertEquals("incorrect week 33 value", 196.5, data[1][31], 0.1);
	    assertEquals("incorrect week 45 value", 62.5, data[1][43], 0.1);
	}
	
	public void testGDDmonthly() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateDataQuery obj = ClimateServiceAccessor.getInstance().getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2000, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
        	CalendarDataCollection result = obj.getGrowingDegreeDays(stations, CalendarPeriod.MONTHLY, start, end, 50, 86);
        	float[][] data = result.getDataMatrix(ASHLAND_ID);
        	
        	assertEquals("incorrect month 1 value", 8.5, data[0][0], 0.1);
        	assertEquals("incorrect month 5 value", 609.5, data[0][5], 0.1);
        	assertEquals("incorrect month 11 value", 4.0, data[0][11], 0.1);
	}
	
	public void testGDDmonthlyMultiyear() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateDataQuery obj = ClimateServiceAccessor.getInstance().getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ARTHUR);
		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2001, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
        	CalendarDataCollection result = obj.getGrowingDegreeDays(stations, CalendarPeriod.MONTHLY, start, end, 50, 86);
        	float[][] data = result.getDataMatrix(ARTHUR);
        	
        	assertEquals("incorrect month 2, year 1 value", 65.59, data[0][1], 0.1);
        	assertEquals("incorrect month 8, year 1 value", 707.3, data[0][7], 0.1);
        	assertEquals("incorrect month 3, year 2 value", 56.5, data[1][2], 0.1);
        	assertEquals("incorrect month 10, year 2 value", 220.64, data[1][9], 0.1);
	}
	
	public void testGDDAnnually() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateDataQuery obj = ClimateServiceAccessor.getInstance().getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2000, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
        CalendarDataCollection result = obj.getGrowingDegreeDays(stations, CalendarPeriod.MONTHLY, start, end, 50, 86);
        int annual_gdd = 0;
        for ( double d : result.getDataMatrix(ASHLAND_ID)[0] ) { 
        	annual_gdd += d;
        }
        
        result = obj.getGrowingDegreeDays(stations, CalendarPeriod.ANNUALLY, start, end, 50, 86);
        
        assertEquals("invalid annual value", annual_gdd, result.getDataMatrix(ASHLAND_ID)[0][0], 0.1);
	}
	
	public void testFFPmultistation() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		stations.add(ARTHUR);
		stations.add(PROVIDENCE_ID);
		
	    CalendarDataCollection coll=obj.getFrostFreePeriod(stations, 2000, 2000);
		float[][] data = coll.getDataMatrix(ASHLAND_ID);
		
		assertEquals("incorrect number of results", 1, data.length);
		assertEquals("contains valid number of values", 1, data[0].length);
		assertEquals("incorrect 2000 Ashland FFP", 179.0, data[0][0], 0.1);
	}
	
	public void testFFP() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		int start1 = 2000;
		int end1 = 2000;
		int data_count=0 , missing_count = 0 ;  
	    CalendarDataCollection coll=obj.getFrostFreePeriod(stations,start1,end1);
		float[][] data = coll.getDataMatrix(ASHLAND_ID);
		for ( String station : coll ) { 
			for ( float[] year : coll.getStationData(station) ) { 
				for ( int i=0; i<1; i++ ) { 
					if ( year[i] == DataType.MISSING ){
						missing_count++;
					}else{
						data_count++;
						assertEquals("Value of FPP ", 179.0f, year[i], 0.1f);
					}
				}
			}
		}

		assertEquals("contains correct number of years", 1, data.length);
		assertEquals("contains valid number of values", 1, data_count);
		assertEquals("contains valid missing values", 0, missing_count);
	}
	
	public void testFFPmultiyear() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		int start1 = 1991;
		int end1 = 2000;
		int data_count=0 , missing_count = 0 ;  
	    CalendarDataCollection coll=obj.getFrostFreePeriod(stations,start1,end1);
	    int cntofyear=0 ; 
		float[][] data = coll.getDataMatrix(ASHLAND_ID);
		for ( String station : coll ) { 
			for ( float[] year : coll.getStationData(station) ) { 
				for ( int i=0; i<1; i++ ) { 
					if ( year[i] == DataType.MISSING ){
						missing_count++;
					}else{
						if(cntofyear==1){
							assertEquals("contains correct value", 177.0f, year[i], 0.1f);
						}
						if(cntofyear==3){
							assertEquals("contains correct value", 201.0f, year[i], 0.1f);
						}
						if(cntofyear==5){
							assertEquals("contains correct value", 204.0f, year[i], 0.1f);
						}
						if(cntofyear==7){
						   	assertEquals("contains correct value", 216.0f, year[i], 0.1f);
						}
						if(cntofyear==8){
							assertEquals("contains correct value", 206.0f, year[i], 0.1f);
						}
						if(cntofyear==9){
							assertEquals("contains correct value", 179.0f, year[i], 0.1f);
						}
							
						data_count++;
					}
				}
				cntofyear++;
			}
			
		}
    
		assertEquals("contains correct number of years", 10, data.length);
		assertEquals("contains valid number of values", 10, data_count);
		assertEquals("contains valid missing values", 0, missing_count);
	}
	
	public void testFFParthur() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ARTHUR);
		int start1 = 1989;
		int end1 = 1989;
		int data_count=0 , missing_count = 0 ;  
	    CalendarDataCollection coll=obj.getFrostFreePeriod(stations,start1,end1);
		
		for ( String station : coll ) { 
			for ( float[] year : coll.getStationData(station) ) { 
				for ( int i=0; i<1; i++ ) { 
					if ( year[i] == DataType.MISSING ){
						missing_count++;
					}else{
						data_count++;
						assertEquals("Value of FPP ", 139.0f, year[i], 0.1f);
					}
				}
			}
		}

		assertEquals("contains valid number of values", 1, data_count);
		assertEquals("contains valid missing values", 0, missing_count);
	}
	
	public void testFFPmultiyearArthur() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ARTHUR);
		int start1 = 1992;
		int end1 = 2001;
		int data_count=0 , missing_count = 0 ;  
	    CalendarDataCollection coll=obj.getFrostFreePeriod(stations,start1,end1);
	    int cntofyear=0 ; 
		for ( String station : coll ) { 
			for ( float[] year : coll.getStationData(station) ) { 
				for ( int i=0; i<1; i++ ) { 
					if ( year[i] == DataType.MISSING ){
						missing_count++;
					}else{
						if(cntofyear==0){
							assertEquals("contains correct value", 170.0f, year[i], 0.1f);
						}
						if(cntofyear==3){
							assertEquals("contains correct value", 146.0f, year[i], 0.1f);
						}
						if(cntofyear==5){
							assertEquals("contains correct value", 190.0f, year[i], 0.1f);
						}
						if(cntofyear==7){
						   	assertEquals("contains correct value", 167.0f, year[i], 0.1f);
						}
						if(cntofyear==8){
							assertEquals("contains correct value", 144.0f, year[i], 0.1f);
						}
						if(cntofyear==9){
							assertEquals("contains correct value", 174.0f, year[i], 0.1f);
						}
						data_count++;
					}
				}
				cntofyear++;
			}
			
		}
    
		assertEquals("contains valid number of values", 10, data_count);
		assertEquals("contains valid missing values", 0, missing_count);
	}
	
	public void testTempDaysAnnualHighGreaterThan() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateDataQuery obj = ClimateServiceAccessor.getInstance().getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2004, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
        	CalendarDataCollection result = obj.getTemperatureDays(stations, start, end, CalendarPeriod.ANNUALLY, TemperatureDayTypes.HIGH_GREATER_THAN, 20);
        	
        	float[][] data = result.getDataMatrix(ASHLAND_ID);
        	assertEquals("incorrect 2000 value", 348, data[0][0], 0.1);
        	assertEquals("incorrect 2002 value", 358, data[2][0], 0.1);
        	assertEquals("incorrect 2004 value", 347, data[4][0], 0.1);
	}
	
	public void testTempDaysMonthlyHighGreaterThan() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateDataQuery obj = ClimateServiceAccessor.getInstance().getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		DateTime start = new DateTime(1995, 3, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2004, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
        	CalendarDataCollection result = obj.getTemperatureDays(stations, start, end, CalendarPeriod.MONTHLY, TemperatureDayTypes.HIGH_GREATER_THAN, 20);
        	
        	float[][] data = result.getDataMatrix(ASHLAND_ID);
        	assertEquals("incorrect March 1995 value", 29, data[0][2], 0.1);
        	assertEquals("incorrect June 2000 value", 30, data[5][5], 0.1);
        	assertEquals("incorrect December 2004 value", 29, data[9][11], 0.1);
	}
	
	public void testTempDaysWeeklyHighGreaterThan() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateDataQuery obj = ClimateServiceAccessor.getInstance().getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		DateTime start = new DateTime(1995, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(1996, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
        	CalendarDataCollection result = obj.getTemperatureDays(stations, start, end, CalendarPeriod.WEEKLY, TemperatureDayTypes.HIGH_GREATER_THAN, 20);
        	
        	float[][] data = result.getDataMatrix(ASHLAND_ID);
        	assertEquals("incorrect week 2, 1995 value", 3, data[1][0], 0.1);
        	assertEquals("incorrect week 50, 1995 value", 6, data[1][49], 0.1);
        	assertEquals("incorrect week 35, 1996 value", 7, data[2][34], 0.1);
	}
	
	public void testTempDaysHighLessThan() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateDataQuery obj = ClimateServiceAccessor.getInstance().getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2004, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
        	CalendarDataCollection result = obj.getTemperatureDays(stations, start, end, CalendarPeriod.MONTHLY, TemperatureDayTypes.HIGH_LESS_THAN, 32);
        	
        	float[][] data = result.getDataMatrix(ASHLAND_ID);
        	assertEquals("incorrect February 2000 value", 5, data[0][1], 0.1);
        	assertEquals("incorrect June 2002 value", 0, data[2][5], 0.1);
        	assertEquals("incorrect December 2004 value", 6, data[4][11], 0.1);
	}
	
	public void testTempDaysLowGreaterThan() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateDataQuery obj = ClimateServiceAccessor.getInstance().getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ARTHUR);
		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2004, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
        	CalendarDataCollection result = obj.getTemperatureDays(stations, start, end, CalendarPeriod.MONTHLY, TemperatureDayTypes.LOW_GREATER_THAN, 32);
        	
        	float[][] data = result.getDataMatrix(ARTHUR);
        	assertEquals("incorrect February 2000 value", 3, data[0][1], 0.1);
        	assertEquals("incorrect June 2002 value", 30, data[2][5], 0.1);
        	assertEquals("incorrect December 2004 value", 0, data[4][11], 0.1);
	}
	
	public void testTempDaysLowLessThan() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateDataQuery obj = ClimateServiceAccessor.getInstance().getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ARTHUR);
		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2004, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
        	CalendarDataCollection result = obj.getTemperatureDays(stations, start, end, CalendarPeriod.MONTHLY, TemperatureDayTypes.LOW_LESS_THAN, 32);
        	
        	float[][] data = result.getDataMatrix(ARTHUR);
        	assertEquals("incorrect February 2000 value", 26, data[0][1], 0.1);
        	assertEquals("incorrect April 2002 value", 12, data[2][3], 0.1);
        	assertEquals("incorrect November 2004 value", 24, data[4][10], 0.1);
	}
	
	public void testTempDaysMultistation() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateDataQuery obj = ClimateServiceAccessor.getInstance().getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ARTHUR);
		stations.add(ASHLAND_ID);
		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2004, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
        	CalendarDataCollection result = obj.getTemperatureDays(stations, start, end, CalendarPeriod.MONTHLY, TemperatureDayTypes.LOW_GREATER_THAN, 32);
        	
        	float[][] data = result.getDataMatrix(ARTHUR);
        	assertEquals("incorrect February 2000 value", 3, data[0][1], 0.1);
        	assertEquals("incorrect June 2002 value", 30, data[2][5], 0.1);
        	assertEquals("incorrect December 2004 value", 0, data[4][11], 0.1);
        	
        	data = result.getDataMatrix(ASHLAND_ID);
        	assertEquals("incorrect February 2000 value", 6, data[0][1], 0.1);
        	assertEquals("incorrect June 2002 value", 30, data[2][5], 0.1);
        	assertEquals("incorrect December 2004 value", 2, data[4][11], 0.1);
	}
	
	public void testgetAnnualExtremeHighTemp1() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		int data_count=0 , missing_count = 0 ;  
		CalendarDataCollection coll=obj.getAnnualExtremeTemp(stations,1990,1991,DataType.HIGH_TEMP);
	    int cntofyear= 0 ;
		for ( String station : coll ) { 
			for ( float[] year : coll.getStationData(station) ) { 
				for ( int i=0; i<1; i++ ) { 
					if ( year[i] == DataType.MISSING ){
						missing_count++;
					}else{
						data_count++;
						if(cntofyear==0){
							assertEquals("contains correct value", 106.0f, year[i], 0.1f);
						}
						if(cntofyear==1){
							assertEquals("contains correct value", 99.0f, year[i], 0.1f);
						}
					}
				}
				cntofyear++;
			}
		}
		assertEquals("contains valid number of values", 2, data_count);
		assertEquals("contains valid missing values", 0, missing_count);
	}
	
	
	public void testgetAnnualExtremeLowTemp1() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		int data_count=0 , missing_count = 0 ;  
		CalendarDataCollection coll=obj.getAnnualExtremeTemp(stations,1981,1985,DataType.LOW_TEMP);
		int cntofyear=0;
		for ( String station : coll ) { 
			for ( float[] year : coll.getStationData(station) ) { 
				for ( int i=0; i<1; i++ ) { 
					if ( year[i] == DataType.MISSING ){
						missing_count++;
					}else{
						if(cntofyear==0){
							assertEquals("contains correct value ", -20.0f, year[i]);
						}
						if(cntofyear==1){
							assertEquals("contains correct value ", -20.0f, year[i]);
						}
						if(cntofyear==2){
							assertEquals("contains correct value ", -26.0f, year[i]);
						}
						if(cntofyear==3){
							assertEquals("contains correct value ", -16.0f, year[i]);
						}
						if(cntofyear==4){
							assertEquals("contains correct value ", -15.0f, year[i]);
						}
						data_count++;
					}
				}
				cntofyear++;
			}
		}
		assertEquals("contains valid number of values", 5, data_count);
		assertEquals("contains valid missing values", 0, missing_count);
	}
	
	public void testSingleYearWeeklyStart53() throws InstantiationException, JMException, RemoteException, InvalidArgumentException {		
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		DateTime start = new DateTime(2005, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		DateTime end = new DateTime(2005, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
	
		CalendarDataCollection col = obj.getPeriodData(stations, start, end, DataType.PRECIP, CalendarPeriod.WEEKLY);
		float[][] data = col.getDataMatrix(ASHLAND_ID);
		
		assertEquals("contains correct number of years", 2, data.length);
		assertEquals("contains correct number of periods", 52, data[0].length);
	}
	
	public void testSingleYearWeeklyEnds53() throws InstantiationException, JMException, RemoteException, InvalidArgumentException {	
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		DateTime start = new DateTime(2004, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		DateTime end = new DateTime(2005, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
	
		CalendarDataCollection col = obj.getPeriodData(stations, start, end, DataType.PRECIP, CalendarPeriod.WEEKLY);
		float[][] data = col.getDataMatrix(ASHLAND_ID);
		
		assertEquals("contains correct number of years", 2, data.length);
		assertEquals("contains correct number of periods", 52, data[0].length);
	}
		
	public void testMonthlyData() throws InstantiationException, JMException, RemoteException, InvalidArgumentException {
			ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
			ClimateDataQuery obj = manager.getClimateDataQuery();
			ArrayList<String> stations = new ArrayList<String>();
			stations.add(INCOMPLETE_ID);
			DateTime start = new DateTime(1944, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
			DateTime end = new DateTime(1950, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
			
			int data_count =0 , missing_count = 0, range_count=0;
	
			CalendarDataCollection coll = obj.getPeriodData(stations, start, end, DataType.PRECIP, CalendarPeriod.MONTHLY);
			for ( String station : coll ) { 
				for ( float[] year : coll.getStationData(station) ) { 
					for ( int i=0; i<12; i++ ) { 
						if ( year[i] == DataType.MISSING )
							missing_count++;
						else if ( year[i] == DataType.OUTSIDE_OF_RANGE ) { 
							range_count++;
						} else {
							data_count++;
						}
					}
				}
			}
			
			assertEquals("contains valid outside of range value", 10, range_count);
			assertEquals("contains valid number of values", 68, data_count);
			assertEquals("contains valid missing values", 6, missing_count);
	}
	
	public void testDailyData() throws InstantiationException, JMException, RemoteException, InvalidArgumentException  {
			ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
			ClimateDataQuery obj = manager.getClimateDataQuery();
			ArrayList<String> stations = new ArrayList<String>();
			stations.add(INCOMPLETE_ID);
			DateTime start = new DateTime(1944, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
			DateTime end = new DateTime(1950, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
			
			int data_count = 0;
			int missing_count = 0;
			int non_existant_count = 0;
			int error_count = 0;
			int outside_range_count = 0;
	
			CalendarDataCollection coll = obj.getPeriodData(stations, start, end, DataType.NORMAL_TEMP, CalendarPeriod.DAILY);
			for ( String station : coll ) { 
				for ( float[] year : coll.getStationData(station) ) { 
					for ( int i=0; i<366; i++ ) { 
						if ( year[i] == DataType.MISSING )
							missing_count++;
						else if (year[i] == DataType.ERROR_RESULT)
							error_count++;
						else if (year[i] == DataType.OUTSIDE_OF_RANGE) 
							outside_range_count++;
						else if ( year[i] == DataType.NONEXISTANT )
							non_existant_count++;
						else
							data_count++;
					}
				}
			}
			
			assertEquals("contains valid number of values", 0, data_count);
			assertEquals("invalid number of error values", 0, error_count);
			assertEquals("invalid number of out-of-range values", 0, outside_range_count);
			assertEquals("contains valid number of non-existant values", 5, non_existant_count);
			assertEquals("contains valid missing values", 2557, missing_count);
	}
	
	public void testDailyNonLinear() throws InstantiationException, JMException, RemoteException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(INCOMPLETE_ID);
		DateTime start = new DateTime(2004, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 2, 24, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		int data_count =0 , missing_count = 0;

		CalendarDataCollection coll = obj.getPeriodData(stations, start, end, DataType.PRECIP, CalendarPeriod.DAILY);
		for ( String station : coll ) { 
			for ( float[] year : coll.getStationData(station) ) { 
				for ( int i=0; i<366; i++ ) { 
					if ( year[i] == DataType.MISSING )
						missing_count++;
					else 
						data_count++;
				}
			}
		}
	}
	
	public void testAverages() throws InstantiationException, JMException, RemoteException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		
		CalendarDataCollection coll = obj.getHistoricalAverageData(stations, DataType.PRECIP, CalendarPeriod.MONTHLY);
		assertEquals("contains montly values on precip", 12, coll.getStationData(ASHLAND_ID).iterator().next().length);
		
		coll = obj.getHistoricalAverageData(stations, DataType.NORMAL_TEMP, CalendarPeriod.WEEKLY);
		assertEquals("contains weekly values on average temp", 52, coll.getStationData(ASHLAND_ID).iterator().next().length);
		
		coll = obj.getHistoricalAverageData(stations, DataType.PRECIP, CalendarPeriod.DAILY);
		assertEquals("contains daily values on precip", 366, coll.getStationData(ASHLAND_ID).iterator().next().length);

	}

	public void testPrePostPopulateSingleYearRange() throws InstantiationException, JMException, RemoteException, InvalidArgumentException { 		
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(PROVIDENCE_ID);
		
		DateTime start = new DateTime(1937, 3, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 9, 13, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		obj.getPeriodData(stations, start, end, DataType.PRECIP, CalendarPeriod.WEEKLY);		
		obj.getPeriodData(stations, start, end, DataType.PRECIP, CalendarPeriod.WEEKLY);
	}

	public void testAllNEWeekly() throws InstantiationException, JMException, RemoteException, InvalidArgumentException { 		
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ClimateMetaDataQuery meta = manager.getClimateMetaDataQuery();
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		List<String> stations = sq.getStationsForState(USState.Nebraska);
		TemporalPeriod period = meta.getLongestPeriod(stations, CalendarPeriod.WEEKLY);
		
		obj.getPeriodData(stations, period.getStart(), period.getEnd(), DataType.PRECIP, CalendarPeriod.WEEKLY);
	}
	
	public void testAllNEMonthly() throws InstantiationException, JMException, RemoteException, InvalidArgumentException { 		
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ClimateMetaDataQuery meta = manager.getClimateMetaDataQuery();
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		List<String> stations = sq.getStationsForState(USState.Nebraska);
		TemporalPeriod period = meta.getLongestPeriod(stations, CalendarPeriod.MONTHLY);
		
		obj.getPeriodData(stations, period.getStart(), period.getEnd(), DataType.PRECIP, CalendarPeriod.MONTHLY);
	}
	
	
	public void testAveragesNE() throws InstantiationException, JMException, RemoteException, InvalidArgumentException  {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		List<String> stations = sq.getStationsForState(USState.Nebraska);
		
		obj.getHistoricalAverageData(stations, DataType.PRECIP, CalendarPeriod.MONTHLY);

	}
	
	public void testSaintBernard() throws InstantiationException, JMException, RemoteException, InvalidArgumentException {	
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(SAINT_BERNARD);
	
		CalendarDataCollection coll = obj.getAvailableData(stations, DataType.PRECIP, CalendarPeriod.WEEKLY);
		float[][] data = coll.getDataMatrix(SAINT_BERNARD);
		for (int i=0; i<data.length; i++) {
			assertEquals("invalid number of results for one year", CalendarPeriod.WEEKLY.getLength(), data[i].length);
		}
		
		DateTime start = new DateTime(1850, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		obj.getPeriodData(stations, start, end, DataType.PRECIP, CalendarPeriod.MONTHLY);
		obj.getPeriodData(stations, start, end, DataType.NORMAL_TEMP, CalendarPeriod.MONTHLY);
	}

	public void testAdamsBeach() throws InstantiationException, JMException, RemoteException, InvalidArgumentException {		
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ADAMS_BEACH);
	
		obj.getAvailableData(stations, DataType.PRECIP, CalendarPeriod.MONTHLY);
		obj.getAvailableData(stations, DataType.NORMAL_TEMP, CalendarPeriod.MONTHLY);
		
		DateTime start = new DateTime(1850, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		obj.getPeriodData(stations, start, end, DataType.PRECIP, CalendarPeriod.MONTHLY);
		obj.getPeriodData(stations, start, end, DataType.NORMAL_TEMP, CalendarPeriod.MONTHLY);
	}
	public void testAllFloridaWeekly() throws InstantiationException, JMException, RemoteException, InvalidArgumentException {		
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		List<String> stations = sq.getStationsForState(USState.Florida);

		obj.getAvailableData(stations, DataType.PRECIP, CalendarPeriod.WEEKLY);
	}
	
	public void testAvgAnnualHigh() throws InstantiationException, JMException, RemoteException, InvalidArgumentException {		
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		
		CalendarDataCollection cdc = obj.getHistoricalAverageData(stations, DataType.HIGH_TEMP, CalendarPeriod.ANNUALLY);
		assertEquals("valid average high temperature", 62, cdc.getDataMatrix(ASHLAND_ID)[0][0], 1.0);
	}
	
	public void testgetAnnualExtremeHighTemp() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ARTHUR);
		int data_count=0 , missing_count = 0 ;  
		CalendarDataCollection coll=obj.getAnnualExtremeTemp(stations,1995,1996,DataType.HIGH_TEMP);
	    int cntofyear= 0 ;
		for ( String station : coll ) { 
			for ( float [] year : coll.getStationData(station) ) { 
				for ( int i=0; i<1; i++ ) { 
					if ( year[i] == DataType.MISSING ){
						missing_count++;
					}else{
						data_count++;
						if(cntofyear==0){
							assertEquals("contains correct value", 104.80999755859375f, year[i], 0.1f);
						}
						if(cntofyear==1){
							assertEquals("contains correct value", 103.63999938964844f, year[i], 0.1f);
						}
					}
				}
				cntofyear++;
			}
		}
		assertEquals("contains valid number of values", 2, data_count);
		assertEquals("contains valid missing values", 0, missing_count);
	}
	
	
	public void testgetAnnualExtremeLowTemp() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ARTHUR);
		int data_count=0 , missing_count = 0 ;  
		CalendarDataCollection coll=obj.getAnnualExtremeTemp(stations,1983,1986,DataType.LOW_TEMP);
		int cntofyear=0;
		for ( String station : coll ) { 
			for ( float[] year : coll.getStationData(station) ) { 
				for ( int i=0; i<1; i++ ) { 
					if ( year[i] == DataType.MISSING ){
						missing_count++;
					}else{
						
						if(cntofyear==0){
							assertEquals("contains correct value ", -21.0f, year[i], 0.1f);
						}
						if(cntofyear==1){
							assertEquals("contains correct value ", -21.0f, year[i], 0.1f);
						}
						if(cntofyear==2){
							assertEquals("contains correct value ", -15.0f, year[i], 0.1f);
						}
						if(cntofyear==3){
							assertEquals("contains correct value ", -15.0f, year[i], 0.1f);
						}
						data_count++;
					}
				}
				cntofyear++;
			}
		}
		assertEquals("contains valid number of values", 4, data_count);
		assertEquals("contains valid missing values", 0, missing_count);
	}
	
	//updated because of jira FARM-445
	//This now gets the sum of of the period
	public void testgetPeriodAnnualData1() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		int data_count=0 , missing_count = 0 ;  
		DateTime start = new DateTime(1999, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2001, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		CalendarDataCollection coll=obj.getPeriodData(stations,start,end,DataType.PRECIP, CalendarPeriod.ANNUALLY);
	    int cntofyear= 0 ;
		for ( String station : coll ) { 
			for ( float [] year : coll.getStationData(station) ) { 
				for ( int i=0; i<1; i++ ) { 
					if ( year[i] == DataType.MISSING ){
						missing_count++;
					}else{
						data_count++;
						if(cntofyear==0){
							assertEquals("contains correct value", 33.145f, year[i], 0.1f);
						}
						if(cntofyear==1){
							assertEquals("contains correct value", 23.73f, year[i], 0.1f);
						
						}
						if(cntofyear==2){
							assertEquals("contains correct value", 28.13f, year[i], 0.1f);
						}
					}
				}
				cntofyear++;
			}
		}
		assertEquals("contains valid number of values", 3, data_count);
		assertEquals("contains valid missing values", 0, missing_count);
	}
	//updated because of jira FARM-445
	public void testgetPeriodAnnualData2() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		int data_count=0 , missing_count = 0 ;  
		DateTime start = new DateTime(1970, 5, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(1975, 3, 4, 0, 0, 0, 0, GregorianChronology.getInstance());
		CalendarDataCollection coll=obj.getPeriodData(stations,start,end,DataType.NORMAL_TEMP, CalendarPeriod.ANNUALLY);
	    int cntofyear= 0 ;
		for ( String station : coll ) { 
			for ( float [] year : coll.getStationData(station) ) { 
				for ( int i=0; i<1; i++ ) { 
					data_count++;
					if(cntofyear==0){
						assertEquals("contains correct value", DataType.MISSING, year[i]);
					}
					if(cntofyear==1){
						assertEquals("contains correct value", 58.89f, year[i], 0.1f);
					}
					if(cntofyear==2){
						assertEquals("contains correct value", 57.92f, year[i], 0.1f);
					}
					if(cntofyear==3){
						assertEquals("contains correct value", 59.99f, year[i], 0.1f);
					}
					if(cntofyear==4){
						assertEquals("contains correct value", 59.19f, year[i], 0.1f);
					}
					if(cntofyear==5){
						assertEquals("contains correct value", DataType.MISSING, year[i]);
					}
				}
				cntofyear++;
			}
		}
		assertEquals("contains valid number of values", 6, data_count);
		assertEquals("contains valid missing values", 0, missing_count);
	}
	
	
	//updated because of jira FARM-445
	//This now gets the sum of of the period
	public void testgetPeriodAnnualData3() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ARTHUR);
		int data_count=0 , missing_count = 0 ;  
		DateTime start = new DateTime(1983, 12, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(1986, 9, 4, 0, 0, 0, 0, GregorianChronology.getInstance());
		CalendarDataCollection coll=obj.getPeriodData(stations,start,end,DataType.PRECIP, CalendarPeriod.ANNUALLY);
	    int cntofyear= 0 ;
		for ( String station : coll ) { 
			for ( float [] year : coll.getStationData(station) ) { 
				for ( int i=0; i<1; i++ ) { 
					data_count++;
					if(cntofyear==0){
						assertEquals("contains correct value", DataType.MISSING, year[i]);
					}
					if(cntofyear==1){
						assertEquals("contains correct value", 13.269f, year[i], 0.1f);
					}
					if(cntofyear==2){
						assertEquals("contains correct value", 11.85f, year[i], 0.1f);
					}
					if(cntofyear==3){
						assertEquals("contains correct value", DataType.MISSING, year[i]);
					}
				}
				cntofyear++;
			}
		}
		assertEquals("contains valid number of values", 4, data_count);
		assertEquals("contains valid missing values", 0, missing_count);
	}
	
	//updated because of jira FARM-445 
	public void testgetPeriodAnnualData4() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ARTHUR);
		int data_count=0 , missing_count = 0 ;  
		DateTime start = new DateTime(1983, 12, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(1986, 9, 4, 0, 0, 0, 0, GregorianChronology.getInstance());
		CalendarDataCollection coll=obj.getPeriodData(stations,start,end,DataType.NORMAL_TEMP, CalendarPeriod.ANNUALLY);
	    int cntofyear= 0 ;
		for ( String station : coll ) { 
			for ( float [] year : coll.getStationData(station) ) { 
				for ( int i=0; i<1; i++ ) { 
					data_count++;
					if(cntofyear==0){
						assertEquals("contains correct value", DataType.MISSING, year[i]);
					}
					if(cntofyear==1){
						assertEquals("contains correct value", 54.6f, year[i], 0.1f);
					}
					if(cntofyear==2){
						assertEquals("contains correct value", 52.53f, year[i], 0.1f);
					}
					if(cntofyear==3){
						assertEquals("contains correct value", DataType.MISSING, year[i]);
					}
				}
				cntofyear++;
			}
		}
		assertEquals("contains valid number of values", 4, data_count);
		assertEquals("contains valid missing values", 0, missing_count);
	}
	
	/*
	 * Test case to get valid return from getClomateDataQuery if data does not exsit for the whole range requsted. -99 is returned for any year is outside of the varible's data range.
	 * Norma_temp data only esits in 1895 not 1894
	 */
	public void testgetPeriodAnnualData5() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(KEARNEY4NE);
		int data_count=0;
		DateTime start = new DateTime(1894, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2006, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		CalendarDataCollection coll=obj.getPeriodData(stations,start,end,DataType.NORMAL_TEMP, CalendarPeriod.ANNUALLY);
	    int cntofyear= 0 ;
		for ( String station : coll ) { 
			for ( float [] year : coll.getStationData(station) ) { 
				for ( int i=0; i<1; i++ ) { 
					data_count++;
					if(cntofyear==0){
						assertEquals("contains correct value", DataType.MISSING, year[i]);
					}
					if(cntofyear==1){
						assertEquals("contains correct value", DataType.MISSING, year[i]);
					}
					if(cntofyear==8){
						assertEquals("contains correct value", 61.3f, year[i], 0.1f);
					}
				}
				cntofyear++;
			}
		}
		assertEquals("contains valid number of values", 113, data_count);
	}
	
	
	//jira FARM-445
	public void testMissingWholeYear() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		int shouldbe=69;
		stations.add(DANBURY);
		DateTime start = new DateTime(1937, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2005, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		CalendarDataCollection coll=obj.getPeriodData(stations, start, end, DataType.PRECIP, CalendarPeriod.ANNUALLY);
		int cntofyear=0;
		for ( String station : coll ) { 
			for ( float [] year : coll.getStationData(station) ) { 
				cntofyear++;
			}
		}
		assertEquals("contains valid number of years", shouldbe, cntofyear);
	}
	//jira FARM-445	
	public void testAVERAGE_MONTHLY_BASE() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		/*
		 * Test AVERAGE_MONTHLY_BASE on a precip only statoin for NORMAL_TEMP to get 12 months of -99
		 */
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		int shouldbe=12;
		int value=0;
		stations.add(KRAMER);
		CalendarDataCollection coll =obj.getHistoricalAverageData(stations, DataType.NORMAL_TEMP, CalendarPeriod.MONTHLY);
		for ( String station : coll ) { 
			for ( float [] year : coll.getStationData(station) ) { 
				value=year.length;
			}
		}
		assertEquals("contains valid number of years", shouldbe, value);
	}
	//jira FARM-445
	public void testAVERAGE_WEEKLY_BASE() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		/*
		 * Test AVERAGE_WEEKLY_BASE on a precip only statoin for NORMAL_TEMP to get 52 weeks of -99
		 */
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		int shouldbe=52;
		int value=0;
		stations.add(KRAMER);
		CalendarDataCollection coll =obj.getHistoricalAverageData(stations, DataType.NORMAL_TEMP, CalendarPeriod.WEEKLY);
		for ( String station : coll ) { 
			for ( float [] year : coll.getStationData(station) ) { 
				value=year.length;
			}
		}
		assertEquals("contains valid number of years", shouldbe, value);
	}
	//jira FARM-445
	public void testAVERAGE_DAILY_BASE() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		/*
		 * Test AVERAGE_DAILY_BASE on a precip only statoin for NORMAL_TEMP to get 366 days of -99
		 */
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		int shouldbe=366;
		int value=0;
		stations.add(KRAMER);
		CalendarDataCollection coll =obj.getHistoricalAverageData(stations, DataType.NORMAL_TEMP, CalendarPeriod.DAILY);
		for ( String station : coll ) { 
			for ( float [] year : coll.getStationData(station) ) { 
				value=year.length;
			}
		}
		assertEquals("contains valid number of years", shouldbe, value);
	}
	//jira FARM-445
	public void testAVERAGE_ANNUAL_BASE() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		/*
		 * Test AVERAGE_ANNUAL_BASE on a precip only statoin for NORMAL_TEMP to get -99 back
		 */
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		int shouldbe=1;
		int value=0;
		stations.add(KRAMER);
		CalendarDataCollection coll =obj.getHistoricalAverageData(stations, DataType.NORMAL_TEMP, CalendarPeriod.ANNUALLY);
		for ( String station : coll ) { 
			for ( float [] year : coll.getStationData(station) ) { 
				value=year.length;
			}
		}
		assertEquals("contains valid number of years", shouldbe, value);
	}
	
	public void testAVERAGE_ANNUAL_BASE_KEARNEY() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		/*
		 * Test AVERAGE_ANNUAL_BASE on a precip only statoin for NORMAL_TEMP to get -99 back
		 */
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		int shouldbe=1;
		int value=0;
		stations.add("20999");
		CalendarDataCollection coll =obj.getHistoricalAverageData(stations, DataType.NORMAL_TEMP, CalendarPeriod.ANNUALLY);
		for ( String station : coll ) { 
			for ( float [] year : coll.getStationData(station) ) { 
				value=year.length;
			}
		}
		assertEquals("contains valid number of years", shouldbe, value);
	}
	
	public void testSiteListExport() throws Exception { 
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();

		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2006, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		stations.add(ASHLAND_ID);
		CalendarDataCollection coll =obj.getPeriodData(stations, start, end, DataType.NORMAL_TEMP, CalendarPeriod.MONTHLY);
		
		ClimateMetaDataQuery mdq = ClimateServiceAccessor.getInstance().getClimateMetaDataQuery();
		MetaDataCollection<StationMetaDataType> meta = mdq.getAllMetaData(stations, CalendarPeriod.WEEKLY);
	
		float f = coll.getDataMatrix(ASHLAND_ID)[2][6]; 
	
		SiteList sites = coll.generateSiteList(new DateTime(2002, 7, 1, 0, 0, 0, 0, GregorianChronology.getInstance()), meta, false);
		
		Site site = sites.getSite(ASHLAND_ID);
		assertEquals("invalid value", f, site.getValue());
		assertEquals("invalid station ID", ASHLAND_ID, sites.getMetaDatum(StationMetaDataType.STATION_ID, ASHLAND_ID));
		assertEquals("invalid station name", "ASHLAND 2", sites.getMetaDatum(StationMetaDataType.STATION_NAME, ASHLAND_ID));
	}
	
	public void testSiteListExportDateBoundaries() throws Exception { 
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();

		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2006, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		stations.add(ASHLAND_ID);
		CalendarDataCollection coll =obj.getPeriodData(stations, start, end, DataType.NORMAL_TEMP, CalendarPeriod.MONTHLY);
		
		float f = coll.getDataMatrix(ASHLAND_ID)[0][0]; 
	
		ClimateMetaDataQuery mdq = ClimateServiceAccessor.getInstance().getClimateMetaDataQuery();
		MetaDataCollection<StationMetaDataType> meta = mdq.getAllMetaData(stations, CalendarPeriod.WEEKLY);
		
		SiteList sites = coll.generateSiteList(new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance()), meta, false);
		
		Site site = sites.getSite(ASHLAND_ID);
		assertEquals("invalid value", f, site.getValue());
		assertEquals("invalid station ID", ASHLAND_ID, sites.getMetaDatum(StationMetaDataType.STATION_ID, ASHLAND_ID));
		assertEquals("invalid station name", "ASHLAND 2", sites.getMetaDatum(StationMetaDataType.STATION_NAME, ASHLAND_ID));
		
		f = coll.getDataMatrix(ASHLAND_ID)[6][11]; 
		
		sites = coll.generateSiteList(new DateTime(2006, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance()), meta, false);
		site = sites.getSite(ASHLAND_ID);
		
		assertEquals("invalid value", f, site.getValue());
	}
	
	// Exposes the bug in FARM-510
	public void testNoData59Expected() 
	throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(COCHRANE);
		
		DateTime start = new DateTime(2006, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(2007, 2, 16, 0, 0, 0, 0, GregorianChronology.getInstance());
		
		CalendarDataCollection cdc = obj.getPeriodData(stations, start, end, DataType.PRECIP, CalendarPeriod.WEEKLY);
		for ( float[] row : cdc.getDataMatrix(COCHRANE) ) {
			for ( float f : row ) {
				assertTrue("COCHRANE station should have all missing data from 2006-2007, instead: " + f,
						(f == DataType.MISSING || f == DataType.OUTSIDE_OF_RANGE || f== DataType.OUTSIDE_OF_REQUEST_RANGE ));
			}
		}
	}
	
	private float[] convertedDepthData = { 2.54f, 35.30600f, 33.78200f, 34.54400f, 56.89600f, 129.28600f, 91.69400f, 28.44800f,
			22.35200f, 64.26200f, 71.88200f, 31.75f, 33.52800f, 65.53200f, 22.35200f, 43.68800f, 255.77800f, 31.75f, 
			15.24f, 41.14800f, 73.15200f, 72.39f, 47.24400f, 12.7f };

	private float[] originalDepthData = { 0.1f, 1.39f, 1.33f, 1.36f, 2.24f, 5.09f, 3.61f, 1.12f, 0.88f, 2.53f, 2.83f, 1.25f, 
			1.32f, 2.58f, 0.88f, 1.72f, 10.07f, 1.25f, 0.6f, 1.62f, 2.88f, 2.85f, 1.86f, 0.5f };

	public void testConvertedDepth() throws Exception { 
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		
		DateTime start_date = new DateTime(2000,1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime end_date = new DateTime(2001,12,31,0,0,0,0,GregorianChronology.getInstance());
		
		CalendarDataCollection cdc = obj.getPeriodData(stations, start_date, end_date, DataType.PRECIP, CalendarPeriod.MONTHLY);
		
		cdc.convertToMetric();

		for ( String str : cdc ) { 
			int i=0;
			for ( float[] data : cdc.getStationData(str) ) { 
				for ( float datum : data ) {
					assertEquals("invalid value", convertedDepthData[i++], datum, 0.001);
				}
			}
		}
		
		cdc.convertToEnglish();
		
		for ( String str : cdc ) { 
			int i=0;
			for ( float[] data : cdc.getStationData(str) ) { 
				for ( float datum : data ) {
					assertEquals("invalid value", originalDepthData[i++], datum, 0.001);
				}
			}
		}
	}
	
	private float[] convertedDegreeData = { -2.4f, 1.58888889f,  6.60555556f, 10.6222222f, 18.8888889f, 22.0833333f, 24.3166667f, 
			25.3666667f, 19.6222222f, 13.5388889f, 0.566666667f, -9.65f, -3.9f, -6.33888889f, 1.38888889f, 12.5444444f, 17.6333333f, 
			21.6944444f, 26.2444444f, 24.7111111f,  18.4611111f, 11.9833333f,  9.94444444f, 0.377777778f };
	
	private float[] originalDegreeData = { 27.68f, 34.86f, 43.89f, 51.12f, 66.0f, 71.75f, 75.77f, 77.66f, 67.32f, 56.37f, 33.02f, 
			14.63f, 24.98f, 20.59f, 34.5f, 54.58f, 63.74f, 71.05f, 79.24f, 76.48f, 65.23f, 53.57f, 49.9f, 32.68f };
	
	
	public void testConvertedDegrees() throws Exception { 
		DateTime start_date = new DateTime(2000,1,1,0,0,0,0,GregorianChronology.getInstance());
		DateTime end_date = new DateTime(2001,12,31,0,0,0,0,GregorianChronology.getInstance());
		
		YearDataBuilder ydb = new YearDataBuilder(start_date, end_date, CalendarPeriod.MONTHLY, DataType.NORMAL_TEMP);
		ydb.openStation("sample");
		for ( float val : originalDegreeData ) { 
			ydb.add(val);
			
		}
		ydb.writeStation();
		CalendarDataCollection cdc = ydb.returnCollection();
		
		cdc.convertToMetric();

		for ( String str : cdc ) { 
			int i=0;
			for ( float[] data : cdc.getStationData(str) ) { 
				for ( float datum : data ) {
					assertEquals("invalid value", convertedDegreeData[i++], datum, 0.001);
				}
			}
		}
		
		cdc.convertToEnglish();
		
		for ( String str : cdc ) { 
			int i=0;
			for ( float[] data : cdc.getStationData(str) ) { 
				for ( float datum : data ) {
					assertEquals("invalid value", originalDegreeData[i++], datum, 0.001);
				}
			}
		}		
	}
	
	public void testFlagConversion() throws Exception { 
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(DANBURY);
		
		DateTime start = new DateTime(1930, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		DateTime end = new DateTime(1931, 12, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
		CalendarDataCollection coll=obj.getPeriodData(stations, start, end, DataType.PRECIP, CalendarPeriod.MONTHLY);
		
		for ( String str : coll ) { 
			for ( float[] data : coll.getStationData(str) ) { 
				for ( float datum : data ) {
					assertEquals("the flag value was converted", DataType.MISSING, datum);
				}
			}
		}
	}
		
	float[] normal_data = { 21.23f, 26.91f, 39.65f, 52.38f, 63.55f, 74.16f, 78.97f, 76.85f, 65.32f, 52.93f, 37.93f, 25.86f };
	
	public void testNormalComputation() throws Exception {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		
		CalendarDataCollection cdc = obj.getDataNormals(stations, 1970, 2000, DataType.NORMAL_TEMP, CalendarPeriod.MONTHLY);
		
		for ( String str : cdc ) { 
			for ( float[] data : cdc.getStationData(str) ) {
				int count = 0;
				for ( float datum : data ) { 
					assertEquals("invalid value", normal_data[count++], datum);
				}
			}
		}
	}
	
	public void testVariableQuery() throws Exception { 
		ClimateMetaDataQuery query = ClimateServiceAccessor.getInstance().getClimateMetaDataQuery();
		List<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		
		Map<String, Map<DataType, VariableMetaData>> data = query.getVariableMetaData(stations);
		Map<DataType, VariableMetaData> station_data = data.get(ASHLAND_ID);
	
		VariableMetaData meta = station_data.get(DataType.PRECIP);
		assertEquals("invalid missing value", meta.getMissingPercent(), .0314991, 0.001);
		assertEquals("invalid starting year", meta.getStartDate().getYear(), 1893);
		assertEquals("invalid ending year", meta.getEndDate().getYear(), 2008);
	}
	public void testFFPNormal() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ARTHUR);
		CalendarDataCollection coll=obj.getFrostFreePeriodNormals(stations, 1992, 2001);
		for ( String station : coll ) { 
			for ( float [] year : coll.getStationData(station) ) { 
				for ( int i=0; i<1; i++ ) { 
					assertEquals("contains correct value", 166.2f, year[i], 0.1f);
					}
			}
		}
		}
		
	public void testWeeklyNormalData() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
	
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		int shouldbe=52;
		int value=0;
		stations.add(INCOMPLETE_ID);
		CalendarDataCollection coll =obj.getDataNormals(stations, 1944, 1945, DataType.PRECIP, CalendarPeriod.WEEKLY);
		for ( String station : coll ) { 
			for ( float [] year : coll.getStationData(station) ) { 
				value=year.length;
			}
		}
		assertEquals("contains valid number of years", shouldbe, value);
	}
	public void testDailyNormalData() throws InstantiationException, JMException, RemoteException, InvalidStateException, InvalidArgumentException {
		
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		int shouldbe=366;
		int value=0;
		stations.add(ASHLAND_ID);
		CalendarDataCollection coll =obj.getDataNormals(stations, 2000, 2001, DataType.PRECIP, CalendarPeriod.DAILY);
		for ( String station : coll ) { 
			for ( float [] year : coll.getStationData(station) ) { 
				value=year.length;
			}
		}
		assertEquals("contains valid number of years", shouldbe, value);
	}
	
	public void testAvailableDailyData() throws InstantiationException, JMException, RemoteException, InvalidArgumentException {		
		ClimateServiceAccessor manager = ClimateServiceAccessor.getInstance();
		ClimateDataQuery obj = manager.getClimateDataQuery();
		ClimateSpatialExtension sq = ClimateServiceAccessor.getInstance().getSpatialExtension();
		List<String> stations = sq.getStationsForState(USState.Rhode_Island);

		obj.getAvailableData(stations, DataType.PRECIP, CalendarPeriod.DAILY);
	}
}

