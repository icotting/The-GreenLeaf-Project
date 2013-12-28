package edu.unl.act.rma.console.usgs;

import java.util.Set;

import org.joda.time.DateTime;

public class USGSDailyResult {
	private String resultID;
	private String agency;
	private String StationID;
	private DateTime date;
	private Set<USGSVariableResult> variables;
	
	
	public String getResultID() {
		return resultID;
	}
	public void setResultID(String resultID) {
		this.resultID = resultID;
	}
	public String getStationID() {
		return StationID;
	}
	public void setStationID(String stationID) {
		StationID = stationID;
	}
	public String getAgency() {
		return agency;
	}
	public void setAgency(String agency) {
		this.agency = agency;
	}
	public DateTime getDate() {
		return date;
	}
	public void setDate(DateTime date) {
		this.date = date;
	}
	public Set<USGSVariableResult> getVariables() {
		return variables;
	}
	public void setVariables(Set<USGSVariableResult> variables) {
		this.variables = variables;
	}
	
}
