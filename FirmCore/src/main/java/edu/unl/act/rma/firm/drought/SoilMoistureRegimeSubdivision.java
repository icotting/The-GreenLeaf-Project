package edu.unl.act.rma.firm.drought;

import java.io.ObjectStreamException;

/**
 * Enumeration of the various soil moisture regime subdivisions used by the Newhall (NSM) component.
 * 
 * @author Jon Dokulil
 */
public enum SoilMoistureRegimeSubdivision {
	UNDEFINED("UNDEFINED", SoilMoistureRegime.UNDEFINED, 0),
	PERUDIC("PERUDIC", SoilMoistureRegime.PERUDIC, 1),
	EXTREME_ARIDIC("EXTREME ARIDIC", SoilMoistureRegime.ARIDIC, 2),
	TYPIC_ARIDIC("TYPIC ARIDIC", SoilMoistureRegime.ARIDIC, 3),
	WEAK_ARIDIC("WEAK ARIDIC", SoilMoistureRegime.ARIDIC, 4),
	DRY_XERIC("DRY XERIC", SoilMoistureRegime.XERIC, 5),
	TYPIC_XERIC("TYPIC XERIC", SoilMoistureRegime.XERIC, 6),
	ARIDIC_TROPUSTIC("ARIDIC TROPUSTIC", SoilMoistureRegime.USTIC, 7),
	TYPIC_TROPUSTIC("TYPIC TROPUSTIC", SoilMoistureRegime.USTIC, 8),
	UDIC_TROPISTIC("UDIC TROPUSTIC", SoilMoistureRegime.USTIC, 9),
	XERIC_TEMPUSTIC("XERIC TEMPUSTIC", SoilMoistureRegime.USTIC, 10),
	WET_TEMPUSTIC("WET TEMPUSTIC", SoilMoistureRegime.USTIC, 11),
	TYPIC_TEMPUSTIC("TYPIC TEMPUSTIC", SoilMoistureRegime.USTIC, 12),
	TYPIC_UDIC("TYPIC UDIC", SoilMoistureRegime.UDIC, 13),
	DRY_TROPUDIC("DRY TROPUDIC", SoilMoistureRegime.UDIC, 14),
	DRY_TEMPUDIC("DRY TEMPUDIC", SoilMoistureRegime.UDIC, 15);

	private SoilMoistureRegimeSubdivision(String name, SoilMoistureRegime regime, int index) {
		this.prettyPrint = name;
		this.index = index;
		this.regime = regime;
	}

	/**
	 * Returns the subdivision corresponding to the given index.  This method is only used by the NSM 
	 * component to interpret the results returned from the NSM native library.
	 * 
	 * @param index The index representing this subdivision
	 * @return The subdivision corresponding to the given index
	 */
	public static SoilMoistureRegimeSubdivision findRegime(int index) {
		for (SoilMoistureRegimeSubdivision regime : values()) {
			if (regime.getIndex() == index)
				return regime;
		}
		return SoilMoistureRegimeSubdivision.UNDEFINED;     
	}

	/**
	 * @return The subdivision's name
	 */
	public String getName() {
		return this.prettyPrint;
	}

	/**
	 * Returns the NSM library's index of this regime.  This attribute is only used by the 
	 * NSM component to interpret the results of the NSM native library.
	 * 
	 * @return The NSM native library's index representing this subdivision
	 */
	public int getIndex() {
		return this.index;
	}
	
	/**
	 * @return The regime associated with this subdivision
	 */
	public SoilMoistureRegime getRegime() {
		return this.regime;
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
	
	private String prettyPrint;
	private int index;
	private SoilMoistureRegime regime;
}
