package edu.unl.act.rma.firm.test.drought;

import junit.framework.TestCase;
import edu.unl.act.rma.firm.drought.PdsiWeeklyStep;

public class PdsiWeeklyStepTest extends TestCase {
	public void testEnumValues() {
		PdsiWeeklyStep[] pdsiWeeklySteps = PdsiWeeklyStep.values();
		assertEquals("enum count", 4, pdsiWeeklySteps.length);
		for (PdsiWeeklyStep pdsiWeeklyStep : pdsiWeeklySteps) {
			assertNotNull("getName() not NULL", pdsiWeeklyStep.getName());
			assertNotSame("getName() not empty string", "", pdsiWeeklyStep.getName());
			assertTrue(
					"getStep() between 1 and 13",
					1 <= pdsiWeeklyStep.getStep() && 13 >= pdsiWeeklyStep.getStep());
			int perYear = 0;
			if (1 == pdsiWeeklyStep.getStep()) {
				perYear = 52;
			}
			else if (2 == pdsiWeeklyStep.getStep()) {
				perYear = 26;
			}
			else if (4 == pdsiWeeklyStep.getStep()) {
				perYear = 13;
			}
			else if (13 == pdsiWeeklyStep.getStep()) {
				perYear = 4;
			}
			assertEquals(
					"getValuesPerYear()",
					perYear,
					pdsiWeeklyStep.getValuesPerYear());
		}
	}
}