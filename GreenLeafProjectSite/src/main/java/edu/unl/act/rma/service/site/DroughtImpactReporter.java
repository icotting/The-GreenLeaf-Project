/* Created on: Mar 16, 2010 */
package edu.unl.act.rma.service.site;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.ManagedBean;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.KeypointPNGEncoderAdapter;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.component.SpatialQuery;
import edu.unl.act.rma.firm.core.configuration.SpatialServiceAccessor;
import edu.unl.act.rma.firm.core.spatial.Layer;
import edu.unl.act.rma.firm.core.spatial.Point;
import edu.unl.act.rma.firm.core.spatial.Region;
import edu.unl.act.rma.firm.core.spatial.SpatialReference;
import edu.unl.act.rma.firm.core.spatial.SpatialReferenceType;
import edu.unl.act.rma.firm.core.spatial.Style;
import edu.unl.act.rma.firm.core.spatial.USCity;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.drought.DroughtImpactStatistics;
import edu.unl.act.rma.firm.drought.DroughtMonitorArea;
import edu.unl.act.rma.firm.drought.DroughtReportCategory;
import edu.unl.act.rma.firm.drought.ImpactBean;
import edu.unl.act.rma.firm.drought.ReportBean;
import edu.unl.act.rma.firm.drought.component.DroughtImpactQuery;
import edu.unl.act.rma.firm.drought.configuration.DroughtServiceAccessor;
import edu.unl.act.rma.web.beans.DirBean;
import edu.unl.act.rma.web.beans.ImpactViewBean;

/**
 * @author Ian Cottingham
 * 
 */
@ManagedBean
@SessionScoped
@Path("/dir/")
public class DroughtImpactReporter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LogManager.getLogger(
			Loggers.APPLICATION_LOG, DroughtImpactReporter.class);
	private final DateTimeFormatter FORMATTER = DateTimeFormat
			.forPattern("yyyyMMdd");

	@Inject
	DirBean dirBean;
	@Inject
	ImpactViewBean impactViewBean;

	@EJB
	SpatialQuery spatialQuery;
	
	@EJB 
	DroughtImpactQuery impactQuery;
	
	@GET
	@Produces("text/xml")
	@Path("/maps/dominantCategory")
	public String impactCountMap() {
		DroughtImpactStatistics<USState> stats = dirBean.getNationStats();

		StringWriter out = new StringWriter();
		try {
			XMLStreamWriter writer;

			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
			writer.writeStartDocument();

			writer.writeStartElement("map");
			writer.writeAttribute("borderColor", "cccccc");
			writer.writeAttribute("showCanvasBorder", "0");
			writer.writeAttribute("fillColor", "F1f1f1");
			writer.writeAttribute("fillAlpha", "100");
			writer.writeAttribute("showLabels", "0");
			writer.writeAttribute("showShadow", "0");
			writer.writeAttribute("showBevel", "0");
			writer.writeAttribute("bgAlpha", "0");
			writer.writeAttribute("mapBottomMargin", "0");
			writer.writeStartElement("data");
			for (USState state : stats) {
				DroughtReportCategory category = stats.getDivisionStatistics(
						state).getDominantCategory();

				writer.writeStartElement("entity");
				writer.writeAttribute("link",
						"javascript:alert('" + state.name() + "')");
				writer.writeAttribute("id", state.getPostalCode());
				writer.writeAttribute("value",
						String.valueOf(stats.getDivisionImpactCount(state)));
				if (category != null) {
					writer.writeAttribute("color", category.getColorHex());
				}
				writer.writeEndElement();
			}
			writer.writeEndElement();
			writer.writeEndElement();

			writer.writeEndDocument();
		} catch (Exception e) {
			LOG.error("An error occured writting the XML stream", e);
			RuntimeException re = new RuntimeException(
					"Could not produce the map data");
			re.initCause(e);
			throw re;
		}

		return out.toString();
	}

	@GET
	@Produces("text/xml")
	@Path("/charts/categories")
	public String impactCategoryChart() {
		DroughtImpactStatistics<USState> stats = dirBean.getNationStats();

		StringWriter out = new StringWriter();
		try {
			XMLStreamWriter writer;

			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
			writer.writeStartDocument();

			writer.writeStartElement("chart");
			// if ( full ) {
			// writer.writeAttribute("showLegend", "1");
			// writer.writeAttribute("showLabels", "1");
			// writer.writeAttribute("showValues", "1");
			// } else {
			writer.writeAttribute("showLegend", "0");
			writer.writeAttribute("showLabels", "1");
			writer.writeAttribute("showValues", "1");
			// }

			writer.writeAttribute("chartTopMargin", "0");
			writer.writeAttribute("captionPadding", "1");
			writer.writeAttribute("bgAlpha", "0");
			writer.writeAttribute("bgColor", "ffffff");
			writer.writeAttribute("canvasbgColor", "ffffff");
			writer.writeAttribute("caption", "Reported Impacts by Category");
			writer.writeAttribute("showBorder", "0");
			writer.writeAttribute("shownames", "0");
			writer.writeAttribute("decimals", "3");

			HashMap<DroughtReportCategory, Integer> category_counts = new HashMap<DroughtReportCategory, Integer>();

			for (USState state : stats) {
				for (DroughtReportCategory cat : stats
						.divisionCategories(state)) {
					if (category_counts.get(cat) == null) {
						category_counts.put(cat,
								stats.getDivisionCategoryCount(state, cat));
					} else {
						category_counts.put(cat,
								(category_counts.get(cat) + stats
										.getDivisionCategoryCount(state, cat)));
					}
				}
			}

			for (DroughtReportCategory cat : category_counts.keySet()) {
				writer.writeStartElement("set");
				writer.writeAttribute("value",
						String.valueOf(category_counts.get(cat)));
				writer.writeAttribute("label", cat.getPrintName());
				writer.writeAttribute("color", cat.getColorHex());
				writer.writeEndElement();
			}

			writer.writeEndElement();

			writer.writeEndDocument();
		} catch (Exception e) {
			LOG.error("An error occured writting the XML stream", e);
			RuntimeException re = new RuntimeException(
					"Could not produce the map data");
			re.initCause(e);
			throw re;
		}

		return out.toString();

	}

	@GET
	@Produces("text/xml")
	@Path("/charts/losses")
	public String impactCategoryLossChart() {
		DroughtImpactStatistics<USState> stats = dirBean.getNationStats();

		StringWriter out = new StringWriter();
		try {
			XMLStreamWriter writer;

			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
			writer.writeStartDocument();

			writer.writeStartElement("chart");
			// if ( full ) {
			writer.writeAttribute("showLegend", "0");
			writer.writeAttribute("showLabels", "1");
			writer.writeAttribute("showValues", "1");
			// } else {
			// writer.writeAttribute("showLegend", "0");
			// writer.writeAttribute("showLabels", "0");
			// writer.writeAttribute("showValues", "0");
			// }

			writer.writeAttribute("captionPadding", "0");
			writer.writeAttribute("numberPrefix", "$");
			writer.writeAttribute("bgAlpha", "100");
			writer.writeAttribute("bgColor", "ffffff");
			writer.writeAttribute("canvasbgColor", "ffffff");
			writer.writeAttribute("showBorder", "0");
			writer.writeAttribute("caption",
					"Drought Impact Losses by Category");
			writer.writeAttribute("shownames", "0");
			writer.writeAttribute("decimals", "3");

			HashMap<DroughtReportCategory, Float> category_losses = new HashMap<DroughtReportCategory, Float>();

			for (USState state : stats) {
				for (DroughtReportCategory cat : stats
						.divisionCategories(state)) {
					if (category_losses.get(cat) == null) {
						category_losses.put(cat,
								stats.getDivisionCategoryLoss(state, cat));
					} else {
						category_losses.put(cat,
								(category_losses.get(cat) + stats
										.getDivisionCategoryLoss(state, cat)));
					}
				}
			}

			for (DroughtReportCategory cat : category_losses.keySet()) {
				writer.writeStartElement("set");
				writer.writeAttribute("value",
						String.valueOf(category_losses.get(cat)));
				writer.writeAttribute("label", cat.getPrintName());
				writer.writeAttribute("color", cat.getColorHex());
				writer.writeEndElement();
			}

			writer.writeEndElement();

			writer.writeEndDocument();
		} catch (Exception e) {
			LOG.error("An error occured writting the XML stream", e);
			RuntimeException re = new RuntimeException(
					"Could not produce the map data");
			re.initCause(e);
			throw re;
		}

		return out.toString();

	}

	@GET
	@Produces("text/xml")
	@Path("/charts/reportdm")
	public String impactDmFrequencies() {
		DroughtMonitorArea dm = impactViewBean.getDmClassifications();

		StringWriter out = new StringWriter();
		try {
			XMLStreamWriter writer;

			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
			writer.writeStartDocument();

			writer.writeStartElement("chart");
			writer.writeAttribute("showLegend", "0");
			writer.writeAttribute("showLabels", "0");
			writer.writeAttribute("showValues", "0");

			writer.writeAttribute("bgAlpha", "100");
			writer.writeAttribute("bgColor", "ffffff");
			writer.writeAttribute("canvasbgColor", "ffffff");
			writer.writeAttribute("showBorder", "0");
			writer.writeAttribute("caption", "");
			writer.writeAttribute("shownames", "0");
			writer.writeAttribute("decimals", "3");
			writer.writeAttribute("useRoundEdges", "1");
			writer.writeAttribute("chartRightMargin", "60");

			writer.writeStartElement("set");
			writer.writeAttribute("value", String.valueOf(dm.getD0()));
			writer.writeAttribute("label", AreaStatistics.DM_NAMES[0]);
			writer.writeAttribute("color", AreaStatistics.DM_COLORS[0]);
			writer.writeEndElement();

			writer.writeStartElement("set");
			writer.writeAttribute("value", String.valueOf(dm.getD1()));
			writer.writeAttribute("label", AreaStatistics.DM_NAMES[1]);
			writer.writeAttribute("color", AreaStatistics.DM_COLORS[1]);
			writer.writeEndElement();

			writer.writeStartElement("set");
			writer.writeAttribute("value", String.valueOf(dm.getD2()));
			writer.writeAttribute("label", AreaStatistics.DM_NAMES[2]);
			writer.writeAttribute("color", AreaStatistics.DM_COLORS[2]);
			writer.writeEndElement();

			writer.writeStartElement("set");
			writer.writeAttribute("value", String.valueOf(dm.getD3()));
			writer.writeAttribute("label", AreaStatistics.DM_NAMES[3]);
			writer.writeAttribute("color", AreaStatistics.DM_COLORS[3]);
			writer.writeEndElement();

			writer.writeStartElement("set");
			writer.writeAttribute("value", String.valueOf(dm.getD4()));
			writer.writeAttribute("label", AreaStatistics.DM_NAMES[4]);
			writer.writeAttribute("color", AreaStatistics.DM_COLORS[4]);
			writer.writeEndElement();

			writer.writeStartElement("set");
			writer.writeAttribute("value", String.valueOf(dm.getUnclassified()));
			writer.writeAttribute("label", AreaStatistics.DM_NAMES[5]);
			writer.writeAttribute("color", AreaStatistics.DM_COLORS[5]);
			writer.writeEndElement();

			writer.writeEndElement();
			writer.writeEndDocument();

		} catch (Exception e) {
			LOG.error("An error occured writting the XML stream", e);
			RuntimeException re = new RuntimeException(
					"Could not write the XML output stream");
			re.initCause(e);
			throw re;
		}

		return out.toString();
	}

	/**
	 * This method cannot use calls to the injected impact bean as it is being
	 * called from an external context when the layer is loaded on google maps
	 * and the impact view bean does not exist in that context.
	 */
	@GET
	@Produces("text/xml")
	@Path("/maps/impactedRegions/{impactId}")
	public String impactedRegions(@PathParam("impactId") long impactId) {
		try {
			SpatialQuery query = SpatialServiceAccessor.getInstance()
					.getSpatialQuery();
			Layer layer = new Layer();

			Region region;
			Style style = new Style();
			style.setId("stateStyle");
			style.setLabelScale("2.000000");
			style.setLineWidth("2");
			style.setPolyColorAlpha("99");
			style.setPolyColorBlue("1b");
			style.setPolyColorGreen("1b");
			style.setPolyColorRed("a2");
			style.setPolyOutline(true);
			style.setPolyFill(true);

			ImpactBean impact = impactQuery.getImpactById(impactId);

			for (SpatialReference ref : impact.getSpatialReferences()) {
				region = null;
				switch (ref.getReferenceType()) {
				case US_STATE:
					region = query.getRegionForState(ref.getState());
					break;
				case US_COUNTY:
					region = query.getRegionForCounty(ref.getCounty());
					break;
				case US_CITY:
					Point p = new Point();
					USCity city = ref.getCity();
					p.setLatitude(city.getLatitude());
					p.setLongitude(city.getLongitude());
					p.setPlaceName(city.getName() + ", "
							+ city.getCounty().getState());
					p.setDescription("<h3>This is a place mark</h3><br/>For "
							+ city.getName() + ", "
							+ city.getCounty().getState());
					layer.addPoint(p);
					break;
				default:
					continue;
				}

				if (region != null) {
					region.setStyle(style);
					region.setDescription("this area is effected by this impact");
					layer.addRegion(region);
				}
			}

			for (ReportBean report : impact.getReports()) {
				for (SpatialReference ref : report.getSpatialReferences()) {
					if (ref.getReferenceType() != SpatialReferenceType.US_CITY) {
						LOG.warn("Report " + report.getReportId()
								+ " has a reference of type "
								+ ref.getReferenceType());
						continue;
					} else {
						Point p = new Point();
						USCity city = ref.getCity();
						p.setLatitude(city.getLatitude());
						p.setLongitude(city.getLongitude());
						p.setPlaceName(city.getName() + ", "
								+ city.getCounty().getState());
						StringBuffer sb = new StringBuffer("<b>Source: </b>");
						sb.append(report.getSource());
						sb.append("<br/>");
						if (report.getUrl() != null) {
							sb.append("<a href=\"");
							sb.append(report.getUrl());
							sb.append("\">Story Link</a>");
						}
						sb.append("<i>Reported in: ");
						sb.append(city.getName());
						sb.append(", ");
						sb.append(city.getCounty().getState());
						sb.append("</i>");
						p.setDescription(sb.toString());
						layer.addPoint(p);
					}
				}
			}

			return layer.toKml();
		} catch (Exception e) {
			LOG.error("Error creating impacted region KML", e);
			RuntimeException re = new RuntimeException(
					"An error occurred generating the map overlay.  See the log for details");
			re.initCause(e);
			throw re;
		}

	}

	/**
	 * This method cannot use calls to the injected impact bean as it is being
	 * called from an external context when the layer is loaded on google maps
	 * and the impact view bean does not exist in that context.
	 */
	@GET
	@Produces("image/png")
	@Path("/charts/categorypie/state/{state}")
	public byte[] stateCategoryPieChart(@PathParam("state") String stateAbbr,
			@QueryParam("date") String dateStr,
			@QueryParam("weeks") String weeks,
			@QueryParam("width") String width,
			@QueryParam("height") String height) {
		
		if ( weeks == null ) { 
			weeks = "4";
		}
		
		DateTime date = FORMATTER.parseDateTime(dateStr);
		USState state = USState.fromPostalCode(stateAbbr);
		DroughtImpactStatistics<USState> stats;

		try {
			stats = impactQuery.lookupUSImpactStats(
					date.minusWeeks(Integer.parseInt(weeks)), date);

		} catch (Exception e) {
			LOG.error("Could not compute the category statistics", e);
			RuntimeException re = new RuntimeException();
			re.initCause(e);
			throw re;
		}

		DefaultPieDataset data_set = new DefaultPieDataset();
		ArrayList<DroughtReportCategory> categories = new ArrayList<DroughtReportCategory>();

        if ( stats.containsDivision(state) ) {
            for (DroughtReportCategory cat : stats.divisionCategories(state)) {
                data_set.setValue(cat.getPrintName(), stats.getDivisionCategoryCount(state, cat));
                categories.add(cat);
            }
        }
        
		PiePlot3D data_plot = new PiePlot3D();
		data_plot.setCircular(true);
		data_plot.setDepthFactor(0.20);
		data_plot.setInteriorGap(0.1);
		data_plot.setForegroundAlpha(0.8f);
		data_plot.setDataset(data_set);
		data_plot.setIgnoreNullValues(true);
		data_plot.setLabelGenerator(null); // no section labels
		data_plot.setOutlinePaint(Color.BLACK);
		data_plot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator(
				"{0}"));
		data_plot.setBackgroundPaint(null);
		data_plot.setOutlinePaint(null);

		for (DroughtReportCategory cat : categories) {
            data_plot.setSectionPaint(cat.getPrintName(), cat.getColor());
		}

		JFreeChart chart = new JFreeChart("", null, data_plot, false);
		chart.setBackgroundPaint(new Color(255, 255, 255, 0));

		KeypointPNGEncoderAdapter encoder = new KeypointPNGEncoderAdapter();
		encoder.setEncodingAlpha(true);
		
		try { 
			return encoder.encode(chart.createBufferedImage(
					Integer.parseInt(width), Integer.parseInt(height),
					BufferedImage.BITMASK, null));
		} catch ( IOException ioe ) { 
			LOG.error("could not write the chart PNG to the response", ioe);
			RuntimeException re = new RuntimeException("Could not write the image");
			re.initCause(ioe);
			throw re;
		}
	}
	
	/**
	 * This method cannot use calls to the injected impact bean as it is being
	 * called from an external context when the layer is loaded on google maps
	 * and the impact view bean does not exist in that context.
	 */
	@GET
	@Produces("image/png")
	@Path("/charts/categorypie/county/{county}")
	public byte[] countyCategoryPieChart(@PathParam("county") String fipsCode,
			@QueryParam("date") String dateStr,
			@QueryParam("weeks") String weeks,
			@QueryParam("width") String width,
			@QueryParam("height") String height) {
		
		if ( weeks == null ) { 
			weeks = "4";
		}
		
		DateTime date = FORMATTER.parseDateTime(dateStr);
		
		USCounty county;
		
		DroughtImpactStatistics<USCounty> stats;

		try {
			county = spatialQuery.getCountyByFips(fipsCode);

			stats = impactQuery.lookupStateImpactStats(county.getState(),
					date.minusWeeks(Integer.parseInt(weeks)), date);

		} catch (Exception e) {
			LOG.error("Could not compute the category statistics", e);
			RuntimeException re = new RuntimeException();
			re.initCause(e);
			throw re;
		}

		DefaultPieDataset data_set = new DefaultPieDataset();
		ArrayList<DroughtReportCategory> categories = new ArrayList<DroughtReportCategory>();

        if ( stats.containsDivision(county) ) {
            for (DroughtReportCategory cat : stats.divisionCategories(county)) {
                data_set.setValue(cat.getPrintName(), stats.getDivisionCategoryCount(county, cat));
                categories.add(cat);
            }
        }
        
		PiePlot3D data_plot = new PiePlot3D();
		data_plot.setCircular(true);
		data_plot.setDepthFactor(0.20);
		data_plot.setInteriorGap(0.1);
		data_plot.setForegroundAlpha(0.8f);
		data_plot.setDataset(data_set);
		data_plot.setIgnoreNullValues(true);
		data_plot.setLabelGenerator(null); // no section labels
		data_plot.setOutlinePaint(Color.BLACK);
		data_plot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator(
				"{0}"));
		data_plot.setBackgroundPaint(null);
		data_plot.setOutlinePaint(null);

		for (DroughtReportCategory cat : categories) {
            data_plot.setSectionPaint(cat.getPrintName(), cat.getColor());
		}

		JFreeChart chart = new JFreeChart("", null, data_plot, false);
		chart.setBackgroundPaint(new Color(255, 255, 255, 0));
		
		KeypointPNGEncoderAdapter encoder = new KeypointPNGEncoderAdapter();
		encoder.setEncodingAlpha(true);
		
		try { 
			return encoder.encode(chart.createBufferedImage(
					Integer.parseInt(width), Integer.parseInt(height),
					BufferedImage.BITMASK, null));
		} catch ( IOException ioe ) { 
			LOG.error("could not write the chart PNG to the response", ioe);
			RuntimeException re = new RuntimeException("Could not write the image");
			re.initCause(ioe);
			throw re;
		}
	}

	@GET
	@Produces("text/xml")
	@Path("/maps/reportstories")
	public String impactReports() {
		StringWriter out = new StringWriter();
		try {
			XMLStreamWriter writer;
			SimpleDateFormat format = new SimpleDateFormat();
			format.applyPattern("MMM dd, yyyy");

			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
			writer.writeStartDocument();

			writer.writeStartElement("reports");
			for (ReportBean report : impactViewBean.getImpactBean()
					.getReports()) {
				writer.writeStartElement("report");

				SpatialReference reference = report.getSpatialReferences().get(
						0);
				writer.writeStartElement("city");
				writer.writeCharacters(reference.getCity().getName() + ", "
						+ reference.getCity().getCounty().getState());
				writer.writeEndElement();

				writer.writeStartElement("title");
				writer.writeCharacters(report.getTitle());
				writer.writeEndElement();

				writer.writeStartElement("source");
				writer.writeCharacters(report.getSource());
				writer.writeEndElement();

				writer.writeStartElement("pubdate");
				writer.writeCharacters(format.format(report
						.getPublicationDate()));
				writer.writeEndElement();

				writer.writeStartElement("url");
				writer.writeCharacters(report.getUrl());
				writer.writeEndElement();

				writer.writeEndElement();
			}

			writer.writeEndElement();
			writer.writeEndDocument();

		} catch (Exception e) {
			LOG.error("An error occured writting the XML stream", e);
			RuntimeException re = new RuntimeException(
					"Could not write the XML output stream");
			re.initCause(e);
			throw re;
		}

		return out.toString();
	}
}
