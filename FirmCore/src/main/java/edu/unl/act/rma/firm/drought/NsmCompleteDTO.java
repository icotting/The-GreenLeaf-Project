package edu.unl.act.rma.firm.drought;

import java.io.Serializable;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.unl.act.rma.firm.core.DTOBase;

/**
 * Data Transfer Object holding NSM complete data for a single station.
 * 
 * @see edu.unl.firm.unsafe.NsmObject
 * @author Jon Dokulil
 */
public class NsmCompleteDTO extends DTOBase {
	
	private static final long serialVersionUID = 4L;

	private SortedMap<Integer, NsmCompleteDTO.DataSet> map;

	private int startYear = 9999999;
	private int endYear = 0;
	
	/**
	 * Constructor, only called from within the NSM native component.
	 */
	public NsmCompleteDTO() {
		this.map = new TreeMap<Integer, NsmCompleteDTO.DataSet>();
	}

	/**
	 * Adss a year of data, replacing any existing data for that year.
	 * 
	 * @param year The year of the given data
	 * @param data Complete data for the given year
	 */
	public void add(int year, NsmCompleteDTO.DataSet data) {
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
	 * @return The number of years of data
	 */
	public int size() {
		return this.map.size();
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
	public NsmCompleteDTO.DataSet getData(Integer year) {
		return this.map.get(year);
	}
	
	@Override
	public String toString() {
		if (map.isEmpty()) {
			return "zero elements";
		}
		return map.firstKey() + " - " + map.lastKey();
	}

	/**
	 * Holds data for a single station for a single year.  
	 * 
	 * @author Jon Dokulil
	 */
	public static class DataSet implements Serializable {
		private static final long serialVersionUID = 1L;

		private List<Float> monthlyTemperatures;
		private List<Float> monthlyPrecipitations;
		private List<Float> monthlyEvapotranspirations;
		private List<Float> soilMoistureSlants;
		private List<Float> soilTemperatureCalendar;
		private List<Integer> soilMoistureCalendar;
		private SoilMoistureRegimeSubdivision moistureRegimeSubdivision;
		private SoilTemperatureRegime temperatureRegime;
		private int totalDryDays;
		private int totalMediumDays;
		private int totalMoistDays;
		private int totalDryDaysAbove5;
		private int totalMediumDaysAbove5;
		private int totalMoistDaysAbove5;
		private int mostConsecutiveMoistDays;
		private int mostConsecutiveMoistDaysAbove8;
		private int consecutiveDryDaysAfterSummerSolstice;
		private int consecutiveMoistDaysAfterWinterSolstice;
		
		/**
		 * Constructor, only called from within the NSM native component.
		 */
		public DataSet() {
			super();
		}
	
		/**
		 * @return The consecutiveDryDaysAfterSummerSolstice.
		 */
		public int getConsecutiveDryDaysAfterSummerSolstice() {
			return consecutiveDryDaysAfterSummerSolstice;
		}
	
		/**
		 * @param consecutiveDryDaysAfterSummerSolstice The consecutiveDryDaysAfterSummerSolstice to set.
		 */
		public void setConsecutiveDryDaysAfterSummerSolstice(int consecutiveDryDaysAfterSummerSolstice) {
			this.consecutiveDryDaysAfterSummerSolstice = consecutiveDryDaysAfterSummerSolstice;
		}
	
		/**
		 * @return The consecutiveMoistDaysAfterWinterSolstice.
		 */
		public int getConsecutiveMoistDaysAfterWinterSolstice() {
			return consecutiveMoistDaysAfterWinterSolstice;
		}
	
		/**
		 * @param consecutiveMoistDaysAfterWinterSolstice The consecutiveMoistDaysAfterWinterSolstice to set.
		 */
		public void setConsecutiveMoistDaysAfterWinterSolstice(int consecutiveMoistDaysAfterWinterSolstice) {
			this.consecutiveMoistDaysAfterWinterSolstice = consecutiveMoistDaysAfterWinterSolstice;
		}
	
		/**
		 * @return The moistureRegimeSubdivision.
		 */
		public SoilMoistureRegimeSubdivision getMoistureRegimeSubdivision() {
			return moistureRegimeSubdivision;
		}
	
		/**
		 * @param moistureRegimeSubdivision The moistureRegimeSubdivision to set.
		 */
		public void setMoistureRegimeSubdivision(SoilMoistureRegimeSubdivision moistureRegimeSubdivision) {
			this.moistureRegimeSubdivision = moistureRegimeSubdivision;
		}
	
		/**
		 * @return The monthlyEvapotranspirations.
		 */
		public List<Float> getMonthlyEvapotranspirations() {
			return monthlyEvapotranspirations;
		}
	
		/**
		 * @param monthlyEvapotranspirations The monthlyEvapotranspirations to set.
		 */
		public void setMonthlyEvapotranspirations(List<Float> monthlyEvapotranspirations) {
			this.monthlyEvapotranspirations = monthlyEvapotranspirations;
		}
	
		/**
		 * @return The monthlyPrecipitations.
		 */
		public List<Float> getMonthlyPrecipitations() {
			return monthlyPrecipitations;
		}
	
		/**
		 * @param monthlyPrecipitations The monthlyPrecipitations to set.
		 */
		public void setMonthlyPrecipitations(List<Float> monthlyPrecipitations) {
			this.monthlyPrecipitations = monthlyPrecipitations;
		}
	
		/**
		 * @return The monthlyTemperatures.
		 */
		public List<Float> getMonthlyTemperatures() {
			return monthlyTemperatures;
		}
	
		/**
		 * @param monthlyTemperatures The monthlyTemperatures to set.
		 */
		public void setMonthlyTemperatures(List<Float> monthlyTemperatures) {
			this.monthlyTemperatures = monthlyTemperatures;
		}
	
		/**
		 * @return The mostConsecutiveMoistDays.
		 */
		public int getMostConsecutiveMoistDays() {
			return mostConsecutiveMoistDays;
		}
	
		/**
		 * @param mostConsecutiveMoistDays The mostConsecutiveMoistDays to set.
		 */
		public void setMostConsecutiveMoistDays(int mostConsecutiveMoistDays) {
			this.mostConsecutiveMoistDays = mostConsecutiveMoistDays;
		}
	
		/**
		 * @return The mostConsecutiveMoistDaysAbove8.
		 */
		public int getMostConsecutiveMoistDaysAbove8() {
			return mostConsecutiveMoistDaysAbove8;
		}
	
		/**
		 * @param mostConsecutiveMoistDaysAbove8 The mostConsecutiveMoistDaysAbove8 to set.
		 */
		public void setMostConsecutiveMoistDaysAbove8(int mostConsecutiveMoistDaysAbove8) {
			this.mostConsecutiveMoistDaysAbove8 = mostConsecutiveMoistDaysAbove8;
		}
	
		/**
		 * @return The soilMoistureCalendar.
		 */
		public List<Integer> getSoilMoistureCalendar() {
			return soilMoistureCalendar;
		}
	
		/**
		 * @param soilMoistureCalendar The soilMoistureCalendar to set.
		 */
		public void setSoilMoistureCalendar(List<Integer> soilMoistureCalendar) {
			this.soilMoistureCalendar = soilMoistureCalendar;
		}
	
		/**
		 * @return The soilMoistureSlants.
		 */
		public List<Float> getSoilMoistureSlants() {
			return soilMoistureSlants;
		}
	
		/**
		 * @param soilMoistureSlants The soilMoistureSlants to set.
		 */
		public void setSoilMoistureSlants(List<Float> soilMoistureSlants) {
			this.soilMoistureSlants = soilMoistureSlants;
		}
	
		/**
		 * @return The soilTemperatureCalendar.
		 */
		public List<Float> getSoilTemperatureCalendar() {
			return soilTemperatureCalendar;
		}
	
		/**
		 * @param soilTemperatureCalendar The soilTemperatureCalendar to set.
		 */
		public void setSoilTemperatureCalendar(List<Float> soilTemperatureCalendar) {
			this.soilTemperatureCalendar = soilTemperatureCalendar;
		}
	
		/**
		 * @return The temperatureRegime.
		 */
		public SoilTemperatureRegime getTemperatureRegime() {
			return temperatureRegime;
		}
	
		/**
		 * @param temperatureRegime The temperatureRegime to set.
		 */
		public void setTemperatureRegime(SoilTemperatureRegime temperatureRegime) {
			this.temperatureRegime = temperatureRegime;
		}
	
		/**
		 * @return The totalDryDays.
		 */
		public int getTotalDryDays() {
			return totalDryDays;
		}
	
		/**
		 * @param totalDryDays The totalDryDays to set.
		 */
		public void setTotalDryDays(int totalDryDays) {
			this.totalDryDays = totalDryDays;
		}
	
		/**
		 * @return The totalDryDaysAbove5.
		 */
		public int getTotalDryDaysAbove5() {
			return totalDryDaysAbove5;
		}
	
		/**
		 * @param totalDryDaysAbove5 The totalDryDaysAbove5 to set.
		 */
		public void setTotalDryDaysAbove5(int totalDryDaysAbove5) {
			this.totalDryDaysAbove5 = totalDryDaysAbove5;
		}
	
		/**
		 * @return The totalMediumDays.
		 */
		public int getTotalMediumDays() {
			return totalMediumDays;
		}
	
		/**
		 * @param totalMediumDays The totalMediumDays to set.
		 */
		public void setTotalMediumDays(int totalMediumDays) {
			this.totalMediumDays = totalMediumDays;
		}
	
		/**
		 * @return The totalMediumDaysAbove5.
		 */
		public int getTotalMediumDaysAbove5() {
			return totalMediumDaysAbove5;
		}
	
		/**
		 * @param totalMediumDaysAbove5 The totalMediumDaysAbove5 to set.
		 */
		public void setTotalMediumDaysAbove5(int totalMediumDaysAbove5) {
			this.totalMediumDaysAbove5 = totalMediumDaysAbove5;
		}
	
		/**
		 * @return The totalMoistDays.
		 */
		public int getTotalMoistDays() {
			return totalMoistDays;
		}
	
		/**
		 * @param totalMoistDays The totalMoistDays to set.
		 */
		public void setTotalMoistDays(int totalMoistDays) {
			this.totalMoistDays = totalMoistDays;
		}
	
		/**
		 * @return The totalMoistDaysAbove5.
		 */
		public int getTotalMoistDaysAbove5() {
			return totalMoistDaysAbove5;
		}
	
		/**
		 * @param totalMoistDaysAbove5 The totalMoistDaysAbove5 to set.
		 */
		public void setTotalMoistDaysAbove5(int totalMoistDaysAbove5) {
			this.totalMoistDaysAbove5 = totalMoistDaysAbove5;
		}
	}
	
}
