<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" import="java.util.List, javax.management.MBeanParameterInfo" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/console.tld" prefix="app" %>

<% List<String> service_names = (List<String>)session.getServletContext().getAttribute("SERVICE_NAMES"); %>

<% 
	String obj_name = request.getParameter("objectName"); 
	if ( obj_name == null ) { 
		obj_name = service_names.get(0);
	}
	
	String path = request.getContextPath();
	String port = ":"+String.valueOf(request.getServerPort());
	if ( port.equals(":80") ) 
		port = "";
	
	String basePath = request.getScheme()+"://"+request.getServerName()+port+path+"/"; 
%>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>FIRM Management Console</title>
		<link rel="stylesheet" type="text/css" href="style.css"/>
		<script type="text/javascript">
			function updateAttribute(object_name, attr_name) { 	
				xmlhttp = new XMLHttpRequest();
				xmlhttp.onreadystatechange = function() {
					if ( xmlhttp.readyState == 4 ) {
						var result = xmlhttp.responseXML.getElementsByTagName('Result')[0];
						var result_message = result.firstChild.nodeValue;
						alert(result_message);
						var submit_button = document.getElementById(attr_name+'_button');
						submit_button.value = attr_name;
						submit_button.disabled = "";
					}
				};
				var n = 'attr_'+attr_name;
				var value = document.getElementById(n).value;
				
				xmlhttp.open("POST", '<%= basePath %>console/jmxupdate', true);
				xmlhttp.setRequestHeader('Content-Type','application/x-www-form-urlencoded; charset=UTF-8');
				xmlhttp.send("attr_name="+attr_name+"&attr_value="+value+"&object_name="+object_name);
				
				var submit_button = document.getElementById(attr_name+'_button');
				submit_button.disabled = "1";
				submit_button.value = "Updating value...";
			}
	
			function invokeOp(object_name, op_name, args) { 	
				xmlhttp = new XMLHttpRequest();
				xmlhttp.onreadystatechange = function() {
					if ( xmlhttp.readyState == 4 ) {
						var result = xmlhttp.responseXML.getElementsByTagName('Result')[0];
						var result_message = result.firstChild.nodeValue;
						alert(result_message);
						var submit_button = document.getElementById(op_name+'_button');
						submit_button.value = op_name;
						submit_button.disabled = "";
					}
				};
				
				xmlhttp.open("POST", '<%= basePath %>console/jmxinvoke', true);
				xmlhttp.setRequestHeader('Content-Type','application/x-www-form-urlencoded; charset=UTF-8');
				var req_string = 'op_name='+op_name+'&object_name='+object_name;
				
				var i;
				
				for ( i in args ) {
					req_string = req_string + '&'+args[i]+'='+document.getElementById("param_"+op_name+"_"+args[i]).value;
				}
				
				xmlhttp.send(req_string);
				
				var submit_button = document.getElementById(op_name+'_button');
				submit_button.disabled = "1";
				submit_button.value = "Invoking Operation...";
			}
		</script>
	</head>
	<body>
		<h1>FIRM Management Console - Service Manager</h1>
		<hr style="border: 0; width: 98%; color: #305fb5; background-color: #305fb5; height: 1px;"/>
		<div style="position: absolute; top: 10px; right: 30px">
			<a href="SummaryView.xhtml">Summary</a>&nbsp;&nbsp;
			<a href="ServiceManager.jsp">Service Manager</a>&nbsp;&nbsp;
			<a href="LogViewer.xhtml">Log Manager</a>
		</div>	
		<div class="main">
			<div class="services">
				<h2>Deployed Services</h2>
				<table class="nav">
					<% for ( String str : service_names ) { %>
						<tr>
							<td><a href="ServiceManager.jsp?objectName=<%= str %>"><%= str %></a></td>
						</tr>
					<% } %>
				</table>
			</div>
			<div class="operations">
				<h2>Service Operations</h2>
				Object Name: <%= obj_name %>
				<p/>
				<h3>Attributes</h3>
				<table width="100%" cellspacing="0" cellpadding="0" border="0">
				<app:AttributeList objectName="<%= obj_name %>">
					<tr>
						<td>
							<% if ( writeable ) { %>
								<input id="<%= attributeName %>_button" type="button" value="<%= attributeName %>" onClick="updateAttribute('<%= obj_name %>', '<%= attributeName %>')"/>
							<% } else { %>
								<%= attributeName %>
							<% } %>
						</td>
						<td><%= attributeDescription %></td>
						<td>
						<% if ( writeable ) { %>
							<input size="35" type="text" id="attr_<%= attributeName %>" value="<%= attributeValue %>"/>
						<% } else { %>
							<%= attributeValue %>
						<% } %>
						</td>
					</tr>
					<tr><td colspan="3" height="5"><img src="images/odot.gif"/></td></tr>
				</app:AttributeList>
				</table>
				
				<h3>Operations</h3>
				<table width="95%" cellspacing="0" cellpadding="0" border="0">
				<app:OperationList objectName="<%= obj_name %>">
					<tr>
						<!-- build the initial argument string -->
						<%  int pos = 0;
						    boolean no_invoke = false;
							String arg_string = "new Array(";
						  for ( MBeanParameterInfo info : operationParameters ) {
							if ( !(info.getType().equals("java.lang.String")) &&
									!(info.getType().equals("java.lang.Double")) &&
									!(info.getType().equals("java.lang.Float")) &&
									!(info.getType().equals("java.lang.Integer")) &&
									!(info.getType().equals("java.lang.Boolean")) &&
									!(info.getType().equals("double")) &&
									!(info.getType().equals("float")) &&
									!(info.getType().equals("int")) &&
									!(info.getType().equals("boolean")) ) {
										no_invoke = true;
								}
							
							if ( pos++ == 0 ) {
								arg_string += "\'"+info.getName()+"\'";
							} else {
								arg_string += ", \'"+info.getName()+"\'";
							} 
						  }
						  	arg_string += ")"; %>	
					
						<td valign="top"><%= operationReturn %>
						<td valign="top">
							<% if (! no_invoke ) { %>
							<input type="button" id="<%= operationName %>_button" onClick="invokeOp('<%= obj_name %>', '<%= operationName %>', <%= arg_string %>)", value="<%= operationName %>"/></td>
							<% } else { %>
								<%= operationName %>
							<% } %>
						<td valign="top"><%= operationDescription %></td>
						<td valign="top">
							<table>
							<%  pos = 0; %>
							<% for ( MBeanParameterInfo info : operationParameters ) {
								pos++; %>				
								<tr>
									<td><%= info.getName() %></td>
									<td><input id="param_<%= operationName %>_<%= info.getName() %>" type="text" value="<%= info.getType() %>"/></td>
									<td> - <%= info.getDescription() %></td>
								</tr>
							<% } %>
							</table>
							<% if ( pos == 0 ) { %>
								no arguments
							<% } %>
							
							
							<input type="hidden" id="<%=operationName %>_args" value="<%= arg_string %>"/>
						</td>
					</tr>
					<tr><td colspan="4" height="10"><img src="images/odot.gif"/></td></tr>
				</app:OperationList>
				</table>

			</div>
		</div>
	</body>
</html>