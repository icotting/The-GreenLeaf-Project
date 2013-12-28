/* Created On: Apr 27, 2006 */
package edu.unl.act.rma.firm.climate;

import java.io.Serializable;

import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.TemporalPeriod;


/**
 * @author Ian Cottingham 
 *
 */
public class VariableFilter implements Serializable {

	private static final long serialVersionUID = 3L;
	
	private DataType variableType;
	private TemporalPeriod validPeriod = new TemporalPeriod();
	
	float missingTolerance;
	
	public VariableFilter() { }
	
	public VariableFilter(float missingTolerance) { 
		this.missingTolerance = missingTolerance;
	}
	
	public float getMissingTolerance() {
		return missingTolerance;
	}
	
	public void setMissingTolerance(float missingTolerance) {
		this.missingTolerance = missingTolerance;
	}
		
	public DataType getVariableType() {
		return variableType;
	}
	
	public void setVariableType(DataType variableType) {
		this.variableType = variableType;
	}

	public TemporalPeriod getValidPeriod() {
		return validPeriod;
	}

	public void setValidPeriod(TemporalPeriod validPeriod) {
		this.validPeriod = validPeriod;
	}
	
	@Override
	public String toString() {
		return "type=" + variableType + ",period=" + validPeriod + ",tolerance=" + missingTolerance;
	}
}
