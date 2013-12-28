/* Created On: Sep 8, 2005 */
package edu.unl.act.rma.firm.climate.component;

import java.rmi.RemoteException;

import javax.ejb.Remote;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.MetaDataCollection;



/**
 * The system component used to query ocean data from the FIRM oceanicDataSource.
 * 
 * @author Ben Kutsch
 */
@Remote
public interface OceanicDataQuery {

	/**
	 * A method to query oceanic data constrained by a temporal period and an oceanic variable.  
	 * All values are returned in a monthly format
	 * 
	 * @param variableID
	 * @param firstDate
	 * @param lastDate
	 * @return A CalendarDataCollection containing 12 values per year per station within a temporal period
	 * @throws RemoteException
	 * @throws InvalidArgumentException
	 */
	public CalendarDataCollection getOceanicMonthlyValues(DataType variableID, DateTime firstDate, DateTime lastDate) throws RemoteException, InvalidArgumentException;
	
	/**
	 * A method to query oceanic data constrained by a temporal period and an oceanic varibale
	 * All values are returned in a weekly format
	 * 
	 * @param variableID
	 * @param firstDate
	 * @param lastDate
	 * @return
	 * @throws RemoteException
	 * @throws InvalidArgumentException
	 */
	public CalendarDataCollection getOceanicWeeklyValues(DataType variableID, DateTime firstDate, DateTime lastDate) throws RemoteException, InvalidArgumentException; 
	
	
	/**
	 * Queries all metadata fields for an oceanic variable
	 * 
	 * @param variableID
	 * @return a MetaDataCollection containing all meta data values for the variable
	 * @throws RemoteException
	 */
	public MetaDataCollection getOceanicMetaData(DataType variableID) throws RemoteException, InvalidArgumentException;

	
	/**
	 * Queries the start date for a given variable type
	 * 
	 * @param variableID
	 * @return a MetaDataCollection containing all meta data values for the variable
	 * @throws RemoteException
	 */
	public DateTime getOceanicStartDate(DataType variableID) throws RemoteException, InvalidArgumentException;

	/**
	 * Queries the end date for a given variable type
	 * 
	 * @param variableID
	 * @return a MetaDataCollection containing all meta data values for the variable
	 * @throws RemoteException
	 */
	public DateTime getOceanicEndDate(DataType variableID) throws RemoteException, InvalidArgumentException;
}