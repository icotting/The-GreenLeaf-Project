package edu.unl.act.rma.firm.test.drought;

import junit.framework.TestCase;
import edu.unl.act.rma.firm.drought.SoilMoistureRegime;
import edu.unl.act.rma.firm.drought.SoilMoistureRegimeSubdivision;

public class SoilMoistureRegimeSubdivisionTest extends TestCase {
	public void testEnumValues() {
		SoilMoistureRegimeSubdivision[] soilMoistureRegimeSubdivisions = SoilMoistureRegimeSubdivision.values();
		assertEquals("enum count", 16, soilMoistureRegimeSubdivisions.length);
		for (SoilMoistureRegimeSubdivision soilMoistureRegimeSubdivision : soilMoistureRegimeSubdivisions) {
			assertEquals(
					"findRegmine()",
					soilMoistureRegimeSubdivision,
					SoilMoistureRegimeSubdivision.findRegime(soilMoistureRegimeSubdivision.getIndex()));
			assertNotNull("getName() not NULL", soilMoistureRegimeSubdivision.getName());
			assertNotSame("getName() not empty string", "", soilMoistureRegimeSubdivision.getName());
			assertTrue(
					"getIndex() between 0 and 15",
					0 <= soilMoistureRegimeSubdivision.getIndex() && 15 >=  soilMoistureRegimeSubdivision.getIndex());
			if (SoilMoistureRegimeSubdivision.UNDEFINED == soilMoistureRegimeSubdivision) {
				assertEquals(
						"getRegime() == UNDEFINED",
						SoilMoistureRegime.UNDEFINED,
						soilMoistureRegimeSubdivision.getRegime());
			}
			else {
				assertNotSame(
						"getRegime() not UNDEFINED",
						SoilMoistureRegime.UNDEFINED,
						soilMoistureRegimeSubdivision.getRegime());
			}
		}
		assertEquals(
				"findRegime() with invalid index",
				SoilMoistureRegimeSubdivision.UNDEFINED,
				SoilMoistureRegimeSubdivision.findRegime(512));
	}
}