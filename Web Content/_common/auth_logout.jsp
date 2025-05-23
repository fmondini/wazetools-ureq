<%@	page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="java.net.*"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wazetools.*"
%>
<!doctype html>
<html>
<body>
<%
	String msg = EnvTool.getStr(request, "msg", "");

	response.setHeader("Pragma","no-cache"); // HTTP 1.0
	response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
	response.setDateHeader ("Expires", 0); // prevents caching at the proxy server

	request.getSession().invalidate();

	String newLocation = AppCfg.getAuthOnLogoutUrl();

	if (!msg.equals(""))
		newLocation += ("?msg=" + URLEncoder.encode(msg, "UTF-8"));

	response.setStatus(301);
	response.setHeader("Location", newLocation);
	response.setHeader("Connection", "close");
%>
</body>
</html>
