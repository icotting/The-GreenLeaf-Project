/* Created On: Sep 30, 2005 */
package edu.unl.act.rma.firm.drought.component;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.Remote;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DTOCollection;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.drought.NsmCompleteDTO;
import edu.unl.act.rma.firm.drought.NsmSummaryDTO;
import edu.unl.act.rma.firm.drought.PdsiType;

/**
 * @author Ian Cottingham
 *
 */
@Remote
public interface DroughtIndexQuery {

	public DTOCollection<NsmSummaryDTO> computeSummaryNewhall(List<String> stations, DateTime start, DateTime end) throws RemoteException, InvalidArgumentException;

	public DTOCollection<NsmCompleteDTO> computeCompleteNewhall(List<String> stations, DateTime start, DateTime end) throws RemoteException, InvalidArgumentException;
	
	public CalendarDataCollection computeScPdsi(List<String> stations, PdsiType type, int step, CalendarPeriod period, DateTime ending, int fromYear) throws RemoteException, InvalidArgumentException;
	
	public CalendarDataCollection computeSpi(List<String> stations, CalendarPeriod period, int step, DateTime ending, int fromYear) throws RemoteException, InvalidArgumentException;

	public CalendarDataCollection computeRangeKBDI(List<String> stations, DateTime firstday, DateTime lastday) throws RemoteException, InvalidArgumentException;
}
