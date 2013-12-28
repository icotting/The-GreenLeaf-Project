package edu.unl.act.rma.firm.test.drought;

import junit.framework.TestCase;
import edu.unl.act.rma.firm.drought.SoilMoistureRegime;

public class SoilMoistureRegimeTest extends TestCase {
	public void testEnumValues() {
		SoilMoistureRegime[] soilMoistureRegimes = SoilMoistureRegime.class.getEnumConstants();
		assertEquals("enum count", 6, soilMoistureRegimes.length);
		for (SoilMoistureRegime soilMoistureRegime : soilMoistureRegimes) {
			assertNotNull("getName() not NULL", soilMoistureRegime.getName());
			assertNotSame("getName() not empty string", "", soilMoistureRegime.getName());
			assertTrue(
					"getIndex() between 0 and 5",
					0 <= soilMoistureRegime.getIndex() && 5 >= soilMoistureRegime.getIndex());
		}
	}
}