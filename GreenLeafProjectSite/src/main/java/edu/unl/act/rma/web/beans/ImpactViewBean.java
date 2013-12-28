/* Created on: Mar 29, 2010 */
package edu.unl.act.rma.web.beans;

import java.io.Serializable;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.servlet.http.HttpServletRequest;

import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.drought.DroughtMonitorArea;
import edu.unl.act.rma.firm.drought.ImpactBean;
import edu.unl.act.rma.firm.drought.component.DroughtImpactQuery;
import edu.unl.act.rma.firm.drought.component.DroughtMonitorQuery;


/**
 * @author Ian Cottingham
 *
 */
@ManagedBean
@SessionScoped
@Named
public class ImpactViewBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LogManager.getLogger(Loggers.APPLICATION_LOG, ImpactViewBean.class);

	@PersistenceContext(unitName="FirmCorePU", type=PersistenceContextType.TRANSACTION)
	private transient EntityManager manager;

	@EJB
	DroughtImpactQuery impactQuery;
	
	@EJB
	DroughtMonitorQuery dmQuery;
	
	private ImpactBean impactBean;
	private DroughtMonitorArea dm;
	private Long impactId;

	@PostConstruct
	public void init() { 

	}

	public ImpactBean getImpactBean() {
		if ( FacesContext.getCurrentInstance() != null ) {
			HttpServletRequest request=(HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String impact_id = request.getParameter("impact");
			Long numeric_id = -1l;
			
			if ( impact_id != null ) { 
				try { 
					numeric_id = Long.parseLong(impact_id);
				} catch ( NumberFormatException nfe ) { LOG.warn("In invalid impact ID was sent to the impact view page: "+impact_id); }
			} 
		
			if ( impactBean == null || (numeric_id != -1 && numeric_id != impactId) ) {
				impactId = numeric_id;
				dm = null;
				setImpactBean(manager.find(ImpactBean.class, numeric_id));
			}
		}
		
		return impactBean;
	}
	
	public void setImpactBean(ImpactBean impactBean) {
		this.impactBean = impactBean;
	}	
	
	public DroughtMonitorArea getDmClassifications() { 		
		if ( dm == null ) { 
			dm = dmQuery.computeForDroughtImpact(impactBean.getImpactId());
		}
		return dm;
	}
}
