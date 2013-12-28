package edu.unl.act.rma.console.usgs;

public enum USGSDailyValueStatusCode {
	SEASONAL ("Ssn"), // Ssn - Parameter monitored seasonally
	ICE ("Ice"), // Ice - Ice affected
	PARTIAL ("Pr"), // Pr - Partial-record site
	RATING_UNRESOLVED ("Rat"), // Rat - Rating being developed or revised
	UNDETERMINED ("Nd"), // Nd - Not determined
	EQUIPMENT_FAILED ("Eqp"), // Eqp - Equipment malfunction
	FLOOD ("Fld"), // Fld - Flood damage
	DRY ("Dry"), // Dry - Zero flow
	DISCONTINUED ("Dis"), // Dis - Data-collection discontinued
	PARAM_UNDETERMINED ("--"), // --  - Parameter not determined
	TEMP_UNAVAILABLE ("***"); // *** - Temporarily unavailable 
	
	private String code;
	
	private USGSDailyValueStatusCode(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	public static USGSDailyValueStatusCode fromCode(String code) {
		for (USGSDailyValueStatusCode status : values()) {
			if (status.getCode().equals(code)) {
				return status;
			}
		}
		throw new RuntimeException("could not find status for code " + code);
	}

}
