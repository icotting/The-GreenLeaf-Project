/* Created on: May 26, 2010 */
package edu.unl.act.rma.firm.core.spatial;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import edu.unl.act.rma.firm.drought.DroughtMonitorDescriptor;

/**
 * @author Ian Cottingham
 *
 */
@Entity(name="Polygon")
@Table(name="Polygons")
public class Polygon implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long polygonId;
	private Region region;
	private List<PolygonPointArray> points;

	private DroughtMonitorDescriptor d0;
	private DroughtMonitorDescriptor d1;
	private DroughtMonitorDescriptor d2;
	private DroughtMonitorDescriptor d3;
	private DroughtMonitorDescriptor d4;
	
	@Column(name = "polygon_id")
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long getPolygonId() {
		return polygonId;
	}

	public void setPolygonId(long polygonId) {
		this.polygonId = polygonId;
	}

	@ManyToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
	@JoinColumn(name="region")
	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	@ManyToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
	@JoinColumn(name="d0")
	public DroughtMonitorDescriptor getD0() {
		return d0;
	}

	public void setD0(DroughtMonitorDescriptor d0) {
		this.d0 = d0;
	}

	@ManyToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
	@JoinColumn(name="d1")
	public DroughtMonitorDescriptor getD1() {
		return d1;
	}

	public void setD1(DroughtMonitorDescriptor d1) {
		this.d1 = d1;
	}

	@ManyToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
	@JoinColumn(name="d2")
	public DroughtMonitorDescriptor getD2() {
		return d2;
	}

	public void setD2(DroughtMonitorDescriptor d2) {
		this.d2 = d2;
	}

	@ManyToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
	@JoinColumn(name="d3")
	public DroughtMonitorDescriptor getD3() {
		return d3;
	}

	public void setD3(DroughtMonitorDescriptor d3) {
		this.d3 = d3;
	}

	@ManyToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
	@JoinColumn(name="d4")
	public DroughtMonitorDescriptor getD4() {
		return d4;
	}

	public void setD4(DroughtMonitorDescriptor d4) {
		this.d4 = d4;
	}

	@OneToMany(mappedBy="polygon", fetch=FetchType.EAGER, cascade={CascadeType.ALL})
	public List<PolygonPointArray> getPoints() {
		return points;
	}

	public void setPoints(List<PolygonPointArray> points) {
		this.points = points;
	}
	
	
}
