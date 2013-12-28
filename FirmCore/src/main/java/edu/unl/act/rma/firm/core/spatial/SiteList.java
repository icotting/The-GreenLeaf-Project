/* Created On: Oct 28, 2005 */
package edu.unl.act.rma.firm.core.spatial;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.unl.act.rma.firm.core.MetaDataCollection;
import edu.unl.act.rma.firm.core.StationMetaDataType;

/**
 * @author Ian Cottingham
 *
 */
public class SiteList implements Iterable<String> {

	private final MetaDataCollection<StationMetaDataType> metaData; 
	private final Map<String, Site> sites;

	public SiteList(MetaDataCollection<StationMetaDataType> metaData, Map<String, Site> sites, boolean check) { 
		if( check ) {
			for ( String str : sites.keySet() ) { 
				if ( metaData.getStationMetaData(str) == null ) {
					throw new RuntimeException("no entry found in meta data collection for "+str);
				}
			}
		}
		
		this.metaData = metaData;
		this.sites = sites;
	}

	public Iterator<String> iterator() {
		return sites.keySet().iterator();
	}	
	
	public Site getSite(String stationId) { 
		return sites.get(stationId);
	}
	
	public Object getMetaDatum(StationMetaDataType type, String stationId) { 
		return metaData.getStationMetaData(stationId).get(type);
	}
	
	public List<StationMetaDataType> getMetaTypes() { 
		return metaData.getTypes();
	}
}