/* Created On Nov 30, 2006 */
package edu.unl.act.rma.firm.drought.component;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Remote;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.core.spatial.BoundingBox;
import edu.unl.act.rma.firm.core.spatial.SpatialReferenceType;
import edu.unl.act.rma.firm.core.spatial.USCity;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.drought.DroughtImpactStatistics;
import edu.unl.act.rma.firm.drought.ImpactBean;
import edu.unl.act.rma.firm.drought.PartialImpactBean;

/**
 * 
 * @author Ian Cottingham
 *
 */
@Remote
public interface DroughtImpactQuery extends Serializable {

	/**
	 * 
	 * @param startDate
	 * @param endDate
	 * @return - list of partially filled 
	 */
	public List<PartialImpactBean> queryImpacts(DateTime startDate, DateTime endDate);	
	public List<PartialImpactBean> queryImpactsForCity(USCity city, DateTime startDate, DateTime endDate);	
	public List<PartialImpactBean> queryImpactsForCounty(USCounty county, DateTime startDate, DateTime endDate);
	public List<PartialImpactBean> queryImpactsForState(USState state, DateTime startDate, DateTime endDate);

	public List<PartialImpactBean> queryAllImpactsForCity(USCity city);
	public List<PartialImpactBean> queryAllImpactsForCounty(USCounty county);
	public List<PartialImpactBean> queryAllImpactsForState(USState state);
	
	public List<PartialImpactBean> queryImpacts(BoundingBox box, DateTime start, DateTime end, SpatialReferenceType type);
	public List<PartialImpactBean> searchAllReports(String query_string);
	
	public DroughtImpactStatistics<USState> lookupUSImpactStats(DateTime start, DateTime end);
	public DroughtImpactStatistics<USCounty> lookupStateImpactStats(USState state, DateTime start, DateTime end);
	public DroughtImpactStatistics<USCounty> lookupBoundedImpacts(BoundingBox region, DateTime start, DateTime end);
	
	public ImpactBean getImpactById(long id);
	public List<ImpactBean> loadAllImpacts(List<PartialImpactBean> partials); 
}
