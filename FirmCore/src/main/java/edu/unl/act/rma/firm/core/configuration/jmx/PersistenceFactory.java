/* Created On: Sep 15, 2005 */
package edu.unl.act.rma.firm.core.configuration.jmx;

import static edu.unl.act.rma.firm.core.configuration.jmx.ServiceDeployer.persistencePath;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;

/**
 * @author Ian Cottingham
 *
 */
public class PersistenceFactory {
	
	private static Logger LOG = LogManager.getLogger(Loggers.SERVICE_LOG, PersistenceFactory.class);
	
	public static void exportObject(ServiceBase obj) throws IOException, ParserConfigurationException, IllegalAccessException, InvocationTargetException, 
		TransformerConfigurationException, TransformerException { 
		
		String fileName = obj.clazz.getName();
		File out_dir = new File(persistencePath);
		out_dir.mkdir();
		FileOutputStream xml = new FileOutputStream(out_dir+File.separator+fileName+".xml");

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element root = doc.createElement("JMXObject");
		root.setAttribute("written", new Date().toString());
		
		Element tmp = null;
		
		
		for ( Object key : obj.write.keySet() ) {
			String key_val = (String)key;
			
			Method getter = null;
			if ( obj.is_read.containsKey(key) ) { 
				getter = (Method)obj.is_read.get(key);
			} else if ( obj.get_read.containsKey(key) ) { 
				getter = (Method)obj.get_read.get(key);
			} else {
				continue;
			}
			
			
			tmp = doc.createElement("ObjectField");
			tmp.setAttribute("name", key_val);
			
			tmp.appendChild(doc.createTextNode(String.valueOf(getter.invoke(obj.backObject, new Object[0]))));
			
			root.appendChild(tmp);
		}
	
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DOMSource source = new DOMSource(root);
		Transformer trans = TransformerFactory.newInstance().newTransformer();
		StreamResult res = new StreamResult(baos);
		trans.transform(source, res);

		xml.write(baos.toByteArray());
		xml.close();
	}
		
	public static void importObject(ServiceBase obj) throws IllegalAccessException, InstantiationException, IOException, ParserConfigurationException, SAXException { 
		String fileName = obj.clazz.getName();
		File out_dir = new File(persistencePath);
		
		File xml = new File(out_dir+File.separator+fileName+".xml");
		
		boolean no_xml = ( xml.exists() ) ? false : true;
		
		Document doc = null;
		NodeList nl = null;
		if ( !no_xml ) { 
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
			nl = doc.getElementsByTagName("ObjectField");
		}
		
		for ( Object key : obj.write.keySet() ) {
			Method method = (Method)obj.write.get(key);
		
			Annotation anno = null;
			
			try {
				anno = ((Method)obj.get_read.get(key)).getAnnotation(ServicePoint.class);
			} catch ( NullPointerException npe ) { 
				anno = ((Method)obj.is_read.get(key)).getAnnotation(ServicePoint.class);
			}
			
			ServicePoint point = (anno == null) ? null : (ServicePoint)anno;
			String default_value = ( point == null ) ? null : point.defaultValue();
			default_value = ( (default_value == null) || (default_value.equals("")) ) ? null : default_value;
			
			String field_name = (String)key;
		
			if ( no_xml && point != null ) { 				
				/* no XML file, the default value will be used */
				LOG.warn("returning the default value for property: "+method.getName()+". Define a value for this property in the JMX console.");				
				if ( !(default_value == null) ) {
					set(default_value, method, obj.backObject);
				}
				continue;
			} else if ( no_xml ) { 
				continue;
			}
			
			boolean found_field = false;
			for ( int i=0; i<nl.getLength(); i++ ) { 
				Node n = nl.item(i);
				String node_name = n.getAttributes().getNamedItem("name").getNodeValue();
				
				if ( node_name.equals(field_name) ) {
					found_field = true;
				}
				
				if ( found_field ) {
					try { 
						set(n.getFirstChild().getNodeValue(), method, obj.backObject);
					} catch ( Exception e ) { 
						//LOG.error("unknown exception loading object", e);
						throw new IOException("unknown exception loading object");
					}
					break;
				}
			}
					
			if ( !found_field && default_value != null ) { 
				/* the field was not recorded in the XML file, use default */
				LOG.warn("returning the default value for property: "+method.getName()+". Define a value for this property in the JMX console.");
				set(default_value, method, obj.backObject);
			}	
		}
	}
	
	
	private static void set(String value, Method setter, Object obj) throws IllegalAccessException {
		if ( setter.getParameterTypes().length == 0 ) { 
			return;
		}
		
		Class type = setter.getParameterTypes()[0];
		try {
			if ( type == String.class ) {  
				setter.invoke(obj, new Object[] {value});
				
			} else if ( type == Integer.class ) { 
				setter.invoke(obj, new Object[] { Integer.valueOf(value) });
				
			} else if ( type == Double.class ) { 
				setter.invoke(obj, new Object[] { Double.valueOf(value) });
				
			} else if ( type == Float.class ) {
				setter.invoke(obj, new Object[] { Float.valueOf(value) });
				
			} else if ( type == Boolean.class ) { 
				setter.invoke(obj, new Object[] { Boolean.valueOf(value) });
				
			} else if ( type == int.class ) { 
				setter.invoke(obj, new Object[] { Integer.parseInt(value) });
				
			} else if ( type == double.class ) { 
				setter.invoke(obj, new Object[] { Double.parseDouble(value) });
										
			} else if ( type == float.class ) {
				setter.invoke(obj, new Object[] { Float.parseFloat(value) });
				
			} else if ( type == boolean.class ) { 
				setter.invoke(obj, new Object[] { Boolean.parseBoolean(value) });
			}
		} catch ( IllegalAccessException iae ) { 
			throw iae;
		} catch ( Exception e ) { 
			LOG.error("could not convert type for field "+setter.getName(), e);
			throw new IllegalAccessException("error parsing type for setter method "+setter.getName());
		}
	}
}