/* Created On: Nov 8, 2007 */
package edu.unl.act.rma.firm.climate;

import java.awt.Color;
import java.io.ObjectStreamException;

/**
 * @author Ian Cottingham
 *
 */
public enum WeatherStationNetwork {

	COOP("coop", "NWS Coop Network", new Color(106, 155, 230), new Color(201,211,224),  1, true), 
	AWDN("awdn", "Automated Weather Data Network", new Color(150,230,106), new Color(204,223,194), 2, true) { };
	
	private String label;
	private String lookupString;
	private int networkID;
	private boolean acisNetwork;
	private Color activeLabelColor;
	private Color inactiveLabelColor;
	
	private WeatherStationNetwork(String str, String label, Color activeLabelColor, Color inactiveLabelColor, int type, boolean acisNetwork) { 
		this.lookupString = str;
		this.networkID = type;
		this.label = label;
		this.acisNetwork = acisNetwork;
		this.activeLabelColor = activeLabelColor;
		this.inactiveLabelColor = inactiveLabelColor;
	}
	
	public String getLookupString() { 
		return lookupString;
	}
	
	public int getNetworkID() { 
		return networkID;
	}
	
	public String getLabel() { 
		return this.label;
	}
	
	public boolean isAcisNetwork() { 
		return this.acisNetwork;
	}

	public Color getActiveLabelColor() {
		return activeLabelColor;
	}

	public Color getInactiveLabelColor() {
		return inactiveLabelColor;
	}

	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}	
}
