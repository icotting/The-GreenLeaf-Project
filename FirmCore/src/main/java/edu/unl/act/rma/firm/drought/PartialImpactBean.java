/* Created on: Jun 14, 2010 */
package edu.unl.act.rma.firm.drought;

import java.io.Serializable;

/**
 * @author Ian Cottingham
 *
 * The partial impact bean contains a subset of ImpactBean data 
 * and is used for faster queries and display of basic impact 
 * data in lists
 */
public class PartialImpactBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String title;
	private String summary;
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getSummary() {
		return summary;
	}
	
	public void setSummary(String summary) {
		this.summary = summary;
	}
}
