/* Created On Nov 21, 2006 */
package edu.unl.act.rma.firm.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;


/**
 * @author Ian Cottingham
 *
 */
@Entity
@Table(name="StationList")
@TableGenerator(name="STATION_LIST_GEN", table="GENERATOR_TABLE")
public class StationList implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long listId;
	private String name;
	private String description;
	private ArrayList<String> stationList;
	
	public StationList() { 
		this.stationList = new ArrayList<String>();
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Column(name = "list_id")
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="STATION_LIST_GEN")
	public long getListId() {
		return listId;
	}
	
	public void setListId(long listId) {
		this.listId = listId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Basic
	@Column(columnDefinition="longtext")
	public ArrayList<String> getStationList() {
		return stationList;
	}

	public void setStationList(ArrayList<String> stationList) {
		this.stationList = stationList;
	}
	
	/* these methods facilitate use of a list without the serialization issue */
	@Transient
	public List<String> getStations() {
		return stationList;
	}
	
	public void setStations(List<String> stations) {
		ArrayList<String> set_list = new ArrayList<String>();
		for ( String station : stations ) { 
			set_list.add(station);
		}
		
		setStationList(set_list);
	}
}
