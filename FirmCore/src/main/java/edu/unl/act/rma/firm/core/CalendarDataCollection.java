/* Created On: Sep 8, 2005 */
package edu.unl.act.rma.firm.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.core.spatial.Site;
import edu.unl.act.rma.firm.core.spatial.SiteList;


/**
 * @author Ian Cottingham
 *
 */
public final class CalendarDataCollection extends DTOBase implements Iterable<String> {

	private static final long serialVersionUID = 2L;
	
	private static Logger LOG = LogManager.getLogger(Loggers.SYSTEM_LOG, CalendarDataCollection.class);
	public static String AREA_ID = "area-data";
	
	private DateTime begin;
	private DateTime end; 
	private DateTime fixedPoint;
	private DataClass dataClass;

	private final CalendarPeriod collectionPeriodType;
	private DataType dataType;	
	
	private List<String> sortedList;
	
	public HashMap<String, float[][]> result;
	
	private UnitType unitType;
	
	protected CalendarDataCollection(DateTime begin, DateTime end, CalendarPeriod dataPeriodType, CalendarPeriod collectionPeriodType, DataType type, HashMap<String, float[][]> result) { 
		this.begin = begin;
		this.end = end;
		this.dataType = type;
		this.collectionPeriodType = collectionPeriodType;
		this.result = result;
		this.unitType = UnitType.ENGLISH;
	}
	
	public UnitType getUnitType() { 
		return this.unitType;
	}
	
	public DateTime getBegin() {
		return begin;
	}
	
	public void setBegin(DateTime begin) {
		this.begin = begin;
	}

	public CalendarPeriod getCollectionPeriodType() {
		return collectionPeriodType;
	}

	public DataType getDataType() {
		return dataType;
	}
	
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public DateTime getEnd() {
		return end;
	}
	
	public void setEnd(DateTime end) {
		this.end = end;
	}
	
	public boolean containsStation(String id) { 
		return result.keySet().contains(id);
	}
	
	public float[][] getDataMatrix(String stationId) { 
		if ( dataClass == null ) {
			return result.get(stationId);
		} else { 
			float[][] data = result.get(stationId);
			int len = data.length;
			float[][] ret = new float[len][];
			for ( int i=0; i<len; i++ ) { 
				ret[i] = applyClassification(data[i]);
			}
			
			return ret;
		}
	}
	
	public Iterable<float[]> getStationData(String stationId) { 
		final float[][] data = result.get(stationId);
		
		/* an invalid ID was provided so an empty iterator will be returned */
		if ( data == null ) {
			LOG.warn(stationId+" is an invalid station ID: an empty iterator is being returned");
			return new Iterable<float[]>() { 
				public Iterator<float[]> iterator() { 
					return new Iterator<float[]>() {						
						public boolean hasNext() {
							return false;
						}

						public float[] next() {
							return new float[0];
						}

						public void remove() { } 
					};
				}
			};	
		} else {
			return new Iterable<float[]>() { 
				public Iterator<float[]> iterator() { 
					return new Iterator<float[]>() {
						private int _counter; 
						
						public boolean hasNext() {
							return (_counter < data.length) ? true : false;
						}

						public float[] next() {
							return applyClassification(data[_counter++]);
						}

						public void remove() { } 
					};
				}
			};	
		}
	}
	
	private float[] applyClassification(float[] data) { 
		if ( dataClass == null ) { 
			return data;
		} else { 
			int len = data.length;
			float[] ret = new float[len];
			for ( int i=0; i<len; i++ ) { 
				ret[i] = dataClass.classifyValue(data[i]);
			}
			
			return ret;
		}
	}
	
	public void setFixedPoint(DateTime fixedPoint) { 
		
		DateTime checkStart;
		DateTime checkEnd; 
		
		switch ( this.collectionPeriodType ) {
		case DAILY:
			checkStart = begin.minusDays(1);
			checkEnd = end.plusDays(1);
			break;
		case WEEKLY:
			checkStart = begin.minusWeeks(1);
			checkEnd = end.plusWeeks(1);
			break;
		case MONTHLY:
			checkStart = begin.minusMonths(1);
			checkEnd = end.plusMonths(1);
			break;
		default:
			checkStart = begin.minusYears(1);
			checkEnd = end.plusYears(1);
		}
		
		if ( !(fixedPoint.isAfter(checkStart) && fixedPoint.isBefore(checkEnd)) ) {
			throw new RuntimeException("the fixed point date must fall between "+begin.toString()+" and "+end.toString());
		}
		
		this.fixedPoint = fixedPoint;
	}
	
	public float getFixedDateStationData(String stationId) { 
		return getStationDataForDate(stationId, fixedPoint);
	}
	
	public float getStationDataForPeriodPosition(String stationId, int yearNum, int periodNum) { 
		return ( dataClass != null ) ? dataClass.classifyValue(result.get(stationId)[yearNum-1][periodNum-1]) : result.get(stationId)[yearNum-1][periodNum-1];
	}
	
	public float getStationDataForDate(String stationId, DateTime date) { 
		if ( date == null ) { 
			throw new RuntimeException("a fixed point has not been set");
		}
		
		try {
		int years = date.getYear() - begin.getYear();
		
		int pos = 0;
		if ( collectionPeriodType.equals(CalendarPeriod.WEEKLY) ) {			
			// deal with week offsets
			pos = date.getWeekOfWeekyear();
			int month = date.getMonthOfYear();
			
			if ( pos == 53 ) {
				if ( month == 1 ) {
					pos = 1;
				} else {
					pos = 52; 
				}
			} else if ( pos == 52 && month == 1 ) {
				pos = 1;
			} else if ( pos == 1 && month == 12 ) {
				pos = 52;
				years--;
			}

			//adjust pos for array position
			pos--;
			
		} else if ( collectionPeriodType.equals(CalendarPeriod.MONTHLY) ) {
			pos = date.getMonthOfYear() - 1;
		} else if ( collectionPeriodType.equals(CalendarPeriod.DAILY) ) {
			pos = date.getDayOfYear() - 1;
		} else {			
			throw new RuntimeException("Invalid period type for date extraction");
		}
		

		return ( dataClass != null ) ? dataClass.classifyValue(result.get(stationId)[years][pos]) : result.get(stationId)[years][pos];
	
		} catch ( Exception e ) { 
			LOG.error("An error occured gathering fixed point data", e);
			RuntimeException re = new RuntimeException("Could not get fixed point data for station "+stationId+" at date "+date);
			re.initCause(e);
			throw re;
		}
	} 
	
	/**
	 * Will attach a sorted list of stations to the object to allow for traversal with the iterator
	 * in sorted order.  The order of the sorted list is independant of any logic contained in this
	 * object. (i.e. it was sorted in some other way wither with a Collections object, SQL query
	 * etc.)
	 * 
	 * This method will ignore stations from the sortedList which are not contained in this object
	 * 
	 * @param sortedList
	 */
	public void attachSortedStationList(List<String> sortedList) { 
		ArrayList<String> stations = new ArrayList<String>();
		
		/* ensure that only data available in this object is included in the "sorted" list */
		for ( String str : sortedList ) { 
			if ( result.get(str) != null ) { 
				stations.add(str);
			}
		}
		
		this.sortedList = stations;
	}

	public Iterator<String> iterator() {
		return (sortedList == null ) ? result.keySet().iterator() : sortedList.iterator();
	}
	
	@Override
	public String toString() {
		return (result == null ? 0 : result.size()) + " stations";
	}
	
	/**
	 * Attach a filter to provide a value normalization mechanism. 
	 * 
	 * @param filter - the value filter
	 */
	public void attachClass(DataClass dataClass) { 
		this.dataClass = dataClass;
	}
	
	public void removeClass() { 
		this.dataClass = null;
	}
		
	public void convertToEnglish() { 
		if ( unitType == UnitType.ENGLISH )  { 
			return;
		}
		
		// only convert the following data types
		switch (dataType) {
		case PRECIP:
			break;
		case NORMAL_TEMP:
			break;
		case HIGH_TEMP:
			break;
		case LOW_TEMP:
			break;
		case AWC:
			break;
		default: return;
		}
		
		for ( String str : result.keySet() ) {
			float[][] data = result.get(str);
			int len = data.length;
			
			for ( int i=0; i<len; i++ ) {
				switch (dataType) {
				case PRECIP:
					data[i] = depthToEnglish(data[i]);
					break;
				case NORMAL_TEMP:
					data[i] = degreesToEnglish(data[i]);
					break;
				case HIGH_TEMP:
					data[i] = degreesToEnglish(data[i]);
					break;
				case LOW_TEMP:
					data[i] = degreesToEnglish(data[i]);
					break;
				case AWC:
					data[i] = depthToEnglish(data[i]);
					break;
				}
			}
		}
		
		this.unitType = UnitType.ENGLISH;
	}
	
	public void convertToMetric() { 
		if ( unitType == UnitType.METRIC ) {
			return;
		}
		
		// only convert the following data types
		switch (dataType) {
		case PRECIP:
			break;
		case NORMAL_TEMP:
			break;
		case HIGH_TEMP:
			break;
		case LOW_TEMP:
			break;
		case AWC:
			break;
		default: return;
		}
		
		for ( String str : result.keySet() ) {
			float[][] data = result.get(str);
			int len = data.length;
			
			for ( int i=0; i<len; i++ ) {
				switch (dataType) {
				case PRECIP:
					data[i] = depthToMetric(data[i]);
					break;
				case NORMAL_TEMP:
					data[i] = degreesToMetric(data[i]);
					break;
				case HIGH_TEMP:
					data[i] = degreesToMetric(data[i]);
					break;
				case LOW_TEMP:
					data[i] = degreesToMetric(data[i]);
					break;
				case AWC:
					data[i] = depthToMetric(data[i]);
					break;
				}
			}
		}
		
		this.unitType = UnitType.METRIC;
	}
		
	public SiteList generateSiteList(DateTime fixedPoint, MetaDataCollection<StationMetaDataType> metaData, boolean checkStations) {
		
		if ( !(metaData.getTypes().contains(StationMetaDataType.LATITUDE) ) && 
				!(metaData.getTypes().contains(StationMetaDataType.LONGITUDE)) ) { 
			throw new RuntimeException("latitude and longitude must be contained in the meta data collection");
		}
		
		/* ensure that state is not changed unexpectedly */
		DateTime prev_point = null;
		if ( this.fixedPoint != null ) { 
			prev_point = this.fixedPoint;
		}
		
		setFixedPoint(fixedPoint);
		Map<String, Site> sites = new HashMap<String, Site>();
	
		Map<String, Object> lats = metaData.extractType(StationMetaDataType.LATITUDE);
		Map<String, Object> longs = metaData.extractType(StationMetaDataType.LONGITUDE);

		if ( lats == null ) { 
			lats = new HashMap<String, Object>();
		}
		
		if ( longs == null ) { 
			longs = new HashMap<String, Object>();
		}
		
		for ( String str : this ) { 
			Site site = new Site((Float)lats.get(str), (Float)longs.get(str), getFixedDateStationData(str));
			sites.put(str, site);
		}
		
		/* ensure that state is not changed unexpectedly */
		if ( prev_point != null ) { 
			this.fixedPoint = prev_point;
		}

		return new SiteList(metaData, sites, checkStations);
	}

	public CalendarDataCollection collectionAverage() { 
		YearDataBuilder ydb = new YearDataBuilder(this.begin, this.end, this.collectionPeriodType, this.dataType);

		DateTime pos = ydb.begin;
	
		try {
			ydb.openStation(AREA_ID);
			
			while ( ydb.addedValues != ydb.expectedValues ) {
				this.setFixedPoint(pos);
				float total = 0;
				int count = 0;
				float val = 0;
				
				for ( String str : this ) { 
					val = this.getFixedDateStationData(str);
					if ( val != DataType.MISSING && val != DataType.ERROR_RESULT && val != DataType.NONEXISTANT &&
							val != DataType.OUTSIDE_OF_RANGE && val != DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						total += val;
						count++;
					}
				}
				
				if ( count > 0 ) {
					ydb.add(total/count);
				} else { 
					ydb.add(DataType.MISSING);
				}
				
				switch ( collectionPeriodType ) {
					case DAILY:
						pos = pos.plusDays(1);
						break;
					case WEEKLY:
						pos = pos.plusWeeks(1);
						break;
					case MONTHLY:
						pos = pos.plusMonths(1);
						break;
					case ANNUALLY:
						pos = pos.plusYears(1);
						break;
					default:
						throw new RuntimeException("invalid period type");
				}
			}
			
		ydb.writeStation();
		} catch ( Exception e ) { 
			LOG.debug("Current: "+pos);
			LOG.debug("Start: "+ydb.begin);
			LOG.debug("End: "+ydb.end);
			LOG.debug("Added: "+ydb.addedValues);
			LOG.debug("Expected Values: "+ydb.expectedValues);
			LOG.error("an error occured writing the averages", e);
			RuntimeException re = new RuntimeException("the averages could not be added");
			re.initCause(e);
			throw re;
		}
		
		return ydb.returnCollection();
	}
	
	public CalendarDataCollection collectionSum() { 
		YearDataBuilder ydb = new YearDataBuilder(this.begin, this.end, this.collectionPeriodType, this.dataType);
		
		try {
			ydb.openStation(AREA_ID);
			DateTime pos = ydb.begin;
			
			while ( pos.isBefore(ydb.end) ) {
				this.setFixedPoint(pos);
				float total = 0;
				float val = 0;
				
				for ( String str : this ) { 
					val = this.getFixedDateStationData(str);
					if ( val != DataType.MISSING && val != DataType.ERROR_RESULT && val != DataType.NONEXISTANT &&
							val != DataType.OUTSIDE_OF_RANGE && val != DataType.OUTSIDE_OF_REQUEST_RANGE ) {
						total += val;
					}
				}
				
				if ( total > 0 ) {
					ydb.add(total);
				} else { 
					ydb.add(DataType.MISSING);
				}
				
				switch ( collectionPeriodType ) {
					case DAILY:
						pos = pos.plusDays(1);
						break;
					case WEEKLY:
						pos = pos.plusWeeks(1);
						break;
					case MONTHLY:
						pos = pos.plusMonths(1);
						break;
					case ANNUALLY:
						pos = pos.plusYears(1);
						break;
					default:
						throw new RuntimeException("invalid period type");
				}
			}
			
		ydb.writeStation();
		} catch ( Exception e ) { 
			LOG.error("an error occured writing the averages", e);
			RuntimeException re = new RuntimeException("the averages could not be added");
			re.initCause(e);
			throw re;
		}
		
		return ydb.returnCollection();
	}
	
	private float[] degreesToEnglish(float[] data) { 
		int len = data.length;
		for ( int i=0; i<len; i++ ) { 
			if ( data[i] != DataType.MISSING && data[i] != DataType.ERROR_RESULT && data[i] != DataType.OUTSIDE_OF_RANGE ) {
				data[i] = (9f/5f) * data[i] + 32f;
			}
		}
		
		return data;
	}
	
	private float[] degreesToMetric(float[] data) { 
		int len = data.length; 
		for ( int i=0; i<len; i++ ) { 
			if ( data[i] != DataType.MISSING && data[i] != DataType.ERROR_RESULT && data[i] != DataType.OUTSIDE_OF_RANGE ) {
				data[i] =  (5f/9f) * (data[i] - 32f);
			}
		}
		
		return data;
	}
	
	private float[] depthToEnglish(float[] data) { 
		int len = data.length; 
		for ( int i=0; i<len; i++ ) { 
			if ( data[i] != DataType.MISSING && data[i] != DataType.ERROR_RESULT && data[i] != DataType.OUTSIDE_OF_RANGE ) {
				data[i] = data[i] / 25.4f;
			}
		}
		
		return data;
	}
	
	private float[] depthToMetric(float[] data) { 
		int len = data.length; 
		for ( int i=0; i<len; i++ ) {
			if ( data[i] != DataType.MISSING && data[i] != DataType.ERROR_RESULT && data[i] != DataType.OUTSIDE_OF_RANGE ) {
				data[i] = data[i] * 25.4f;
			}			
		}
		
		return data;
	}
}
