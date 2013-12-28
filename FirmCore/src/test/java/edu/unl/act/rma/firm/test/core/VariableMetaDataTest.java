package edu.unl.act.rma.firm.test.core;

import junit.framework.TestCase;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.climate.VariableMetaData;
import edu.unl.act.rma.firm.core.TemporalPeriod;
import edu.unl.act.rma.firm.test.DataGenerator;
import edu.unl.act.rma.firm.test.SupportMethods;

public class VariableMetaDataTest extends TestCase {
	public void testConstructorParameters() {
		long start = DataGenerator.getLongGreaterThan(0L);
		long end = DataGenerator.getLongGreaterThan(start);
		String name = DataGenerator.getString(",=");
		float missingPercent = DataGenerator.getFloat();
		VariableMetaData variableMetaData = new VariableMetaData(
				start,
				end,
				name,
				missingPercent);
		TemporalPeriod temporalPeriod = variableMetaData.getVariablePeriod();
		assertEquals("getStartDate()", new DateTime(start), variableMetaData.getStartDate());
		assertEquals("getEndDate()", new DateTime(end), variableMetaData.getEndDate());
		assertEquals("getName()", name, variableMetaData.getName());
		assertEquals("getMissingPercent()", missingPercent, variableMetaData.getMissingPercent());
		assertNotNull("getVariablePeriod() != NULL", variableMetaData.getVariablePeriod());
		assertEquals("getVariablePeriod()", new DateTime(start), temporalPeriod.getStart());
		assertEquals("getVariablePeriod()", new DateTime(end), temporalPeriod.getEnd());
		String[][] expectedNamesAndValues = new String[][] {
				{"name", name},
				{"start", new DateTime(start).toString()},
				{"end", new DateTime(end).toString()},
				{"missing", Float.toString(missingPercent)}
		};
		SupportMethods.assertToStringFormat(
				variableMetaData.toString(),
				4,
				expectedNamesAndValues);
	}
}