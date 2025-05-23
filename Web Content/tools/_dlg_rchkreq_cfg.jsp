<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wazetools.ureq.*"
%>
<%
	boolean chkMyQueueOnly = false;

	Cookie AllCookies[] = request.getCookies();

	for (int i=0; i<AllCookies.length; i++) {

		if (AllCookies[i].getName().equals(Request.getMyQueueOnlyCookieName()))
			chkMyQueueOnly = AllCookies[i].getValue().equalsIgnoreCase("true");
	}
%>
	<div class="DS-padding-8px DS-back-darkgray DS-border-dn">
		<div class="DS-text-huge">Recheck Queue Options</div>
	</div>

	<div class="DS-padding-8px DS-back-white DS-border-dn">

		<div class="DS-text-big">Users Filter</div>
		<div class="DS-text-compact DS-text-italic DS-text-gray">Uncheck this option to view the full queue</div>

		<%= MdcTool.Check.Box(
			Request.getMyQueueOnlyObjectName(),
			"Show only the <b>" + SysTool.getCurrentUser(request) + "</b> request queue",
			"Y",
			(chkMyQueueOnly ? MdcTool.Check.Status.CHECKED : MdcTool.Check.Status.UNCHECKED),
			"onChange=\"setCookie('" + Request.getMyQueueOnlyCookieName() + "', $(this).is(':checked'));\""
		) %>

	</div>

	<div class="DS-padding-8px DS-back-darkgray" align="center">
		<%= MdcTool.Dialog.BtnDismiss(
			"btnDismiss",
			"Close",
			true,
			"onClick=\"PopulateList();\"",
			null
		) %>
	</div>
