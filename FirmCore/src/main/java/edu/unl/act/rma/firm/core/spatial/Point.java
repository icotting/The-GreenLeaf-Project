/* Created on: Jun 10, 2010 */
package edu.unl.act.rma.firm.core.spatial;

import java.io.Serializable;

/**
 * @author Ian Cottingham
 *
 */
public class Point implements Serializable, Comparable<Point> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String description;
	private float latitude; 
	private float longitude;
	private String placeName;
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public float getLatitude() {
		return latitude;
	}
	
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	
	public float getLongitude() {
		return longitude;
	}
	
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	public String getPlaceName() {
		return placeName;
	}

	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}

	@Override
	public int compareTo(Point o) {
		if ((o.getLatitude() == latitude) && (o.getLongitude() == longitude)) { 
			return 1;
		} else { 
			return 0;
		}
	} 
	
	
}
