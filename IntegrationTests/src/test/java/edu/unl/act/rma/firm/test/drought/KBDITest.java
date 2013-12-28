/**
 * 
 */
package edu.unl.act.rma.firm.test.drought;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.climate.component.ClimateDataQuery;
import edu.unl.act.rma.firm.climate.component.ClimateMetaDataQuery;
import edu.unl.act.rma.firm.climate.configuration.ClimateServiceAccessor;
import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.StationMetaDataType;
import edu.unl.act.rma.firm.drought.component.SoilsDataQuery;
import edu.unl.act.rma.firm.drought.configuration.DroughtServiceAccessor;
import edu.unl.act.rma.firm.drought.index.KeetchByramDroughtIndex;
import edu.unl.act.rma.firm.test.StationList;

/**
 * @author Ian Cottingham
 *
 */

public class KBDITest extends TestCase {
	
	public static final String ASHLAND_ID = StationList.ASHLAND_ID.getStationID();
	public static final String ARTHUR = StationList.ARTHUR.getStationID();
	
	
	public void testKBDI1() throws Exception { 
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ARTHUR);
		DateTime startdate = new DateTime(1990, 5, 5, 0, 0, 0, 0);
		DateTime enddate = new DateTime(1990, 9, 25, 0, 0, 0, 0);
		CalendarDataCollection coll = new KeetchByramDroughtIndex().computeKBDI(stations,startdate,enddate);
		int data_count=0,missing_count=0,range_count=0;
		int cntofyear=0; 
		for ( String station : coll ) { 
			for ( float[] year : coll.getStationData(station) ) {
				System.out.printf("\n");
				for ( int i=0; i<366; i++ ) { 
					if (( year[i] == DataType.MISSING )||( year[i] == DataType.NONEXISTANT )){
						missing_count++;
					}else if ( year[i] == DataType.OUTSIDE_OF_RANGE || year[i] == DataType.OUTSIDE_OF_REQUEST_RANGE) { 
						range_count++;
					}else{
						data_count++;
						if((cntofyear==0)&&(i==127)){
							assertEquals("contains correct value",0f, year[i], 1f);
						}
						if((cntofyear==0)&&(i==133)){
							assertEquals("contains correct value",2.66f, year[i], 1f);
						}
						if((cntofyear==0)&&(i==234)){
							assertEquals("contains correct value",176.66f, year[i], 1f);
						}
						if((cntofyear==0)&&(i==241)){
							assertEquals("contains correct value",208.98f, year[i], 1f);
						}
						if((cntofyear==0)&&(i==245)){
							assertEquals("contains correct value",215.03f, year[i], 1f);
						}
					}
				}
				cntofyear++;
			}
		}
		assertEquals("contains valid number of values", 144, data_count);
		assertEquals("contains valid missing values",0, missing_count);
		assertEquals("contains valid number of outside range values",222, range_count);
	}
	

	public void testKBDI2() throws Exception { 
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ARTHUR);
		DateTime startdate = new DateTime(1990, 4, 5, 0, 0, 0, 0);
		DateTime enddate = new DateTime(1991, 7, 25, 0, 0, 0, 0);
		CalendarDataCollection coll = new KeetchByramDroughtIndex().computeKBDI(stations,startdate,enddate);
		int data_count=0,missing_count = 0,range_count=0;
		int cntofyear=0; 
		for ( String station : coll ) { 
			for ( float[] year : coll.getStationData(station) ) { 
				for ( int i=0; i<366; i++ ) { 
					if (( year[i] == DataType.MISSING )||( year[i] == DataType.NONEXISTANT )){
						missing_count++;
					}else if ( year[i] == DataType.OUTSIDE_OF_RANGE || year[i] == DataType.OUTSIDE_OF_REQUEST_RANGE) { 
						range_count++;
					}else{
						data_count++;
						if((cntofyear==0)&&(i==127)){
							assertEquals("contains correct value",0f, year[i], 1f);
						}
						/*
						if((cntofyear==0)&&(i==133)){
						}
						*/
						if((cntofyear==0)&&(i==182)){
							assertEquals("contains correct value",88.58f, year[i], 1f);
						}
						if((cntofyear==0)&&(i==241)){
							assertEquals("contains correct value",208.98f, year[i], 1f);
						}
						if((cntofyear==1)&&(i==1)){
							assertEquals("contains correct value",192.09f, year[i], 1f);
						}
						if((cntofyear==1)&&(i==13)){
							assertEquals("contains correct value",192.61f, year[i], 1f);
						}
						if((cntofyear==1)&&(i==49)){
							assertEquals("contains correct value",197.09, year[i], 1f);
						}
						if((cntofyear==1)&&(i==187)){
							assertEquals("contains correct value",44.36f, year[i], 1f);
						}
					}
				}
				cntofyear++;
			}
		}
		assertEquals("contains valid number of values", 478, data_count);
		assertEquals("contains valid missing values",0, missing_count);
		assertEquals("contains valid number of outside range values",254, range_count);
		
	}
	
	public void testKBDI3() throws Exception { 
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		DateTime startdate = new DateTime(1980, 1, 5, 0, 0, 0, 0);
		DateTime enddate = new DateTime(1981, 2, 25, 0, 0, 0, 0);
		CalendarDataCollection coll = new KeetchByramDroughtIndex().computeKBDI(stations,startdate,enddate);
		int data_count=0,missing_count = 0,range_count=0;
		int cntofyear=0; 
		for ( String station : coll ) { 
			for ( float[] year : coll.getStationData(station) ) { 
				for ( int i=0; i<366; i++ ) { 
					if (( year[i] == DataType.MISSING )||( year[i] == DataType.NONEXISTANT )){
						missing_count++;
					}else if ( year[i] == DataType.OUTSIDE_OF_RANGE || year[i] == DataType.OUTSIDE_OF_REQUEST_RANGE) { 
						range_count++;
					}else{
						data_count++;
						if((cntofyear==0)&&(i==68)){
							assertEquals("contains correct value",62.99f, year[i], 1f);
						}
						if((cntofyear==0)&&(i==136)){
							assertEquals("contains correct value",56.33f, year[i], 1f);
						}
						if((cntofyear==0)&&(i==137)){
							assertEquals("contains correct value",0f, year[i], 1f);
						}
						if((cntofyear==0)&&(i==201)){
							assertEquals("contains correct value",79.42f, year[i], 1f);
						}
						if((cntofyear==0)&&(i==238)){
							assertEquals("contains correct value",42.28f, year[i], 1f);
						}
						if((cntofyear==1)&&(i==1)){
							assertEquals("contains correct value",2.65f, year[i], 1f);
						}
						if((cntofyear==1)&&(i==50)){
							assertEquals("contains correct value",5.4f, year[i], 1f);
						}
					}
				}
				cntofyear++;
			}
		}
		assertEquals("contains valid number of values", 354, data_count);
		assertEquals("contains valid missing values",64, missing_count);
		assertEquals("contains valid number of outside range values",314, range_count);
		
	}
	
	public void testKBDI4() throws Exception { 
		ArrayList<String> stations = new ArrayList<String>();
		stations.add(ASHLAND_ID);
		DateTime startdate = new DateTime(2002, 1, 5, 0, 0, 0, 0);
		DateTime enddate = new DateTime(2002, 4, 25, 0, 0, 0, 0);
		CalendarDataCollection coll = new KeetchByramDroughtIndex().computeKBDI(stations,startdate,enddate);
		int data_count=0,missing_count = 0,range_count=0;
		int cntofyear=0; 
		for ( String station : coll ) { 
			for ( float[] year : coll.getStationData(station) ) { 
				for ( int i=0; i<366; i++ ) { 
					if (( year[i] == DataType.MISSING )||( year[i] == DataType.NONEXISTANT )){
						missing_count++;
					}else if ( year[i] == DataType.OUTSIDE_OF_RANGE || year[i] == DataType.OUTSIDE_OF_REQUEST_RANGE) { 
						range_count++;
					}else{
						data_count++;
						if((cntofyear==0)&&(i==9)){
							assertEquals("contains correct value",1.21f, year[i], 1f);
						}
						if((cntofyear==0)&&(i==40)){
							assertEquals("contains correct value",0f, year[i], 1f);
						}
						if((cntofyear==0)&&(i==60)){
							assertEquals("contains correct value",3.17f, year[i], 1f);
						}
						if((cntofyear==0)&&(i==72)){
							assertEquals("contains correct value",0.89f, year[i], 1f);
						}
						if((cntofyear==0)&&(i==107)){
							assertEquals("contains correct value",18.64f, year[i], 1f);
						}
					}
				}
				cntofyear++;
			}
		}
		assertEquals("contains valid number of values", 112, data_count);
		assertEquals("contains valid missing values",0, missing_count);
		assertEquals("contains valid number of outside range values",254, range_count);
	}
	
	public void testKBDIcustom() throws Exception {
		ClimateDataQuery obj = ClimateServiceAccessor.getInstance().getClimateDataQuery();
		ArrayList<String> stations = new ArrayList<String>();
		String station = StationList.PROVIDENCE_ID.getStationID(); 
		stations.add(station);
		float[][] precip = obj.getAvailableData(stations, DataType.PRECIP, CalendarPeriod.DAILY).getDataMatrix(station);
		float[][] temp = obj.getAvailableData(stations, DataType.HIGH_TEMP, CalendarPeriod.DAILY).getDataMatrix(station);
		DateTime startdate = new DateTime(2002, 1, 5, 0, 0, 0, 0);
		DateTime enddate = new DateTime(2002, 4, 25, 0, 0, 0, 0);
		SoilsDataQuery sdo = DroughtServiceAccessor.getInstance().getSoilsDataQuery();
		float ave_precip = obj.getHistoricalAverageData(stations, DataType.PRECIP, CalendarPeriod.ANNUALLY).getDataMatrix(station)[0][0];
		float csm = sdo.getCurrentSoilMoisture(stations, enddate).get(station);
		float awc = sdo.getWaterHoldingCapacity(stations).get(station);

		ClimateMetaDataQuery cmdq = ClimateServiceAccessor.getInstance().getClimateMetaDataQuery();
		int station_start = (Integer)cmdq.getMetaData(stations, StationMetaDataType.ABS_START_DATE, CalendarPeriod.DAILY).getStationMetaData(station).get(StationMetaDataType.ABS_START_DATE);
		KeetchByramDroughtIndex kbdi = new KeetchByramDroughtIndex();
		float[][] kbdi_data = kbdi.compute(station_start, precip, temp, ave_precip, csm, awc, startdate, enddate);
		for (int i = 0; i < kbdi_data.length; i++) {
			for (int j = 0; j < kbdi_data[i].length; j++) {
				System.out.print(kbdi_data[i][j] + "\t");
			}
			System.out.println();
		}
	}
}
