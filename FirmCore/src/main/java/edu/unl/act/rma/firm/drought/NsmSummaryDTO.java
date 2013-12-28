package edu.unl.act.rma.firm.drought;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.unl.act.rma.firm.core.DTOBase;

/**
 * Data Transfer Object holding NSM summary data for a single station.  Averages of the yearly data accompanies the complete
 * yearly data.
 * 
 * @see edu.unl.firm.unsafe.NsmObject
 * @author Jon Dokulil
 */
public class NsmSummaryDTO extends DTOBase {
	
	private static final long serialVersionUID = 4L;
	
	private SortedMap<Integer, NsmSummaryDTO.DataSet> map;
	private Collection<RegimeSummary> regimes;
	private Collection<RegimeSubdivisionSummary> subdivisions;
	private float precipitationAvg;
	private float evapotranspirationAvg;
	private float awbAvg;
	private float msdAvg;
	private int dryDaysAvg;
	private int mediumDaysAvg; 
	private int moistDaysAvg;
	private int bio8Avg;
	
	private int startYear = 9999999;
	private int endYear = 0;
	
	/**
	 * Default constructor
	 */
	public NsmSummaryDTO(){
		this.map = new TreeMap<Integer, DataSet>();
		this.regimes = new ArrayList<RegimeSummary>();
		this.subdivisions = new ArrayList<RegimeSubdivisionSummary>();
		
		this.precipitationAvg = 0.0f;
		this.evapotranspirationAvg = 0.0f;
		this.awbAvg = 0.0f;
		this.msdAvg = 0.0f;
		this.dryDaysAvg = 0;
		this.mediumDaysAvg = 0;
		this.moistDaysAvg = 0;
		this.bio8Avg = 0;
	}
	/**
	 * Constructor, only called from within the NSM native component.
	 */
	public NsmSummaryDTO(float precAvg, float evapAvg, float awbAvg, float msdAvg, int dryDayAvg, int mdDayAvg, int moistDayAvg, int bio8Avg) {
		this.map = new TreeMap<Integer, DataSet>();
		this.regimes = new ArrayList<RegimeSummary>();
		this.subdivisions = new ArrayList<RegimeSubdivisionSummary>();
		
		this.precipitationAvg = precAvg;
		this.evapotranspirationAvg = evapAvg;
		this.awbAvg = awbAvg;
		this.msdAvg = msdAvg;
		this.dryDaysAvg = dryDayAvg;
		this.mediumDaysAvg = mdDayAvg;
		this.moistDaysAvg = moistDayAvg;
		this.bio8Avg = bio8Avg;
	}
	
	/**
	 * @return The number of years of summary data
	 */
	public int size() {
		return this.map.size();
	}
	
	/**
	 * Adss a year of summary data, replacing any existing data for that year.
	 * 
	 * @param year The year of the given data
	 * @param data Summary data for the given year
	 */
	public void add(int year, NsmSummaryDTO.DataSet data) {
		this.map.put(new Integer(year), data);
		if ( year < startYear ) { 
			startYear = year;
		}
		
		if ( year > endYear ) { 
			endYear = year;
		}
	}

	public int getStartYear() {
		return startYear;
	}

	public int getEndYear() {
		return endYear;
	}

	/**
	 * Adds a regime summary to the set of existing summaries.
	 * 
	 * @param summary The regime summary to add
	 */
	public void addRegimeSummary(RegimeSummary summary) {
		this.regimes.add(summary);
	}
	
	/**
	 * Adds a regime summary to the set of existing summaries.
	 * 
	 * @param summary The regime summary to add
	 */
	public void addRegimeSubdivisionSummary(RegimeSubdivisionSummary summary) {
		this.subdivisions.add(summary);
	}
	
	/**
	 * @return An iterable over the years of stored data, returned in ascending order
	 */
	public Iterable<Integer> years() {
		return this.map.keySet();
	}
	
	/**
	 * Gets the data set for the given year if it exists, null otherwise.
	 * 
	 * @param year The year of data requested
	 * @return The data set for the given year if it exists, null otherwise.
	 */
	public NsmSummaryDTO.DataSet getData(Integer year) {
		return this.map.get(year);
	}
	
	public List<NsmSummaryDTO.DataSet> getData() { 
		ArrayList<NsmSummaryDTO.DataSet> data = new ArrayList<NsmSummaryDTO.DataSet>();
		for ( NsmSummaryDTO.DataSet datum : map.values() ) {
			data.add(datum);
		}
		
		return data;
	}
	
	@Override
	public String toString() {
		if (map.isEmpty()) {
			return "zero elements";
		}
		return map.firstKey() + " - " + map.lastKey();
	}
	
	/**
	 * Holds summary data related to a single soil moisture regime.
	 * 
	 * @see SoilMoistureRegime
	 * @author Jon Dokulil
	 */
	public static class RegimeSummary implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private int count;
		private float frequency;
		private SoilMoistureRegime regime;

		/**
		 * Constructor, only called from within the NSM native component.
		 */
		public RegimeSummary(int count, float frequency, SoilMoistureRegime regime) {
			this.count = count;
			this.frequency = frequency;
			this.regime = regime;
		}

		/**
		 * @return The count.
		 */
		public int getCount() {
			return count;
		}

		/**
		 * @return The frequency.
		 */
		public float getFrequency() {
			return frequency;
		}

		/**
		 * @return The regime.
		 */
		public SoilMoistureRegime getRegime() {
			return regime;
		}
	}
	

	/**
	 * Holds summary data related to a single soil moisture regime subdivision.
	 * 
	 * @see SoilMoistureRegimeSubdivision
	 * @author Jon Dokulil
	 */
	public static class RegimeSubdivisionSummary implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private int count;
		private float frequency;
		private SoilMoistureRegimeSubdivision regime;

		/**
		 * Constructor, only called from within the NSM native component.
		 */
		public RegimeSubdivisionSummary(int count, float frequency, SoilMoistureRegimeSubdivision regime) {
			this.count = count;
			this.frequency = frequency;
			this.regime = regime;
		}

		/**
		 * @return The count.
		 */
		public int getCount() {
			return count;
		}

		/**
		 * @return The frequency.
		 */
		public float getFrequency() {
			return frequency;
		}

		/**
		 * @return The regime.
		 */
		public SoilMoistureRegimeSubdivision getRegime() {
			return regime;
		}
	}
	
	/**
	 * Holds summary data for a single station for a single year.  
	 * 
	 * @author Jon Dokulil
	 */
	public static class DataSet implements Serializable {
		private static final long serialVersionUID = 1L;

		private float precipitation;
		private float evapotranspiration;
		private float awb;
		private float msd;
		private int dryDays;
		private int mediumDays;
		private int moistDays;
		private int bio8;
		private SoilMoistureRegimeSubdivision regime;

		/**
		 * Constructor, only called from within the NSM native component.
		 */
		public DataSet(float pret, float pet, float awb, float msd, int dryDays, int mdDays, int moistDays, int bio8, SoilMoistureRegimeSubdivision regime) {
			this.precipitation = pret;
			this.evapotranspiration = pet;
			this.awb = awb;
			this.msd = msd;
			this.dryDays = dryDays;
			this.mediumDays = mdDays;
			this.moistDays = moistDays;
			this.bio8 = bio8;
			this.regime = regime;
		}

		/**
		 * @return The awb.
		 */
		public float getAwb() {
			return awb;
		}

		/**
		 * @param awb The awb to set.
		 */
		public void setAwb(float awb) {
			this.awb = awb;
		}

		/**
		 * @return The bio8.
		 */
		public int getBio8() {
			return bio8;
		}

		/**
		 * @param bio8 The bio8 to set.
		 */
		public void setBio8(int bio8) {
			this.bio8 = bio8;
		}

		/**
		 * @return The dryDays.
		 */
		public int getDryDays() {
			return dryDays;
		}

		/**
		 * @param dryDays The dryDays to set.
		 */
		public void setDryDays(int dryDays) {
			this.dryDays = dryDays;
		}

		/**
		 * @return The evapotranspiration.
		 */
		public float getEvapotranspiration() {
			return evapotranspiration;
		}

		/**
		 * @param evapotranspiration The evapotranspiration to set.
		 */
		public void setEvapotranspiration(float evapotranspiration) {
			this.evapotranspiration = evapotranspiration;
		}

		/**
		 * @return The mediumDays.
		 */
		public int getMediumDays() {
			return mediumDays;
		}

		/**
		 * @param mediumDays The mediumDays to set.
		 */
		public void setMediumDays(int mediumDays) {
			this.mediumDays = mediumDays;
		}

		/**
		 * @return The moistDays.
		 */
		public int getMoistDays() {
			return moistDays;
		}

		/**
		 * @param moistDays The moistDays to set.
		 */
		public void setMoistDays(int moistDays) {
			this.moistDays = moistDays;
		}

		/**
		 * @return The msd.
		 */
		public float getMsd() {
			return msd;
		}

		/**
		 * @param msd The msd to set.
		 */
		public void setMsd(float msd) {
			this.msd = msd;
		}

		/**
		 * @return The precipitation.
		 */
		public float getPrecipitation() {
			return precipitation;
		}

		/**
		 * @param precipitation The precipitation to set.
		 */
		public void setPrecipitation(float precipitation) {
			this.precipitation = precipitation;
		}

		/**
		 * @return The regime.
		 */
		public SoilMoistureRegimeSubdivision getRegime() {
			return regime;
		}

		/**
		 * @param regime The regime to set.
		 */
		public void setRegime(SoilMoistureRegimeSubdivision regime) {
			this.regime = regime;
		}
	}

	/**
	 * @return The awbAvg.
	 */
	public float getAwbAvg() {
		return awbAvg;
	}
	
	/**
	 * @param The awbAvg.
	 */
	public void setAwbAvg(float awbAvg) {
		this.awbAvg = awbAvg;
	}

	/**
	 * @return The bio8Avg.
	 */
	public float getBio8Avg() {
		return bio8Avg;
	}
	
	/**
	 * @param The bio8Avg.
	 */
	public void setBio8Avg(int bio8Avg) {
		this.bio8Avg = bio8Avg;
	}

	/**
	 * @return The dryDaysAvg.
	 */
	public float getDryDaysAvg() {
		return dryDaysAvg;
	}
	
	/**
	 * @param The dryDaysAvg.
	 */
	public void setDryDaysAvg(int dryDaysAvg) {
		this.dryDaysAvg = dryDaysAvg;
	}

	/**
	 * @return The evapotranspirationAvg.
	 */
	public float getEvapotranspirationAvg() {
		return evapotranspirationAvg;
	}
	
	/**
	 * @param The evapotranspirationAvg.
	 */
	public void setEvapotranspirationAvg(float evapotranspirationAvg) {
		this.evapotranspirationAvg = evapotranspirationAvg;
	}

	/**
	 * @return The mediumDaysAvg.
	 */
	public float getMediumDaysAvg() {
		return mediumDaysAvg;
	}
	
	/**
	 * @param The mediumDaysAvg.
	 */
	public void setMediumDaysAvg(int mediumDaysAvg) {
		this.mediumDaysAvg = mediumDaysAvg;
	}

	/**
	 * @return The moistDaysAvg.
	 */
	public float getMoistDaysAvg() {
		return moistDaysAvg;
	}
	
	/**
	 * @param The moistDaysAvg.
	 */
	public void setMoistDaysAvg(int moistDaysAvg) {
		this.moistDaysAvg = moistDaysAvg;
	}

	/**
	 * @return The msdAvg.
	 */
	public float getMsdAvg() {
		return msdAvg;
	}
	
	/**
	 * @param The msdAvg.
	 */
	public void setMsdAvg(float msdAvg) {
		this.msdAvg = msdAvg;
	}

	/**
	 * @return The precipitationAvg.
	 */
	public float getPrecipitationAvg() {
		return precipitationAvg;
	}
	
	/**
	 * @param The precipitationAvg.
	 */
	public void setPrecipitationAvg(float precipitationAvg) {
		this.precipitationAvg = precipitationAvg;
	}
	
	/**
	 * @return The regimes.
	 */
	public Collection<RegimeSummary> getRegimes() {
		return regimes;
	}

	/**
	 * @param regimes The regimes to set.
	 */
	public void setRegimes(Collection<RegimeSummary> regimes) {
		this.regimes = regimes;
	}

	/**
	 * @return The subdivisions.
	 */
	public Collection<RegimeSubdivisionSummary> getSubdivisions() {
		return subdivisions;
	}

	/**
	 * @param subdivisions The subdivisions to set.
	 */
	public void setSubdivisions(Collection<RegimeSubdivisionSummary> subdivisions) {
		this.subdivisions = subdivisions;
	}
}

