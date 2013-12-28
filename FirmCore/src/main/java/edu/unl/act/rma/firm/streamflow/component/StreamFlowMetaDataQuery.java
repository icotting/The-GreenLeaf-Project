package edu.unl.act.rma.firm.streamflow.component;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

import org.joda.time.Interval;

import edu.unl.act.rma.firm.climate.VariableFilter;
import edu.unl.act.rma.firm.climate.VariableMetaData;
import edu.unl.act.rma.firm.climate.component.ClimateMetaDataQuery;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.MetaDataCollection;
import edu.unl.act.rma.firm.core.StationMetaDataType;
import edu.unl.act.rma.firm.core.StationSearchTerms;
import edu.unl.act.rma.firm.core.TemporalPeriod;

@Remote
public interface StreamFlowMetaDataQuery {

	/**
	 * Generates a list of all weather stations matching the given search terms
	 * 
	 * @param terms
	 * @return
	 * @throws RemoteException
	 */
	public MetaDataCollection<StationMetaDataType> findStations(StationSearchTerms terms) throws RemoteException;
	
	/**
	 * Queries a specific metadata field for a list of weather station IDs
	 * 
	 * @param stations
	 * @param field
	 * @return a MetaDataCollection containing a single meta data value per station
	 * @throws RemoteException
	 */
	public MetaDataCollection<StationMetaDataType> getMetaData(List<String> stations, StationMetaDataType field, CalendarPeriod period) throws RemoteException, InvalidArgumentException; 
	
	/**
	 * Queries all metadata fields for a list of weather station IDs
	 * 
	 * @param stations
	 * @return a MetaDataCollection containing all meta data values for each station
	 * @throws RemoteException
	 */
	public MetaDataCollection<StationMetaDataType> getAllMetaData(List<String> stations, CalendarPeriod period) throws RemoteException, InvalidArgumentException;
	
	/**
	 * Computes the longest temporal period encompassing the period of record for all stations in the argument 
	 * list
	 * 
	 * @param stations
	 * @return a temporal period beginning on the earilest possible station starting date and ending on the latest possible
	 * station ending date
	 * @throws RemoteException
	 */
	public TemporalPeriod getLongestPeriod(List<String> stations, CalendarPeriod period, DataType type) throws RemoteException, InvalidArgumentException;
	
	/**
	 * Queries the latest ending date for all data for the given period.
	 * 
	 * @param period The period to query by, only DAILY, WEEKLY, or MONTHLY are valid.
	 * @return
	 * @throws RemoteException
	 * @throws SQLException
	 */
//	public DateTime getEndingDate(CalendarPeriod period) throws InvalidArgumentException, RemoteException;
	
	/**
	 * Computes the longest temporal period encompassing the period of record of a specific variable for all stations in the argument 
	 * list
	 * 
	 * @param stations
	 * @param variable
	 * @return
	 * @throws RemoteException
	 */
	public Map<String, Map<DataType, VariableMetaData>> getVariableMetaData(List<String> stations) throws RemoteException;
	
	/**
	 * Filters a list of stations based on filter criteria
	 * 
	 * @param stations the list of stations to filter
	 * @param filters the filters to apply
	 * @param overallPeriod the period over which all variables must have data
	 * @param overallTolerance the tolerance or missing data which all variables must meet
	 * @return a list of stations meeting the criteria 
	 * @throws RemoteException
	 */
	public List<String> filterStations(List<String> stations, List<VariableFilter> filters, TemporalPeriod overallPeriod, float overallTolerance, boolean actualFilter) throws RemoteException;
	
	/** Returns a list of Intervals for which the station has no data (i.e., -99 value)
	 * 
	 * @param stations the list of stations for which to find gaps
	 * @param type data type of interest
	 * @return list of Intervals for which there is no data
	 * @throws RemoteException
	 */
	public Map <String, List<Interval>> getIntervalGaps(List <String> stations, DataType type) throws RemoteException;
	
	
	/**
	 * Validates a station ID as belonging to the FIRM system station set. 
	 * 
	 * @param stationId a FIRM station ID
	 * @return true if the station ID matches a station in the system, false otherwise
	 * 
	 * @throws RemoteException
	 */
	public boolean isValidStation(String stationId) throws RemoteException;

	/**
	 * Similar to {@link ClimateMetaDataQuery#isValidStation(String)} this method will remove station IDs from a list 
	 * which are not valid FIRM station IDs. 
	 * 
	 * @param stations a list of station IDs.
	 * 
	 * @return a list of station IDs contianing only valid IDs. 
	 * 
	 * @throws RemoteException
	 */
	public List<String> removeInvalidStations(List<String> stations) throws RemoteException;

}
