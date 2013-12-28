package edu.unl.act.rma.console.usgs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.TreeSet;

import javax.sql.DataSource;

import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.spatial.USState;

public class USGSDataWriter {
	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG,
			USGSDataWriter.class);
	private USGSDataBuilder builder = new USGSDataBuilder();

	private static DataSource source = DataSourceInjector
			.injectDataSource(DataSourceTypes.STREAM_FLOW);

	private static final String DAILY_INSERT = "LOAD DATA LOCAL INFILE ? REPLACE INTO TABLE daily FIELDS TERMINATED BY ',' LINES TERMINATED BY '|' (daily_id, variable_id, year, month_num, day_num, value, qualification_code, status)";
	private static final String WEEKLY_INSERT = "LOAD DATA LOCAL INFILE ? REPLACE INTO TABLE weekly FIELDS TERMINATED BY ',' LINES TERMINATED BY '|' (weekly_id, variable_id, year, week_num, value)";
	private static final String MONTHLY_INSERT = "LOAD DATA LOCAL INFILE ? REPLACE INTO TABLE monthly FIELDS TERMINATED BY ',' LINES TERMINATED BY '|' (monthly_id, variable_id, year, month_num, value)";
	private static final String VARIABLE_INSERT = "LOAD DATA LOCAL INFILE ? REPLACE INTO TABLE variable FIELDS TERMINATED BY ',' LINES TERMINATED BY '|' (variable_id, variable_name, station_id, start_date, end_date, missing_percent)";
	private static final String STATION_INSERT = "LOAD DATA LOCAL INFILE ? REPLACE INTO TABLE station FIELDS TERMINATED BY ';' LINES TERMINATED BY '|' (station_id, station_name, agency, state, county, latitude, longitude, coord_accuracy, lat_long_datum, elevation, elev_accuracy, elev_datum, hydrologic_unit, drain_area, contrib_drain_area, start_date, end_date, has_reported_data)";
	
	private String sqlPath = "C:/sqlTmp";

	private void clearFiles() {
		File folder = new File(sqlPath);
		clearFolder(folder);
		folder.delete();
	}

	private void clearFolder(File folder) {
		for (File file : folder.listFiles()) {
			file.delete();
		}
	}

	public void loadDailyData(USState state) {
		File folder = new File(writeDailyData(state));
		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement psLoad = conn.prepareStatement(DAILY_INSERT);
			LOG.debug("loading daily values for " + folder.list().length
					+ " stations in " + state.name());
			for (File file : folder.listFiles()) {
				try {
					psLoad.setString(1, file.getAbsolutePath());
					psLoad.execute();
				} catch (SQLException sqle) {
					LOG.error("could not load daily data for file "
							+ file.getAbsolutePath(), sqle);
				}
			}
		} catch (SQLException sqe) {
			LOG.error("could not load daily files", sqe);
		} finally {
			try {
				if (null != conn) {
					conn.close();
				}
			} catch (SQLException sqe) {
				LOG.error("Error closing connection", sqe);
			}
		}
	}

	public void loadData(USState state) {
		loadDailyData(state);
		loadVariableData(state);
		loadStationDates(state);
		loadWeeklyData(state);
		loadMonthlyData(state);
		clearFiles();
	}

	public void loadMonthlyData(USState state) {
		File folder = new File(writeMonthlyData(state));
		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement psLoad = conn.prepareStatement(MONTHLY_INSERT);
			LOG.debug("loading monthly values for " + folder.list().length
					+ " stations in " + state.name());
			for (File file : folder.listFiles()) {
				try {
					psLoad.setString(1, file.getAbsolutePath());
					psLoad.execute();
				} catch (SQLException sqle) {
					LOG.error("could not load monthly data for file "
							+ file.getAbsolutePath(), sqle);
				}
			}
		} catch (SQLException sqe) {
			LOG.error("could not load daily files", sqe);
		} finally {
			try {
				if (null != conn) {
					conn.close();
				}
			} catch (SQLException sqe) {
				LOG.error("Error closing connection", sqe);
			}
		}
	}

	public void loadStationDates(USState state) {
		File file = new File(writeStationDates(state));
		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement psLoad = conn.prepareStatement(STATION_INSERT);
			LOG.debug("loading station dates ");
			try {
				psLoad.setString(1, file.getAbsolutePath());
				psLoad.execute();
			} catch (SQLException sqle) {
				LOG.error("could not load station dates for file "
						+ file.getAbsolutePath(), sqle);
			}
		} catch (SQLException sqe) {
			LOG.error("could not load daily files", sqe);
		} finally {
			try {
				if (null != conn) {
					conn.close();
				}
			} catch (SQLException sqe) {
				LOG.error("Error closing connection", sqe);
			}
		}
	}

	public void loadVariableData(USState state) {
		File folder = new File(writeVariableData(state));
		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement psLoad = conn.prepareStatement(VARIABLE_INSERT);
			LOG.debug("loading variable values for " + folder.list().length
					+ " stations in " + state.name());
			for (File file : folder.listFiles()) {
				try {
					psLoad.setString(1, file.getAbsolutePath());
					psLoad.execute();
				} catch (SQLException sqle) {
					LOG.error("could not load variable data for file "
							+ file.getAbsolutePath(), sqle);
				}
			}
		} catch (SQLException sqe) {
			LOG.error("could not load variable files", sqe);
		} finally {
			try {
				if (null != conn) {
					conn.close();
				}
			} catch (SQLException sqe) {
				LOG.error("Error closing connection", sqe);
			}
		}
	}

	public void loadWeeklyData(USState state) {
		File folder = new File(writeWeeklyData(state));
		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement psLoad = conn.prepareStatement(WEEKLY_INSERT);
			LOG.debug("loading weekly values for " + folder.list().length
					+ " stations in " + state.name());
			for (File file : folder.listFiles()) {
				try {
					psLoad.setString(1, file.getAbsolutePath());
					psLoad.execute();
				} catch (SQLException sqle) {
					LOG.error("could not load weekly data for file "
							+ file.getAbsolutePath(), sqle);
				}
			}
		} catch (SQLException sqe) {
			LOG.error("could not load daily files", sqe);
		} finally {
			try {
				if (null != conn) {
					conn.close();
				}
			} catch (SQLException sqe) {
				LOG.error("Error closing connection", sqe);
			}
		}
	}

	private void writeDailyData(String stationID, String path) {
		File dailySQL = new File(path + "/" + stationID + ".txt");
		BufferedWriter out = null;
		try {
			dailySQL.createNewFile();
			out = new BufferedWriter(new FileWriter(dailySQL));
			out.write(builder.getDailySQL(stationID));
		} catch (IOException ioe) {
			LOG.error("error writing daily sql for " + stationID, ioe);
			dailySQL.delete();
		} finally {
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (IOException ioe) {
				LOG.error("could not close file writer", ioe);
			}
		}
	}

	private String writeDailyData(USState state) {
		String tmpPath = sqlPath + "/daily/" + state.name();
		File folder = new File(tmpPath);
		USGSDataReader reader = new USGSDataReader();
		folder.mkdirs();
		try {
			folder.createNewFile();
			clearFolder(folder);
			LOG.debug("writing daily sql for stations in " + state.name()
					+ " (" + folder.getAbsolutePath() + ") ");
			TreeSet<String> stations = reader.getSiteIds(state);
			int totalCount = stations.size();
			int updateInterval = (int) (totalCount * 0.2);
			int updateCount = 0;
			for (String stationID : stations) {
				if (updateCount > 0 && updateCount % updateInterval == 0) {
					LOG
							.info((((double) updateCount / (double) totalCount) * 100)
									+ " percent done writing daily sql for stations in "
									+ state.name());
				}
				writeDailyData(stationID, folder.getAbsolutePath());
				updateCount++;
			}
			LOG.debug("finished writing daily sql for stations in "
					+ state.name());
		} catch (IOException ioe) {
			LOG.error("could not create directory " + tmpPath, ioe);
		}
		return folder.getAbsolutePath();
	}

	private String writeMonthlyData(USState state) {
		String tmpPath = sqlPath + "/monthly/" + state.name();
		File folder = new File(tmpPath);
		folder.mkdirs();
		USGSDataReader reader = new USGSDataReader();
		try {
			folder.createNewFile();
			clearFolder(folder);
			LOG.debug("writing monthly sql for stations in " + state.name()
					+ " (" + folder.getAbsolutePath() + ") ");
			TreeSet<String> stations = reader.getSiteIds(state);
			int totalCount = stations.size();
			int updateInterval = (int) (totalCount * 0.2);
			int updateCount = 0;
			for (String stationID : stations) {
				if (updateCount > 0 && updateCount % updateInterval == 0) {
					LOG
							.info((((double) updateCount / (double) totalCount) * 100)
									+ " percent done with writing monthly sql for stations in "
									+ state.name());
				}
				File monthlySQL = new File(folder.getAbsolutePath() + "/"
						+ stationID + ".txt");
				BufferedWriter out = null;
				try {
					String sql = builder.getMonthlyDataSQL(stationID);
					if (sql.equals("")) {
						continue;
					} else {
						monthlySQL.createNewFile();
						out = new BufferedWriter(new FileWriter(monthlySQL));
						out.write(sql);
					}
				} catch (IOException ioe) {
					LOG
							.error(
									"error writing monthly sql for "
											+ stationID, ioe);
					monthlySQL.delete();
				} finally {
					try {
						if (out != null) {
							out.flush();
							out.close();
						}
					} catch (IOException ioe) {
						LOG.error("could not close file writer", ioe);
					}
				}
				updateCount++;
			}
			LOG.debug("finished writing monthly sql for stations in "
					+ state.name());
		} catch (IOException ioe) {
			LOG.error("could not create directory " + tmpPath, ioe);
		}
		return folder.getAbsolutePath();
	}

	private String writeStationDates(USState state) {
		try {
			File folder = new File(sqlPath + "/stations");
			folder.mkdir();
			folder.createNewFile();
			File stationSQL = new File(folder.getAbsolutePath()
					+ "/" + state.name() + ".txt");
			BufferedWriter out = null;
			try {
				String sql = builder.getStationUpdateSQL(state.name());
				stationSQL.createNewFile();
				out = new BufferedWriter(new FileWriter(stationSQL));
				out.write(sql);
			} catch (IOException ioe) {
				LOG.error("error writing station update sql", ioe);
				stationSQL.delete();
			} finally {
				try {
					if (out != null) {
						out.flush();
						out.close();
					}
				} catch (IOException ioe) {
					LOG.error("could not close file writer", ioe);
				}
			}
			return stationSQL.getAbsolutePath();
		} catch (IOException ioe) {
			LOG.error("could not create directory " + sqlPath, ioe);
		}
		throw new RuntimeException("error creating station dates file");
	}

	private String writeVariableData(USState state) {
		String tmpPath = sqlPath + "/variable/" + state.name();
		File folder = new File(tmpPath);
		folder.mkdirs();
		USGSDataReader reader = new USGSDataReader();
		try {
			folder.createNewFile();
			clearFolder(folder);
			LOG.debug("writing variable sql for stations in " + state.name()
					+ " (" + folder.getAbsolutePath() + ") ");
			TreeSet<String> stations = reader.getSiteIds(state);
			int totalCount = stations.size();
			int updateInterval = (int) (totalCount * 0.2);
			int updateCount = 0;
			for (String stationID : stations) {
				if (updateCount > 0 && updateCount % updateInterval == 0) {
					LOG
							.info((((double) updateCount / (double) totalCount) * 100)
									+ " percent done writing variable sql for stations in "
									+ state.name());
				}
				File varSQL = new File(folder.getAbsolutePath() + "/"
						+ stationID + ".txt");
				BufferedWriter out = null;
				try {
					varSQL.createNewFile();
					out = new BufferedWriter(new FileWriter(varSQL));
					out.write(builder.getVariableSQL(stationID));
				} catch (IOException ioe) {
					LOG.error("error writing variable sql for " + stationID,
							ioe);
					varSQL.delete();
				} finally {
					try {
						if (out != null) {
							out.flush();
							out.close();
						}
					} catch (IOException ioe) {
						LOG.error("could not close file writer", ioe);
					}
				}
				updateCount++;
			}
			LOG.debug("finished writing variable sql for stations in "
					+ state.name());
		} catch (IOException ioe) {
			LOG.error("could not create directory " + tmpPath, ioe);
		}
		return folder.getAbsolutePath();
	}

	private String writeWeeklyData(USState state) {
		String tmpPath = sqlPath + "/weekly/" + state.name();
		File folder = new File(tmpPath);
		folder.mkdirs();
		USGSDataReader reader = new USGSDataReader();
		try {
			folder.createNewFile();
			clearFolder(folder);
			LOG.debug("writing weekly sql for stations in " + state.name()
					+ " (" + folder.getAbsolutePath() + ") ");
			TreeSet<String> stations = reader.getSiteIds(state);
			int totalCount = stations.size();
			int updateInterval = (int) (totalCount * 0.2);
			int updateCount = 0;
			for (String stationID : stations) {
				if (updateCount > 0 && updateCount % updateInterval == 0) {
					LOG
							.info((((double) updateCount / (double) totalCount) * 100)
									+ " percent done writing weekly sql for stations in "
									+ state.name());
				}
				File weeklySQL = new File(folder.getAbsolutePath() + "/"
						+ stationID + ".txt");
				BufferedWriter out = null;
				try {
					String sql = builder.getWeeklyDataSQL(stationID);
					if (sql.equals("")) {
						continue;
					} else {
						weeklySQL.createNewFile();
						out = new BufferedWriter(new FileWriter(weeklySQL));
						out.write(sql);
					}
				} catch (IOException ioe) {
					LOG.error("error writing weekly sql for " + stationID, ioe);
					weeklySQL.delete();
				} finally {
					try {
						if (out != null) {
							out.flush();
							out.close();
						}
					} catch (IOException ioe) {
						LOG.error("could not close file writer", ioe);
					}
				}
				updateCount++;
			}
			LOG.debug("finished writing weekly sql for stations in "
					+ state.name());
		} catch (IOException ioe) {
			LOG.error("could not create directory " + tmpPath, ioe);
		}
		return folder.getAbsolutePath();
	}
}
