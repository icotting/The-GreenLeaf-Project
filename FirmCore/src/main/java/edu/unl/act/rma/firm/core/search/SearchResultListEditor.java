package edu.unl.act.rma.firm.core.search;

import java.util.ArrayList;
import java.util.List;

import edu.unl.act.rma.firm.core.BooleanOperable;

/**
 * 
 * @author Ian Cottingham
 *
 */
public class SearchResultListEditor implements BooleanOperable<SearchResultList> {

	private final SearchResultList list;
	
	public SearchResultListEditor(SearchResultList list) { 
		this.list = list;
	}
	
	public void and(SearchResultList object) {
		ArrayList<SearchResult> to_remove = new ArrayList<SearchResult>();
		
		for ( SearchResult result : object ) { 
			if ( !list.baseResults.contains(result) ) {
				to_remove.add(result);
			}
		}
		
		removeResults(to_remove);
		
	}

	public void not(SearchResultList object) {
		ArrayList<SearchResult> to_remove = new ArrayList<SearchResult>();
		
		for ( SearchResult result : object ) { 
			if ( list.baseResults.contains(result) ) {
				to_remove.add(result);
			}
		}
		
		removeResults(to_remove);
	}

	public void or(SearchResultList object) {
		ArrayList<SearchResult> to_add = new ArrayList<SearchResult>();
		for ( SearchResult result : object ) { 
			if ( !list.baseResults.contains(result) ) {
				to_add.add(result);
			}
		}
		
		for ( SearchResult result : to_add ) { 
			list.baseResults.add(result);
			ArrayList<SearchResult> result_list = list.resultMap.get(result.getResultType());
			if ( result_list == null ) { 
				result_list = new ArrayList<SearchResult>();
				list.resultMap.put(result.getResultType(), result_list);
			}
			
			result_list.add(result);
		}
	}
	
	private void removeResults(ArrayList<SearchResult> removeResults) { 
		for ( SearchResult to_remove : removeResults ) { 
			list.baseResults.remove(to_remove);
			for ( List<SearchResult> type_list : list.resultMap.values() ) { 
				type_list.remove(to_remove);
			}
		}
	}

}
