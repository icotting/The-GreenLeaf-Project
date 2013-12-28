/* Created On Feb 5, 2007 */
package edu.unl.act.rma.firm.core.component;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.ejb.Remote;

import edu.unl.act.rma.firm.core.search.SearchQuery;
import edu.unl.act.rma.firm.core.search.SearchResultList;

/**
 * @author Ian Cottingham
 *
 */
@Remote
public interface SearchEngine extends Serializable {

	public SearchResultList fuzzySearch(SearchQuery query) throws RemoteException;
	
}
