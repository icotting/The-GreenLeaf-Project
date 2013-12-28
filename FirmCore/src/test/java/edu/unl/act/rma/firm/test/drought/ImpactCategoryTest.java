package edu.unl.act.rma.firm.test.drought;

import java.util.ArrayList;

import junit.framework.TestCase;
import edu.unl.act.rma.firm.drought.DroughtReportCategory;

public class ImpactCategoryTest extends TestCase {
	public void testEnumValues() {
		DroughtReportCategory[] impactCategories = DroughtReportCategory.values();
		assertEquals("enum count", 10, impactCategories.length);
		for (DroughtReportCategory impactCategory : impactCategories) {
			assertNotNull("getPrintName() not NULL", impactCategory.getPrintName());
			assertNotSame("getPrintName() not empty string", "", impactCategory.getPrintName());
			assertNotNull("getColor() not NULL", impactCategory.getColor());
		}
	}
	
	public void testList() {
		ArrayList<DroughtReportCategory> impactCategories = (ArrayList)DroughtReportCategory.list();
		assertEquals(
				"number of impact categories",
				DroughtReportCategory.class.getEnumConstants().length,
				impactCategories.size());
	}
}