/* Created On Feb 13, 2007 */
package edu.unl.act.rma.firm.core.spatial;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.component.SpatialQuery;
import edu.unl.act.rma.firm.core.configuration.SpatialServiceAccessor;
import edu.unl.act.rma.firm.drought.DroughtMonitorArea;
import edu.unl.act.rma.firm.drought.ImpactBean;
import edu.unl.act.rma.firm.drought.ReportBean;

/**
 * @author Ian Cottingham
 *
 */
@Entity(name="SpatialReference")
@Table(name="SpatialReferences")
public class SpatialReference implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static Logger LOG = LogManager.getLogger(Loggers.SYSTEM_LOG, SpatialReference.class);
	
	private long referenceId;
	private BoundingBox boundary;
	private USState state;
	private USCounty county;
	private USCity city;
	private USZipCode zipCode;
	private SpatialReferenceType referenceType;
	
	@OneToOne(cascade={CascadeType.ALL})
	@JoinColumn(name="box_id")
	public BoundingBox getBoundary() {
		return boundary;
	}
	
	public void setBoundary(BoundingBox boundary) {
		this.boundary = boundary;
	}
	
    @ManyToOne(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER, targetEntity=USCounty.class)
    @JoinColumn(name="county_id")
    public USCounty getCounty() {
    		return county;
	}
    
	public void setCounty(USCounty county) {
		this.county = county;
	}
    
    @ManyToOne(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER, targetEntity=USCity.class)
    @JoinColumn(name="city_id")
	public USCity getCity() {
    		return city;
    }
    
	public void setCity(USCity city) {
		this.city = city;
	}
	
	@ManyToOne(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER, targetEntity=USZipCode.class)
	@JoinColumn(name="zip_id")
	public USZipCode getZipCode() {
		return zipCode;
	}

	public void setZipCode(USZipCode zipCode) {
		this.zipCode = zipCode;
	}

	@Column(name = "ref_id")
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long getReferenceId() {
		return referenceId;
	}
	
	public void setReferenceId(long referenceId) {
		this.referenceId = referenceId;
	}
	
	@Column(name="state")
	public USState getState() {
		return state;
	}
	
	public void setState(USState state) {
		this.state = state;
	}

	@Column(name="ref_type")
	public SpatialReferenceType getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(SpatialReferenceType referenceType) {
		this.referenceType = referenceType;
	}

	@Transient
	public Set<USState> extractStates() {
		Set<USState> the_states = new TreeSet<USState>();
		switch ( referenceType ) { 
			case CUSTOM:
				try {
					SpatialQuery query = SpatialServiceAccessor.getInstance().getSpatialQuery();
					the_states.addAll(query.getStatesByRegion(this.boundary));
				} catch ( Exception e ) { 
					LOG.error("error getting counties", e);
				}
				return the_states;
			case US_STATE:
				try {
					the_states.add(this.state);
				} catch ( Exception e ) { 
					LOG.error("error getting counties", e);
				}
				return the_states;
			case US_COUNTY:
				the_states.add(county.getState());
				return the_states;
			case US_CITY:
				the_states.add(city.getCounty().getState());
				return the_states;
			default: 
				return null;
		}
		
	}
	
	@Transient
	public Set<USCounty> extractCounties() {
    	Set<USCounty> county_lookup = new TreeSet<USCounty>();
    	switch ( referenceType ) { 
    	case CUSTOM:
    		try {
    			SpatialQuery query = SpatialServiceAccessor.getInstance().getSpatialQuery();
    			county_lookup.addAll(query.getCountiesByRegion(this.boundary));
    		} catch ( Exception e ) { 
    			LOG.error("error getting counties", e);
    		}
    		return county_lookup;
    	case US_STATE:
    		try {
    			SpatialQuery query = SpatialServiceAccessor.getInstance().getSpatialQuery();
    			county_lookup.addAll(query.getCountiesByState(this.state));
    		} catch ( Exception e ) { 
    			LOG.error("error getting counties", e);
    		}
    		return county_lookup;
    	case US_CITY:
    		county_lookup.add(city.getCounty());	
    		return county_lookup;
    	default:
    		county_lookup.add(county);
    		return county_lookup;
    	}
	}
	
	@Transient
	public Set<USCity> extractCities() { 
		Set<USCity> cities = new TreeSet<USCity>();
		switch ( referenceType ) { 
		case CUSTOM:
			try {
				SpatialQuery query = SpatialServiceAccessor.getInstance().getSpatialQuery();
				Set<USCounty> counties = query.getCountiesByRegion(this.boundary);
				for ( USCounty county : counties ) { 
					cities.addAll(county.getCities());
				}
    		} catch ( Exception e ) { 
    			LOG.error("error getting cities", e);
    		}			
			break;
		case US_STATE:
			try {
				SpatialQuery query = SpatialServiceAccessor.getInstance().getSpatialQuery();
				cities.addAll(query.getCitiesByState(this.state));
    		} catch ( Exception e ) { 
    			LOG.error("error getting cities", e);
    		}
			break;
		case US_COUNTY:
			cities.addAll(county.getCities());
			break;
		default:
			cities.add(this.city);
		}
		return cities;
	}
	
	@Transient
	public boolean overlap(SpatialReference ref) {		
		try {
			switch ( this.getReferenceType() ) {
			case CUSTOM:
				return compareCustom(ref);
			case US_COUNTY:
				return compareCounty(ref);
			case US_STATE:
				return compareState(ref);
			case US_CITY:
				return compareCity(ref);
			}
		} catch ( Exception e ) { 
			LOG.error("An error occured comparing spatial types", e);
		}
		return false;
	}
	
	private boolean compareCounty(SpatialReference ref) throws Exception {
		if ( ref.referenceType.equals(SpatialReferenceType.US_COUNTY) ) {
			return county.equals(ref.getCounty());
		} else {
			Set<USCounty> counties = ref.extractCounties();
			Set<USCounty> comp_counties = this.extractCounties();
			
			for ( USCounty county : counties ) { 
				if ( comp_counties.contains(county) ) {
					return true;
				}
			}
		}

		return false;
	}
	
	private boolean compareState(SpatialReference ref) throws Exception {
		switch ( ref.referenceType ) {
		case CUSTOM:
			
			Set<USState> states = ref.extractStates();
			return states.contains(this.state);
		case US_COUNTY:
			state = this.getState();
			
			for ( USCounty county : ref.extractCounties() ) { 
				if ( county.getState().equals(state) ) {
					return true;
				}
			}
			break;
		case US_STATE:
			return this.state.equals(ref.getState());
		case US_CITY:
			for ( USCity city : ref.extractCities() ) { 
				if ( city.getCounty().getState().equals(this.state) ) {
					return true;
				}
			}
		}
		
		return false;
		
	}
	
	private boolean compareCity(SpatialReference ref) { 
		
		return false;
	}
	
	//TODO: implement this
	private boolean compareCustom(SpatialReference ref) throws Exception {
		switch ( ref.referenceType ) {
		case CUSTOM:
			return this.getBoundary().contains(ref.getBoundary());
		case US_COUNTY:
			break;
		case US_STATE:
			break;
		}
		
		return false;
	}
	
	private ReportBean mediaReport;
	private ImpactBean impactReport;
	private DroughtMonitorArea dmDescriptor;
	
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
}
