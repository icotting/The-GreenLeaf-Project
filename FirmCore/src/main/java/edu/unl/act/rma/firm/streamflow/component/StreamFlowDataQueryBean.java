package edu.unl.act.rma.firm.streamflow.component;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;

import edu.unl.act.rma.firm.core.CalendarDataCollection;
import edu.unl.act.rma.firm.core.CalendarPeriod;
import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidArgumentException;
import edu.unl.act.rma.firm.core.InvalidStateException;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.TemporalPeriod;
import edu.unl.act.rma.firm.core.YearDataBuilder;
import edu.unl.act.rma.firm.streamflow.StreamFlowQueryStrings;

@Stateless
@Local( { LocalStreamFlowDataQuery.class })
@Remote( { StreamFlowDataQuery.class })
public class StreamFlowDataQueryBean implements LocalStreamFlowDataQuery {
	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG,
			StreamFlowDataQuery.class);

	private DataSource source = DataSourceInjector
			.injectDataSource(DataSourceTypes.STREAM_FLOW);

	@EJB
	private StreamFlowMetaDataQuery metaDataQuery;

	private void emptyResult(YearDataBuilder builder, DateTime from,
			boolean isDaily) throws InvalidStateException {
		/* populate the entire range of expected values with the missing value */
		if (isDaily) {
			// when the builder is doing daily data, February 29th needs to be
			// accounted for
			DateTime clock = new DateTime(from);
			while (!builder.isLimitReached()) {
				if (!clock.year().isLeap() && clock.getDayOfYear() == 59) {
					builder.add(DataType.NONEXISTANT);
					if (builder.isLimitReached()) {
						break;
					}
				}

				builder.add(DataType.MISSING);
				clock = clock.plusDays(1);
			}
		} else {
			// for weekly, monthly, and annually there are no special cases to
			// account for
			while (!builder.isLimitReached()) {
				builder.add(DataType.MISSING);
			}
		}
		builder.writeStation();
	}

	public CalendarDataCollection getAnnualDataNormals(List<String> stationIDs,
			int startYear, int endYear, DataType type) throws RemoteException,
			InvalidArgumentException {
		DateTime startDate = new DateTime(startYear, 1, 1, 0, 0, 0, 0,
				GregorianChronology.getInstance());
		DateTime endDate = new DateTime(endYear, 12, 31, 0, 0, 0, 0,
				GregorianChronology.getInstance());

		CalendarDataCollection period_data = getPeriodAnnualData(stationIDs,
				startDate, endDate, type);

		try {
			return ExtendedDataCalculations
					.computeAveragePeriodData(period_data);
		} catch (InvalidStateException ise) {
			LOG
					.error(
							"could not compute data normals, the object state is invalid",
							ise);
			RemoteException re = new RemoteException();
			re.initCause(ise);

			throw re;
		}
	}

	private CalendarDataCollection getAvailableAnnualData(
			List<String> stationIDs, DataType type) throws RemoteException,
			InvalidArgumentException {
		return null;
	}

	private CalendarDataCollection getAvailableDailyData(
			List<String> stationIDs, DataType type) throws RemoteException,
			InvalidArgumentException {
		if (type != null) {
			type = DataType.valueOf(type.name());
		}

		/* enforces a specific set of types which can be queried */
		switch (type) {
		case DISCHARGE_MEAN:
			break;
		case GAGE_HEIGHT_MEAN:
			break;
		case GAGE_HEIGHT_MAX:
			break;
		case GAGE_HEIGHT_MIN:
			break;
		default:
			throw new InvalidArgumentException(
					"method is not applicable for type " + type.name());
		}

		TemporalPeriod longest_period = null;
		try {
			longest_period = metaDataQuery.getLongestPeriod(stationIDs, CalendarPeriod.DAILY, type);
		} catch (RemoteException jme) {
			LOG.error("could not get StreamFlowMetaDataObject", jme);
			throw new RemoteException("could not get StreamFlowMetaDataObject");
		} catch (RuntimeException ie) {
			LOG.error("could not instantiate a DataServiceAccessor", ie);
			throw new RemoteException(
					"could not instantiate a DataServiceAccessor");
		}

		return this.getPeriodDailyData(stationIDs, longest_period.getStart(),
				longest_period.getEnd(), type);
	}

	@Override
	public CalendarDataCollection getAvailableData(List<String> stationIDs,
			DataType type, CalendarPeriod period) throws RemoteException,
			InvalidArgumentException {
		
		switch ( period )  { 
		case DAILY:
			return this.getAvailableDailyData(stationIDs, type);
		case WEEKLY:
			return this.getAvailableWeeklyData(stationIDs, type);
		case MONTHLY:
			return this.getAvailableMonthlyData(stationIDs, type);
		case ANNUALLY:
			return this.getAvailableAnnualData(stationIDs, type);
		default:
			throw new InvalidArgumentException("An invalid calendar period was specified");
		}
	}

	private CalendarDataCollection getAvailableMonthlyData(
			List<String> stationIDs, DataType type) throws RemoteException,
			InvalidArgumentException {

		/* enforces a specific set of types which can be queried */
		switch (type) {
		case DISCHARGE_MEAN:
			break;
		case GAGE_HEIGHT_MEAN:
			break;
		case GAGE_HEIGHT_MAX:
			break;
		case GAGE_HEIGHT_MIN:
			break;
		default:
			throw new InvalidArgumentException(
					"method is not applicable for type " + type.name());
		}

		TemporalPeriod longest_period = null;
		try {
			longest_period = metaDataQuery.getLongestPeriod(stationIDs,
					CalendarPeriod.MONTHLY, type);
		} catch (RemoteException jme) {
			LOG.error("could not get StreamFlowMetaDataObject", jme);
			throw new RemoteException("could not get StreamFlowMetaDataObject");
		} catch (RuntimeException ie) {
			LOG.error("could not instantiate a DataServiceAccessor", ie);
			throw new RemoteException(
					"could not instantiate a DataServiceAccessor");
		}

		return this.getPeriodMonthlyData(stationIDs, longest_period.getStart(),
				longest_period.getEnd(), type);
	}

	private CalendarDataCollection getAvailableWeeklyData(
			List<String> stationIDs, DataType type) throws RemoteException,
			InvalidArgumentException {

		/* enforces a specific set of types which can be queried */
		switch (type) {
		case DISCHARGE_MEAN:
			break;
		case GAGE_HEIGHT_MEAN:
			break;
		case GAGE_HEIGHT_MAX:
			break;
		case GAGE_HEIGHT_MIN:
			break;
		default:
			throw new InvalidArgumentException(
					"method is not applicable for type " + type.name());
		}

		TemporalPeriod longest_period = null;
		try {
			longest_period = metaDataQuery.getLongestPeriod(stationIDs,
					CalendarPeriod.WEEKLY, type);
		} catch (RemoteException jme) {
			LOG.error("could not get StreamFlowMetaDataObject", jme);
			throw new RemoteException("could not get StreamFlowMetaDataObject");
		} catch (RuntimeException ie) {
			LOG.error("could not instantiate a DataServiceAccessor", ie);
			throw new RemoteException(
					"could not instantiate a DataServiceAccessor");
		}

		return getPeriodWeeklyData(stationIDs, longest_period.getStart(),
				longest_period.getEnd(), type);
	}
	
	private CalendarDataCollection getDailyDataNormals(List<String> stationIDs,
			int startYear, int endYear, DataType type) throws RemoteException,
			InvalidArgumentException {
		switch (type) {
		case DISCHARGE_MEAN:
			break;
		case GAGE_HEIGHT_MEAN:
			break;
		case GAGE_HEIGHT_MAX:
			break;
		case GAGE_HEIGHT_MIN:
			break;
		default:
			throw new InvalidArgumentException(
					"method is not applicable for type " + type.name());
		}

		DateTime startDate = new DateTime(startYear, 1, 1, 0, 0, 0, 0,
				GregorianChronology.getInstance());
		DateTime endDate = new DateTime(endYear, 12, 31, 0, 0, 0, 0,
				GregorianChronology.getInstance());

		CalendarDataCollection period_data = getPeriodDailyData(stationIDs,
				startDate, endDate, type);

		try {
			return ExtendedDataCalculations
					.computeAveragePeriodData(period_data);
		} catch (InvalidStateException ise) {
			LOG
					.error(
							"could not compute data normals, the object state is invalid",
							ise);
			RemoteException re = new RemoteException();
			re.initCause(ise);

			throw re;
		}
	}

	@Override
	public CalendarDataCollection getDataNormals(List<String> stationIDs,
			int startYear, int endYear, DataType type, CalendarPeriod period)
			throws RemoteException, InvalidArgumentException {

		switch (period) {
		case DAILY:
			return this.getDailyDataNormals(stationIDs, startYear, endYear,
					type);
		case WEEKLY:
			return this.getWeeklyDataNormals(stationIDs, startYear, endYear,
					type);
		case MONTHLY:
			return this.getMonthlyDataNormals(stationIDs, startYear, endYear,
					type);
		case ANNUALLY:
			return this.getAnnualDataNormals(stationIDs, startYear, endYear,
					type);
		default:
			throw new InvalidArgumentException(
					"An invalid calendar period was specified");
		}
	}

	private CalendarDataCollection getHistoricalAverageAnnualData(
			List<String> stationIDs, DataType type) throws RemoteException,
			InvalidArgumentException {

		/* enforces a specific set of types which can be queried */
		switch (type) {
		case DISCHARGE_MEAN:
			break;
		case GAGE_HEIGHT_MEAN:
			break;
		case GAGE_HEIGHT_MAX:
			break;
		case GAGE_HEIGHT_MIN:
			break;
		default:
			throw new InvalidArgumentException(
					"method is not applicable for type " + type.name());
		}

		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = source.getConnection();

			stmt = conn
					.prepareStatement(StreamFlowQueryStrings.AVERAGE_DAILY_BASE
							.getQueryString());
			CalendarDataCollection cdc = ExtendedDataCalculations
					.computeAveragePeriodData(stmt, type, CalendarPeriod.DAILY,
							stationIDs);

			DateTime begin = new DateTime(2000, 1, 1, 0, 0, 0, 0,
					GregorianChronology.getInstance());
			DateTime end = new DateTime(2000, 12, 31, 0, 0, 0, 0,
					GregorianChronology.getInstance());
			YearDataBuilder builder = new YearDataBuilder(begin, end,
					CalendarPeriod.ANNUALLY, type);

			for (String station : cdc) {
				builder.openStation(station);
				float avg = 0;
				for (float d : cdc.getDataMatrix(station)[0]) {
					avg += d;
				}
				builder.add(avg / 366);
				builder.writeStation();
			}

			return builder.returnCollection();

		} catch (SQLException sqe) {
			LOG.error("sql exception querying data for type " + type.name(),
					sqe);
			throw new RemoteException(
					"unable to query data from data base for type "
							+ type.name());
		} catch (InvalidStateException ise) {
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally {
			try {
				stmt.close();
				conn.close();
			} catch (SQLException sqe2) {
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
	}

	private CalendarDataCollection getHistoricalAverageDailyData(
			List<String> stationIDs, DataType type) throws RemoteException,
			InvalidArgumentException {
		/* enforces a specific set of types which can be queried */
		switch (type) {
		case DISCHARGE_MEAN:
			break;
		case GAGE_HEIGHT_MEAN:
			break;
		case GAGE_HEIGHT_MAX:
			break;
		case GAGE_HEIGHT_MIN:
			break;
		default:
			throw new InvalidArgumentException(
					"method is not applicable for type " + type.name());
		}

		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = source.getConnection();
			stmt = conn
					.prepareStatement(StreamFlowQueryStrings.AVERAGE_DAILY_BASE
							.getQueryString());
			return ExtendedDataCalculations.computeAveragePeriodData(stmt,
					type, CalendarPeriod.DAILY, stationIDs);

		} catch (SQLException sqe) {
			LOG.error("sql exception querying data for type " + type.name(),
					sqe);
			throw new RemoteException(
					"unable to query data from data base for type "
							+ type.name());
		} catch (InvalidStateException ise) {
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally {
			try {
				stmt.close();
				conn.close();
			} catch (SQLException sqe2) {
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
	}

	@Override
	public CalendarDataCollection getHistoricalAverageData(
			List<String> stationIDs, DataType type, CalendarPeriod period)
			throws RemoteException, InvalidArgumentException {
		switch (period) {
		case DAILY:
			return this.getHistoricalAverageDailyData(stationIDs, type);
		case WEEKLY:
			return this.getHistoricalAverageWeeklyData(stationIDs, type);
		case MONTHLY:
			return this.getHistoricalAverageMonthlyData(stationIDs, type);
		case ANNUALLY:
			return this.getHistoricalAverageAnnualData(stationIDs, type);
		default:
			throw new InvalidArgumentException(
					"An invalid calendar period was specified");
		}
	}

	private CalendarDataCollection getHistoricalAverageMonthlyData(
			List<String> stationIDs, DataType type) throws RemoteException,
			InvalidArgumentException {
		/* enforces a specific set of types which can be queried */
		switch (type) {
		case DISCHARGE_MEAN:
			break;
		case GAGE_HEIGHT_MEAN:
			break;
		case GAGE_HEIGHT_MAX:
			break;
		case GAGE_HEIGHT_MIN:
			break;
		default:
			throw new InvalidArgumentException(
					"method is not applicable for type " + type.name());
		}

		PreparedStatement stmt = null;
		Connection conn = null;
		try {
			conn = source.getConnection();
			stmt = conn
					.prepareStatement(StreamFlowQueryStrings.AVERAGE_MONTHLY_BASE
							.getQueryString());
			return ExtendedDataCalculations.computeAveragePeriodData(stmt,
					type, CalendarPeriod.MONTHLY, stationIDs);

		} catch (SQLException sqe) {
			LOG.error("sql exception querying data for type " + type.name(),
					sqe);
			throw new RemoteException(
					"unable to query data from data base for type "
							+ type.name());
		} catch (InvalidStateException ise) {
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally {
			try {
				stmt.close();
				conn.close();
			} catch (SQLException sqe2) {
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
	}

	private CalendarDataCollection getHistoricalAverageWeeklyData(
			List<String> stationIDs, DataType type) throws RemoteException,
			InvalidArgumentException {

		/* enforces a specific set of types which can be queried */
		switch (type) {
		case DISCHARGE_MEAN:
			break;
		case GAGE_HEIGHT_MEAN:
			break;
		case GAGE_HEIGHT_MAX:
			break;
		case GAGE_HEIGHT_MIN:
			break;
		default:
			throw new InvalidArgumentException(
					"method is not applicable for type " + type.name());
		}

		PreparedStatement stmt = null;
		Connection conn = null;
		try {
			conn = source.getConnection();
			stmt = conn
					.prepareStatement(StreamFlowQueryStrings.AVERAGE_WEEKLY_BASE
							.getQueryString());
			return ExtendedDataCalculations.computeAveragePeriodData(stmt,
					type, CalendarPeriod.WEEKLY, stationIDs);

		} catch (SQLException sqe) {
			LOG.error("sql exception querying data for type " + type.name(),
					sqe);
			throw new RemoteException(
					"unable to query data from data base for type "
							+ type.name());
		} catch (InvalidStateException ise) {
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally {
			try {
				stmt.close();
				conn.close();
			} catch (SQLException sqe2) {
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
	}

	private CalendarDataCollection getMonthlyDataNormals(
			List<String> stationIDs, int startYear, int endYear, DataType type)
			throws RemoteException, InvalidArgumentException {
		/* enforces a specific set of types which can be queried */
		switch (type) {
		case DISCHARGE_MEAN:
			break;
		case GAGE_HEIGHT_MEAN:
			break;
		case GAGE_HEIGHT_MAX:
			break;
		case GAGE_HEIGHT_MIN:
			break;
		default:
			throw new InvalidArgumentException(
					"method is not applicable for type " + type.name());
		}

		DateTime startDate = new DateTime(startYear, 1, 1, 0, 0, 0, 0,
				GregorianChronology.getInstance());
		DateTime endDate = new DateTime(endYear, 12, 31, 0, 0, 0, 0,
				GregorianChronology.getInstance());

		CalendarDataCollection period_data = getPeriodMonthlyData(stationIDs,
				startDate, endDate, type);

		try {
			return ExtendedDataCalculations
					.computeAveragePeriodData(period_data);
		} catch (InvalidStateException ise) {
			LOG
					.error(
							"could not compute data normals, the object state is invalid",
							ise);
			RemoteException re = new RemoteException();
			re.initCause(ise);

			throw re;
		}
	}

	private CalendarDataCollection getPeriodAnnualData(List<String> stationIDs,
			DateTime firstDate, DateTime lastDate, DataType type)
			throws RemoteException, InvalidArgumentException {

		switch (type) {
		case DISCHARGE_MEAN:
			break;
		case GAGE_HEIGHT_MEAN:
			break;
		case GAGE_HEIGHT_MAX:
			break;
		case GAGE_HEIGHT_MIN:
			break;
		default:
			throw new InvalidArgumentException(
					"method is not applicable for type " + type.name());
		}
		Connection conn = null;
		YearDataBuilder builder = new YearDataBuilder(firstDate, lastDate,
				CalendarPeriod.ANNUALLY, type);
		int first_year = firstDate.getYear();
		int last_year = lastDate.getYear();

		/* adjust for 53rd week */
		int first_week = firstDate.getWeekOfWeekyear();
		int last_week = lastDate.getWeekOfWeekyear();
		int begin_month = firstDate.getMonthOfYear();
		int end_month = lastDate.getMonthOfYear();

		if (first_week == 53) {
			if (begin_month == 1) {
				first_week = 1;
			} else {
				first_week = 52;
			}
		} else if (first_week == 52 && begin_month == 1) {
			first_week = 1;
		} else if (first_week == 1 && begin_month == 12) {
			first_week = 52;
		}

		if (last_week == 53) {
			if (end_month == 1) {
				last_week = 1;
			} else {
				last_week = 52;
			}
		} else if (last_week == 52 && end_month == 1) {
			last_week = 1;
		} else if (last_week == 1 && end_month == 12) {
			last_week = 52;
		}

		ResultSet query_result = null;
		PreparedStatement stmt = null;
		try {
			conn = source.getConnection();
			if (type == DataType.DISCHARGE_MEAN) {
				stmt = conn.prepareStatement(StreamFlowQueryStrings.ANNUAL_BASE
						.getQueryString());
				// } else if ( type == DataType.HIGH_TEMP ) {
				// stmt =
				// conn.prepareStatement(ClimateQueryStrings.ANNUAL_BASE_AVG.getQueryString());
				// } else if ( type == DataType.LOW_TEMP ) {
				// stmt =
				// conn.prepareStatement(ClimateQueryStrings.ANNUAL_BASE_AVG.getQueryString());
			}
			stmt.setString(2, type.name());
			stmt.setString(11, type.name());

			for (String station : stationIDs) {
				builder.openStation(station);
				stmt.setString(1, station);
				stmt.setInt(3, first_year);
				stmt.setInt(4, first_year);
				stmt.setInt(5, first_week);
				stmt.setInt(6, last_year);
				stmt.setInt(7, last_year);
				stmt.setInt(8, last_week);
				stmt.setInt(9, DataType.WEEKS_IN_YEAR_THRESHOLD);

				stmt.setInt(10, first_year); // start of the year range
				stmt.setInt(11, last_year); // end of the range of years

				query_result = stmt.executeQuery();

				if (!query_result.next()) {
					emptyResult(builder, firstDate, false);
					continue;
				}
				do {
					builder.add(query_result.getFloat(1));
				} while (query_result.next());

				query_result.last();
				builder.writeStation();
			}
		} catch (SQLException sqe) {
			LOG.error("sql exception querying data for type " + type.name(),
					sqe);
			throw new RemoteException(
					"unable to query data from data base for type "
							+ type.name());
		} catch (InvalidStateException ise) {
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally {
			try {
				if (query_result != null) {
					query_result.close();
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
		return builder.returnCollection();
	}

	private CalendarDataCollection getPeriodDailyData(List<String> stationIDs,
			DateTime firstDate, DateTime lastDate, DataType type)
			throws RemoteException, InvalidArgumentException {

		/* enforces a specific set of types which can be queried */
		switch ( type ) {
		case DISCHARGE_MEAN:
			break;
		case GAGE_HEIGHT_MEAN:
			break;
		case GAGE_HEIGHT_MAX:
			break;
		case GAGE_HEIGHT_MIN:
			break;
		default:
			throw new InvalidArgumentException("method is not applicable for type "+type.name());		
		}
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet query_result = null;
		YearDataBuilder builder = new YearDataBuilder(firstDate, lastDate, CalendarPeriod.DAILY, type);
		try { 
			conn = source.getConnection();
			stmt = conn.prepareStatement(StreamFlowQueryStrings.DAILY_BASE.getQueryString());
			stmt.setString(2, type.name());
			stmt.setInt(3, firstDate.getYear());
			stmt.setInt(4, firstDate.getYear());
			stmt.setInt(5, firstDate.getMonthOfYear());
			stmt.setInt(6, firstDate.getMonthOfYear());
			stmt.setInt(7, firstDate.getDayOfMonth());		
			stmt.setInt(8, lastDate.getYear());
			stmt.setInt(9, lastDate.getYear());
			stmt.setInt(10, lastDate.getMonthOfYear());
			stmt.setInt(11, lastDate.getMonthOfYear());
			stmt.setInt(12, lastDate.getDayOfMonth());
						
			for ( String station : stationIDs ) { 
				builder.openStation(station);
				stmt.setString(1, station);
				query_result = stmt.executeQuery();
				
				if ( !query_result.next() ) {
					emptyResult(builder, firstDate, true);
					continue;
				}
				
				/* the query actual is the first date for the specific station on which data is reported this is the starting boundry */
				DateTime queryActual = new DateTime(query_result.getInt(3), query_result.getInt(2), query_result.getInt(1), 0, 0, 0, 0, GregorianChronology.getInstance());
				int real_day = firstDate.getDayOfYear();
				int actual_day = queryActual.getDayOfYear();
						
				/* if the year is not a leap year and the date is past 02/28
				 * the counter is incremented by 1 to adjust for the NON-EXISTANT
				 * value which the builder puts in the array (NOTE: all FIRM years have 366 days)
				 */
				if ( !(firstDate.year().isLeap()) && (real_day > 59) )
					real_day++;
				
				if ( !(queryActual.year().isLeap()) && (actual_day > 59) )
					actual_day++;
				
				missingPopulate(firstDate.getYear(), queryActual.getYear(), real_day, actual_day, CalendarPeriod.DAILY, builder);
				
				do { 
					builder.add(query_result.getFloat(4));
				} while ( query_result.next() );
				
				query_result.last();
				
				/* the ending data boundry for this station */
				queryActual = new DateTime(query_result.getInt(3), query_result.getInt(2), query_result.getInt(1), 0, 0, 0, 0, GregorianChronology.getInstance());
				real_day = lastDate.getDayOfYear();
				actual_day = queryActual.getDayOfYear();
				if ( !(lastDate.year().isLeap()) && (real_day > 59) )
					real_day++;
				
				if ( !(queryActual.year().isLeap()) && (actual_day > 59) )
					actual_day++;
				
				missingPopulate(queryActual.getYear(), lastDate.getYear(), actual_day, real_day, CalendarPeriod.DAILY, builder);
				builder.writeStation();
			}
		} catch ( SQLException sqe ) { 
			LOG.error("sql exception querying data for type "+type.name(), sqe);
			throw new RemoteException("unable to query data from data base for type "+type.name());
		} catch (  InvalidStateException ise ) { 
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally { 
			try {
				if ( query_result != null ) {
					query_result.close();
				} 
				
				if ( stmt != null ) { 
					stmt.close();
				}
				
				if ( conn != null ) {
					conn.close();
				}
			} catch ( SQLException sqe2 ) { 
				LOG.error("could not close the connection", sqe2);
				throw new RemoteException("could not close connection");
			}
		}
		
		return builder.returnCollection();
	}

	private CalendarDataCollection getPeriodMonthlyData(
			List<String> stationIDs, DateTime firstDate, DateTime lastDate,
			DataType type) throws RemoteException, InvalidArgumentException {
		/* enforces a specific set of types which can be queried */
		switch (type) {
		case DISCHARGE_MEAN:
			break;
		case GAGE_HEIGHT_MEAN:
			break;
		case GAGE_HEIGHT_MAX:
			break;
		case GAGE_HEIGHT_MIN:
			break;
		default:
			throw new InvalidArgumentException(
					"method is not applicable for type " + type.name());
		}

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet query_result = null;
		YearDataBuilder builder = new YearDataBuilder(firstDate, lastDate,
				CalendarPeriod.MONTHLY, type);
		try {
			conn = source.getConnection();
			stmt = conn.prepareStatement(StreamFlowQueryStrings.MONTHLY_BASE
					.getQueryString());
			stmt.setString(2, type.name());
			stmt.setInt(3, firstDate.getYear());
			stmt.setInt(4, firstDate.getYear());
			stmt.setInt(5, firstDate.getMonthOfYear());
			stmt.setInt(6, lastDate.getYear());
			stmt.setInt(7, lastDate.getYear());
			stmt.setInt(8, lastDate.getMonthOfYear());

			for (String station : stationIDs) {
				builder.openStation(station);
				stmt.setString(1, station);
				query_result = stmt.executeQuery();

				if (!query_result.next()) {
					emptyResult(builder, firstDate, false);
					continue;
				}

				missingPopulate(firstDate.getYear(), query_result.getInt(2),
						firstDate.getMonthOfYear(), query_result.getInt(1),
						CalendarPeriod.MONTHLY, builder);

				do {
					builder.add(query_result.getFloat(3));
				} while (query_result.next());

				query_result.last();
				missingPopulate(query_result.getInt(2), lastDate.getYear(),
						query_result.getInt(1), lastDate.getMonthOfYear(),
						CalendarPeriod.MONTHLY, builder);

				builder.writeStation();
			}
		} catch (SQLException sqe) {
			LOG.error("sql exception querying data for type " + type.name(),
					sqe);
			throw new RemoteException(
					"unable to query data from data base for type "
							+ type.name());
		} catch (InvalidStateException ise) {
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally {
			try {
				if (query_result != null) {
					query_result.close();
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

		return builder.returnCollection();
	}

	private CalendarDataCollection getPeriodWeeklyData(List<String> stationIDs,
			DateTime firstDate, DateTime lastDate, DataType type)
			throws RemoteException, InvalidArgumentException {

		/* enforces a specific set of types which can be queried */
		switch (type) {
		case DISCHARGE_MEAN:
			break;
		case GAGE_HEIGHT_MEAN:
			break;
		case GAGE_HEIGHT_MAX:
			break;
		case GAGE_HEIGHT_MIN:
			break;
		default:
			throw new InvalidArgumentException(
					"method is not applicable for type " + type.name());
		}
		int first_year = firstDate.getYear();
		int last_year = lastDate.getYear();

		/* adjust for 53rd week */
		int first_week = firstDate.getWeekOfWeekyear();
		int last_week = lastDate.getWeekOfWeekyear();
		int begin_month = firstDate.getMonthOfYear();
		int end_month = lastDate.getMonthOfYear();

		if (first_week == 53) {
			if (begin_month == 1) {
				first_week = 1;
			} else {
				first_week = 52;
			}
		} else if (first_week == 52 && begin_month == 1) {
			first_week = 1;
		} else if (first_week == 1 && begin_month == 12) {
			first_week = 52;
		}

		if (last_week == 53) {
			if (end_month == 1) {
				last_week = 1;
			} else {
				last_week = 52;
			}
		} else if (last_week == 52 && end_month == 1) {
			last_week = 1;
		} else if (last_week == 1 && end_month == 12) {
			last_week = 52;
		}

		Connection conn = null;
		YearDataBuilder builder = new YearDataBuilder(firstDate, lastDate,
				CalendarPeriod.WEEKLY, type);
		PreparedStatement stmt = null;
		ResultSet query_result = null;
		try {
			conn = source.getConnection();
			stmt = conn.prepareStatement(StreamFlowQueryStrings.WEEKLY_BASE
					.getQueryString());
			stmt.setString(2, type.name());
			stmt.setInt(3, first_year);
			stmt.setInt(4, first_year);
			stmt.setInt(5, first_week);
			stmt.setInt(6, last_year);
			stmt.setInt(7, last_year);
			stmt.setInt(8, last_week);

			for (String station : stationIDs) {
				builder.openStation(station);
				stmt.setString(1, station);
				query_result = stmt.executeQuery();

				if (!query_result.next()) {
					emptyResult(builder, firstDate, false);
					continue;
				}

				missingPopulate(first_year, query_result.getInt(2), first_week,
						query_result.getInt(1), CalendarPeriod.WEEKLY, builder);

				do {
					builder.add(query_result.getFloat(3));
				} while (query_result.next());

				query_result.last();
				missingPopulate(query_result.getInt(2), last_year, query_result
						.getInt(1), last_week, CalendarPeriod.WEEKLY, builder);

				builder.writeStation();
			}
		} catch (SQLException sqe) {
			LOG.error("sql exception querying data for type " + type.name(),
					sqe);
			throw new RemoteException(
					"unable to query data from data base for type "
							+ type.name());
		} catch (InvalidStateException ise) {
			LOG.error("invalid state for data builder", ise);
			throw new RemoteException("could not load data into builder");
		} finally {
			try {
				if (query_result != null) {
					query_result.close();
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

		return builder.returnCollection();
	}

	@Override
	public CalendarDataCollection getStreamFlowPeriodData(
			List<String> stationIDs, DateTime firstDate, DateTime lastDate,
			DataType type, CalendarPeriod period) throws RemoteException,
			InvalidArgumentException {

		switch (period) {
		case DAILY:
			return this.getPeriodDailyData(stationIDs, firstDate, lastDate, type);
		case WEEKLY:
			return this.getPeriodWeeklyData(stationIDs, firstDate, lastDate, type);
		case MONTHLY:
			return this.getPeriodMonthlyData(stationIDs, firstDate, lastDate, type);
		case ANNUALLY:
			return this.getPeriodAnnualData(stationIDs, firstDate, lastDate, type);
		default:
			throw new InvalidArgumentException(
					"An invalid calendar period was specified");
		}

	}

	private CalendarDataCollection getWeeklyDataNormals(
			List<String> stationIDs, int startYear, int endYear, DataType type)
			throws RemoteException, InvalidArgumentException {
		/* enforces a specific set of types which can be queried */
		switch (type) {
		case DISCHARGE_MEAN:
			break;
		case GAGE_HEIGHT_MEAN:
			break;
		case GAGE_HEIGHT_MAX:
			break;
		case GAGE_HEIGHT_MIN:
			break;
		default:
			throw new InvalidArgumentException(
					"method is not applicable for type " + type.name());
		}

		DateTime startDate = new DateTime(startYear, 1, 1, 0, 0, 0, 0,
				GregorianChronology.getInstance());
		DateTime endDate = new DateTime(endYear, 12, 31, 0, 0, 0, 0,
				GregorianChronology.getInstance());

		CalendarDataCollection period_data = getPeriodWeeklyData(stationIDs,
				startDate, endDate, type);

		try {
			return ExtendedDataCalculations
					.computeAveragePeriodData(period_data);
		} catch (InvalidStateException ise) {
			LOG
					.error(
							"could not compute data normals, the object state is invalid",
							ise);
			RemoteException re = new RemoteException();
			re.initCause(ise);

			throw re;
		}
	}

	private void missingPopulate(int begin_year, int end_year,
			int begin_period, int end_period, CalendarPeriod type,
			YearDataBuilder builder) throws InvalidStateException {
		while (true) {
			while (begin_period < end_period) {
				builder.add(DataType.OUTSIDE_OF_RANGE);
				begin_period++;
			}

			while (begin_year < end_year) {
				while (begin_period <= type.getLength()) {
					builder.add(DataType.OUTSIDE_OF_RANGE);
					begin_period++;
				}

				begin_year++;
				begin_period = 1;
			}

			if (begin_year == end_year && begin_period == end_period)
				return;
		}
	}

}
