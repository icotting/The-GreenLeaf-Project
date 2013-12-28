/* Created On Apr 17, 2007 */
package edu.unl.act.rma.firm.core.spatial;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;


/**
 * @author Ian Cottingham
 *
 */
@Entity(name="USCity")
@Table(name="Cities")
public class USCity implements Serializable, Comparable<USCity> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long cityId;
	private int referenceId;
	private String name;
	private USCounty county;
	private String placeFips;
	private List<USZipCode> zipCodes;
	private float latitude;
	private float longitude;
	
	
	@Column(name = "city_id")
	@Id
	public long getCityId() {
		return cityId;
	}

	public void setCityId(long cityId) {
		this.cityId = cityId;
	}

	@ManyToOne(cascade={CascadeType.MERGE}, fetch = FetchType.EAGER)
	@JoinColumn(name="county")
	public USCounty getCounty() {
		return county;
	}

	public void setCounty(USCounty county) {
		this.county = county;
	}

	@Column(name="city_name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name="place_fips")
	public String getPlaceFips() {
		return placeFips;
	}
	
	public void setPlaceFips(String placeFips) {
		this.placeFips = placeFips;
	}

	@Column(name="ref_id")
	public int getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(int referenceId) {
		this.referenceId = referenceId;
	}
	
	@OneToMany(mappedBy="city", fetch=FetchType.EAGER, cascade={CascadeType.ALL})
	public List<USZipCode> getZipCodes() {
		return zipCodes;
	}

	public void setZipCodes(List<USZipCode> zipCodes) {
		this.zipCodes = zipCodes;
	}
	
	@Column(name="latitude")
	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	@Column(name="longitude")
	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return this.name+", "+this.county.getState().name();
	}

	@Override
	public boolean equals(Object obj) { 
		if ( !(obj instanceof USCity) ) { 
			return false;
		}
		
		return ((USCity)obj).getCityId() == this.cityId;
	}
	
	public int compareTo(USCity city) {		
		if ( city.getCityId() == this.cityId ) { 
			return 0;
		} else { 
			return this.name.compareTo(city.getName());
		}
	}
}
