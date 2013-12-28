/* Created On Apr 4, 2007 */
package edu.unl.act.rma.firm.core.configuration;

import java.io.Serializable;
import java.rmi.Remote;

/**
 * @author Ian Cottingham
 *
 */
public class FrameworkVersion implements Serializable, Remote {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String REGISTRY_BINDING = "FIRM_VERSION";
	
	public static final boolean RELEASE = false;
	
	private static final int major = 2;
	private static final int minor = 0;
	private static final String date = "90904";
	private static final String codeName = "Mozart";

	public String getDate() {
		return date;
	}
	
	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}
	
	@Override
	public boolean equals(Object obj) { 
		if ( !(obj instanceof FrameworkVersion) ) { 
			return false;
		}
		
		FrameworkVersion comp = (FrameworkVersion)obj;
		
		if ( RELEASE ) { 
			return ( comp.getMajor() == major && comp.getMinor() == minor );	
		} else { 
			return ( comp.getDate().equals(date) && 
					comp.getMajor() == major && 
					comp.getMinor() == minor );
		}
	}
	
	@Override 
	public String toString() { 
		StringBuffer sb = new StringBuffer();
		sb.append(major);
		sb.append(".");
		sb.append(minor);
		sb.append(".");
		sb.append(date);
		
		return sb.toString();
	}
	
	public String versionDisplay() {
		StringBuffer sb = new StringBuffer();
		sb.append("FIRM (codename ");
		sb.append(codeName);
		sb.append(") version ");
		sb.append(major);
		sb.append(".");
		sb.append(minor);
		sb.append(".");
		sb.append(date);
		
		return sb.toString();
	}	
	
	public static String buildNumber() { 
		StringBuffer sb = new StringBuffer();
		sb.append(major);
		sb.append(".");
		sb.append(minor);
		sb.append(".");
		sb.append(date);
		
		return sb.toString();
	}
	
	public static String codeName() { 
		return codeName;
	}
}