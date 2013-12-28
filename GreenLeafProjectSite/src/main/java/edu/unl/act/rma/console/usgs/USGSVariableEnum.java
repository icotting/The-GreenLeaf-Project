package edu.unl.act.rma.console.usgs;



public enum USGSVariableEnum {
	DISCHARGE_MEAN ("00060_00003"),
	GAGE_HEIGHT_MEAN ("00065_00003"),
	GAGE_HEIGHT_MIN ("00065_00002"),
	GAGE_HEIGHT_MAX ("00065_00001");	
	
	private String usgsID;
	
	private USGSVariableEnum(String code) { 
		this.usgsID = code;
	}
	
	public String getUSGScode() {
		return usgsID;
	}
	
	public static USGSVariableEnum fromCode(String code) {
		for (USGSVariableEnum var : values()) {
			if (var.getUSGScode().equals(code)) {
				return var;
			}
		}
		return null;
	}
}
