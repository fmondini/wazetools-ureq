<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wazetools.*"
	import="net.danisoft.wazetools.ureq.*"
%>
<%!
	private static final String PAGE_Title = "Last Requests Received";
	private static final String PAGE_Keywords = "Waze.Tools UREQ, Waze, Tools, Unlock, Last Requests";
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

	<script src="../_common/request_actions.js"></script>

	<script>

		/**
		 * Populate LastReq DIV
		 */
		function PopulateList() {

			const dateMin = getCookie('<%= QryPeriod.getCookieNamePerMin() %>');
			const dateMax = getCookie('<%= QryPeriod.getCookieNamePerMax() %>');

			if (dateMin == '' || dateMax == '') {
				setCookie('<%= QryPeriod.getCookieNamePeriod() %>', '<%= QryPeriod.THIS_M.toString() %>');
				setCookie('<%= QryPeriod.getCookieNamePerMin() %>', '<%= FmtTool.fmtDateTimeSqlStyle(QryPeriod.THIS_M.getDateMin()) %>');
				setCookie('<%= QryPeriod.getCookieNamePerMax() %>', '<%= FmtTool.fmtDateTimeSqlStyle(QryPeriod.THIS_M.getDateMax()) %>');
				console.log('Reloading the page to apply the default period...');
				window.location.reload();
			}

			$.ajax({

				async: true,
				cache: false,
				type: 'GET',
				dataType: 'text',
				url: '../tools/_div_details.jsp',

				data: {
					Type: 'LAST',
					DateMin: dateMin,
					DateMax: dateMax
				},

				beforeSend: function(XMLHttpRequest) {
					$('#divDetails').html('');
					$('#divDisplayPeriod').html('');
					$('#divAjaxWait').show();
				},

				success: function(data) {
					$('#divDetails').html(data);
					$('#divDisplayPeriod').html('<b>' + dateMin.split(' ')[0] + '</b> &#129094; <b>' + dateMax.split(' ')[0] + '</b>');
				},

				error: function(jqXHR, textStatus, errorThrown) {
					ShowDialog_OK('An error has occurred', jqXHR.responseText, 'OK');
					console.error('jqXHR: %o', jqXHR);
					console.error('textStatus: %o', textStatus);
					console.error('errorThrown: %o', errorThrown);
				},

				complete: function(jqXHR, textStatus) {
					$('#divAjaxWait').hide();
				}
			});
		}

		/**
		 * Set Query Period
		 */
		function setQryPeriod() {

			const perArray = $('#selPeriod').val().split('|');

			setCookie('<%= QryPeriod.getCookieNamePeriod() %>', perArray[0]);
			setCookie('<%= QryPeriod.getCookieNamePerMin() %>', perArray[1]);
			setCookie('<%= QryPeriod.getCookieNamePerMax() %>', perArray[2]);

			PopulateList();
		}

		/**
		 * Startup Scripts
		 */
		$(document).ready(function() {
			setQryPeriod();
			PopulateList();
		});

	</script>

</head>

<body>

	<jsp:include page="../_common/header.jsp" />

	<div class="mdc-layout-grid DS-layout-body">
	<div class="mdc-layout-grid__inner">
	<div class="<%= MdcTool.Layout.Cell(12, 8, 4) %>">

		<div class="DS-padding-updn-8px">
			<div class="mdc-layout-grid__inner">
				<div class="<%= MdcTool.Layout.Cell(4, 3, 2) %> DS-grid-middle-left">
					<div class="DS-text-title-shadow"><%= PAGE_Title %></div>
				</div>
				<div class="<%= MdcTool.Layout.Cell(4, 2, 1) %> DS-grid-middle-center">
					<div align="center">
						<div class="DS-back-FloralWhite DS-padding-updn-4px DS-padding-lfrg-8px DS-border-full DS-border-round">
							<div class="DS-text-DarkGreen DS-text-italic">Data extraction period</div>
							<div id="divDisplayPeriod" class="DS-text-black"></div>
						</div>
					</div>
				</div>
				<div class="<%= MdcTool.Layout.Cell(4, 3, 1) %> DS-grid-middle-right DS-text-large">
					<div>
						<%= MdcTool.Select.Box(
							"selPeriod",
							MdcTool.Select.Width.NORM,
							"Select period",
							QryPeriod.getCombo(request, QryPeriod.getCookieNamePeriod()),
							"onChange=\"setQryPeriod();\""
						) %>
					</div>
					<div class="DS-padding-lfrg-8px"></div>
					<div>
						<%= MdcTool.Button.IconOutlined(
							"settings",
							"onClick=\"ShowConfigDlg('LAST');\"",
							"Settings"
						) %>
					</div>
				</div>
			</div>
		</div>

		<div id="divAjaxWait" style="position: absolute; width: 100px; margin-left: -50px; height: 80px; margin-top: -40px; top: 50%; left: 50%; padding: 5px; background-color: white; box-shadow: 3px 3px 5px #888888; border-radius: 5px; -moz-border-radius: 5px;">
			<div class="DS-padding-top-8px" align="center">
				<div><img src="../images/ajax-loader.gif"></div>
				<div>Loading...</div>
			</div>
		</div>

		<div class="DS-padding-updn-8px">
			<div id="divDetails"></div>
		</div>

		<div class="DS-card-foot">
			<%= MdcTool.Button.BackTextIcon("Back", "../home/") %>
		</div>

	</div>
	</div>
	</div>

	<jsp:include page="../_common/footer.jsp" />

	<script>
		$('#divAjaxWait').hide();
	</script>

	<script defer src="../_common/WebSocketMgr.js" data-server="<%= AppCfg.getServerEndpointURI(SysTool.getCurrentUser(request)).toString() %>"></script>

</body>
</html>
