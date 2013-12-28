/* Created On: November 8, 2007 */
package edu.unl.act.rma.firm.core.spatial;

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
 * 
 * @author Ian Cottingham
 *
 */
@Entity(name="ZipCode")
@Table(name="ZipCodes")
@TableGenerator(name="ZIP_CODE_GEN", table="GENERATOR_TABLE")
public class USZipCode implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;
	private String zipCode;
	private float latitude;
	private float longitued;
	private USCity city;
	
	@Column(name = "zip_id")
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="ZIP_CODE_GEN")
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	@Column(name="zip_code")
	public String getZipCode() {
		return zipCode;
	}
	
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	
	@Column(name="lat")
	public float getLatitude() {
		return latitude;
	}
	
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	
	@Column(name="lon")
	public float getLongitued() {
		return longitued;
	}
	
	public void setLongitued(float longitued) {
		this.longitued = longitued;
	}
	
	@ManyToOne(cascade={CascadeType.MERGE}, fetch = FetchType.EAGER)
	@JoinColumn(name="city")
	public USCity getCity() {
		return city;
	}
	
	public void setCity(USCity city) {
		this.city = city;
	}
}
