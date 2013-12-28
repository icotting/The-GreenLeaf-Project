/* Created On: Sep 11, 2005 */
package edu.unl.act.rma.firm.core;

import java.io.ObjectStreamException;

import org.joda.time.DateTime;

/**
 * @author Ian Cottingham
 *
 */
public enum StationMetaDataType {

	NETWORK_ID("network_station_link.network_id", "Network ID", Integer.class),
	STATION_ID("station.station_id", "Station ID", String.class), 
	STATION_NAME("station.station_name", "Station Name", String.class), 
	STATE("station.state", "State", String.class), 
	COUNTY("station.county", "County", String.class), 
	LONGITUDE("absolute_location.longitude", "Longitude", Double.class), 
	LATITUDE("absolute_location.latitude", "Latitude", Double.class), 
	ELEVATION("absolute_location.elevation", "Elevation", Double.class), 
	CLIMATE_DIV("station.climate_div", "Climate Divison", Integer.class), 
	ABS_START_DATE("station.abs_start_date", "Start of Record", DateTime.class), 
	ABS_END_DATE("station.abs_end_date", "End of Record", DateTime.class), 
	START_DATE("location_station_link.start_date", "Station Start Date", DateTime.class), 
	END_DATE("location_station_link.end_date", "Station End Date", DateTime.class), 
	NETWORK_NAME("network_type.network_name", "Network", String.class);
	
	private String fieldName;
	private Class type;
	private String printName;
	
	private StationMetaDataType(String str, String printName, Class type) { 
		this.fieldName = str;
		this.type = type;
		this.printName = printName;
	}
	
	public String getFieldName() { 
		return this.fieldName;
	}
	
	public Class getType() { 
		return type;
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
	
	@Override
	public String toString() { return this.printName; }
}
