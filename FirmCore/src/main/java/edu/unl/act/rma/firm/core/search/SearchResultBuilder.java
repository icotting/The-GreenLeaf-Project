/* Created On Mar 27, 2007 */
package edu.unl.act.rma.firm.core.search;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.TreeSet;

import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;

/**
 * @author Ian Cottingham
 *
 */
public class SearchResultBuilder implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG, SearchResultBuilder.class);
		
	private String currentType;
	private String currentEnumType;
	private TreeSet<SearchResult> typeResults;
	private SearchResult currentResult;
	private boolean typeOpen;
	private boolean resultOpen;
	private final int descriptionLength;
	private final int nameLength;
	private final SearchResultList resultList;
			
	public SearchResultBuilder(int descriptionLength, int nameLength, int resultLength) {

		resultList = new SearchResultList(resultLength);
		this.descriptionLength = descriptionLength;
		this.nameLength = nameLength;
	}

	public void openTypeSet(String type, String enumeratedType) {
		if ( typeOpen ) { 
			throw new IllegalStateException("the current type must be closed before a new one can be opened");
		}
		
		currentType = type;
		currentEnumType = enumeratedType;
		typeResults = new TreeSet<SearchResult>();
		typeOpen = true;
	}
	
	public void closeTypeSet() { 
		if ( !typeOpen ) { 
			throw new IllegalStateException("there is no active type set open");
		} else if ( resultOpen ) { 
			throw new IllegalStateException("there is a result currently open, close it before you close the type set");
		}
		
		ArrayList<SearchResult> res = new ArrayList<SearchResult>();
		res.addAll(typeResults);
		
		resultList.addTypedResult(currentType, res);
		typeOpen = false;
	}
	
	public void processObject(Object obj) { 
		openResult();
		Method[] methods = obj.getClass().getMethods();
		
		for ( Method method : methods ) {
			try {
				if ( method.getAnnotation(SearchResultName.class) != null ) { 
					this.setName((String)method.invoke(obj, new Object[0]));
				} else if ( method.getAnnotation(SearchResultDescription.class) != null ) { 
					this.setDescription((String)method.invoke(obj, new Object[0]));
				} else if ( method.getAnnotation(SearchResultIdentifier.class) != null) { 
					this.setIdentifier((Long)method.invoke(obj, new Object[0]));
				} else if ( method.getAnnotation(SearchResultMeta.class) != null ) { 
					String name = method.getAnnotation(SearchResultMeta.class).name();
					this.addMeta(name, method.invoke(obj, new Object[0]));
				}
			} catch ( Exception e ) { 
				LOG.error("could not create search result for object", e);
				discardResult();
				return;
			}
		}
		
		closeResult();
	}
	
	public void openResult() { 
		if ( resultOpen ) { 
			throw new IllegalStateException("the result cannot be opened until the previous result is closed");
		} else if ( !typeOpen ) { 
			throw new IllegalStateException("the result cannot be opened until a type set has been opened");
		}
		
		currentResult = new SearchResult(currentType, currentEnumType);
		resultOpen = true;
	}
	
	public void closeResult() { 
		typeResults.add(currentResult);
		resultOpen = false;
	}
	
	public void discardResult() {
		currentResult = null;
		resultOpen = false;
	}
	
	public void setName(String name) {
		if ( !resultOpen ) { 
			throw new IllegalStateException("the result must be opened before any data can be set");
		}
		
		if ( name == null ) { 
			name = "NO TITLE";
		} else if ( name.length() > nameLength ) { 
			name = name.substring(0, nameLength)+"...";
		}
		
		currentResult.setName(name);
	}
	
	public void setDescription(String description) { 
		if ( !resultOpen ) { 
			throw new IllegalStateException("the result must be opened before any data can be set");
		}
		
		if ( description == null ) {
			description = "no description provided";
		} else if ( description.length() > descriptionLength ) { 
			description = description.substring(0, descriptionLength)+"...";
		}
		
		currentResult.setDescription(description);
	}
	
	public void setIdentifier(long identifier) { 
		if ( !resultOpen ) { 
			throw new IllegalStateException("the result must be opened before any data can be set");
		}
		currentResult.setIdentifier(identifier);
	}
	
	public void addMeta(String name, Object meta) { 
		if ( !resultOpen ) { 
			throw new IllegalStateException("the result must be opened before any data can be set");
		}
		currentResult.addMeta(name, meta);
	}
	
	public SearchResultList getResults() { 
		return this.resultList;
	}
}