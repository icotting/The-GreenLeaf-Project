package edu.unl.act.rma.firm.streamflow.component;

import java.rmi.RemoteException;
import java.util.List;

import edu.unl.act.rma.firm.core.StationList;
import edu.unl.act.rma.firm.core.spatial.BoundingBox;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USRegion;
import edu.unl.act.rma.firm.core.spatial.USState;

public interface StreamFlowSpatialExtension {
	public abstract List<String> getStationsByZipCode(String zipCode,
			int distance) throws RemoteException;

	public abstract USCounty getCounty(String station) throws RemoteException;

	public abstract StationList queryStations(BoundingBox region)
			throws RemoteException;

	public abstract List<String> getStationsForState(USState state)
			throws RemoteException;

	public abstract List<String> getStationsForGeographicRegion(USRegion region)
			throws RemoteException;

	public abstract List<String> getStationsForDefinedRegion(BoundingBox region)
			throws RemoteException;

	public abstract List<String> getStationsFromPoint(float lat, float lon, int distance) throws RemoteException;
}
