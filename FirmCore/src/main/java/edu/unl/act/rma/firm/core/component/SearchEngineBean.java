/* Created On Feb 5, 2007 */
package edu.unl.act.rma.firm.core.component;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.sql.DataSource;

import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.configuration.CoreServiceManager;
import edu.unl.act.rma.firm.core.search.SearchQuery;
import edu.unl.act.rma.firm.core.search.SearchResultBuilder;
import edu.unl.act.rma.firm.core.search.SearchResultList;
import edu.unl.act.rma.firm.drought.ImpactBean;

/**
 * @author Ian Cottingham
 *
 */
@Stateful
@Local({SearchEngine.class})
@Remote({SearchEngine.class})
public class SearchEngineBean implements SearchEngine {

	private static int DESC_LEN = -1;
	private static int TITLE_LEN = -1; 
	private static int RES_LEN = -1;
	
	private static final long serialVersionUID = 1L;
	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG, SearchEngineBean.class);

	private transient DataSource source = DataSourceInjector.injectDataSource(DataSourceTypes.SYSTEM);
	
	@PersistenceContext(unitName="FirmCorePU")
	private transient EntityManager manager;
	
		
	public SearchResultList fuzzySearch(SearchQuery query) throws RemoteException {		
		String query_string = query.getQueryString();
		
		DESC_LEN = 100;
		TITLE_LEN = 50;
		RES_LEN = 10;
				
		SearchResultBuilder builder = new SearchResultBuilder(DESC_LEN, TITLE_LEN, RES_LEN);
		Connection conn = null; 
		try { 
			conn = source.getConnection();
		} catch ( Exception e ) { 
			LOG.error("could not get a db connection");
			RemoteException re = new RemoteException();
			re.initCause(e);
			throw re;
		}
		
		try {
			for ( String search_on : query ) { 
				StringBuffer sql_query = new StringBuffer("select ");
				int field_count = 0;
				
				builder.openTypeSet(search_on, query.getEnumeratedType(search_on));
				
				if ( query.isDistinctResult(search_on) ) { 
					sql_query.append("distinct(");
					sql_query.append(query.getResultFields(search_on).iterator().next());
					sql_query.append(")");
				} else { 
					for ( String field : query.getResultFields(search_on) ) { 
						if ( field_count++ > 0 ) { 
							sql_query.append(",");
						}
						sql_query.append(field);
					}	
				}
				
				sql_query.append(" from ");
				sql_query.append(search_on);
				sql_query.append(" where ");
				
				StringBuffer match_sub = new StringBuffer(" match(");
				
				field_count = 0;
				for ( String field : query.getSearchFields(search_on) ) {				
					if ( field_count++ > 0 ) { 
						match_sub.append(",");
					}
					match_sub.append(field);
				}
				
				match_sub.append(") ");
				
				String[] tokens = query.getQueryString().split("\\s");
				
				for ( int i=0; i<tokens.length; i++ ) {
					if ( i > 0 ) { 
						sql_query.append(" and ");
					}
					sql_query.append(match_sub.toString());
					sql_query.append("against (?)");
				}
				
				PreparedStatement stmt = conn.prepareStatement(sql_query.toString());
				
				for ( int i=0; i<tokens.length; i++ ) { 
					stmt.setString(i+1, tokens[i]);
				}
				
				ResultSet search_result = stmt.executeQuery();
				
				while ( search_result.next() ) { 
					/* this is hard coded as 1.1.20090220-R needs to only support the sinle use case of impact reports. 
					 * This functionality should be made much more generalized for 2.0
					 * 
					 * TODO: fix this - IT SHOULD NOT GO OUT AS PART OF THE 2.0 RELEASE!!
					 * 
					 * Daily WTF, maybe?
					 */
					builder.processObject(manager.find(ImpactBean.class, search_result.getLong(1)));
				}
				
				builder.closeTypeSet();
			}
		} catch ( Exception e ) { 
			LOG.error("error performing the search", e);
			RemoteException re = new RemoteException();
			re.initCause(e);
			throw re;
		} finally { 
			if ( conn != null ) { 
				try { 
					conn.close();
				} catch ( Exception e ) { 
					LOG.warn("could not close a connection", e);
				}
			}
		}
		return builder.getResults();
		
	}
}
