/* Created On: Jun 9, 2006 */
package edu.unl.act.rma.firm.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



/**
 * @author Ian Cottingham 
 *
 */
public class MetaDataCollection<T> implements Iterable<String>, Serializable {

	private static final long serialVersionUID = 2L;
	
	protected final Map<String, Map<T, Object>> metaMap;
	private boolean openStation;
	private final List<T> typeList;
	
	private Map<T, Object> stationMetaData;
	private String currentStation;
	
	private List<String> sortedList;
	
	public MetaDataCollection() {
		metaMap = new HashMap<String, Map<T, Object>>();
		typeList = new ArrayList<T>();
	}
	
	public MetaDataCollection(Map<String, Map<T, Object>> metaMap) {
		this.metaMap = metaMap;
		typeList = new ArrayList<T>();
		Map<T, Object> map;
		for ( String str : metaMap.keySet() ) { 
			map = metaMap.get(str);
			for ( T elem : map.keySet() ) { 
				if ( !typeList.contains(elem) ) { 
					typeList.add(elem);
				}
			}
		}
	}
	
	public void startStation(String stationID) throws InvalidStateException { 
		if ( openStation ) {
			throw new InvalidStateException("the previous station must be written or disposed before opening a new station");
		}
		
		stationMetaData = new HashMap<T, Object>();
		openStation = true;
		currentStation = stationID;
	}
	
	public void add(T type, Object value) throws InvalidStateException { 
		if ( !openStation ) {
			throw new InvalidStateException("a station must be opened before adding data to it");
		}
		
		typeList.add(type);
		stationMetaData.put(type, value);
	}
	
	public void writeStation() throws InvalidStateException { 
		if ( !openStation ) {
			throw new InvalidStateException("a station must be opened before writing it");
		}
		
		metaMap.put(currentStation, stationMetaData);
		openStation = false;
	}
	
	public void importMap(T type, Map<String, Object> map) throws InvalidStateException { 
		for ( String str : map.keySet() ) { 
			Map<T, Object> station_map = metaMap.get(str);
			if ( station_map == null ) { 
				station_map = new HashMap<T, Object>();
				metaMap.put(str, station_map);
			}
			station_map.put(type, map.get(str));
		}
		typeList.add(type);
	}
	
	public Map<T, Object> getStationMetaData(String stationID) { 
		return metaMap.get(stationID);
	}
	
	/**
	 * This method will allow easy generation of pre 0.3 style meta data maps and is a convenient
	 * way to get a set of values for a single meta datumm.
	 * 
	 * @param type - the field type to extract
	 * @return a single Mapping of station ID to the argument MetaDataType value for that station
	 */
	public Map<String, Object> extractType(StationMetaDataType type) { 
		Map<String, Object> datum = new HashMap<String, Object>();
		for ( String str : metaMap.keySet() ) { 
			datum.put(str, metaMap.get(str).get(type));
		}
		
		return datum;
	}
	
	/**
	 * Will attach a sorted list of stations to the object to allow for traversal with the iterator
	 * in sorted order.  The order of the sorted list is independant of any logic contained in this
	 * object. (i.e. it was sorted in some other way wither with a Collections object, SQL query
	 * etc.)
	 * 
	 * This method will ignore stations from the sortedList which are not contained in this object
	 * 
	 * @param sortedList
	 */
	public void attachSortedStationList(List<String> sortedList) { 
		ArrayList<String> stations = new ArrayList<String>();
		
		/* ensure that only data available in this object is included in the "sorted" list */
		for ( String str : sortedList ) { 
			if ( metaMap.get(str) != null ) { 
				stations.add(str);
			}
		}
		
		this.sortedList = stations;
	}
	
	public Iterator<String> iterator() { 
		return (sortedList == null ) ? metaMap.keySet().iterator() : sortedList.iterator();
	}
	
	public List<T> getTypes() { 
		return this.typeList;
	}

	@Override
	public String toString() {
		if (currentStation != null) {
			return currentStation;
		}
		if (metaMap != null) {
			return metaMap.size() + " stations";
		}
		return "";
	}
}
