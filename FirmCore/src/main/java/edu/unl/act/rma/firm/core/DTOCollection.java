package edu.unl.act.rma.firm.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates a mapping of station ID to {@link edu.unl.firm.shared.objectmodel.DTOBase} instances.
 * 
 * @author Jon Dokulil
 *
 * @param <T> The subclass of {@link edu.unl.firm.shared.objectmodel.DTOBase} to store
 */
public class DTOCollection<T extends DTOBase> implements Serializable {
	
	private static final long serialVersionUID = 2L;

	protected Map<String, T> map;
	
	public DTOCollection() {
		this.map = new HashMap<String, T>();
	}
	
	public void add(String stationID, T dto) {
		this.map.put(stationID, dto);
	}
	
	public Iterable<String> stations() {
		return this.map.keySet();
	}
	
	public T get(String stationID) {
		return this.map.get(stationID);
	}
	
	public int size() {
		return this.map.size();
	}
	
	@Override
	public String toString() {
		return "[" + size() + "]";
	}
}
