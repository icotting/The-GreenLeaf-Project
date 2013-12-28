package edu.unl.act.rma.console.web;

import java.rmi.RemoteException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import edu.unl.act.rma.console.usgs.USGSDataReader;
import edu.unl.act.rma.console.usgs.USGSStreamFlowStation;
import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.configuration.jmx.JMXException;
import edu.unl.act.rma.firm.core.spatial.USState;

public class StreamFlowDataService {
	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG,
			StreamFlowDataService.class);
	private static DataSource source = DataSourceInjector
			.injectDataSource(DataSourceTypes.STREAM_FLOW);
	private Connection conn;

	public void createTables() throws JMXException {
		try {
			StreamFlowSourceSQL.createSchemas();
		} catch (SQLException sqe) {
			LOG.error("exception creating schemas", sqe);
			throw new JMXException("exception creating schemas");
		} catch (Exception e) {
			LOG.error("unknown exception", e);
			throw new JMXException(e.getMessage());
		}

	}

	public void buildStations() throws RemoteException {
		for (USState state : USState.values()) {
			buildStations(state);
		}
	}

	public void buildStations(USState state) throws RemoteException {
		USGSDataReader reader = new USGSDataReader();
		ArrayList<USGSStreamFlowStation> stations = new ArrayList<USGSStreamFlowStation>();
		try {
			reader = new USGSDataReader();
			stations.addAll(reader.loadStations(state));
			LOG.debug("preparing to insert data for " + stations.size()
					+ " stations");
			conn = source.getConnection();
			String sqlInsertStations = "INSERT INTO stations( "
					+ "station_id, agency, station_name, county, "
					+ "state, hydrologic_unit, latitude, longitude, "
					+ "coord_accuracy, lat_long_datum, altitude, "
					+ "alt_accuracy, alt_datum, drain_area, contrib_drain_area ) "
					+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement ps = conn.prepareStatement(sqlInsertStations);
			int records = 0;
			for (USGSStreamFlowStation station : stations) {
				try {
					ps.setString(1, station.getStationID());
					ps.setString(2, station.getAgencyCode());
					ps.setString(3, station.getStationName());
					ps.setString(4, station.getCounty().getName());
					ps.setString(5, station.getState().name());
					ps.setString(6, station.getHydrologicUnit());
					ps.setFloat(7, station.getLatitude());
					ps.setFloat(8, station.getLongitude());
					ps.setString(9, station.getCoordinateAccuracy());
					ps.setString(10, station.getLatLongDatumCode());
					ps.setFloat(11, station.getAltitude());
					ps.setFloat(12, station.getAltitudeAccuracy());
					ps.setString(13, station.getAltitudeDatum());
					ps.setFloat(14, station.getDrainageArea());
					ps.setFloat(15, station.getContributingDrainageArea());
				} catch (NullPointerException npe) {
					// do nothing
					LOG.error("could not add station", npe);
				}
				ps.addBatch();
				records++;

				// clear batch every 500
				if (records == 500) {
					try {
						LOG.debug("Committing batch ending with "
								+ station.getStationID() + "("
								+ station.getState().name() + ")");
						ps.executeBatch();
						ps.clearBatch();
					} catch (BatchUpdateException e) {
						LOG.error("Error in updating batch", e);
						break;
					} finally {
						records = 0;
					}
				}
			}
			if (records > 0) {
				ps.executeBatch(); // last set of records
				ps.clearBatch();
			}
			LOG.debug("finished writing station data for " + state.name());
			ps.close();
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
	}

}
