/* Created on Aug 25, 2009 */
package edu.unl.act.rma.service.mobile;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.unl.act.rma.firm.climate.component.ClimateMetaDataQuery;
import edu.unl.act.rma.firm.climate.component.ClimateSpatialExtension;
import edu.unl.act.rma.firm.climate.configuration.ClimateServiceAccessor;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.MetaDataCollection;
import edu.unl.act.rma.firm.core.StationMetaDataType;

/**
 * 
 * @author Ian Cottingham
 * 
 */
@Path("/stations/list")
public class WeatherStationListService {
	private static final long serialVersionUID = 1L;
       
    private static final Logger LOG = LogManager.getLogger(Loggers.APPLICATION_LOG, WeatherStationListService.class);
	
    @GET
    @Path("/coords/{latitude}/{longitude}/{distance}")
    @Produces("text/xml")
    public String getStationList(@PathParam("latitude") float lat, 
    		@PathParam("longitude") float lon, 
    		@PathParam("distance") int distance) throws IOException {
		
    	List<String> station_ids = null;
		try { 
			ClimateSpatialExtension query = ClimateServiceAccessor.getInstance().getSpatialExtension();
			station_ids = query.getStationsFromPoint(lat, lon, distance);
		} catch ( Exception e ) { 
			LOG.error("could not query the stations", e);
			IOException ioe = new IOException("could not query the stations");
			ioe.initCause(e);
			throw ioe;
		}
		
		Document doc; 
		try { 
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch ( Exception e ) { 
			LOG.error("could not create an XML document");
			IOException se = new IOException("could not create an XML document");
			se.initCause(e);
			throw se;
		}
		
		try { 
			ClimateMetaDataQuery meta_query = ClimateServiceAccessor.getInstance().getClimateMetaDataQuery();
			MetaDataCollection<StationMetaDataType> meta_data = meta_query.getAllMetaData(station_ids, CalendarPeriod.MONTHLY);
			Element root = doc.createElement("StationList");
			doc.appendChild(root);
			
			for ( String str : meta_data ) { 
				Map<StationMetaDataType, Object> meta_map = meta_data.getStationMetaData(str);
				Element station = doc.createElement("Station");
				station.setAttribute("id", str);
				
				Element node = doc.createElement("Name");
				node.appendChild(doc.createCDATASection((String)meta_map.get(StationMetaDataType.STATION_NAME)));
				station.appendChild(node);
				
				node = doc.createElement("Latitude");
				node.appendChild(doc.createCDATASection(String.valueOf((Float)meta_map.get(StationMetaDataType.LATITUDE))));
				station.appendChild(node);
				
				node = doc.createElement("Longitude");
				node.appendChild(doc.createCDATASection(String.valueOf((Float)meta_map.get(StationMetaDataType.LONGITUDE))));
				station.appendChild(node);
						
				root.appendChild(station);
			}
		
		} catch ( Exception e ) { 
			LOG.error("could not create the response", e);
			IOException se = new IOException("could not create the response");
			se.initCause(e);
			throw se;
		}
		
		StringWriter out = new StringWriter();
		try {
			TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(out));
		} catch ( Exception e ) { 
			LOG.error("could not create the chart XML", e);
			IOException se = new IOException();
			se.initCause(e);
			throw se;
		}
		
		return out.toString();
	}
}