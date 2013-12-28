/* Created on: Apr 21, 2010 */
package edu.unl.act.rma.firm.drought;

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

import edu.unl.act.rma.firm.core.spatial.SpatialReferenceType;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;

/**
 * @author Ian Cottingham
 *
 */
@Entity(name="DroughtMonitorArea")
@Table(name="DroughtMonitorAreas")
public class DroughtMonitorArea implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long areaId;
	private float unclassified;
	private float d0; 
	private float d1;
	private float d2; 
	private float d3;
	private float d4;
	private SpatialReferenceType type;
	private USCounty county; 
	private USState state;
	private DroughtMonitorDescriptor descriptor;
	
	@Column(name = "area_id")
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long getAreaId() {
		return areaId;
	}

	public void setAreaId(long areaId) {
		this.areaId = areaId;
	}
	
	@Column(name="unclassified")
	public float getUnclassified() {
		return unclassified;
	}

	public void setUnclassified(float unclassified) {
		this.unclassified = unclassified;
	}

	@Column(name="d0_classified")
	public float getD0() {
		return d0;
	}
	
	public void setD0(float d0) {
		this.d0 = d0;
	}

	@Column(name="d1_classified")
	public float getD1() {
		return d1;
	}
	
	public void setD1(float d1) {
		this.d1 = d1;
	}
	
	@Column(name="d2_classified")
	public float getD2() {
		return d2;
	}
	
	public void setD2(float d2) {
		this.d2 = d2;
	}
	
	@Column(name="d3_classified")
	public float getD3() {
		return d3;
	}
	
	public void setD3(float d3) {
		this.d3 = d3;
	}
	
	@Column(name="d4_classified")
	public float getD4() {
		return d4;
	}
	
	public void setD4(float d4) {
		this.d4 = d4;
	}

	@Column(name="type")
	public SpatialReferenceType getType() {
		return type;
	}

	public void setType(SpatialReferenceType type) {
		this.type = type;
	}

	@ManyToOne(cascade={CascadeType.MERGE}, fetch = FetchType.EAGER)
	@JoinColumn(name="county")
	public USCounty getCounty() {
		return county;
	}

	public void setCounty(USCounty county) {
		this.county = county;
	}

	@Column(name="state")
	public USState getState() {
		return state;
	}

	public void setState(USState state) {
		this.state = state;
	}

	@ManyToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
	@JoinColumn(name="descriptor")
	public DroughtMonitorDescriptor getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(DroughtMonitorDescriptor descriptor) {
		this.descriptor = descriptor;
	}
}
