/* Created On: Sep 12, 2005 */
package edu.unl.act.rma.firm.drought.component;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.climate.component.ClimateDataQuery;
import edu.unl.act.rma.firm.climate.component.ClimateMetaDataQuery;
import edu.unl.act.rma.firm.climate.configuration.ClimateServiceAccessor;
import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.StationMetaDataType;
import edu.unl.act.rma.firm.core.TemporalPeriod;
import edu.unl.act.rma.firm.core.configuration.ConfigurationException;

/**
 * @see edu.unl.firm.terra.SoilsDataQuery
 * 
 * @author Ian Cottingham
 *
 */
@Stateless
@Local({LocalSoilsDataQuery.class})
@Remote({SoilsDataQuery.class})
public class SoilsDataQueryBean implements LocalSoilsDataQuery {

	private static final float[] PHI = { -0.3865982f, -0.2316132f, -0.0378180f, 0.1715539f, 0.3458803f, 
										0.4308320f, 0.3916645f, 0.2452467f, 0.0535511f, -0.15583436f, 
										-0.3340551f, -0.4310691f };
	

	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG, SoilsDataQueryBean.class);

	private DataSource source = DataSourceInjector.injectDataSource(DataSourceTypes.SOIL);
	private DataSource climate_source = DataSourceInjector.injectDataSource(DataSourceTypes.CLIMATIC);
	
	/**
	 * @see SoilsDataQuery#getWaterHoldingCapacity(List)
	 */
	public HashMap<String, Float> getWaterHoldingCapacity(List<String> stationIds) throws RemoteException {
		Connection conn = null;
		Connection climate_conn = null;
		HashMap<String, Float> data = new HashMap<String, Float>();
		PreparedStatement stmt = null;
		PreparedStatement station_lookup = null;
		ResultSet station_query = null;
		ResultSet network_id_result = null;
		try { 
			conn = source.getConnection();
			climate_conn = climate_source.getConnection();
			
			station_lookup = climate_conn.prepareStatement("select network_id from network_station_link where network_station_link.station_id = ?");
			
			stmt = conn.prepareStatement("select value from awc where awc.network_id = ?");
			
			for ( String station : stationIds ) { 
				station_lookup.setString(1, station);
				network_id_result = station_lookup.executeQuery();
				if ( !network_id_result.next() ) { 
					LOG.warn("no network ID found for station ID: "+station);
					continue;
				}
				
				stmt.setString(1, network_id_result.getString(1));
				station_query = stmt.executeQuery();
				
				if ( station_query.next() ) { 
					data.put(station, station_query.getFloat(1));
				} else {
					data.put(station, DataType.DEFAULT_AWC);
				}
			}
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying meta data", sqe);
			throw new RemoteException("unable to query metadata from datasource");
		} finally { 
			try {
				if ( station_query != null ) { 
					station_query.close();
				}
				
				if ( stmt != null ) { 
					stmt.close();
				}
				
				if ( conn != null ) {
					conn.close();
				}
				
				if ( climate_conn != null ) { 
					climate_conn.close();
				}
			} catch ( SQLException sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
		
		return data;
	}

	/**
	 * @see SoilsDataQuery#getCurrentSoilMoisture(List, DateTime)
	 */
	public HashMap<String, Float> getCurrentSoilMoisture(List<String> stationIds, DateTime date) throws RemoteException {
		HashMap<String, Float> ret = new HashMap<String, Float>();
		
		/* the first step is to get the neccessary data for the computation 
		 * this data includes:
		 * 
		 * 1) the AWC values for each station
		 * 2) the latitude of each station
		 * 3) the normal temperature for the day being considered for each station
		 * 4) the precipitation on the day being considered
		 * 5) the high temperature on the day being considered
		 * */
		TemporalPeriod longest_period = null;
		try { 
			longest_period = ClimateServiceAccessor.getInstance().getClimateMetaDataQuery().getLongestPeriod(stationIds, CalendarPeriod.DAILY);
		} catch ( ConfigurationException jme ) { 
			LOG.error("could not get ClimateMetaDataObject", jme);
			throw new RemoteException("could not get ClimateMetaDataObject");
		} catch ( InstantiationException ie ) { 
			LOG.error("could not instantiate a DataServiceAccessor", ie);
			throw new RemoteException("could not instantiate a DataServiceAccessor");
		} catch ( InvalidArgumentException iae ) {
			LOG.error("coult not get longest period, invalid argument", iae);
			throw new RemoteException("could not get the longest period");
		}
		
		if(( date.compareTo(longest_period.getStart()) >=0) && ( date.compareTo(longest_period.getEnd()) <=0)){
			Map<String, Float> awc_values = getWaterHoldingCapacity(stationIds);
			Map<String, Object> station_lats = null;
			Map<String, Float> precip = new HashMap<String, Float>();
			Map<String, Float> high_temp = new HashMap<String, Float>();
			CalendarDataCollection normals = null;
			try {
				ClimateServiceAccessor accessor = ClimateServiceAccessor.getInstance();
				ClimateMetaDataQuery cmdo = accessor.getClimateMetaDataQuery();
				ClimateDataQuery cdo = accessor.getClimateDataQuery();
				
				station_lats = cmdo.getMetaData(stationIds, StationMetaDataType.LATITUDE, CalendarPeriod.DAILY).extractType(StationMetaDataType.LATITUDE);
				CalendarDataCollection cdc = cdo.getPeriodData(stationIds, date, date, DataType.PRECIP, CalendarPeriod.DAILY);
							
				/* add the daily precip value for the calculation day */
				for ( String s : cdc ) { 
					precip.put(s, extractValue(cdc.getDataMatrix(s)[0], date));
			    }

				/* add the daily high temp value for the calculation day */
				cdc = cdo.getHistoricalAverageData(stationIds, DataType.HIGH_TEMP, CalendarPeriod.ANNUALLY);
				for ( String s : cdc ) { 
					high_temp.put(s, cdc.getDataMatrix(s)[0][0]);
				}
				
				/* add the daily normal temp values */
				normals = cdo.getHistoricalAverageData(stationIds, DataType.NORMAL_TEMP, CalendarPeriod.DAILY);

			} catch ( RemoteException re ) { 
				LOG.error("exception getting station data", re);
				throw new RemoteException("exception getting station data");
			} catch ( InstantiationException ie ) { 
				LOG.error("could not instantiate the DataServiceAccessor", ie);
				throw new RemoteException("could not instantiate the DataServiceAccessor");
			} catch ( InvalidArgumentException ive ) { 
				LOG.error("could not query climate data", ive);
				throw new RemoteException("an error prevented the climate data from being queried");
			}
			
			float TLA, Ss, Su = 0f;
			float lat, prec, wc = 0f;
			int south = 0;
			
			for ( String station : stationIds ) { 
				if ( station_lats.get(station) == null ) { 
					//TODO: remove float cast when MISSING is updated
					ret.put(station, (float)DataType.MISSING);
					continue;
				} else { 
					lat = (Float)station_lats.get(station);
				}
				
				prec = precip.get(station);
				float tave = high_temp.get(station);
				wc = awc_values.get(station);
						
				if ( wc != DataType.MISSING ) { 
					TLA = (float)-Math.tan(lat);
				} else {
					//TODO: remove float cast when MISSING is updated
					ret.put(station, (float)DataType.MISSING);
					continue;
				}
				
				Ss = 1.0f; // assume the top soil can hold 1in
				
				if ( wc < Ss ) { 
					wc = Ss; // the soil should be able to hold at least the default Ss value
				}
				
				Su = (float)wc - Ss;
				Su = ( Su < 0 ) ? 0 : Su; // if Su is less than 0 then Su is set to 0
			    
				/* I am pretty sure this calculation is for southern hemisphere points, 
				 * which do not exist for the purposes of FIRM.  This is a bit of logic
				 * from the original code
				 */
				//TODO: test this assumption, and remove is this is the case
				if ( TLA > 0 ) { 
					south = 1;
					TLA = -TLA;
				} else { 
					south = 0;
				}
				
				float I = 0f;
				int count = 0;
				for ( float[] d : normals.getStationData(station) ) { 
					for ( int i=0; i<d.length; i++ ) { 
						if ( d[i] != DataType.MISSING ) { 
							 count++;
						}
						I = ( d[i] > 32 ) ? I+(float)Math.pow((d[i] - 32) / 9, 1.514) : I;
					}
				}
				
				/* make sure it's an annual estimate */
				if ( count > 0 ) { 
					I = I / count * 12;
				}
				float A = (float)(6.75 * (Math.pow(I, 3)) / 10000000 - 7.71 * (Math.pow(I, 2)) / 100000 + 0.0179 * I + 0.49);
				float Dum, Dk = 0;
				float PE;
				int offset = ( south > 0 ) ? 6 : 0;
				
				if ( tave <= 32 ) { 
				    PE = 0;
				} else { 
					Dum = PHI[(date.getMonthOfYear()-1 + offset) % 12] * TLA;
					Dk = (float)Math.atan(Math.sqrt(Math.abs( 1- Dum * Dum)) / Dum);
				
					if ( Dk < 0 ) {
						Dk += Math.PI;
					}
				
					Dk = (float)(Dk + 0.0157f) / 1.57f;
								
					if ( tave >= 80 ) { 
						PE = (float)(Math.sin(tave / 57.3 - 0.166) - 0.76) * Dk;
					} else { 
						Dum = (float)Math.log(tave-32);
						PE = (float)(Math.exp(-3.863233 + A * 1.715598 - A * Math.log(I) + A * Dum)) * Dk;
					}
				}
				PE *= date.monthOfYear().getMaximumValue();
						
				float PRO = (Ss+Su);
				float PL = 0.0f;
					
				if ( Ss >= PE ) { 
					PL = PE;
				} else { 
					PL = ((PE - Ss) * Su) / wc + Ss;
					// if PL > PRO then PL > water in the soil, which isn't possible, so PL is set to the water in the soil
					PL = ( PL > PRO ) ? PRO : PL;
				}
				float R_surface, R_under, surface_L, under_L = 0.0f;
				float new_Su, new_Ss;
				
				if ( prec >= PE ) { 
				
					if ( (prec - PE) > (1-Ss) ) {
						/* The excess precip will recharge both layers. (Note -Ss is the amount of 
						 * water needed to saturate the top layer of soil assuming it can only hold 
						 * 1in. of water.
						 */
						R_surface=1.0f-Ss;
						new_Ss = 1.0f;
						if ( (prec - PE - R_surface) < ((wc - 1.0) - Su)) { 
							/* The entire amount of precip can be absorbed by the soil (no runoff) and 
							 * the underlying layer will receive what's left after the top layer
							 * Note: (AWC - 1.0) is the amount able to be stored in the lower layer.
							 */
							R_under = (prec - PE - R_surface);
							
						} else { 
							/* the underlying layer is fully recharged and some runoff will occur */
							R_under = (wc - 1.0f) - Su;
							
						}
						new_Su = (float)(Su + R_under);
					} else { 
						/* there is only enough moisture to recharge some fo the top layer */
						new_Ss = (float)(Ss + prec - PE);
						new_Su = Su;
					}
				} else { 
					/* the evapotranspiration is greater than the precipitation. This means some moisture loos 
					 * will occur from the soil.
					 */
					if ( Ss > (PE - prec)) { 
						/* the moisture from the top layer is enough to meet the remaining PE so only the top layer
						 * loses moisture.
						 */
						surface_L = PE - prec;
						under_L = 0.0f;
						new_Ss = (float)(Ss - surface_L);
						new_Su = Su;
						
					} else { 
						/* The top layer is drained, so the underlying layer loses moisture also */
						surface_L = Ss;
						under_L = (PE - prec - surface_L) * Su / wc;
						if ( Su < under_L ) {
							under_L = Su;
						}
						new_Ss = 0.0f;
						new_Su = (float)(Su - under_L);
					}
				}
			
				Ss = new_Ss;
				Su = new_Su;
				
				ret.put(station, Su+Ss);
			}
		}
		else{
			for (String all_stations : stationIds) {
			  	ret.put(all_stations,(float)DataType.MISSING);
			}
		}
     	return ret;
	}
	
	private float extractValue(float[] data, DateTime date) {
		int index = date.getDayOfYear() - 1;
		// if date is not on a leap year and after 2-28, add 1 to account for missing value 
		if (date.getYear() % 4 > 0 && date.getDayOfYear() > 59) {
			index++;
		}
		return data[index];
	}
}
