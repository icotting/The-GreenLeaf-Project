/* Created On Nov 30, 2006 */
package edu.unl.act.rma.firm.core.component;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import edu.unl.act.rma.firm.core.spatial.BoundingBox;
import edu.unl.act.rma.firm.core.spatial.Region;
import edu.unl.act.rma.firm.core.spatial.SpatialReference;
import edu.unl.act.rma.firm.core.spatial.USCity;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;

/**
 * @author Ian Cottingham
 *
 */
@Remote
public interface SpatialQuery {

	public Set<USCounty> getCountiesByState(USState state) throws RemoteException;
	public Set<USCounty> searchCounties(String countyName) throws RemoteException;
	public Set<USCounty> getCountiesByFips(List<String> fips) throws RemoteException;
	public Set<USCounty> searchCountiesByState(String countyName, USState state) throws RemoteException;
	
	public Set<USCounty> getCountiesByRegion(BoundingBox region) throws RemoteException;
	
	public Set<USState> getStatesByRegion(BoundingBox region) throws RemoteException;
	
	public USCounty getCountyById(long id) throws RemoteException;
	public USCounty getCountyByFips(String fips) throws RemoteException;
	
	public USCity getCityById(long id) throws RemoteException;
	public USCity getCityForName(String name, USState state) throws RemoteException;
	public List<USCity> getCitiesForName(String name) throws RemoteException;
	
	public USCounty getCountyForName(String name, USState state) throws RemoteException;
	public List<USCounty> getCountiesForName(String name) throws RemoteException;
	
	public Set<USCity> getCitiesByState(USState state) throws RemoteException;
	public Set<USCity> searchCitiesByCounty(String cityName, USCounty county) throws RemoteException;
	public USCity getCityByZip(String zipCode) throws RemoteException;
	
	public List<SpatialReference> loadCompleteReferences(List<SpatialReference> references) throws RemoteException;

	public void removeSpatialReference(SpatialReference reference) throws RemoteException;
	
	public Region getRegionForState(USState state) throws RemoteException;
	public Region getRegionForCounty(USCounty county) throws RemoteException;
	
	public List<Region> getRegionsForStates(List<USState> states) throws RemoteException;
	public List<Region> getRegionsForCounties(List<USCounty> counties) throws RemoteException;
}
