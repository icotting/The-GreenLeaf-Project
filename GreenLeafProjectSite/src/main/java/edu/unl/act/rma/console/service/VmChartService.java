/* Created on: Jan 28, 2010 */
package edu.unl.act.rma.console.service;

import java.io.StringWriter;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import edu.unl.act.rma.console.beans.VmStatusBean;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;

/**
 * @author Ian Cottingham
 *
 */
@ManagedBean
@Path("/console/charts")
public class VmChartService {
	
	private Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, VmChartService.class);
	
	@Inject @Named("vmBean") VmStatusBean vmBean;
	
	@Path("/memory/nonheap")
	@GET
	@Produces("text/xml")
	public String nonHeapPieChart() { 
		
		XMLStreamWriter writer;
		StringWriter out = new StringWriter();
		
		try {
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
			writer.writeStartDocument();
			writer.writeStartElement("chart");
			writer.writeAttribute("bgcolor", "#ffffff,#ffffff");
			writer.writeAttribute("caption", "Non-Heap Memory");
			writer.writeAttribute("showBorder", "0");
			writer.writeAttribute("showPercentageValues", "0");
			writer.writeAttribute("showLabels", "1");
			writer.writeAttribute("chartLeftMargin", "0");
			writer.writeAttribute("chartRightMargin", "0");
			writer.writeAttribute("chartTopMargin", "0");
			writer.writeAttribute("chartBottomMargin", "0");			
			writer.writeAttribute("showValues", "0");
			writer.writeAttribute("animation", "0");
			
			writer.writeStartElement("set");
			writer.writeAttribute("label", "Initial");
			writer.writeAttribute("value", String.valueOf(vmBean.getNonHeapInit()));
			writer.writeEndElement();
			
			
			writer.writeStartElement("set");
			writer.writeAttribute("label", "Used");
			writer.writeAttribute("value", String.valueOf(vmBean.getNonHeapUsed()));
			writer.writeEndElement();
			
						
			writer.writeStartElement("set");
			writer.writeAttribute("label", "Committed");
			writer.writeAttribute("value", String.valueOf(vmBean.getNonHeapCommitted()));
			writer.writeEndElement();
			
						
			writer.writeStartElement("set");
			writer.writeAttribute("label", "Max");
			writer.writeAttribute("value", String.valueOf(vmBean.getNonHeapMax()));
			writer.writeEndElement();
			
			writer.writeEndElement();
			writer.writeEndDocument();
			
		} catch ( Exception e ) { 
			LOG.error("An error occured generating the chart xml", e);
			RuntimeException re = new RuntimeException("The chart could not be generated");
			re.initCause(e);
			throw re;
		}
		
		return out.toString();
	}
	
	
	@Path("/memory/heap")
	@GET
	@Produces("text/xml")
	public String heapPieChart() { 
		
		XMLStreamWriter writer;
		StringWriter out = new StringWriter();
		
		try {
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
			writer.writeStartDocument();
			writer.writeStartElement("chart");
			writer.writeAttribute("bgcolor", "#ffffff,#ffffff");
			writer.writeAttribute("caption", "Heap Memory");
			writer.writeAttribute("showBorder", "0");
			writer.writeAttribute("showPercentageValues", "0");
			writer.writeAttribute("showLabels", "1");
			writer.writeAttribute("chartLeftMargin", "0");
			writer.writeAttribute("chartRightMargin", "0");
			writer.writeAttribute("chartTopMargin", "0");
			writer.writeAttribute("chartBottomMargin", "0");			
			writer.writeAttribute("showValues", "0");
			writer.writeAttribute("animation", "0");
			
			writer.writeStartElement("set");
			writer.writeAttribute("label", "Initial");
			writer.writeAttribute("value", String.valueOf(vmBean.getHeapInit()));
			writer.writeEndElement();
			
			
			writer.writeStartElement("set");
			writer.writeAttribute("label", "Used");
			writer.writeAttribute("value", String.valueOf(vmBean.getHeapUsed()));
			writer.writeEndElement();
			
						
			writer.writeStartElement("set");
			writer.writeAttribute("label", "Committed");
			writer.writeAttribute("value", String.valueOf(vmBean.getHeapCommitted()));
			writer.writeEndElement();
			
						
			writer.writeStartElement("set");
			writer.writeAttribute("label", "Max");
			writer.writeAttribute("value", String.valueOf(vmBean.getHeapMax()));
			writer.writeEndElement();
			
			writer.writeEndElement();
			writer.writeEndDocument();
			
		} catch ( Exception e ) { 
			LOG.error("An error occured generating the chart xml", e);
			RuntimeException re = new RuntimeException("The chart could not be generated");
			re.initCause(e);
			throw re;
		}
		
		return out.toString();
	}
}
