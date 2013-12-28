/* Created on: Apr 22, 2010 */
package edu.unl.act.rma.firm.drought.component;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Remote;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.core.spatial.Layer;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.drought.DroughtMonitorArea;

/**
 * @author Ian Cottingham
 *
 */
@Remote
public interface DroughtMonitorQuery extends Serializable {
	
	public List<DroughtMonitorArea> queryCountyDMSequence(USCounty county, DateTime start, DateTime end);
	public List<DroughtMonitorArea> queryStateDMSequence(USState state, DateTime start, DateTime end);
	public List<DroughtMonitorArea> queryNationalDMSequence(DateTime start, DateTime end);
	
	public DroughtMonitorArea queryCountyDM(USCounty county, DateTime date);
	public DroughtMonitorArea queryStateDM(USState state, DateTime date);
	public DroughtMonitorArea queryNationalDM(DateTime date);
	
	public DroughtMonitorArea computeForDroughtImpact(long impactId);
	
	public Layer getDroughtMonitorLayerForDate(DateTime date);
	
	/**
	 * This methdo will directly assemble a KML document from database data, 
	 * bypassing the object model layer structure that is used in the 
	 * {@link #getDroughtMonitorLayerForDate(DateTime)} method.  The purpose
	 * of this method is to provide efficient access to the KML document for
	 * display on google maps.  When layer data is being exported or otherwise
	 * used by the system for manipulation, the {@link #getDroughtMonitorLayerForDate(DateTime)}
	 * should be used.
	 * 
	 * @param date
	 * 
	 * @return a KML document string representation of the layer
	 */
	public String getDroughtMonitorKmlForDate(DateTime date, float opacity);
	
}
