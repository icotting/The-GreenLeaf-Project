/* Created On: Oct 28, 2005 */
package edu.unl.act.rma.firm.core.spatial;

/**
 * @author Ian Cottingham
 *
 */
public class Site {

	final float y;
	final float x;
	final float value;
	
	public Site(float y, float x, float value) { 
		this.y = y;
		this.x = x;
		this.value = value;
	}

	/**
	 * Latitude Point
	 * @return
	 */
	public float getY() {
		return y;
	}

	/**
	 * Longitude Point
	 * @return
	 */
	public float getX() {
		return x;
	}

	public float getValue() {
		return value;
	}
	
	public String toString() { 
		return "y="+y+" x="+x+" value="+value;
	}

}
