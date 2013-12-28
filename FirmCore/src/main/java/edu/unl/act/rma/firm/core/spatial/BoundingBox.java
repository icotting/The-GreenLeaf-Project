/* 
 * MapBoundingBox
 * 
 * Created On: Oct 13, 2006 
 * 
 * Change Log:
 * 
 */
package edu.unl.act.rma.firm.core.spatial;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;


/**
 * MapBoundingBox used to set up and retrieve the region interested.
 * 
 * @author Xueming wu
 *
 */
@Entity(name="BoundingBox")
@Table(name="SpatialRegions")
@TableGenerator(name="BOUNDING_BOX_GEN", table="GENERATOR_TABLE")
public class BoundingBox implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long boxId;
	private float west;
	private float north;
	private float east;
	private float south;	
	private SpatialReference reference;
	
	public BoundingBox() { }
	
	public BoundingBox(float north, float west, float south, float east) { 
		this.west = west;
		this.north = north;
		this.east = east;
		this.south = south;
	}	
		
	@Column(name = "box_id")
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="BOUNDING_BOX_GEN")
	public long getBoxId() {
		return boxId;
	}

	public void setBoxId(long boxId) {
		this.boxId = boxId;
	}

	@Column(name="east")
	public float getEast() {
		return east;
	}

	@Column(name="south")
	public float getSouth() {
		return south;
	}
	
	@Column(name="west")
	public float getWest() {
		return west;
	}

	@Column(name="north")
	public float getNorth() {
		return north;
	}
	
	@OneToOne(mappedBy="boundary")
	public SpatialReference getReference() {
		return reference;
	}

	public void setReference(SpatialReference reference) {
		this.reference = reference;
	}

	public void setEast(float east) {
		this.east = east;
	}

	public void setNorth(float north) {
		this.north = north;
	}

	public void setSouth(float south) {
		this.south = south;
	}

	public void setWest(float west) {
		this.west = west;
	}

	public float getCenterLat() { 
		return 0f; //TODO: compute center lat
	}
	
	public float getCenterLon() { 
		return 0f; //TODO: compute center lon
	}
	
	@Override
	public String toString() {
		return west + " " + south + " " + east + " " + north;
	}
	
	public String toSqlString() { 
		StringBuffer poly_buffer = new StringBuffer("Polygon((");
		poly_buffer.append(this.getEast());
		poly_buffer.append(" ");
		poly_buffer.append(this.getNorth());
		poly_buffer.append(", ");
		poly_buffer.append(this.getEast());
		poly_buffer.append(" ");
		poly_buffer.append(this.getSouth());
		poly_buffer.append(", ");
		poly_buffer.append(this.getWest());
		poly_buffer.append(" ");
		poly_buffer.append(this.getSouth());
		poly_buffer.append(", ");
		poly_buffer.append(this.getWest());
		poly_buffer.append(" ");
		poly_buffer.append(this.getNorth());		
		poly_buffer.append(", ");
		poly_buffer.append(this.getEast());
		poly_buffer.append(" ");
		poly_buffer.append(this.getNorth());	
		poly_buffer.append("))");
		
		return poly_buffer.toString();
	}
	
	//TODO: Xueming, please check these calculations
	public boolean contains(BoundingBox box) {
		// Validate the parameter
		if ( box == null ) return false;
		BoundingBox targetBox = box;
		
		/* if the box north,east is less than or equal to this north, east and
		 * the box north,east is greater than this south,west then the upper  
		 * right point is in this Box.
		 * if the targetBox south,west is greater than or equal to this south,west
		 * and the targetBox south,west is less than or requal to this north,east
		 * then the lower left point is in this Box. When both upper right point
		 * and lower left point are in this Box, this Box contain the box.
		 */			
		if ( (this.north >= targetBox.north && this.east >= targetBox.east) 
				&& (targetBox.north >= this.south && targetBox.east >= this.west) 
				&& (this.south <= targetBox.south && this.west <= targetBox.west) 
				&& (targetBox.south <= this.north && targetBox.west <= this.east) ) {
			return true;
		}
		
		return false;
	}
	
	public boolean validate() {
		if ( this.north < this.south || this.east < this.west 
				|| (this.north == 0 && this.south  == 0 && this.east == 0 && this.west == 0) ) 
			return false;
		
		return true;
	}
}

