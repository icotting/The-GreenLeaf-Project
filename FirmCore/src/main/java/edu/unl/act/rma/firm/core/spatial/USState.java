/* Created On: May 24, 2005 */
package edu.unl.act.rma.firm.core.spatial;

import static edu.unl.act.rma.firm.core.spatial.USRegion.MIDWEST;
import static edu.unl.act.rma.firm.core.spatial.USRegion.MOUNTIAN;
import static edu.unl.act.rma.firm.core.spatial.USRegion.NORTHEAST;
import static edu.unl.act.rma.firm.core.spatial.USRegion.NORTHWEST;
import static edu.unl.act.rma.firm.core.spatial.USRegion.PLAINS;
import static edu.unl.act.rma.firm.core.spatial.USRegion.SOUTH;
import static edu.unl.act.rma.firm.core.spatial.USRegion.SOUTHEAST;
import static edu.unl.act.rma.firm.core.spatial.USRegion.SOUTHWEST;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;


/**
 * An enumeration of all US states
 * 
 * @author Ian Cottingham
 */
public enum USState {
	
	Alabama("AL", "01", SOUTH, 32.614471, -86.680740),
	Alaska("AK", "02", NORTHWEST, 62.890301, -149.054077),
	Arizona("AZ", "04", SOUTHWEST, 34.168091, -111.930344),
	Arkansas("AR", "05", SOUTH, 34.751888, -92.131348),
	California("CA", "06", SOUTHWEST, 37.271832, -119.270203),
	Colorado("CO", "08", MOUNTIAN, 38.997841, -105.550911),
	Connecticut("CT", "09", NORTHEAST, 41.515572, -72.757477),
	Delaware("DE", "10", NORTHEAST, 39.145199, -75.418610),
	Florida("FL", "12", SOUTHEAST, 27.975639, -81.541183),
	Georgia("GA", "13", SOUTH, 32.678131, -83.222931),
	Hawaii("HI", "15", NORTHWEST, 19.589640, -155.434036),
	Idaho("ID", "16", MOUNTIAN, 45.494419, -114.143219),
	Illinois("IL", "17", MIDWEST, 39.739281, -89.504128),
	Indiana("IN", "18", MIDWEST, 39.766201, -86.441254),
	Iowa("IA", "19", PLAINS, 41.938221, -93.389900),
	Kansas("KS", "20", PLAINS, 38.498058, -98.320213),
	Kentucky("KY", "21", SOUTH, 37.822399, -85.691101),
	Louisiana("LA", "22", SOUTH, 30.972260, -91.521797),
	Maine("ME", "23", NORTHEAST, 45.262379, -69.008301),
	Maryland("MD", "24", SOUTHEAST, 38.823399, -75.923759),
	Massachusetts("MA", "25", NORTHEAST, 42.163891, -71.717941),
	Michigan("MI", "26", MIDWEST, 43.742691, -84.621620),
	Minnesota("MN", "27", MIDWEST, 46.441929, -93.365471),
	Mississippi("MS", "28", SOUTH, 32.585159, -89.876381),
	Missouri("MO", "29", SOUTH, 38.304611, -92.436653),
	Montana("MT", "30", MOUNTIAN, 46.679440, -110.044472),
	Nebraska("NE", "31", PLAINS, 41.500839, -99.680771),
	Nevada("NV", "32", SOUTHWEST, 38.502460, -117.022720),
	New_Hampshire("NH", "33", NORTHEAST, 44.001301, -71.632828),
	New_Jersey("NJ", "34", NORTHEAST, 40.142780, -74.726723),
	New_Mexico("NM", "35", SOUTHWEST, 34.166161, -106.026123),
	New_York("NY", "36", NORTHEAST, 40.71463, -74.005806),
	North_Carolina("NC", "37", SOUTHEAST, 35.219410, -80.018333),
	North_Dakota("ND", "38", PLAINS, 47.467731, -100.301712),
	Ohio("OH", "39", MIDWEST, 40.190269, -82.669403),
	Oklahoma("OK", "40", PLAINS, 35.308960, -98.716942),
	Oregon("OR", "41", NORTHWEST, 44.114552, -120.514908),
	Pennsylvania("PA", "42", NORTHEAST, 40.994640, -77.604507),
	Rhode_Island("RI", "44", NORTHEAST, 41.661171, -71.555771),
	South_Carolina("SC", "45", SOUTHEAST, 33.624981, -80.947441),
	South_Dakota("SD", "46", PLAINS, 44.212391, -100.247101),
	Tennessee("TN", "47", SOUTH, 35.830620, -85.978554),
	Texas("TX", "48", SOUTH, 31.168989, -100.076790),
	Utah("UT", "49", SOUTHWEST, 39.499611, -111.547050),
	Vermont("VT", "50", NORTHEAST, 43.871769, -72.451218),
	Virginia("VA", "51", SOUTHEAST, 38.003349, -79.771446),
	Washington("WA", "53", NORTHWEST, 47.124401, -120.676003),
	West_Virginia("WV", "54", SOUTHEAST, 38.920101, -80.181808),
	Wisconsin("WI", "55", MIDWEST, 44.727242, -90.101563),
	Wyoming("WY", "56", PLAINS, 43.000320, -107.554626), 
	Washington_DC("DC", "11", NORTHEAST, 38.89047, -77031959),
	Puerto_Rico("PR", "43", SOUTHEAST, 18.2453, -65.6434); 
	
	private String postalCode;
	private String fips;
	private USRegion region;
	private double latitude; 
	private double longitude;
	
	private USState(String postal, String fips, USRegion region, double latitude, double longitude) { 
		this.postalCode = postal;
		this.fips = fips;
		this.region = region;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	/**
	 * 
	 * @return The two digit postal code for the state
	 */
	public String getPostalCode() {
		return postalCode;
	}
	
	/**
	 * 
	 * @return the region in which the state belongs
	 */
	public USRegion getRegion() { 
		return region;
	}
	
	public String getFipsCode() { 
		return this.fips;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	/** 
	 * Generates a subset of all states based on a regular expression 
	 * 
	 * @param regex the regular expression for evaluating membership in the sublist
	 * @return a sublist of states
	 */
	public static Collection<USState> getStatesByRegex(Pattern regex) { 
		ArrayList<USState> state_list = new ArrayList<USState>();
		for ( USState state : USState.values() ) {
			if ( regex.matcher(state.name()).find() )
				state_list.add(state);
		}
		
		return state_list;
	}
	
	/**
	 * Generates a list of all stats in a region
	 * 
	 * @param region the region
	 * @return a sublist of states
	 */
	public static Collection<USState> getStatesByRegion(USRegion region) { 
		ArrayList<USState> region_states = new ArrayList<USState>();
		for ( USState state : USState.values() ) {
			if ( state.getRegion() == region )
				region_states.add(state);
		}
		
		return region_states;
	}
	
	public static USState fromPostalCode(String code) throws NoSuchElementException { 
		for ( USState state : USState.values() ) {
			if ( state.postalCode.equals(code.toUpperCase())) {
				return state;
			}
		}
		
		throw new NoSuchElementException("Could not match postal code "+code+" to any state");
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
}
