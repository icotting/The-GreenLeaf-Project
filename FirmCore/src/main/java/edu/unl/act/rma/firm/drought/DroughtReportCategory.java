/* Created On Nov 30, 2006 */
package edu.unl.act.rma.firm.drought;

import java.awt.Color;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ian Cottingham
 *
 */
public enum DroughtReportCategory {

	AGRICULTURE("Agriculture", new Color(155,199,28)), 
	WATER_SUPPLY_QUALITY("Water Supply and Quality", new Color(28,100,199)),
	ENERGY("Energy", new Color(161,28,199)), 
	TOURISM_REC("Tourism and Recreation", new Color(80,79,79)),
	BIZ_INDUSTRY("Other Business and Industry", new Color(157,157,157)),
	PLANT_WILDLIFE("Plants and Wildlife", new Color(86,155,40)),
	WILDFIRE("Wildfire", new Color(199,141,28)),
	DISASTER_DECLARATION("Disaster Declaration and Aid", new Color(199,60,28)),
	SOCIETY_HEALTH("Society and Public Health", new Color(28,149,199)), 
	RELIEF_RESTRICTION("Relief, Response, and Restrictions", new Color(40,59,131)),
	GENERAL("General Awareness", new Color(99,86,136)),
	OTHER("Other or Unclassified", new Color(175,190,133));
	
	private String prettyPrint;
	private Color color;
	
	private DroughtReportCategory(String prettyPrint, Color color) { 
		this.prettyPrint = prettyPrint;
		this.color = color;
	}
	
	public static DroughtReportCategory fromString(String category) { 
		if (category.equals("Agriculture")) {
			return AGRICULTURE;
		} else if (category.equals("Fire")) {
			return WILDFIRE;
		} else if (category.equals("Plants & Wildlife")) {
			return PLANT_WILDLIFE;
		} else if (category.equals("Water Supply & Quality")) {
			return WATER_SUPPLY_QUALITY;
		} else if (category.equals("Society & Public Health")) {
			return SOCIETY_HEALTH;
		} else if (category.equals("Business & Industry")) {
			return BIZ_INDUSTRY;
		} else if (category.equals("Energy")) {
			return ENERGY;
		} else if (category.equals("Tourism & Recreation")) {
			return TOURISM_REC;
		} else if (category.equals("Disaster Declaration & Aid")) {
			return DISASTER_DECLARATION;
		} else if (category
				.equals("Relief, Response & Restrictions")) {
			return RELIEF_RESTRICTION;
		} else if (category.equals("General Awareness")) {
			return GENERAL;
		} else {
			return OTHER;
		}
	}
	
	public String getPrintName() { 
		return prettyPrint;
	}
	
	public Color getColor() { 
		return this.color;
	}
	
	public String getColorHex() { 
		StringBuffer sb = new StringBuffer(Integer.toHexString(color.getRed()));
		sb.append(Integer.toHexString(color.getGreen()));
		sb.append(Integer.toHexString(color.getBlue()));
	
		return sb.toString();
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
	
	public static List<DroughtReportCategory> list() { 
		ArrayList<DroughtReportCategory> list = new ArrayList<DroughtReportCategory>();
		
		for ( DroughtReportCategory cat : DroughtReportCategory.values() ) { 
			list.add(cat);
		}
		
		return list;
	}
}
