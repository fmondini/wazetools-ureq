<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wazetools.*"
	import="net.danisoft.wazetools.ureq.*"
%>
<%!
	private static final String PAGE_Title = "Manually Add a New Unlock Request (by Permalink)";
	private static final String PAGE_Keywords = "Waze.Tools UREQ, Waze, Tools, Unlock, Request, Permalink";
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

	<script>

		function UpdatePlinkValues() {

			if ($('#reqPlink').val() != '') {

				try {

					var divContent = '';
					var qStr = $('#reqPlink').val().split('?')[1].split('&');

					for (var i=0; i<qStr.length; i++) {

				        var Pair = qStr[i].split('=');

						if (Pair[0] == 'env')		{	$('#hidEnv').val(Pair[1]);	} else
						if (Pair[0] == 'lat')		{	$('#hidLat').val(Pair[1]);	} else
						if (Pair[0] == 'lon')		{	$('#hidLon').val(Pair[1]);	} else
						if (Pair[0] == 'zoomLevel')	{	$('#hidZoom').val(Pair[1]);	} else
						if (Pair[0] == 'segments')	{	$('#hidSegs').val(Pair[1]);	} else
						if (Pair[0] == 'venues')	{	$('#hidVens').val(Pair[1]);	}

						divContent += (
							(divContent.length > 0 ? '&nbsp;&nbsp;&mdash;&nbsp;&nbsp;' : '') +
							Pair[0] + ':"' +
							'<b>' + (Pair[0] == 'segments'
								? (Pair[1].split(',').length + ' segments selected')
								: Pair[1]
							) + '</b>"'
						);
					}

					$('#plinkDetails').html('<div class="DS-text-compact DS-text-italic DS-text-gray" align="center">Decoding...&nbsp;&nbsp;' + divContent + '</div>');

				} catch (err) {
					$('#plinkDetails').html('<div class="DS-text-exception"><b>UpdatePlinkValues()</b>: ' + err + '</div>');
				}

				$('#plinkDetails').show();

			} else
				$('#plinkDetails').hide();
		}

		function CheckFormData() {

			if (
				document.forms['frmAddReq']['reqEnv'].value == ''	||
				document.forms['frmAddReq']['reqUser'].value == ''	||
				document.forms['frmAddReq']['reqRank'].value == ''	||
				document.forms['frmAddReq']['reqPlink'].value == ''	||
				document.forms['frmAddReq']['reqNotes'].value == ''
			) {
				ShowDialog_OK(
					'Incomplete field(s) found',
					'ALL fields must be filled out, but I\'ve found some incomplete field(s) in this request.',
					'Well, now I\'ll fix it'
				);
				return false;
			}
		}

	</script>

</head>

<body>

	<jsp:include page="../_common/header.jsp" />

	<div class="mdc-layout-grid DS-layout-body">
	<div class="mdc-layout-grid__inner">
<%
	String RedirectTo = "";

	MsgTool MSG = new MsgTool(session);

	String Action = EnvTool.getStr(request, "Action", "");

	if (Action.equals("")) {

		////////////////////////////////////////////////////////////////////////////////////////////////////
		//
		// GET DATA
		//
%>
		<div class="<%= MdcTool.Layout.Cell(12, 8, 4) %>">

		<div class="DS-card-body">
			<div class="DS-text-title-shadow"><%= PAGE_Title %></div>
		</div>

		<form id="frmAddReq" name="frmAddReq" onsubmit="return CheckFormData()">

			<input type="hidden" name="Action" value="create">

			<div class="mdc-layout-grid__inner">

				<!-- Line #1 -->

				<div class="<%= MdcTool.Layout.Cell(3, 4, 4) %>">
					<div class="DS-card-head">
						<%= MdcTool.Select.Box("reqCtry", MdcTool.Select.Width.FULL, "Country",
							"<option value=\"IT\" selected>Italy</option>",
							null
						) %>
					</div>
				</div>

				<div class="<%= MdcTool.Layout.Cell(3, 4, 4) %>">
					<div class="DS-card-head">
						<%= MdcTool.Text.Box("reqUser", "", MdcTool.Text.Type.TEXT, MdcTool.Text.Width.FULL, "Your Waze Username", "maxlength=\"32\"") %>
					</div>
				</div>

				<div class="<%= MdcTool.Layout.Cell(3, 4, 4) %>">
					<div class="DS-card-head">
						<%= MdcTool.Select.Box("reqRank", MdcTool.Select.Width.FULL, "Your Editor Rank",
							"<option selected value=\"\"></option>" +
							"<option value=\"1\">Level 1 (1 cone)</option>" +
							"<option value=\"2\">Level 2 (2 cones)</option>" +
							"<option value=\"3\">Level 3 (3 cones)</option>" +
							"<option value=\"4\">Level 4 (4 cones)</option>" +
							"<option value=\"5\">Level 5 (5 cones)</option>",
							null
						) %>
					</div>
				</div>

				<div class="<%= MdcTool.Layout.Cell(3, 4, 4) %>">
					<div class="DS-card-head">
						<%= MdcTool.Select.Box("reqLock", MdcTool.Select.Width.FULL, "Max Segm Lock",
							"<option selected value=\"\"></option>" +
							"<option value=\"2\">Level 2 (2 cones)</option>" +
							"<option value=\"3\">Level 3 (3 cones)</option>" +
							"<option value=\"4\">Level 4 (4 cones)</option>" +
							"<option value=\"5\">Level 5 (5 cones)</option>" +
							"<option value=\"6\">Level 6 (6 cones)</option>" +
							"<option value=\"7\">Level 7 (Staff)</option>",
							null
						) %>
					</div>
				</div>

				<!-- Line #2 -->

				<div class="<%= MdcTool.Layout.Cell(12, 8, 4) %>">
					<div class="DS-card-none">
						<div class="DS-text-huge">Permalink with the reference to the object(s) to be unlocked</div>
					</div>
					<div class="DS-card-body">
						<%= MdcTool.Text.Box("reqPlink", "", MdcTool.Text.Type.TEXT, MdcTool.Text.Width.FULL, "Paste here the permalink (with segments, places or cameras selected)", "onKeyUp=\"UpdatePlinkValues();\" onBlur=\"UpdatePlinkValues();\"") %>
						<div id="plinkDetails" class="DS-padding-8px" style="display: none">To be filled at runtime</div>
						<input type="hidden" id="hidEnv" name="reqEnv" value="">
						<input type="hidden" id="hidLat" name="reqLat" value="">
						<input type="hidden" id="hidLon" name="reqLon" value="">
						<input type="hidden" id="hidZoom" name="reqZoom" value="">
						<input type="hidden" id="hidSegs" name="reqSegs" value="">
						<input type="hidden" id="hidVens" name="reqVens" value="">
					</div>
				</div>

				<!-- Line #3 -->

				<div class="<%= MdcTool.Layout.Cell(12, 8, 4) %>">
					<div class="mdc-layout-grid__inner">
						<div class="<%= MdcTool.Layout.Cell(5, 3, 4) %>">
							<div class="DS-card-none">
								<div class="DS-text-huge">Desired unlocking method</div>
							</div>
							<div class="DS-card-none">
								<%= MdcTool.Radio.Box("reqSolve", "reqSolve_Y", "Please resolve this issue for me", "Y", MdcTool.Radio.Status.CHECKED) %>
								<br>
								<%= MdcTool.Radio.Box("reqSolve", "reqSolve_N", "Please unlock only, I want to solve this issue myself", "N", null) %>
							</div>
						</div>
						<div class="<%= MdcTool.Layout.Cell(7, 5, 4) %>">
							<div class="DS-card-none">
								<div class="DS-text-huge">Reason for the request and/or comments for the unlocker</div>
							</div>
							<div class="DS-card-foot">
								<%= MdcTool.Text.Area("reqNotes", "", "Reason / Explain here why you're requesting an unlock", 5, 7, null, false, null) %>
							</div>								
						</div>
					</div>
				</div>

			</div> <!-- /mdc-layout-grid__inner -->

			<div class="DS-card-foot">
				<div class="mdc-layout-grid__inner">
					<div class="<%= MdcTool.Layout.Cell(2, 1, 1) %> DS-grid-middle-left">
						<%= MdcTool.Button.BackTextIcon("Cancel", "../home/") %>
					</div>
					<div class="<%= MdcTool.Layout.Cell(8, 4, 2) %> DS-grid-middle-center">
						<div class="DS-text-exception DS-text-italic" align="center">
							<b>This manual unlock method is enabled for compatibility only and will likely be removed in the future.</b><br>
							To insert a new unlock request it is best to use the appropriate script downloadable
							<a href="https://code.waze.tools/home/browse.jsp?ShowSID=d323e155-f555-4006-944f-f8757fa59bb2">from here</a>.
						</div>
					</div>
					<div class="<%= MdcTool.Layout.Cell(2, 1, 1) %> DS-grid-middle-right">
						<%= MdcTool.Button.SubmitTextIconClass(
							"add_circle",
							"&nbsp;Add Request",
							null,
							"DS-text-lime",
							null,
							"Add Request"
						) %>
					</div>
				</div>
			</div>

		</form>

		</div>

		<script>
			try {
				$('#reqUser').focus();
				$('#reqUser').select();
			} catch (e) { }
		</script>
<%
	} else if (Action.equals("create")) {

		////////////////////////////////////////////////////////////////////////////////////////////////////
		//
		// Create the request
		//
%>
		<div id="AjaxLoaderDIV" class="DS-ajax-loader-div">
			<div align="center"><img src="../images/ajax-loader.gif"></div>
			<div align="center">Creating...</div>
		</div>
<%
		out.flush();

		Database DB = null;

		try {

			DB = new Database();
			Request REQ = new Request(DB.getConnection());
			Request.Data reqData = REQ.new Data();

			reqData.setJsVersion("WebApp " + AppCfg.getAppVersion());
			reqData.setEnvironment(EnvTool.getStr(request, "reqEnv", ""));
			reqData.setCountry(EnvTool.getStr(request, "reqCtry", ""));
			reqData.setUser(EnvTool.getStr(request, "reqUser", ""));
			reqData.setUserRank(EnvTool.getInt(request, "reqRank", 0));
			reqData.setLock(EnvTool.getInt(request, "reqLock", 0));
			reqData.setLat(EnvTool.getDbl(request, "reqLat", 0.0D));
			reqData.setLon(EnvTool.getDbl(request, "reqLon", 0.0D));
			reqData.setZoom(EnvTool.getInt(request, "reqZoom", 0));
			reqData.setMotivation(EnvTool.getStr(request, "reqNotes", ""));
			reqData.setSegments(EnvTool.getStr(request, "reqSegs", ""));
			reqData.setVenues(EnvTool.getStr(request, "reqVens", ""));
			reqData.setResolve(EnvTool.getStr(request, "reqSolve", "").equals("Y"));
			reqData.setStatus(RequestStatus.OPEN);

			int LastID = REQ.Insert(reqData);

			reqData = REQ.Read(LastID); // Refresh Request (The location may have been changed by iBot)
			REQ.SendMailAlert(request, reqData.getID());
			REQ.SlackSendToChannel(reqData.getID());

			MSG.setInfoText("Request sent", "Your request has been sent");

		} catch (Exception e) {
			MSG.setAlertText("Internal Error", e.toString());
		}

		if (DB != null)
			DB.destroy();

		RedirectTo = "../home/";

	} else {

		////////////////////////////////////////////////////////////////////////////////////////////////////
		//
		// BAD ACTION
		//

		MSG.setAlertText("Internal Error", "Missing Action: '" + Action + "'");
		RedirectTo = "../home/";
	}
%>
	</div> <!-- /mdc-layout-grid__inner -->
	</div> <!-- /mdc-layout-grid -->

	<jsp:include page="../_common/footer.jsp">
		<jsp:param name="RedirectTo" value="<%= RedirectTo %>"/>
	</jsp:include>

</body>
</html>
