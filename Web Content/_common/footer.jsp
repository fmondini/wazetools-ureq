<%@ page
	language="java"
	contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"
	import="java.util.*"
	import="net.danisoft.dslib.*"
%>
	<!--
		[FOOTER] START
	-->
<%
	//
	// Check parameters
	//

	boolean Force_Show = false;
	boolean Force_Hide = false;
	String RedirectTo = "";
	String parName;
	Enumeration<?> parEnum = request.getParameterNames();

	while (parEnum.hasMoreElements()) {

		parName = (String) parEnum.nextElement();

		if (parName.equals("RedirectTo"))	RedirectTo = EnvTool.getStr(request, "RedirectTo", "");
		if (parName.equals("Force-Show"))	Force_Show = EnvTool.getStr(request, "Force-Show", "").equals("Y");
		if (parName.equals("Force-Hide"))	Force_Hide = EnvTool.getStr(request, "Force-Hide", "").equals("Y");

		if (parName.equals("Force-Foot"))
			System.err.println(request.getRequestURI() + " - Param [" + parName + "] in footer.jsp is deprecated, use the new JSON Config or Force-Show or Force-Hide");

		if (parName.equals("HideFooter") ||
			parName.equals("ExtraSpace") ||
			parName.equals("HideBorder")
		)
			System.err.println(request.getRequestURI() + " - Param [" + parName + "] in footer.jsp is deprecated, use the new JSON Config");
	}

	//
	// Start footer
	//

	if (RedirectTo.equals("")) {

		//
		// JSON CONFIG INIT
		//

		SiteCfg SCFG = new SiteCfg();

		String SpaceUpClass = SCFG.getFooter().getSpaceUpClass();
		String SpaceDnClass = SCFG.getFooter().getSpaceDnClass();
		String GoogleAnalyticsID = SCFG.getGoogleAnalytics().getId();

		boolean isEnabled = SCFG.getFooter().isEnabled();

		if (Force_Show)
			isEnabled = true;

		if (Force_Hide)
			isEnabled = false;

		//
		// DRAW FOOTER
		//

		if (isEnabled) {
%>
			<div class="<%= SpaceUpClass %> <%= SpaceDnClass %>">
				<jsp:include page="../_common/_local_footer_bottom.jsp"></jsp:include>
			</div>
<%
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////
		//
		// MDC INIT
		//
		////////////////////////////////////////////////////////////////////////////////////////////////////

		//
		// TOPAPP BAR AND DRAWER
		//
%>
		<!-- Init TopAppBar and Drawer -->

		<script id="_footer_jsp_topbar">
		<% if (SCFG.getTopAppBar().isEnabled()) { %>
			var __mdc_topappbar = mdc.topAppBar.MDCTopAppBar.attachTo(document.getElementById('__HeaderTopAppBar'));
			var __mdc_drawer = mdc.drawer.MDCDrawer.attachTo(document.getElementById('__HeaderDrawer'));
			__mdc_topappbar.setScrollTarget(document.getElementById('__HeaderDrawerContent'));
			__mdc_topappbar.listen('MDCTopAppBar:nav', () => {
				__mdc_drawer.open = !__mdc_drawer.open;
			});
		<% } else { %>
			// No TopAppBar or Drawer
		<% } %>
		</script>
<%
		//
		// TEXTBOX / TEXTAREA / SELECT
		//
%>
		<!-- Init TextBox, TextArea and Select -->

		<script id="_footer_jsp_textarea">
			var _footer_textbox_all = document.querySelectorAll('.mdc-text-field');
			for (_footer_i = 0; _footer_i < _footer_textbox_all.length; _footer_i++)
				var textField = new mdc.textField.MDCTextField(_footer_textbox_all[_footer_i]);
		</script>
<%
		//
		// TAB BAR
		//
%>
		<!-- Init TabBar -->

		<script id="_footer_jsp_tabbar">
			var _footer_tabbar_all = document.querySelectorAll('.mdc-tab-bar');
			var _footer_tabbar_all_content = document.querySelectorAll('.DS-tab-panel');
			for (_footer_i = 0; _footer_i < _footer_tabbar_all.length; _footer_i++) {
				var footerTabBar = new mdc.tabBar.MDCTabBar(_footer_tabbar_all[_footer_i]);
				footerTabBar.listen('MDCTabBar:activated', function(event) {
					document.querySelector('.DS-tab-panel-active').classList.remove('DS-tab-panel-active');
					_footer_tabbar_all_content[event.detail.index].classList.add('DS-tab-panel-active');
				});
			}
		</script>
<%
		//
		// GOOGLE ANALYTICS
		//
		
		out.print("\n" + MdcTool.GoogleAnalytics.Create(GoogleAnalyticsID));

	} else {

		//
		// FORCED REDIRECTION
		//
%>
		<script id="_footer_jsp_redirect">
			window.location.href='<%= RedirectTo %>';
		</script>
<%
	}

	//
	// DIALOG DIVs
	//

	out.print(
		"\n" +
		"		<!-- Pre-Built Dialog divs -->\n" +
		"\n" +
		session.getAttribute("DIALOGS_AllDivs")
	);
%>
	<!--
		[FOOTER] -END-
	-->
