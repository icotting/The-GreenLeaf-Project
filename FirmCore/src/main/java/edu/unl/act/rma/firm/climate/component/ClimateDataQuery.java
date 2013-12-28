/* Created On: Sep 8, 2005 */
package edu.unl.act.rma.firm.climate.component;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.Remote;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.climate.TemperatureDayTypes;
import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.InvalidStateException;


/**
 * The system component used to query climate data from the FIRM climaticDataSource.
 * 
 * @author Ian Cottingham
 */
@Remote
public interface ClimateDataQuery {

	
	public CalendarDataCollection getPeriodData(List<String> stationIds, DateTime firstDate, DateTime lastDate, DataType type, CalendarPeriod period) throws RemoteException, InvalidArgumentException;
	
	public CalendarDataCollection getAvailableData(List<String> stationIds, DataType type, CalendarPeriod period) throws RemoteException, InvalidArgumentException;
	
	public CalendarDataCollection getHistoricalAverageData(List<String> stationIds, DataType type, CalendarPeriod period) throws RemoteException, InvalidArgumentException;
	
	public CalendarDataCollection getDataNormals(List<String> stationIds, int startYear, int endYear, DataType type, CalendarPeriod period) throws RemoteException, InvalidArgumentException;
	
	public CalendarDataCollection getGrowingDegreeDayNormals(List<String> stationIds, CalendarPeriod summary, int startYear, int endYear, float minDailyTemp, float maxDailyTemp) throws RemoteException, InvalidArgumentException, InvalidStateException;
	
	public CalendarDataCollection getFrostFreePeriodNormals(List<String> stationIds, int startYear, int endYear) throws RemoteException, InvalidArgumentException;
	
	/**
	 * 
	 * @param stationIds
	 * @param type
	 * @param begin
	 * @param end
	 * 
	 * @return GDD values summarized to a CalendarPeriod for some years of data
	 * @throws RemoteException
	 */
	public CalendarDataCollection getGrowingDegreeDays(List<String> stationIds, CalendarPeriod summary, DateTime firstDate, DateTime lastDate, float minDailyTemp, float maxDailyTemp) throws RemoteException, InvalidArgumentException, InvalidStateException;
	
	/**
	 * 
	 * @param stationIds
	 * @param firstYear
	 * @param lastYear
	 * @return an annually summarized CalendarDataCollection
	 * @throws RemoteException
	 */
	public CalendarDataCollection getFrostFreePeriod(List<String> stationIds, int firstYear, int lastYear) throws RemoteException, InvalidArgumentException;
	
	
	/**
	 * 
	 * @param stationIds
	 * @param firstDate
	 * @param lastDate
	 * @param type
	 * @return
	 * @throws RemoteException
	 */
	public CalendarDataCollection getTemperatureDays(List<String> stationIds, DateTime firstDate, DateTime lastDate,CalendarPeriod summary, TemperatureDayTypes type,int reftemp) throws RemoteException, InvalidArgumentException, InvalidStateException;
	
	/**
	 * @param stationIds
	 * @param firstYear
	 * @param lastYear
	 * @param type
	 * @return
	 * @throws RemoteException
	 * @throws InvalidStateException
	 */
	public CalendarDataCollection getAnnualExtremeTemp(List<String> stationIds,int firstYear, int lastYear ,DataType type) throws RemoteException, InvalidStateException, InvalidArgumentException; 
	
	
	/**
	 * @param stationIds
	 * @param type
	 * @param summary
	 * @param start
	 * @param end
	 * @return
	 * @throws RemoteException
	 * @throws InvalidStateException
	 */
	public CalendarDataCollection getPercentageNormal(List<String> stationIds,DataType type,CalendarPeriod summary,DateTime start,DateTime end) throws RemoteException,InvalidStateException, InvalidArgumentException;
	
}
