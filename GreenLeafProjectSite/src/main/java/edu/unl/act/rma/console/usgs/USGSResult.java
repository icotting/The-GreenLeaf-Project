package edu.unl.act.rma.console.usgs;

import org.joda.time.DateTime;

public class USGSResult {
	private String daily_id;
	private USGSVariableResult variable;
	private DateTime date;
	private double value;
	private USGSDataValueQualificationCode qualification;
	private USGSDailyValueStatusCode status;

	public String getDailyID() {
		return daily_id;
	}

	public void setDailyID(String dailyId) {
		daily_id = dailyId;
	}

	public USGSVariableResult getVariable() {
		return variable;
	}

	public void setVariable(USGSVariableResult variable) {
		this.variable = variable;
	}

	public DateTime getDate() {
		return date;
	}

	public void setDate(DateTime date) {
		this.date = date;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public USGSDataValueQualificationCode getQualification() {
		return qualification;
	}

	public void setQualification(USGSDataValueQualificationCode qualification) {
		this.qualification = qualification;
	}

	public USGSDailyValueStatusCode getStatus() {
		return status;
	}

	public void setStatus(USGSDailyValueStatusCode status) {
		this.status = status;
	}

}
