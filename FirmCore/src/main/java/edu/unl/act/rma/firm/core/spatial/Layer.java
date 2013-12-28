/* Created on: May 26, 2010 */
package edu.unl.act.rma.firm.core.spatial;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Ian Cottingham
 *
 */
public class Layer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Set<Region> regions;
	private Set<Point> points;
	private Set<Style> styles;
	private String name;
	
	public Layer() { 
		regions = new TreeSet<Region>();
		points = new TreeSet<Point>();		
		styles = new TreeSet<Style>();
	}
	
	public Layer(String name) { 
		this();
		this.name = name;
	}
	
	public void addRegion(Region region) { 
		if ( region.getRegionId() == -1 ) { 
			throw new RuntimeException("The region must have a valid ID to be added to a layer");
		}
		styles.add(region.getStyle());
		regions.add(region);
	}
	
	public void addPoint(Point point) { 
		points.add(point);
	}
	
	public void setFillOpacity(float opacity) { 
		if ( opacity < 0 || opacity > 1 ) { 
			throw new RuntimeException("the opacity must be a value between 0 and 1");
		}
		
		String new_alpha = Integer.toHexString((int)(255 * opacity));
		
		for ( Style style : styles ) { 
			style.setPolyColorAlpha(new_alpha);
		}
	}
	
	public String toKml() { 
		StringWriter out = new StringWriter();
		try { 
			XMLStreamWriter writer;
			
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
			writer.writeStartDocument();
			
			writer.writeStartElement("kml");
			writer.writeAttribute("xmlns", "http://www.opengis.net/kml/2.2");
			writer.writeStartElement("Document");
			if ( name != null ) { 
				writer.writeStartElement("name");
				writer.writeCharacters(this.name);
				writer.writeEndElement();
			}
			
			for ( Style s : styles ) { 
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
			
			for ( Region r : regions ) { 

				writer.writeStartElement("Folder");
				writer.writeStartElement("name");
				writer.writeCharacters(r.extractName());
				writer.writeEndElement();
					
				writer.writeStartElement("Placemark");
	
				writer.writeStartElement("styleUrl");
				writer.writeCharacters("#"+r.getStyle().getId());
				writer.writeEndElement();
				
				writer.writeStartElement("name");
				writer.writeCharacters(r.extractName());
				writer.writeEndElement();
				
				if ( r.getDescription() != null ) {
					writer.writeStartElement("description");
					writer.writeCData(r.getDescription());
					writer.writeEndElement();
				}
				
				writer.writeStartElement("MultiGeometry");
				for ( Polygon p : r.getPolygon() ) {
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
					
					for ( PolygonPointArray array : p.getPoints() ) {
						switch ( array.getType() ) { 
						case INNER_POLYGON: 						
							writer.writeStartElement("innerBoundaryIs");
							break;
						case OUTER_POLYGON: 
							writer.writeStartElement("outerBoundaryIs");
							break;
						}

						writer.writeStartElement("LinearRing");
						writer.writeStartElement("coordinates");
						writer.writeCharacters(array.getArrayString());
						writer.writeEndElement(); // end coordinates
						writer.writeEndElement(); // end linearring
						writer.writeEndElement(); // end innerboundary
					}
				
					writer.writeEndElement(); // end polygon
				}
				
				writer.writeEndElement(); // end multigeometry
				writer.writeEndElement(); // end placemark
				
				writer.writeEndElement(); // end folder
			}
			
			for ( Point point : points ) { 
				writer.writeStartElement("Folder");
				writer.writeStartElement("name");
				writer.writeCharacters(point.getPlaceName());
				writer.writeEndElement();
				
				writer.writeStartElement("Placemark");

				if ( point.getDescription() != null ) {
					writer.writeStartElement("description");
					writer.writeCData(point.getDescription());
					writer.writeEndElement();
				}
				writer.writeStartElement("Point");
				
				writer.writeStartElement("coordinates");
				writer.writeCharacters(point.getLongitude()+","+point.getLatitude());
				writer.writeEndElement(); 
				
				writer.writeEndElement(); // end point
				writer.writeEndElement(); // end placemark

				writer.writeEndElement(); // end folder
			}
			
			writer.writeEndElement(); // end document
			writer.writeEndElement(); // end kml
			writer.writeEndDocument();
			
			return out.toString();
			
		} catch ( Exception e ) { 
			RuntimeException re = new RuntimeException("The KML document could not be generated, an error occurred");
			re.initCause(e);
			throw re;
		}
	}
	
}
