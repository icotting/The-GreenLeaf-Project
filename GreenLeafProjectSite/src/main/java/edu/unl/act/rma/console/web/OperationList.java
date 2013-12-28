/* Created On: Sep 27, 2006 */
package edu.unl.act.rma.console.web;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import edu.unl.act.rma.firm.core.configuration.ConfigurationServer;

/**
 * @author Ian Cottingham
 *
 * @jsp.tag		name="OperationList"
 *          	body-content = "JSP"
 * 
 * @jsp.variable	name-given="operationName"
 * @jsp.variable	name-given="operationDescription"
 * @jsp.variable	name-given="operationParameters"
 * 					class="javax.management.MBeanParameterInfo[]"
 * @jsp.variable	name-given="operationReturn"
 *
 */
public class OperationList extends BodyTagSupport {

	private static final long serialVersionUID = 1L;

	private int len;
	private MBeanOperationInfo[] ops;
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
		
		ops = info.getOperations();
		len = ops.length;
		pos = 0;
		if ( len == 0 ) { 
			return SKIP_BODY;
		}
		
		MBeanOperationInfo op_info = ops[pos++];
		pageContext.setAttribute("operationName", op_info.getName());
		pageContext.setAttribute("operationDescription", op_info.getDescription());
		pageContext.setAttribute("operationParameters", op_info.getSignature());
		pageContext.setAttribute("operationReturn", op_info.getReturnType());
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doAfterBody() throws JspException {
		if ( pos < len ) {
			MBeanOperationInfo op_info = ops[pos++];
			pageContext.setAttribute("operationName", op_info.getName());
			pageContext.setAttribute("operationDescription", op_info.getDescription());
			pageContext.setAttribute("operationParameters", op_info.getSignature());
			pageContext.setAttribute("operationReturn", op_info.getReturnType());
			
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