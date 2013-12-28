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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author Ian Cottingham
 *
 */
@Entity(name="Region")
@Table(name="Regions")
public class Region implements Serializable, Comparable<Region> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long regionId = -1;
	private List<Polygon> polygon;
	private USCounty county;
	private USState state;
	private Style style;
	private String description;
	private String name;
	
	public Region() { }
	
	public Region(long regionId, List<Polygon> polygons, Style style) { 
		this.polygon = polygons;
		this.style = style;
		this.regionId = regionId;
	}
	
	public Region(long regionId, List<Polygon> polygons, Style style, String name, String description) { 
		this(regionId, polygons, style);
		this.description = description;
		this.name = name;
	}
	
	@Column(name = "region_id")
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long getRegionId() {
		return regionId;
	}

	public void setRegionId(long regionId) {
		this.regionId = regionId;
	}

	@OneToMany(mappedBy="region", fetch=FetchType.EAGER, cascade={CascadeType.ALL})
	public List<Polygon> getPolygon() {
		return polygon;
	}
	
	public void setPolygon(List<Polygon> polygon) {
		this.polygon = polygon;
	}
	
	@OneToOne(cascade={CascadeType.ALL})
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

	@Transient
	public Style getStyle() {
		return style;
	}
	
	public void setStyle(Style style) {
		this.style = style;
	}
	
	@Transient
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String extractName() { 
		if ( county == null & state == null ) { 
			return ( name == null ) ? "FeatureLaye"+regionId : name;
		}else if ( county == null ) { 
			return state.name();
		} else { 
			return county+" County "+state.name();
		}
	}
	
	@Transient
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int compareTo(Region o) {
		long comp_id = o.getRegionId();
		
		if ( comp_id > regionId ) { 
			return -1;
		} else if ( comp_id < regionId ) { 
			return 1;
		} else { 
			return 0;
		}
	}
}
