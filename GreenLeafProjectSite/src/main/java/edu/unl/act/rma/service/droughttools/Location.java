package edu.unl.act.rma.service.droughttools;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Location")
public class Location {

	private float longitude;
	private float latitude;
	private String state;
	private String county;
	
	public float getLongitude() {
		return longitude;
	}
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}
	public float getLatitude() {
		return latitude;
	}
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCounty() {
		return county;
	}
	public void setCounty(String county) {
		this.county = county;
	}
}
