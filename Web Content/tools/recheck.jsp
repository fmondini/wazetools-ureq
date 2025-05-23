<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wazetools.*"
%>
<%!
	private static final String PAGE_Title = "Recheck Queue";
	private static final String PAGE_Keywords = "Waze.Tools UREQ, Waze, Tools, Unlock, Recheck, Queue";
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

			$.ajax({

				async: true,
				cache: false,
				type: 'GET',
				dataType: 'text',
				url: '../tools/_div_details.jsp',

				data: {
					Type: 'RCHK'
				},

				beforeSend: function(XMLHttpRequest) {
					$('#divDetails').html('');
					$('#divAjaxWait').show();
				},

				success: function(data) {
					$('#divDetails').html(data);
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
		 * Startup Scripts
		 */
		$(document).ready(function() {
			PopulateList();
		});

	</script>

</head>

<body class="DS-topbar-space">

	<jsp:include page="../_common/header.jsp" />

	<div class="mdc-layout-grid DS-layout-body">
	<div class="mdc-layout-grid__inner">

		<div class="<%= MdcTool.Layout.Cell(12, 8, 4) %>">

			<div class="DS-padding-updn-8px">
				<div class="mdc-layout-grid__inner">
					<div class="<%= MdcTool.Layout.Cell(6, 4, 2) %> DS-grid-middle-left">
						<div class="DS-text-title-shadow"><%= PAGE_Title %></div>
					</div>
					<div class="<%= MdcTool.Layout.Cell(6, 4, 1) %> DS-grid-middle-right">
						<div>
							<%= MdcTool.Button.IconOutlined(
								"settings",
								"onClick=\"ShowConfigDlg('RCHK');\"",
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
