/* Created On: Sep 27, 2006 */
package edu.unl.act.rma.console.web;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import edu.unl.act.rma.firm.core.configuration.ConfigurationException;
import edu.unl.act.rma.firm.core.configuration.ConfigurationServer;

/**
 * @author Ian Cottingham
 *
 * @jsp.tag		name="AttributeList"
 *          	body-content = "JSP"
 *          
 * @jsp.variable	name-given="writeable"
 * 					class="java.lang.Boolean"
 * 
 * @jsp.variable	name-given="attributeName"
 * @jsp.variable	name-given="attributeValue" 
 * @jsp.variable	name-given="attributeDescription"
 *
 */
public class AttributeList extends BodyTagSupport {

	private static final long serialVersionUID = 1L;

	private int len;
	private MBeanAttributeInfo[] attributes;
	private ConfigurationServer container;
	private int pos;
	private String objectName;
	
	@Override
	public int doStartTag() throws JspException {
		MBeanInfo info = null;
		try {
			container = new ConfigurationServer(new ObjectName(objectName));
			info = container.getInfo();			
		} catch ( Exception e ) { 
			throw new JspException(e.getMessage());
		}
		
		attributes = info.getAttributes();
		len = attributes.length;
		pos = 0;
		if ( len == 0 ) { 
			return SKIP_BODY;
		}
		
		MBeanAttributeInfo attr_info = attributes[pos++];
		try {
			pageContext.setAttribute("writeable", Boolean.valueOf(attr_info.isWritable()));
			pageContext.setAttribute("attributeName", attr_info.getName());
			pageContext.setAttribute("attributeDescription", attr_info.getDescription());
			pageContext.setAttribute("attributeValue", String.valueOf(container.get(attr_info.getName())));
		} catch ( ConfigurationException jme ) { 
			throw new JspException(jme.getMessage());
		}
		
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doAfterBody() throws JspException {
		if ( pos < len ) {
			MBeanAttributeInfo info = attributes[pos++];
			
			try {
				pageContext.setAttribute("writeable", Boolean.valueOf(info.isWritable()));
				pageContext.setAttribute("attributeName", info.getName());
				pageContext.setAttribute("attributeDescription", info.getDescription());
				pageContext.setAttribute("attributeValue", String.valueOf(container.get(info.getName())));
			} catch ( ConfigurationException jme ) { 
				throw new JspException(jme.getMessage());
			}
			
			return EVAL_BODY_AGAIN;
		} else { 
			return EVAL_PAGE;
		}
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}	
}