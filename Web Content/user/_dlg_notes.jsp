<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wazetools.ureq.*"
%>
	<div class="DS-padding-8px DS-back-gray DS-border-dn">
		<div class="DS-text-huge DS-text-bold DS-text-italic DS-text-black">Original Message</div>
	</div>

	<div class="DS-padding-8px">
<%
	Database DB = new Database();
	Request REQ = new Request(DB.getConnection());

	int ReqID = EnvTool.getInt(request, "ReqID", 0);

	try {

		Request.Data reqData = REQ.Read(ReqID);
%>
		<div class="DS-text-fixed-compact DS-text-black"><%= reqData.getMotivation().replace("\n", "<br>") %></div>
<%
	} catch (Exception e) {
%>
		<div class="DS-text-exception"><%= e.toString() %></div>
<%
	}

	DB.destroy();
%>
	</div>

	<div class="DS-padding-8px DS-back-gray DS-border-up" align="center">
		<%= MdcTool.Dialog.BtnDismiss("btnDismiss", "Close", true, "", "") %>
	</div>
