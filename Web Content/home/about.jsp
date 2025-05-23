<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wazetools.*"
%>
<%!
	private static final String PAGE_Title = "About this WebApp...";
	private static final String PAGE_Keywords = "";
	private static final String PAGE_Description = AppCfg.getAppAbstract();

	class AboutClass {
		
		private class ItemValue {
			String Name;
			String Data;
			public ItemValue(String name, String data) {
				this.Name = name;
				this.Data = data;
			}
		}

		ItemValue AppTagName	= new ItemValue("WebApp Tag",				AppCfg.getAppTag());
		ItemValue AppShortName	= new ItemValue("WebApp Title",				AppCfg.getAppName());
		ItemValue Abstract		= new ItemValue("Abstract",					AppCfg.getAppAbstract());
		ItemValue Version		= new ItemValue("Version",					AppCfg.getAppVersion());
		ItemValue Release		= new ItemValue("Release",					AppCfg.getAppRelDate());
		ItemValue Library		= new ItemValue("Library",					"DsLib " + SysTool.getLibVersionExt());
		ItemValue LicenseType	= new ItemValue("License Type",				"${VALUE}");
		ItemValue DevelopedBy	= new ItemValue("Developed By",				"${VALUE} (<a href=\"" + SysTool.getDevLink() + "\" target=\"_blank\">" + SysTool.getDevLink() + "</a>)");
		ItemValue DevelopedWith	= new ItemValue("Developed With",			SysTool.getIdeName() + " (<a href=\"" + SysTool.getIdeLink() + "\" target=\"_blank\">" + SysTool.getIdeLink() + "</a>)");
		ItemValue GeoLocation	= new ItemValue("Geographic Host Location",	"${VALUE}");
		ItemValue HostDetails	= new ItemValue("Host System Details",		SysTool.getSystemType() + " with " + SysTool.getJavaVersion());
		ItemValue HostCredits	= new ItemValue("Hosting Credits",			SysTool.getHostedBy());
		ItemValue ClusterRouter	= new ItemValue("Database Cluster Router",	"${VALUE}");
	}

	private static String GetItemDiv(AboutClass.ItemValue itemValue) {
		return(GetItemDiv(itemValue, null));
	}

	private static String GetItemDiv(AboutClass.ItemValue itemValue, String Replacement) {

		String itemData = itemValue.Data;

		if (Replacement != null)
			itemData = itemValue.Data.replace("${VALUE}", Replacement);

		return(
			"<div class=\"DS-padding-0px DS-text-micro DS-text-italic DS-text-gray\">" + itemValue.Name + "</div>" +
			"<div class=\"DS-text-compact DS-padding-0px\">" + itemData + "</div>"
		);
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
		function ZoomGraph(title, url) {
			ShowDialog_OK(
				'<%= AppCfg.getAppName() %> - ' + title + ' - Last 24 hours',
				'<img class="DS-cursor-pointer" src="' + url + '" width="100%" height="auto">',
				'Close'
			);
		}
	</script>

</head>

<body>

	<jsp:include page="../_common/header.jsp" />

	<div class="mdc-layout-grid DS-layout-body">
	<div class="mdc-layout-grid__inner">
	<div class="<%= MdcTool.Layout.Cell(12, 8, 4) %>">

	<div class="DS-padding-top-0px DS-padding-bottom-8px">
		<div class="DS-text-title-shadow"><%= PAGE_Title %></div>
	</div>
<%
	SiteCfg SCFG = new SiteCfg();

	String developerName = SCFG.getAbout().getDevName();
	SiteCfg.SiteRegion siteRegion = SCFG.getGlobals().getSiteRegion();
	String licenseLink = SysTool.getLicense();

	String clusterAddress = (SCFG.getMySQL().getConnClass().equals("")
		? "<span class=\"DS-text-disabled DS-text-italic\">No Database Cluster connected</span>"
		: (	"jdbc:mysql://" +
			(SysTool.isWindog() ? SCFG.getMySQL().getDevlHost() : SCFG.getMySQL().getProdHost()) + ":" +
			(SysTool.isWindog() ? SCFG.getMySQL().getDevlPort() : SCFG.getMySQL().getProdPort()) + "/" +
			SCFG.getMySQL().getSchema()
		)
	);

	//
	// APPDATA
	//

	AboutClass aboutClass = new AboutClass();
%>
	<!-- LINE 1 -->

	<div class="mdc-layout-grid__inner DS-padding-updn-8px DS-border-updn">
		<div class="<%= MdcTool.Layout.Cell(1, 4, 4) %>"><%= GetItemDiv(aboutClass.AppTagName) %></div>
		<div class="<%= MdcTool.Layout.Cell(3, 4, 4) %>"><%= GetItemDiv(aboutClass.AppShortName) %></div>
		<div class="<%= MdcTool.Layout.Cell(4, 4, 4) %>"><%= GetItemDiv(aboutClass.Abstract) %></div>
		<div class="<%= MdcTool.Layout.Cell(1, 4, 4) %>"><%= GetItemDiv(aboutClass.Version) %></div>
		<div class="<%= MdcTool.Layout.Cell(1, 4, 4) %>"><%= GetItemDiv(aboutClass.Release) %></div>
		<div class="<%= MdcTool.Layout.Cell(2, 4, 4) %>"><%= GetItemDiv(aboutClass.Library) %></div>
	</div>

	<!-- LINE 2 -->

	<div class="mdc-layout-grid__inner DS-padding-updn-8px DS-border-dn">
		<div class="<%= MdcTool.Layout.Cell(4, 4, 4) %>"><%= GetItemDiv(aboutClass.LicenseType, licenseLink) %></div>
		<div class="<%= MdcTool.Layout.Cell(4, 4, 4) %>"><%= GetItemDiv(aboutClass.DevelopedBy, developerName) %></div>
		<div class="<%= MdcTool.Layout.Cell(4, 4, 4) %>"><%= GetItemDiv(aboutClass.DevelopedWith) %></div>
	</div>

	<!-- LINE 3 -->

	<div class="mdc-layout-grid__inner DS-padding-updn-8px DS-border-dn">
		<div class="<%= MdcTool.Layout.Cell(4, 3, 4) %>"><%= GetItemDiv(aboutClass.HostCredits) %></div>
		<div class="<%= MdcTool.Layout.Cell(8, 5, 4) %>"><%= GetItemDiv(aboutClass.HostDetails) %></div>
	</div>

	<!-- LINE 4 -->

	<div class="mdc-layout-grid__inner DS-padding-updn-8px DS-border-dn">
		<div class="<%= MdcTool.Layout.Cell(4, 3, 4) %>"><%= GetItemDiv(aboutClass.ClusterRouter, clusterAddress) %></div>
		<div class="<%= MdcTool.Layout.Cell(8, 5, 4) %>"><%= GetItemDiv(aboutClass.GeoLocation, SysTool.getHostLocation(siteRegion)) %></div>
	</div>
<%
	//
	// DSMON
	//

	final String RRD_IMAGES_BASE_URL = "https://dsmon.danisoft.net/rrd-data/img";

	String EthDevName = SCFG.getAbout().getEthName();

	if (!EthDevName.equals("")) {

		String NapName = siteRegion.getNapName();
		String NapLink = siteRegion.getNapLink();

		String ServerPing = SCFG.getAbout().getPingSrv();
		String ServerTraf = SCFG.getAbout().getTrafSrv();
		String ServerTemp = SCFG.getAbout().getTempSrv();

		String ImagePing = RRD_IMAGES_BASE_URL + "/" + ServerPing + "_PING_D-SML.png";
		String ImageTraf = RRD_IMAGES_BASE_URL + "/" + ServerTraf + "_TRAF_" + EthDevName + "_D-SML.png";
		String ImageTemp = RRD_IMAGES_BASE_URL + "/" + ServerTemp + "_TEMP_D-SML.png";

		String PopUpPing = RRD_IMAGES_BASE_URL + "/" + ServerPing + "_PING_D-LRG.png";
		String PopUpTraf = RRD_IMAGES_BASE_URL + "/" + ServerTraf + "_TRAF_" + EthDevName + "_D-LRG.png";
		String PopUpTemp = RRD_IMAGES_BASE_URL + "/" + ServerTemp + "_TEMP_D-LRG.png";

		if (ServerPing.trim().equals("")) ImagePing = PopUpPing = "";
		if (ServerTraf.trim().equals("")) ImageTraf = PopUpTraf = "";
		if (ServerTemp.trim().equals("")) ImageTemp = PopUpTemp = "";
%>
		<div class="DS-padding-top-8px">
			<div class="DS-text-big">Network Status</div>
		</div>

		<div class="mdc-layout-grid__inner DS-grid-gap-8px">

			<div class="<%= MdcTool.Layout.Cell(4, 3, 4) %>">
				<div class="DS-padding-updn-8px" align="center">
					<div class="DS-text-gray DS-text-bold DS-text-italic DS-back-gray">Connection Latency</div>
					<div><img class="DS-cursor-pointer" src="<%= ImagePing %>" width="100%" height="auto" onClick="ZoomGraph('Connection Latency', '<%= PopUpPing %>');"></div>
					<div class="DS-text-small DS-text-italic DS-text-gray">Ping time in ms to the nearest NAP, <a href="<%= NapLink %>" target="_blank"><%= NapName %></a></div>
				</div>
			</div>

			<div class="<%= MdcTool.Layout.Cell(4, 3, 4) %>">
				<div class="DS-padding-updn-8px" align="center">
					<div class="DS-text-gray DS-text-bold DS-text-italic DS-back-gray">Web Server Traffic</div>
					<div><img class="DS-cursor-pointer" src="<%= ImageTraf %>" width="100%" height="auto" onClick="ZoomGraph('Web Server Traffic', '<%= PopUpTraf %>');"></div>
					<div class="DS-text-small DS-text-italic DS-text-gray">Amount of data served by this ethernet interface</div>
				</div>
			</div>

			<div class="<%= MdcTool.Layout.Cell(4, 2, 4) %>">
				<div class="DS-padding-updn-8px" align="center">
					<div class="DS-text-gray DS-text-bold DS-text-italic DS-back-gray">Rack Temperature</div>
					<div><img class="DS-cursor-pointer" src="<%= ImageTemp %>" width="100%" height="auto" onClick="ZoomGraph('Rack Temperature', '<%= PopUpTemp %>');"></div>
					<div class="DS-text-small DS-text-italic DS-text-gray">Values in &deg;C - Shutdown forced if over 95&deg;</div>
				</div>
			</div>

		</div>
<%
	}

	//
	// DISCLAIMER
	//
%>
	<div class="DS-padding-top-8px">

		<div class="DS-padding-5px DS-back-pastel-red DS-border-full">
			<div class="DS-text-exception DS-text-bold DS-text-italic">Disclaimer</div>
		</div>

		<div class="DS-padding-5px DS-border-dn DS-border-lfrg DS-back-pastel-red">
			<div class="DS-text-exception DS-text-small DS-text-justified DS-text-italic">
				<%= SCFG.getAbout().getDisclaimer(SysTool.isBrowserITA(request)) %>
			</div>
		</div>
	</div>

	<div class="DS-padding-updn16px">
		<%= MdcTool.Button.BackTextIcon("Back", "../home/") %>
	</div>

	</div>
	</div>
	</div>

	<jsp:include page="../_common/footer.jsp" />

</body>
</html>
