/* Created On: Sep 11, 2005 */
package edu.unl.act.rma.firm.climate;

import java.io.ObjectStreamException;

import org.joda.time.DateTime;

/**
 * @author Ian Cottingham
 *
 */
public enum OceanicMetaDataType {

	VARIABLE_ID("oceanic_variable.variable_ID",String.class),
	NAME("oceanic_variable.name",String.class),
	START_DATE("oceanic_variable.start_date",DateTime.class),
	END_DATE("oceanic_variable.end_date",DateTime.class),
	TYPE("type.type",String.class)
	;
	
	private String fieldName;
	private Class type;
	
	private OceanicMetaDataType(String str, Class type) { 
		this.fieldName = str;
		this.type = type;
	}
	
	public String getFieldName() { 
		return this.fieldName;
	}
	
	public Class getType() { 
		return type;
	}
	
	public Object readResolve() throws ObjectStreamException {
		return valueOf(name());
	}
}
