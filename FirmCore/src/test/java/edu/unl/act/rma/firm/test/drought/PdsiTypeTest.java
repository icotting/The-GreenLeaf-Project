package edu.unl.act.rma.firm.test.drought;

import junit.framework.TestCase;
import edu.unl.act.rma.firm.drought.PdsiType;

public class PdsiTypeTest extends TestCase {
	public void testEnumValues() {
		PdsiType[] pdsiTypes = PdsiType.values();
		assertEquals("enum count", 4, pdsiTypes.length);
		for (PdsiType pdsiType : pdsiTypes) {
			assertNotNull("getName() not NULL", pdsiType.getName());
			assertNotSame("getName() not empty string", "", pdsiType.getName());
			assertTrue(
					"getType() between 1 and 4",
					1 <= pdsiType.getType() && 4 >= pdsiType.getType());
		}
	}
}