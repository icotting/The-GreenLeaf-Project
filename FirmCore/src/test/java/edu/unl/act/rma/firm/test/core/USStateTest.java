package edu.unl.act.rma.firm.test.core;

import java.io.ObjectStreamException;
import java.util.Collection;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.test.SupportMethods;

public class USStateTest extends TestCase {
	public void testGetFipsCode() throws ObjectStreamException {
		assertEquals("15", USState.Hawaii.getFipsCode());
	}
	
	public void testGetPostalCode() throws ObjectStreamException {
		assertEquals("GA", USState.Georgia.getPostalCode());
	}
	
	public void testGetStatesByRegex() {
		Collection<USState> states = USState.getStatesByRegex(Pattern.compile("^....$"));
		assertEquals(3, states.size());
		int count = 0;
		for (USState state : states) {
			if (USState.Iowa == state) {
				count++;
			} else if (USState.Ohio == state) {
				count++;
			} else if (USState.Utah == state) {
				count++;
			}
		}
		assertEquals(3, states.size());
	}
	
	public void testReadResolve() throws ObjectStreamException {
		SupportMethods.logInfo("Arizona");
		assertEquals(USState.Arizona, USState.Arizona.readResolve());
	}
}
