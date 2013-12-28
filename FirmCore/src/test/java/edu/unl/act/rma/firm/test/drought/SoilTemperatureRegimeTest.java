package edu.unl.act.rma.firm.test.drought;

import junit.framework.TestCase;
import edu.unl.act.rma.firm.drought.SoilTemperatureRegime;

public class SoilTemperatureRegimeTest extends TestCase {
	public void testEnumValues() {
		SoilTemperatureRegime[] soilTemperatureRegimes = SoilTemperatureRegime.values();
		assertEquals("enum count", 11, soilTemperatureRegimes.length);
		for (SoilTemperatureRegime soilTemperatureRegime : soilTemperatureRegimes) {
			assertEquals(
					"findRegmine()",
					soilTemperatureRegime,
					SoilTemperatureRegime.findRegime(soilTemperatureRegime.getIndex()));
			assertNotNull("getName() not NULL", soilTemperatureRegime.getName());
			assertNotSame("getName() not empty string", "", soilTemperatureRegime.getName());
			assertTrue(
					"getIndex() between -1 and 9",
					-1 <= soilTemperatureRegime.getIndex() && 9 >= soilTemperatureRegime.getIndex());
		}
		assertEquals(
				"findRegime() with invalid index",
				SoilTemperatureRegime.UNDEFINED,
				SoilTemperatureRegime.findRegime(512));
	}
}