/* Created On Nov 30, 2006 */
package edu.unl.act.rma.firm.core.spatial;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;



/**
 * @author Ian Cottingham
 *
 */
@Entity(name="USCounty")
@Table(name="Counties")
public class USCounty implements Serializable, Comparable<USCounty> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long countyId;
	private USState state;
	private String fips;
	private String name;
	private String countyFips;
	private double latitude;
	private double longitude;
	private List<USCity> cities;
	
	@Column(name = "county_id")
	@Id
	public long getCountyId() {
		return countyId;
	}
	
	public void setCountyId(long countyId) {
		this.countyId = countyId;
	}
	
	@Column(name="fips_code")
	public String getFips() {
		return fips;
	}
	
	public void setFips(String fips) {
		this.fips = fips;
	}
	
	@Column(name="county_name")
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Column(name="county_fips")
	public String getCountyFips() {
		return countyFips;
	}

	public void setCountyFips(String countyFips) {
		this.countyFips = countyFips;
	}

	@Column(name="state")
	public USState getState() {
		return state;
	}

	public void setState(USState state) {
		this.state = state;
	}
	
	@Column(name="latitude")
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	@Column(name="longitude")
	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	@OneToMany(mappedBy="county", fetch=FetchType.LAZY, cascade={CascadeType.ALL})
	public List<USCity> getCities() {
		return cities;
	}

	public void setCities(List<USCity> cities) {
		this.cities = cities;
	}

	@Override
	public boolean equals(Object obj) { 
		if ( obj == this ) { 
			return true;
		} else if ( !(obj instanceof USCounty) ) { 
			return false;
		}
		
		USCounty comp = (USCounty)obj;
		
		return (comp.getCountyId() == this.getCountyId());
	}

	public int compareTo(USCounty county) {
		int my_state = this.state.ordinal();
		int other_state = county.state.ordinal();
		int my_county = Integer.parseInt(this.countyFips);
		int other_county = Integer.parseInt(county.getCountyFips());
		
		if ( my_state == other_state ) { 
			if ( my_county == other_county ) { 
				return 0;
			} else if ( my_county > other_county ) { 
				return 1;
			} else { 
				return -1;
			}
		} else if ( my_state > other_state ) { 
			return 1;
		} else { 
			return -1;
		}
	}
	
	@Override
	public int hashCode() {
		return Integer.parseInt(this.fips);
	}

	@Override
	public String toString() { 
		return this.name+", "+this.state.name();
	}
}
