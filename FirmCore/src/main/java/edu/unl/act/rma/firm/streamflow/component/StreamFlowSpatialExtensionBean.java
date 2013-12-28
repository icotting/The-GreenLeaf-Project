package edu.unl.act.rma.firm.streamflow.component;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.sql.DataSource;

import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.StationList;
import edu.unl.act.rma.firm.core.component.SpatialQuery;
import edu.unl.act.rma.firm.core.spatial.BoundingBox;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USRegion;
import edu.unl.act.rma.firm.core.spatial.USState;

@Stateless
@Remote( { StreamFlowSpatialExtension.class })
public class StreamFlowSpatialExtensionBean implements
		StreamFlowSpatialExtension {
	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG,
			StreamFlowSpatialExtensionBean.class);

	private static String ZIP_QUERY = "select lat, lon from ZipCodes where zip_code = ?";

	private static String STATION_QUERY = "select station_id FROM station WHERE 1=1 AND 3963.191 * ACOS( (SIN(PI()* LAT /180)*SIN(PI() * latitude/180)) + "
			+ "(COS(PI()* LAT /180)*cos(PI()*latitude/180)*COS(PI() * longitude/180-PI()* LON/180))) <= ? ORDER BY 3963.191 * ACOS((SIN(PI()* LAT /180)*SIN(PI()*latitude/180)) "
			+ "+ (COS(PI()* LAT /180)*cos(PI()*latitude/180)*COS(PI() * longitude/180-PI()* LON/180)))";

	private DataSource source = DataSourceInjector
			.injectDataSource(DataSourceTypes.STREAM_FLOW);
	private DataSource firm_source = DataSourceInjector
			.injectDataSource(DataSourceTypes.SYSTEM);

	@EJB(name = "SpatialQuery", beanInterface = SpatialQuery.class)
	private SpatialQuery spatialQuery;

	@WebMethod
	public List<String> getStationsByZipCode(String zipCode, int distance)
			throws RemoteException {
		Connection zip_conn = null;
		Connection station_conn = null;
		ArrayList<String> stations = new ArrayList<String>();

		PreparedStatement zip_stmt = null;
		ResultSet zip_code_result = null;
		PreparedStatement station_query = null;
		ResultSet station_results = null;

		try {
			zip_conn = firm_source.getConnection();
			station_conn = source.getConnection();

			zip_stmt = zip_conn.prepareStatement(ZIP_QUERY);
			zip_stmt.setString(1, zipCode);
			zip_code_result = zip_stmt.executeQuery();

			if (zip_code_result.next()) {
				String lat = zip_code_result.getString(1);
				String lon = zip_code_result.getString(2);

				String station_query_string = STATION_QUERY.replaceAll("LAT",
						lat);
				station_query_string = station_query_string.replaceAll("LON",
						lon);

				station_query = station_conn
						.prepareStatement(station_query_string);
				station_query.setInt(1, distance);

				LOG.error("query string: " + station_query_string);

				station_results = station_query.executeQuery();

				while (station_results.next()) {
					stations.add(station_results.getString(1));
				}
			}

		} catch (SQLException sqe) {
			LOG.error("sql exception querying stations", sqe);
			RemoteException re = new RemoteException();
			re.initCause(sqe);
			throw re;
		} catch (Exception e) {
			LOG.error("unknown exception creating meta result", e);
			RemoteException re = new RemoteException();
			re.initCause(e);
			throw re;
		} finally {
			try {
				if (zip_code_result != null) {
					zip_code_result.close();
				}

				if (station_results != null) {
					station_results.close();
				}

				if (zip_stmt != null) {
					zip_stmt.close();
				}

				if (station_query != null) {
					station_query.close();
				}
				if (zip_conn != null) {
					zip_conn.close();
				}

				if (station_conn != null) {
					station_conn.close();
				}
			} catch (SQLException sqe2) {
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}

		return stations;
	}

	@WebMethod
	public USCounty getCounty(String stationID) throws RemoteException {
		Connection station_conn = null;
		PreparedStatement station_query = null;
		ResultSet station_results = null;
		String county_name = null;
		String state_name = null;

		try {
			station_conn = source.getConnection();
			station_query = station_conn
					.prepareStatement("select county, state from station where station.station_id = ?");
			station_query.setString(1, stationID);
			station_results = station_query.executeQuery();

			if (station_results.next()) {
				county_name = station_results.getString(1);
				state_name = station_results.getString(2);
			}
			try {
				return spatialQuery.searchCountiesByState(county_name,
						USState.valueOf(state_name)).iterator().next();
			} catch (NoSuchElementException nse) {
				LOG.warn("No county could be found for: " + county_name
						+ " in state " + state_name);
				return null;
			}

		} catch (SQLException sqe) {
			LOG.error("sql exception querying stations", sqe);
			RemoteException re = new RemoteException();
			re.initCause(sqe);
			throw re;
		} catch (Exception e) {
			LOG.error("unknown exception creating meta result", e);
			RemoteException re = new RemoteException();
			re.initCause(e);
			throw re;
		} finally {
			try {

				if (station_results != null) {
					station_results.close();
				}
				if (station_query != null) {
					station_query.close();
				}
				if (station_conn != null) {
					station_conn.close();
				}
			} catch (SQLException sqe2) {
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
	}

	@WebMethod
	public StationList queryStations(BoundingBox region) throws RemoteException {
		List<String> stations = this.getStationsForDefinedRegion(region);
		StationList list = new StationList();
		list.setStations(stations);

		return list;
	}

	@WebMethod
	public List<String> getStationsForState(USState state)
			throws RemoteException {

		Connection conn = null;
		ArrayList<String> station_ids = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet station_query = null;
		try {
			conn = source.getConnection();

			stmt = conn
					.prepareStatement("select station_id from station where state = ? order by station_name");
			stmt.setString(1, state.name());
			station_query = stmt.executeQuery();

			while (station_query.next()) {
				station_ids.add(station_query.getString(1));
			}
		} catch (SQLException sqe) {
			LOG.error("sql exception querying meta data", sqe);
			throw new RemoteException(
					"unable to query metadata from datasource");
		} finally {
			try {
				if (station_query != null) {
					station_query.close();
				}

				if (stmt != null) {
					stmt.close();
				}

				if (conn != null) {
					conn.close();
				}
			} catch (SQLException sqe2) {
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}

		return station_ids;
	}

	@WebMethod
	public List<String> getStationsForGeographicRegion(USRegion region)
			throws RemoteException {
		Connection conn = null;
		ArrayList<String> station_ids = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet station_query = null;
		try {
			conn = source.getConnection();

			stmt = conn
					.prepareStatement("select station_id from station where state = ? order by state, station_name");
			for (USState state : USState.getStatesByRegion(region)) {
				stmt.setString(1, state.name());
				station_query = stmt.executeQuery();

				while (station_query.next()) {
					station_ids.add(station_query.getString(1));
				}
			}
		} catch (SQLException sqe) {
			LOG.error("sql exception querying meta data", sqe);
			throw new RemoteException(
					"unable to query metadata from datasource");
		} finally {
			try {
				if (station_query != null) {
					station_query.close();
				}

				if (stmt != null) {
					stmt.close();
				}

				if (conn != null) {
					conn.close();
				}
			} catch (SQLException sqe2) {
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}

		return station_ids;
	}

	@WebMethod
	public List<String> getStationsForDefinedRegion(BoundingBox region)
			throws RemoteException {
		Connection conn = null;
		ArrayList<String> station_ids = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet station_query = null;
		try {
			conn = source.getConnection();

			stmt = conn
					.prepareStatement("select station_id from station where longitude >= ? and "
							+ "longitude <= ? and latitude >= ? and latitude <= ? order by state, station_name");

			stmt.setDouble(1, region.getWest());
			stmt.setDouble(2, region.getEast());
			stmt.setDouble(3, region.getSouth());
			stmt.setDouble(4, region.getNorth());

			station_query = stmt.executeQuery();

			while (station_query.next()) {
				station_ids.add(station_query.getString(1));
			}
		} catch (SQLException sqe) {
			LOG.error("sql exception querying meta data", sqe);
			throw new RemoteException(
					"unable to query metadata from datasource");
		} finally {
			try {
				if (station_query != null) {
					station_query.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException sqe2) {
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}

		return station_ids;
	}

	@Override
	public List<String> getStationsFromPoint(float lat, float lon, int distance)
			throws RemoteException {

		Connection conn;
		try {
			conn = source.getConnection();
		} catch (Exception e) {
			LOG.error("could not get the connection", e);
			RemoteException re = new RemoteException(
					"could not get a connection");
			re.initCause(e);
			throw re;
		}

		try {
			PreparedStatement stmt = conn
					.prepareStatement("SELECT station_id, ( 3959 * acos( cos( radians(?) ) * cos( radians( latitude ) ) * "
							+ "cos( radians( longitude ) - radians(?) ) + sin( radians(?) ) * sin( radians( latitude ) ) ) ) AS distance FROM "
							+ "station HAVING distance < ?;");

			stmt.setFloat(1, lat);
			stmt.setFloat(2, lon);
			stmt.setFloat(3, lat);
			stmt.setFloat(4, distance);

			ArrayList<String> results = new ArrayList<String>();

			ResultSet station_ids = stmt.executeQuery();
			while (station_ids.next()) {
				results.add(station_ids.getString(1));
			}

			return results;

		} catch (Exception e) {
			LOG.error("could not query the data", e);
			RemoteException re = new RemoteException();
			re.initCause(e);
			throw re;
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				LOG.warn("could not close a connection", e);
			}
		}
	}
}
