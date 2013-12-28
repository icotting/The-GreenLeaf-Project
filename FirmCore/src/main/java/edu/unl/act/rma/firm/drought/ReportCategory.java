/* Created On Jan 22, 2007 */
package edu.unl.act.rma.firm.drought;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 * @author Ian Cottingham
 *
 */
@Entity(name="ReportCategory")
@Table(name="ReportCategories")
public class ReportCategory implements Serializable, Comparable<ReportCategory> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ReportBean mediaReport;
	private ImpactBean impactReport;
	private DroughtReportCategory impactCategory;
	
	private double dollarLoss;
	private long reportId;

	public DroughtReportCategory getImpactCategory() {
		return impactCategory;
	}

	public void setImpactCategory(DroughtReportCategory impactCategory) {
		this.impactCategory = impactCategory;
	}

	@ManyToOne(cascade={CascadeType.MERGE}, fetch = FetchType.EAGER)
	@JoinColumn(name="media_report")
	public ReportBean getMediaReport() {
		return mediaReport;
	}

	public void setMediaReport(ReportBean mediaReport) {
		this.mediaReport = mediaReport;
	}


	@ManyToOne(cascade={CascadeType.MERGE}, fetch = FetchType.EAGER)
	@JoinColumn(name="impact_report")
	public ImpactBean getImpactReport() {
		return impactReport;
	}

	public void setImpactReport(ImpactBean impactReport) {
		this.impactReport = impactReport;
	}

	@Column(name="dollar_loss")
	public double getDollarLoss() {
		return dollarLoss;
	}

	public void setDollarLoss(double dollarLoss) {
		this.dollarLoss = dollarLoss;
	}

	@Column(name = "categoryId")
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long getReportId() {
		return reportId;
	}

	public void setReportId(long reportId) {
		this.reportId = reportId;
	}

	@Override
	public String toString() { 
		return impactCategory.getPrintName();
	}
	
	public int compareTo(ReportCategory category) {
		DroughtReportCategory i_cat = category.getImpactCategory();
		
		if ( i_cat.equals(this.impactCategory) ) {
			return 0;
		} else if ( i_cat.ordinal() > this.impactCategory.ordinal() ) {
			return 1;
		} else {
			return -1;
		}
	}	
	
}
