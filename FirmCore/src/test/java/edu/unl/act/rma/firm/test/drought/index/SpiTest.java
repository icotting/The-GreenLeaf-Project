/* Created on Nov 21, 2008 */
package edu.unl.act.rma.firm.test.drought.index;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

import junit.framework.TestCase;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.drought.index.StandardizedPrecipitationIndex;

/**
 * 
 * @author Ian Cottingham
 *
 */
public class SpiTest extends TestCase {

	private static HashMap<String, Object> ORACLE;
	
	static { 
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SpiTest.class.getClassLoader().getResource("DroughtTestOracle").getPath()));
			ORACLE = (HashMap<String, Object>)ois.readObject();
		} catch  ( Exception e ) { 
			RuntimeException re = new RuntimeException("could not load the data oracle");
			re.initCause(e);
			throw re;
		}
	}
	
	public void testOneMonthSpi() throws Exception { 
		StandardizedPrecipitationIndex spi = new StandardizedPrecipitationIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("MONTHLY_PRECIP");
		HashMap<String, float[][]> spi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("SPI_M_1");
		
		for ( String str : precip_data.keySet() ) {
			float[][] spi_oracle = spi_oracle_data.get(str);
			spi.setData(precip_data.get(str));
			float[][] spi_data = spi.computeSpi(1);
			
			for ( int i=0; i<spi_data.length; i++ ) { 
				for ( int j=0; j<spi_data[i].length; j++ ) { 
					float oracle_val = spi_oracle[i][j];
					float spi_val = spi_data[i][j];
					if ( spi_val == 0 ) { 
						spi_val = DataType.MISSING;
					}
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}

					assertEquals("value does not match oracle for station "+str, oracle_val, spi_val, 0.01f);
				}
			}
			
		}
	}
	
	public void testTwoMonthSpi() throws Exception { 
		StandardizedPrecipitationIndex spi = new StandardizedPrecipitationIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("MONTHLY_PRECIP");
		HashMap<String, float[][]> spi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("SPI_M_2");
		
		int diff = 0;
		for ( String str : precip_data.keySet() ) {
			float[][] spi_oracle = spi_oracle_data.get(str);
			spi.setData(precip_data.get(str));
			float[][] spi_data = spi.computeSpi(2);
			
			for ( int i=0; i<spi_data.length; i++ ) { 
				for ( int j=0; j<spi_data[i].length; j++ ) { 
					float oracle_val = spi_oracle[i][j];
					float spi_val = spi_data[i][j];
					if ( spi_val == 0 ) { 
						spi_val = DataType.MISSING;
					}
					
					if ( spi_val == DataType.MISSING && oracle_val == 0 ) { 
						oracle_val = DataType.MISSING;
					}
					
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}

					assertEquals("value does not match oracle for station "+str, oracle_val, spi_val, 0.01f);
				}
			}
		}
		
		System.out.println(diff);
	}
	
	public void testFourMonthSpi() throws Exception { 
		StandardizedPrecipitationIndex spi = new StandardizedPrecipitationIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("MONTHLY_PRECIP");
		HashMap<String, float[][]> spi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("SPI_M_4");
		
		for ( String str : precip_data.keySet() ) {
			float[][] spi_oracle = spi_oracle_data.get(str);
			spi.setData(precip_data.get(str));
			float[][] spi_data = spi.computeSpi(4);
			
			for ( int i=0; i<spi_data.length; i++ ) { 
				for ( int j=0; j<spi_data[i].length; j++ ) { 
					float oracle_val = spi_oracle[i][j];
					float spi_val = spi_data[i][j];
					if ( spi_val == 0 ) { 
						spi_val = DataType.MISSING;
					}
					
					if ( spi_val == DataType.MISSING && oracle_val == 0 ) { 
						oracle_val = DataType.MISSING;
					}
					
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}

					assertEquals("value does not match oracle for station "+str, oracle_val, spi_val, 0.01f);
				}
			}
			
		}
	}
	
	public void testSixMonthSpi() throws Exception { 
		StandardizedPrecipitationIndex spi = new StandardizedPrecipitationIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("MONTHLY_PRECIP");
		HashMap<String, float[][]> spi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("SPI_M_6");
		
		for ( String str : precip_data.keySet() ) {
			float[][] spi_oracle = spi_oracle_data.get(str);
			spi.setData(precip_data.get(str));
			float[][] spi_data = spi.computeSpi(6);
			
			for ( int i=0; i<spi_data.length; i++ ) { 
				for ( int j=0; j<spi_data[i].length; j++ ) { 
					float oracle_val = spi_oracle[i][j];
					float spi_val = spi_data[i][j];
					if ( spi_val == 0 ) { 
						spi_val = DataType.MISSING;
					}
					
					if ( spi_val == DataType.MISSING && oracle_val == 0 ) { 
						oracle_val = DataType.MISSING;
					}
					
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}

					assertEquals("value does not match oracle for station "+str, oracle_val, spi_val, 0.01f);
				}
			}
			
		}
	}
	
	public void testTwelveMonthSpi() throws Exception { 
		StandardizedPrecipitationIndex spi = new StandardizedPrecipitationIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("MONTHLY_PRECIP");
		HashMap<String, float[][]> spi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("SPI_M_12");
		
		for ( String str : precip_data.keySet() ) {
			float[][] spi_oracle = spi_oracle_data.get(str);
			spi.setData(precip_data.get(str));
			float[][] spi_data = spi.computeSpi(12);
			
			for ( int i=0; i<spi_data.length; i++ ) { 
				for ( int j=0; j<spi_data[i].length; j++ ) { 
					float oracle_val = spi_oracle[i][j];
					float spi_val = spi_data[i][j];
					if ( spi_val == 0 ) { 
						spi_val = DataType.MISSING;
					}
					
					if ( spi_val == DataType.MISSING && oracle_val == 0 ) { 
						oracle_val = DataType.MISSING;
					}
					
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}

					assertEquals("value does not match oracle for station "+str, oracle_val, spi_val, 0.01f);
				}
			}
			
		}
	}
	
	public void testTwoWeekSpi() throws Exception { 
		StandardizedPrecipitationIndex spi = new StandardizedPrecipitationIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_PRECIP");
		HashMap<String, float[][]> spi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("SPI_W_2");
		
		for ( String str : precip_data.keySet() ) {
			float[][] spi_oracle = spi_oracle_data.get(str);
			spi.setData(precip_data.get(str));
			float[][] spi_data = spi.computeSpi(2);
			
			for ( int i=0; i<spi_data.length; i++ ) { 
				for ( int j=0; j<spi_data[i].length; j++ ) { 
					float oracle_val = spi_oracle[i][j];
					float spi_val = spi_data[i][j];
					if ( spi_val == 0 ) { 
						spi_val = DataType.MISSING;
					}
					
					if ( spi_val == DataType.MISSING && oracle_val == 0 ) { 
						oracle_val = DataType.MISSING;
					}
					
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}

					assertEquals("value does not match oracle for station "+str, oracle_val, spi_val, 0.01f);
				}
			}
			
		}
	}

	public void testFourWeekSpi() throws Exception { 
		StandardizedPrecipitationIndex spi = new StandardizedPrecipitationIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_PRECIP");
		HashMap<String, float[][]> spi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("SPI_W_4");
		
		for ( String str : precip_data.keySet() ) {
			float[][] spi_oracle = spi_oracle_data.get(str);
			spi.setData(precip_data.get(str));
			float[][] spi_data = spi.computeSpi(4);
			
			for ( int i=0; i<spi_data.length; i++ ) { 
				for ( int j=0; j<spi_data[i].length; j++ ) { 
					float oracle_val = spi_oracle[i][j];
					float spi_val = spi_data[i][j];
					if ( spi_val == 0 ) { 
						spi_val = DataType.MISSING;
					}
					
					if ( spi_val == DataType.MISSING && oracle_val == 0 ) { 
						oracle_val = DataType.MISSING;
					}
					
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}

					assertEquals("value does not match oracle for station "+str, oracle_val, spi_val, 0.01f);
				}
			}
			
		}
	}
	
	public void testEightWeekSpi() throws Exception { 
		StandardizedPrecipitationIndex spi = new StandardizedPrecipitationIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_PRECIP");
		HashMap<String, float[][]> spi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("SPI_W_8");
		
		for ( String str : precip_data.keySet() ) {
			float[][] spi_oracle = spi_oracle_data.get(str);
			spi.setData(precip_data.get(str));
			float[][] spi_data = spi.computeSpi(8);
			
			for ( int i=0; i<spi_data.length; i++ ) { 
				for ( int j=0; j<spi_data[i].length; j++ ) { 
					float oracle_val = spi_oracle[i][j];
					float spi_val = spi_data[i][j];
					if ( spi_val == 0 ) { 
						spi_val = DataType.MISSING;
					}
					
					if ( spi_val == DataType.MISSING && oracle_val == 0 ) { 
						oracle_val = DataType.MISSING;
					}
					
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}

					assertEquals("value does not match oracle for station "+str, oracle_val, spi_val, 0.01f);
				}
			}
			
		}
	}
	
	public void testTwelveWeekSpi() throws Exception { 
		StandardizedPrecipitationIndex spi = new StandardizedPrecipitationIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_PRECIP");
		HashMap<String, float[][]> spi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("SPI_W_12");
		
		for ( String str : precip_data.keySet() ) {
			float[][] spi_oracle = spi_oracle_data.get(str);
			spi.setData(precip_data.get(str));
			float[][] spi_data = spi.computeSpi(12);
			
			for ( int i=0; i<spi_data.length; i++ ) { 
				for ( int j=0; j<spi_data[i].length; j++ ) { 
					float oracle_val = spi_oracle[i][j];
					float spi_val = spi_data[i][j];
					if ( spi_val == 0 ) { 
						spi_val = DataType.MISSING;
					}
					
					if ( spi_val == DataType.MISSING && oracle_val == 0 ) { 
						oracle_val = DataType.MISSING;
					}
					
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}

					assertEquals("value does not match oracle for station "+str, oracle_val, spi_val, 0.01f);
				}
			}
			
		}
	}
	
	public void testTwentySixWeekSpi() throws Exception { 
		StandardizedPrecipitationIndex spi = new StandardizedPrecipitationIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_PRECIP");
		HashMap<String, float[][]> spi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("SPI_W_26");
		
		for ( String str : precip_data.keySet() ) {
			float[][] spi_oracle = spi_oracle_data.get(str);
			spi.setData(precip_data.get(str));
			float[][] spi_data = spi.computeSpi(26);
			
			for ( int i=0; i<spi_data.length; i++ ) { 
				for ( int j=0; j<spi_data[i].length; j++ ) { 
					float oracle_val = spi_oracle[i][j];
					float spi_val = spi_data[i][j];
					if ( spi_val == 0 ) { 
						spi_val = DataType.MISSING;
					}
					
					if ( spi_val == DataType.MISSING && oracle_val == 0 ) { 
						oracle_val = DataType.MISSING;
					}
					
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}

					assertEquals("value does not match oracle for station "+str, oracle_val, spi_val, 0.01f);
				}
			}
			
		}
	}
	
	public void testFiftyTwoWeekSpi() throws Exception { 
		StandardizedPrecipitationIndex spi = new StandardizedPrecipitationIndex(1893);
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("WEEKLY_PRECIP");
		HashMap<String, float[][]> spi_oracle_data = (HashMap<String, float[][]>) ORACLE.get("SPI_W_52");
		
		for ( String str : precip_data.keySet() ) {
			float[][] spi_oracle = spi_oracle_data.get(str);
			spi.setData(precip_data.get(str));
			float[][] spi_data = spi.computeSpi(52);
			
			for ( int i=0; i<spi_data.length; i++ ) { 
				for ( int j=0; j<spi_data[i].length; j++ ) { 
					float oracle_val = spi_oracle[i][j];
					float spi_val = spi_data[i][j];
					if ( spi_val == 0 ) { 
						spi_val = DataType.MISSING;
					}
					
					if ( spi_val == DataType.MISSING && oracle_val == 0 ) { 
						oracle_val = DataType.MISSING;
					}
					
					if ( oracle_val == DataType.OUTSIDE_OF_RANGE || oracle_val == DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						oracle_val = DataType.MISSING;
					}

					assertEquals("value does not match oracle for station "+str, oracle_val, spi_val, 0.01f);
				}
			}
			
		}
	}
}
