/* Created On: May 24, 2005 */
package edu.unl.act.rma.console.acis;

import java.io.ObjectStreamException;

/**
 * @author Ian Cottingham
 *
 */
public enum ACISMetaField {

	ACIS_ID("ucan_id"),
	STATION_NAME("station_name"),
	LATITUDE("lat"),
	LONGITUDE("lon"), 
	ELEVATION("elev"), 
	STARTDATE("begin_date"), 
	ENDDATE("end_date"), 
	VARIABLE("var_major_id"),
	IDS("ids"), 
	NETWORKID("network_id"), 
	CLIMATE_DIVISION("clim_div"),
	COUNTY("county"), 
	GMT_OFFSET("gmt_offset"),
	POSTAL("postal"),
	STATIONID("internal_station_id"),
	STATE ("state") {
		
	};
	
	private String acisName;
	
	private ACISMetaField(String str) {
		acisName = str;
	}
	
	public String getUcanName() { 
		return acisName;
	}
	
	public static ACISMetaField fromString(String str) throws NoSuchFieldException { 
		for ( ACISMetaField field : ACISMetaField.values() )
			if ( field.getUcanName().equals(str) )
				return field;
		
		throw new NoSuchFieldException("no enumeration value for "+str);
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
}
