package edu.unl.act.rma.web.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.unl.act.rma.firm.climate.VariableMetaData;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.StationMetaDataType;

/**
 * @author Jon Dokulil
 *
 */
public class Station {
	private String stationId;
	private Map<String, Object> data;
	private Map<DataType, VariableMetaData> variableMetaData;
	
	public Station(String id, Map<StationMetaDataType, Object> data, Map<DataType, VariableMetaData> variableMetaData) {
		super();
		this.stationId = id;
		this.data = new HashMap<String, Object>();
		this.variableMetaData = variableMetaData;
		
		for ( Map.Entry<StationMetaDataType, Object> entry : data.entrySet() ) {
			this.data.put(entry.getKey().name(), entry.getValue());
		}
	}
	
	public Iterable<DataType> variableIterator() { 
		final TreeSet<DataType> sorted = new TreeSet<DataType>(variableMetaData.keySet());
		return new Iterable<DataType>() {

			public Iterator<DataType> iterator() {
				return sorted.iterator();
			} 
			
		};
	}
	
	public String getStationId() {
		return stationId;
	}
	
	public Object getData(StationMetaDataType type) {
		return data.get(type.name());
	}
	
	public Object getData(String name) {
		return data.get(name);
	}
	
	public Map<String, Object> getData() {
		return data;
	}
	
	public Set<DataType> getDataTypes() { 
		return new TreeSet<DataType>(variableMetaData.keySet());
	}
	
	public VariableMetaData getVariableData(DataType type) { 
		return variableMetaData.get(type);
	}
}