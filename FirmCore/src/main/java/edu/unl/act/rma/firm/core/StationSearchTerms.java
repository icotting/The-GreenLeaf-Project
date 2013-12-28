/**
 * 
 */
package edu.unl.act.rma.firm.core;

import java.util.NoSuchElementException;

import edu.unl.act.rma.firm.core.spatial.USState;


/**
 * @author Jon Dokulil
 *
 */
public class StationSearchTerms extends DTOBase {
	/** */
	private static final long serialVersionUID = 2L;
	
	private String stationID;
	private String stationName;
	private String county;
	private String city;
	private USState state;
	private String zipCode;

	public StationSearchTerms() {
		super();
	}

	/**
	 * @return the stationID
	 */
	public String getStationID() {
		return stationID;
	}

	/**
	 * @param stationID the stationID to set
	 */
	public void setStationID(String stationID) {
		this.stationID = stationID;
	}

	/**
	 * @return Returns the county.
	 */
	public String getCounty() {
		return county;
	}

	/**
	 * @param county The county to set.
	 */
	public void setCounty(String county) {
		this.county = county;
	}

	/**
	 * @return Returns the state.
	 */
	public USState getState() {
		return state;
	}

	/**
	 * @param state The state to set.
	 */
	public void setState(USState state) {
		this.state = state;
	}

	public void setStateFromString(String state) { 
		if ( state == null ) {
			return;
		}
		
		if ( state.length() == 2 ) {
			try {
				setState(USState.fromPostalCode(state));
			} catch (NoSuchElementException nsee) {
				return;
			}
		} else {
			state = state.toUpperCase();
			for ( USState s : USState.values() ) {
				if ( s.name().replace('_', ' ').toUpperCase().equals(state) ) {
					setState(s);
				}
			}
		}
	}
	
	/**
	 * @return Returns the city.
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city The city to set.
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return Returns the stationName.
	 */
	public String getStationName() {
		return stationName;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	/**
	 * @param stationName The stationName to set.
	 */
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	
	@Override
	public String toString() {
		return "station="+getStationName()+", city="+getCity()+", county="+getCounty()+", state="+(getState() == null ? "null" : getState().name());
	}
}
