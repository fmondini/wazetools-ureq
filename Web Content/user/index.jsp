<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wtlib.auth.*"
	import="net.danisoft.wazetools.ureq.*"
%>
<%!
	private static final String PAGE_Title = "Check Requests History";
	private static final String PAGE_Keywords = "";
	private static final String PAGE_Description = "";

	/**
	 * Get QryPeriod from cookies 
	 */
	public QryPeriod getPeriod(HttpServletRequest request) {

		QryPeriod qryPeriod = QryPeriod.THIS_D;

		try {

			for (Cookie cookie : request.getCookies())
				if (cookie.getName().equals(QryPeriod.getCookieNamePeriod()))
					qryPeriod = QryPeriod.getEnum(cookie.getValue());

		} catch (Exception e) {	}

		return(qryPeriod);
	}
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

		function ViewNotes(ReqID) {

			$.ajax({

				async: true,
				cache: false,
				type: 'POST',
				dataType: 'text',
				url: '_dlg_notes.jsp',

				data: { ReqID: ReqID },

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

		function ExportData() {

			$.ajax({

				async: true,
				cache: false,
				type: 'POST',
				dataType: 'text',
				url: '_dlg_export.jsp',

				beforeSend: function(XMLHttpRequest) {
					ShowDialog_AJAX('<%= MdcTool.PleaseWait.CreateContentOnly("Requests History", "Exporting your requests history data, please wait...") %>');
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

		function PopulateList() {

			const dateMin = getCookie('<%= QryPeriod.getCookieNamePerMin() %>');
			const dateMax = getCookie('<%= QryPeriod.getCookieNamePerMax() %>');

			$.ajax({

				async: true,
				cache: false,
				type: 'GET',
				dataType: 'text',
				url: '../user/_div_details.jsp',

				data: {
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

	<div id="divAjaxWait" style="position: absolute; width: 100px; margin-left: -50px; height: 80px; margin-top: -40px; top: 50%; left: 50%; padding: 5px; background-color: white; box-shadow: 3px 3px 5px #888888; border-radius: 5px; -moz-border-radius: 5px;">
		<div class="DS-padding-top-8px" align="center">
			<div><img src="../images/ajax-loader.gif"></div>
			<div>Loading...</div>
		</div>
	</div>
<%
	String RedirectTo = "";

	Database DB = null;

	try {

		DB = new Database();
		User USR = new User(DB.getConnection());
		User.Data usrData = USR.Read(SysTool.getCurrentUser(request));

		// Save params to be used in _dlg_export.jsp
		session.setAttribute("export_user", usrData.getName());
		session.setAttribute("export_pass", usrData.getPass());
%>
		<div class="DS-padding-updn-8px">
			<div class="mdc-layout-grid__inner">
				<div class="<%= MdcTool.Layout.Cell(6, 6, 2) %> DS-grid-middle-left">
					<div class="DS-text-title-shadow">List of all requests for
						<span class="DS-text-blue"><%= usrData.getName() %></span>
						<span class="DS-text-gray">(<%= usrData.getRank() %>)</span>
					</div>
				</div>
				<div class="<%= MdcTool.Layout.Cell(4, 1, 1) %> DS-grid-middle-center">
					<div align="center">
						<div class="DS-back-FloralWhite DS-padding-updn-4px DS-padding-lfrg-8px DS-border-full DS-border-round">
							<div class="DS-text-DarkGreen DS-text-italic">Data extraction period</div>
							<div id="divDisplayPeriod" class="DS-text-black"></div>
						</div>
					</div>
				</div>
				<div class="<%= MdcTool.Layout.Cell(2, 1, 1) %> DS-grid-middle-right DS-text-big">
					<%= MdcTool.Select.Box(
						"selPeriod",
						MdcTool.Select.Width.NORM,
						"Select period",
						QryPeriod.getCombo(getPeriod(request).toString()),
						"onChange=\"setQryPeriod();\""
					) %>
				</div>
			</div>
		</div>

		<div class="DS-padding-updn-8px">
			<div id="divDetails"></div>
		</div>

		<div class="DS-padding-updn-8px">
			<div class="mdc-layout-grid__inner">
				<div class="<%= MdcTool.Layout.Cell(6, 4, 2) %> DS-grid-middle-left">
					<%= MdcTool.Button.BackTextIcon("Back", "../home/") %>
				</div>
				<div class="<%= MdcTool.Layout.Cell(6, 4, 2) %> DS-grid-middle-right">
					<%= MdcTool.Button.TextIconOutlinedClass(
						"download",
						"&nbsp;Download Data",
						null,
						"DS-text-green",
						"DS-text-green",
						"onClick=\"ExportData();\"",
						"Export your requests history in Excel&trade; format"
					) %>
				</div>
			</div>
		</div>
<%
	} catch (Exception e) {
%>
		<div class="DS-padding-16px DS-text-exception DS-back-pastel-red DS-border-full" align="center">
			<div class="DS-text-huge DS-text-bold">Internal Error</div>
			<div class="DS-text-large"><%= e.toString() %></div>
		</div>
<%
	}

	if (DB != null)
		DB.destroy();
%>
	</div>
	</div>
	</div>

	<jsp:include page="../_common/footer.jsp">
		<jsp:param name="RedirectTo" value="<%= RedirectTo %>"/>
	</jsp:include>

</body>
</html>
