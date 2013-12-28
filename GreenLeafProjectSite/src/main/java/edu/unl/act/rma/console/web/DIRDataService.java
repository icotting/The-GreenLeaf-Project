/* Created On Nov 30, 2006 */
package edu.unl.act.rma.console.web;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;

import javax.sql.DataSource;

import net.sourceforge.jtds.jdbcx.JtdsDataSource;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.component.SpatialQuery;
import edu.unl.act.rma.firm.core.configuration.SpatialServiceAccessor;
import edu.unl.act.rma.firm.core.spatial.SpatialReferenceType;
import edu.unl.act.rma.firm.core.spatial.USCity;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.drought.DroughtReportCategory;

/**
 * @author Laura Meerkatz
 * 
 * Upated by Ian Cottingham 2010.02.02
 * 
 */
public class DIRDataService extends Thread {

	private static final Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG,
			DIRDataService.class);

	public static final String NAME = "DirService";
	
	private DataSource firmSource = DataSourceInjector.injectDataSource(DataSourceTypes.SYSTEM);
	
	private int totalUnits;
	private int completeUnits;
	private DateTime startTime;
	private DateTime endTime;
	private String status;
	private final int startId;
	private final int endId;
	
	private PreparedStatement linkStatement;
	private HashMap<Integer, Long> impactIdMap;
	private HashMap<Integer, Long> reportIdMap;
	
	public double getPercentComplete() {
		return ((double) completeUnits / (double) totalUnits);
	}

	public String getStatus() {
		return status;
	}

	public DIRDataService(int startId, int endId) {
		 this.startId = startId;
		 this.endId = endId;

		totalUnits = 1;
		completeUnits = 0;
	}

	public String getRunTime() {
		Period p;
		if (endTime == null) {
			p = new Period(startTime, new DateTime(System.currentTimeMillis()),
					PeriodType.seconds());
		} else {
			p = new Period(startTime, endTime, PeriodType.seconds());
		}

		float seconds = p.getSeconds();
		float minutes = (seconds / 60);
		float hours = (minutes / 60);

		return (int) hours + "hr " + (int) (minutes % 60) + "min "
				+ (int) (seconds % 60) + "sec";
	}

	@Override
	public void run() {
		impactIdMap = new HashMap<Integer, Long>();
		reportIdMap = new HashMap<Integer, Long>();
		
		startTime = new DateTime(System.currentTimeMillis());
		Connection conn = null;
		Connection firm_conn = null;

		boolean error = false;
		try {
			DIRDataServiceManager mgr = DIRDataServiceManager.getInstance();
			JtdsDataSource source = new JtdsDataSource();
			source.setServerName(mgr.getDatabaseHost());
			source.setPortNumber(Integer.parseInt(mgr.getDatabasePort()));
			source.setUser(mgr.getDatabaseUsername());
			source.setPassword(mgr.getDatabasePassword());
			source.setDatabaseName(mgr.getDatabaseName());
			conn = source.getConnection();
			
			firm_conn = firmSource.getConnection();
			firm_conn.setAutoCommit(false);
		} catch (InstantiationException ie) {
			LOG.error("could not get the dataset service manager", ie);
			return;
		} catch (RemoteException re) {
			LOG.error("error reading db connection property", re);
			return;
		} catch (SQLException sqe) {
			LOG.error("could not get the db connection", sqe);
			return;
		} catch (RuntimeException re) {
			LOG.error("unknown error", re);
			return;
		}

		try {
			linkStatement = firm_conn.prepareStatement("insert into DroughtImpacts_DroughtReports (impacts_impact_id, reports_report_id) values (?, ?)");
			
			// determine how many reports will be processed			
			PreparedStatement sql_count = conn.prepareStatement("select count(distinct(i.ImpactID)) as impacts, count(distinct(r.ReportID)) " +
					"as reports from dbo.CseImpacts as i inner join dbo.CseReports as r on i.ReportID = r.ReportID where i.impactID >= ? and i.impactID <= ?");
			sql_count.setInt(1, startId);
			sql_count.setInt(2, endId);
			
			
			ResultSet rsCount = sql_count.executeQuery();
			rsCount.next();
			totalUnits = rsCount.getInt(1) + rsCount.getInt(2);
			rsCount.close();
						
			ResultSet id_query = firm_conn.createStatement().executeQuery("select max(report_id) from DroughtReports");
			long report_id = ((id_query.next()) ? id_query.getLong(1) : 0)+1;
			id_query.close();
			
			id_query = firm_conn.createStatement().executeQuery("select max(impact_id) from DroughtImpacts");
			long impact_id = ((id_query.next()) ? id_query.getLong(1) : 0)+1;
			id_query.close();
			
			id_query = firm_conn.createStatement().executeQuery("select max(categoryId) from ReportCategories");
			long category_id = ((id_query.next()) ? id_query.getLong(1) : 0)+1;
			id_query.close();
			
			id_query = firm_conn.createStatement().executeQuery("select max(ref_id) from SpatialReferences");
			long reference_id = ((id_query.next()) ? id_query.getLong(1) : 0)+1;
			id_query.close();
			
			PreparedStatement stmt = conn.prepareStatement("select ImpactID, Title, Start_date, End_date, Summary from " +
					"dbo.CseImpacts where dbo.CseImpacts.ImpactID >= ? and CseImpacts.ImpactID <= ? group by ImpactID, " +
					"Title, Start_date, End_date, Summary");
			stmt.setInt(1, startId);
			stmt.setInt(2, endId);
			
			PreparedStatement impact_insert = firm_conn.prepareStatement("insert into DroughtImpacts (impact_id, summary, start_date, title, legacy_id, " +
			"end_date) values (?,?,?,?,?,?)");
			
			impact_id = processImpacts(stmt, impact_insert, impact_id);
			
			stmt = conn.prepareStatement("select ImpactID, Categories, Losses from dbo.CseImpacts where " +
					"dbo.CseImpacts.ImpactID >= ? and CseImpacts.ImpactID <= ? group by ImpactID, Categories, Losses");
			stmt.setInt(1, startId);
			stmt.setInt(2, endId);
			
			PreparedStatement category_insert_statement = firm_conn.prepareStatement("insert into ReportCategories (categoryId, IMPACTCATEGORY, dollar_loss, impact_report, media_report) values (?,?,?,?,?)");

			category_id = processCategories(stmt, category_insert_statement, category_id, true);
			
			stmt = conn.prepareStatement("select ImpactID, StateAbbreviation, County, City from dbo.CseImpacts where " +
					"dbo.CseImpacts.ImpactID >= ? and CseImpacts.ImpactID <= ? group by ImpactID, StateAbbreviation, County, City");
			stmt.setInt(1, startId);
			stmt.setInt(2, endId);
			
			PreparedStatement spatial_ref_insert = firm_conn.prepareStatement("insert into SpatialReferences (ref_id, ref_type, state, media_report, " +
					"impact_report, city_id, county_id) values (?,?,?,?,?,?,?)");
			
			reference_id = processSpatialReferences(stmt, spatial_ref_insert, reference_id, true);
				
			stmt = conn.prepareStatement("select reports.ReportID, reports.src, reports.Title, reports.pubdate, reports.URL, impacts.ImpactID " +
					"from dbo.CseReports as reports inner join dbo.CseImpacts as impacts on impacts.ReportID = reports.ReportID " +
					"where impacts.ImpactID >= ? and impacts.ImpactID <= ?" +
					"group by reports.ReportID, reports.src, reports.Title, reports.pubdate, reports.URL, impacts.ImpactID");
			stmt.setInt(1, startId);
			stmt.setInt(2, endId);
			
			PreparedStatement report_insert = firm_conn.prepareStatement("insert into DroughtReports (report_id, title, legacy_id, source, url, " +
					"publication_date) values (?,?,?,?,?,?)");
			
			
			report_id = processReports(stmt, report_insert, report_id);
			
			stmt = conn.prepareStatement("select reports.ReportID, reports.Categories, reports.Losses from dbo.CseReports as reports inner " +
					"join dbo.CseImpacts as impacts on impacts.ReportID = reports.ReportID where impacts.ImpactID >= ? and impacts.ImpactID <= ? " +
					"group by reports.ReportID, reports.Categories, reports.Losses");
			stmt.setInt(1, startId);
			stmt.setInt(2, endId);

			category_id = processCategories(stmt, category_insert_statement, category_id, false);
			
			stmt = conn.prepareStatement("select reports.ReportID, reports.StateAbbreviation, reports.County, reports.City from dbo.CseReports " +
					"as reports inner join dbo.CseImpacts as impacts on impacts.ReportID = reports.ReportID where impacts.ImpactID >= ? and " +
					"impacts.ImpactID <= ? group by reports.ReportID, reports.StateAbbreviation, reports.County, reports.City");
			stmt.setInt(1, startId);
			stmt.setInt(2, endId);
			
			reference_id = processSpatialReferences(stmt, spatial_ref_insert, reference_id, false);
			

			firm_conn.commit();
			stmt.close();
			
		} catch (SQLException sqe) {
			error = true;
			LOG.error("could not execute db query", sqe);
			try {
				this.status = "Process failed with error message: "+sqe.getMessage();
			} catch (Exception re) {
				LOG
						.warn(
								"the transaction could not be rolledback ... this is occurring in the context of another error",
								re);
			}
			return;
		} catch (InstantiationException ie) {
			error = true;			
			LOG.error("could not get the drought index service accessor", ie);
			try {
				this.status = "Process failed with error message: "+ie.getMessage();
			} catch (Exception re) {
				LOG
						.warn(
								"the transaction could not be rolled back ... this is occurring in the context of another error",
								re);
			}
			return;
		} catch (RemoteException re) {
			error = true;			
			LOG.error("could not call the query object", re);
			try {
				this.status = "Process failed with error message: "+re.getMessage();
			} catch (Exception exc) {
				LOG
						.warn(
								"the transaction could not be rolled back ... this is occurring in the context of another error",
								exc);
			}
			return;
		} catch (Exception e) {
			error = true;			
			LOG.error("unknown exception", e);
			try {
				this.status = "Process failed with error message: "+e.getMessage();
			} catch (Exception re) {
				LOG
						.warn(
								"the transaction could not be rolled back ... this is occurring in the context of another error",
								re);
			}
			return;
		} finally {
			try {
				conn.close();
				firm_conn.close();
				Runtime.getRuntime().gc();
			} catch (SQLException s) {
				// do nothing
			}
		}
		
		endTime = new DateTime(System.currentTimeMillis());
		this.status = "Process complete";
	}
	
	public DateTime endTime() { 
		return endTime;
	}
	
	private long processReports(PreparedStatement queryStatement, PreparedStatement insertStatement, Long id) throws Exception { 
		ResultSet report_results = queryStatement.executeQuery();
		Long impact_id; 
		Long report_id;
		
		while ( report_results.next() ) { 
			int legacy_impact_id = report_results.getInt(6);
			int legacy_report_id = report_results.getInt(1);
			
			this.status = "Processing report "+legacy_report_id;
			impact_id = impactIdMap.get(legacy_impact_id);
			report_id = reportIdMap.get(report_results.getInt(1));

			if ( report_id == null ) { 
				report_id = id++;
				status = "Adding new report "+report_id;
				insertStatement.setLong(1, report_id);
				insertStatement.setString(2, report_results.getString(3));
				insertStatement.setInt(3, legacy_report_id);
				insertStatement.setString(4, report_results.getString(2));
				insertStatement.setString(5, report_results.getString(5));
				insertStatement.setDate(6, report_results.getDate(4));
				
				insertStatement.execute();
				completeUnits++;
				
				reportIdMap.put(legacy_report_id, report_id);
			} else { 
				status = "Processing report "+report_id;
			}
			
			linkStatement.setLong(1, impact_id);
			linkStatement.setLong(2, report_id);
			linkStatement.execute();
		}
		
		report_results.close();
		queryStatement.close();
		
		return id;
	}
	
	private long processImpacts(PreparedStatement queryStatement, PreparedStatement insertStatement, Long id) throws Exception { 
		ResultSet impact_results = queryStatement.executeQuery();
	
		while (impact_results.next()) { 
			long impact_id = id++;
			int legacy_impact_id = impact_results.getInt(1);
			this.status = "Processing impact "+legacy_impact_id;
			
			insertStatement.setLong(1, impact_id);
			insertStatement.setString(2, impact_results.getString(5));
			insertStatement.setDate(3, impact_results.getDate(3));
			insertStatement.setString(4, impact_results.getString(2));
			insertStatement.setInt(5, legacy_impact_id);
			insertStatement.setDate(6, impact_results.getDate(4));
			
			insertStatement.execute();
			impactIdMap.put(legacy_impact_id, impact_id);
			completeUnits++;
		}
		
		impact_results.close();
		queryStatement.close();
		
		return id;
	}
		
	private long processCategories(PreparedStatement queryStatement, PreparedStatement insertStatement, Long id, boolean forImpacts) throws Exception { 
		ResultSet category_results = queryStatement.executeQuery();
		while ( category_results.next() ) { 
			int legacy_id = category_results.getInt(1);
			this.status = "Processing categories for "+(forImpacts ? "impact":"report")+" "+legacy_id;
			
			if ( forImpacts ) {
				insertStatement.setLong(1, id++);
				insertStatement.setInt(2, DroughtReportCategory.fromString(category_results.getString(2)).ordinal());
				insertStatement.setDouble(3, category_results.getDouble(3));
				insertStatement.setLong(4, impactIdMap.get(legacy_id));
				insertStatement.setNull(5, Types.BIGINT);

			} else { // is a report 
				insertStatement.setLong(1, id++);
				insertStatement.setInt(2, DroughtReportCategory.fromString(category_results.getString(2)).ordinal());
				insertStatement.setDouble(3, category_results.getDouble(3));
				insertStatement.setNull(4, Types.BIGINT);
				insertStatement.setLong(5, reportIdMap.get(legacy_id));
			}
			
			insertStatement.execute();
		}
		
		category_results.close();
		queryStatement.close();
		
		return id;
	}
	
	private long processSpatialReferences(PreparedStatement stmt, PreparedStatement insertStatement, long id, boolean forImpacts) throws Exception { 
		
		ResultSet spatial_references = stmt.executeQuery();
		SpatialQuery query = SpatialServiceAccessor.getInstance().getSpatialQuery();
		
		while ( spatial_references.next() ) { 
			int legacy_id = spatial_references.getInt(1);
			this.status = "Processing spatial references for "+(forImpacts ? "impact":"report")+" "+legacy_id;
			
			USState state = USState.fromPostalCode(spatial_references.getString(2));
			
			
			insertStatement.setLong(1, id++);
			
			if ( spatial_references.getString(4) != null ) { // reference is a city
				try {
					USCity city = query.getCityForName(filterCity(spatial_references.getString(4), state), state);
					
					insertStatement.setInt(2, SpatialReferenceType.US_CITY.ordinal());
					insertStatement.setInt(3, city.getCounty().getState().ordinal());
					if ( forImpacts ) { 
						insertStatement.setNull(4, Types.BIGINT);
						insertStatement.setLong(5, impactIdMap.get(legacy_id));
					} else { 
						insertStatement.setLong(4, reportIdMap.get(legacy_id));
						insertStatement.setNull(5, Types.BIGINT);
					}
					insertStatement.setLong(6, city.getCityId());
					insertStatement.setLong(7, city.getCounty().getCountyId());					
				} catch ( Exception e ) { 
					LOG.warn("No city was found for name: "+spatial_references.getString(4)+" "+state.name());
				}
			} else if ( spatial_references.getString(3) != null ) { // refrence is a county
				String county = spatial_references.getString(3);
				try { 	
					county = this.filterCounty(county, state.getPostalCode());
				} catch ( StringIndexOutOfBoundsException se ) { 
					// do nothing, the name doesn't need to be cleaned
				}

				try {
					USCounty county_obj = query.getCountyForName(county, state);
					
					insertStatement.setInt(2, SpatialReferenceType.US_COUNTY.ordinal());
					insertStatement.setInt(3, county_obj.getState().ordinal());
					if ( forImpacts ) { 
						insertStatement.setNull(4, Types.BIGINT);
						insertStatement.setLong(5, impactIdMap.get(legacy_id));
					} else { 
						insertStatement.setLong(4, reportIdMap.get(legacy_id));
						insertStatement.setNull(5, Types.BIGINT);
					}
					insertStatement.setNull(6, Types.BIGINT);
					insertStatement.setLong(7, county_obj.getCountyId());					
				} catch ( Exception e ) { 
					LOG.warn("No county was found for name: "+spatial_references.getString(3)+" "+state.name());
				}				
			} else {
				insertStatement.setInt(2, SpatialReferenceType.US_STATE.ordinal());
				insertStatement.setInt(3, state.ordinal());

				insertStatement.setNull(6, Types.BIGINT);
				insertStatement.setNull(7, Types.BIGINT);
			}
			
			if ( forImpacts ) { 
				insertStatement.setNull(4, Types.BIGINT);
				insertStatement.setLong(5, impactIdMap.get(legacy_id));
			} else { 
				insertStatement.setLong(4, reportIdMap.get(legacy_id));
				insertStatement.setNull(5, Types.BIGINT);
			}
			insertStatement.execute();
		}
		spatial_references.close();
		stmt.close();
		
		return id;
	}
	
	private String filterCounty(String county, String state_code) {
		county = county.replace(" County", "").replace(" Parish", "");
		if (state_code.equals("FL")) {
			if (county.equals("Saint Lucie")) {
				return county.replace("Saint", "St.");
			}
		} else if (state_code.equals("LA")) {
			if (county.equals("Saint Bernard")
					|| county.equals("Saint Charles")
					|| county.equals("Saint Helena")
					|| county.equals("Saint James")
					|| county.equals("Saint John the Baptist")
					|| county.equals("Saint Landry")
					|| county.equals("Saint Martin")
					|| county.equals("Saint Mary")
					|| county.equals("Saint Tammany")
				) {
				return county.replace("Saint", "St.");
			}
		} else if (state_code.equals("MN")) {
			if (county.equals("Saint Louis")) {
				return "St. Louis";
			}
		} else if (state_code.equals("NM")) {
			if (county.equals("DeBaca")) {
				return "De Baca";
			}
		} else if (state_code.equals("VA")) {
			if (county.equals("Buena Vista City")
					|| county.equals("Charlottesville City")
					|| county.equals("Colonial Heights City")
					|| county.equals("Hopewell City")
					|| county.equals("Lexington City")
					|| county.equals("Martinsville City")
					|| county.equals("Newport News City")
					|| county.equals("Petersburg City")
					|| county.equals("Suffolk City")) {
				return county.replace(" City", "");
			}
		} else if (state_code.equals("WI")) {
			if (county.equals("Saint Croix")) {
				return "St. Croix";
			}
		}

		return county;
	}
	
	private String filterCity(String city, USState state) { 
		if ( state.equals(USState.Kentucky) && city.equals("Lexington")) { 
			return "Lexington-Fayette";
		} else { 
			return city;
		}
	}
}
