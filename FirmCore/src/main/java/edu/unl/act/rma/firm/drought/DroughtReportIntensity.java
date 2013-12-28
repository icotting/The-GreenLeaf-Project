/* Created On: Nov 6, 2007 */
package edu.unl.act.rma.firm.drought;

import java.awt.Color;
import java.io.ObjectStreamException;

/**
 * @author Ian Cottingham
 *
 */
public enum DroughtReportIntensity {

	NORMAL("Normal Reporting Activity", new Color(240,238,219), new Color(153,153,153)),
	ABNORMAL("Moderate Reporting Activity", new Color(251,255,5), new Color(128,128,128)), 
	MODERATE("Increased Reporting Activity", new Color(255,200,53), new Color(89,89,89)),
	SEVERE("Significant Reporting Activity", new Color(248,139,0), new Color(51,51,51)),
	EXTREME("Extreme Reporting Activity", new Color(236,0,0), new Color(26,26,26)),
	EXCEPTIONAL("Exceptional Report Activity", new Color(173,0,0), new Color(0,0,0));
	
	private String label; 
	private Color color;
	private Color greyScale; 
	
	private DroughtReportIntensity(String label, Color color, Color greyScale) { 
		this.label = label;
		this.color = color;
		this.greyScale = greyScale;
	}
	
	public Color getColor() { 
		return this.color;
	}
	
	public Color getGreyColor() { 
		return this.greyScale;
	}
	
	public String getLabel() { 
		return this.label;
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}

	private String getHex(Color color) { 
		StringBuffer sb = new StringBuffer("#");
		
		if ( color.getRed() < 10 ) { 
			sb.append("0");
		}
		sb.append(Integer.toHexString(color.getRed()));
		
		if ( color.getGreen() < 10 ) { 
			sb.append("0");
		}
		sb.append(Integer.toHexString(color.getGreen()));
		
		if ( color.getBlue() < 10 ) { 
			sb.append("0");
		}
		sb.append(Integer.toHexString(color.getBlue()));
	
		return sb.toString();
	}
	
	public String getHexGreyScale() { 
		return getHex(greyScale);
	}
	
	public String getHexColor() { 
		return getHex(color);
	}
}
