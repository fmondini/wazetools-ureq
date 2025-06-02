<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wtlib.auth.*"
	import="net.danisoft.wazetools.*"
%>
<%!
	private static final String PAGE_Title = AppCfg.getAppName() + " Home Map";
	private static final String PAGE_Keywords = "Waze.Tools UREQ, Waze, Tools, Unlock, Request";
	private static final String PAGE_Description = AppCfg.getAppAbstract();

	private static final String DRAW_USRAREA_SCRIPT = "DrawUsrAreaScript";
	private static final String DRAW_MARKERS_SCRIPT = "DrawMarkersScript";
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

		var MapObj = null;
		var MapOptions = null;

		const cookieNameZoom = 'UreqMgrMap-Zoom';
		const cookieNameCLat = 'UreqMgrMap-CLat';
		const cookieNameCLng = 'UreqMgrMap-CLng';

		/**
		 * Retrieve Request details
		 */
		function getDetails(ReqID) {

			ShowRequestDetails(ReqID, 'MAP'); // NOTE: ShowRequestDetails is in ../_common/request_actions.js
		}

		/**
		 * Automagically open a predefined request
		 */
		function PreOpenRequest(ReqID) {

			if (ReqID > 0)
				ShowRequestDetails(ReqID, 'MAP'); // NOTE: ShowRequestDetails is in ../_common/request_actions.js
		}

		/**
		 * Store current map settings
		 */
		function SaveMapPosition() {

			setCookie(cookieNameZoom, MapObj.getZoom());
			setCookie(cookieNameCLat, MapObj.getCenter().lat());
			setCookie(cookieNameCLng, MapObj.getCenter().lng());
		}

		/**
		 * Config & Options Dialog
		 */
		function ShowConfigDialog() {

			$.ajax({

				cache: false,
				type: 'POST',
				dataType: 'text',
				url: '_dlg_config.jsp',

				beforeSend: function() {
					$('#divAjaxWait').show();
				},
				success: function(data) {
					ShowDialog_AJAX(data);
				},
				error: function(jqXHR, textStatus, errorThrown) {
					ShowDialog_AJAX(jqXHR.responseText);
				},
				complete: function(jqXHR, textStatus) {
					$('#divAjaxWait').hide();
				}
			});
		}

		/**
		 * Create and Launch User Area Paths and Requests Markers Scripts
		 */
		function CreateAndRunDataScripts(mapObjName) {

			// User Area Paths

			$.ajax({

				cache: false,
				type: 'POST',
				dataType: 'text',
				url: '../servlet/UserAreaPaths',

				data: {
					mapObjName: mapObjName,
					user: '<%= SysTool.getCurrentUser(request) %>'
				},

				beforeSend: function() {
					$('#divAjaxWait').show();
				},

				success: function(data) {

					var UserAreaPathsScript = document.createElement('script');
					UserAreaPathsScript.id = '<%= DRAW_USRAREA_SCRIPT %>';
					UserAreaPathsScript.type = 'text/javascript';
					UserAreaPathsScript.text = 'function <%= DRAW_USRAREA_SCRIPT %>(){' + data + '}';
					$('head').append(UserAreaPathsScript);

					<%= DRAW_USRAREA_SCRIPT %>();

					// Requests Markers

					$.ajax({

						cache: false,
						type: 'POST',
						dataType: 'text',
						url: '../servlet/AdvMarkersScript',

						data: {
							mapObjName: mapObjName,
							jCfg: getCookie('UreqMgrCfg')
						},

						success: function(data) {

							var MarkersScript = document.createElement('script');
							MarkersScript.id = '<%= DRAW_MARKERS_SCRIPT %>';
							MarkersScript.type = 'text/javascript';
							MarkersScript.text = 'function <%= DRAW_MARKERS_SCRIPT %>(){' + data + '}';
							$('head').append(MarkersScript);

							<%= DRAW_MARKERS_SCRIPT %>();
						},

						error: function(jqXHR, textStatus, errorThrown) {
							console.error('[/unlock/index.jsp] AdvMarkersScript Error: jqXHR: %o', jqXHR);
							console.error('[/unlock/index.jsp] AdvMarkersScript Error: textStatus: %o', textStatus);
							console.error('[/unlock/index.jsp] AdvMarkersScript Error: errorThrown: %o', errorThrown);
						},

						complete: function(jqXHR, textStatus) {
							$('#divAjaxWait').hide();
						}
					});
				},

				error: function(jqXHR, textStatus, errorThrown) {
					console.error('[/unlock/index.jsp] UserAreaPaths Error: jqXHR: %o', jqXHR);
					console.error('[/unlock/index.jsp] UserAreaPaths Error: textStatus: %o', textStatus);
					console.error('[/unlock/index.jsp] UserAreaPaths Error: errorThrown: %o', errorThrown);
				}
			});
		}

	</script>

	<% if (SysTool.isWindog()) { %>
	<script>
		/**
		 * TEST WebSocket
		 */
		function TestWs(userId, reqId) {

			$.ajax({
				cache: false,
				type: 'GET',
				url: '__TEST_WebSocket.jsp',
				data: {
					UserID: userId,
					ReqID: reqId // 18042
				}
			});
		}
	</script>
	<% } %>


</head>

<body>

	<jsp:include page="../_common/header.jsp">
		<jsp:param name="Force-Hide" value="Y"/>
	</jsp:include>

	<div id="<%= AppCfg.getMapContainerID() %>" style="position: absolute; top: 0px; left: 0px; width: 100%; height: 100%; z-index: -1;"></div>

	<div id="divAjaxWait" style="position: absolute; width: 100px; margin-left: -50px; height: 80px; margin-top: -40px; top: 50%; left: 50%; padding: 5px; background-color: white; box-shadow: 3px 3px 5px #888888; border-radius: 5px; -moz-border-radius: 5px;">
		<div class="DS-padding-top-8px" align="center">
			<div><img src="../images/ajax-loader.gif"></div>
			<div>Loading...</div>
		</div>
	</div>
<%
	out.flush();

	Database DB = null;

	int PreOpenedReqID = EnvTool.getInt(request, "ReqID", 0);
	int setZoom = EnvTool.getInt(request, "setZoom", 0);
	String setLat = EnvTool.getStr(request, "setLat", "");
	String setLon = EnvTool.getStr(request, "setLon", "");

	try {

		DB = new Database();
		User USR = new User(DB.getConnection());
		User.Data usrData = USR.Read(SysTool.getCurrentUser(request));
%>
		<% if (usrData.getWazerContacts().isEmpty()) { %>
			<div style="top: 60px; left: 15px; position: absolute;">
				<div class="DS-back-pastel-red DS-padding-12px DS-border-full DS-border-round" align="center">
					<div class="DS-text-big DS-text-exception DS-text-bold">WARNING: There is no SlackID in your profile</div>
					<div class="DS-text-exception">Your SlackID is required to send and receive messages</div>
					<div class="DS-text-exception"><a href="<%= AppCfg.getAuthServerHomeUrl() %>/user/profile.jsp" target="_blank">Please log in to AUTH and set your SlackID by clicking here</a></div>
				</div>
			</div>
		<% } %>

		<div style="top: 60px; right: 15px; position: absolute;">
			<% if (SysTool.isWindog()) { %>
				<%= MdcTool.Button.TextIconOutlinedClass(
					"quiz",
					"&nbsp;WebSocket Test",
					null,
					"DS-text-Crimson",
					"DS-text-FireBrick",
					"onClick=\"TestWs('" + SysTool.getCurrentUser(request) + "', 18042);\"",
					"TEST WebSocket"
				) %>
			<% } %>
			<%= MdcTool.Button.Icon(
				"assignment_ind",
				"onClick=\"window.location.href='../user/';\"",
				"Jump to Your Request List"
			) %>
			<%= MdcTool.Button.Icon(
				"assignment",
				"onClick=\"window.location.href='../tools/lastreq.jsp';\"",
				"Jump to Last Request List"
			) %>
			<%= MdcTool.Button.Icon(
				"history",
				"onClick=\"window.location.href='../tools/recheck.jsp';\"",
				"Jump to Recheck Queue List"
			) %>
			<%= MdcTool.Button.Icon(
				"settings",
				"onClick=\"ShowConfigDialog();\"",
				"Configuration &amp; Options"
			) %>
		</div>
<%
		////////////////////////////////////////////////////////////////////////////////////////////////////
		//
		// SCRIPTS
		//
%>
		<script>

		/**
		 * Map Initialization
		 */
		function initMap() {

			// Getting Lat/Lon/Zoom from cookies or from url

			var mapDefaultZoom = parseInt('0' + getCookie(cookieNameZoom));
			var mapDefaultCLat = parseFloat('0' + getCookie(cookieNameCLat));
			var mapDefaultCLng = parseFloat('0' + getCookie(cookieNameCLng));

			<% if (!setLat.equals("") && !setLon.equals("")) { %>
				// Force Lat/Lon if requested
				mapDefaultCLat = parseFloat('<%= setLat %>');
				mapDefaultCLng = parseFloat('<%= setLon %>');
			<% } %>

			mapDefaultZoom = (mapDefaultZoom == 0 ? <%= AppCfg.getMapDefaultZoom() %> : mapDefaultZoom);
			mapDefaultCLat = (mapDefaultCLat == 0 ? <%= AppCfg.getMapDefaultCenterLAT() %> : mapDefaultCLat);
			mapDefaultCLng = (mapDefaultCLng == 0 ? <%= AppCfg.getMapDefaultCenterLNG() %> : mapDefaultCLng);

			<% if (setZoom > 0) { %>
				mapDefaultZoom = <%= setZoom %>
			<% } %>

			// Setting Map Options

			const MapOptions = {
				mapId: '21c0ef9413a8d208',
				mapTypeId: google.maps.MapTypeId.ROADMAP,
				zoom: mapDefaultZoom,
				fullscreenControl: false,
				scaleControl: false,
				mapTypeControl: false,
				streetViewControl: false,
				panControl: true,
				center: new google.maps.LatLng(mapDefaultCLat, mapDefaultCLng)
			};

			// Creating map object

			MapObj = new google.maps.Map(
				document.getElementById('<%= AppCfg.getMapContainerID() %>'),
				MapOptions
			);

			// Adding a listener to store last map settings

			google.maps.event.addListener(MapObj, 'tilesloaded', function() {
				google.maps.event.addListener(MapObj, 'zoom_changed', SaveMapPosition);
				google.maps.event.addListener(MapObj, 'dragend', SaveMapPosition);
			});

			// Finalize & Draw

			CreateAndRunDataScripts('MapObj'); // Map Object name to use (string)
			PreOpenRequest(<%= PreOpenedReqID %>);
		}

		</script>

		<script <%= "async" %> src="https://maps.googleapis.com/maps/api/js
			?key=<%= AppCfg.getMapActivationKey() %>
			&v=weekly
			&loading=async
			&libraries=marker
			&callback=initMap
		"></script>

		<jsp:include page="../_common/footer.jsp">
			<jsp:param name="RedirectTo" value=""/>
			<jsp:param name="Force-Hide" value="Y"/>
		</jsp:include>
<%
	} catch (Exception e) {
%>
		<div class="mdc-layout-grid">
			<div class="mdc-layout-grid__inner">
				<div style="box-shadow: 3px 3px 5px #888888; border-radius: 5px; top: 150px; left: 100px; right: 100px; background-color: white; -moz-border-radius: 5px; position: absolute; padding: 10px">
					<div class="DS-card-head">
						<div class="DS-text-subtitle DS-text-exception"><b>INTERNAL ERROR</b></div>
					</div>
					<div class="DS-card-body">
						<div class="DS-text-exception"><%= e.toString() %></div>
					</div>
					<div class="DS-card-foot">
						<div class="DS-text-paragraph DS-text-exception">Please report to <a href="mailto:dev@waze.tools">dev@waze.tools</a></div>
					</div>
				</div>
			</div>
		</div>
<%
	}

	if (DB != null)
		DB.destroy();
%>
	<script defer src="../_common/WebSocketMgr.js" data-server="<%= AppCfg.getServerEndpointURI(SysTool.getCurrentUser(request)).toString() %>"></script>

</body>
</html>
