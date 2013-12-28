/* Created on: Apr 22, 2010 */
package edu.unl.act.rma.firm.drought.component;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.sql.DataSource;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.spatial.Layer;
import edu.unl.act.rma.firm.core.spatial.Region;
import edu.unl.act.rma.firm.core.spatial.SpatialReference;
import edu.unl.act.rma.firm.core.spatial.SpatialReferenceType;
import edu.unl.act.rma.firm.core.spatial.Style;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.drought.DroughtMonitorArea;
import edu.unl.act.rma.firm.drought.DroughtMonitorDescriptor;
import edu.unl.act.rma.firm.drought.ImpactBean;

/**
 * @author Ian Cottingham
 *
 */
@Stateless
@Remote({DroughtMonitorQuery.class})
public class DroughtMonitorQueryBean implements DroughtMonitorQuery {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG, DroughtMonitorQuery.class);
	
	/** The peristence manager, used to manage the bound entity beans. */
	@PersistenceContext(unitName="FirmCorePU", type=PersistenceContextType.TRANSACTION)
	private transient EntityManager manager;
	
	private DataSource source = DataSourceInjector.injectDataSource(DataSourceTypes.SYSTEM);
	
	@Override
	public DroughtMonitorArea queryCountyDM(USCounty county,
			DateTime date) {
		
		date = roundDMDate(date);
		
		List<DroughtMonitorArea> descriptors = manager.createQuery("select distinct d from DroughtMonitorArea d " +
				"where d.county = :county and d.descriptor.mapDate = :date").setParameter("county", county).setParameter("date", date.toDate()).getResultList();
		
		if ( descriptors.size() == 0 ) { 
			LOG.warn("No descriptor could be found for county "+county.getName()+" on "+date.toString());
			return null;
		} else { 
			return descriptors.get(0);
		}
	}

	@Override
	public List<DroughtMonitorArea> queryCountyDMSequence(
			USCounty county, DateTime start, DateTime end) {
		
		return manager.createQuery("select distinct d from DroughtMonitorArea d " +
			"where d.county = :county and (d.descriptor.mapDate between :start and :end)").setParameter("county", county)
			.setParameter("start", start.toDate()).setParameter("end", end.toDate()).getResultList();
	}

	@Override
	public DroughtMonitorArea queryNationalDM(DateTime date) {

		date = roundDMDate(date);
		
		List<DroughtMonitorArea> descriptors = manager.createQuery("select distinct d from DroughtMonitorArea d " +
				"where d.type = :type and d.descriptor.mapDate = :date").setParameter("type", SpatialReferenceType.US).setParameter("date", date.toDate()).getResultList();
		
		if ( descriptors.size() == 0 ) { 
			LOG.warn("No descriptor could be found for the US on "+date.toString());
			return null;
		} else { 
			return descriptors.get(0);
		}
	}

	@Override
	public List<DroughtMonitorArea> queryNationalDMSequence(
			DateTime start, DateTime end) {

		return manager.createQuery("select distinct d from DroughtMonitorArea d " +
			"where d.type = :type and (d.descriptor.mapDate between :start and :end)").setParameter("type", SpatialReferenceType.US)
			.setParameter("start", start.toDate()).setParameter("end", end.toDate()).getResultList();

	}

	@Override
	public DroughtMonitorArea queryStateDM(USState state,
			DateTime date) {

		date = roundDMDate(date);
		
		List<DroughtMonitorArea> descriptors = manager.createQuery("select distinct d from DroughtMonitorArea d " +
				"where d.state = :state and d.descriptor.mapDate = :date").setParameter("state", state).setParameter("date", date.toDate()).getResultList();
		
		if ( descriptors.size() == 0 ) { 
			LOG.warn("No descriptor could be found for state "+state.name()+" on "+date.toString());
			return null;
		} else { 
			return descriptors.get(0);
		}
	}

	@Override
	public List<DroughtMonitorArea> queryStateDMSequence(USState state,
			DateTime start, DateTime end) {

		return manager.createQuery("select distinct d from DroughtMonitorArea d " +
			"where d.state = :state and (d.descriptor.mapDate between :start and :end)").setParameter("state", state)
			.setParameter("start", start.toDate()).setParameter("end", end.toDate()).getResultList();

	}

	@Override
	public DroughtMonitorArea computeForDroughtImpact(long impactId) {
		
		ImpactBean impact = manager.find(ImpactBean.class, impactId);
		if ( impact == null ) { 
			throw new RuntimeException("no impact found for id "+impactId);
		}
		
		DateTime start = new DateTime(impact.getStartDate());		
		DateTime end = (impact.getEndDate() == null) ? new DateTime(System.currentTimeMillis()) : new DateTime(impact.getEndDate());

		ArrayList<DroughtMonitorArea> descriptors = new ArrayList<DroughtMonitorArea>();
		Set<USCounty> seen_counties = new TreeSet<USCounty>();

		for ( SpatialReference ref : impact.getSpatialReferences() ) { 
			switch ( ref.getReferenceType() ) { 
				case US_COUNTY:
					if ( !seen_counties.contains(ref.getCounty()) ) {
						descriptors.addAll(queryCountyDMSequence(ref.getCounty(), start, end));
						seen_counties.add(ref.getCounty());
					}
					break;
				case US_STATE:
					descriptors.addAll(queryStateDMSequence(ref.getState(), start, end));
					break;
				case US_CITY:
					if ( !seen_counties.contains(ref.getCity().getCounty()) ) {
						descriptors.addAll(queryCountyDMSequence(ref.getCity().getCounty(), start, end));
						seen_counties.add(ref.getCity().getCounty());
					}
				default: 
					continue;
			}
		}
		
		DroughtMonitorArea composite_descriptor = new DroughtMonitorArea();
		composite_descriptor.setD0(0f);
		composite_descriptor.setD1(0f);
		composite_descriptor.setD2(0f);
		composite_descriptor.setD3(0f);
		composite_descriptor.setD4(0f);
		composite_descriptor.setUnclassified(0f);
		
		
		for ( DroughtMonitorArea descriptor : descriptors ) { 
			composite_descriptor.setD0(composite_descriptor.getD0() + descriptor.getD0());
			composite_descriptor.setD1(composite_descriptor.getD1() + descriptor.getD1());
			composite_descriptor.setD2(composite_descriptor.getD2() + descriptor.getD2());
			composite_descriptor.setD3(composite_descriptor.getD3() + descriptor.getD3());
			composite_descriptor.setD4(composite_descriptor.getD4() + descriptor.getD4());			
			composite_descriptor.setUnclassified(composite_descriptor.getUnclassified() + descriptor.getUnclassified());			
		}

		if ( descriptors.size() > 0 ) {
			composite_descriptor.setD0(composite_descriptor.getD0() / descriptors.size());
			composite_descriptor.setD1(composite_descriptor.getD1() / descriptors.size());
			composite_descriptor.setD2(composite_descriptor.getD2() / descriptors.size());
			composite_descriptor.setD3(composite_descriptor.getD3() / descriptors.size());
			composite_descriptor.setD4(composite_descriptor.getD4() / descriptors.size());			
			composite_descriptor.setUnclassified(composite_descriptor.getUnclassified() / descriptors.size());
		}
		
		return composite_descriptor;
	}
	
	@Override
	public Layer getDroughtMonitorLayerForDate(DateTime date) {
		date = roundDMDate(date);
		
		try { 
			List<DroughtMonitorDescriptor> descriptors = manager.createQuery("select distinct d from DroughtMonitorDescriptor d where d.mapDate = " +
					":date").setParameter("date", date.toDate()).getResultList();
			
			if ( descriptors.size() == 0 ) { 
				LOG.warn("No map was found for the date "+date.toString());
				return null;
			} else if ( descriptors.size() > 1 ) { 
				LOG.warn("More than one map was returned for date "+date.toString()+" the first map will be used.");
			}
			
			DroughtMonitorDescriptor descriptor = descriptors.get(0);
			

			
		 	// instantiate the lazy relationship in presistence context
			descriptor.getD0Polygons().size();
			descriptor.getD1Polygons().size();
			descriptor.getD2Polygons().size();
			descriptor.getD3Polygons().size();
			descriptor.getD4Polygons().size();
			
			DateTimeFormatter format = DateTimeFormat.forPattern("MMM dd, yyyy");
			
			List<Style> dm_styles = getDMStyles("99");
			Layer layer = new Layer("US Drought Monitor - "+format.print(date));
			layer.addRegion(new Region(0, descriptor.getD0Polygons(), dm_styles.get(0), "D0 Area", "Abnormally Dry"));
			layer.addRegion(new Region(1, descriptor.getD1Polygons(), dm_styles.get(1), "D1 Area", "Drought - Moderate"));
			layer.addRegion(new Region(2, descriptor.getD2Polygons(), dm_styles.get(2), "D2 Area", "Drought - Severe"));
			layer.addRegion(new Region(3, descriptor.getD3Polygons(), dm_styles.get(3), "D3 Area", "Drought - Extreme"));
			layer.addRegion(new Region(4, descriptor.getD4Polygons(), dm_styles.get(4), "D4 Area", "Drought - Exceptional"));
			
			return layer;
			
		} catch ( Exception e ) { 
			LOG.error("Error creating DM layer", e);
			RuntimeException re = new RuntimeException("The layer could not be created");
			re.initCause(e);
			throw re;
		}
	}
	
	
	
	@Override
	public String getDroughtMonitorKmlForDate(DateTime date, float opacity) {
		
		if ( opacity < 0 || opacity > 1 ) { 
			throw new RuntimeException("the opacity must be a value between 0 and 1");
		}
		
		String alpha = Integer.toHexString((int)(255 * opacity));
		
		DateTimeFormatter format = DateTimeFormat.forPattern("MMM dd, yyyy");
		
		date = roundDMDate(date);
		Connection conn = null;
		ResultSet polygons = null;
		ResultSet descriptor = null;
		
		try { 
			conn = source.getConnection();
			
			PreparedStatement descriptor_query = conn.prepareStatement("select descriptor_id from DroughtMonitorDescriptors where map_date = ?");
			descriptor_query.setDate(1, new java.sql.Date(date.getMillis()));
			
			descriptor = descriptor_query.executeQuery();
			
			if ( !descriptor.next() ) { 
				LOG.warn("No map was found for the date "+date.toString());
				return null;
			}
			
			long id = descriptor.getLong(1);
			
			StringWriter out = new StringWriter();
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
			writer.writeStartDocument();
			
			writer.writeStartElement("kml");
			writer.writeAttribute("xmlns", "http://www.opengis.net/kml/2.2");
			writer.writeStartElement("Document");
			writer.writeStartElement("name");
			writer.writeCharacters("US Drought Monitor - "+format.print(date));
			writer.writeEndElement();
			
			ArrayList<PreparedStatement> polygon_queries = new ArrayList<PreparedStatement>();
			
			PreparedStatement polygon_query = conn.prepareStatement("select type, array_string, polygon_id from PolygonPointArrays inner join Polygons on " +
					"PolygonPointArrays.polygon = Polygons.polygon_id where Polygons.d0 = ? order by polygon_id, type asc");
			polygon_query.setLong(1, id);
			polygon_queries.add(polygon_query);
			
			polygon_query = conn.prepareStatement("select type, array_string, polygon_id from PolygonPointArrays inner join Polygons on " +
					"PolygonPointArrays.polygon = Polygons.polygon_id where Polygons.d1 = ? order by polygon_id, type asc");
			polygon_query.setLong(1, id);
			polygon_queries.add(polygon_query);
			
			polygon_query = conn.prepareStatement("select type, array_string, polygon_id from PolygonPointArrays inner join Polygons on " +
					"PolygonPointArrays.polygon = Polygons.polygon_id where Polygons.d2 = ? order by polygon_id, type asc");
			polygon_query.setLong(1, id);
			polygon_queries.add(polygon_query);
			
			polygon_query = conn.prepareStatement("select type, array_string, polygon_id from PolygonPointArrays inner join Polygons on " +
					"PolygonPointArrays.polygon = Polygons.polygon_id where Polygons.d3 = ? order by polygon_id, type asc");
			polygon_query.setLong(1, id);
			polygon_queries.add(polygon_query);
			
			polygon_query = conn.prepareStatement("select type, array_string, polygon_id from PolygonPointArrays inner join Polygons on " +
					"PolygonPointArrays.polygon = Polygons.polygon_id where Polygons.d4 = ? order by polygon_id, type asc");
			polygon_query.setLong(1, id);
			polygon_queries.add(polygon_query);

			for ( Style s : getDMStyles(alpha) ) { 
				writer.writeStartElement("Style");
				writer.writeAttribute("id", String.valueOf(s.getId()));
				
				writer.writeStartElement("LabelStyle");
				if ( s.getLabelColor() != null ) {
					writer.writeStartElement("color");
					writer.writeCharacters(s.getLabelColor());
					writer.writeEndElement(); // end color	
				}
				
				if ( s.getLabelScale() != null ) {
					writer.writeStartElement("scale");
					writer.writeCharacters(s.getLabelScale());
					writer.writeEndElement(); // end scale
				}
				writer.writeEndElement(); // end labelstyle
				
				writer.writeStartElement("LineStyle");
				if ( s.getLineColor() != null ) {
					writer.writeStartElement("color");
					writer.writeCharacters(s.getLineColor());
					writer.writeEndElement(); // end color	
				}
				
				if ( s.getLineWidth() != null ) {
					writer.writeStartElement("width");
					writer.writeCharacters(s.getLineWidth());
					writer.writeEndElement(); // end width
				}
				writer.writeEndElement(); // end linestyle
				
				writer.writeStartElement("PolyStyle");
				if ( s.getPolyColor() != null ) {
					writer.writeStartElement("color");
					writer.writeCharacters(s.getPolyColor());
					writer.writeEndElement(); // end color
				} 
				
				writer.writeStartElement("outline");
				if ( s.getPolyOutline() ) {
					writer.writeCharacters("1");
				} else { 
					writer.writeCharacters("0");
				}
				writer.writeEndElement(); // end outline

				writer.writeStartElement("fill");
				if ( s.getPolyFill() ) {
					writer.writeCharacters("1");
				} else { 
					writer.writeCharacters("0");
				}
				writer.writeEndElement(); // end outline
				
				writer.writeEndElement(); // end polystyle
				writer.writeEndElement(); // end style
			}
			
			for ( int i=0; i<5; i++ ) { 
				PreparedStatement stmt = polygon_queries.get(i);
				polygons = stmt.executeQuery();
				
				writer.writeStartElement("Folder");
				writer.writeStartElement("name");
				writer.writeCharacters("D"+i+" Areas");
				writer.writeEndElement();
					
				writer.writeStartElement("Placemark");
	
				writer.writeStartElement("styleUrl");
				writer.writeCharacters("#d"+i+"Style");
				writer.writeEndElement();
				
				writer.writeStartElement("name");
				writer.writeCharacters("D"+i+" Area");
				writer.writeEndElement();
				
				writer.writeStartElement("description");

				switch ( i ) { 
				case 0:
					writer.writeCData("Abnormally Dry");
					break;
				case 1: 
					writer.writeCData("Drought - Moderate");
					break;
				case 2: 
					writer.writeCData("Drought - Severe");
					break;
				case 3: 
					writer.writeCData("Drought - Extreme");
					break;
				case 4:
					writer.writeCData("Drought - Exceptional");
					break;
				}
				
				writer.writeEndElement();
				
				long old_id = -1;
				long current_id;
				writer.writeStartElement("MultiGeometry");
				while ( polygons.next() ) {
					current_id = polygons.getLong(3);
					if ( current_id != old_id ) {
						
						if ( old_id != -1 ) {
							writer.writeEndElement(); // close the polygon
						}
						old_id = current_id;
						
						writer.writeStartElement("Polygon");
						writer.writeStartElement("tessellate");
						writer.writeCharacters("1");
						writer.writeEndElement();
						writer.writeStartElement("extrude");
						writer.writeCharacters("0");
						writer.writeEndElement();
						writer.writeStartElement("altitudeMode");
						writer.writeCharacters("clampToGround");
						writer.writeEndElement();
					} 

					if ( polygons.getInt(1) == 0 ) { 
						writer.writeStartElement("outerBoundaryIs");
					} else { 
						writer.writeStartElement("innerBoundaryIs");
					}
					
					writer.writeStartElement("LinearRing");
					writer.writeStartElement("coordinates");
					writer.writeCharacters(polygons.getString(2));
					writer.writeEndElement(); // end coordinates
					writer.writeEndElement(); // end linearring
					writer.writeEndElement(); // end boundary
				}
				writer.writeEndElement(); // close the last polygon
				writer.writeEndElement(); // end multigeometry
				writer.writeEndElement(); // end placemark
				
				writer.writeEndElement(); // end folder
				polygons.close();
			}
			writer.writeEndElement(); // end document
			writer.writeEndElement(); // end kml
			writer.writeEndDocument();
			
			return out.toString();
		} catch ( Exception e ) { 
			LOG.error("Error creating DM layer", e);
			RuntimeException re = new RuntimeException("The layer could not be created");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				polygons.close();
				descriptor.close();
				conn.close();
			} catch ( Exception ex ) { 
				LOG.warn("A connection could not be closed", ex);
			}
		}
	}

	/* DM maps are valid on Tuesday of a week but not published until Thursday, 
	 * therefore the calculation is if the date is Thursday or after of 'this' 
	 * week, round to Tuesday of 'this' week, otherwise to Tuesday of last week.
	 * If the date falls on or after Tuesday of some week in the past, round 
	 * to Tuesday of that week.  Otherwise, round to Tuesday of the previous
	 * week.
	 * 
	 * Basically: this week, the map isn't published until Thursday, so we use
	 * last weeks map for two extra days while it is published. 
	 */
	public static DateTime roundDMDate(DateTime theDate) { 
		DateTime rounded_date = theDate;
		DateTime now = new DateTime(System.currentTimeMillis());
		boolean this_week = ( now.getWeekOfWeekyear() == rounded_date.getWeekOfWeekyear() && now.getYear() == rounded_date.getYear() );
		
		
		// the date is part of a week in the past
		if ( !this_week && rounded_date.getDayOfWeek() < DateTimeConstants.TUESDAY ) { 
			rounded_date = rounded_date.minusWeeks(1);
		} else if ( this_week && rounded_date.getDayOfWeek() < DateTimeConstants.THURSDAY ) {
			rounded_date = rounded_date.minusWeeks(1);
		}
			
		// now, round to Tuesday
		rounded_date = rounded_date.minusDays(rounded_date.getDayOfWeek() - DateTimeConstants.TUESDAY);
		
		return rounded_date;
	}
	
	private List<Style> getDMStyles(String alpha) { 
		ArrayList<Style> styles = new ArrayList<Style>();
		
		Style d0_style = new Style();
		d0_style.setId("d0Style");
		d0_style.setPolyColorAlpha(alpha);
		d0_style.setPolyColorBlue("00");
		d0_style.setPolyColorGreen("ff");
		d0_style.setPolyColorRed("ff");
		styles.add(d0_style);
		
		Style d1_style = new Style();
		d1_style.setId("d1Style");
		d1_style.setPolyColorAlpha(alpha);
		d1_style.setPolyColorBlue("7f");
		d1_style.setPolyColorGreen("d3");
		d1_style.setPolyColorRed("ff");
		styles.add(d1_style);
		
		Style d2_style = new Style();
		d2_style.setId("d2Style");
		d2_style.setPolyColorAlpha(alpha);
		d2_style.setPolyColorBlue("00");
		d2_style.setPolyColorGreen("aa");
		d2_style.setPolyColorRed("ff");
		styles.add(d2_style);
		
		Style d3_style = new Style();
		d3_style.setId("d3Style");
		d3_style.setPolyColorAlpha(alpha);
		d3_style.setPolyColorBlue("00");
		d3_style.setPolyColorGreen("00");
		d3_style.setPolyColorRed("ff");
		styles.add(d3_style);
		
		Style d4_style = new Style();
		d4_style.setId("d4Style");
		d4_style.setPolyColorAlpha(alpha);
		d4_style.setPolyColorBlue("00");
		d4_style.setPolyColorGreen("00");
		d4_style.setPolyColorRed("73");
		styles.add(d4_style);
		
		return styles;
	}
}
