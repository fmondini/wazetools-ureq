<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="java.util.*"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wtlib.auth.*"
	import="net.danisoft.wazetools.ureq.*"
%>
<%!
	private static HashMap<String, Boolean> getStatusesHashMap(Cookie[] allCookies) {

		HashMap<String, Boolean> StatusesHashMap = new HashMap<>();
		String cookieName;

		if (allCookies != null) {
			for (RequestStatus X : RequestStatus.values()) {
				if (!X.equals(RequestStatus.UNKN)) {

					cookieName = Request.getLastStatusCookieMask().replace("{}", X.getValue().toLowerCase());
					StatusesHashMap.put(cookieName, false);

					for (Cookie cookie : allCookies) {
						if (cookie.getName().equals(cookieName))
							StatusesHashMap.put(cookieName, cookie.getValue().equalsIgnoreCase("true"));
					}
				}
			}
		}

		return(StatusesHashMap);
	}
%>
	<script>

		/**
		 * Show Status DIV
		 */
		function ShowStatus(ReqUUID) {

			$.ajax({

				async: true,
				cache: false,
				type: 'GET',
				dataType: 'text',
				url: '../status/_div_status.jsp',

				data: {
					uuid: ReqUUID,
					withCloseBtn: 'Y'
				},

				success: function(data) {
					ShowDialog_AJAX(data);
				},

				error: function(jqXHR, textStatus, errorThrown) {
					ShowDialog_OK('An error has occurred', jqXHR.responseText, 'OK');
					console.error('jqXHR: %o', jqXHR);
					console.error('textStatus: %o', textStatus);
					console.error('errorThrown: %o', errorThrown);
				}
			});
		}

		/**
		 * Show Config DIV
		 */
		function ShowConfigDlg(cfgType) {

			$.ajax({

				async: true,
				cache: false,
				type: 'GET',
				dataType: 'text',

				url: (cfgType == 'LAST'
					? './_dlg_lastreq_cfg.jsp'
					: (cfgType == 'RCHK'
						? './_dlg_rchkreq_cfg.jsp'
						: './NotFoundError'
					)
				),

				success: function(data) {
					ShowDialog_AJAX(data);
				},

				error: function(jqXHR, textStatus, errorThrown) {
					ShowDialog_OK('An error has occurred', jqXHR.responseText, 'OK');
					console.error('jqXHR: %o', jqXHR);
					console.error('textStatus: %o', textStatus);
					console.error('errorThrown: %o', errorThrown);
				}
			});
		}

	</script>

	<table class="TableSpacing_0px DS-full-width">

	<tr class="DS-text-bold DS-back-gray DS-border-updn">
		<td class="DS-padding-2px DS-border-lfrg" align="center">Date/Time</td>
		<td class="DS-padding-2px DS-border-rg" align="center">Age</td>
		<td class="DS-padding-2px DS-border-rg" align="center">User / ReqID</td>
		<td class="DS-padding-2px DS-border-rg" align="center" ColSpan="2">Location &amp; Details</td>
		<td class="DS-padding-2px DS-border-rg" align="center" ColSpan="2">Operation</td>
		<td class="DS-padding-2px DS-border-rg" align="center">Managed</td>
		<td class="DS-padding-2px DS-border-rg" align="center">Closed</td>
		<td class="DS-padding-2px DS-border-rg" align="center">Elapsed</td>
	</tr>
<%
	Database DB = null;

	try {

		DB = new Database();
		Request REQ = new Request(DB.getConnection());

		// Logged In User Data

		User USR = new User(DB.getConnection());
		User.Data usrData =	USR.Read(SysTool.getCurrentUser(request));
		int loggedUserRank = usrData.getRank();

		// Read cookies

		HashMap<String, Boolean> StatusesHashMap = getStatusesHashMap(request.getCookies());

		// Request Period

		String DateMin = EnvTool.getStr(request, "DateMin", FmtTool.fmtDateTimeSqlStyle(FmtTool.DATEZERO));
		String DateMax = EnvTool.getStr(request, "DateMax", FmtTool.fmtDateTimeSqlStyle(FmtTool.DATEMAXV));

		if (DateMin.trim().equals("") || DateMax.trim().equals(""))
			throw new Exception("Error in dates range");

		// Request Type

		String reqType = EnvTool.getStr(request, "Type", "");

		boolean isTypeLast = false;
		boolean isTypeRchk = false;

		if (reqType.equals("LAST"))
			isTypeLast = true;
		else if (reqType.equals("RCHK"))
			isTypeRchk = true;
		else
			throw new Exception("Bad Type: '" + reqType + "'");

		//
		// Get data vector
		//

		Vector<Request.Data> vecReqData;

		if (isTypeLast) {
			vecReqData = REQ.getRecent(
				"", // TODO: Country Filter in REQ.getRecent()
				(Request.isMyAreasOnly(request.getCookies()) ? SysTool.getCurrentUser(request) : ""),
				DateMin,
				DateMax
			);
		} else if (isTypeRchk) {
			vecReqData = REQ.getRecheckQueue(
				"IT", // TODO: Country Filter in REQ.getRecheckQueue()
				(Request.isMyQueueOnly(request.getCookies()) ? SysTool.getCurrentUser(request) : null)
			);

		} else
			throw new Exception("Bad Type: '" + reqType + "'");

		// Show data

		int ReqCount = 0;
		String ResolveIcon, ResolveColor, ResolveDescr, ResolveTitle, ElapsedTime, RequestAge;

		for (Request.Data reqData : vecReqData) {

			ResolveDescr = reqData.isResolve() ? "Resolve" : "Unlock";
			ResolveIcon = reqData.isResolve() ? "edit_location_alt" : "lock_open";
			ResolveColor = reqData.isResolve() ? "FireBrick" : "teal";
			ResolveTitle = reqData.isResolve() ? "Resolve Request" : "Unlock Only";
			RequestAge = FmtTool.DaysBetween(reqData.getTimestamp(), FmtTool.Date2Timestamp(new Date()), "<br>day(s)");
			ElapsedTime = reqData.getElapsedTimeSpan(false);

			if (StatusesHashMap.get(Request.getLastStatusCookieMask().replace("{}", reqData.getStatus().getValue().toLowerCase()))) {
%>
				<tr class="DS-text-compact DS-border-dn">

					<td class="DS-padding-2px DS-border-lfrg" align="center">
						<div class="DS-text-fixed-compact"><%= FmtTool.fmtDateTime(reqData.getTimestamp()).replace(" ", "<br>") %></div>
					</td>
					<td class="DS-padding-2px DS-border-rg" align="center">
						<%= RequestAge.contains(">0<") ? "<span style=\"color:green\">[new]</span>" : RequestAge %>
					</td>
					<td class="DS-padding-2px DS-border-rg" align="center">
						<div><a href="<%= REQ.GetUserProfileLink(reqData.getUser()) %>" title="Open User Info" target="_blank"><%= reqData.getUser() %></a><span class="DS-text-gray">(<%= reqData.getUserRank() %>)</span></div>
						<div class="DS-text-fixed-compact">ID #<%= FmtTool.fmtZeroPad(reqData.getID(), 5) %></div>
					</td>
					<td class="">
						<table class="TableSpacing_0px">
							<tr>
								<td class="DS-padding-lf-8px">
									<%= RequestStatus.getColorizedSpan(reqData.getStatus()) %>
								</td>
								<td class="DS-padding-lf-8px">
									<%= reqData.getRequestRankSpan(loggedUserRank) %>
								</td>
								<td class="DS-padding-lf-8px">
									<a href="#" onClick="ShowRequestDetails(<%= reqData.getID() %>, '<%= isTypeLast ? "LAST" : "RCHK" %>');" title="Show Request Details">
										<%= reqData.getLocation().trim().equals("")
											? "<span class=\"DS-text-italic DS-text-disabled\">[no location data found]</span>"
											: reqData.getLocation()
										%>
									</a>
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
					<td class="DS-padding-2px DS-border-rg" align="center"><%= ElapsedTime %></td>
				</tr>
<%
				ReqCount++;

				if (ReqCount >= Request.getMaxReqToShow())
					break;
			}
		}

		if (ReqCount == 0) {
%>
			<tr>
				<td class="DS-padding-24px DS-back-pastel-red DS-border-full" ColSpan="10">
					<div class="DS-text-large DS-text-exception DS-text-bold" align="center">No requests found in this period</div>
					<div class="DS-text-FireBrick" align="center">Select another period from the period drop-down menu</div>
					<div class="DS-text-FireBrick" align="center">and/or check your filter settings by clicking the <span class="material-icons DS-icons-icon-small">settings</span> icon</div>
				</td>
			</tr>
<%
		} else if (ReqCount == Request.getMaxReqToShow()) {
%>
			<tr>
				<td class="DS-padding-24px DS-back-pastel-red DS-border-full" ColSpan="10" align="center">
					<div class="DS-text-large DS-text-exception DS-text-bold">Too many requests were found - Limit Exceeded</div>
					<div class="DS-text-FireBrick">Due to an internal limit, only the <b><%= Request.getMaxReqToShow() %> most recent</b> requests are displayed</div>
				</td>
			</tr>
<%
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
