/* Created On Nov 30, 2006 */
package edu.unl.act.rma.firm.drought.component;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.sql.DataSource;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.component.SpatialQuery;
import edu.unl.act.rma.firm.core.spatial.BoundingBox;
import edu.unl.act.rma.firm.core.spatial.SpatialReferenceType;
import edu.unl.act.rma.firm.core.spatial.USCity;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.drought.DroughtImpactDivisionStatistics;
import edu.unl.act.rma.firm.drought.DroughtImpactStatistics;
import edu.unl.act.rma.firm.drought.DroughtReportCategory;
import edu.unl.act.rma.firm.drought.ImpactBean;
import edu.unl.act.rma.firm.drought.PartialImpactBean;

/**
 * @author Ian Cottingham
 *
 */
@Stateless
@Remote({DroughtImpactQuery.class})
@TransactionAttribute(TransactionAttributeType.NEVER)
public class DroughtImpactQueryBean implements DroughtImpactQuery {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG, DroughtImpactQuery.class);
	
	/** The peristence manager, used to manage the bound entity beans. */
	@PersistenceContext(unitName="FirmCorePU", type=PersistenceContextType.TRANSACTION)
	private transient EntityManager manager;

	/* an injected reference to a SpatialQuery which is used to access server side functionality for searching */
	@EJB(name="SpatialQuery", beanInterface=SpatialQuery.class)
	private SpatialQuery spatialQuery;
		
	private DataSource source = DataSourceInjector.injectDataSource(DataSourceTypes.SYSTEM);
	
	@Override
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public List<PartialImpactBean> queryImpacts(DateTime startDate, DateTime endDate) {
		/*return manager.createQuery("select distinct i from DroughtImpact i where (i.startDate between " +
				":start and :end or i.endDate between :start and :end)").setParameter("start", startDate.toDate())
				.setParameter("end", endDate.toDate()).getResultList();
				
		*/
		
		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select impact_id, title, summary from DroughtImpacts where (start_date between ? and ?) " +
					"or (end_date between ? and ?) group by impact_id");
			
			stmt.setDate(1, new java.sql.Date(startDate.getMillis()));
			stmt.setDate(2, new java.sql.Date(endDate.getMillis()));			
			stmt.setDate(3, new java.sql.Date(startDate.getMillis()));
			stmt.setDate(4, new java.sql.Date(endDate.getMillis()));	
			
			return processSql(stmt.executeQuery());
			
		} catch ( Exception e ) { 
			LOG.error("An error occurred processing impacts", e);
			RuntimeException re = new RuntimeException("The impacts could not be queried");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception e ) { 
				LOG.warn("A connection could not be properly closed", e);
			}
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public List<PartialImpactBean> queryImpactsForCity(USCity city, DateTime startDate,
			DateTime endDate) {

		/*return manager.createQuery("select distinct i from DroughtImpact i, in (i.spatialReferences) as s where " +
			"(s.city = :city or (s.state = :state and s.referenceType = :statetype) or (s.county = :county and s.referenceType = :countytype)) " +
			"and (i.startDate between :start and :end or i.endDate between :start and :end)")
			.setParameter("city", city)
			.setParameter("state", city.getCounty().getState())
			.setParameter("county", city.getCounty())
			.setParameter("statetype", SpatialReferenceType.US_STATE)
			.setParameter("countytype", SpatialReferenceType.US_COUNTY)
			.setParameter("start", startDate.toDate())
			.setParameter("end", endDate.toDate())
			.getResultList();*/
		
		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select impact_id, title, summary from DroughtImpacts inner join SpatialReferences on " +
					"SpatialReferences.impact_report = impact_id inner join Cities on SpatialReferences.city_id = Cities.city_id where Cities.city_id = ? and" +
					" ((start_date between ? and ?) or (end_date between ? and ?)) group by impact_id");
			
			stmt.setLong(1, city.getCityId());
			stmt.setDate(2, new java.sql.Date(startDate.getMillis()));
			stmt.setDate(3, new java.sql.Date(endDate.getMillis()));			
			stmt.setDate(4, new java.sql.Date(startDate.getMillis()));
			stmt.setDate(5, new java.sql.Date(endDate.getMillis()));	
			
			return processSql(stmt.executeQuery());
			
		} catch ( Exception e ) { 
			LOG.error("An error occurred processing impacts", e);
			RuntimeException re = new RuntimeException("The impacts could not be queried");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception e ) { 
				LOG.warn("A connection could not be properly closed", e);
			}
		}
	}
	
	@Override
	public List<PartialImpactBean> queryImpactsForCounty(USCounty county,
			DateTime startDate, DateTime endDate) {
		
		/*return manager.createQuery("select distinct i from DroughtImpact i, in (i.spatialReferences) as s where " +
			"(s.county = :county or (s.state = :state and s.referenceType = :type)) and (i.startDate between :start and :end or i.endDate between :start and :end)")
			.setParameter("county", county)
			.setParameter("state", county.getState())
			.setParameter("type", SpatialReferenceType.US_STATE)
			.setParameter("start", startDate.toDate())
			.setParameter("end", endDate.toDate())
			.getResultList(); */
		
		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select impact_id, title, summary from DroughtImpacts inner join SpatialReferences on " +
					"SpatialReferences.impact_report = impact_id inner join Counties on SpatialReferences.county_id = Counties.county_id where Counties.county_id = ? and" +
					" ((start_date between ? and ?) or (end_date between ? and ?)) group by impact_id");
			
			stmt.setLong(1, county.getCountyId());
			stmt.setDate(2, new java.sql.Date(startDate.getMillis()));
			stmt.setDate(3, new java.sql.Date(endDate.getMillis()));			
			stmt.setDate(4, new java.sql.Date(startDate.getMillis()));
			stmt.setDate(5, new java.sql.Date(endDate.getMillis()));	
			
			return processSql(stmt.executeQuery());
			
		} catch ( Exception e ) { 
			LOG.error("An error occurred processing impacts", e);
			RuntimeException re = new RuntimeException("The impacts could not be queried");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception e ) { 
				LOG.warn("A connection could not be properly closed", e);
			}
		}
	}

	@Override
	public List<PartialImpactBean> queryImpactsForState(USState state,
			DateTime startDate, DateTime endDate) {

		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select impact_id, title, summary from DroughtImpacts inner join SpatialReferences on " +
					"SpatialReferences.impact_report = impact_id where SpatialReferences.state = ? and (" +
					" (start_date between ? and ?) or (end_date between ? and ?)) group by impact_id");
			
			stmt.setInt(1, state.ordinal());
			stmt.setDate(2, new java.sql.Date(startDate.getMillis()));
			stmt.setDate(3, new java.sql.Date(endDate.getMillis()));			
			stmt.setDate(4, new java.sql.Date(startDate.getMillis()));
			stmt.setDate(5, new java.sql.Date(endDate.getMillis()));	
			
			return processSql(stmt.executeQuery());
			
		} catch ( Exception e ) { 
			LOG.error("An error occurred processing impacts", e);
			RuntimeException re = new RuntimeException("The impacts could not be queried");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception e ) { 
				LOG.warn("A connection could not be properly closed", e);
			}
		}
	}

	@Override
	public List<PartialImpactBean> queryAllImpactsForCity(USCity city) {
		/*return manager.createQuery("select distinct i from DroughtImpact i, in (i.spatialReferences) as s where " +
				"(s.city = :city or (s.state = :state and s.referenceType = :statetype) or (s.county = :county and s.referenceType = :countytype))")
				.setParameter("city", city)
				.setParameter("state", city.getCounty().getState())
				.setParameter("county", city.getCounty())
				.setParameter("statetype", SpatialReferenceType.US_STATE)
				.setParameter("countytype", SpatialReferenceType.US_COUNTY)
				.getResultList();
				*/
		
		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select impact_id, title, summary from DroughtImpacts inner join SpatialReferences on " +
					"SpatialReferences.impact_report = impact_id inner join Cities on SpatialReferences.city_id = Cities.city_id where Cities.city_id = ? group by impact_id");
			
			stmt.setLong(1, city.getCityId());
	
			return processSql(stmt.executeQuery());
			
		} catch ( Exception e ) { 
			LOG.error("An error occurred processing impacts", e);
			RuntimeException re = new RuntimeException("The impacts could not be queried");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception e ) { 
				LOG.warn("A connection could not be properly closed", e);
			}
		}

	}

	@Override
	public List<PartialImpactBean> queryAllImpactsForCounty(USCounty county) {
		/*return manager.createQuery("select distinct i from DroughtImpact i, in (i.spatialReferences) as s where " +
				"(s.county = :county or (s.state = :state and s.referenceType = :type))").setParameter("county", county)
				.setParameter("state", county.getState())
				.setParameter("type", SpatialReferenceType.US_STATE)
				.getResultList();*/
		
		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select impact_id, title, summary from DroughtImpacts inner join SpatialReferences on " +
					"SpatialReferences.impact_report = impact_id inner join Counties on SpatialReferences.county_id = Counties.county_id where " +
					"Counties.county_id = ? group by impact_id");
			
			stmt.setLong(1, county.getCountyId());
	
			return processSql(stmt.executeQuery());
			
		} catch ( Exception e ) { 
			LOG.error("An error occurred processing impacts", e);
			RuntimeException re = new RuntimeException("The impacts could not be queried");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception e ) { 
				LOG.warn("A connection could not be properly closed", e);
			}
		}
	}

	@Override
	public List<PartialImpactBean> queryAllImpactsForState(USState state) {
		//return manager.createQuery("select distinct i from DroughtImpact i, in (i.spatialReferences) as s where (s.state = :state)").setParameter("state", state).getResultList();

		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select impact_id, title, summary from DroughtImpacts inner join SpatialReferences on " +
					"SpatialReferences.impact_report = impact_id where SpatialReferences.state = ? group by impact_id");
			
			stmt.setLong(1, state.ordinal());
	
			return processSql(stmt.executeQuery());
			
		} catch ( Exception e ) { 
			LOG.error("An error occurred processing impacts", e);
			RuntimeException re = new RuntimeException("The impacts could not be queried");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception e ) { 
				LOG.warn("A connection could not be properly closed", e);
			}
		}
	
	}

	@Override
	public List<PartialImpactBean> queryImpacts(BoundingBox box, DateTime start,
			DateTime end, SpatialReferenceType type) {

		try {
			switch ( type ) { 
			case US_COUNTY:
				return processCounties(box, start, end);
			case US_STATE:
				return processStates(box, start, end);
			default:
				throw new RuntimeException("only US_COUNTY and US_STATE are valid type arguments");
			}
		} catch ( Exception e ) { 
			LOG.error("could not query impacts", e);
			RuntimeException re = new RuntimeException();
			re.initCause(e);
			throw re;
		}
	}

	@Override
	public List<PartialImpactBean> searchAllReports(String queryString) {
		// TODO implement this when new search logic is available
		return null;
	}

	@Override
	public DroughtImpactStatistics<USCounty> lookupStateImpactStats(USState state, DateTime start,
			DateTime end) {
		
		Connection conn = null;
		ResultSet results = null;
		ResultSet category_results = null;
		try { 
			conn = source.getConnection();
			PreparedStatement count_statement = conn.prepareStatement("select count(impact_id), SpatialReferences.county_id from DroughtImpacts " +
					"inner join SpatialReferences on SpatialReferences.impact_report = DroughtImpacts.impact_id where SpatialReferences.state = ? " +
					"and ((DroughtImpacts.start_date between ? and ?) or (DroughtImpacts.end_date between ? and ?) or (DroughtImpacts.start_date " +
					"between ? and ? and DroughtImpacts.end_date is null)) group by SpatialReferences.county_id order by SpatialReferences.county_id");
			
			PreparedStatement category_statement = conn.prepareStatement("select count(*), county_id, impactcategory, dollar_loss from " +
					"DroughtImpacts inner join ReportCategories on ReportCategories.impact_report = DroughtImpacts.impact_id inner join " +
					"SpatialReferences on SpatialReferences.impact_report = DroughtImpacts.impact_id where ((DroughtImpacts.start_date between " +
					"? and ?) or (DroughtImpacts.end_date between ? and ?) or (DroughtImpacts.start_date " +
					"between ? and ? and DroughtImpacts.end_date is null)) and (SpatialReferences.state = ?) group by SpatialReferences.county_id, " +
					"ReportCategories.impactcategory order by SpatialReferences.county_id");
			
			count_statement.setInt(1, state.ordinal());
			count_statement.setDate(2, new java.sql.Date(start.getMillis()));
			count_statement.setDate(3, new java.sql.Date(end.getMillis()));
			count_statement.setDate(4, new java.sql.Date(start.getMillis()));
			count_statement.setDate(5, new java.sql.Date(end.getMillis()));
			count_statement.setDate(6, new java.sql.Date(start.getMillis()));
			count_statement.setDate(7, new java.sql.Date(end.getMillis()));

			category_statement.setDate(1, new java.sql.Date(start.getMillis()));
			category_statement.setDate(2, new java.sql.Date(end.getMillis()));
			category_statement.setDate(3, new java.sql.Date(start.getMillis()));
			category_statement.setDate(4, new java.sql.Date(end.getMillis()));
			category_statement.setDate(5, new java.sql.Date(start.getMillis()));
			category_statement.setDate(6, new java.sql.Date(end.getMillis()));
			category_statement.setInt(7, state.ordinal());
			
			results = count_statement.executeQuery();
			category_results = category_statement.executeQuery();
			
			if ( !category_results.next() ) { 
				if ( results.next() ) {
					LOG.warn("DIR results produced no category results, this is an abnormal condition.");
					LOG.debug("State: "+state+" for date range: "+start+" to "+end);
				}
				
				return new DroughtImpactStatistics<USCounty>(null);
			}
			
			HashMap<USCounty, DroughtImpactDivisionStatistics<USCounty>> division_map = new HashMap<USCounty, DroughtImpactDivisionStatistics<USCounty>>();
			HashMap<DroughtReportCategory, Integer> category_counts;
			HashMap<DroughtReportCategory, Float> category_losses;
			
			HashMap<DroughtReportCategory, Integer> state_category_counts = new HashMap<DroughtReportCategory, Integer>();
			HashMap<DroughtReportCategory, Float> state_category_losses = new HashMap<DroughtReportCategory, Float>();
			int state_wide_impacts = 0;
			
			while ( results.next() ) { 
				if ( results.getObject(2) == null ) { // the county id is null so this is a state-wide impact
					state_wide_impacts = results.getInt(1);
				} 
				
				USCounty county = spatialQuery.getCountyById(results.getLong(2));
				Long id = county.getCountyId();
				
				category_counts = new HashMap<DroughtReportCategory, Integer>();
				category_losses = new HashMap<DroughtReportCategory, Float>();
				
				do { 
				 	if ( category_results.getObject(2) == null ) { // this is a category for state wide impacts 
				 		state_category_counts.put(DroughtReportCategory.values()[category_results.getInt(3)], category_results.getInt(1));
				 		state_category_losses.put(DroughtReportCategory.values()[category_results.getInt(3)], category_results.getFloat(4));
					} else if ( category_results.getLong(2) != id || (id == null && category_results.getObject(2) != null) ) { 
						// the result set has moved passed the current county
						break;
					} else { 
						category_counts.put(DroughtReportCategory.values()[category_results.getInt(3)], category_results.getInt(1));
						category_losses.put(DroughtReportCategory.values()[category_results.getInt(3)], category_results.getFloat(4));	
					}
				} while ( category_results.next() );

				// add in the state wide category info
				for ( DroughtReportCategory cat : state_category_counts.keySet() ) {
					if ( category_counts.get(cat) != null ) { 
						category_counts.put(cat, category_counts.get(cat) + state_category_counts.get(cat));
						category_losses.put(cat, category_losses.get(cat) + state_category_losses.get(cat));
					} else {
						category_counts.put(cat, state_category_counts.get(cat));
						category_losses.put(cat, state_category_losses.get(cat));
					}
				}
				
				int county_count = results.getInt(1);
				division_map.put(county, new DroughtImpactDivisionStatistics<USCounty>(county, (county_count+state_wide_impacts), 0, 
						category_counts, category_losses));
			}
			
			/* add all the other state counties for the state-wide impacts */
			Set<USCounty> state_counties = spatialQuery.getCountiesByState(state);
			for ( USCounty county : state_counties ) { 
				if ( division_map.containsKey(county) ) {
					continue; // the county is already accounted for with its county specific impacts
				}
				
				// add in the state wide category info for this county
				division_map.put(county, new DroughtImpactDivisionStatistics<USCounty>(county, state_wide_impacts, 0, 
						state_category_counts, state_category_losses));
			}
			
			return new DroughtImpactStatistics<USCounty>(division_map);
			
		} catch ( Exception e ) { 
			LOG.error("Error querying county impact statistics", e);
			RuntimeException re = new RuntimeException("could not query the county impact data");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				category_results = null;
				results.close();
				conn.close();
			} catch ( Exception ex ) { 
				LOG.warn("A connection could not be closed", ex);
			}
		}
		
	}

	@Override
	public DroughtImpactStatistics<USState> lookupUSImpactStats(DateTime start, DateTime end) {
		
		Connection conn = null;
		ResultSet results = null;
		ResultSet category_results = null;
		try { 
			conn = source.getConnection();
			PreparedStatement count_statement = conn.prepareStatement("select count(*) as impact_counts, SpatialReferences.state from " +
					"DroughtImpacts inner join SpatialReferences on SpatialReferences.impact_report = DroughtImpacts.impact_id " +
					"where (DroughtImpacts.start_date between ? and ?) or (DroughtImpacts.end_date between ? and ?) or (DroughtImpacts.start_date " +
					"between ? and ? and DroughtImpacts.end_date is null) group by SpatialReferences.state order by SpatialReferences.state");
			
			PreparedStatement category_statement = conn.prepareStatement("select count(*), state, impactcategory, dollar_loss from " +
					"DroughtImpacts inner join ReportCategories on ReportCategories.impact_report = DroughtImpacts.impact_id inner join " +
					"SpatialReferences on SpatialReferences.impact_report = DroughtImpacts.impact_id where (DroughtImpacts.start_date between " +
					"? and ?) or (DroughtImpacts.end_date between ? and ?) or (DroughtImpacts.start_date " +
					"between ? and ? and DroughtImpacts.end_date is null)group by SpatialReferences.state, " +
					"ReportCategories.impactcategory order by SpatialReferences.state");
			
			count_statement.setDate(1, new java.sql.Date(start.getMillis()));
			count_statement.setDate(2, new java.sql.Date(end.getMillis()));
			count_statement.setDate(3, new java.sql.Date(start.getMillis()));
			count_statement.setDate(4, new java.sql.Date(end.getMillis()));
			count_statement.setDate(5, new java.sql.Date(start.getMillis()));
			count_statement.setDate(6, new java.sql.Date(end.getMillis()));

			category_statement.setDate(1, new java.sql.Date(start.getMillis()));
			category_statement.setDate(2, new java.sql.Date(end.getMillis()));
			category_statement.setDate(3, new java.sql.Date(start.getMillis()));
			category_statement.setDate(4, new java.sql.Date(end.getMillis()));
			category_statement.setDate(5, new java.sql.Date(start.getMillis()));
			category_statement.setDate(6, new java.sql.Date(end.getMillis()));
			
			results = count_statement.executeQuery();
			category_results = category_statement.executeQuery();
			
			if ( !category_results.next() ) { 
				// if this happens something went very wrong - this is an abnormal data condition
				throw new RuntimeException("The category count result contained no data");
			}
			
			HashMap<USState, DroughtImpactDivisionStatistics<USState>> division_map = new HashMap<USState, DroughtImpactDivisionStatistics<USState>>();
			HashMap<DroughtReportCategory, Integer> category_counts;
			HashMap<DroughtReportCategory, Float> category_losses;
			
			while ( results.next() ) { 
				USState state = USState.values()[results.getInt(2)];
				
				category_counts = new HashMap<DroughtReportCategory, Integer>();
				category_losses = new HashMap<DroughtReportCategory, Float>();
				
				do { 
					if ( category_results.getInt(2) != state.ordinal() ) { 
						// the result set has moved passed the current state
						break;
					} else { 
						category_counts.put(DroughtReportCategory.values()[category_results.getInt(3)], category_results.getInt(1));
						category_losses.put(DroughtReportCategory.values()[category_results.getInt(3)], category_results.getFloat(4));
						
					}
				} while ( category_results.next() );

				division_map.put(state, new DroughtImpactDivisionStatistics<USState>(state, results.getInt(1), 0, 
						category_counts, category_losses));
			}
			
			return new DroughtImpactStatistics<USState>(division_map);
			
		} catch ( Exception e ) { 
			LOG.error("Error querying state impact statistics", e);
			RuntimeException re = new RuntimeException("could not query the state impact data");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				category_results = null;
				results.close();
				conn.close();
			} catch ( Exception ex ) { 
				LOG.warn("A connection could not be closed", ex);
			}
		}
	}
	
	@Override
	public DroughtImpactStatistics<USCounty> lookupBoundedImpacts(
			BoundingBox region, DateTime start, DateTime end) {
		
		
		Set<USState> states;
		if ( region == null ) {
			throw new RuntimeException("A valid region must be specified");
		} 
		
		try {
			states = spatialQuery.getStatesByRegion(region);
		} catch ( Exception e ) { 
			LOG.error("could not get the state list", e);
			RuntimeException re = new RuntimeException("An error occurreed querying the state list");
			re.initCause(e);
			throw re;
		}
		
		DroughtImpactStatistics<USCounty> stats = new DroughtImpactStatistics<USCounty>(null);
		for ( USState state : states ) { 
			stats.merge(lookupStateImpactStats(state, start, end));
		}
		
		return stats;
	}

	@Override
	public ImpactBean getImpactById(long id) {
		try {
			return manager.find(ImpactBean.class, id);
		} catch ( Exception e ) { 
			RuntimeException re = new RuntimeException("could not load the impact bean");
			re.initCause(e);
			throw re;
		}
	}

	@Override
	public List<ImpactBean> loadAllImpacts(List<PartialImpactBean> partials) {

		try { 
			ArrayList<ImpactBean> beans = new ArrayList<ImpactBean>();
			for ( PartialImpactBean partial : partials ) {
				beans.add(manager.find(ImpactBean.class, partial.getId()));
			}
			
			return beans;
		} catch ( Exception e ) { 
			LOG.error("could not load complete beans", e);
			RuntimeException re = new RuntimeException("An error occurred loading impact bean objects");
			re.initCause(e);
			throw re;
		}
	}

	private List<PartialImpactBean> processCounties(BoundingBox box, DateTime start, DateTime end) throws RemoteException {
		ArrayList<PartialImpactBean> result = new ArrayList<PartialImpactBean>();
		
		try {			
			Set<USCounty> counties = spatialQuery.getCountiesByRegion(box);
			for ( USCounty county : counties ) {
				for ( PartialImpactBean impact : queryImpactsForCounty(county, start, end) ) { 
					if ( !result.contains(impact) ) { 
						result.add(impact);
					}
				}
			}
		} catch ( Exception e ) { 
			LOG.error("could not create the query result", e);
			RemoteException re = new RemoteException();
			re.initCause(e);
			throw re;
		}
		
		return result;
	}
	
	private List<PartialImpactBean> processStates(BoundingBox box, DateTime start, DateTime end) throws RemoteException {
		ArrayList<PartialImpactBean> result = new ArrayList<PartialImpactBean>();
		
		Set<USState> states = spatialQuery.getStatesByRegion(box);
		for ( USState state : states ) { 
			result.addAll(queryImpactsForState(state, start, end));
		}
		
		return result;
	}
	
	private List<PartialImpactBean> processSql(ResultSet results) throws Exception { 
		
		ArrayList<PartialImpactBean> impacts = new ArrayList<PartialImpactBean>();

		while ( results.next() ) { 
			PartialImpactBean bean = new PartialImpactBean();
			bean.setId(results.getLong(1));
			bean.setTitle(results.getString(2));
			bean.setSummary(results.getString(3));
			impacts.add(bean);
		}

		results.close();
		
		return impacts;
		
	}
}
