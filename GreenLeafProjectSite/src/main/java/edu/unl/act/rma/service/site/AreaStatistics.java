/* Created on Sept 21, 2009 */
package edu.unl.act.rma.service.site;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

import javax.annotation.ManagedBean;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.unl.act.rma.firm.climate.component.ClimateDataQuery;
import edu.unl.act.rma.firm.climate.component.ClimateMetaDataQuery;
import edu.unl.act.rma.firm.climate.component.ClimateSpatialExtension;
import edu.unl.act.rma.firm.climate.configuration.ClimateServiceAccessor;
import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.MetaDataCollection;
import edu.unl.act.rma.firm.core.StationMetaDataType;
import edu.unl.act.rma.firm.drought.DroughtMonitorArea;
import edu.unl.act.rma.firm.drought.DroughtReportCategory;
import edu.unl.act.rma.firm.drought.ImpactBean;
import edu.unl.act.rma.firm.drought.ReportCategory;
import edu.unl.act.rma.web.beans.LocationBean;

/**
 * 
 * @author Ian Cottingham 
 */
@ManagedBean
@SessionScoped
@Path("/home/stats")
public class AreaStatistics implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = LogManager.getLogger(Loggers.APPLICATION_LOG, AreaStatistics.class);
    public static final String[] DM_COLORS = { "ffff00", "ffd280", "e69900", "e50000", "720101", "cccccc" };
    public static final String[] DM_NAMES = { "D0 - Abnormally Dry", "D1 - Moderate Drought", "D2 - Severe Drought", "D3 - Extreme Drought", "D4 - Exceptional Drought", "Normal Conditions" };
	
	private static final DateTimeFormatter FORMAT = DateTimeFormat.forPattern("MMM dd");
    
	@Inject LocationBean locationBean;

	@GET
	@Produces("text/xml")
	@Path("/precip/full")
	public String getFullPrecipChart() throws Exception { 		
		return buildPrecipChart(true);
	}
	
	@GET
	@Produces("text/xml")
	@Path("/precip")
	public String getSmallPrecipChart() throws Exception { 		
		return buildPrecipChart(false);
	}

	@GET
	@Produces("text/plain")
	@Path("/dm/dmConditionsHead")
	public String computePrevailingDmConditions() throws Exception { 
		
		if ( locationBean.isNeedDmRefresh() ) { 
			locationBean.refreshDmFrequencies();
		}
		
		return locationBean.getPrevailingDmConditionsHeader();
	}
	
	@GET
	@Produces("text/xml")
	@Path("/dm/frequencyChart")
	public String getSmallDmFrequencyChart() throws Exception { 
		return buildDmChart(false);
	}	
	
	@GET
	@Produces("text/xml")
	@Path("/dm/frequencyChart/full")
	public String getFullDmFrequencyChart() throws Exception { 
		return buildDmChart(true);
	}	
	
	@GET
	@Produces("text/xml")
	@Path("/dir/categories")
	public String getSmallDirCategoriesChart() throws Exception { 
		return buildImpactCategoriesChart(false);
	}
	
	@GET
	@Produces("text/xml")
	@Path("/dir/categories/full")
	public String getFullDirCategoriesChart() throws Exception { 
		return buildImpactCategoriesChart(true);
	}
	
	@GET
	@Produces("text/xml")
	@Path("/dir/categories/loss")
	public String getSmallDirCategoryLossChart() throws Exception { 
		return buildImpactCategoryLossChart(false);
	}
	
	@GET
	@Produces("text/xml")
	@Path("/dir/categories/loss/full")
	public String getFullDirCategoryLossChart() throws Exception { 
		return buildImpactCategoryLossChart(true);
	}
	
	private float checkVal(float val) { 
		if ( val == DataType.MISSING | val == DataType.ERROR_RESULT || val == DataType.NONEXISTANT || val == DataType.OUTSIDE_OF_RANGE || val == DataType.OUTSIDE_OF_REQUEST_RANGE ) { 
			return 0;
		} else { 
			return val;
		}
	}
	
	private String buildDmChart(boolean full) throws Exception { 
		StringWriter out = new StringWriter();
		try { 
			XMLStreamWriter writer;
			
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
			writer.writeStartDocument();

			writer.writeStartElement("chart");
			if ( full ) {
				writer.writeAttribute("showLegend", "1");
				writer.writeAttribute("showLabels", "1");
			} else { 
				writer.writeAttribute("showLegend", "0");
				writer.writeAttribute("showLabels", "0");
			}
			writer.writeAttribute("bgColor", "ffffff");
			writer.writeAttribute("canvasbgColor", "ffffff");			
			writer.writeAttribute("showBorder", "0");		
			writer.writeAttribute("caption", "Weekly Drought Monitor Frequency");
			writer.writeAttribute("shownames", "1");
			writer.writeAttribute("showvalues", "0");
			writer.writeAttribute("showSum", "1");
			writer.writeAttribute("decimals", "0");
			writer.writeAttribute("useRoundEdges", "1");
			writer.writeAttribute("numberSuffix", "%");
			writer.writeAttribute("yAxisMaxValue", "100");
			writer.writeAttribute("chartRightMargin", "60");
			writer.writeAttribute("yAxisValuesPadding", "20");
			writer.writeStartElement("categories");
			
			
			writer.writeStartElement("category");
			writer.writeAttribute("label", "Four Months Ago");
			writer.writeEndElement();
			
			writer.writeStartElement("category");
			writer.writeAttribute("label", "Three Months Ago");
			writer.writeEndElement();
			
			writer.writeStartElement("category");
			writer.writeAttribute("label", "Two Months Ago");
			writer.writeEndElement();
			
			writer.writeStartElement("category");
			writer.writeAttribute("label", "Last Month");
			writer.writeEndElement();
			
			writer.writeStartElement("category");
			writer.writeAttribute("label", "This Week");
			writer.writeEndElement();

			writer.writeEndElement();
			
			List<DroughtMonitorArea> dm_descriptors = locationBean.getDmFrequencies();

			
			
			for ( int i=0; i<5; i++ ) {
				writer.writeStartElement("dataset");
				writer.writeAttribute("seriesName", DM_NAMES[i]);
				writer.writeAttribute("color", DM_COLORS[i]);
				writer.writeAttribute("showValues", "0");
				
				for ( int j=dm_descriptors.size() -1; j >= 0; j-- ) {
					DroughtMonitorArea d = dm_descriptors.get(j);
					
					writer.writeStartElement("set");
					if ( d == null ) { 
						writer.writeAttribute("value", "0");
					} else {
						switch ( i ) { 
							case 0:
								writer.writeAttribute("value", String.valueOf(d.getD0()));
								break;
							case 1:
								writer.writeAttribute("value", String.valueOf(d.getD1()));
								break;
							case 2:
								writer.writeAttribute("value", String.valueOf(d.getD2()));
								break;
							case 3:
								writer.writeAttribute("value", String.valueOf(d.getD3()));
								break;
							case 4:
								writer.writeAttribute("value", String.valueOf(d.getD4()));
								break;
						}
					}
					writer.writeEndElement();
				}
				
				writer.writeEndElement();
			}
			
			writer.writeEndElement();
			
			writer.writeEndDocument();
			
		} catch ( Exception e ) { 
			LOG.error("An error occured writting the XML stream", e);
			RuntimeException re = new RuntimeException("Could not write the XML output stream");
			re.initCause(e);
			throw re;
		}
		
		return out.toString();
	}
	
	private String buildPrecipChart(boolean full) throws Exception { 
		ClimateSpatialExtension spatial_query; 
		ClimateMetaDataQuery meta_query;
		ClimateDataQuery data_query;
		try { 
			spatial_query = ClimateServiceAccessor.getInstance().getSpatialExtension();
			meta_query = ClimateServiceAccessor.getInstance().getClimateMetaDataQuery();
			data_query = ClimateServiceAccessor.getInstance().getClimateDataQuery();
			
		} catch ( Exception e ) { 
			LOG.error("An error occured while getting the query object" ,e);
			RuntimeException re = new RuntimeException("Could not get the query object");
			re.initCause(e);
			throw re;
		}
		
		DateTime now = locationBean.getLocationDate();
		DateTime then = now.minusMonths(4);
		
		MetaDataCollection<StationMetaDataType> meta_data;
		CalendarDataCollection avg_precip_data;
		CalendarDataCollection precip_data;
		
		try {
			List<String> stations = spatial_query.getStationsByZipCode(locationBean.getZipCode(), 10);

			meta_data = meta_query.getAllMetaData(stations, CalendarPeriod.WEEKLY);
			avg_precip_data = data_query.getHistoricalAverageData(stations, DataType.PRECIP, CalendarPeriod.WEEKLY).collectionAverage();
			precip_data = data_query.getPeriodData(stations, then, now, DataType.PRECIP, CalendarPeriod.WEEKLY).collectionAverage();
			
		} catch ( Exception e ) { 
			LOG.error("An error occured obtaining the data summaries", e);
			RuntimeException re = new RuntimeException("Could not compute the data summaries");
			re.initCause(e);
			throw re;
		}
		
		XMLStreamWriter writer;
		StringWriter out = new StringWriter();
		
		writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
		writer.writeStartDocument();
		
		writer.writeStartElement("chart"); // start root element
				
		// root element attributes
		if ( full ) {
			writer.writeAttribute("showLegend", "1");
			writer.writeAttribute("showLabels", "1");
		} else {
			writer.writeAttribute("showLegend", "0");
			writer.writeAttribute("showLabels", "0");
		}
		writer.writeAttribute("bgColor", "ffffff");
		writer.writeAttribute("canvasbgColor", "ffffff");		
		writer.writeAttribute("showBorder", "0");		
		writer.writeAttribute("caption", "Weekly Precipitation Summary");
		writer.writeAttribute("shownames", "1");
		writer.writeAttribute("decimals", "3");
		writer.writeAttribute("useRoundEdges", "1");
		writer.writeAttribute("chartRightMargin", "60");
		writer.writeAttribute("yAxisValuesPadding", "20");

		writer.writeStartElement("categories"); // start categories element
		DateTime counter = then;
		while ( counter.isBefore(now) ) { 
			writer.writeStartElement("category");
			writer.writeAttribute("label", FORMAT.print(counter));
			writer.writeEndElement();
			counter = counter.plusWeeks(1);
		}
		writer.writeEndElement(); // end categories element
		
		
		counter = then;
		writer.writeStartElement("dataset"); // start dataset element
		//dataset attributes
		writer.writeAttribute("seriesName", "Current Precip");
		if ( full ) {
			writer.writeAttribute("showvalues", "1");
		} else { 
			writer.writeAttribute("showvalues", "0");
		}
		while ( counter.isBefore(now) ) {
			writer.writeStartElement("set");
			writer.writeAttribute("value", String.valueOf(checkVal(precip_data.getStationDataForDate(CalendarDataCollection.AREA_ID, counter))));
			writer.writeEndElement();
			counter = counter.plusWeeks(1);
		}
		writer.writeEndElement(); // end dataset element
		
		counter = then;
		
		writer.writeStartElement("dataset"); // start dataset element
		//dataset attributes
		writer.writeAttribute("seriesName", "Average Precip");
		writer.writeAttribute("renderAs", "line");
		writer.writeAttribute("showvalues", "0");
		
		while ( counter.isBefore(now) ) {
			writer.writeStartElement("set");
			
			int pos = counter.getWeekOfWeekyear();
			int month = counter.getMonthOfYear();
			// deal with the month problem yet again -- Maybe this should be put into a single static method somewhere
			if ( pos == 53 ) {
				if ( month == 1 ) {
					pos = 1;
				} else {
					pos = 52; 
				}
			} else if ( pos == 52 && month == 1 ) {
				pos = 1;
			} else if ( pos == 1 && month == 12 ) {
				pos = 52;
			}
			
			writer.writeAttribute("value", String.valueOf(checkVal(avg_precip_data.getStationDataForPeriodPosition(CalendarDataCollection.AREA_ID, 1, pos))));
			writer.writeEndElement();
			counter = counter.plusWeeks(1);
		}
		writer.writeEndElement(); // end dataset element
		
		writer.writeEndElement(); // end root element
		writer.writeEndDocument();
		
		return out.toString();
	}
	
	private String buildImpactCategoriesChart(boolean full) throws Exception { 
		StringWriter out = new StringWriter();
		try { 
			XMLStreamWriter writer;
			
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
			writer.writeStartDocument();

			writer.writeStartElement("chart");
			if ( full ) {
				writer.writeAttribute("showLegend", "1");
				writer.writeAttribute("showLabels", "1");
				writer.writeAttribute("showValues", "1");				
			} else { 
				writer.writeAttribute("showLegend", "0");
				writer.writeAttribute("showLabels", "0");
				writer.writeAttribute("showValues", "0");
			}
			
			writer.writeAttribute("bgAlpha", "100");
			writer.writeAttribute("bgColor", "ffffff");
			writer.writeAttribute("canvasbgColor", "ffffff");		
			writer.writeAttribute("showBorder", "0");		
			writer.writeAttribute("caption", "Drought Impact Categories");
			writer.writeAttribute("shownames", "0");
			writer.writeAttribute("decimals", "3");
			writer.writeAttribute("useRoundEdges", "1");
			writer.writeAttribute("chartRightMargin", "60");
			
			HashMap<DroughtReportCategory, Integer> category_counts = new HashMap<DroughtReportCategory, Integer>();
			
			for ( ImpactBean impact : locationBean.getDroughtImpactReports() ) { 
				for ( ReportCategory cat : impact.getReportCategories() ) { 
					if ( category_counts.get(cat.getImpactCategory()) == null ) { 
						category_counts.put(cat.getImpactCategory(), 1);
					} else { 
						category_counts.put(cat.getImpactCategory(), (category_counts.get(cat.getImpactCategory())+1));
					}
				}
			}
			
			for ( DroughtReportCategory cat : category_counts.keySet() ) { 
				writer.writeStartElement("set");
				writer.writeAttribute("value", String.valueOf(category_counts.get(cat)));
				writer.writeAttribute("label", cat.getPrintName());
				writer.writeAttribute("color", cat.getColorHex());
				writer.writeEndElement();
			}
			
			writer.writeEndElement();
			writer.writeEndDocument();
			
		} catch ( Exception e ) { 
			LOG.error("An error occured writting the XML stream", e);
			RuntimeException re = new RuntimeException("Could not write the XML output stream");
			re.initCause(e);
			throw re;
		}
		
		return out.toString();
	}
	
	private String buildImpactCategoryLossChart(boolean full) throws Exception { 
		StringWriter out = new StringWriter();
		try { 
			XMLStreamWriter writer;
			
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
			writer.writeStartDocument();

			writer.writeStartElement("chart");
			if ( full ) {
				writer.writeAttribute("showLegend", "1");
				writer.writeAttribute("showLabels", "1");
				writer.writeAttribute("showValues", "1");				
			} else { 
				writer.writeAttribute("showLegend", "0");
				writer.writeAttribute("showLabels", "0");
				writer.writeAttribute("showValues", "0");
			}
			
			writer.writeAttribute("numberPrefix", "$");
			writer.writeAttribute("bgAlpha", "100");
			writer.writeAttribute("bgColor", "ffffff");
			writer.writeAttribute("canvasbgColor", "ffffff");		
			writer.writeAttribute("showBorder", "0");		
			writer.writeAttribute("caption", "Losses by Impact Categories");
			writer.writeAttribute("shownames", "0");
			writer.writeAttribute("decimals", "3");
			writer.writeAttribute("useRoundEdges", "1");
			writer.writeAttribute("chartRightMargin", "60");
			
			HashMap<DroughtReportCategory, Double> category_losses = new HashMap<DroughtReportCategory, Double>();
			
			for ( ImpactBean impact : locationBean.getDroughtImpactReports() ) { 
				for ( ReportCategory cat : impact.getReportCategories() ) { 
					if ( category_losses.get(cat.getImpactCategory()) == null ) { 
						category_losses.put(cat.getImpactCategory(), cat.getDollarLoss());
					} else { 
						category_losses.put(cat.getImpactCategory(), (category_losses.get(cat.getImpactCategory())+cat.getDollarLoss()));
					}
				}
			}
			
			for ( DroughtReportCategory cat : category_losses.keySet() ) { 
				writer.writeStartElement("set");
				writer.writeAttribute("value", String.valueOf(category_losses.get(cat)));
				writer.writeAttribute("label", cat.getPrintName());
				writer.writeAttribute("color", cat.getColorHex());
				writer.writeEndElement();
			}
			
			writer.writeEndElement();
			writer.writeEndDocument();
			
		} catch ( Exception e ) { 
			LOG.error("An error occured writting the XML stream", e);
			RuntimeException re = new RuntimeException("Could not write the XML output stream");
			re.initCause(e);
			throw re;
		}
		
		return out.toString();
	}
}