/* Created on: Mar 15, 2010 */
package edu.unl.act.rma.firm.drought;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;


/**
 * @author Ian Cottingham
 *
 */
public class DroughtImpactStatistics<T> implements Serializable, Iterable<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private HashMap<T, DroughtImpactDivisionStatistics<T>> statistics;
	
	public DroughtImpactStatistics(HashMap<T, DroughtImpactDivisionStatistics<T>> statistics) { 
		this.statistics = statistics;
	}


	@Override
	public Iterator<T> iterator() {
		if ( statistics == null ) { 
			return new Iterator<T>() {

				@Override
				public boolean hasNext() { return false; }

				@Override
				public T next() { return null; }

				@Override
				public void remove() { }
				
			};
		}
		return statistics.keySet().iterator();
	}

    public boolean containsDivision(T division) {
        return statistics.get(division) != null;
    }

	public Iterable<DroughtReportCategory> divisionCategories(T division) {
		if ( statistics.get(division).getCategories() == null ) { 
			throw new RuntimeException("There are no categories for that division");
		}
		
		final Iterator<DroughtReportCategory> iterator = statistics.get(division).getCategories().iterator();
		return new Iterable<DroughtReportCategory>() {

			@Override
			public Iterator<DroughtReportCategory> iterator() {
				return iterator;
			}
		};
	}
	
	public int getDivisionImpactCount(T division) { 
		if ( statistics.get(division) == null ) { 
			return 0;
		} else { 
			return statistics.get(division).getImpactCount();
		}
	}
	
	public DroughtImpactDivisionStatistics<T> getDivisionStatistics(T division) { 
		return statistics.get(division);
	}
	
	public Collection<DroughtImpactDivisionStatistics<T>> getStatistics() { 
		ArrayList<DroughtImpactDivisionStatistics<T>> stats = new ArrayList<DroughtImpactDivisionStatistics<T>>();
		for ( DroughtImpactDivisionStatistics<T> stat : statistics.values() ) { 
			stats.add(stat);
		}
		
		Collections.sort(stats, new Comparator<DroughtImpactDivisionStatistics<T>>() {

			@Override
			public int compare(DroughtImpactDivisionStatistics<T> o1,
					DroughtImpactDivisionStatistics<T> o2) {
					return o1.getDivision().toString().compareTo(o2.getDivision().toString());
			}
		});
		
		return stats;
	}
	
	public int getDivisionCategoryCount(T division, DroughtReportCategory category) { 
		HashMap<DroughtReportCategory, Integer> division_categories = statistics.get(division).getCategoryCounts();
		if ( division_categories == null ) { 
			return 0;
		} else if ( division_categories.get(category) == null ) { 
			return 0;
		} else { 
			return division_categories.get(category);
		}
	}
	
	public float getDivisionCategoryLoss(T division, DroughtReportCategory category) { 
		HashMap<DroughtReportCategory, Float> division_category_losses = statistics.get(division).getCategoryLosses();
		if ( division_category_losses == null ) { 
			return 0f;
		} else if ( division_category_losses.get(category) == null ) { 
			return 0f;
		} else { 
			return division_category_losses.get(category);
		}
	}
	
	public void merge(DroughtImpactStatistics<T> mergeStats) { 
		if ( statistics == null ) { 
			statistics = new HashMap<T, DroughtImpactDivisionStatistics<T>>();
		}
		
		if ( mergeStats != null ) {
			for ( T div : mergeStats ) { 
				statistics.put(div, mergeStats.getDivisionStatistics(div));
			}
		}
	}
}
