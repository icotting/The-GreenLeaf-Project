/* Created on Oct 28, 2008 */
package edu.unl.act.rma.firm.test.core;

import edu.unl.act.rma.firm.core.StationSearchTerms;
import edu.unl.act.rma.firm.core.spatial.USState;
import junit.framework.TestCase;

/**
 * 
 * @author Ian Cottingham
 *
 */
public class StationSearchTermsTest extends TestCase {

	public void testStateName() { 
		StationSearchTerms terms = new StationSearchTerms();
		
		terms.setStateFromString("Nebraska");
		assertEquals("incorrect state value", USState.Nebraska, terms.getState());
	}

	public void testStateNameUpper() { 
		StationSearchTerms terms = new StationSearchTerms();
		
		terms.setStateFromString("UTAH");
		assertEquals("incorrect state value", USState.Utah, terms.getState());
	}
	
	public void testStateNameLower() { 
		StationSearchTerms terms = new StationSearchTerms();
		
		terms.setStateFromString("colorado");
		assertEquals("incorrect state value", USState.Colorado, terms.getState());
	}
	
	public void testStateNameAbbr() { 
		StationSearchTerms terms = new StationSearchTerms();
		
		terms.setStateFromString("CT");
		assertEquals("incorrect state value", USState.Connecticut, terms.getState());
	}

	public void testStateAbbrLower() { 
		StationSearchTerms terms = new StationSearchTerms();
		
		terms.setStateFromString("ma");
		assertEquals("incorrect state value", USState.Massachusetts, terms.getState());
	}
}
