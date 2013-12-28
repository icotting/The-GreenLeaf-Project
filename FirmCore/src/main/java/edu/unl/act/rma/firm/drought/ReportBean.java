/* Created On Jan 19, 2007 */
package edu.unl.act.rma.firm.drought;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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

import edu.unl.act.rma.firm.core.search.SearchResultIdentifier;
import edu.unl.act.rma.firm.core.search.SearchResultMeta;
import edu.unl.act.rma.firm.core.search.SearchResultName;
import edu.unl.act.rma.firm.core.spatial.SpatialReference;

/**
 * @author Ian Cottingham
 *
 */
@Entity(name="DroughtReport")
@Table(name="DroughtReports")
public class ReportBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long reportId;
	private int legacyId;
	private Date publicationDate;
	private String title;
	private String source;
	private String url;
	
	private List<SpatialReference> spatialReferences;
	private List<ImpactBean> impacts;
	private List<ReportCategory> reportCategories;
			
	@Column(name="title")
	@SearchResultName
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Column(name = "report_id")
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@SearchResultIdentifier
	public long getReportId() {
		return reportId;
	}
	
	public void setReportId(long reportId) {
		this.reportId = reportId;
	}
		
	@Temporal(value=TemporalType.DATE)
	@Column(name="publication_date")
	@SearchResultMeta(name="displayDate")
	public Date getPublicationDate() {
		return publicationDate;
	}
	
	public void setPublicationDate(Date publicationDate) {
		this.publicationDate = publicationDate;
	}
	
	@Column(name="source")
	@SearchResultMeta(name="source")
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	@Column(name="url")
	@SearchResultMeta(name="url")
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	@OneToMany(mappedBy="mediaReport", fetch=FetchType.EAGER, cascade={CascadeType.ALL})
	@SearchResultMeta(name="categories")
	public List<ReportCategory> getReportCategories() {
		return reportCategories;
	}

	public void setReportCategories(List<ReportCategory> reportCategories) {
		this.reportCategories = reportCategories;
	}
		
	@OneToMany(mappedBy="mediaReport", fetch=FetchType.EAGER, cascade={CascadeType.ALL})
	public List<SpatialReference> getSpatialReferences() {
		return spatialReferences;
	}


	public void setSpatialReferences(List<SpatialReference> spatialReferences) {
		this.spatialReferences = spatialReferences;
	}
	
	@ManyToMany(cascade={CascadeType.MERGE}, fetch=FetchType.EAGER, mappedBy="reports", targetEntity=ImpactBean.class)
	public List<ImpactBean> getImpacts() {
		return impacts;
	}

	public void setImpacts(List<ImpactBean> impacts) {
		this.impacts = impacts;
	}

	@Override
	public boolean equals(Object obj) { 
		if ( !(obj instanceof ReportBean) ) { 
			return false;
		}
				
		return ((ReportBean)obj).reportId == this.reportId;
	}
	
	@Transient
	public double getDollarLoss() { 
		double amt = 0d;
		for ( ReportCategory cat : reportCategories ) { 
			amt += cat.getDollarLoss();
		}
		
		return amt;
	}
	
	/**
	 * The id of the report in the legacy db.  This field, taken together with legacy, 
	 * will allow one to detrmine if a report should be exported to the legacy db.  
	 * 
	 * iff ( !legacy && legacyId == 0 ) => this report was not entered automatically
	 * from the legacy db, and it has not been exported to the legacy db; meaning that
	 * this report is not currently in the legacy db.
	 * 
	 */
	@Column(name="legacy_id")
	public int getLegacyId() {
		return legacyId;
	}

	public void setLegacyId(int legacyId) {
		this.legacyId = legacyId;
	}
	
	public int compareTo(ReportBean obj) {
		if ( obj.getReportId() == this.reportId ) { 
			return 0;
		}
		
		return obj.getTitle().compareTo(this.title);
	}
}
