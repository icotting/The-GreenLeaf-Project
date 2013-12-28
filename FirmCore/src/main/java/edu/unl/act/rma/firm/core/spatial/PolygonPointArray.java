/* Created on: Jun 28, 2010 */
package edu.unl.act.rma.firm.core.spatial;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Ian Cottingham
 *
 */
@Entity(name="PolygonPointArray")
@Table(name="PolygonPointArrays")
public class PolygonPointArray implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long arrayId;
	private String arrayString;
	private ArrayType type;
	private Polygon polygon;
		
	@Column(name = "array_id")
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long getArrayId() {
		return arrayId;
	}

	public void setArrayId(long arrayId) {
		this.arrayId = arrayId;
	}

	@Column(name="array_string", columnDefinition="longtext")
	public String getArrayString() {
		return arrayString;
	}

	public void setArrayString(String arrayString) {
		this.arrayString = arrayString;
	}

	@Column(name="type")
	public ArrayType getType() {
		return type;
	}

	public void setType(ArrayType type) {
		this.type = type;
	}
	
	@ManyToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
	@JoinColumn(name="polygon")
	public Polygon getPolygon() {
		return polygon;
	}

	public void setPolygon(Polygon polygon) {
		this.polygon = polygon;
	}
	
	public enum ArrayType { 
		OUTER_POLYGON,
		INNER_POLYGON; 
	};
}
