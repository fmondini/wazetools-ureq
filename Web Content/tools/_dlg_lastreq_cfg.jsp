<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="java.util.*"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wazetools.ureq.*"
%>
<%
	//
	// Get statuses cookie settings
	//

	String StatusesCookieName;
	Cookie[] StatusesCookie = request.getCookies();
	HashMap<String, Boolean> StatusesHashMap = new HashMap<>();

	if (StatusesCookie != null) {
		for (RequestStatus X : RequestStatus.values()) {
			if (!X.equals(RequestStatus.UNKN)) {
				StatusesCookieName = Request.getLastStatusCookieMask().replace("{}", X.getValue().toLowerCase());
				StatusesHashMap.put(StatusesCookieName, false);
				for (Cookie statusCookie : StatusesCookie)
					if (statusCookie.getName().equals(StatusesCookieName))
						StatusesHashMap.put(StatusesCookieName, statusCookie.getValue().equalsIgnoreCase("true"));
			}
		}
	}
%>
	<div class="DS-padding-8px DS-back-darkgray DS-border-dn">
		<div class="DS-text-huge">Last Request List Options</div>
	</div>

	<div class="DS-padding-8px DS-back-white DS-border-dn">

		<div class="DS-text-big">Limit entries by area</div>

		<%= MdcTool.Check.Box(
			Request.getMyAreasOnlyObjectName(),
			"Show requests in my area only",
			"Y",
			(Request.isMyAreasOnly(request.getCookies()) ? MdcTool.Check.Status.CHECKED : MdcTool.Check.Status.UNCHECKED),
			"onChange=\"setCookie('" + Request.getMyAreasOnlyCookieName() + "', $(this).is(':checked'));\""
		) %>

	</div>

	<div class="DS-padding-8px DS-back-white DS-border-dn">

		<div class="DS-text-big">Status of items to show</div>

		<div class="mdc-layout-grid__inner DS-grid-gap-0px">
<%
			String CookieName = "";

			for (RequestStatus X : RequestStatus.values()) {

				if (!X.equals(RequestStatus.UNKN)) {

					CookieName = Request.getLastStatusCookieMask().replace("{}", X.getValue().toLowerCase());
%>
					<div class="<%= MdcTool.Layout.Cell(6, 4, 4) %>">
						<%= MdcTool.Check.Box(
							"chkConfig".concat(X.toString()),
							RequestStatus.getColorizedSpan(X) + "&nbsp;Enable &quot;<b>" + X.getDescr() + "</b>&quot; alerts",
							"Y",
							StatusesHashMap.get(CookieName) ? MdcTool.Check.Status.CHECKED : null,
							"onClick=\"setCookie('" + CookieName + "', $(this).is(':checked'));\""
						) %>
					</div>
<%
				}
			}
%>
		</div>
	</div>

	<div class="DS-padding-8px DS-back-darkgray" align="center">
		<%= MdcTool.Dialog.BtnDismiss(
			"btnDismiss",
			"Close",
			true,
			"onClick=\"PopulateList();\"",
			null
		) %>
	</div>
