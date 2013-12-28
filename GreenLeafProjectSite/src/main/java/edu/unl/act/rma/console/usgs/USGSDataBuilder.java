package edu.unl.act.rma.console.usgs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.core.CalendarDataParser;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;

public class USGSDataBuilder {

	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG,
			USGSDataBuilder.class);
	private static DataSource source = DataSourceInjector
			.injectDataSource(DataSourceTypes.STREAM_FLOW);
	private Connection conn;

	public static String DAILY_VALUES = "SELECT year, month_num, day_num, value, daily.variable_id FROM daily INNER JOIN variable ON daily.variable_id = variable.variable_id AND variable.variable_name = ? WHERE variable.station_id = ? ORDER BY year, month_num, day_num ";
	public static final float MISSING = -99;
	public static final float NONEXISTENT = -100;

	private CalendarDataParser getCalendarDataParser(String stationID,
			USGSVariableEnum variable) {
		CalendarDataParser parser = null;
		try {
			conn = source.getConnection();
			PreparedStatement psDaily = conn.prepareStatement(DAILY_VALUES);
			psDaily.setString(1, variable.name());
			psDaily.setString(2, stationID);
			ResultSet dailyResults = psDaily.executeQuery();
			ArrayList<Float> values = new ArrayList<Float>();
			int first_year = 0;
			int first_month = 0;
			int first_day = 0;
			boolean first = true;
			while (dailyResults.next()) {
				if (first) {
					first_year = dailyResults.getInt("year");
					first_month = dailyResults.getInt("month_num");
					first_day = dailyResults.getInt("day_num");
					first = false;
				}
				values.add(dailyResults.getFloat("value"));
			}
			float[] data = new float[values.size()];
			for (int i = 0; i < values.size(); i++) {
				data[i] = values.get(i);
			}
			try {
				parser = new CalendarDataParser(data, first_year, first_month,
						first_day);
			} catch (IllegalFieldValueException ife) {
				// parser throws exception if any date values equal 0
				return null;
			}
			psDaily.close();
			dailyResults.close();
		} catch (SQLException sqe) {
			LOG.error("Error reading daily values", sqe);
		} finally {
			try {
				conn.close();
			} catch (SQLException sqe) {
				LOG.error("error closing connection", sqe);
			}
		}
		return parser;
	}

	protected String getWeeklyDataSQL(String stationID) {
		StringBuffer sql = new StringBuffer();
		CalendarDataParser parser = null;
		for (USGSVariableEnum variable : USGSVariableEnum.values()) {
			parser = getCalendarDataParser(stationID, variable);
			if (null != parser) {
				int year = 0;
				int week = 0;
				float value = 0;
				String weeklyID = null;
				String variableID = null;
				while (parser.hasNextWeek()) {
					try {
						variableID = stationID + ":" + variable.getUSGScode();
						year = parser.getYear(CalendarPeriod.WEEKLY);
						week = parser.getWeekOfYear();
						value = parser.nextWeekAverage();
						weeklyID = variableID + ":" + year + ":w" + week;
						sql.append(weeklyID + "," + variableID + "," + year
								+ "," + week + "," + value + "|");
					} catch (ArrayIndexOutOfBoundsException aie) {
						LOG.error("error getting weekly data for station "
								+ stationID + " year : " + year + " week: "
								+ week, aie);
					}
				}
			}
		}
		return sql.toString();
	}

	protected String getMonthlyDataSQL(String stationID) {
		StringBuffer sql = new StringBuffer();
		CalendarDataParser parser = null;
		for (USGSVariableEnum variable : USGSVariableEnum.values()) {
			parser = getCalendarDataParser(stationID, variable);
			if (null != parser) {
				int year = 0;
				int month = 0;
				float value = 0;
				String monthlyID = null;
				String variableID = null;
				while (parser.hasNextMonth()) {
					try {
						variableID = stationID + ":" + variable.getUSGScode();
						year = parser.getYear(CalendarPeriod.MONTHLY);
						month = parser.getMonthOfYear();
						value = parser.nextMonthAverage();
						monthlyID = variableID + ":" + year + ":m" + month;
						sql.append(monthlyID + "," + variableID + "," + year
								+ "," + month + "," + value + "|");
					} catch (ArrayIndexOutOfBoundsException aie) {
						LOG.error("error getting monthly data for station "
								+ stationID + " year : " + year + " month: "
								+ month, aie);
					}
				}
			}
		}
		return sql.toString();
	}

	protected String getDailySQL(String stationID) {
		USGSDataReader reader = new USGSDataReader();
		ArrayList<USGSResult> results = reader.getDailyResults(stationID);
		HashMap<USGSVariableEnum, USGSVariableResult> vars = new HashMap<USGSVariableEnum, USGSVariableResult>();
		StringBuffer sql = new StringBuffer();
		USGSVariableResult tempResult = null;
		for (USGSResult result : results) {
			if (null == vars.get(result.getVariable().getType())) {
				USGSVariableResult varResult = new USGSVariableResult();
				varResult.setStationID(result.getVariable().getStationID());
				varResult.setType(result.getVariable().getType());
				varResult.setVariableID(result.getVariable().getVariableID());
				varResult.setStartDate(new DateTime());
				varResult.setEndDate(new DateTime(1800, 1,
						1, 0, 0, 0, 0, GregorianChronology.getInstance()));
				vars.put(result.getVariable().getType(), varResult);
			}
			sql.append(result.getDailyID()
					+ ","
					+ result.getVariable().getVariableID()
					+ ","
					+ result.getDate().getYear()
					+ ","
					+ result.getDate().getMonthOfYear()
					+ ","
					+ result.getDate().getDayOfMonth()
					+ ","
					+ result.getValue()
					+ ","
					+ ((null != result.getQualification()) ? result
							.getQualification().getCode() : "")
					+ ","
					+ ((null != result.getStatus()) ? result.getStatus()
							.getCode() : "") + "|");
			tempResult = vars.get(result.getVariable().getType());
			if (tempResult.getStartDate().isAfter(result.getDate())) {
				tempResult.setStartDate(result.getDate());
			} 
			if (tempResult.getEndDate().isBefore(result.getDate())) {
				tempResult.setEndDate(result.getDate());
			} 
		}

		// add nonexistent results (for 2/29 in non-leap years)
		DateTime currentDate = null;
		USGSVariableResult varResult = null;
		String dailyID = "";

		for (USGSVariableEnum var : vars.keySet()) {
			varResult = vars.get(var);
			currentDate = new DateTime(varResult.getStartDate().getYear(), 3,
					1, 0, 0, 0, 0, GregorianChronology.getInstance());
			if (currentDate.isBefore(varResult.getStartDate())) {
				currentDate = currentDate.plusYears(1);
			}

			while (currentDate.isBefore(varResult.getEndDate())) {
				if (currentDate.getYear() % 4 > 0) {
					dailyID = varResult.getVariableID() + ":"
							+ currentDate.getYear() + ("0229");
					sql.append(dailyID + "," + varResult.getVariableID() + ","
							+ currentDate.getYear() + "," + 2 + "," + 29 + ","
							+ NONEXISTENT + "," + "" + "," + "|");
				}
				currentDate = currentDate.plusYears(1);
			}

		}

		return sql.toString();
	}

	protected String getVariableSQL(String stationID) {
		StringBuffer sql = new StringBuffer();
		try {
			conn = source.getConnection();

			PreparedStatement psDaily = conn
					.prepareStatement("SELECT * from daily WHERE variable_id = ? ORDER by year, month_num, day_num");
			ResultSet rsDaily = null;
			String variableID = null;
			DateTime start = null;
			DateTime end = null;
			for (USGSVariableEnum var : USGSVariableEnum.values()) {
				variableID = stationID + ":" + var.getUSGScode();
				psDaily.setString(1, variableID);
				rsDaily = psDaily.executeQuery();
				boolean first = true;
				int resultCount = 0;
				int missingCount = 0;
				start = null;
				end = null;
				while (rsDaily.next()) {
					if (first) {
						try {
							start = new DateTime(rsDaily
									.getInt("year"), rsDaily
									.getInt("month_num"), rsDaily
									.getInt("day_num"), 0, 0, 0, 0);
						} catch (IllegalFieldValueException ife) {
							// throws exception for non-existent 2/29 values
							LOG.debug("illegal date value for start in variable sql " + variableID);
							if ((rsDaily.getInt("year") % 4 > 0
									&& rsDaily.getInt("month_num") == 2 && rsDaily
									.getInt("day_num") == 29)) {
								start = new DateTime(rsDaily
										.getInt("year"), 3, 1, 0, 0, 0, 0);
							} else {
								throw ife;
							}
						}
						first = false;
					}
					if (rsDaily.getFloat("value") < 0) {
						missingCount++;
					}
					resultCount++;
					if (!(rsDaily.getInt("year") % 4 > 0
							&& rsDaily.getInt("month_num") == 2 && rsDaily
							.getInt("day_num") == 29)) {
						end = new DateTime(rsDaily.getInt("year"), rsDaily
								.getInt("month_num"),
								rsDaily.getInt("day_num"), 0, 0, 0, 0);
					}
				}
				if (null != start) {
					sql.append(variableID + "," + var.name() + "," + stationID
							+ "," + ((start != null) ? start.toString().substring(0, 10) : "") + ","
							+ ((end != null) ? end.toString().substring(0, 10) : "") + ","
							+ ((double) missingCount / (double) resultCount)
							+ "|");
				}
			}
			psDaily.close();
			rsDaily.close();
		} catch (SQLException sqe) {
			LOG.error("error getting variable sql", sqe);
		} finally {
			try {
				conn.close();
			} catch (SQLException sqe) {
				LOG.error("error closing connection", sqe);
			}
		}
		return sql.toString();
	}
	
	protected String getStationUpdateSQL(String state) {
		StringBuffer sql = new StringBuffer();
		ArrayList<String> stations = new ArrayList<String>();
		try {
			conn = source.getConnection();
			PreparedStatement psStations = conn
					.prepareStatement("SELECT DISTINCT station.station_id FROM station, variable WHERE station.state = ? AND variable.station_id = station.station_id ORDER BY station.station_id");
			psStations.setString(1, state);
			ResultSet rsStations = psStations.executeQuery();
			while (rsStations.next()) {
				stations.add(rsStations.getString("station_id"));
			}
			psStations.close();
			rsStations.close();
			
			PreparedStatement psStationDates = conn.prepareStatement("SELECT variable.start_date, variable.end_date, station.station_id, station_name, agency, state, county, latitude, longitude, coord_accuracy, lat_long_datum, elevation, elev_accuracy, elev_datum, hydrologic_unit, drain_area, contrib_drain_area, has_reported_data FROM station, variable WHERE variable.station_id = ? AND station.station_id = variable.station_id");
			ResultSet rsStationDates = null;
			DateTime start = null;
			DateTime end = null;
			DateTime checkStart = null;
			DateTime checkEnd = null;
			String stationInfo = null;
			
			for (String station : stations) {
				psStationDates.setString(1, station);
				rsStationDates = psStationDates.executeQuery();
				start = null;
				end = null;
				stationInfo = null;
				while (rsStationDates.next()) {
					checkStart = new DateTime(rsStationDates.getDate("start_date"));
					checkEnd = new DateTime(rsStationDates.getDate("end_date"));
					if (start == null || checkStart.isBefore(start)) {
						start = checkStart;
					}
					if (end == null || checkEnd.isAfter(end)) {
						end = checkEnd;
					}
					if (stationInfo == null) {
						stationInfo = station 
						+ ";" + rsStationDates.getString("station_name")  
						+ ";" + rsStationDates.getString("agency")
						+ ";" + rsStationDates.getString("state")
						+ ";" + rsStationDates.getString("county")
						+ ";" + rsStationDates.getFloat("latitude")
						+ ";" + rsStationDates.getFloat("longitude")
						+ ";" + rsStationDates.getString("coord_accuracy")
						+ ";" + rsStationDates.getString("lat_long_datum")
						+ ";" + rsStationDates.getFloat("elevation")
						+ ";" + rsStationDates.getFloat("elev_accuracy")
						+ ";" + rsStationDates.getString("elev_datum")
						+ ";" + rsStationDates.getString("hydrologic_unit")
						+ ";" + rsStationDates.getString("drain_area")
						+ ";" + rsStationDates.getString("contrib_drain_area");
					}
				}
				
				sql.append(stationInfo + ";" + start.toString("yyyy-MM-dd") + ";" + end.toString("yyyy-MM-dd") + ";1|");
			}
			psStationDates.close();
			rsStationDates.close();
		} catch (SQLException sqe) {
			LOG.error("error getting station start/end date sql", sqe);
		} finally {
			try {
				conn.close();
			} catch (SQLException sqe) {
				LOG.error("error closing connection", sqe);
			}
		}
		return sql.toString();
	}
}
