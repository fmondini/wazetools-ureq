<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
%>
<%!
	private static final String PAGE_Title = "UREQ Monitor Help";
	private static final String PAGE_Keywords = "";
	private static final String PAGE_Description = "";
%>
<!DOCTYPE html>
<html>
<head>
	<jsp:include page="../_common/head.jsp">
		<jsp:param name="PAGE_Title" value="<%= PAGE_Title %>"/>
		<jsp:param name="PAGE_Keywords" value="<%= PAGE_Keywords %>"/>
		<jsp:param name="PAGE_Description" value="<%= PAGE_Description %>"/>
	</jsp:include>
</head>

<body>

	<jsp:include page="../_common/header.jsp" />

	<div class="mdc-layout-grid DS-layout-body">
	<div class="mdc-layout-grid__inner">
	<div class="<%= MdcTool.Layout.Cell(12, 8, 4) %>">

	<div class="DS-padding-updn-4px">
		<div class="DS-text-title-shadow"><%= PAGE_Title %></div>
	</div>

	<div class="DS-padding-updn-8px DS-text-big">If you just received a
		<span class="DS-text-FireBrick DS-text-fixed-compact DS-back-pastel-red DS-padding-lfrg-8px DS-border-full">UREQ Monitor Connection Error</span>
		message...
	</div>

	<div class="DS-padding-updn-8px">...it means that the <a href="https://en.wikipedia.org/wiki/WebSocket" target="_blank">WebSocket</a>
		used to receive UREQ notifications did not connect correctly.
	</div>

	<div class="DS-padding-updn-8px DS-text-big">
		Main causes
	</div>

	<div class="DS-padding-updn-8px">
		One of the most frequent causes (if not the only one) is due to the fact that the
		<a href="https://en.wikipedia.org/wiki/Content_Security_Policy" target="_blank">content security policy</a> header
		settings that Waze imposes on WME prohibit certain operations (such as, for example, opening a WebSocket) from working.
	</div>

	<div class="DS-padding-updn-8px DS-text-big">
		The solution
	</div>

	<div class="DS-padding-updn-8px">
		If you are using Jan Biniok's <a href="https://www.tampermonkey.net/" target="_blank">Tampermonkey</a> to run this
		script (and you are, otherwise UREQ wouldn't work) the restriction can be worked around with the changes below:
	</div>

	<div class="DS-padding-updn-8px">
		<ul class="DS-ul-padding">
			<li class="DS-li-padding">Open the Tampermonkey dashboard</li>
			<li class="DS-li-padding">Select the <b>Settings</b> tab at the top right</li>
			<li class="DS-li-padding">Scroll down to the <b>Security</b> section.</li>
			<li class="DS-li-padding">Change the setting of <i>"Modify existing content security policy (CSP) headers"</i> to <b>Remove entirely</b></li>
			<li class="DS-li-padding">Reload WME by pressing <b>F5</b> or <b>Ctrl-F5</b></li>
		</ul>
	</div>

	<div class="DS-padding-updn-8px">
		And that's all folks, have fun with UREQ.
	</div>

	</div>
	</div>
	</div>

	<jsp:include page="../_common/footer.jsp" />

</body>
</html>
