package edu.unl.act.rma.firm.test.drought.index;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

import junit.framework.TestCase;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.drought.index.PalmerDroughtSeverityIndex;

public class PdsiTest extends TestCase {

	
private static HashMap<String, Object> ORACLE;
	
	static { 
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PdsiTest.class.getClassLoader().getResource("DroughtTestOracle").getPath()));
			ORACLE = (HashMap<String, Object>)ois.readObject();
		} catch  ( Exception e ) { 
			RuntimeException re = new RuntimeException("could not load the data oracle");
			re.initCause(e);
			throw re;
		}
	}
	
	public void testOneMonthPDSI() throws Exception { 
		PalmerDroughtSeverityIndex pdsi = new PalmerDroughtSeverityIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("MONTHLY_PRECIP_P");
		HashMap<String, float[][]> temp_data = (HashMap<String, float[][]>) ORACLE.get("MONTHLY_TEMP_P");
		HashMap<String, float[][]> average_data = (HashMap<String, float[][]>) ORACLE.get("MONTHLY_AVG_TEMP_P");
		
		HashMap<String, float[][]> additional_inputs = (HashMap<String, float[][]>) ORACLE.get("PDSI_ADDITIONAL_INPUTS");
		HashMap<String, float[][]> pdsi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("PDSI_M_1");
			
		for ( String str : precip_data.keySet() ) {
		
			float[][] pdsi_oracle = pdsi_oracle_data.get(str);
			float[][] precipitationData=precip_data.get(str);
			float[][] temperatureAverage=average_data.get(str);
			float[][] temperatureData=temp_data.get(str);
			float[][] add=additional_inputs.get(str);
			pdsi.setData(precipitationData, temperatureData, temperatureAverage[0], add[1][0], add[0][0]);
		
			float[][] pdsi_data = pdsi.scMonthlyPDSI();
		
			for ( int i=0; i<pdsi_data.length; i++ ) { 
				for ( int j=0; j<pdsi_data[i].length; j++ ) { 
					float oracle_val = pdsi_oracle[i][j];
					float pdsi_val = pdsi_data[i][j];
					if ( pdsi_val == 0 ) { 
						pdsi_val = DataType.MISSING;
					}
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}
					assertEquals("value does not match oracle for station "+str, oracle_val, pdsi_val, 0.05f);
				}
			}
				
		}
	}

	public void testOneMonthZNDI() throws Exception { 
		PalmerDroughtSeverityIndex pdsi = new PalmerDroughtSeverityIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("MONTHLY_PRECIP_P");
		HashMap<String, float[][]> temp_data = (HashMap<String, float[][]>) ORACLE.get("MONTHLY_TEMP_P");
		HashMap<String, float[][]> average_data = (HashMap<String, float[][]>) ORACLE.get("MONTHLY_AVG_TEMP_P");
		HashMap<String, float[][]> additional_inputs = (HashMap<String, float[][]>) ORACLE.get("PDSI_ADDITIONAL_INPUTS");
		HashMap<String, float[][]> z_oracle_data = (HashMap<String, float[][]>) ORACLE.get("ZIND_M_1");
				
		for ( String str : precip_data.keySet() ) {
			float[][] pdsi_oracle = z_oracle_data.get(str);
			float[][] precipitationData=precip_data.get(str);
			float[][] temperatureAverage=average_data.get(str);
			float[][] temperatureData=temp_data.get(str);
			float[][] add=additional_inputs.get(str);
			pdsi.setData(precipitationData, temperatureData, temperatureAverage[0], add[1][0], add[0][0]);
			
			float[][] pdsi_data = pdsi.scMonthlyZNDI();
			for ( int i=0; i<pdsi_data.length; i++ ) { 
				for ( int j=0; j<pdsi_data[i].length; j++ ) { 
					float oracle_val = pdsi_oracle[i][j];
					float pdsi_val = pdsi_data[i][j];
					if ( pdsi_val == 0 ) { 
						pdsi_val = DataType.MISSING;
					}
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}
					assertEquals("value does not match oracle for station "+str, oracle_val, pdsi_val, 0.05f);
				}
			}
		}
	}
	
	public void testOneWeekZNDI() throws Exception { 
		PalmerDroughtSeverityIndex pdsi = new PalmerDroughtSeverityIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_PRECIP_P");
		HashMap<String, float[][]> temp_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_TEMP_P");
		HashMap<String, float[][]> average_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_AVG_TEMP_P");
		HashMap<String, float[][]> additional_inputs = (HashMap<String, float[][]>) ORACLE.get("PDSI_ADDITIONAL_INPUTS");
		HashMap<String, float[][]> zndi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("ZIND_W_1");
	
		
		for ( String str : precip_data.keySet() ) {
			float[][] pdsi_oracle = zndi_oracle_data.get(str);
			float[][] precipitationData=precip_data.get(str);
			float[][] temperatureAverage=average_data.get(str);
			float[][] temperatureData=temp_data.get(str);
			float[][] add=additional_inputs.get(str);
			pdsi.setData(precipitationData, temperatureData, temperatureAverage[0], add[1][0], add[0][0]);
			float[][] pdsi_data = pdsi.weeklyZNDI(1);
			for ( int i=0; i<pdsi_data.length; i++ ) { 
				for ( int j=0; j<pdsi_data[i].length; j++ ) { 
					float oracle_val = pdsi_oracle[i][j];
					float pdsi_val = pdsi_data[i][j];
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}
					assertEquals("value does not match oracle for station "+str, oracle_val, pdsi_val, 0.05f);
			
				}
				
			}
		}
	}


	
	public void testOneWeekPDSI() throws Exception { 
		PalmerDroughtSeverityIndex pdsi = new PalmerDroughtSeverityIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_PRECIP_P");
		HashMap<String, float[][]> temp_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_TEMP_P");
		HashMap<String, float[][]> average_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_AVG_TEMP_P");
		HashMap<String, float[][]> additional_inputs = (HashMap<String, float[][]>) ORACLE.get("PDSI_ADDITIONAL_INPUTS");
		HashMap<String, float[][]> pdsi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("PDSI_W_1");
	
		for ( String str : precip_data.keySet() ) {
			float[][] pdsi_oracle = pdsi_oracle_data.get(str);
			float[][] precipitationData=precip_data.get(str);
			float[][] temperatureAverage=average_data.get(str);
			float[][] temperatureData=temp_data.get(str);
			float[][] add=additional_inputs.get(str);
			pdsi.setData(precipitationData, temperatureData, temperatureAverage[0], add[1][0], add[0][0]);
			float[][] pdsi_data = pdsi.weeklyPDSI(1);
			for ( int i=0; i<pdsi_data.length; i++ ) { 
				for ( int j=0; j<pdsi_data[i].length; j++ ) { 
					float oracle_val = pdsi_oracle[i][j];
					float pdsi_val = pdsi_data[i][j];
					if ( pdsi_val == 0 ) { 
						pdsi_val = DataType.MISSING;
					}
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}
					assertEquals(i+"value does not match oracle for station "+str, oracle_val, pdsi_val, 0.01f);
				}
			}
		}
	}
	
	public void testTwoWeekPDSI() throws Exception { 
		PalmerDroughtSeverityIndex pdsi = new PalmerDroughtSeverityIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_PRECIP_P");
		HashMap<String, float[][]> temp_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_TEMP_P");
		HashMap<String, float[][]> average_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_AVG_TEMP_P");
		HashMap<String, float[][]> additional_inputs = (HashMap<String, float[][]>) ORACLE.get("PDSI_ADDITIONAL_INPUTS");
		HashMap<String, float[][]> pdsi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("PDSI_W_2");
		
		for ( String str : precip_data.keySet() ) {
			float[][] pdsi_oracle = pdsi_oracle_data.get(str);
			float[][] precipitationData=precip_data.get(str);
			float[][] temperatureAverage=average_data.get(str);
			float[][] temperatureData=temp_data.get(str);
			float[][] add=additional_inputs.get(str);
			pdsi.setData(precipitationData, temperatureData, temperatureAverage[0], add[1][0], add[0][0]);
			float[][] pdsi_data = pdsi.weeklyPDSI(2);
			for ( int i=0; i<pdsi_data.length; i++ ) { 
				for ( int j=0; j<pdsi_data[i].length; j++ ) { 
					float oracle_val = pdsi_oracle[i][j];
					float pdsi_val = pdsi_data[i][j];
					if ( pdsi_val == 0 ) { 
						pdsi_val = DataType.MISSING;
					}
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}
					
					assertEquals("value does not match oracle for station "+str, oracle_val, pdsi_val, 0.01f);
				}
			}
		}
	}
	
	public void testTwoWeekZNDI() throws Exception { 
		PalmerDroughtSeverityIndex pdsi = new PalmerDroughtSeverityIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_PRECIP_P");
		HashMap<String, float[][]> temp_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_TEMP_P");
		HashMap<String, float[][]> average_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_AVG_TEMP_P");
		HashMap<String, float[][]> additional_inputs = (HashMap<String, float[][]>) ORACLE.get("PDSI_ADDITIONAL_INPUTS");
		HashMap<String, float[][]> pdsi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("ZIND_W_2");
		
		for ( String str : precip_data.keySet() ) {
			
			float[][] pdsi_oracle = pdsi_oracle_data.get(str);
			float[][] precipitationData=precip_data.get(str);
			float[][] temperatureAverage=average_data.get(str);
			float[][] temperatureData=temp_data.get(str);
			float[][] add=additional_inputs.get(str);
			pdsi.setData(precipitationData, temperatureData, temperatureAverage[0], add[1][0], add[0][0]);
			float[][] pdsi_data = pdsi.weeklyZNDI(2);
			for ( int i=0; i<pdsi_data.length; i++ ) { 
				for ( int j=0; j<pdsi_data[i].length; j++ ) { 
					float oracle_val = pdsi_oracle[i][j];
					float pdsi_val = pdsi_data[i][j];
					
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}
					
					assertEquals("value does not match oracle for station "+str, oracle_val, pdsi_val, 0.05f);
				}
			}
		}
		
	}
	
	public void testFourWeekPDSI() throws Exception { 
		PalmerDroughtSeverityIndex pdsi = new PalmerDroughtSeverityIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_PRECIP_P");
		HashMap<String, float[][]> temp_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_TEMP_P");
		HashMap<String, float[][]> average_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_AVG_TEMP_P");
		HashMap<String, float[][]> additional_inputs = (HashMap<String, float[][]>) ORACLE.get("PDSI_ADDITIONAL_INPUTS");
		HashMap<String, float[][]> pdsi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("PDSI_W_4");
		
		for ( String str : precip_data.keySet() ) {
			float[][] pdsi_oracle = pdsi_oracle_data.get(str);
			float[][] precipitationData=precip_data.get(str);
			float[][] temperatureAverage=average_data.get(str);
			float[][] temperatureData=temp_data.get(str);
			float[][] add=additional_inputs.get(str);
			pdsi.setData(precipitationData, temperatureData, temperatureAverage[0], add[1][0], add[0][0]);
			float[][] pdsi_data = pdsi.weeklyPDSI(4);
			for ( int i=0; i<pdsi_data.length; i++ ) { 
				for ( int j=0; j<pdsi_data[i].length; j++ ) { 
					float oracle_val = pdsi_oracle[i][j];
					float pdsi_val = pdsi_data[i][j];
					if ( pdsi_val == 0 ) { 
						pdsi_val = DataType.MISSING;
					}
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}

					assertEquals("value does not match oracle for station "+str, oracle_val, pdsi_val, 0.01f);
				}
			}
		}
	}
	public void testFourWeekZNDI() throws Exception { 
		PalmerDroughtSeverityIndex pdsi = new PalmerDroughtSeverityIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_PRECIP_P");
		HashMap<String, float[][]> temp_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_TEMP_P");
		HashMap<String, float[][]> average_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_AVG_TEMP_P");
		HashMap<String, float[][]> additional_inputs = (HashMap<String, float[][]>) ORACLE.get("PDSI_ADDITIONAL_INPUTS");
		HashMap<String, float[][]> pdsi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("ZIND_W_4");
		
		for ( String str : precip_data.keySet() ) {
			
			float[][] pdsi_oracle = pdsi_oracle_data.get(str);
			float[][] precipitationData=precip_data.get(str);
			float[][] temperatureAverage=average_data.get(str);
			float[][] temperatureData=temp_data.get(str);
			float[][] add=additional_inputs.get(str);
			pdsi.setData(precipitationData, temperatureData, temperatureAverage[0], add[1][0], add[0][0]);
			float[][] pdsi_data = pdsi.weeklyZNDI(4);
			for ( int i=0; i<pdsi_data.length; i++ ) { 
				for ( int j=0; j<pdsi_data[i].length; j++ ) { 
					float oracle_val = pdsi_oracle[i][j];
					float pdsi_val = pdsi_data[i][j];
					
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}
					
					assertEquals("value does not match oracle for station "+str, oracle_val, pdsi_val, 0.05f);
				}
			}
		}
		
	}
	public void testThirteenWeekPDSI() throws Exception { 
		PalmerDroughtSeverityIndex pdsi = new PalmerDroughtSeverityIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_PRECIP_P");
		HashMap<String, float[][]> temp_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_TEMP_P");
		HashMap<String, float[][]> average_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_AVG_TEMP_P");
		HashMap<String, float[][]> additional_inputs = (HashMap<String, float[][]>) ORACLE.get("PDSI_ADDITIONAL_INPUTS");
		HashMap<String, float[][]> pdsi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("PDSI_W_13");
		
		for ( String str : precip_data.keySet() ) {
			float[][] pdsi_oracle = pdsi_oracle_data.get(str);
			float[][] precipitationData=precip_data.get(str);
			float[][] temperatureAverage=average_data.get(str);
			float[][] temperatureData=temp_data.get(str);
			float[][] add=additional_inputs.get(str);
			pdsi.setData(precipitationData, temperatureData, temperatureAverage[0], add[1][0], add[0][0]);
			float[][] pdsi_data = pdsi.weeklyPDSI(13);
			for ( int i=0; i<pdsi_data.length; i++ ) { 
				for ( int j=0; j<pdsi_data[i].length; j++ ) { 
					float oracle_val = pdsi_oracle[i][j];
					float pdsi_val = pdsi_data[i][j];
					if ( pdsi_val == 0 ) { 
						pdsi_val = DataType.MISSING;
					}
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}

					assertEquals("value does not match oracle for station "+str, oracle_val, pdsi_val, 0.01f);
				}
			}
		}
	}
	
	public void testThirteenWeekZNDI() throws Exception { 
		PalmerDroughtSeverityIndex pdsi = new PalmerDroughtSeverityIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_PRECIP_P");
		HashMap<String, float[][]> temp_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_TEMP_P");
		HashMap<String, float[][]> average_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_AVG_TEMP_P");
		HashMap<String, float[][]> additional_inputs = (HashMap<String, float[][]>) ORACLE.get("PDSI_ADDITIONAL_INPUTS");
		HashMap<String, float[][]> pdsi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("ZIND_W_13");
		
		for ( String str : precip_data.keySet() ) {
			float[][] pdsi_oracle = pdsi_oracle_data.get(str);
			float[][] precipitationData=precip_data.get(str);
			float[][] temperatureAverage=average_data.get(str);
			float[][] temperatureData=temp_data.get(str);
			float[][] add=additional_inputs.get(str);
			pdsi.setData(precipitationData, temperatureData, temperatureAverage[0], add[1][0], add[0][0]);
			float[][] pdsi_data = pdsi.weeklyZNDI(13);
			for ( int i=0; i<pdsi_data.length; i++ ) { 
				for ( int j=0; j<pdsi_data[i].length; j++ ) { 
					float oracle_val = pdsi_oracle[i][j];
					float pdsi_val = pdsi_data[i][j];
					if ( pdsi_val == 0 ) { 
						pdsi_val = DataType.MISSING;
					}
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}

					assertEquals("value does not match oracle for station "+str, oracle_val, pdsi_val, 0.01f);
				}
			}
		}
	}
		
}
