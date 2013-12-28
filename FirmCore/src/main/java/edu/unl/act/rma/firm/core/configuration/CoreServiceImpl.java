/* Created On: Oct 24, 2005 */
package edu.unl.act.rma.firm.core.configuration;

import java.io.Serializable;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.configuration.jmx.Service;
import edu.unl.act.rma.firm.core.configuration.jmx.ServicePoint;
import edu.unl.act.rma.firm.core.configuration.jmx.ServicePointParameter;


/**
 * @author Ian Cottingham
 */
@Service(objectName="edu.unl.firm:type=CoreService", providerInterface=CoreServiceMBean.class)
public class CoreServiceImpl implements CoreServiceMBean, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String climaticName;
	private String soilsName;
	private String oceanicName;
	private String systemName;
	private String smtpHost;
	private String smtpUser;
	private String smtpPassword;
	private String fromAddr;
	private Float defaultThreshold;
	private int normalPeriodStartYear;
	private int normalPeriodEndYear;
	private String streamFlowName;
	private String climaticBuildDSJNDIName;
	
	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, CoreServiceImpl.class);
	
	private transient Session session;
			
	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#sendEmail(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void sendEmailSingle(@ServicePointParameter(name="to", description="Send to address") String to, 
			@ServicePointParameter(name="subject", description="Email subject") String subject, 
			@ServicePointParameter(name="text", description="Email content") String text) throws ConfigurationException {
		MimeMessage message = new MimeMessage(this.session);
		
		try {
			message.setFrom(new InternetAddress(getFromAddr(), true));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to, true));
			message.setSubject(subject);
			message.setText(text);
		
			Transport.send(message);
		} catch ( AddressException ae ) { 
			LOG.error("exception setting address", ae);
			throw new ConfigurationException("could not set message address");
		} catch ( MessagingException me ) { 
			LOG.error("exception sending message", me);
			throw new ConfigurationException("error sending message");
		}
	}

	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#sendEmail(java.lang.String[], java.lang.String, java.lang.String)
	 */
	public void sendEmailMulti(@ServicePointParameter(name="to", description="Send to addresses") String[] to, 
			@ServicePointParameter(name="subject", description="Email subject") String subject, 
			@ServicePointParameter(name="text", description="Email content") String text) throws ConfigurationException {
		
		MimeMessage message = new MimeMessage(this.session);
		
		try {
			message.setFrom(new InternetAddress(getFromAddr(), true));
			for ( String recp : to ) { 
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(recp, true));
			}
			message.setSubject(subject);
			message.setText(text);
		
			Transport.send(message);
		} catch ( AddressException ae ) { 
			LOG.error("exception setting address", ae);
			throw new ConfigurationException("could not set message address");
		} catch ( MessagingException me ) { 
			LOG.error("exception sending message", me);
			throw new ConfigurationException("error sending message");
		}
	}

	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getClimaticDSJNDIName()
	 */
	@ServicePoint(defaultValue="jdbc/firm/climateData")
	public String getClimaticDSJNDIName() {
		return this.climaticName;
	}

	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getSoilsDSJNDIName()
	 */
	@ServicePoint(defaultValue="jdbc/firm/soilData")
	public String getSoilsDSJNDIName() {
		return this.soilsName;
	}
	
	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getOceanicDSJNDIName()
	 */
	@ServicePoint(defaultValue="jdbc/firm/oceanData")
	public String getOceanicDSJNDIName() {
		return this.oceanicName;
	}

	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getSystemDSJNDIName()
	 */
	@ServicePoint(defaultValue="jdbc/firm/systemData")
	public String getSystemDSJNDIName() {
		return this.systemName;
	}
	
	@ServicePoint(defaultValue="jdbc/firm/streamData")
	public String getStreamFlowDSJNDIName() {
		return this.streamFlowName;
	}


	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getDefaultThreshold()
	 */
	@ServicePoint(defaultValue="0.9")
	public Float getDefaultThreshold() throws ConfigurationException {
		return this.defaultThreshold;
	}

	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#setDefaultThreshold(java.lang.Float)
	 */
	public void setDefaultThreshold(Float value) throws ConfigurationException {
		this.defaultThreshold = value;
	}

	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#setClimaticDSJNDIName(java.lang.String)
	 */
	public void setClimaticDSJNDIName(String str) {
		this.climaticName = str;
	}

	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#setSoilsDSJNDIName(java.lang.String)
	 */
	public void setSoilsDSJNDIName(String str) {
		this.soilsName = str;
	}
	
	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#setOceanicDSJNDIName(java.lang.String)
	 */
	public void setOceanicDSJNDIName(String str) {
		this.oceanicName = str;
	}

	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#setSystemDSJNDIName(java.lang.String)
	 */
	public void setSystemDSJNDIName(String str) {
		this.systemName = str;
	}

	
	
	public void setStreamFlowDSJNDIName(String str){
			this.streamFlowName=str;
		
	}
	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getFromAddr()
	 */
	@ServicePoint(defaultValue="yetijira@cse.unl.edu")
	public String getFromAddr() {
		return fromAddr;
	}

	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#setFromAddr(java.lang.String)
	 */
	public void setFromAddr(String fromAddr) {
		this.fromAddr = fromAddr;
	}

	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getSmtpHost()
	 */
	@ServicePoint(defaultValue="cse-mail.unl.edu")
	public String getSmtpHost() {
		
		return smtpHost;
	}

	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#setSmtpHost(java.lang.String)
	 */
	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
		setUpEmailSession();
	}

	/**
	 * Configures the e-mail session based on the current configuration settings
	 * @see #getSmtpHost()
	 */
	private void setUpEmailSession() { 
		Properties props = System.getProperties();
		props.put("mail.smtp.host", getSmtpHost());
		
		String user, password;
		if ( (user = getSmtpUser() ) != null ) {
			props.put("mail.smtp.user", user);
		}
		
		if ( (password = getSmtpPassword()) != null ) {
			props.put("mail.smtp.password", password);
		}
		
		this.session = Session.getDefaultInstance(props, null);
	}

	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getSmtpPassword()
	 */
	@ServicePoint(defaultValue="IGNORE", description="the SMTP user - if not required this should be IGNORE")
	public String getSmtpPassword() {
		if ( smtpPassword != null && smtpPassword.equals("IGNORE") ) { 
			return null;
		} else {
			return smtpPassword;
		}
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}
	
	/**
	 * 
	 * @see edu.unl.firm.CoreServiceMBean.SharedServiceMBean#getSmtpUser()
	 */
	@ServicePoint(defaultValue="IGNORE", description="the password for the SMTP user - if not required this should be IGNORE")
	public String getSmtpUser() {
	    if ( smtpUser != null && smtpUser.equals("IGNORE") ) {
		return null;
	    } else {
		return smtpUser;
	    }
	}
	
	
	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}
	
	public String getFrameworkVersion() {
		return new FrameworkVersion().toString();
	}

	@ServicePoint(defaultValue="1970")
	public int getNormalPeriodStartYear() {
		return normalPeriodStartYear;
	}

	public void setNormalPeriodStartYear(int normalPeriodStartYear) {
		this.normalPeriodStartYear = normalPeriodStartYear;
	}

	@ServicePoint(defaultValue="2000")
	public int getNormalPeriodEndYear() {
		return normalPeriodEndYear;
	}

	public void setNormalPeriodEndYear(int normalPeriodEndYear) {
		this.normalPeriodEndYear = normalPeriodEndYear;
	}

	@ServicePoint(defaultValue="climateBuildDS")
	public String getClimaticBuildDSJNDIName() {
		return climaticBuildDSJNDIName;
	}

	public void setClimaticBuildDSJNDIName(String climaticBuildDSJNDIName) {
		this.climaticBuildDSJNDIName = climaticBuildDSJNDIName;
	}	
}
