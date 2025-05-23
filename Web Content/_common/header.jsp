<%@ page
	language="java"
	contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"
	import="net.danisoft.dslib.*"
%>
	<!--
		[HEADER] START
	-->
<%
	SiteCfg SCFG = new SiteCfg();

	// TOPAPP BAR & DRAWER
	out.println(MdcTool.Header.getTopAppBarDiv(SCFG, request));

	// Draw image
	out.println(MdcTool.Header.getTopImageDiv(SCFG, request));

	// Draw Page Title
	out.println(MdcTool.Header.getPageTitleDiv(EnvTool.getStr(request, "Page-Title", "")));
%>
	<!--
		[HEADER] -END-
	-->
