/* Created On: Sep 12, 2005 */
package edu.unl.act.rma.firm.drought.component;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

import org.joda.time.DateTime;

/**
 * @author Ian Cottingham
 *
 */
@Remote
public interface SoilsDataQuery {

	/**
	 * Returns the available water holding capacity for a station
	 * sense.
	 * 
	 * @param stationIds
	 * @return a mapping of station IDs to AWC values
	 * @throws RemoteException
	 */
	public Map<String, Float> getWaterHoldingCapacity(List<String> stationIds) throws RemoteException;
	
	
	/**
	 * Returns the current amount of moisture in the soil for a given data
	 * 
	 * @param stationIds
	 * @return a mapping of station IDs to the CSM values
	 * @throws RemoteException
	 */
	public Map<String, Float> getCurrentSoilMoisture(List<String> stationIds, DateTime date) throws RemoteException;
	
}
