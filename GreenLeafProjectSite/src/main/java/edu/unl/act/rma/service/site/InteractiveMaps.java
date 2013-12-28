/* Created on: Jun 24, 2010 */
package edu.unl.act.rma.service.site;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.ManagedBean;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.component.SpatialQuery;
import edu.unl.act.rma.firm.core.spatial.BoundingBox;
import edu.unl.act.rma.firm.core.spatial.Layer;
import edu.unl.act.rma.firm.core.spatial.Region;
import edu.unl.act.rma.firm.core.spatial.SpatialReferenceType;
import edu.unl.act.rma.firm.core.spatial.Style;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.drought.DroughtImpactDivisionStatistics;
import edu.unl.act.rma.firm.drought.DroughtImpactStatistics;
import edu.unl.act.rma.firm.drought.DroughtReportCategory;
import edu.unl.act.rma.firm.drought.component.DroughtImpactQuery;
import edu.unl.act.rma.firm.drought.component.DroughtMonitorQuery;

/**
 * @author Ian Cottingham
 *
 */
@ManagedBean
@SessionScoped
@Path("/maps/")
public class InteractiveMaps implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger LOG = LogManager.getLogger(Loggers.APPLICATION_LOG, InteractiveMaps.class);
	
	@EJB DroughtMonitorQuery dmQuery;
	@EJB DroughtImpactQuery dirQuery;
	@EJB SpatialQuery spatialQuery;

    @Context UriInfo uriInfo;

	private final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyyMMdd");
	
	@GET
	@Produces("text/xml")
	@Path("/dm/{date}.kml")
	public String dmMap(@PathParam("date") String mapDate, @QueryParam("opacity") String opacity) { 
	
		try {
			if ( opacity == null ) { 
				opacity = "99";
			}
			
			return dmQuery.getDroughtMonitorKmlForDate(FORMATTER.parseDateTime(mapDate), Float.parseFloat(opacity));
		
		} catch ( Exception e ) { 
			LOG.error("There was an error getting the KML layer", e);
			RuntimeException re = new RuntimeException("Could not generate the KML layer");
			re.initCause(e);
			throw re;
		}
	}
	
	@GET
	@Produces("application/vnd.google-earth.kmz")
	@Path("/dm/{date}.kmz")
	public byte[] dmMapCompressed(@PathParam("date") String mapDate, @QueryParam("opacity") String opacity) { 
	
		try {
			if ( opacity == null ) { 
				opacity = "99";
			}
			
			byte[] kml = dmQuery.getDroughtMonitorKmlForDate(FORMATTER.parseDateTime(mapDate), Float.parseFloat(opacity)).getBytes();
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(baos);
			zos.putNextEntry(new ZipEntry("doc.kml"));
			zos.write(kml);
			zos.close();
			return baos.toByteArray();
			
		} catch ( Exception e ) { 
			LOG.error("There was an error getting the KML layer", e);
			RuntimeException re = new RuntimeException("Could not generate the KML layer");
			re.initCause(e);
			throw re;
		}
	}
	
	@GET
	@Produces("appliation/vnd.google-earth.kmz")
	@Path("/dir/categories/{date}.kmz")
	public byte[] dirCategories(@PathParam("date") String mapDate, @QueryParam("weeks") Integer weeks, @QueryParam("opacity") String opacity, 
			@QueryParam("spatialType") SpatialReferenceType type, @QueryParam("north") Float north, @QueryParam("south") Float south, 
			@QueryParam("east") Float east, @QueryParam("west") Float west) { 
		
		DateTime date = FORMATTER.parseDateTime(mapDate);
		if ( weeks == null ) { 
			weeks = 4;
		}
		
		BoundingBox box = null;
		if ( north != null && south != null && east != null && west != null ) { 
			box = new BoundingBox(north, west, south, east);
		}
		
		byte[] kml = new byte[0];
	
		switch ( type ) {
			case US_STATE: 
				 kml = new ResultProcessor<USState>().generateDirCategoryKml(dirQuery.lookupUSImpactStats(date.minusWeeks(weeks), date), 
						 weeks, date, opacity);
				break;
			case US_COUNTY:
				 kml = new ResultProcessor<USCounty>().generateDirCategoryKml(dirQuery.lookupBoundedImpacts(box, date.minusWeeks(weeks), date), 
						 weeks, date, opacity);
				break;
		
		}

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(baos);
			zos.putNextEntry(new ZipEntry("doc.kml"));
			zos.write(kml);
			zos.close();
			return baos.toByteArray();
		} catch ( Exception e ) { 
			LOG.error("could not write the DIR layer zip file", e);
			RuntimeException re = new RuntimeException("An error occurred creating the KMZ file");
			re.initCause(e);
			throw re;
		}
	}
	
	
	private class ResultProcessor<T> {
		
		private byte[] generateDirCategoryKml(DroughtImpactStatistics<T> stats, int weeks, DateTime date, String opacity) { 
			
			Layer layer = new Layer();
			HashMap<DroughtReportCategory, Style> style_cache = new HashMap<DroughtReportCategory, Style>();
			
			try {
				for ( DroughtImpactDivisionStatistics<T> div_stats : stats.getStatistics() ) {
					T type = div_stats.getDivision();
					DroughtReportCategory category = div_stats.getDominantCategory();
					if ( category == null ) { 
						LOG.warn("A null category was found for division "+type);
						continue;
					}
					Region r = null;
					
					if ( type instanceof USState ) {
						r = spatialQuery.getRegionForState((USState)type);
					 } else if ( type instanceof USCounty ) { 
						 r = spatialQuery.getRegionForCounty((USCounty)type);
					 }
					
					r.setName(type.toString()+" Reported Impacts");
					StringBuffer sb = new StringBuffer("<div style=\"width: 300px\">There were ");
					sb.append(stats.getDivisionImpactCount(type));
					sb.append(" impacts reported for ");
					sb.append(type.toString());
					sb.append(". The most frequently reported impact category was <span style=\"font-weight: bold; color: #");
					sb.append(category.getColorHex());
					sb.append("\">");
					sb.append(category.getPrintName());
					sb.append("</span>.");

					URI uri = uriInfo.getBaseUri();
					
	                sb.append("<img style=\"float: right\" src=\"http://");
	                sb.append(uri.getHost());
	                sb.append(":");
	                sb.append(uri.getPort());
	                sb.append("/GreenLeafProjectSite/service/dir/charts/categorypie/");
	                
	                if ( type instanceof USState ) {
		                sb.append("state/");
	                	sb.append(((USState)type).getPostalCode());
	                } else if ( type instanceof USCounty ) { 
	                	sb.append("county/");
	                	sb.append(((USCounty)type).getFips());
	                }
	                
	                sb.append("?date=");
	                sb.append(FORMATTER.print(date));
	                sb.append("&weeks=");
	                sb.append(weeks);
	                sb.append("&width=120&height=120\"/></div>");

					r.setDescription(sb.toString());
					Style s = style_cache.get(category);
					if ( s == null ) { 
						s = new Style();
						s.setId(category.toString()+"Style");
						Color c = category.getColor();
						s.setPolyColorBlue(Integer.toHexString(c.getBlue()));
						s.setPolyColorGreen(Integer.toHexString(c.getGreen()));
						s.setPolyColorRed(Integer.toHexString(c.getRed()));
						s.setPolyOutline(true);
						style_cache.put(category, s);
					}
					r.setStyle(s);
					layer.addRegion(r);
				}
			} catch ( Exception e ) { 
				LOG.error("An error occurred building the DIR layer", e);
				RuntimeException re = new RuntimeException();
				re.initCause(e);
				throw re;
			}
			
			if ( opacity != null ) { 
				layer.setFillOpacity(Float.parseFloat(opacity));
			}
			
			return layer.toKml().getBytes();
		}	
	}
}
