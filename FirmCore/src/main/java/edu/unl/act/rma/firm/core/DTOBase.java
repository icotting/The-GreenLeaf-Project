/* Created On: Sep 2, 2005 */
package edu.unl.act.rma.firm.core;

import java.io.Serializable;
import java.util.HashMap;

/**
 * The base DTO object for the FIRM ssytem.  All FIRM DTOs should extend this 
 * base functionality.  Typesafty is defined for this class to allow for typesafe
 * imports of XML data.  Extensions should implement getter/setter methods which
 * populate a map which is a memebr of this super class.
 * 
 * @author Ian Cottingham
 */
public abstract class DTOBase implements Serializable {		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected HashMap<String, Object> properties = new HashMap<String, Object>();
	
	
	/**
	 * The generic set method used to populate the property map
	 * 
	 * @param propertyName the name of the bean property
	 * @param propertyValue the value of the bean property
	 */
	protected void set(String propertyName, Object propertyValue) { 
		properties.put(propertyName, propertyValue);
	}
	
	/**
	 * A method for reading a bean property
	 * @param propertyName the name of the bean property
	 * @return the value of the bean property for this object
	 */
	protected Object get(String propertyName) {
		return properties.get(propertyName);
	}
}
