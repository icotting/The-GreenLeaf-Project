package edu.unl.act.rma.console.usgs;

public enum USGSDataValueQualificationCode {
	APPROVED ("A"), // A - Approved for publication -- Processing and review completed.
	APPROVED_LESS_THAN ("A<"), // See A, < 
	APPROVED_GREATER_THAN ("A>"), // See A, > 
	EDITED ("e"), // e - Value has been edited or estimated by USGS personnel and is write protected
	APPROVED_EDITED("Ae"), // - See A, e
	ESTIMATED ("E"), // E - Value was computed from estimated unit values.
	AFFECTED ("&"), // & - Value was computed from affected unit values
	PROVISIONAL ("P"), // P - Provisional data subject to revision.
	PROVISIONAL_EDITED ("Pe"), // See P, e
	PROVISIONAL_GREATER_THAN ("P>"), // See P, >
	PROVISIONAL_LESS_THAN ("P<"), // See P, <
	LESS_THAN_REPORTED ("<"), // < - The value is known to be less than reported value and is write protected.
	GREATER_THAN_REPORTED (">"), // > - The value is known to be greater than reported value and is write protected.
	VALUE_PROTECTED_UNKNOWN ("1"), // 1 - Value is write protected without any remark code to be printed
	REMARK_PROTECTED_UNKNOWN ("2"); // 2 - Remark is write protected without any remark code to be printed

	private String code;
	
	private USGSDataValueQualificationCode(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	public static USGSDataValueQualificationCode fromCode(String code) {
		for (USGSDataValueQualificationCode qual : values()) {
			if (qual.getCode().equals(code)) {
				return qual;
			}
		}
		throw new RuntimeException(code + " was not recognized as a valid qualification code");
	}
}
