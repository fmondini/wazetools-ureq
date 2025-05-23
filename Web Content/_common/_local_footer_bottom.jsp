<%@ page
	language="java"
	contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wazetools.*"
%>
<%
	//
	// DESKTOP
	//
%>
	<!-- [start] local_footer_bottom.jsp - desktop -->

	<div class="DS-layout-foot DS-card-body DS-desktop-only DS-border-up">

		<div class="mdc-layout-grid__inner">

			<div class="<%= MdcTool.Layout.Cell(1, 1, 1) %> DS-grid-middle-left">
				<a href="<%= AppCfg.getAppExitLink() %>">
					<img src="../images/waze.png" width="32" height="auto">
				</a>
			</div>

			<div class="<%= MdcTool.Layout.Cell(10, 6, 2) %>">
				<div class="DS-text-small DS-text-italic" align="center">This is an unofficial Waze-related site managed by
					<a href="https://www.waze.com/user/editor/fmondini" target="_blank">fmondini</a> --
					<a href="https://www.waze.com/" target="_blank">Waze</a>&trade; is a trademark of
					<a href="https://uspto.report/company/Google-L-L-C" target="_blank">GOOGLE LLC</a><br>
					Site hosted for free by <a href="https://danisoft.software/" target="_blank">Danisoft Srl</a>,
					Northern Italy - Vat no. IT02914410986 - Datacenter located in (<a href="../home/about.jsp">see here</a>)</div>
			<div class="DS-tablet-only DS-text-italic DS-text-small" align="center">Unofficial site by fmondini &amp; the Italian Waze Community<br>
				<a href="https://www.waze.com/" target="_blank">Waze</a>&trade; is a trademark of Waze Mobile Ltd</div>
			</div>

			<div class="<%= MdcTool.Layout.Cell(1, 1, 1) %> DS-grid-middle-right">
				<a href="https://twitter.com/WazeTools" target="_blank"><img border="0" style="opacity: 0.4; padding-right: 1px" src="../images/footer-twitter.png" width="24" height="24"></a>
				&nbsp;
				<a href="https://www.facebook.com/pages/WazeTools/716924971736308" target="_blank"><img border="0" style="opacity: 0.4;" src="../images/footer-facebook.png" width="24" height="24"></a>
			</div>

		</div>

		<div class="DS-padding-bottom-32px"></div>

	</div>
<%
	//
	// TABLET
	//
%>
	<!-- [start] local_footer_bottom.jsp - tablet -->

	<div class="DS-layout-foot DS-card-none DS-tablet-only DS-border-up">

		<div class="mdc-layout-grid__inner">

			<div class="<%= MdcTool.Layout.Cell(6, 4, 2) %> DS-grid-middle-left">
				<a href="<%= AppCfg.getAppExitLink() %>">
					<img src="../images/waze.png" width="32" height="auto">
				</a>
			</div>

			<div class="<%= MdcTool.Layout.Cell(6, 4, 2) %> DS-grid-middle-right">
				<a href="https://twitter.com/WazeTools" target="_blank"><img border="0" style="opacity: 0.4; padding-right: 1px" src="../images/footer-twitter.png" width="24" height="24"></a>
				&nbsp;
				<a href="https://www.facebook.com/pages/WazeTools/716924971736308" target="_blank"><img border="0" style="opacity: 0.4;" src="../images/footer-facebook.png" width="24" height="24"></a>
			</div>

		</div>

		<div class="DS-padding-bottom-16px"></div>

	</div>
<%
	//
	// PHONE
	//
%>
	<!-- [start] local_footer_bottom.jsp - phone -->

	<div class="DS-layout-foot DS-card-none DS-phone-only DS-border-up">

		<div class="mdc-layout-grid__inner">

			<div class="<%= MdcTool.Layout.Cell(6, 4, 2) %> DS-grid-middle-left DS-padding-lfrg-4px">
				<a href="<%= AppCfg.getAppExitLink() %>">
					<img src="../images/waze.png" width="32" height="auto">
				</a>
			</div>

			<div class="<%= MdcTool.Layout.Cell(6, 4, 2) %> DS-grid-middle-right DS-padding-lfrg-4px">
				<a href="https://twitter.com/WazeTools" target="_blank"><img border="0" style="opacity: 0.4; padding-right: 1px" src="../images/footer-twitter.png" width="24" height="24"></a>
				&nbsp;
				<a href="https://www.facebook.com/pages/WazeTools/716924971736308" target="_blank"><img border="0" style="opacity: 0.4;" src="../images/footer-facebook.png" width="24" height="24"></a>
			</div>

		</div>

		<div class="DS-padding-bottom-8px"></div>

	</div>

	<!-- [-end-] local_footer_bottom.jsp -->
