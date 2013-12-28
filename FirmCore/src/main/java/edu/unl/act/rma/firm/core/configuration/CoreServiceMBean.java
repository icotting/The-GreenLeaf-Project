/* Created On: Oct 24, 2005 */
package edu.unl.act.rma.firm.core.configuration;


/**
 * Service interface for internal system configuration, maintenance, and management.  Provides access to current {@link javax.sql.DataSource} JNDI names,
 * notification e-mail configuration, event scheduling, and runtime encryption key management.
 * 
 * @author Ian Cottingham
 */
public interface CoreServiceMBean {
	
	String getSystemDSJNDIName() throws ConfigurationException;
	void setSystemDSJNDIName(String str) throws ConfigurationException;
	
	public String getClimaticBuildDSJNDIName() throws ConfigurationException;
	public void setClimaticBuildDSJNDIName(String str) throws ConfigurationException;
	
	String getClimaticDSJNDIName() throws ConfigurationException;
	void setClimaticDSJNDIName(String str) throws ConfigurationException;
	
	String getSoilsDSJNDIName() throws ConfigurationException;
	void setSoilsDSJNDIName(String str) throws ConfigurationException;
	
	String getOceanicDSJNDIName() throws ConfigurationException;
	void setOceanicDSJNDIName(String str) throws ConfigurationException;
	
	String getStreamFlowDSJNDIName() throws ConfigurationException;
	void setStreamFlowDSJNDIName(String str) throws ConfigurationException;
	
	String getFrameworkVersion() throws ConfigurationException;

	/**
	 * Gets the from address for e-mail notification messages.
	 * 
	 * @return Noficiation from addresses
	 * @throws ConfigurationException
	 */
	String getFromAddr() throws ConfigurationException;
	
	/**
	 * Sets the from address for e-mail notification messages.
	 * 
	 * @param fromAddr The new from address
	 * @throws ConfigurationException
	 */
	void setFromAddr(String fromAddr) throws ConfigurationException;
	
	/**
	 * Gets the e-mail SMTP host for outgoing e-mail notification messages.
	 * 
	 * @return The SMTP host
	 * @throws ConfigurationException
	 */
	String getSmtpHost() throws ConfigurationException;
	
	/**
	 * Sets the e-mail SMTP host for outgoing e-mail notification messages.
	 * 
	 * @param smtpHost The new SMTP host
	 * @throws ConfigurationException
	 */
	void setSmtpHost(String smtpHost) throws ConfigurationException;
	
	/**
	 * @param smtpUser The smtp username to be used to authenticate
	 * @throws ConfigurationException
	 */
	void setSmtpUser(String smtpUser) throws ConfigurationException;
	
	/**
	 * @param smtpUser The smtp username to be used to authenticate
	 * @throws ConfigurationException
	 */
	void setSmtpPassword(String smtpPassword) throws ConfigurationException;
	
	/**
	 * Gets the default discard threshold value.  This is the percent of allowable missing data for the system components.
	 * 
	 * @return The default discard threshold
	 * @throws ConfigurationException
	 */
	Float getDefaultThreshold() throws ConfigurationException;
	
	/**
	 * Sets the default discard threshold value.  This is the percent of allowable missing data for the system components.
	 * 
	 * @param value The new discard threshold
	 * @throws ConfigurationException
	 */
	void setDefaultThreshold(Float value) throws ConfigurationException;
	
	/**
	 * Sends an e-mail message.
	 * 
	 * @param to The to address
	 * @param subject The message subject
	 * @param text The message body
	 * @throws ConfigurationException
	 */
	void sendEmailSingle(String to, String subject, String text) throws ConfigurationException;
	
	/**
	 * Sends an e-mail message to multiple recipients.
	 * 
	 * @param to The message recipients
	 * @param subject The message subject
	 * @param text The message body
	 * @throws ConfigurationException
	 */
	void sendEmailMulti(String[] to, String subject, String text) throws ConfigurationException;
	
	public int getNormalPeriodStartYear() throws ConfigurationException;
	public void setNormalPeriodStartYear(int normalPeriodStartYear) throws ConfigurationException;
	
	public int getNormalPeriodEndYear() throws ConfigurationException;
	public void setNormalPeriodEndYear(int normalPeriodEndYear) throws ConfigurationException;
}
