package edu.unl.act.rma.firm.streamflow.component;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.Remote;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;

@Remote
public interface StreamFlowDataQuery {
	
	public CalendarDataCollection getAvailableData(List<String> stationIds,
			DataType type, CalendarPeriod period) throws RemoteException,
			InvalidArgumentException;
			
	public CalendarDataCollection getDataNormals(List<String> stationIds,
			int startYear, int endYear, DataType type, CalendarPeriod period)
			throws RemoteException, InvalidArgumentException;

	public CalendarDataCollection getHistoricalAverageData(
			List<String> stationIds, DataType type, CalendarPeriod period)
			throws RemoteException, InvalidArgumentException;
	
	public CalendarDataCollection getStreamFlowPeriodData(
			List<String> stationIds, DateTime firstDate, DateTime lastDate,
			DataType type, CalendarPeriod period) throws RemoteException,
			InvalidArgumentException;
	
}
