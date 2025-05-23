<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wtlib.auth.*"
	import="net.danisoft.wtlib.ureq.*"
	import="net.danisoft.wazetools.*"
	import="net.danisoft.wazetools.ureq.*"
%>
<%!
	private static final String PAGE_Title = AppCfg.getAppName() + " Home";
	private static final String PAGE_Keywords = "Waze.Tools UREQ, Waze, Tools, Unlock, Request";
	private static final String PAGE_Description = AppCfg.getAppAbstract();
%>
<!DOCTYPE html>
<html>
<head>
	<jsp:include page="../_common/head.jsp">
		<jsp:param name="PAGE_Title" value="<%= PAGE_Title %>"/>
		<jsp:param name="PAGE_Keywords" value="<%= PAGE_Keywords %>"/>
		<jsp:param name="PAGE_Description" value="<%= PAGE_Description %>"/>
	</jsp:include>
</head>

<body>

	<jsp:include page="../_common/header.jsp" />

	<div class="mdc-layout-grid DS-layout-body">
	<div class="mdc-layout-grid__inner">
<%
	String RedirectTo = "";

	Database DB = null;

	try {

		DB = new Database();
		TomcatRole TCR = new TomcatRole(DB.getConnection());

		if (!SysTool.isUserLoggedIn(request) || !TCR.haveRoleUREQ(SysTool.getCurrentUser(request), UreqRole.UNLCK)) {

			////////////////////////////////////////////////////////////////////////////////////////////////////
			//
			// ANONYMOUS
			//
%>
			<div class="<%= MdcTool.Layout.Cell(12, 8, 4) %>">
				<div class="DS-card-body">
					<div class="DS-text-title-shadow">UnLock Requests for Segments, Places &amp; Speed Cameras</div>
				</div>
			</div>

			<div class="<%= MdcTool.Layout.Cell(3, 4, 4) %> DS-padding-bottom-16px DS-border-rg">
				<%= MdcTool.Layout.IconCard(
					true,	// isEnabled
					"",		// Div Class
					"file_download",
					"https://code.waze.tools/home/browse.jsp?ShowSID=" + Request.getUreqScriptUUID(),
					"Download",
					"Download the script",
					"Get the WUREQ script from the<br>Waze.Tools CODE Repository",
					true,
					true
				) %>
			</div>
			<div class="<%= MdcTool.Layout.Cell(3, 4, 4) %> DS-padding-bottom-16px DS-border-rg">
				<%= MdcTool.Layout.IconCard(
					true,	// isEnabled
					"",		// Div Class		
					"note_add",
					"add_by_plink.jsp",
					"Handiwork",
					"Enter the data manually",
					"Manually enter the request data<br>Note: This method is obsolete",
					true,
					true
				) %>
			</div>
			<div class="<%= MdcTool.Layout.Cell(3, 4, 4) %> DS-padding-bottom-16px DS-border-rg">
				<%= MdcTool.Layout.IconCard(
					true,	// isEnabled
					"",		// Div Class		
					"manage_history",
					"../user/",
					"History",
					"Your Requests History",
					"Check the history of requests you have<br>made in the past and their status",
					true,
					true
				) %>
			</div>
			<div class="<%= MdcTool.Layout.Cell(3, 4, 4) %> DS-padding-bottom-16px">
				<%= MdcTool.Layout.IconCard(
					true,	// isEnabled
					"",		// Div Class		
					"vpn_key",
					"../unlock/",
					"Unlockers",
					"Unlockers Access",
					"If you are an unlocker please login<br>here to access all unlock features",
					true,
					true
				) %>
			</div>

			<div class="<%= MdcTool.Layout.Cell(12, 8, 4) %>">

				<div class="DS-card-body">
					<div class="DS-text-huge">UREQ in a nutshell <span class="DS-text-gray">(a.k.a. How Does It Work)</span></div>
				</div>
				<div class="DS-card-body">
					<div class="DS-text-justified">During the WME editing process it is quite common to come across some objects stuck
						at a block level higher than ours. In this case, our work on modifying an entire area could be interrupted by a
						single small stretch of road. Now, with this Script and WebApp, you will have an easy way to request an UnLock for segments,
						places and speed cameras from a higher level editor.</div>
				</div>
				<div class="DS-card-body">
					<div>Daily use of UREQ is very simple and straightforward:</div>
				</div>
				<div class="DS-card-body">
					<ul class="DS-ul-padding">
						<li class="DS-li-padding">Download and install the WUREQ Script for WME (see the download link)<br>
							<span class="DS-text-small DS-text-italic">NOTE: The script requires
							<a href="https://www.tampermonkey.net/" target="_blank">Tampermonkey</a>, a userscript manager by Jan Biniok.</span></li>
						<li class="DS-li-padding">In WME select one or more objects and click on "UREQ" in the top buttons bar</li>
						<li class="DS-li-padding">Enter a brief explanation of the reason for the request.
							This can help our unlockers better understand your request</li>
						<li class="DS-li-padding">Click on the "SEND" button</li>
						<li class="DS-li-padding">Wait a few minutes and one of our high-level editors will take care of your request, solving it.
							Our community have a team of qualified high-level editors and the average waiting queue is only a few minutes.
							You will be (optionally) informed of the result by a PM in the Waze Forum</li>
					</ul>
				</div>
				<div class="DS-card-body">
					<div class="DS-text-huge">Other ways to send unlock requests</div>
				</div>
				<div class="DS-card-foot">
					<div>If you don't want to get / install / use the WUREQ script, you can always enter your requests manually using
						the &quot;Handiwork&quot; link.</div>
					<div>Please note that this method, which is enabled for compatibility only,
						<span style="text-decoration: underline">is not currently recommended and will be removed in the future</span>.</div>
				</div>
			</div>
<%
		} else
			RedirectTo = "../unlock/"; // USER ALREADY LOGGED, SWITCH TO HIS HOME PAGE

	} catch (Exception e) {
%>
		<div class="<%= MdcTool.Layout.Cell(12, 8, 4) %>">
			<div class="DS-card-head">
				<div class="DS-text-subtitle DS-text-exception">Internal Error</div>
			</div>
			<div class="DS-card-foot">
				<div class="DS-text-exception"><%= e.toString() %></div>	
			</div>
		</div>
<%
	}

	if (DB != null)
		DB.destroy();
%>
	</div> <!-- /mdc-layout-grid__inner -->
	</div> <!-- /mdc-layout-grid -->

	<jsp:include page="../_common/footer.jsp">
		<jsp:param name="RedirectTo" value="<%= RedirectTo %>"/>
	</jsp:include>

</body>
</html>
