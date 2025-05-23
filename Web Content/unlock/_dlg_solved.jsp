<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wtlib.auth.*"
	import="net.danisoft.wazetools.ureq.*"
%>
<%
	Database DB = null;

	int ReqID = EnvTool.getInt(request, "ReqID", 0);
	String Caller = EnvTool.getStr(request, "Caller", "");

	try {

		DB = new Database();
		User USR = new User(DB.getConnection());
		Request REQ = new Request(DB.getConnection());

		Request.Data reqData = REQ.Read(ReqID);
		User.Data usrData = USR.Read(reqData.getUser());
%>
		<div class="DS-padding-8px DS-back-gray DS-border-dn">
			<div class="DS-text-huge DS-text-black">Close Request #<%= ReqID %> as
				<%= RequestStatus.getColorizedSpan(RequestStatus.DONE) %>
			</div>
		</div>

		<form action="../unlock/_actions.jsp">

			<input type="hidden" name="Action" value="actSolve">
			<input type="hidden" name="ReqID" value="<%= ReqID %>">
			<input type="hidden" name="Caller" value="<%= Caller %>">

			<div class="DS-padding-8px">
				<%= reqData.getSummaryTable(Caller) %>
			</div>

			<div class="DS-padding-lfrg-8px DS-padding-bottom-8px">
				<% if (usrData.getWazerContacts().isEmpty()) { %>
					<div class="DS-padding-24px DS-border-full DS-back-pastel-red" align="center">
						<div class="DS-text-big DS-text-exception">Messaging feature not available for <%= reqData.getUser() %></div>
						<br>
						<div class="DS-text-purple"><%= usrData.getWazerContacts().getError() %></div>
					</div>
				<% } else { %>
					<div class="DS-text-italic"><%= usrData.getWazerContacts().getHead() %></div>
					<div class="DS-text-italic DS-text-compact DS-text-gray">Leave the text box blank to use the default message</div>
					<%= MdcTool.Text.Area("customMsg", "", null, 3, 70, null, false, null) %>
				<% } %>
			</div>

			<div class="DS-padding-8px DS-back-gray DS-border-up">
				<table class="TableSpacing_0px DS-full-width">
					<tr>
						<td class="CellPadding_0px" width="20%" align="left">
							<%= MdcTool.Dialog.BtnDismiss("btnDismiss", "&nbsp;Cancel", true, "", "", "arrow_back_ios_new", MdcTool.Button.Look.RAISED) %>
						</td>
						<td class="CellPadding_0px" width="60%" align="center" nowrap>
							<%= MdcTool.Check.Box("chkRecheck", "Re-Check needed", "ON", MdcTool.Check.Status.UNCHECKED, null) %>
						</td>
						<td class="CellPadding_0px" width="20%" align="right">
							<%= MdcTool.Button.SubmitTextIconClass(
								"check_circle",
								"&nbsp;Solved",
								null,
								"DS-text-lime",
								null,
								""
							) %>
						</td>
					</tr>
				</table>
			</div>

		</form>
<%
	} catch (Exception e) {
%>
		<div class="DS-padding-8px DS-back-gray">
			<div class="DS-text-exception"><%= e.toString() %></div>
		</div>
<%
	}

	if (DB != null)
		DB.destroy();
%>
