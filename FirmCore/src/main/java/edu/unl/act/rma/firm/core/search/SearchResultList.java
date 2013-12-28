/* Created On Feb 21, 2007 */
package edu.unl.act.rma.firm.core.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Ian Cottingham
 *
 */
public class SearchResultList implements Serializable, Iterable<SearchResult> {

	protected static final long serialVersionUID = 1L;
	
	protected ArrayList<SearchResult> baseResults;
	private ArrayList<SearchResult> resultList;
	protected Map<String, ArrayList<SearchResult>> resultMap;
	private int position;
	private int maxResults;

	public SearchResultList(int maxResults) {
		this.maxResults = maxResults;
		baseResults = new ArrayList<SearchResult>();
		resultMap = new HashMap<String, ArrayList<SearchResult>>();
	}
	
	public SearchResultList(int maxResults, ArrayList<SearchResult> resultList) {
		this.maxResults = maxResults;
		this.resultList = resultList;
		this.baseResults = resultList;
	}
	
	public boolean displayPrevious() { 
		return ( position > maxResults ) ? true: false;
	}
	
	public boolean displayNext() { 
		return ( position >= resultList.size() ) ? false : true;
	}
	
	public List<SearchResult> next(boolean hold) {
		if ( resultList.size() == 0 ) { 
			return new ArrayList<SearchResult>();
		}
		
		ArrayList<SearchResult> list = new ArrayList<SearchResult>();
		
		if ( hold ) { 
			position -= maxResults;
			position = ( position < 0 ) ? 0 : position;
		}
		
		if ( position >= resultList.size() ) { 
			position = 0;
		}
				
		do {
			list.add(resultList.get(position));
		} while ( (!(++position >= resultList.size())) && (list.size() < maxResults) );
		
		return list;
	}
	
	public void resetPagePosition() { 
		position = 0;
	}
	
	public List<SearchResult> back() { 
		position -= (2*maxResults);
		
		if ( position < 0 ) {
			position = 0;
		}
		
		return next(false);
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}
	
	public void sort(Comparator<SearchResult> sorter) { 
		Collections.sort(resultList, sorter);
	}
	
	public void applyFilter(SearchResultFilter filter) { 
		resultList = new ArrayList<SearchResult>();
		
		for ( SearchResult res : baseResults ) { 
			if ( filter.allowResult(res) ) { 
				resultList.add(res);
			}
		}
	}
	
	public void clearFilter() { 
		this.resultList = baseResults;
	}
	
	protected void addTypedResult(String type, ArrayList<SearchResult> resultList) { 
		this.baseResults.addAll(resultList);
		this.resultList = baseResults;
		this.resultMap.put(type, resultList);
	}

	public Iterator<SearchResult> iterator() {
		return resultList.iterator();
	}
}
