/* Created On: Sep 15, 2006 */
package edu.unl.act.rma.console.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.core.configuration.ConfigurationServer;

@WebServlet(name="JMXUpdate", urlPatterns="/console/jmxupdate")
public class JMXUpdate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, JMXUpdate.class);
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String object_name = req.getParameter("object_name");
		
		ConfigurationServer container = null;
		MBeanAttributeInfo[] infos = null;
		try {
			container = new ConfigurationServer(new ObjectName(object_name));
			infos = container.getInfo().getAttributes();
		} catch ( Exception e ) { 
			LOG.error(e.getMessage());
			writeResponse(res, false, e.getMessage());
			return;
		}

		String value = req.getParameter("attr_value");
		String name = req.getParameter("attr_name");
		
		MBeanAttributeInfo info = null;
		for ( MBeanAttributeInfo check : infos ) { 
			if ( check.getName().equals(name)) { 
				info = check;
				break;
			}
		}

		Object real_value = null;
		String type = info.getType();
		if ( type.equals("java.lang.Boolean") || type.equals("boolean") ) { 
			real_value = Boolean.valueOf(value);
		} else if ( type.equals("java.lang.Long") || type.equals("long") ) { 
			real_value = Long.valueOf(value);
		} else if ( type.equals("java.lang.Integer") || type.equals("int") ) {
			real_value = Integer.valueOf(value);
		} else if ( type.equals("java.lang.Double") || type.equals("double") ) {
			real_value = Double.valueOf(value);
		} else if ( type.equals("java.lang.Float") || type.equals("float") ) {
			real_value = Float.valueOf(value);
		} else if ( type.equals("java.lang.String") ) {
			real_value = value;
		} else { 
			writeResponse(res, false, "invalid type "+type);
			return;
		}

		container.set(info.getName(), real_value);
		
		writeResponse(res, true, name+" updated to value "+value);
	}
	
	private void writeResponse(HttpServletResponse resp, boolean success, String message) throws IOException {
		try {
	        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	        Document d = db.newDocument();
	
	        d.setXmlStandalone(true);
	        Element root = d.createElement("Result");
	        root.setAttribute("success", success ? "true" : "false");
	        root.appendChild(d.createTextNode(message));

            resp.setContentType("text/xml");
            PrintWriter out = resp.getWriter();

            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(root), new StreamResult(out));
		} catch (Exception e) {
			LOG.error("error creating response document", e);
			throw new IOException("Error creating response document");
		}
	}
}
