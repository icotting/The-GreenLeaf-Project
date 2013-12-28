/* Created On: Sep 15, 2006 */
package edu.unl.act.rma.console.web;

import java.io.StringWriter;
import java.sql.SQLException;

import javax.management.BadAttributeValueExpException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.joda.time.DateTime;
import org.joda.time.Period;

import edu.unl.act.rma.console.acis.ACISDataBuilder;
import edu.unl.act.rma.console.acis.BuildMonitor;
import edu.unl.act.rma.console.acis.BuildType;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.configuration.jmx.JMXException;


/**
 * @author Ian Cottingham
 *
 */
public class NWSDataService {

	private Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, NWSDataServiceImpl.class);
	
	public static final String NAME = "DATASET_BUILDER";
	
	private ACISDataBuilder builder;
	private BuildMonitor monitor;
	
	public NWSDataService() { 
		this.builder = new ACISDataBuilder();
	}
	
	public String getDataWriterStatus() throws BadAttributeValueExpException, JMXException {
		if ( monitor == null ) 
			return "<ManagementTask><State>NOT STARTED</State></ManagementTask>";
		
		try { 
			DOMSource source = new DOMSource(monitor.dataWriterXML());
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StringWriter xml = new StringWriter();
			StreamResult res = new StreamResult(xml);
			
			transformer.transform(source, res);
			return xml.toString();
		} catch ( ParserConfigurationException pce ) { 
			LOG.error("exception configuring the parser", pce);
			throw new BadAttributeValueExpException("could not configure an XML parser");
		} catch ( TransformerConfigurationException tce ) { 
			LOG.error("exception configuring the transformer", tce);
			throw new BadAttributeValueExpException("could not configure the XML transformer");
		} catch ( TransformerException te ) { 
			LOG.error("exception transforming XML", te);
			throw new BadAttributeValueExpException("could not transform XML");
		}
	}

	public String getQueryWriterStatus() throws BadAttributeValueExpException {
		if ( monitor == null ) 
			return "<ManagementTask><State>NOT STARTED</State></ManagementTask>";
		
		try { 
			DOMSource source = new DOMSource(monitor.queryWriterXML());
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StringWriter xml = new StringWriter();
			StreamResult res = new StreamResult(xml);
			
			transformer.transform(source, res);
			return xml.toString();
		} catch ( ParserConfigurationException pce ) { 
			LOG.error("exception configuring the parser", pce);
			throw new BadAttributeValueExpException("could not configure an XML parser");
		} catch ( TransformerConfigurationException tce ) { 
			LOG.error("exception configuring the transformer", tce);
			throw new BadAttributeValueExpException("could not configure the XML transformer");
		} catch ( TransformerException te ) { 
			LOG.error("exception transforming XML", te);
			throw new BadAttributeValueExpException("could not transform XML");
		}
	}

	public int getQueueCapacity() {
		return ( monitor == null) ? -1 : monitor.queueSize();
	}

	public int getQueueSize() {
		return ( monitor == null ) ? -1 : monitor.remainingQueueCapacity();
	}

	public String getRunTime() throws BadAttributeValueExpException {
		if ( monitor == null ) 
			return "0hr 0min 0sec";
		
		Period p = monitor.getRuntime();
		float seconds = p.getSeconds();
		float minutes = ( seconds / 60 );
		float hours = ( minutes / 60 );
		
		return (int)hours+"hr "+(int)(minutes%60)+"min "+(int)(seconds%60)+"sec";
	}
	
	public String getType() {
		return (monitor == null) ? "" : monitor.getType().name();
	}

	/* operations */
	public void createClimateSchemas() throws JMXException {
		try { 
			ClimateSourceSQL.createSchemas();
		} catch ( SQLException sqe ) { 
			LOG.error("exception creating schemas", sqe);
			throw new JMXException("exception creating schemas");
		} catch ( Exception e ) { 
			LOG.error("unknown exception", e);
			throw new JMXException(e.getMessage());
		}
		
	}

	/*
	public void createCropSchemas() throws JMXException {
		try { 
			CropSourceSQL.createSchemas();
		} catch ( SQLException sqe ) { 
			LOG.error("exception creating schemas", sqe);
			throw new JMXException("exception creating schemas");
		}
	}

	public void truncateCropTables() throws JMXException {
		try { 
			CropSourceSQL.truncateData();
		} catch ( SQLException sqe ) { 
			LOG.error("exception truncating tables", sqe);
			throw new JMXException("exception truncating tables");
		}
	}

	public void runNASSBuild(final Integer start, final Integer end) throws JMXException {
		try { 
			new Thread() {
				public void run() {
					try {
						NassDataBuilder.build(start, end);
					} catch ( RemoteException re ) { 
						LOG.error("exception building nass dataset", re);
					}
				}
			}.start();
		} catch ( Exception sqe ) { 
			LOG.error("exception starting nass build", sqe);
			throw new JMXException("exception starting nass build");
		}
	}
	*/

	public void runACISBuild(String stateExpression) {
		try {
			monitor = builder.runBuild(NWSDataServiceManager.getInstance().getTemporaryPath(), BuildType.BUILD, new DateTime(System.currentTimeMillis()), stateExpression, -1);
		} catch ( Exception e ) {
			LOG.error("could not start the builder", e);
			throw new RuntimeException("error communicating with the data set service");
		}
	}
	
	public void haltACISBuild() { 
		try {
			monitor.haltBuild();
		} catch ( Exception e ) {
			LOG.error("could not halt the builder", e);
			throw new RuntimeException("error communicating with the data set service");
		}
	}
	
	public BuildMonitor getMonitor() { 
		return monitor;
	}
}
