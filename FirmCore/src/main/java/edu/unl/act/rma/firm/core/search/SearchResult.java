/* Created On Mar 27, 2007 */
package edu.unl.act.rma.firm.core.search;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ian Cottingham
 *
 */
public class SearchResult implements Serializable, Comparable<SearchResult> {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private String description;
	private final String resultType;
	private final String enumeratedType;
	private long identifier;
	private final Map<String, Object> meta;
	
	/**
	 * 
	 * @param resultType
	 * @param enumeratedType
	 */
	protected SearchResult(String resultType, String enumeratedType) { 
		this.meta = new HashMap<String, Object>();
		this.resultType = resultType;
		this.enumeratedType = enumeratedType;
	}
	
	protected void addMeta(String name, Object obj) { 
		this.meta.put(name, obj);
	}
	
	protected void setName(String name) { 
		this.name = name;
	}
	
	protected void setIdentifier(long identifier) { 
		this.identifier = identifier;
	}
	
	protected void setDescription(String description) {
		this.description = description;
	}
	
	public String getEnumeratedType() {
		return enumeratedType;
	}
	
	public String getName() { 
		return this.name;
	}
	
	public String getDescription() { 
		return this.description;
	}
	
	public long getIdentifier() { 
		return this.identifier;
	}
	
	public Object getMeta(String name) { 
		return this.meta.get(name);
	}

	public Map<String, Object> getMeta() {
		return this.meta;
	}
	
	public String getResultType() {
		return resultType;
	}

	public int compareTo(SearchResult o) {
		String table = o.getResultType();
		long id = o.getIdentifier();
		
		if ( (table.equals(this.resultType)) && ( id == this.identifier) ) {
			return 0;
		}
		
		if ( table.equals(this.resultType) ) { 
			return (this.name+this.identifier).compareTo(o.name+o.identifier);
		} else {
			return table.compareTo(this.resultType);
		}
	}
}
