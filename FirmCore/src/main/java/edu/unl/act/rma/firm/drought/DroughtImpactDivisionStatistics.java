/* Created on: Mar 18, 2010 */
package edu.unl.act.rma.firm.drought;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;

/**
 * @author Ian Cottingham
 *
 */
public class DroughtImpactDivisionStatistics<T> implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG, DroughtImpactDivisionStatistics.class);
	
	private final T division;
	private final int impactCount;
	private final int reportcount;
	private final HashMap<DroughtReportCategory, Integer> categoryCounts;
	private final HashMap<DroughtReportCategory, Float> categoryLosses;
	
	public DroughtImpactDivisionStatistics(T division, int impactCount, int reportCount, 
			HashMap<DroughtReportCategory, Integer> categoryCounts, HashMap<DroughtReportCategory, Float> categoryLosses) { 
		
		this.division = division;
		this.impactCount = impactCount; 
		this.reportcount = reportCount;
		this.categoryCounts = categoryCounts;
		this.categoryLosses = categoryLosses;
	}
	
	public T getDivision() {
		return division;
	}
	
	public HashMap<DroughtReportCategory, Integer> getCategoryCounts() {
		return categoryCounts;
	}

	public HashMap<String, String> getCategoryCountsForJsf() { 
		return new HashMap<String, String>() { 
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String get(Object key) {
				try {
					String key_str = (String)key;
					Integer i = categoryCounts.get(DroughtReportCategory.fromString(key_str));
					if ( i == null ) { 
						return "-";
					} else {
						return String.valueOf(i);
					}
				} catch ( ClassCastException cce ) { 
					LOG.warn("The getter for JSF count data was not a string");
					return "";
				}
			}
		};
	}

	public HashMap<DroughtReportCategory, Float> getCategoryLosses() {
		return categoryLosses;
	}

	public Set<DroughtReportCategory> getCategories() { 
		return categoryCounts.keySet();
	}

	public int getImpactCount() {
		return impactCount;
	}



	public int getReportcount() {
		return reportcount;
	}

	
	public DroughtReportCategory getDominantCategory() { 
		DroughtReportCategory dominant = null;
		int max = 0;
		
		for ( DroughtReportCategory cat : categoryCounts.keySet() ) { 
			if ( categoryCounts.get(cat) > max ) { 
				max = categoryCounts.get(cat);
				dominant = cat;
			}
		}
		
		return dominant;
	}
	
}
