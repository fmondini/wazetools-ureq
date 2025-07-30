<%
	response.setHeader("Pragma","no-cache"); // HTTP 1.0
	response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
	response.setDateHeader ("Expires", 0); // prevents caching at the proxy server

	response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
	response.setHeader("Location", "./home/");
	response.setHeader("Connection", "close");
%>
