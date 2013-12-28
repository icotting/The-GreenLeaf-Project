package edu.unl.act.rma.service.droughttools;

import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;

import com.thoughtworks.xstream.XStream;

import edu.unl.act.rma.firm.climate.component.ClimateDataQuery;
import edu.unl.act.rma.firm.climate.component.ClimateMetaDataQuery;
import edu.unl.act.rma.firm.climate.component.ClimateSpatialExtension;
import edu.unl.act.rma.firm.climate.configuration.ClimateServiceAccessor;
import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarDataParser;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.InvalidStateException;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.MetaDataCollection;
import edu.unl.act.rma.firm.core.StationMetaDataType;
import edu.unl.act.rma.firm.core.StationSearchTerms;
import edu.unl.act.rma.firm.core.component.SpatialQuery;
import edu.unl.act.rma.firm.core.configuration.SpatialServiceAccessor;
import edu.unl.act.rma.firm.core.spatial.BoundingBox;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.drought.NsmCompleteDTO;
import edu.unl.act.rma.firm.drought.NsmSummaryDTO;
import edu.unl.act.rma.firm.drought.component.SoilsDataQuery;
import edu.unl.act.rma.firm.drought.configuration.DroughtServiceAccessor;
import edu.unl.act.rma.firm.drought.index.KeetchByramDroughtIndex;
import edu.unl.act.rma.firm.drought.index.NewhallSimulationModel;
import edu.unl.act.rma.firm.drought.index.PalmerDroughtSeverityIndex;
import edu.unl.act.rma.firm.drought.index.StandardizedPrecipitationIndex;

@Path("/drought")
public class DroughtService {

	private final XStream streamHandler;
	private Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG,
			DroughtService.class);

	public DroughtService() {
		streamHandler = new XStream();
		streamHandler.processAnnotations(Location.class);
		streamHandler.processAnnotations(Station.class);
	}

	@Path("/getLocation")
	@GET
	@Produces("text/xml")
	public String getLocation(@QueryParam("Latitude") String latitude,
			@QueryParam("Longitude") String longitude) throws RemoteException,
			InstantiationException {

		float lat = Float.valueOf(latitude);
		float lon = Float.valueOf(longitude);

		Location location = new Location();

		location.setLatitude(lat);
		location.setLongitude(lon);

		BoundingBox bb = new BoundingBox(lat, lon, lat, lon);

		SpatialQuery sq = SpatialServiceAccessor.getInstance()
				.getSpatialQuery();
		USState state;
		USCounty county;
		String stateName = "not defined";
		String countyName = "not defined";
		Set<USState> stateArray;
		Set<USCounty> countyArray;

		try {
			stateArray = sq.getStatesByRegion(bb);
			countyArray = sq.getCountiesByRegion(bb);
		} catch (RemoteException e) {
			return streamHandler.toXML("error");
		}

		if (!stateArray.isEmpty()) {
			state = (USState) stateArray.toArray()[0];
			stateName = state.name();
		}
		if (!countyArray.isEmpty()) {
			county = (USCounty) countyArray.toArray()[0];
			countyName = county.getName();
		}
		location.setCounty(countyName);
		location.setState(stateName);

		return streamHandler.toXML(location);
	}

	@Path("/doPdsi")
	@POST
	@Consumes(MediaType.TEXT_XML)
	@Produces("text/xml")
	public String doPdsi(String body) throws InvalidStateException {
		Station station = (Station) streamHandler.fromXML(body);
		PalmerDroughtSeverityIndex pdsi = new PalmerDroughtSeverityIndex(
				station.getStartYear());
		pdsi.setData(station.getPrecipitationData(), station
				.getTemperatureData(), station.getTemperatureAverage(), station
				.getLocation().getLatitude(), station.getAwc());
		float[][] result;
		if (station.getPrecipitationData()[0].length == 12) {
			result = pdsi.scMonthlyPDSI();
		} else if (station.getPrecipitationData()[0].length == 52
				|| station.getPrecipitationData()[0].length == 53) {
			result = pdsi.weeklyPDSI(1);
		} else {
			return "invalid data";
		}
		return streamHandler.toXML(result);
	}

	@Path("/doSpi")
	@POST
	@Consumes(MediaType.TEXT_XML)
	@Produces("text/xml")
	public String doSpi(String body) throws InvalidStateException {
		Station station = (Station) streamHandler.fromXML(body);
		StandardizedPrecipitationIndex spi = new StandardizedPrecipitationIndex(
				(int) station.getPrecipitationData()[0][0]);
		spi.setData(station.getPrecipitationData());
		float[][] result;
		result = spi.computeSpi(1);

		return streamHandler.toXML(result);
	}

	@Path("/doNsmComplete")
	@POST
	@Consumes(MediaType.TEXT_XML)
	@Produces("text/xml")
	public String doNsmComplete(String body) throws InvalidStateException {
		Station station = (Station) streamHandler.fromXML(body);
		NewhallSimulationModel nsm = new NewhallSimulationModel(station
				.getStartYear(), station.getEndYear());
		nsm.setData(station.getPrecipitationData(), station
				.getTemperatureData(), station.getLocation().getLatitude(),
				station.getAwc());
		NsmCompleteDTO result = nsm.computeCompleteNsm();

		return streamHandler.toXML(result);
	}

	@Path("/doNsmSummary")
	@POST
	@Consumes(MediaType.TEXT_XML)
	@Produces("text/xml")
	public String doNsmSummary(String body) throws InvalidStateException {
		Station station = (Station) streamHandler.fromXML(body);
		NewhallSimulationModel nsm = new NewhallSimulationModel(station
				.getStartYear(), station.getEndYear());
		nsm.setData(station.getPrecipitationData(), station
				.getTemperatureData(), station.getLocation().getLatitude(),
				station.getAwc());
		NsmSummaryDTO result = nsm.computeSummaryNsm();
		return streamHandler.toXML(result);
	}

	@Path("/doKbdi")
	@POST
	@Consumes(MediaType.TEXT_XML)
	@Produces("text/xml")
	public String doKbdi(String body) throws InvalidArgumentException,
			RemoteException {
		Station station = (Station) streamHandler.fromXML(body);

		float[][] precipData = station.getPrecipitationData();
		float[][] highTempData = station.getHighTemperatureData();
		// find first date with precip and high temp data
		int firstDayIndex = 0;
		boolean found = false;
		for (int i = 0; i < precipData[0].length; i++) {
			if (precipData[0][i] > -99 && !found) {
				firstDayIndex = i;
				found = true;
			}
		}
		found = false;
		for (int i = 0; i < highTempData[0].length; i++) {
			if (highTempData[0][i] > -99 && !found) {
				if (i > firstDayIndex) {
					firstDayIndex = i;
				}
				found = true;
			}
		}
		DateTime first = new DateTime(station.getStartYear(), 1, 1, 0, 0, 0, 0);
		first = first.plusDays(firstDayIndex);
		// KBDI requires 6-month leadin
		first = first.plusMonths(6);

		// find last date with precip and high temp data
		int lastYearIndex = 0;
		int lastDayIndex = 0;
		found = false;
		for (int i = precipData.length - 1; i > -1; i--) {
			for (int j = precipData[i].length - 1; j > -1; j--) {
				if (precipData[i][j] > -99 && !found) {
					lastYearIndex = i;
					lastDayIndex = j;
					found = true;
				}
			}
		}

		found = false;
		for (int i = highTempData.length - 1; i > -1; i--) {
			for (int j = highTempData[i].length - 1; j > -1; j--) {
				if (highTempData[i][j] > -99 && !found) {
					if ((i < lastYearIndex)
							|| (i == lastYearIndex && j < lastDayIndex)) {
						lastYearIndex = i;
						lastDayIndex = j;
					}
					found = true;
				}
			}
		}

		DateTime last = new DateTime(station.getStartYear(), 1, 1, 0, 0, 0, 0);
		last = last.plusYears(lastYearIndex);
		last = last.plusDays(lastDayIndex);
		float csm = SoilDataCalculation.getCurrentSoilMoisture(station, last);

		KeetchByramDroughtIndex kbdi = new KeetchByramDroughtIndex();
		float[][] result = kbdi.compute(station
				.getStartYear(), station.getPrecipitationData(), station
				.getHighTemperatureData(), SoilDataCalculation
				.calculateAnnualAverage(station.getPrecipitationData()), csm,
				station.getAwc(), first, last);

		return streamHandler.toXML(result);
	}


	@Path("/getMissingData")
	@POST
	@Consumes(MediaType.TEXT_XML)
	@Produces("text/xml")
	public String getMissingData(String body) throws InvalidStateException {

		Station station = (Station) streamHandler.fromXML(body);
		DateTime start = new DateTime(station.getStartYear(), 1, 1, 0, 0, 0, 0);

		float[] precipitationData = station.getPrecipDataArray();
		float[] temperatureData = station.getTempDataArray();
		float[] aveTempData = station.getTemperatureAverage();
		float[][] allData = new float[6][];

		if (null != precipitationData && precipitationData.length > 0) {
			CalendarDataParser cdp = new CalendarDataParser(precipitationData,
					start.getYear(), start.getMonthOfYear(), start
							.getDayOfMonth());
			ArrayList<Float> weeklyPrecipData = new ArrayList<Float>();
			while (cdp.hasNextWeek()) {
				weeklyPrecipData.add(cdp.nextWeekSum());
			}
			float[] weeklyPrecipCopy = new float[weeklyPrecipData.size()];
			for (int i = 0; i < weeklyPrecipData.size(); i++) {
				weeklyPrecipCopy[i] = weeklyPrecipData.get(i);
			}

			cdp = new CalendarDataParser(precipitationData, start.getYear(),
					start.getMonthOfYear(), start.getDayOfMonth());
			ArrayList<Float> monthlyPrecipData = new ArrayList<Float>();
			while (cdp.hasNextMonth()) {
				monthlyPrecipData.add(cdp.nextMonthSum());
			}
			float[] monthlyPrecipCopy = new float[monthlyPrecipData.size()];
			for (int i = 0; i < monthlyPrecipData.size(); i++) {
				monthlyPrecipCopy[i] = monthlyPrecipData.get(i);
			}

			allData[0] = weeklyPrecipCopy;
			allData[3] = monthlyPrecipCopy;
		}

		if (null != temperatureData && temperatureData.length > 0) {
			CalendarDataParser cdp = new CalendarDataParser(temperatureData,
					start.getYear(), start.getMonthOfYear(), start
							.getDayOfMonth());
			ArrayList<Float> weeklyTempData = new ArrayList<Float>();
			while (cdp.hasNextWeek()) {
				weeklyTempData.add(cdp.nextWeekAverage());
			}
			float[] weeklyTempCopy = new float[weeklyTempData.size()];
			for (int i = 0; i < weeklyTempData.size(); i++) {
				weeklyTempCopy[i] = weeklyTempData.get(i);
			}

			cdp = new CalendarDataParser(temperatureData, start.getYear(),
					start.getMonthOfYear(), start.getDayOfMonth());
			ArrayList<Float> monthlyTempData = new ArrayList<Float>();
			while (cdp.hasNextMonth()) {
				monthlyTempData.add(cdp.nextMonthAverage());
			}
			float[] monthlyTempCopy = new float[monthlyTempData.size()];
			for (int i = 0; i < monthlyTempData.size(); i++) {
				monthlyTempCopy[i] = monthlyTempData.get(i);
			}

			allData[1] = weeklyTempCopy;
			allData[4] = monthlyTempCopy;
		}

		if (null != aveTempData && aveTempData.length > 0) {
			CalendarDataParser cdp = new CalendarDataParser(aveTempData, start
					.getYear(), start.getMonthOfYear(), start.getDayOfMonth());
			ArrayList<Float> weeklyAveTempData = new ArrayList<Float>();
			while (cdp.hasNextWeek()) {
				weeklyAveTempData.add(cdp.nextWeekAverage());
			}
			float[] weeklyAveTempCopy = new float[weeklyAveTempData.size()];
			for (int i = 0; i < weeklyAveTempData.size(); i++) {
				weeklyAveTempCopy[i] = weeklyAveTempData.get(i);
			}

			cdp = new CalendarDataParser(aveTempData, start.getYear(), start
					.getMonthOfYear(), start.getDayOfMonth());
			ArrayList<Float> monthlyAveTempData = new ArrayList<Float>();
			while (cdp.hasNextMonth()) {
				monthlyAveTempData.add(cdp.nextMonthAverage());
			}
			float[] monthlyAveTempCopy = new float[monthlyAveTempData.size()];
			for (int i = 0; i < monthlyAveTempData.size(); i++) {
				monthlyAveTempCopy[i] = monthlyAveTempData.get(i);
			}

			allData[2] = weeklyAveTempCopy;
			allData[5] = monthlyAveTempCopy;
		}

		return streamHandler.toXML(allData);
	}

	@Path("/station/{stationID}")
	@GET
	@Produces("text/xml")
	public String getStation(@PathParam("stationID") String stationID) {
		Station monthly = new Station();
		Station weekly = new Station();
		Station daily = new Station();
		try {
			ClimateDataQuery cdq = ClimateServiceAccessor.getInstance()
					.getClimateDataQuery();

			List<String> stationIds = new ArrayList<String>();
			stationIds.add(stationID);
			ClimateMetaDataQuery cmdq = ClimateServiceAccessor.getInstance()
					.getClimateMetaDataQuery();
			MetaDataCollection<StationMetaDataType> mdc = cmdq.getAllMetaData(
					stationIds, CalendarPeriod.MONTHLY);
			Map<StationMetaDataType, Object> metadata = mdc
					.getStationMetaData(stationID);
			monthly
					.setId((String) metadata
							.get(StationMetaDataType.STATION_ID));
			monthly.setName((String) metadata
					.get(StationMetaDataType.STATION_NAME));

			Location loc = new Location();
			loc.setLatitude((Float) metadata.get(StationMetaDataType.LATITUDE));
			loc.setLongitude((Float) metadata
					.get(StationMetaDataType.LONGITUDE));
			loc.setCounty((String) metadata.get(StationMetaDataType.COUNTY));
			loc.setState((String) metadata.get(StationMetaDataType.STATE));
			monthly.setLocation(loc);

			SoilsDataQuery sdq = DroughtServiceAccessor.getInstance()
					.getSoilsDataQuery();
			monthly.setAwc((Float) sdq.getWaterHoldingCapacity(stationIds).get(
					stationID));

			monthly.setStartYear(((DateTime) metadata
					.get(StationMetaDataType.START_DATE)).get(DateTimeFieldType
					.year()));
			monthly.setEndYear(((DateTime) metadata
					.get(StationMetaDataType.END_DATE)).get(DateTimeFieldType
					.year()));

			CalendarDataCollection cdcTempMonthly = cdq.getAvailableData(
					stationIds, DataType.NORMAL_TEMP, CalendarPeriod.MONTHLY);
			CalendarDataCollection cdcPrecipMonthly = cdq.getAvailableData(
					stationIds, DataType.PRECIP, CalendarPeriod.MONTHLY);
			CalendarDataCollection cdcHighTempMonthly = cdq.getAvailableData(
					stationIds, DataType.HIGH_TEMP, CalendarPeriod.MONTHLY);

			monthly.setTemperatureData(cdcTempMonthly.result.get(stationID));
			monthly
					.setPrecipitationData(cdcPrecipMonthly.result
							.get(stationID));
			monthly.setHighTemperatureData(cdcHighTempMonthly.result
					.get(stationID));

			CalendarDataCollection cdcTempWeekly = cdq.getAvailableData(
					stationIds, DataType.NORMAL_TEMP, CalendarPeriod.WEEKLY);
			CalendarDataCollection cdcPrecipWeekly = cdq.getAvailableData(
					stationIds, DataType.PRECIP, CalendarPeriod.WEEKLY);
			CalendarDataCollection cdcHighTempWeekly = cdq.getAvailableData(
					stationIds, DataType.HIGH_TEMP, CalendarPeriod.WEEKLY);

			weekly.setTemperatureData(cdcTempWeekly.result.get(stationID));
			weekly.setPrecipitationData(cdcPrecipWeekly.result.get(stationID));
			weekly.setHighTemperatureData(cdcHighTempWeekly.result
					.get(stationID));

			CalendarDataCollection cdcTempDaily = cdq.getAvailableData(
					stationIds, DataType.NORMAL_TEMP, CalendarPeriod.DAILY);
			CalendarDataCollection cdcPrecipDaily = cdq.getAvailableData(
					stationIds, DataType.PRECIP, CalendarPeriod.DAILY);
			CalendarDataCollection cdcHighTempDaily = cdq.getAvailableData(
					stationIds, DataType.HIGH_TEMP, CalendarPeriod.DAILY);

			daily.setTemperatureData(cdcTempDaily.result.get(stationID));
			daily.setPrecipitationData(cdcPrecipDaily.result.get(stationID));
			daily
					.setHighTemperatureData(cdcHighTempDaily.result
							.get(stationID));

		} catch (InstantiationException ie) {
			LOG.error("could not get data for station " + stationID, ie);
		} catch (RemoteException re) {
			LOG.error("could not get data for station " + stationID, re);
		} catch (InvalidArgumentException iae) {
			LOG.error("could not get data for station " + stationID, iae);
		}
		return "<Stations>" + streamHandler.toXML(monthly)
				+ streamHandler.toXML(weekly) + streamHandler.toXML(daily)
				+ "</Stations>";
	}

	@Path("/station/browse/{state}")
	@GET
	@Produces("text/xml")
	public String searchStationsByState(@PathParam("state") String state) {
		StationSearchTerms terms = new StationSearchTerms();
		terms.setStateFromString(state);
		return searchStations(terms);
	}

	@Path("/station/browse/{state}/{county}")
	@GET
	@Produces("text/xml")
	public String searchStationsByCounty(@PathParam("state") String state,
			@PathParam("county") String county) {
		StationSearchTerms terms = new StationSearchTerms();
		terms.setStateFromString(state);
		terms.setCounty(county);
		return searchStations(terms);
	}

	@Path("/station/search/{name}")
	@GET
	@Produces("text/xml")
	public String searchStationsByName(@PathParam("name") String name) {
		StationSearchTerms terms = new StationSearchTerms();
		terms.setStationName(name);
		return searchStations(terms);
	}

	private String searchStations(StationSearchTerms terms) {
		StringWriter out = new StringWriter();
		try {
			ClimateMetaDataQuery cmdq = ClimateServiceAccessor.getInstance()
					.getClimateMetaDataQuery();
			MetaDataCollection<StationMetaDataType> mdc = cmdq
					.findStations(terms);
			XMLStreamWriter writer;
			try {
				writer = XMLOutputFactory.newInstance().createXMLStreamWriter(
						out);
				writer.writeStartDocument();
			} catch (Exception e) {
				RuntimeException re = new RuntimeException(
						"An error occured creating the xml writer");
				re.initCause(e);
				throw re;
			}

			try {
				writer.writeStartElement("Stations");
				for (String stationId : mdc) {
					try {
						String name = (String) mdc
								.getStationMetaData(stationId).get(
										StationMetaDataType.STATION_NAME);
						String county = (String) mdc.getStationMetaData(
								stationId).get(StationMetaDataType.COUNTY);
						String state = (String) mdc.getStationMetaData(
								stationId).get(StationMetaDataType.STATE);
						if (county == null) {
							continue;
						}
						writer.writeStartElement("Station");

						writer.writeStartElement("ID");
						writer.writeCharacters(stationId);
						writer.writeEndElement(); // ID

						writer.writeStartElement("Name");
						writer.writeCharacters(name);
						writer.writeEndElement(); // Name

						writer.writeStartElement("County");
						writer.writeCharacters(county);
						writer.writeEndElement(); // County

						writer.writeStartElement("State");
						writer.writeCharacters(state);
						writer.writeEndElement(); // State

						writer.writeEndElement(); // Station
					} catch (NullPointerException npe) {
						//
					}
				}

				writer.writeEndElement(); // Stations
				writer.writeEndDocument();
			} catch (XMLStreamException xse) {
				LOG.error("could not write query response", xse);
			}
		} catch (InstantiationException ie) {
			LOG.error("could not get the search query object", ie);
		} catch (RemoteException re) {
			LOG.error("could not get the search query object", re);
		}
		return out.toString();
	}

	@Path("/counties/{state}")
	@GET
	@Produces("text/xml")
	public String listCounties(@PathParam("state") String stateCode) {
		USState state = USState.fromPostalCode(stateCode);
		SpatialQuery sq = null;
		Set<USCounty> counties = null;

		try {
			sq = SpatialServiceAccessor.getInstance().getSpatialQuery();
			counties = sq.getCountiesByState(state);
		} catch (InstantiationException ie) {
			LOG.error("could not get the search query object", ie);
		} catch (RemoteException re) {
			LOG.error("could not get the search query object", re);
		}

		StringWriter out = new StringWriter();

		XMLStreamWriter writer;
		try {
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
			writer.writeStartDocument();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(
					"An error occured creating the xml writer");
			re.initCause(e);
			throw re;
		}

		try {
			writer.writeStartElement("State");
			writer.writeAttribute("Name", state.name());
			for (USCounty county : counties) {
				writer.writeStartElement("County");
				writer.writeCharacters(county.getName());
				writer.writeEndElement(); // County
			}
			writer.writeEndElement(); // State
			writer.writeEndDocument();
		} catch (XMLStreamException xse) {
			LOG.error("could not write query response", xse);
		}
		return out.toString();
	}

	@Path("/getAwc")
	@GET
	@Produces("text/plain")
	public String getAwc(@QueryParam("Latitude") String latitude,
			@QueryParam("Longitude") String longitude) {
		float lat = Float.valueOf(latitude);
		float lon = Float.valueOf(longitude);
		ClimateMetaDataQuery cmdq = null;
		try {
			ClimateSpatialExtension cseb = ClimateServiceAccessor.getInstance()
					.getSpatialExtension();
			SoilsDataQuery sdq = DroughtServiceAccessor.getInstance()
					.getSoilsDataQuery();
			List<String> stationIds = new ArrayList<String>();
			Map<String, Float> awc = new HashMap<String, Float>();
			for (int i = 0; i < 50; i++) {
				stationIds = cseb.getStationsFromPoint(lat, lon, i);
				if (stationIds.size() > 0) {
					awc = sdq.getWaterHoldingCapacity(stationIds);
					break;
				}
			}
			cmdq = ClimateServiceAccessor.getInstance()
					.getClimateMetaDataQuery();

			if (awc.size() > 0) {
				StringWriter out = new StringWriter();
				XMLStreamWriter writer;
				try {
					writer = XMLOutputFactory.newInstance()
							.createXMLStreamWriter(out);
					writer.writeStartDocument();
				} catch (Exception e) {
					RuntimeException re = new RuntimeException(
							"An error occured creating the xml writer");
					re.initCause(e);
					throw re;
				}
				String stationId = stationIds.get(0);
				float awcVal = awc.get(stationId);
				Map<StationMetaDataType, Object> meta = cmdq.getAllMetaData(
						stationIds, CalendarPeriod.MONTHLY).getStationMetaData(
						stationIds.get(0));
				String stationName = (String) meta
						.get(StationMetaDataType.STATION_NAME);
				float stationLon = (Float) meta
						.get(StationMetaDataType.LONGITUDE);
				float stationLat = (Float) meta
						.get(StationMetaDataType.LATITUDE);
				double distance = distanceBetweenPoints(lat, lon, stationLat,
						stationLon);
				try {
					writer.writeStartElement("Station");
					writer.writeStartElement("Name");
					writer.writeCharacters(stationName);
					writer.writeEndElement(); // Name
					writer.writeStartElement("Distance");
					writer.writeCharacters(String.valueOf(distance));
					writer.writeEndElement(); // Distance
					writer.writeStartElement("AWC");
					writer.writeCharacters(String.valueOf(awcVal));
					writer.writeEndElement(); // AWC
					writer.writeEndElement(); // Station
					writer.writeEndDocument();
					return out.toString();
				} catch (XMLStreamException xse) {
					LOG.error("error writing awc xml", xse);
				}
			}
		} catch (InstantiationException ie) {
			LOG.error("could not get the metadata query object", ie);
		} catch (RemoteException re) {
			LOG.error("could not get the metadata query object", re);
		} catch (InvalidArgumentException iae) {
			LOG.error("could not get query the dates", iae);
		}
		return "No nearby station";
	}

	private double distanceBetweenPoints(float lat1, float lon1, float lat2,
			float lon2) {
		return (Math.acos(Math.sin(Math.toRadians(lat1))
				* Math.sin(Math.toRadians(lat2))
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2))
				* Math.cos(Math.toRadians(lon2 - lon1))) * 3959.00f);
	}

	@Path("/testStation")
	@GET
	@Produces("text/xml")
	public String testStation() {
		Station station = new Station();
		Location location = new Location();
		location.setCounty("Lincoln");
		location.setState("Nebraska");
		location.setLatitude(41);
		location.setLongitude(-101);
		station.setAwc(23);
		station.setEndYear(2008);
		station.setId("1");
		station.setName("North Platte");
		float[][] tdarray = { { (float) .5, 23, 34 }, { 56, 67, 78 } };
		float[][] tdarray2 = { { 16, 26, 36 }, { 56, 67, 78 } };
		float[] array = { 98, 87, 76 };
		station.setPrecipitationData(tdarray);
		station.setStartYear(1990);
		station.setTemperatureAverage(array);
		station.setTemperatureData(tdarray2);
		station.setLocation(location);

		return streamHandler.toXML(station);
	}

	@Path("/check")
	@GET
	@Produces("text/xml")
	public String check() {
		return streamHandler.toXML("Valid server");
	}
	
}