package edu.unl.act.rma.firm.test.core;

import junit.framework.TestCase;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.climate.VariableFilter;
import edu.unl.act.rma.firm.climate.VariableMetaData;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.TemporalPeriod;

public class DataDTOTest extends TestCase {

	public void testEmptyTemporalPeriodToString() {
		TemporalPeriod period = new TemporalPeriod((Long)null, (Long)null);
		assertNotNull("null toString() on empty period", period.toString());
		
		period = new TemporalPeriod((DateTime)null, (DateTime)null);
		assertNotNull("null toString() on empty period", period.toString());
	}

	public void testTemporalPeriodToString() {
		TemporalPeriod period = new TemporalPeriod(new DateTime(), new DateTime().plusYears(1));
		assertNotNull("null toString() on period", period.toString());
	}
	
	public void testVariableFilterToString() {
		VariableFilter filter = new VariableFilter();
		assertNotNull("null toString() on empty filter", filter.toString());
		
		filter.setVariableType(DataType.AWC);
		assertNotNull("null toString() on filter", filter.toString());
	}
	
	public void testVariableMetaDataToString() {
		DateTime now = new DateTime();
		VariableMetaData dto = new VariableMetaData(now.getMillis(), now.plusYears(1).getMillis(), null, 0.1f);
		assertNotNull("null toString() on dto", dto.toString());
		
		dto = new VariableMetaData(now.getMillis(), now.plusYears(1).getMillis(), "name", 0.1f);
		assertNotNull("null toString() on dto", dto.toString());
	}
}
