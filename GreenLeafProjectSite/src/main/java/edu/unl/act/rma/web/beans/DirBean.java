/* Created on: Mar 17, 2010 */
package edu.unl.act.rma.web.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.drought.DroughtImpactStatistics;
import edu.unl.act.rma.firm.drought.PartialImpactBean;
import edu.unl.act.rma.firm.drought.component.DroughtImpactQuery;

/**
 * @author Ian Cottingham
 *
 */
@ManagedBean
@SessionScoped
@Named
public class DirBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LogManager.getLogger(Loggers.APPLICATION_LOG, DirBean.class);
	private DroughtImpactStatistics<USState> nationStats;
	private List<PartialImpactBean> nationImpacts;
	
	private Date endDate = new Date(System.currentTimeMillis());
	private String state;
	
	public static final int MONTHS_BACK = 6;
	
	@EJB
	private DroughtImpactQuery query;
	
	@PostConstruct
	public void init() { 
		try { 
			DateTime end = new DateTime(endDate);
			DateTime start = end.minusMonths(MONTHS_BACK);
			
			nationStats = query.lookupUSImpactStats(start, end);
			nationImpacts = query.queryImpacts(start, end);
			
		} catch ( Exception e ) { 
			LOG.error("Could not get the impact statistics", e);
			RuntimeException re = new RuntimeException("The impact statistics could not be mapped");
			re.initCause(e);
			throw re;
		}
	}
	
	public DroughtImpactStatistics<USState> getNationStats() {
		return nationStats;
	}
	
	public List<PartialImpactBean> getNationImpacts() { 
		return nationImpacts;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		if ( this.endDate.equals(endDate) ) { 
			return;
		} else {
			this.endDate = endDate;
		
			nationStats = null;
			nationImpacts = null;
			init();
		}
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
