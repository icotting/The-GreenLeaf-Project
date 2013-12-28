package edu.unl.act.rma.firm.drought;

import java.io.ObjectStreamException;

/**
 * Enumeration of the various soil temperature regimes used by the Newhall (NSM) component.
 * 
 * @author Jon Dokulil
 */
public enum SoilTemperatureRegime {
	UNDEFINED("Undefined", -1),
	PERGELIC("Pergelic", 0),
	CRYIC("Cryic", 1),
	ISOFRIGID("Isofrigid", 2),
	FRIGID("Frigid", 3),
	ISOMESIC("Isomesic", 4),
	MESIC("Mesic", 5),
	ISOTHERMIC("Isothermic", 6),
	THERMIC("Thermic", 7),
	ISOHYPERTHERMIC("Isohyperthermic", 8),
	HYPERTHERMIC("Hyperthermic", 9);
	
	private SoilTemperatureRegime(String name, int index) {
		this.prettyPrint = name;
		this.index = index;
	}
	
	public String getName() {
		return this.prettyPrint;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public static SoilTemperatureRegime findRegime(int index) {
		for (SoilTemperatureRegime regime : values()) {
			if (regime.getIndex() == index)
				return regime;
		}
		return SoilTemperatureRegime.UNDEFINED;
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
	
	private String prettyPrint;
	private int index;
}
