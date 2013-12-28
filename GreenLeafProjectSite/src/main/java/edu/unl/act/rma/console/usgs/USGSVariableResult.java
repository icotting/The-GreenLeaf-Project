package edu.unl.act.rma.console.usgs;

import org.joda.time.DateTime;

public class USGSVariableResult {
	private String variable_id;
	private USGSVariableEnum type;
	private String station_id;
	private DateTime start_date;
	private DateTime end_date;
	private double missing_percent;
	
	public String getVariableID() {
		return variable_id;
	}
	public void setVariableID(String variableId) {
		variable_id = variableId;
	}
	public USGSVariableEnum getType() {
		return type;
	}
	public void setType(USGSVariableEnum type) {
		this.type = type;
	}
	public String getStationID() {
		return station_id;
	}
	public void setStationID(String stationId) {
		station_id = stationId;
	}
	public DateTime getStartDate() {
		return start_date;
	}
	public void setStartDate(DateTime startDate) {
		start_date = startDate;
	}
	public DateTime getEndDate() {
		return end_date;
	}
	public void setEndDate(DateTime endDate) {
		end_date = endDate;
	}
	public double getMissingPercent() {
		return missing_percent;
	}
	public void setMissingPercent(double missingPercent) {
		missing_percent = missingPercent;
	}

}
