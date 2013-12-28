/* Created on Aug 6, 2009 */
package edu.unl.act.rma.service.site;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

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
 */
@Path("/stations/Nearby")
public class NearbyStations {

	private static final Logger LOG = LogManager.getLogger(Loggers.APPLICATION_LOG, NearbyStations.class);
	
	@GET
	@Produces("text/xml")
	@Path("/zip/{zipCode}")
	public String stationsFromZip(@PathParam("zipCode") String zipCode) { 
		
		ClimateSpatialExtension spatial_query; 
		ClimateMetaDataQuery meta_query;
		try { 
			spatial_query = ClimateServiceAccessor.getInstance().getSpatialExtension();
			meta_query = ClimateServiceAccessor.getInstance().getClimateMetaDataQuery();
		} catch ( Exception e ) { 
			LOG.error("An error occured while getting the query object" ,e);
			RuntimeException re = new RuntimeException("Could not get the query object");
			re.initCause(e);
			throw re;
		}
		
		List<String> stations;
		MetaDataCollection<StationMetaDataType> meta_data;
		try {
			stations = spatial_query.getStationsByZipCode(zipCode, 5);
			meta_data = meta_query.getAllMetaData(stations, CalendarPeriod.WEEKLY);
		} catch ( Exception e ) { 
			LOG.error("An error occured getting station data", e);
			RuntimeException re = new RuntimeException("Could not get the station data");
			re.initCause(e);
			throw re;
		}
		
		StringWriter out = new StringWriter();
		try { 
			XMLStreamWriter writer;
			
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
			writer.writeStartDocument();
			writer.writeStartElement("NearbyStations");
			writer.writeAttribute("queryParam", "zipCode");
			
			for ( String station : stations ) { 
				Map<StationMetaDataType, Object> station_meta = meta_data.getStationMetaData(station);
				
				writer.writeStartElement("Station");
				writer.writeAttribute("ID", station);
				
				writer.writeStartElement("NetworkID");
				writer.writeCData((String)station_meta.get(StationMetaDataType.NETWORK_ID));
				writer.writeEndElement();
				
				writer.writeStartElement("StationName");
				writer.writeCData((String)station_meta.get(StationMetaDataType.STATION_NAME));
				writer.writeEndElement();
				
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
