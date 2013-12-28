package edu.unl.act.rma.console.usgs;

import java.util.Date;

import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;

public class USGSStreamFlowStation {
	private String stationID;
	private String stationName;
	private String agencyCode;
	private USState state;
	private USCounty county;
	private float latitude;
	private float longitude;
	private String coordinateAccuracy;
	private String latLongDatumCode;
	private float altitude;
	private float altitudeAccuracy;
	private String altitudeDatum;
	private String hydrologicUnit;
	private float drainageArea;
	private float contributingDrainageArea;
	private Date dateStart;
	private Date dateEnd;
	
	public String getStationID() {
		return stationID;
	}
	public void setStationID(String stationID) {
		this.stationID = stationID;
	}
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	public String getAgencyCode() {
		return agencyCode;
	}
	public void setAgencyCode(String agencyCode) {
		this.agencyCode = agencyCode;
	}
	public USState getState() {
		return state;
	}
	public void setState(USState state) {
		this.state = state;
	}
	public USCounty getCounty() {
		return county;
	}
	public void setCounty(USCounty county) {
		this.county = county;
	}
	public float getLatitude() {
		return latitude;
	}
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	public float getLongitude() {
		return longitude;
	}
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}
	public String getCoordinateAccuracy() {
		return coordinateAccuracy;
	}
	public void setCoordinateAccuracy(String coordinateAccuracy) {
		this.coordinateAccuracy = coordinateAccuracy;
	}
	public String getLatLongDatumCode() {
		return latLongDatumCode;
	}
	public void setLatLongDatumCode(String latLongDatumCode) {
		this.latLongDatumCode = latLongDatumCode;
	}
	public float getAltitude() {
		return altitude;
	}
	public void setAltitude(float altitude) {
		this.altitude = altitude;
	}
	public float getAltitudeAccuracy() {
		return altitudeAccuracy;
	}
	public void setAltitudeAccuracy(float altitudeAccuracy) {
		this.altitudeAccuracy = altitudeAccuracy;
	}
	public String getAltitudeDatum() {
		return altitudeDatum;
	}
	public void setAltitudeDatum(String altitudeDatum) {
		this.altitudeDatum = altitudeDatum;
	}
	public String getHydrologicUnit() {
		return hydrologicUnit;
	}
	public void setHydrologicUnit(String hydrologicUnit) {
		this.hydrologicUnit = hydrologicUnit;
	}
	public float getDrainageArea() {
		return drainageArea;
	}
	public void setDrainageArea(float drainageArea) {
		this.drainageArea = drainageArea;
	}
	public float getContributingDrainageArea() {
		return contributingDrainageArea;
	}
	public void setContributingDrainageArea(float contributingDrainageArea) {
		this.contributingDrainageArea = contributingDrainageArea;
	}
	public Date getDateStart() {
		return dateStart;
	}
	public void setDateStart(Date dateStart) {
		this.dateStart = dateStart;
	}
	public Date getDateEnd() {
		return dateEnd;
	}
	public void setDateEnd(Date dateEnd) {
		this.dateEnd = dateEnd;
	}
	
}
