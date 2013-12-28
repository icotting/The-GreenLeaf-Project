/* Created on Jun 3, 2008 */
package edu.unl.act.rma.web.beans;

import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

import edu.unl.act.rma.firm.climate.VariableMetaData;
import edu.unl.act.rma.firm.climate.component.ClimateMetaDataQuery;
import edu.unl.act.rma.firm.climate.component.ClimateSpatialExtension;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.MetaDataCollection;
import edu.unl.act.rma.firm.core.StationMetaDataType;
import edu.unl.act.rma.firm.core.component.SpatialQuery;
import edu.unl.act.rma.firm.core.spatial.USCity;
import edu.unl.act.rma.firm.core.spatial.USZipCode;
import edu.unl.act.rma.firm.drought.DroughtMonitorArea;
import edu.unl.act.rma.firm.drought.ImpactBean;
import edu.unl.act.rma.firm.drought.component.DroughtImpactQuery;
import edu.unl.act.rma.firm.drought.component.DroughtMonitorQuery;
import edu.unl.act.rma.firm.drought.configuration.DroughtServiceAccessor;
import edu.unl.act.rma.service.site.AreaStatistics;
import edu.unl.act.rma.web.model.Station;

/**
 * @author Ian Cottingham
 *
 */
@ManagedBean
@SessionScoped
@Named
public class LocationBean implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LogManager.getLogger(Loggers.APPLICATION_LOG, LocationBean.class);
	private static final DateTimeFormatter FORMAT = DateTimeFormat.forPattern("MMM dd, yyyy");

	private Location location;
	private List<Station> stations;
	private List<ImpactBean> droughtImpactReports;

	private boolean newLocation;
	private USCity city;
	private USZipCode zipCode;
	
	private List<DroughtMonitorArea> dmDescriptors;

	private String updateZipCode;
	private boolean needDmRefresh; 

	@EJB private DroughtMonitorQuery dmQuery;
	@EJB private SpatialQuery spatialQuery;
	@EJB private ClimateMetaDataQuery climateMetaQuery;
	@EJB private ClimateSpatialExtension climateSpatialQuery;
	
	@PostConstruct
	public void init() {
		File db = new File(LocationBean.class.getClassLoader().getResource("GeoIPCityus.dat").getPath());

		try {
			LookupService lookup = new LookupService(db, LookupService.GEOIP_MEMORY_CACHE);
			location = lookup.getLocation(InetAddress.getLocalHost().getHostAddress());
			
			if ( location == null || location.region == null || location.city == null || location.postalCode == null ) {
				// if the location cannot be found, use the server location
				location = lookup.getLocation("129.93.88.66");
				LOG.warn("The default zip code is being used because the location could not be optained by ip address");
			}
			
			this.city = spatialQuery.getCityByZip(location.postalCode);
			if ( city != null ) {
				for ( USZipCode zip : this.city.getZipCodes() ) { 
					if ( zip.getZipCode().equals(this.updateZipCode) ) { 
						this.zipCode = zip;
						break;
					}
				}
			}
			
		} catch ( Exception ioe ) { 
			LOG.error("could not look up the location", ioe);
			FacesException fe = new FacesException();
			fe.initCause(ioe);
			throw fe;
		}
		
		processLocation();
	}

	private void processLocation() { 
		needDmRefresh = true;
		
		try {
			findStations();
		} catch ( Exception e ) {
			LOG.error("could not query the stations for the location", e);
			FacesException fe = new FacesException();
			fe.initCause(e);
			throw fe;
		}

		try { 
			findDroughtImpactReports();
		} catch ( Exception e ) { 
			LOG.error("could not query the drought impact reports for the location", e);
			FacesException fe = new FacesException();
			fe.initCause(e);
			throw fe;
		}
	}

	private void findStations() throws Exception { 
		List<String> station_ids = climateSpatialQuery.getStationsByZipCode(this.getZipCode(), 5);
		MetaDataCollection<StationMetaDataType> station_meta = climateMetaQuery.getAllMetaData(station_ids, CalendarPeriod.WEEKLY);
		Map<String, Map<DataType, VariableMetaData>> variable_meta = climateMetaQuery.getVariableMetaData(station_ids);
		
		stations = new ArrayList<Station>();
		
		for ( String id : station_ids ) { 
			stations.add(new Station(id, station_meta.getStationMetaData(id), variable_meta.get(id)));
		}
	}

	private void findDroughtImpactReports() throws Exception { 
		DateTime now = getLocationDate();
		DateTime then = now.minusMonths(4);
		
		DroughtImpactQuery query = DroughtServiceAccessor.getInstance().getDroughtImpactQuery();
		this.droughtImpactReports = query.loadAllImpacts(query.queryImpactsForCounty(this.getCityObject().getCounty(), then, now));
	}
	
	public float getLat() { 
		return location.latitude;
	}
	
	public float getLon() { 
		return location.longitude;
	}
	
	public String getCity() { 
		return location.city;
	}
	
	public String getState() { 
		return location.region;
	}
	
	public USZipCode getZipCodeObject() { 
		return this.zipCode;
	}

	public USCity getCityObject() { 
		return this.city;
	}
	
	public String getZipCode() { 
		return location.postalCode;
	}
	
	public List<Station> getStations() {
		return stations;
	}

	public List<ImpactBean> getDroughtImpactReports() {
		return droughtImpactReports;
	}
	
	public boolean getRenderStations() { 
		return ( stations != null && stations.size() > 0 );
	}
	
	public boolean getRenderDroughtImpactReports() { 
		return ( droughtImpactReports != null && droughtImpactReports.size() > 0 );
	}

	public boolean isNewLocation() {
		return newLocation;
	}

	public void setNewLocation(boolean newLocation) {
		this.newLocation = newLocation;
	}

	public String getUpdateZipCode() {
		return updateZipCode;
	}

	public void setUpdateZipCode(String updateZipCode) {
		this.updateZipCode = updateZipCode;
	}
	
	public String updateLocation() { 

		try { 
			this.city = spatialQuery.getCityByZip(this.updateZipCode);
			
			if ( city == null ) { 
				FacesMessage message = new FacesMessage();
				message.setSeverity(FacesMessage.SEVERITY_ERROR);
				message.setDetail("Invalid or unrecognized zip code");
				message.setSummary("Invalid or unrecognized zip code");
				FacesContext.getCurrentInstance().addMessage("footerForm:zip_code", message);
				this.updateZipCode = "";
				
			} else { 
				location = new Location();
				location.postalCode = this.updateZipCode;
				location.city = city.getName();
				location.region = city.getCounty().getState().getPostalCode();
				location.countryName = city.getCounty().getName();
				for ( USZipCode zip : city.getZipCodes() ) { 
					if ( zip.getZipCode().equals(this.updateZipCode) ) { 
						this.zipCode = zip;
						location.latitude = zip.getLatitude();
						location.longitude = zip.getLongitued();
						break;
					}
				}
				processLocation();
				this.newLocation = false;
				this.updateZipCode = "";
			}
			
		} catch ( Exception e ) { 
			LOG.error("could not query the new location", e);
			FacesException fe = new FacesException();
			fe.initCause(e);
			throw fe;
		}
		
		return null;
	}
	
	public String getStationCount() { 
		return String.valueOf(stations.size());
	}
	
	public String getDroughtImpactReportCount() { 
		if ( this.droughtImpactReports != null ) { 
			return String.valueOf(droughtImpactReports.size());
		} else { 
			return "0";
		}
	}
	
	/*
	public String invokeStationSearch() { 
		
		FacesContext fc = FacesContext.getCurrentInstance();
		SearchController search_controller = (SearchController)fc.getApplication().getExpressionFactory().
			createValueExpression(fc.getELContext(), "#{search}", SearchController.class).getValue(fc.getELContext());

		search_controller.setQuery("");
		search_controller.setAdvancedStationMode();
		AdvancedStationSearchParameters param = new AdvancedStationSearchParameters();
		param.setZip(location.postalCode);
		param.setMiles(5);
		search_controller.STATION_BEAN.setAdvancedParameters(param);

		return search_controller.performSearch();
	}
	
	public String invokeDroughtImpactReportSearch() { 
		
		FacesContext fc = FacesContext.getCurrentInstance();
		SearchController search_controller = (SearchController)fc.getApplication().getExpressionFactory().
			createValueExpression(fc.getELContext(), "#{search}", SearchController.class).getValue(fc.getELContext());

		DateTime now = new DateTime(System.currentTimeMillis());
		DateTime start = now.minusMonths(1);
		
		search_controller.setQuery("");
		search_controller.DROUGHT_REPORT_BEAN.setZipQuery(location.postalCode);
		search_controller.setAdvancedDroughtReportMode();
		
		search_controller.DROUGHT_REPORT_BEAN.setEffectStart(start.toDate());
		search_controller.DROUGHT_REPORT_BEAN.setEffectEnd(now.toDate());
		search_controller.DROUGHT_REPORT_BEAN.setEffectFilter(true);

		return search_controller.performSearch();
	}
	*/
	
	public void refreshDmFrequencies() throws Exception { 
		DateTime dm_date = getLocationDate();
		
		this.dmDescriptors = new ArrayList<DroughtMonitorArea>();
		
		for ( int i=0; i<5; i++ ) { 
			dmDescriptors.add(dmQuery.queryCountyDM(this.getCityObject().getCounty(), dm_date));
			dm_date = dm_date.minusMonths(1);
		}

		needDmRefresh = false;
	}

	// this algorithim is NOT very efficient, it should probably be refactored at some point
	public String getPrevailingDmConditionsHeader() { 
						
		double[] totals = {0,0,0,0,0,0}; // this array will hold the matrix column sums
		
		for ( DroughtMonitorArea d : dmDescriptors ) { 
			if ( d == null ) { 
				continue;
			}
			
			// load values into the matrix
			totals[0] += d.getD0();
			totals[1] += d.getD1();
			totals[2] += d.getD2();
			totals[3] += d.getD3();
			totals[4] += d.getD4();
			totals[5] += d.getUnclassified();
		}
		
		double all_drought = 0;
		double max_val = 0;
		int max_pos = 0;
		for ( int i=0; i<5; i++ ) { 
			all_drought += totals[i]; // total all the drought values (0-4)
			if ( totals[i] > max_val ) { // determine the highest % area of drought across D0 - D4
				max_val = totals[i];
				max_pos = i;
			}
		}
		
		StringBuffer condition_text = new StringBuffer("The prevailing drought conditions based on the " +
				"<a style=\"font-size: 10pt\" href=\"http://drought.unl.edu/dm/monitor.html\">The US Drought Monitor</a> from ");
		
		DateTime now = getLocationDate();
		DateTime then = now.minusMonths(4);
		
		condition_text.append(FORMAT.print(then));
		condition_text.append(" to ");
		condition_text.append(FORMAT.print(now));
		condition_text.append(" for this region around ");
		condition_text.append(city.getName());
		condition_text.append(", ");
		condition_text.append(city.getCounty().getState().name());
		condition_text.append(" are: ");
		if ( totals[5] > all_drought ) { // compare to non-drought total (5)
			condition_text.append("<span style=\"font-weight: bold;\">");
			condition_text.append("Normal.</span>");
		} else { 
			condition_text.append("<span style=\"font-weight: bold; color: #");
			condition_text.append(AreaStatistics.DM_COLORS[max_pos]);
			condition_text.append("\">");
			condition_text.append(AreaStatistics.DM_NAMES[max_pos]);
			condition_text.append(".</span>");
		}
		
		return condition_text.toString();
	}
	
	public List<DroughtMonitorArea> getDmFrequencies() {
		if ( needDmRefresh ) { 
			throw new RuntimeException("The DM frequency data is out of sync with the location, a refresh call should be made.");
		} else {
			return dmDescriptors;
		}
	}

	public boolean isNeedDmRefresh() {
		return needDmRefresh;
	}
	
	/* single point for obtaining the date for the location calculations, 
	 * this ensures consistency in methods and allows for easy change for
	 * generating data for demos.
	 */
	public DateTime getLocationDate() { 
		return new DateTime(System.currentTimeMillis());
	}
}