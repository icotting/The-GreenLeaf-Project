package edu.unl.act.rma.console.usgs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.joda.time.IllegalFieldValueException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.component.SpatialQuery;
import edu.unl.act.rma.firm.core.configuration.SpatialServiceAccessor;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;

public class USGSDataReader {
	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG,
			USGSDataReader.class);
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");
	private static DataSource source = DataSourceInjector
			.injectDataSource(DataSourceTypes.STREAM_FLOW);
	private static DateTime historicalStart = new DateTime(1850, 1, 1, 0, 0, 0,
			0);
	public static final float MISSING = -99;

	private static String LAST_DAILY_UPDATE = "SELECT year, month_num, day_num FROM daily WHERE variable_id = ? ORDER BY year DESC, month_num DESC, day_num DESC LIMIT 1 ";

	public ArrayList<USGSResult> getDailyResults(String stationID) {
		ArrayList<USGSResult> results = new ArrayList<USGSResult>();
		PreparedStatement psLastUpdate = null;
		Connection conn = null;
		try {
			conn = source.getConnection();
			ResultSet rsLastUpdate = null;
			DateTime startDate = null;
			for (USGSVariableEnum var : USGSVariableEnum.values()) {
				try {
					psLastUpdate = conn.prepareStatement(LAST_DAILY_UPDATE);
					psLastUpdate.setString(1, stationID + ":"
							+ var.getUSGScode());
					rsLastUpdate = psLastUpdate.executeQuery();
					if (rsLastUpdate.next()) {
						try {
							startDate = new DateTime(rsLastUpdate
									.getInt("year"), rsLastUpdate
									.getInt("month_num"), rsLastUpdate
									.getInt("day_num"), 0, 0, 0, 0);
						} catch (IllegalFieldValueException ife) {
							// throws exception for non-existent 2/29 values
							if ((rsLastUpdate.getInt("year") % 4 > 0
									&& rsLastUpdate.getInt("month_num") == 2 && rsLastUpdate
									.getInt("day_num") == 29)) {
								startDate = new DateTime(rsLastUpdate
										.getInt("year"), 3, 1, 0, 0, 0, 0);
							} else {
								throw ife;
							}
						}
					} else {
						startDate = historicalStart;
					}
					try {
						results.addAll(getDailyResults(stationID, var,
								startDate, new DateTime()));
					} catch (NullPointerException npe) {
						// do nothing
					}
				} catch (SQLException sqle) {
					LOG.error("error checking for last daily update", sqle);
				}
				psLastUpdate.close();
				rsLastUpdate.close();
			}
		} catch (SQLException sqe) {
			LOG.error("error checking for last daily update", sqe);
		} finally {
			try {
				if (null != conn) {
					psLastUpdate.close();
					conn.close();
				}
			} catch (SQLException sqe) {
				LOG.error("error closing connection", sqe);
			}
		}
		return results;
	}

	protected TreeSet<DateTime> findDatesInRange(DateTime startDate,
			DateTime endDate) {
		TreeSet<DateTime> dates = new TreeSet<DateTime>();
		DateTime currentDate = startDate;
		while (currentDate.isBefore(endDate)) {
			dates.add(currentDate);
			currentDate = currentDate.plusDays(1);
		}
		dates.add(endDate);
		return dates;
	}

	public ArrayList<USGSResult> getDailyResults(String stationID,
			USGSVariableEnum variable, DateTime startDate, DateTime endDate) {
		URLConnection uconn;
		String agency = "USGS";
		String variableParameter = variable.getUSGScode().substring(0, 5);
		String statisticParameter = variable.getUSGScode().substring(6, 11);
		ArrayList<USGSResult> results = new ArrayList<USGSResult>();

		String address = "http://waterservices.usgs.gov/NWISQuery/GetDV1?SiteNum="
				+ stationID
				+ "&ParameterCode="
				+ variableParameter
				+ "&StatisticCode="
				+ statisticParameter
				+ "&AgencyCode="
				+ agency
				+ "&StartDate="
				+ dateFormat.format(startDate.toDate())
				+ "&EndDate="
				+ dateFormat.format(endDate.toDate()) + "&action=Submit";
		String variableID = stationID + ":" + variable.getUSGScode();
		StringBuffer xml = new StringBuffer();

		DateTime start = null;
		DateTime end = null;
		try {
			URL url = new URL(address);
			uconn = url.openConnection();
			BufferedReader rd = new BufferedReader(new InputStreamReader(uconn
					.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				xml.append(line);
			}
			if (xml.toString().contains("Error")) {
				return null;
			}
			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(
							new InputSource(new StringReader(xml.toString())));
			NodeList values = doc.getElementsByTagName("value");

			for (int i = 0; i < values.getLength(); i++) {
				String dateString = values.item(i).getAttributes()
						.getNamedItem("dateTime").getTextContent();
				dateString = dateString.substring(0, dateString.indexOf("T"));
				DateTime resultDate = new DateTime(dateFormat.parse(dateString));
				if (i == 0) {
					start = resultDate;
				}
				if (i == values.getLength() - 1) {
					end = resultDate;

				}
				String dailyID = variableID + ":" + dateString.replace("-", "");
				USGSResult result = new USGSResult();
				result.setDailyID(dailyID);
				result.setDate(resultDate);
				result.setValue(Double.parseDouble(values.item(i)
						.getTextContent()));
				String qual = values.item(i).getAttributes().getNamedItem(
						"qualifiers").getTextContent().trim();
				if (qual.contains(",")) {
					String[] qualSplit = qual.split(",");
					qual = qualSplit[0];
					result.setStatus(USGSDailyValueStatusCode
							.fromCode(qualSplit[1]));
				}
				result.setQualification(USGSDataValueQualificationCode
						.fromCode(qual));

				USGSVariableResult varResult = new USGSVariableResult();
				varResult.setStationID(stationID);
				varResult.setType(variable);
				varResult.setVariableID(variableID);
				varResult.setStartDate(start);
				varResult.setEndDate(end);
				result.setVariable(varResult);
				results.add(result);
			}
		} catch (MalformedURLException mue) {
			LOG.error("Error reading usgs data", mue);
		} catch (IOException ioe) {
			LOG.error("Error reading usgs data", ioe);
		} catch (ParserConfigurationException pce) {
			LOG.error("Error reading usgs data", pce);
		} catch (SAXException saxe) {
			LOG.error("Error reading usgs data", saxe);
		} catch (ParseException pe) {
			LOG.error("Error reading usgs data", pe);
		}
		return addMissingResults(stationID, variable, start, end, results);
	}

	private ArrayList<USGSResult> addMissingResults(String stationID,
			USGSVariableEnum variable, DateTime startDate, DateTime endDate,
			ArrayList<USGSResult> results) {
		startDate = new DateTime(startDate.getYear(), startDate
				.getMonthOfYear(), startDate.getDayOfMonth(), 0, 0, 0, 0);
		endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(),
				endDate.getDayOfMonth(), 0, 0, 0, 0);
		TreeSet<DateTime> expected = findDatesInRange(startDate, endDate);
		TreeSet<DateTime> found = new TreeSet<DateTime>();
		for (USGSResult result : results) {
			found.add(result.getDate());
		}
		int missCount = 0;
		String variableID = stationID + ":" + variable.getUSGScode();
		for (DateTime exp : expected) {
			if (!found.contains(exp)) {
				USGSResult missing = new USGSResult();
				missing.setDailyID(variableID + ":" + exp.toString("yyyyMMdd"));
				missing.setDate(exp);
				missing.setValue(MISSING);
				USGSVariableResult varResult = new USGSVariableResult();
				varResult.setStationID(stationID);
				varResult.setType(variable);
				varResult.setVariableID(variableID);
				varResult.setStartDate(startDate);
				varResult.setEndDate(endDate);
				missing.setVariable(varResult);
				results.add(missing);
				missCount++;
			}
		}
		return results;
	}

	public TreeSet<String> getSiteIds(USState state) {
		TreeSet<String> siteIDs = new TreeSet<String>();
		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement ps = conn
					.prepareStatement("SELECT station_id FROM station WHERE state = ?");
			ps.setString(1, state.name());
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				siteIDs.add(results.getString(1));
			}
			ps.close();
			results.close();
		} catch (SQLException sqle) {
			LOG.error("SQl Exception", sqle);
		} finally {
			try {
				if (null != conn) {
					conn.close();
				}
			} catch (SQLException sqle) {
				LOG.error("SQl Exception", sqle);
			}
		}
		LOG.debug("found " + siteIDs.size() + " sites in " + state.name());
		return siteIDs;
	}

	public HashSet<USGSStreamFlowStation> loadStations(USState state)
			throws RemoteException {
		HashSet<USGSStreamFlowStation> stations = new HashSet<USGSStreamFlowStation>();
		try {
			URLConnection conn;
			Document doc;
			URL url;
			NodeList sites;
			try {
				url = new URL(
						"http://waterdata.usgs.gov/nwis/dv?referred_module=sw&state_cd="
								+ state.getPostalCode()
								+ "&site_tp_cd=ST&index_pmcode_00060=1&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=xml&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=state_cd&column_name=county_cd&column_name=huc_cd&column_name=dec_lat_va&column_name=dec_long_va&column_name=dec_coord_datum_cd&column_name=alt_va&column_name=alt_acy_va&column_name=alt_datum_cd&column_name=drain_area_va&column_name=contrib_drain_area_va&range_selection=days&period=365&date_format=YYYY-MM-DD&rdb_compression=file&list_of_search_criteria=state_cd%2Csite_tp_cd%2Crealtime_parameter_selection");
				conn = url.openConnection();
				doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
						.parse(conn.getInputStream());
				sites = doc.getElementsByTagName("site");
				LOG.debug("found " + sites.getLength() + " sites in "
						+ state.name());
				for (int i = 0; i < sites.getLength(); i++) {
					USGSStreamFlowStation station = new USGSStreamFlowStation();
					station.setStationID(sites.item(i).getChildNodes().item(3)
							.getTextContent().trim());
					try {
						station.setAgencyCode(sites.item(i).getChildNodes()
								.item(1).getTextContent().trim());
						station.setStationName(sites.item(i).getChildNodes()
								.item(5).getTextContent().trim());
						SpatialQuery query = SpatialServiceAccessor
								.getInstance().getSpatialQuery();
						Set<USCounty> counties = query
								.getCountiesByState(state);
						String fips = sites.item(i).getChildNodes().item(9)
								.getTextContent().trim();
						for (USCounty county : counties) {
							if (county.getCountyFips().equals(fips)) {
								station.setCounty(county);
							}
						}
						station.setState(state);
						station.setHydrologicUnit(sites.item(i).getChildNodes()
								.item(11).getTextContent().trim());
						if (sites.item(i).getChildNodes().item(13)
								.getTextContent().trim().length() > 0) {
							station.setLatitude(Float.parseFloat(sites.item(i)
									.getChildNodes().item(13).getTextContent()
									.trim()));
						}
						if (sites.item(i).getChildNodes().item(15)
								.getTextContent().trim().length() > 0) {
							station.setLongitude(Float.parseFloat(sites.item(i)
									.getChildNodes().item(15).getTextContent()
									.trim()));
						}
						station.setCoordinateAccuracy(sites.item(i)
								.getChildNodes().item(17).getTextContent()
								.trim());
						station.setLatLongDatumCode(sites.item(i)
								.getChildNodes().item(19).getTextContent()
								.trim());
						if (sites.item(i).getChildNodes().item(21)
								.getTextContent().trim().length() > 0) {
							station.setAltitude(Float.parseFloat(sites.item(i)
									.getChildNodes().item(21).getTextContent()
									.trim()));
						}
						if (sites.item(i).getChildNodes().item(23)
								.getTextContent().trim().length() > 0
								&& (!sites.item(i).getChildNodes().item(23)
										.getTextContent().trim().equals("."))) {
							station.setAltitudeAccuracy(Float.parseFloat(sites
									.item(i).getChildNodes().item(23)
									.getTextContent().trim()));
						}
						if (sites.item(i).getChildNodes().item(27)
								.getTextContent().trim().length() > 0) {
							station.setDrainageArea(Float.parseFloat(sites
									.item(i).getChildNodes().item(27)
									.getTextContent().trim()));
						}
						if (sites.item(i).getChildNodes().item(29)
								.getTextContent().trim().length() > 0
								&& (!sites.item(i).getChildNodes().item(29)
										.getTextContent().trim().equals("."))) {
							station.setContributingDrainageArea(Float
									.parseFloat(sites.item(i).getChildNodes()
											.item(29).getTextContent().trim()));
						}
						station.setAltitudeDatum(sites.item(i).getChildNodes()
								.item(25).getTextContent().trim());
						stations.add(station);
					} catch (NumberFormatException nfe) {
						LOG.error("Error reading data for station "
								+ station.getStationID(), nfe);
					}
				}
				LOG.debug("read " + stations.size() + " sites in "
						+ state.name());
			} catch (IOException ioe) {
				LOG.error("Error reading data ", ioe);
			} catch (ParserConfigurationException pce) {
				LOG.error("Error parsing data ", pce);
			} catch (SAXException se) {
				LOG.error("Error building document ", se);
			} catch (InstantiationException ie) {
				LOG.error("Error reading county data ", ie);
			}
		} catch (Exception e) {
			LOG.error("Error loading state data ", e);
			throw new RemoteException("Could not load stations for "
					+ state.name());
		}
		return stations;
	}
}
