/* Created Feb. 25, 2010 */
package edu.unl.act.rma.web.beans;

import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.inject.Named;
import javax.sql.DataSource;

import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;

/**
 * @author Ian Cottingham
 *
 */
@ManagedBean
@ApplicationScoped
@Named
public class DataStatsBean {

	private DataSource climateSource = DataSourceInjector.injectDataSource(DataSourceTypes.CLIMATIC);
	private static final Logger LOG = LogManager.getLogger(Loggers.APPLICATION_LOG, DataStatsBean.class);
	
	private SimpleDateFormat FORMAT = new SimpleDateFormat();
	
	public DataStatsBean() { 
		FORMAT.applyPattern("MMM dd, yyyy");
	}
	
	public String getWeeklyDataDate() { 
		Connection conn = null; 
		
		try { 
			conn = climateSource.getConnection();
			ResultSet rs = conn.prepareStatement("select date from buildmeta where name = 'WEEKLY'").executeQuery();
			if ( !rs.next() ) { 
				throw new RuntimeException("The data source does not contain a climate data set");
			} else { 
				return FORMAT.format(rs.getDate(1));
			}
		} catch ( Exception e ) { 
			LOG.error("An error occured while querying the climate meta data", e);
			RuntimeException re = new RuntimeException("could not get the weekly current date");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception ex ) { 
				LOG.error("An error occured while closing the connection", ex);
			}
		}
	}
}
