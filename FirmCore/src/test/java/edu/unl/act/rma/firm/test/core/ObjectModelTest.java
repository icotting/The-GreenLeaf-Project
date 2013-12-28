package edu.unl.act.rma.firm.test.core;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.core.DateOptionsDTO;
import edu.unl.act.rma.firm.test.SupportMethods;

public class ObjectModelTest extends TestCase {

	
	public void testDateOptionsToString() {
		DateOptionsDTO dto = new DateOptionsDTO();
		assertNotNull("null toString() on empty dto", dto.toString());
		assertEquals("Incorrect toString()", "", dto.toString());
		
		DateTime start = new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		dto.setStart(start);
		assertNotNull("null toString() on non-empty dto", dto.toString());
		SupportMethods.assertToStringFormat(dto.toString());
		
		dto = new DateOptionsDTO();
		assertNotNull("null toString() on empty dto", dto.toString());
		SupportMethods.assertToStringFormat(dto.toString());
		
		DateTime end = new DateTime(2000, 1, 1, 0, 0, 0, 0, GregorianChronology.getInstance());
		dto.setEnd(end);
		assertNotNull("null toString() on non-empty dto", dto.toString());
		SupportMethods.assertToStringFormat(dto.toString());

		dto.setStart(start);
		assertNotNull("null toString() on non-empty dto", dto.toString());
		SupportMethods.assertToStringFormat(dto.toString());
	}

}
