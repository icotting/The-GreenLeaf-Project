/* Created on Feb 5, 2009 */
package edu.unl.act.rma.firm.drought.component;

import java.util.HashMap;
import java.util.Iterator;

import edu.unl.act.rma.firm.drought.DroughtReportCategory;

/**
 * 
 * @author Ian Cottingham
 *
 */
public class ReportCountCollection implements Iterable<DroughtReportCategory> {

	private HashMap<DroughtReportCategory, Integer> categoryCounts = new HashMap<DroughtReportCategory, Integer>();
	private DroughtReportCategory dominateCategory = null;
	private int dominateCount = 0;
	
	protected void addCounts(DroughtReportCategory category, int count) { 
		int new_count;
		if ( categoryCounts.get(category) == null ) { 
			categoryCounts.put(category, count);
			new_count = count;
		} else { 
			new_count = categoryCounts.get(category)+count;
			categoryCounts.put(category, new_count);
		}
		
		if ( new_count > dominateCount ) { 
			dominateCount = new_count;
			dominateCategory = category;
		}
	}

	public DroughtReportCategory getDominateCategory() {
		return dominateCategory;
	}

	public int getCount(DroughtReportCategory category) { 
		return categoryCounts.get(category);
	}

	@Override
	public Iterator<DroughtReportCategory> iterator() {
		return categoryCounts.keySet().iterator();
	}
}
