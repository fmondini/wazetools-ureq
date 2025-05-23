<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wazetools.ureq.*"
%>
<%!
	/**
	 * Create a CheckBox
	 */
	private static String CreateCheckBox(UreqConfig.EntryParam entryParam) {

		String chkBoxDescr = entryParam.getDescr();
		
		if (chkBoxDescr.contains(">RJCT<") || chkBoxDescr.contains(">DONE<"))
			chkBoxDescr += " (<span style=\"color:red\"><b>*</b></span>)";

		return(
			MdcTool.Check.Box(
				entryParam.getJKeyw(),
				chkBoxDescr,
				"Y",
				entryParam.getValue() ? MdcTool.Check.Status.CHECKED : MdcTool.Check.Status.UNCHECKED,
				""
			) + "<br>"								
		);
	}

	/**
	 * Create a RadioBox
	 */
	private static String CreateRadioBox(String groupName, UreqConfig.EntryParam entryParam) {

		return(
			MdcTool.Radio.Box(
				groupName,
				entryParam.getJKeyw(),
				entryParam.getDescr(),
				entryParam.getJKeyw(),
				entryParam.getValue() ? MdcTool.Radio.Status.CHECKED : MdcTool.Radio.Status.UNCHECKED
			) + "<br>"								
		);
	}
%>
	<script>

		function SaveOptions() {

			var jConfig = {};

 			const allTextBoxes = document.querySelectorAll('input');
 			
 			for (i=0; i<allTextBoxes.length; i++) {

				if (allTextBoxes[i].name.startsWith('jCfg-')) {

					if (allTextBoxes[i].type == 'checkbox') {
						jConfig[allTextBoxes[i].name] = allTextBoxes[i].checked;
					} else if (allTextBoxes[i].type == 'radio') {
						jConfig[allTextBoxes[i].value] = allTextBoxes[i].checked;
 					}
 				}
 			}

 			setCookie('<%= UreqConfig.UREQ_MANAGER_CFG_COOKIE_NAME %>', JSON.stringify(jConfig));
			window.location.href='../unlock/';
		}

	</script>
<%
	Database DB = null;

	try {

		DB = new Database();
		UreqConfig ureqConfig = new UreqConfig(request.getCookies());

		//
		// Get current settings
		//

//		RequestStatus requestStatus;
//		UreqConfig.EntryParam entryParam;
//		JSONObject jCfg = ureqConfig.getJsonCfg();
%>
		<div class="DS-padding-8px DS-back-gray DS-border-dn">
			<div class="DS-text-huge DS-text-black">Configuration &amp; Options</div>
		</div>

		<form action="../unlock/_actions.jsp">

			<input type="hidden" name="Action" value="UpdateCfg">

			<div class="DS-padding-8px">

				<div class="mdc-layout-grid__inner">
<%
					//
					// LEFT COLUMN
					//
%>
					<div class="<%= MdcTool.Layout.Cell(4, 3, 4) %> DS-padding-lf-8px DS-padding-rg16px DS-border-rg">

						<div class="DS-text-huge DS-text-black DS-padding-8px">Requests to show</div>

						<div class="DS-padding-8px DS-border-dn">
<%
							out.println(CreateCheckBox(ureqConfig.getLayerOpen()));
							out.println(CreateCheckBox(ureqConfig.getLayerWork()));
							out.println(CreateCheckBox(ureqConfig.getLayerInfo()));
							out.println(CreateCheckBox(ureqConfig.getLayerRjct()));
							out.println(CreateCheckBox(ureqConfig.getLayerRchk()));
							out.println(CreateCheckBox(ureqConfig.getLayerDone()));
%>
						</div>

						<div class="DS-text-huge DS-text-black DS-padding-8px">Default WME</div>

						<div>
<%
							out.println(CreateRadioBox("jCfg-rbWme", ureqConfig.getWmeProd()));
							out.println(CreateRadioBox("jCfg-rbWme", ureqConfig.getWmeBeta()));
%>
						</div>
					
					</div>
<%
					//
					// CENTER COLUMN
					//
%>
					<div class="<%= MdcTool.Layout.Cell(4, 3, 4) %> DS-padding-rg16px DS-border-rg">
					
						<div class="DS-text-huge DS-text-black DS-padding-8px">Lock filters</div>

						<div class="DS-padding-8px DS-border-dn">
<%
							out.println(CreateCheckBox(ureqConfig.getLock1()));
							out.println(CreateCheckBox(ureqConfig.getLock2()));
							out.println(CreateCheckBox(ureqConfig.getLock3()));
							out.println(CreateCheckBox(ureqConfig.getLock4()));
							out.println(CreateCheckBox(ureqConfig.getLock5()));
							out.println(CreateCheckBox(ureqConfig.getLock6()));
%>
						</div>
						
						<div class="DS-text-huge DS-text-black DS-padding-8px">View filters</div>
						
						<div>
<%
						out.println(CreateRadioBox("jCfg-rbArea", ureqConfig.getAreaAll()));
						out.println(CreateRadioBox("jCfg-rbArea", ureqConfig.getAreaMine()));
%>
						</div>

					</div>
<%
					//
					// RIGHT COLUMN
					//
%>
					<div class="<%= MdcTool.Layout.Cell(4, 2, 4) %> DS-padding-rg-8px">

						<div class="DS-text-huge DS-text-black DS-padding-8px">Request filters</div>

						<div class="DS-padding-8px DS-border-dn">
<%
						out.println(CreateRadioBox("jCfg-rbReq", ureqConfig.getReqAll()));
						out.println(CreateRadioBox("jCfg-rbReq", ureqConfig.getReqMine()));
						out.println(CreateRadioBox("jCfg-rbReq", ureqConfig.getReqOthers()));
						out.println(CreateRadioBox("jCfg-rbReq", ureqConfig.getReqNew()));
%>
						</div>

						<div class="DS-text-huge DS-text-black DS-padding-lfrg-8px DS-padding-8px">Notes</div>
						
						<div>
							<div class="DS-text-big DS-text-italic DS-text-gray DS-padding-lfrg-8px">Map Pins are disabled</div>
							<div class="DS-text-compact DS-text-italic DS-text-gray DS-padding-lfrg-8px DS-padding-bottom-8px">The
								selection of pins to display on the map is disabled because now the type of pin is automatically
								selected depending on the type of request</div>
						</div>
					</div>
				</div>
			</div>

			<div class="DS-padding-8px DS-back-gray DS-border-up">
				<table class="TableSpacing_0px DS-full-width">
					<tr>
						<td class="CellPadding_0px">
							<%= MdcTool.Dialog.BtnDismiss("btnDismiss", "&nbsp;Back", true, "", "", "arrow_back_ios_new", MdcTool.Button.Look.RAISED) %>
						</td>
						<td class="CellPadding_0px" align="right">
							<%= MdcTool.Button.TextIconClass(
								"save",
								"&nbsp;Save",
								null,
								"DS-text-lime",
								null,
								"onClick=\"SaveOptions();\"",
								"Save your options"
							) %>
						</td>
					</tr>
				</table>
			</div>

		</form>
<%
	} catch (Exception e) {
%>
		<div class="DS-padding-8px DS-back-gray">
			<div class="DS-text-exception"><%= e.toString() %></div>
		</div>
<%
	}

	if (DB != null)
		DB.destroy();
%>
