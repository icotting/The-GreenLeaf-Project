/* Created on: May 26, 2010 */
package edu.unl.act.rma.firm.core.spatial;

import java.io.Serializable;

/**
 * @author Ian Cottingham
 *
 */
public class Style implements Serializable, Comparable<Style> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;
	private String labelScale = "2.000000";
	private String lineWidth = "1";
	private boolean polyOutline = false;
	private boolean polyFill = true;
	
	private String polyColorAlpha = "ff";
	private String polyColorRed = "00";
	private String polyColorGreen ="00";
	private String polyColorBlue = "00";

	private String lineColorAlpha = "ff";
	private String lineColorRed ="00";
	private String lineColorGreen ="00";
	private String lineColorBlue = "00";
	
	private String labelColorAlpha = "ff";
	private String labelColorRed = "00";
	private String labelColorGreen = "00";
	private String labelColorBlue = "00";
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabelScale() {
		return labelScale;
	}

	public void setLabelScale(String labelScale) {
		this.labelScale = labelScale;
	}

	public String getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(String lineWidth) {
		this.lineWidth = lineWidth;
	}

	public boolean getPolyOutline() {
		return polyOutline;
	}

	public void setPolyOutline(boolean polyOutline) {
		this.polyOutline = polyOutline;
	}
	
	public boolean getPolyFill() {
		return polyFill;
	}

	public void setPolyFill(boolean polyFill) {
		this.polyFill = polyFill;
	}

	public String getPolyColorAlpha() {
		return polyColorAlpha;
	}

	public void setPolyColorAlpha(String polyColorAlpha) {
		this.polyColorAlpha = polyColorAlpha;
	}

	public String getPolyColorRed() {
		return polyColorRed;
	}

	public void setPolyColorRed(String polyColorRed) {
		this.polyColorRed = polyColorRed;
	}

	public String getPolyColorGreen() {
		return polyColorGreen;
	}

	public void setPolyColorGreen(String polyColorGreen) {
		this.polyColorGreen = polyColorGreen;
	}

	public String getPolyColorBlue() {
		return polyColorBlue;
	}

	public void setPolyColorBlue(String polyColorBlue) {
		this.polyColorBlue = polyColorBlue;
	}

	public String getLineColorAlpha() {
		return lineColorAlpha;
	}

	public void setLineColorAlpha(String lineColorAlpha) {
		this.lineColorAlpha = lineColorAlpha;
	}

	public String getLineColorRed() {
		return lineColorRed;
	}

	public void setLineColorRed(String lineColorRed) {
		this.lineColorRed = lineColorRed;
	}

	public String getLineColorGreen() {
		return lineColorGreen;
	}

	public void setLineColorGreen(String lineColorGreen) {
		this.lineColorGreen = lineColorGreen;
	}

	public String getLineColorBlue() {
		return lineColorBlue;
	}

	public void setLineColorBlue(String lineColorBlue) {
		this.lineColorBlue = lineColorBlue;
	}

	public String getLabelColorAlpha() {
		return labelColorAlpha;
	}

	public void setLabelColorAlpha(String labelColorAlpha) {
		this.labelColorAlpha = labelColorAlpha;
	}

	public String getLabelColorRed() {
		return labelColorRed;
	}

	public void setLabelColorRed(String labelColorRed) {
		this.labelColorRed = labelColorRed;
	}

	public String getLabelColorGreen() {
		return labelColorGreen;
	}

	public void setLabelColorGreen(String labelColorGreen) {
		this.labelColorGreen = labelColorGreen;
	}

	public String getLabelColorBlue() {
		return labelColorBlue;
	}

	public void setLabelColorBlue(String labelColorBlue) {
		this.labelColorBlue = labelColorBlue;
	}
	
	public String getLabelColor() { 
		return labelColorAlpha+labelColorBlue+labelColorGreen+labelColorRed;
	}
	
	public String getLineColor() { 
		return lineColorAlpha+lineColorBlue+lineColorGreen+lineColorRed;
	}
	
	public String getPolyColor() { 
		return polyColorAlpha+polyColorBlue+polyColorGreen+polyColorRed;
	}

	@Override
	public int compareTo(Style o) {
		return o.getId().compareTo(id);
	}
}
