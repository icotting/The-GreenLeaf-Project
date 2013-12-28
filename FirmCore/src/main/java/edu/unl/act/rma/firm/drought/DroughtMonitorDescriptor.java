/* Created on: Jun 21, 2010 */
package edu.unl.act.rma.firm.drought;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import edu.unl.act.rma.firm.core.spatial.Polygon;

/**
 * @author Ian Cottingham
 *
 */
@Entity(name="DroughtMonitorDescriptor")
@Table(name="DroughtMonitorDescriptors")
public class DroughtMonitorDescriptor implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long descriptorId;
	private Date mapDate;
	private List<DroughtMonitorArea> areas;
	
	private List<Polygon> d0Polygons;
	private List<Polygon> d1Polygons;
	private List<Polygon> d2Polygons;
	private List<Polygon> d3Polygons;
	private List<Polygon> d4Polygons;
	
	@Column(name = "descriptor_id")
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long getDescriptorId() {
		return descriptorId;
	}
	
	public void setDescriptorId(long descriptorId) {
		this.descriptorId = descriptorId;
	}
	
	
	@Temporal(value=TemporalType.DATE)
	@Column(name="map_date")
	public Date getMapDate() {
		return mapDate;
	}
	
	public void setMapDate(Date mapDate) {
		this.mapDate = mapDate;
	}

	@OneToMany(mappedBy="descriptor", fetch=FetchType.LAZY, cascade={CascadeType.ALL})
	public List<DroughtMonitorArea> getAreaDescriptors() {
		return areas;
	}

	public void setAreaDescriptors(
			List<DroughtMonitorArea> areas) {
		this.areas = areas;
	}

	@OneToMany(mappedBy="d0", fetch=FetchType.LAZY, cascade={CascadeType.ALL})
	public List<Polygon> getD0Polygons() {
		return d0Polygons;
	}

	public void setD0Polygons(List<Polygon> d0Polygons) {
		this.d0Polygons = d0Polygons;
	}

	@OneToMany(mappedBy="d1", fetch=FetchType.LAZY, cascade={CascadeType.ALL})
	public List<Polygon> getD1Polygons() {
		return d1Polygons;
	}

	public void setD1Polygons(List<Polygon> d1Polygons) {
		this.d1Polygons = d1Polygons;
	}

	@OneToMany(mappedBy="d2", fetch=FetchType.LAZY, cascade={CascadeType.ALL})
	public List<Polygon> getD2Polygons() {
		return d2Polygons;
	}

	public void setD2Polygons(List<Polygon> d2Polygons) {
		this.d2Polygons = d2Polygons;
	}

	@OneToMany(mappedBy="d3", fetch=FetchType.LAZY, cascade={CascadeType.ALL})
	public List<Polygon> getD3Polygons() {
		return d3Polygons;
	}

	public void setD3Polygons(List<Polygon> d3Polygons) {
		this.d3Polygons = d3Polygons;
	}

	@OneToMany(mappedBy="d4", fetch=FetchType.LAZY, cascade={CascadeType.ALL})
	public List<Polygon> getD4Polygons() {
		return d4Polygons;
	}

	public void setD4Polygons(List<Polygon> d4Polygons) {
		this.d4Polygons = d4Polygons;
	}
}

