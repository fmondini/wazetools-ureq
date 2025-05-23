<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="java.util.*"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wtlib.auth.*"
	import="net.danisoft.wazetools.ureq.*"
%>
	<table class="TableSpacing_0px DS-full-width">

	<tr class="DS-text-bold DS-back-gray DS-border-updn">
		<td class="DS-padding-2px DS-border-lfrg" align="center" nowrap>Date / Time</td>
		<td class="DS-padding-2px DS-border-rg" align="center" nowrap>ID</td>
		<td class="DS-padding-2px DS-border-rg" align="center" ColSpan="2">Location &amp; Details</td>
		<td class="DS-padding-2px DS-border-rg" align="center" ColSpan="2">Operation</td>
		<td class="DS-padding-2px DS-border-rg" align="center" nowrap>Last Edit</td>
		<td class="DS-padding-2px DS-border-rg" align="center" nowrap>Solved By</td>
		<td class="DS-padding-2px DS-border-rg" align="center" nowrap>Elapsed</td>
		<td class="DS-padding-2px DS-border-rg" align="center" nowrap>Notes</td>
	</tr>
<%
	Database DB = null;

	try {

		String DateMin = EnvTool.getStr(request, "DateMin", "");
		String DateMax = EnvTool.getStr(request, "DateMax", "");

		if (DateMin.trim().equals("") || DateMax.trim().equals(""))
			throw new Exception("Error in dates range");

		DB = new Database();
		User USR = new User(DB.getConnection());
		Request REQ = new Request(DB.getConnection());

		String ResolveIcon, ResolveColor, ResolveDescr, ResolveTitle;
		User.Data usrData = USR.Read(SysTool.getCurrentUser(request));

		Vector<Request.Data> verReqData = REQ.getAll(usrData.getName(), DateMin, DateMax);

		if (verReqData.isEmpty()) {
%>
		<tr>
			<td class="DS-padding-24px DS-back-pastel-red DS-border-full" ColSpan="10">
				<div class="DS-text-large DS-text-exception DS-text-bold" align="center">No requests found in this period</div>
				<div class="DS-text-FireBrick" align="center">Select another period from the period drop-down menu</div>
			</td>
		</tr>
<%
		} else {
			
			for (Request.Data reqData : verReqData) {

				ResolveDescr = reqData.isResolve() ? "Resolve" : "Unlock";
				ResolveIcon = reqData.isResolve() ? "edit_location_alt" : "lock_open";
				ResolveColor = reqData.isResolve() ? "FireBrick" : "teal";
				ResolveTitle = reqData.isResolve() ? "Resolve Request" : "Unlock Only";
%>
				<tr class="DS-text-compact DS-border-dn">

					<td class="DS-padding-2px DS-border-lfrg" align="center">
						<div class="DS-text-fixed-compact"><%= FmtTool.fmtDateTime(reqData.getTimestamp()).replace(" ", "<br>") %></div>
					</td>
					<td class="DS-padding-2px DS-border-rg" align="center">
						<div class="DS-text-fixed-compact">#<%= FmtTool.fmtZeroPad(reqData.getID(), 5) %></div>
					</td>
					<td class="">
						<table class="TableSpacing_0px">
							<tr>
								<td class="DS-padding-lf-8px">
									<%= RequestStatus.getColorizedSpan(reqData.getStatus()) %>
								</td>
								<td class="DS-padding-lf-8px">
									<%= reqData.getRequestRankSpan(usrData.getRank()) %>
								</td>
								<td class="DS-padding-lf-8px">
									<a href="<%= reqData.CreatePermalink() %>" target="_blank"><%= reqData.getLocation() %></a>
								</td>
							</tr>
						</table>
					</td>
					<td class="DS-padding-rg-4px DS-border-rg" align="right">
						<span class="material-icons DS-cursor-pointer DS-text-gray DS-padding-top-4px"
							title="Status Details Info" onClick="ShowStatus('<%= reqData.getUUID() %>');">info_outline</span>
					</td>
					<td class="DS-padding-top-4px" title="<%= ResolveTitle %>" align="right">
						<span class="material-icons" style="color: <%= ResolveColor %>"><%= ResolveIcon %></span>
					</td>
					<td class="DS-padding-top-2px DS-padding-rg-2px DS-border-rg" title="<%= ResolveTitle %>">
						<div class="DS-text-<%= ResolveColor %> DS-padding-0px">&nbsp;<%= ResolveDescr %></div>
					</td>
					<td class="DS-padding-2px DS-border-rg" align="center"><%= reqData.getManagedBySpan() %></td>
					<td class="DS-padding-2px DS-border-rg" align="center"><%= reqData.getClosedBySpan() %></td>
					<td class="DS-padding-2px DS-border-rg" align="center"><%= reqData.getElapsedTimeSpan(false) %></td>
					<td class="DS-padding-2px DS-border-rg" align="center">
						<% if (reqData.getMotivation().trim().equals("")) { %>
							<div title="No notes attached to this request">
								<span class="material-icons DS-text-disabled DS-padding-top-8px">chat_bubble_outline</span>
							</div>
						<% } else { %>
							<div class="DS-cursor-pointer" title="Click to view request notes" onClick="ViewNotes(<%= reqData.getID() %>);">
								<span class="material-icons DS-text-green DS-padding-top-8px">speaker_notes</span>
							</div>
						<% } %>
					</td>

				</tr>
<%
			}
		}

	} catch (Exception e) {
%>
		<tr class="DS-back-pastel-red">
			<td class="DS-padding-16px DS-text-exception DS-border-full" align="center" ColSpan="10">
				<div class="DS-text-huge DS-text-bold">Internal Error</div>
				<div class="DS-text-large"><%= e.toString() %></div>
			</td>
		</tr>
<%
	}

	if (DB != null)
		DB.destroy();
%>
	</table>
