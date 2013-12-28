/* Created On Jan 19, 2007 */
package edu.unl.act.rma.firm.drought;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import edu.unl.act.rma.firm.core.search.SearchResultDescription;
import edu.unl.act.rma.firm.core.search.SearchResultIdentifier;
import edu.unl.act.rma.firm.core.search.SearchResultMeta;
import edu.unl.act.rma.firm.core.search.SearchResultName;
import edu.unl.act.rma.firm.core.spatial.SpatialReference;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;

/**
 * @author Ian Cottingham
 *
 */
@Entity(name="DroughtImpact")
@Table(name="DroughtImpacts")
public class ImpactBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long impactId;

	private String summary;
	private String title;
	private Date startDate;
	private Date endDate;
	private int legacyId;
	
	private List<SpatialReference> spatialReferences;
	private List<ReportCategory> reportCategories;
	private List<ReportBean> reports;
	
	public ImpactBean() { 
		reports = new ArrayList<ReportBean>();
	}
		
	@Column(name="summary", columnDefinition="longtext")
	@SearchResultDescription
	public String getSummary() {
		return summary;
	}
	
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	@Column(name="title")
	@SearchResultName
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Column(name = "impact_id")
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@SearchResultIdentifier
	public long getImpactId() {
		return impactId;
	}
	
	public void setImpactId(long impactId) {
		this.impactId = impactId;
	}
	
    @ManyToMany(cascade={CascadeType.PERSIST}, fetch=FetchType.EAGER, targetEntity=ReportBean.class)
	public List<ReportBean> getReports() {
		return reports;
	}
	
	public void setReports(List<ReportBean> reports) {
		this.reports = reports;
	}
			
	@Temporal(value=TemporalType.DATE)
	@Column(name="end_date")
	@SearchResultMeta(name="effectEnd")
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	@Temporal(value=TemporalType.DATE)
	@Column(name="start_date")
	@SearchResultMeta(name="effectStart")
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@OneToMany(mappedBy="impactReport", fetch=FetchType.EAGER, cascade={CascadeType.ALL})
	@SearchResultMeta(name="categories")
	public List<ReportCategory> getReportCategories() {
		return reportCategories;
	}

	public void setReportCategories(List<ReportCategory> reportCategories) {
		this.reportCategories = reportCategories;
	}

	@OneToMany(mappedBy="impactReport", fetch=FetchType.EAGER, cascade={CascadeType.ALL})
	public List<SpatialReference> getSpatialReferences() {
		return spatialReferences;
	}

	public void setSpatialReferences(List<SpatialReference> spatialReferences) {
		this.spatialReferences = spatialReferences;
	}

	@Transient
	public double getDollarLoss() { 
		double amt = 0d;
		for ( ReportCategory cat : reportCategories ) { 
			amt += cat.getDollarLoss();
		}
		
		return amt;
	}
	
	@Transient
	public Iterable<USCounty> getCounties() { 
		Set<USCounty> counties = new TreeSet<USCounty>();
		for ( SpatialReference ref : spatialReferences ) { 
			counties.addAll(ref.extractCounties());
		}
		
		return counties;
	}

	@Transient
	public Iterable<USState> getStates() { 
		Set<USState> states = new TreeSet<USState>();
		for ( SpatialReference ref : spatialReferences ) { 
			states.add(ref.getState());
		}
		
		return states;
	}
	
	
	public boolean matchCategory(DroughtReportCategory category) { 
		return ((ArrayList)this.getReportCategories()).contains(category);
	}
	
	public boolean matchState(USState state) { 
		return ((ArrayList)this.getStates()).contains(state);
	}
	
	@Override
	public boolean equals(Object obj) { 
		if ( !(obj instanceof ImpactBean) ) { 
			return false;
		}
				
		return ((ImpactBean)obj).impactId == this.impactId;
	}

	public int compareTo(ImpactBean obj) {
		if ( obj.getImpactId() == this.impactId ) {
			return 0;
		}
		
		return obj.getTitle().compareTo(this.title);
	}

	@Column(name="legacy_id")
	public int getLegacyId() {
		return legacyId;
	}

	public void setLegacyId(int legacyId) {
		this.legacyId = legacyId;
	}	
}
