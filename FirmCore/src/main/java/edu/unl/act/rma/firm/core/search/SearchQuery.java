/* Created On Mar 27, 2007 */
package edu.unl.act.rma.firm.core.search;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Ian Cottingham
 *
 */
public class SearchQuery implements Serializable, Iterable<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final Map<String, SortedSet<String>> queryParameters;
	
	private final Map<String, SortedSet<String>> resultFields;
	
	/* this is used to hold optional enumerated type names, which can be useful when iterating over 
	 * type mappings if the implementing system has such a type mapping
	 */
	private final Map<String, String> enumTypeMap;
	
	private final String queryString;
	
	public SearchQuery(String query) { 
		this.queryString = query;
		queryParameters = new HashMap<String, SortedSet<String>>();
		resultFields = new HashMap<String, SortedSet<String>>();
		enumTypeMap = new HashMap<String, String>();
	}
	
	public void addQueryParameter(String tableName, String fieldName, String enumeratedType) {
		enumTypeMap.put(tableName, enumeratedType);
		
		SortedSet<String> fields = queryParameters.get(tableName);
		
		if ( fields == null ) { 
			fields = new TreeSet<String>();
			queryParameters.put(tableName, fields);
		}
		
		if ( !fields.contains(fieldName) ) { 
			fields.add(fieldName);
		}
	}
	
	public void addQueryParameter(String tableName, String fieldName) {
		SortedSet<String> fields = queryParameters.get(tableName);
		
		if ( fields == null ) { 
			fields = new TreeSet<String>();
			queryParameters.put(tableName, fields);
		}
		
		if ( !fields.contains(fieldName) ) { 
			fields.add(fieldName);
		}
	}
	
	public void addResultField(String tableName, String fieldName) {
		SortedSet<String> fields = resultFields.get(tableName);
		
		if ( fields == null ) { 
			fields = new TreeSet<String>();
			resultFields.put(tableName, fields);
		}
		
		if ( !fields.contains(fieldName) ) { 
			fields.add(fieldName);
		}
	}
	
	public Iterator<String> iterator() {
		return queryParameters.keySet().iterator();
	}

	public Iterable<String> getSearchFields(String objectType) { 
		return queryParameters.get(objectType);
	}
	
	public Iterable<String> getResultFields(String objectType) { 
		return resultFields.get(objectType);
	}
	
	public String getEnumeratedType(String objectName) { 
		return enumTypeMap.get(objectName);
	}
	
	public String getQueryString() { 
		return this.queryString;
	}
	
	public boolean isDistinctResult(String type) { 
		return (resultFields.get(type).size() == 1); 
	}
}
