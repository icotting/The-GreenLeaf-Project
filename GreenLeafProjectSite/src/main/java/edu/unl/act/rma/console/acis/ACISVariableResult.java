/* Created On: May 31, 2005 */
package edu.unl.act.rma.console.acis;

import java.rmi.server.UID;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Ian Cottingham
 *
 */
public class ACISVariableResult {

	private Map<ACISVariableEnumeration, short[]> startDates;
	private Map<ACISVariableEnumeration, short[]> endDates;
	private Map<ACISVariableEnumeration, String> variableIds;
	private Map<ACISVariableEnumeration, float[]> variableData;
	
	public ACISVariableResult() { 
		startDates = new HashMap<ACISVariableEnumeration, short[]>();
		endDates = new HashMap<ACISVariableEnumeration, short[]>();
		variableData = new HashMap<ACISVariableEnumeration, float[]>();
		variableIds = new HashMap<ACISVariableEnumeration, String>();
	}
	
	public void addVariable(ACISVariableEnumeration variable, short[] startDate, short[] endDate) { 
		this.startDates.put(variable, startDate);
		this.endDates.put(variable, endDate);
		this.variableIds.put(variable, new UID().toString());
	}
	
	public void updateVariableID(ACISVariableEnumeration variable, String id) {
		this.variableIds.put(variable, id);
	}
	
	public void removeVariable(ACISVariableEnumeration variable) { 
		startDates.remove(variable);
		endDates.remove(variable);
		variableIds.remove(variable);
		variableData.remove(variable);
	}
	
	public void addVariableData(ACISVariableEnumeration variable, float[] data) { 
		this.variableData.put(variable, data);
	}
	
	public String getVariableId(ACISVariableEnumeration variable) { 
		return variableIds.get(variable);
	}
		
	public short[] getStartDate(ACISVariableEnumeration variable) { 
		return startDates.get(variable);
	}
	
	public short[] getEndDate(ACISVariableEnumeration variable) { 
		return endDates.get(variable);
	}
	
	public float[] getData(ACISVariableEnumeration variable) { 
		return variableData.get(variable);
	}
	
	public Set<ACISVariableEnumeration> variables() {
		return startDates.keySet();
	}
	
	public boolean contains(ACISVariableEnumeration var) { 
		return startDates.keySet().contains(var);
	}
	
	public int size() { 
		return variableIds.size();
	}
}
