<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="java.util.*"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wazetools.ureq.*"
%>
<%!
	private static String CreateTR(String head, String body) {

		return(
			"<tr class=\"DS-border-dn\">" +
				"<td style=\"background: rgba(255, 255, 255, 0.50)\" class=\"DS-padding-lfrg-4px DS-padding-updn-2px DS-text-bold\" align=\"left\" valign=\"top\">" + head + "</td>" +
				"<td style=\"background: rgba(255, 255, 255, 0.25)\" class=\"DS-padding-lfrg-4px DS-padding-updn-2px DS-border-lf\" valign=\"top\">" + body + "</td>" +
			"</tr>"
		);
	}
%>
	<div class="DS-padding-8px" <%= (
		"style=\"" +
			"background-color:var(--body-background-color);" +
			"background-image:var(--body-background-image);" +
			"background-repeat:var(--body-background-repeat);" +
			"background-attachment:var(--body-background-attachment);" +
		"\""
	) %>

	align="center">

	<table class="TableSpacing_0px">
<%
	Database DB = null;

	String UreqUUID = EnvTool.getStr(request, "uuid", "");
	boolean withCloseBtn = EnvTool.getStr(request, "withCloseBtn", "N").equals("Y");

	try {

		DB = new Database();
		Request REQ = new Request(DB.getConnection());
		History URH = new History(DB.getConnection());

		if (UreqUUID.equals(Request.getDefaultUuid()))
			throw new Exception(
				"<div class=\"DS-text-large\">" +
					"<div>We're sorry, but this request was created with</div>" +
					"<div>an older version of UREQ, so it has no history</div>" +
				"</div>"
			);

		int ReqID = REQ.getIdByUuid(UreqUUID);

		if (ReqID == 0)
			throw new Exception(
				"<div class=\"DS-text-large\">" +
					"<div>The link you followed is wrong or no longer active.</div>" +
					"<div>Send the QR Code to SysOp and ask him to check.</div>" +
				"</div>"
			);

		Request.Data reqData = REQ.Read(ReqID);

		// Resolve Time

		String ResolveTime = "";

		if (reqData.isStatusRchk())
			reqData.setStatus(RequestStatus.DONE);

		if (reqData.getStatus().equals(RequestStatus.DONE))
			ResolveTime = reqData.getElapsedTimeSpan(true);
%>
		<tr>
			<td class="DS-padding-bottom-8px" ColSpan="2" align="center">
				<div class="DS-padding-updn-4px DS-back-lightgray DS-border-full DS-border-round <%= MdcTool.Elevation.Thin() %>">
					<div class="DS-text-title-shadow">UREQ Request Status</div>
					<div class="DS-text-big DS-padding-updn-4px">
						<%= RequestStatus.getColorizedSpan(reqData.getStatus()) %> (<%= reqData.getStatus().getDescr() %>)
					</div>
					<% if (!ResolveTime.equals("")) { %>
						<div class="DS-text-large DS-text-italic DS-text-green DS-padding-updn-4px">Solved in <%= ResolveTime %></div>
					<% } %>
				</div>
			</td>
		</tr>

		<%= CreateTR("ID", "#" + FmtTool.fmtZeroPad(reqData.getID(), 7)) %>
		<%= CreateTR("Generator", (reqData.getJsVersion().startsWith("W") ? "" : "UREQ Script ") + " " + reqData.getJsVersion()) %>
		<%= CreateTR("Date", FmtTool.fmtDateTime(reqData.getTimestamp())) %>
		<%= CreateTR("Mode", reqData.isResolve() ? "To be solved by an expert editor" : "Unlock only") %>
		<%= CreateTR("Requestor", reqData.getUser() + "<span class=\"DS-text-gray\">(" + reqData.getUserRank() + ")</span>") %>
		<%= CreateTR("Location", reqData.getLocation()) %>
		<%= CreateTR("Lock", "Items locked at no more than " + reqData.getLock()) %>
		<%= CreateTR("Reason", "<div class=\"DS-text-fixed-compact\">" + reqData.getMotivation().replace("\n", "<br>") + "</div>") %>
		<%= CreateTR("Updated", reqData.getManagedBy().equals("") ? "<span class=\"DS-text-disabled\">NO</span>" : FmtTool.fmtDateTime(reqData.getLastUpdate()).replace(" ", "&nbsp;") + "&nbsp;<span class=\"DS-text-gray\">by</span>&nbsp;" + reqData.getManagedBy()) %>
		<%= CreateTR("Closed", reqData.getSolvedBy().equals("") ? "<span class=\"DS-text-Crimson\">NO</span>" : FmtTool.fmtDateTime(reqData.getSolvedDate()).replace(" ", "&nbsp;") + "&nbsp;<span class=\"DS-text-gray\">by</span>&nbsp;" + reqData.getSolvedBy()) %>

		<tr class="DS-border-dn">
			<td class="DS-padding-top-4px DS-padding-bottom-8px" ColSpan="2">
				<div class="DS-padding-updn-4px DS-text-big DS-text-black DS-border-full DS-border-round DS-back-gray <%= MdcTool.Elevation.Thin() %>" align="center">Request History</div>
			</td>
		</tr>
<%
		Vector<History.Data> vecUrhData = URH.getAll(ReqID);

		String reqDate, reqTime, reqAction, reqEditor, reqComments;

		for (History.Data urhData : vecUrhData) {

			reqDate = FmtTool.fmtDate(urhData.getTimestamp());
			reqTime = FmtTool.fmtTimeNoSecs(urhData.getTimestamp());
			reqAction = RequestStatus.getColorizedSpan(urhData.getAction());
			reqEditor = urhData.getEditor();
			reqComments = urhData.getComments().replace("\n", "<br>");

			if (reqComments.trim().equals(""))
				reqComments = "<div class=\"DS-text-disabled DS-text-italic\">No comments found</div>";
%>
			<tr class="DS-text-small DS-border-none">
				<td class="DS-padding-4px DS-border-rg" align="center" nowrap><%= reqDate %> <%= reqTime %></td>
				<td class="DS-padding-4px"><%= reqAction %> by <%= reqEditor %></td>
			</tr>

			<tr class="DS-text-small DS-border-dn">
				<td class="DS-border-rg">&nbsp;</td>
				<td class="DS-padding-lfrg-4px DS-padding-bottom-4px" valign="top">
					<div class=""><%= reqComments %></div>
				</td>
			</tr>
<%
		}

	} catch (Exception e) {
%>
		<tr>
			<td class="DS-padding-4px" ColSpan="2">
				<div class="DS-padding-lfrg-8px DS-padding-updn-4px DS-back-pastel-red DS-border-round" align="center">
					<div class="DS-padding-0px">
						<div class="DS-text-exception">
							<div class="DS-text-huge DS-text-bold">ERROR</div>
						</div>
					</div>
					<div class="DS-padding-4px">
						<div class="DS-text-exception"><%= e.getMessage() %></div>
					</div>
				</div>
			</td>
		</tr>
<%
	}

	if (DB != null)
		DB.destroy();
	
	if (withCloseBtn) {
%>
		<tr class="DS-border-none">
			<td class="DS-padding-top-8px" ColSpan="2" align="center">
				<%= MdcTool.Dialog.BtnDismiss("btnDismiss", "Close", true, null, null) %>
			</td>
		</tr>
<%
	}
%>
	</table>
	</div>
