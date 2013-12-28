package edu.unl.act.rma.firm.core;

import java.util.ArrayList;
import java.util.List;



public class MetaDataCollectionEditor<T> implements BooleanOperable<MetaDataCollection<T>> {

	private final MetaDataCollection<T> collection;
	
	public MetaDataCollectionEditor(MetaDataCollection<T> collection) { 
		this.collection = collection;
	}
	
	public void and(MetaDataCollection<T> object) {
		ArrayList<String> to_remove = new ArrayList<String>();
		for ( String station_id : collection.metaMap.keySet() ) { 
			if ( object.getStationMetaData(station_id) == null ) { 
				to_remove.add(station_id);
			}
		}
		
		for ( String remove_id : to_remove ) { 
			collection.metaMap.remove(remove_id);
		}
	}

	public void not(MetaDataCollection<T> object) {
		ArrayList<String> to_remove = new ArrayList<String>();
		for ( String station_id : collection.metaMap.keySet() ) { 
			if ( object.getStationMetaData(station_id) != null ) { 
				to_remove.add(station_id);
			}
		}
		
		for ( String remove_id : to_remove ) { 
			collection.metaMap.remove(remove_id);
		}
	}

	public void or(MetaDataCollection<T> object) {
		for ( String station_id : object ) { 
			if ( collection.metaMap.get(station_id) == null ) { 
				collection.metaMap.put(station_id, object.getStationMetaData(station_id));
			}
		}
	}
	
	public void and(List<String> stations) {
		ArrayList<String> to_remove = new ArrayList<String>(); // avoid a concurrent modification exception
		
		for ( String str : collection.metaMap.keySet() ) { 
			if ( !stations.contains(str) ) { 
				to_remove.add(str);
			}
		}
		
		for ( String str : to_remove ) { 
			collection.metaMap.remove(str);
		}
	}
}
