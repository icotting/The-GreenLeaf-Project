/* Created On: Sep 8, 2005 */
package edu.unl.act.rma.firm.core;

import java.util.HashMap;
import java.util.Iterator;

import org.joda.time.DateTime;


/**
 * @author Ian Cottingham
 *
 */
public abstract class PeriodOrderedDataBuilder extends DTOBase implements Iterable<String> {

	private static final long serialVersionUID = 1L;

	private static Logger LOG = LogManager.getLogger(Loggers.SYSTEM_LOG, PeriodOrderedDataBuilder.class);
	
	protected float[][] stationResults;
	protected HashMap<String, float[][]> results;
	protected boolean station;
	protected String currentStation;
	protected int xpos;
	protected int ypos;
	protected int addedValues;
	protected int periodStart;
	protected int periodEnd;
	
	protected CalendarPeriod periodType;
	protected int periodLength;
	protected DataType dataType;
	protected DateTime begin;
	protected DateTime end;
	protected int expectedValues;
	protected boolean beginInclusive;
	protected boolean endInclusive;
	
	private float rounder;
	private boolean valueCheck;
	
	public abstract CalendarDataCollection returnCollection();
	protected abstract void buildResultStorage() throws InvalidArgumentException, InvalidStateException;
	
	public PeriodOrderedDataBuilder(DateTime begin, DateTime end, CalendarPeriod period, DataType type) { 
		this.beginInclusive = true;
		this.endInclusive = true;
		this.valueCheck = false;
		init(begin, end, period, type);
	}
	
	public PeriodOrderedDataBuilder(DateTime begin, DateTime end, CalendarPeriod period, DataType type, boolean beginInclusive, boolean endInclusive) { 
		this.beginInclusive = beginInclusive;
		this.endInclusive = endInclusive;
		this.valueCheck = false;
		init(begin, end, period, type);
	}
	
	protected void init(DateTime begin, DateTime end, CalendarPeriod period, DataType type) {
		this.begin = begin;
		this.end = end;
		
		this.periodType = period;
		this.periodLength = this.periodType.getLength();
		if ( !endInclusive ) { 
			periodLength--;
		}
		
		if ( !beginInclusive ) { 
			periodLength--;
		}
		
		this.dataType = type;
		results = new HashMap<String, float[][]>();
		
		try {
			buildResultStorage();
		} catch ( Exception e ) { 
			// nothing should be done here since this is only called to compute the number of expected values
		}
		rounder = (float)Math.pow(10d, (float)type.getRoundingPlaces());
	}
	
	public void writeStation() throws InvalidStateException {	
		if ( addedValues != expectedValues ) {
			try {
				LOG.warn("station " + currentStation + " incorrectly filled, expected "+expectedValues+" but found "+addedValues);
				LOG.info("station "+currentStation+" will be filled with error values");
				populateError();
			} catch ( NullPointerException npe ) { 
				System.err.println("station " + currentStation + " incorrectly filled, expected "+expectedValues+" but found "+addedValues);
				System.err.println("station "+currentStation+" will be filled with error values");
				populateError();
			}
		}
		
		this.valueCheck = false;
		if ( periodType != CalendarPeriod.ANNUALLY ) {
			for ( int i=0; i<=(periodLength - periodEnd)-1; i++ )
				add(DataType.OUTSIDE_OF_REQUEST_RANGE);
		}
		
		results.put(currentStation, stationResults);
		station = false;
		
	}
	
	private void populateError() throws InvalidStateException { 
		station = false;
		try {
			openStation(currentStation);
		} catch ( InvalidArgumentException ile ) { 
			throw new RuntimeException(ile.getMessage());
		}
		
		while ( addedValues < expectedValues ) {
			if ( xpos >= periodLength ) {
				ypos++; xpos = 0;
			}
				
			stationResults[ypos][xpos++] = DataType.ERROR_RESULT;
			addedValues++;
		}

	}
	
	public void disposeStation() {
		station = false;
	}
	
	public void add(float value) throws InvalidStateException {
		if ( !station )
			throw new InvalidStateException("a station must be created before writing data");
		
		if ( valueCheck && addedValues > expectedValues ) {
			StringBuffer sb = new StringBuffer();
			sb.append("Data is being added to station ");
			sb.append(currentStation);
			sb.append(" in excess of the ");
			sb.append(expectedValues);
			sb.append(" values expected; this additional data will be truncated.");
			
			LOG.error(sb.toString());
			return;
		}
		
		if ( xpos >= periodLength ) {
			ypos++; xpos = 0;
		}
		
		stationResults[ypos][xpos++] = Math.round(value * rounder)/rounder;
		addedValues++;
	}
	
	public int getExpectedValues() { 
		return expectedValues;
	}
	
	public void openStation(String stationId) throws InvalidStateException, InvalidArgumentException {
		if ( station )
			throw new InvalidStateException("the previous station must be written or disposed before opening a new station");
			
		station = true;
		currentStation = stationId;
		xpos = 0;
		ypos = 0;
		currentStation = stationId;

		buildResultStorage();
		for ( int i=0; i<periodStart-1; i++ )
			add(DataType.OUTSIDE_OF_REQUEST_RANGE);
		
		addedValues = 0;
		this.valueCheck = true;
	}
	
	public void importStation(String stationId, float[][] dataMatrix) throws InvalidStateException, InvalidArgumentException { 
		if ( station)
			throw new InvalidStateException("the previous station must be written or disposed before opening a new station");
			
		assert(dataMatrix.length == (end.getYear() - begin.getYear())+1);
		assert(dataMatrix[0].length == periodLength);
		
		if ( results.containsKey(stationId) )
			throw new InvalidStateException("cannot import station "+stationId+", already contained in results");
		
		results.put(stationId, dataMatrix);
		station = false;
		
	}
	
	public float[][] exportStation(String stationId) { 		
		return results.get(stationId);
	}
	
	public YearDataBuilder cloneParameters() { 
		return new YearDataBuilder(begin, end, periodType, dataType, beginInclusive, endInclusive);
	}
	
	public Iterator<String> iterator() {
		return results.keySet().iterator();
	}
	
	public boolean isLimitReached() { 
		return (addedValues == expectedValues);
	}
	
	public int getPeriodStartIndex() { 
		return this.periodStart;
	}
	
	/**
	 * 
	 * @return the ending period.  NOTE: this is the ending period over the entire data range.
	 */
	public int getPeriodEndIndex() { 
		return getExpectedValues()+getPeriodStartIndex()-1;
	}
	
	/* the following two methods will give the actual period start and end values, 
	 * the previous methods are designed to be used to get the index points into the 
	 * data matrix. 
	 */
	public int getPeriodStartValue() { // this getter is provided redundantly to avoid confusion as to what is being done with the values
		return this.periodStart;
	}
	
	public int getPeriodEndValue() { 
		return this.periodEnd;
	}
	
	@Override
	public String toString() {
		if (currentStation == null && results != null) {
			return results.size() + " stations";
		} else if (currentStation != null) {
			return currentStation + " (" + addedValues + " of " + expectedValues + ")";
		}
		return super.toString();
	}
	public DateTime getBegin() {
		return begin;
	}
	public DateTime getEnd() {
		return end;
	}
	
	
}
