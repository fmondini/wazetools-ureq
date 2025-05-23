<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wazetools.ureq.*"
%>
	<script>

		function _dlg_details_UpdateNotes(obj, ReqID) {

			var newNotesValue = $('#' + obj.id).val();

			$.ajax({

				async: true,
				cache: false,
				type: 'POST',
				dataType: 'text',
				url: '../unlock/_actions.jsp',
				data: { Action: 'actUpdateNotes', ReqID: ReqID, NewNotesValue: encodeURI(newNotesValue) },

				error: function(jqXHR, textStatus, errorThrown) {
					console.log('_dlg_details_UpdateNotes() ERROR ' + jqXHR.status + ' in ' + errorThrown);
				}
			});

		}

	</script>
<%
	Database DB = null;

	String Caller = EnvTool.getStr(request, "Caller", "");
	int ReqID = EnvTool.getInt(request, "ReqID", 0);

	try {

		DB = new Database();
		Request REQ = new Request(DB.getConnection());

		Request.Data reqData = REQ.Read(ReqID);

		// Prepare permalink

		String Permalink = reqData.CreatePermalink();
		Cookie[] cookies = request.getCookies();

		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("ureq-wmeb"))
				if (cookie.getValue().equals("true"))
					Permalink = reqData.CreatePermalinkBeta();
		}

		// Set Buttons Status

		boolean btnUnlEnabled = false;
		boolean btnRejEnabled = false;
		boolean btnSolEnabled = false;

		if (reqData.isStatusOpen()) { btnUnlEnabled =  true; btnRejEnabled =  true; btnSolEnabled =  true; } else {
		if (reqData.isStatusWork()) { btnUnlEnabled =  true; btnRejEnabled =  true; btnSolEnabled =  true; } else {
		if (reqData.isStatusInfo()) { btnUnlEnabled =  true; btnRejEnabled =  true; btnSolEnabled =  true; } else {
		if (reqData.isStatusRjct()) { btnUnlEnabled = false; btnRejEnabled = false; btnSolEnabled = false; } else {
		if (reqData.isStatusRchk()) { btnUnlEnabled =  true; btnRejEnabled = false; btnSolEnabled =  true; } else {
		if (reqData.isStatusDone()) { btnUnlEnabled = false; btnRejEnabled = false; btnSolEnabled = false; } else {
			// Do Nothing
		}}}}}}
%>
		<div class="DS-padding-8px DS-back-gray DS-border-dn">
			<div class="DS-text-huge DS-text-black">Processing unlock request #<%= ReqID %></div>
		</div>

		<div class="DS-padding-8px">
			<%= reqData.getSummaryTable(Caller) %>
		</div>

		<div class="DS-padding-lfrg-8px DS-padding-bottom-8px">
			<div class="DS-text-italic">Comments on this request</div>
			<div class="DS-text-compact DS-text-italic DS-text-exception">These comments are private and <b>not visible</b> to the user</div>
			<%= MdcTool.Text.Area("REQ_Notes", "", "", 3, 70, reqData.getNotes(), false, "onBlur=\"_dlg_details_UpdateNotes(this, " + reqData.getID() + ");\"") %>
		</div>

		<div class="DS-padding-8px DS-back-gray DS-border-up" align="center">
			<table class="TableSpacing_0px">
				<tr>

					<td class="CellPadding_0px" align="left">
						<%= MdcTool.Dialog.BtnDismiss( 
							"btnDismiss",				// id
							"&nbsp;Back",				// label
							true,						// isDefault
							"",							// additionalData
							"",							// tooltip
							"arrow_back_ios_new",		// btnLeftIcon
							MdcTool.Button.Look.RAISED	// btnLook
						) %>
					</td>

					<td class="DS-padding-4px"></td>

					<td class="CellPadding_0px" align="center">
						<%= MdcTool.Button.TextIconClass(
							"live_help",				// btnIconLeft
							"&nbsp;Request Info",		// btnLabel
							null,						// btnIconRight
							"DS-text-lightblue",		// IconClass
							"DS-text-lightblue",		// TextClass
							"onclick=\"InfoRequest(" + ReqID + ", '" + Caller + "')\"",
							"Request more info to the user"
						) %>
					</td>

					<td class="DS-padding-4px"></td>

					<td class="CellPadding_0px" align="center">
						<%= MdcTool.Button.TextIconClass(
							"lock",						// btnIconLeft
							"&nbsp;Edit in WME",		// btnLabel
							null,						// btnIconRight
							"DS-text-" + (btnUnlEnabled ? "yellow" : "gray"),	// IconClass
							"DS-text-" + (btnUnlEnabled ? "yellow" : "gray"),	// TextClass
							(btnUnlEnabled ? "" : "disabled ") + "onclick=\"window.open('" + Permalink + "', '_blank'); window.location.href='../unlock/_actions.jsp?Action=actPutInWork&ReqID=" + ReqID + "&Caller=" + Caller + "'\"",
							"Jump to WME to process the request"
						) %>
					</td>

					<td class="DS-padding-4px"></td>

					<td class="CellPadding_0px" align="center">
						<%= MdcTool.Button.TextIconClass(
							"thumb_down",				// btnIconLeft
							"&nbsp;Reject",				// btnLabel
							null,						// btnIconRight
							"DS-text-" + (btnRejEnabled ? "lightred" : "gray"),	// IconClass
							"DS-text-" + (btnRejEnabled ? "lightred" : "gray"),	// TextClass
							(btnRejEnabled ? "" : "disabled ") + "onclick=\"RejectRequest(" + ReqID + ", '" + Caller + "')\"",
							"Close this Request as REJECTED and send message"
						) %>
					</td>

					<td class="DS-padding-4px"></td>

					<td class="CellPadding_0px" align="right">
						<%= MdcTool.Button.TextIconClass(
							"thumb_up",					// btnIconLeft
							"&nbsp;Solved",				// btnLabel
							null,						// btnIconRight
							"DS-text-" + (btnSolEnabled ? "lime" : "gray"),	// IconClass
							"DS-text-" + (btnSolEnabled ? "lime" : "gray"),	// TextClass
							(btnSolEnabled ? "" : "disabled ") + "onclick=\"SolveRequest(" + ReqID + ", '" + Caller + "')\"",
							"Close this Request as SOLVED and send message"
						) %>
					</td>
				</tr>
			</table>
		</div>
<%
	} catch (Exception e) {
%>
		<div class="DS-padding-8px DS-back-gray">
			<div class="DS-text-huge">An error was found processing your request</div>
		</div>
		<div class="DS-padding-8px DS-card-white  DS-border-updn">
			<div class="DS-text-exception"><%= e.toString() %></div>
		</div>
		<div class="DS-padding-8px DS-back-gray" align="center">
			<%= MdcTool.Dialog.BtnDismiss( 
				"btnDismiss",					// id
				"Close",						// label
				true,							// isDefault
				"",								// additionalData
				"",								// tooltip
				null,							// btnLeftIcon
				MdcTool.Button.Look.OUTLINED	// btnLook
			) %>
		</div>
<%
	}

	if (DB != null)
		DB.destroy();
%>
