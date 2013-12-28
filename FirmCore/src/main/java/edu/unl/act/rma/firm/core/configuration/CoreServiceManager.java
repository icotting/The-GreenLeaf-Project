/* Created On: Oct 24, 2005 */
package edu.unl.act.rma.firm.core.configuration;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import edu.unl.act.rma.firm.core.BeanInjector;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.component.SearchEngine;

/**
 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean
 * @author Ian Cottingham
 *
 */
public class CoreServiceManager extends ConfigurationServer implements CoreServiceMBean {

	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, CoreServiceManager.class);
	
	private static CoreServiceManager instance;
	
	protected CoreServiceManager() {
		super("edu.unl.firm:type=CoreService", CoreServiceImpl.class);
	}

	/**
	 * Returns the singleton system service manager.
	 * 
	 * @return The singleton manager
	 */
	public static CoreServiceManager getInstance() throws InstantiationException {
		try { 
			return (instance == null) ? (instance = new CoreServiceManager()) : instance;
		} catch ( RuntimeException re ) { 
			LOG.error("could not instantiate the manager", re);
			throw new InstantiationException("could not instantiate the manager");
		}
	}
	
	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getClimaticDSJNDIName()
	 */
	public String getClimaticDSJNDIName() throws ConfigurationException {
		return (String) get("ClimaticDSJNDIName");
	}
	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getSoilsDSJNDIName()
	 */
	public String getSoilsDSJNDIName() throws ConfigurationException {
		return (String)get("SoilsDSJNDIName");
	}

	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getOceanicDSJNDIName()
	 */
	public String getOceanicDSJNDIName() throws ConfigurationException {
		return (String)get("OceanicDSJNDIName");
	}
	
	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getSystemDSJNDIName()
	 */
	public String getSystemDSJNDIName() throws ConfigurationException {
		return (String)get("SystemDSJNDIName");
	}

	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getDefaultThreshold()
	 */
	public Float getDefaultThreshold() throws ConfigurationException {
		return (Float)get("DefaultThreshold");
	}

	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#setClimaticDSJNDIName(java.lang.String)
	 */
	public void setClimaticDSJNDIName(String str) throws ConfigurationException {
		set("ClimaticDSJNDIName", str);	
	}

	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#setSoilsDSJNDIName(java.lang.String)
	 */
	public void setSoilsDSJNDIName(String str) throws ConfigurationException {
		set("SoilsDSJNDIName", str);
	}
	
	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#setOceanicDSJNDIName(java.lang.String)
	 */
	public void setOceanicDSJNDIName(String str) throws ConfigurationException {
		set("OceanicDSJNDIName", str);
	}

	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#setSystemDSJNDIName(java.lang.String)
	 */
	public void setSystemDSJNDIName(String str) throws ConfigurationException {
		set("SystemDSJNDIName", str);
	}

	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#setDefaultThreshold(java.lang.Float)
	 */
	public void setDefaultThreshold(Float value) throws ConfigurationException {
		set("DefaultThreshold", value);
	}

	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getFromAddr()
	 */
	public String getFromAddr() throws ConfigurationException {
		return (String)get("FromAddr");
	}
	
	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getFromAddr()
	 */
	public String getFrameworkVersion() throws ConfigurationException {
		return (String)get("FrameworkVersion");
	}

	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getSmtpHost()
	 */
	public String getSmtpHost() throws ConfigurationException {
		return (String)get("SmtpHost");
	}


	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getSmtpUser()
	 */
	public String getSmtpUser() throws ConfigurationException {
		return (String)get("SmtpUser");
	}
	

	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getSmtpPassword()
	 */
	public String getSmtpPassword() throws ConfigurationException {
		return (String)get("SmtpPassword");
	}
	
	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#sendEmail(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void sendEmailSingle(String to, String subject, String text) throws ConfigurationException {
		Object[] args = { to, subject, text };
		this.invoke("sendEmailSingle", args);
	}

	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#sendEmail(java.lang.String[], java.lang.String, java.lang.String)
	 */
	public void sendEmailMulti(String[] to, String subject, String text) throws ConfigurationException { 
		Object[] args = { to, subject, text };
		this.invoke("sendEmailMulti", args);
	}


	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#setFromAddr(java.lang.String)
	 */
	public void setFromAddr(String fromAddr) throws ConfigurationException {
		set("FromAddr", fromAddr);
	}

	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#setSmtpHost(java.lang.String)
	 */
	public void setSmtpHost(String smtpHost) throws ConfigurationException {		
		set("SmtpHost", smtpHost);
	}
	

	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#setSmtpUser(java.lang.String)
	 */
	public void setSmtpUser(String smtpUser) throws ConfigurationException {		
		set("SmtpUser", smtpUser);
	}
	
	/**
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#setSmtpPassword(java.lang.String)
	 */
	public void setSmtpPassword(String smtpPassword) throws ConfigurationException {		
		set("smtpPassword", smtpPassword);
	}
	
	public int getNormalPeriodEndYear() throws ConfigurationException {
		return (Integer)get("NormalPeriodEndYear");
	}

	public int getNormalPeriodStartYear() throws ConfigurationException {
		return (Integer)get("NormalPeriodStartYear");
	}

	public void setNormalPeriodEndYear(int normalPeriodStartYear)
			throws ConfigurationException {
		set("NormalPeriodStartYear", normalPeriodStartYear);
	}

	public void setNormalPeriodStartYear(int normalPeriodEndYear)
			throws ConfigurationException {
		set("NormalPeriodEndYear", normalPeriodEndYear);
	}
	
	public SearchEngine getSearchEngine() throws RemoteException {
		try {
			return BeanInjector.getInstance().getSessionBean(SearchEngine.class);
		} catch ( NamingException ne ) { 
			LOG.error("could not get the SearchEngine", ne);
			throw new RemoteException(ne.getMessage());
		}
	}

	@Override
	public String getClimaticBuildDSJNDIName() throws ConfigurationException {
		return (String)get("ClimaticBuildDSJNDIName");
	}

	@Override
	public void setClimaticBuildDSJNDIName(String str) throws ConfigurationException {
		set("ClimaticBuildDSJNDIName", str);
	}

	@Override
	public String getStreamFlowDSJNDIName() throws ConfigurationException {
		return (String)get("StreamFlowDSJNDIName");
	}

	@Override
	public void setStreamFlowDSJNDIName(String str)	throws ConfigurationException {
		set("StreamFlowDSJNDIName", str);
		
	}
}
