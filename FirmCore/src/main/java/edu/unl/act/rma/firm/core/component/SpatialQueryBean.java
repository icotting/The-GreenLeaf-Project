/* Created On Nov 30, 2006 */
package edu.unl.act.rma.firm.core.component;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.sql.DataSource;

import edu.unl.act.rma.firm.core.DataSourceInjector;
import edu.unl.act.rma.firm.core.DataSourceTypes;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.spatial.BoundingBox;
import edu.unl.act.rma.firm.core.spatial.Region;
import edu.unl.act.rma.firm.core.spatial.SpatialReference;
import edu.unl.act.rma.firm.core.spatial.USCity;
import edu.unl.act.rma.firm.core.spatial.USCounty;
import edu.unl.act.rma.firm.core.spatial.USState;
import edu.unl.act.rma.firm.core.spatial.USZipCode;

/**
 * @author Ian Cottingham
 *
 */
@Stateless
@Remote({SpatialQuery.class})
public class SpatialQueryBean implements SpatialQuery, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG, SpatialQueryBean.class);
	
	/** The peristence manager, used to manage the bound entity beans. */
	@PersistenceContext(unitName="FirmCorePU", type=PersistenceContextType.TRANSACTION)
	private transient EntityManager manager;

	private DataSource source = DataSourceInjector.injectDataSource(DataSourceTypes.SYSTEM);
	
	public Set<USCounty> getCountiesByFips(List<String> fips) throws RemoteException {
		TreeSet<USCounty> counties = new TreeSet<USCounty>();
		for ( String str : fips ) { 
			List matched = manager.createQuery("select county from USCounty as county where county.fips = ?1").setParameter(1, str).getResultList();
			if ( matched.size() > 0 ) { 
				for ( Object obj : matched ) { 
					if ( !(counties.contains(obj)) ) { 
						counties.add((USCounty)obj);
					}
				}
			} else { 
				LOG.warn("no matched county for "+str);
			}
		}

		return counties;
	}

	public Set<USCounty> getCountiesByState(USState state) throws RemoteException {
		TreeSet<USCounty> counties = new TreeSet<USCounty>();
		List matched = manager.createQuery("select county from USCounty as county where county.state = ?1").setParameter(1, state).getResultList();
		for ( Object obj : matched ) { 
			counties.add((USCounty)obj);
		}
		
		return counties;
	}

	public Set<USCounty> searchCounties(String countyName) throws RemoteException {
		TreeSet<USCounty> counties = new TreeSet<USCounty>();
		List matched = manager.createQuery("select county from USCounty as county where county.name like ?1").setParameter(1, countyName).getResultList();
		for ( Object obj : matched ) { 
			counties.add((USCounty)obj);
		}
		
		return counties;
	}

	public Set<USCounty> searchCountiesByState(String countyName, USState state) throws RemoteException {
		TreeSet<USCounty> counties = new TreeSet<USCounty>();
		List matched = manager.createQuery("select county from USCounty as county where county.name like ?1 and county.state = ?2").setParameter(1, countyName)
			.setParameter(2, state).getResultList();
		
		for ( Object obj : matched ) { 
			counties.add((USCounty)obj);
		}
		
		return counties;
	}

	public Set<USCounty> getCountiesByRegion(BoundingBox region) throws RemoteException {
		Connection conn = null; 
		try { 
			conn = source.getConnection();
			
			PreparedStatement stmt = conn.prepareStatement("select county from Polygons inner join Regions on region_id = region where Intersects(PolygonFromText(?), geo) and county is not null");
			stmt.setString(1, region.toSqlString());

			TreeSet<USCounty> counties = new TreeSet<USCounty>();
			ResultSet county_results = stmt.executeQuery();
			while ( county_results.next() ) { 
				counties.add(manager.find(USCounty.class, county_results.getLong(1)));
			}
			
			return counties;
			
		} catch ( Exception e ) { 
			LOG.error("An error occurred querying the database", e);
			RemoteException re = new RemoteException("The spatial query ended with an error");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception e ) { 
				LOG.warn("A connection could not be closed", e);
			}
		}
	}

	public Set<USState> getStatesByRegion(BoundingBox region) throws RemoteException {
		Connection conn = null; 
		try { 
			conn = source.getConnection();
			
			PreparedStatement stmt = conn.prepareStatement("select state from Polygons inner join Regions on region_id = region where Intersects(PolygonFromText(?), geo) and county is null");
			stmt.setString(1, region.toSqlString());

			USState[] state_values = USState.values();
			TreeSet<USState> states = new TreeSet<USState>();
			ResultSet state_results = stmt.executeQuery();
			while ( state_results.next() ) { 
				states.add(state_values[state_results.getInt(1)]);
			}
			
			return states;
			
		} catch ( Exception e ) { 
			LOG.error("An error occurred querying the database", e);
			RemoteException re = new RemoteException("The spatial query ended with an error");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception e ) { 
				LOG.warn("A connection could not be closed", e);
			}
		}
	}

	public Set<USCity> getCitiesByState(USState state) throws RemoteException {
		TreeSet<USCity> cities = new TreeSet<USCity>();
		
		for ( USCounty county : this.getCountiesByState(state) ) {
			cities.addAll(county.getCities());
		}
		
		return cities;
	}
	
	public Set<USCity> searchCitiesByCounty(String cityName, USCounty county) throws RemoteException {
		TreeSet<USCity> cities = new TreeSet<USCity>();
		List matched = manager.createQuery("select city from USCity as city where city.name like ?1 and city.county = ?2").setParameter(1, cityName)
			.setParameter(2, county).getResultList();
		
		for ( Object obj : matched ) { 
			cities.add((USCity)obj);
		}
		
		return cities;
	}

	/**
	 * This function is used primarily to prevent large memory foot prints for searched objects with SpatialReferences, 
	 * while it provides some overhead when the counties are needed from the reference, it significantly decreases a 
	 * memory foot print issue.  
	 * 
	 * NOTE: This method is only needed for SpatialReferences fo type US_COUNTY
	 */
	public List<SpatialReference> loadCompleteReferences(List<SpatialReference> references) throws RemoteException {
		if ( references == null ) { 
			return null;
		}
		
		ArrayList<SpatialReference> new_refs = new ArrayList<SpatialReference>();
		for ( SpatialReference reference : references ) {
			/* this happens when a call to load complete references is made for a reference which has not been persisted to the db yet */
			if ( reference.getReferenceId() == 0 ) { 
				new_refs.add(reference);
				continue;
			}
			// get a reference to the object which has not been serialized in order to load the LAZY relationship
			SpatialReference loaded_reference = manager.find(SpatialReference.class, reference.getReferenceId());
			new_refs.add(loaded_reference);
		}
		
		return new_refs;
	}

	public void removeSpatialReference(SpatialReference reference) throws RemoteException {
		/* since it was likely not loaded out of this persistence context, ensure that it is reloaded before it is removed */
		manager.remove(manager.find(SpatialReference.class, reference.getReferenceId()));
	}

	public USCity getCityById(long id) throws RemoteException {
		return manager.find(USCity.class, id);
	}

	public USCounty getCountyById(long id) throws RemoteException {
		return manager.find(USCounty.class, id);
	}
	
	

	@Override
	public USCounty getCountyByFips(String fips) throws RemoteException {
		List matched = manager.createQuery("select county from USCounty as county where county.fips = ?1").setParameter(1, fips).getResultList();
		if ( matched.size() > 0 ) { 
			LOG.warn("more than one matched county for fips: "+fips);
			return (USCounty)matched.get(0);
		} else { 
			return null;
		}
	}

	@Override
	public USCity getCityByZip(String zipCode) throws RemoteException {
		List<USZipCode> zip_codes = manager.createQuery("select z from ZipCode z where z.zipCode = ?1").setParameter(1, zipCode).getResultList();
		
		if ( zip_codes == null || zip_codes.size() < 1 ) { 
			return null;
		} else { 
			return zip_codes.get(0).getCity();
		}
	}

	@Override
	public USCity getCityForName(String name, USState state)
			throws RemoteException {
		List<USCity> cities = getCitiesForName(name);
		
		for ( USCity city : cities ) { 
			if ( city.getCounty().getState().equals(state) ) { 
				return city;
			}
		}
		throw new RemoteException("Object not found: no city matched "+name+" "+state.name());
	}
	
	@Override
	public List<USCity> getCitiesForName(String name) throws RemoteException {
		return manager.createQuery("select c from USCity c where c.name = ?1").setParameter(1, name).getResultList();
	}

	@Override
	public List<USCounty> getCountiesForName(String name)
			throws RemoteException {
		return manager.createQuery("select c from USCounty c where c.name = ?1").setParameter(1, name).getResultList();
	}

	@Override
	public USCounty getCountyForName(String name, USState state)
			throws RemoteException {
		
		List<USCounty> counties = manager.createQuery("select c from USCounty c where c.name = ?1 and c.state = ?2")
			.setParameter(1, name).setParameter(2, state).getResultList();
		
		if ( counties.size() == 0 ) { 
			throw new RemoteException("Object not found: no county matched "+name+" "+state.name());
		} else if ( counties.size() > 1 ) { 
			LOG.warn("More than one county was returned for "+name+" "+state.name()+" the first will be returned");
			return counties.get(0);
		} else { 
			return counties.get(0);
		}
	}

	@Override
	public Region getRegionForCounty(USCounty county) throws RemoteException {
		List<Region> match = manager.createQuery("select r from Region r where r.county = ?1").setParameter(1, county).getResultList();
		if ( match.size() == 0 ) {
			throw new RemoteException("No region was found for county "+county);
		} else if ( match.size() > 1 ) { 
			LOG.warn("More than one region was found for county "+county+" the first will be returned");
		} 
		
		return match.get(0);
	}

	@Override
	public Region getRegionForState(USState state) throws RemoteException {
		List<Region> match = manager.createQuery("select r from Region r where r.state = ?1 and r.county is null").setParameter(1, state).getResultList();
		if ( match.size() == 0 ) {
			throw new RemoteException("No region was found for state "+state);
		} else if ( match.size() > 1 ) { 
			LOG.warn("More than one region was found for state "+state+" the first will be returned");
		} 
		
		return match.get(0);
	}

	@Override
	public List<Region> getRegionsForCounties(List<USCounty> counties)
			throws RemoteException {
		
		ArrayList<Region> regions = new ArrayList<Region>();
		
		for ( USCounty county : counties ) { 
			regions.add(getRegionForCounty(county));
		}
		
		return regions;
	}

	@Override
	public List<Region> getRegionsForStates(List<USState> states)
			throws RemoteException {
		
		ArrayList<Region> regions = new ArrayList<Region>();
		
		for ( USState state : states ) { 
			regions.add(getRegionForState(state));
		}
		
		return regions;
	}
}
