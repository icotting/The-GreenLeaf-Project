package edu.unl.act.rma.firm.core;

import java.io.ObjectStreamException;

/**
 * A format independant enumeration of all possible data types archived in a FIRM dataset.
 * @author Ian Cottingham
 */
public enum DataType {
	SPI("Standardized Precipitation Index", 2, true, "", "", true), 
	scPDSI("Self Calibrating Palmer Drought Severity Index", 3, true, "", "", true),  
	NSM("Newhall Simulation Model", true, "", "", false), 
	KBDI("Keetch-Byram Drought Index", true, "", "", false), 
	PRECIP("Precipitation", false, "inches", "millimeters", false), 
	LOW_TEMP("Low Temperature", false, "degrees F", "degrees C", false),
	HIGH_TEMP("High Temperature", false, "degrees F", "degrees C", false),
	NORMAL_TEMP("Average Temperature", false, "degrees F", "degrees C", false), 
	WSAVG_3M("Average Wind Speed", false, "", "", false), 
	SOILTAVG_10("Average Soil Temperature", false, "", "", false), 
	RHAVG("Average Relative Humidity", false, "", "", false), 
	SOLARRAD_LANGLEY("Solar Radiation", false, "", "",false), 
	AWC("Available Water Holding Capacity", true, "inches", "millimeters", false), 
	META_DATA("Weather Station Mata Data", true, "", "", false), 
	GDD("Growing Degree Days", true, "", "", false), 
	FFP("Frost Free Period", true, "", "", false), 
	// Added Oceanic variables FARM-449
	SOI_ANOMALY("Southern Oscillation Index-Anomaly", false, "", "", false), 	
	SOI_STANDARD("Southern Oscillation Index-Standard", false, "", "", false), 
	MEI("Multivariate ENSO Index ", 3, false, "", "", false), 
	NAM_PC1("Northern Annular Mode", 4, false, "", "", false), 
	PNA_PC2("Pacific / North American (PNA) index", 4, false, "", "", false), 
	PC3("PC3", 4, false, "", "", false), 
	NAO("North Atlantic Oscillation", false, "", "", false), 
	ONI("Oceanic Ni–o Index", false, "", "", false), 
	NPI("North Pacific Index", false, "", "", false), 
	PDO("The Pacific Decadal Oscillation", 2, false, "", "", false), 
	JMASST("JMA Sea Surface Anomally", false, "", "", false), 
	RMM1("Real-time Multivariate MJO series 1",4, false, "", "", false), 
	RMM2("Real-time Multivariate MJO series 2",4,false, "", "", false), 
	AMO("Atlantic Multidecadal Oscillation",4, false, "", "", false), 
	DROUGHT_MONITOR("Drought Monitor", 0, true, "", "", true),
	UNKNOWN("Unknown Data Type", true, "", "", false),
	// Stream Flow data
	DISCHARGE_MEAN("Stream Flow",4,false,"cubic feet per sec","",false),
	GAGE_HEIGHT_MEAN("Stream Gage Height Mean", 4, false, "feet","", false),
	GAGE_HEIGHT_MIN("Stream Gage Height Minimum", 4, false, "feet","", false),
	GAGE_HEIGHT_MAX("Stream Gage Height Maximum", 4, false, "feet","", false),
	NWS_RAINFALL("AHPS Precipitation Analysis", 0, true, "", "", true){};
	
	public static final float MISSING = -99;
	public static final float NONEXISTANT = -100;
	
	
	/**
	 * Indicates that the value for this element could not be calculated due 
	 * to an error
	 */
	public static final float ERROR_RESULT = -999.0f;
	
	/**
	 * Indicates that the value for this field was not requested and
	 * ommitted.  This will typically appear when data within a year 
	 * is requested, this value will appear in the year, as all 
	 * returns run at a min from Jan 1 - Dec. 31
	 */
	public static final float OUTSIDE_OF_RANGE = -9999.0f;
	
	public static final float OUTSIDE_OF_REQUEST_RANGE = -99999.0f;
	
	public static final float DEFAULT_AWC = 7.61f;

	public static final float DISCARD_THRESHHOLD = 0.2f;
	
	/*
	 * 52-(.2-52)	
	 * added as threshold value jira FARM-445
	 */
	public static final int WEEKS_IN_YEAR_THRESHOLD=42;		
	
	private boolean derived;
	private boolean mappable;
	
	private String englishUnitLabel;
	private String metricUnitLabel;
	
	private DataType(String name, boolean derived, String englishUnitLabel, String metricUnitLabel, boolean mappable) { 
		this.name = name;
		this.roundingPlaces = 2;
		this.derived = derived;
		this.englishUnitLabel = englishUnitLabel;
		this.metricUnitLabel = metricUnitLabel;
		this.mappable = mappable;
	}
	
	private DataType(String name, int roundingPlaces, boolean derived, String englishUnitLabel, String metricUnitLabel, boolean mappable) { 
		this(name, derived, englishUnitLabel, metricUnitLabel, mappable);
		this.roundingPlaces = roundingPlaces;
	}
	
	private String name;
	private int roundingPlaces;
	
	/**
	 * 
	 * @param name the human readable name of the data type
	 */
	public String getName() {
		return name;
	}

	public int getRoundingPlaces() {
		return roundingPlaces;
	}	
	
	public boolean isDerived() { 
		return derived;
	}
	
	public boolean isMappable() { 
		return this.mappable;
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
	
	public String unitLabel(UnitType type) {
		if ( type == null ) { 
			return "";
		}
		
		switch ( type ) { 
		case ENGLISH:
			return this.englishUnitLabel;
		case METRIC: 
			return this.metricUnitLabel;
		default:
			return "";
		}
	}
}