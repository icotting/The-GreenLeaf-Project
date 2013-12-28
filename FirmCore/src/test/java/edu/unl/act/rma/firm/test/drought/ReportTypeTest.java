package edu.unl.act.rma.firm.test.drought;

import junit.framework.TestCase;
import edu.unl.act.rma.firm.drought.ReportType;

public class ReportTypeTest extends TestCase {
	public void testEnumValues() {
		ReportType[] reportTypes = ReportType.values();
		assertEquals("enum count", 4, reportTypes.length);
		for (ReportType reportType : reportTypes) {
			assertNotNull("print() not NULL", reportType.print());
			assertNotSame("print() not empty string", "", reportType.print());
		}
	}
}