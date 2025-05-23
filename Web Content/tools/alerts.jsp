<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wtlib.auth.*"
	import="net.danisoft.wtlib.ureq.*"
	import="net.danisoft.wazetools.*"
	import="net.danisoft.wazetools.ureq.*"
%>
<%!
	private static final String PAGE_Title = "Alerts Management";
	private static final String PAGE_Keywords = "Waze.Tools UREQ, Waze, Tools, Unlock, Alerts";
	private static final String PAGE_Description = AppCfg.getAppAbstract();
	
	private static final String CHKBOX_STATUSES_PREFIX = "chkRssStatuses";
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
<%
	String RedirectTo = "";
	
	String Action = EnvTool.getStr(request, "Action", "");

	Database DB = new Database();
	Alert ALR = new Alert(DB.getConnection());
	Request REQ = new Request(DB.getConnection());
	MsgTool MSG = new MsgTool(session);

	Alert.Data alrData = ALR.new Data();

	try {

		User USR = new User(DB.getConnection());
		User.Data usrData = USR.Read(SysTool.getCurrentUser(request));

		String auuSlackID = usrData.getSlackID();
		String auuMailAdd = usrData.getMail();

		if (Action.equals("")) {

			try {

				alrData = ALR.Read(SysTool.getCurrentUser(request));

			} catch (Exception e) {

				// It's a new user, insert a default record
				alrData.setUser(SysTool.getCurrentUser(request));
				ALR.Insert(alrData);

				// Re-read
				alrData = ALR.Read(SysTool.getCurrentUser(request));
			}
%>
			<div class="<%= MdcTool.Layout.Cell(12, 8, 4) %>">

				<form>
				<input type="hidden" name="Action" value="update">
<%
				//
				// Header
				//
%>
				<div class="DS-card-body">
					<div class="DS-text-title-shadow"><%= PAGE_Title %></div>
				</div>

				<div class="DS-card-body">
					<div class="mdc-tab-bar DS-back-lightgray DS-border-full" role="tablist">
						<div class="mdc-tab-scroller">
							<div class="mdc-tab-scroller__scroll-area">
								<div class="mdc-tab-scroller__scroll-content">
									<%= MdcTool.Tab.ElementIconText(0, "comment", "Via Slack", "DS-text-blue DS-text-bold", true) %>
									<%= MdcTool.Tab.ElementIconText(1, "email", "Via e-Mail", "DS-text-blue DS-text-bold", false) %>
									<%= MdcTool.Tab.ElementIconText(2, "rss_feed", "Via RSS Feed", "DS-text-blue DS-text-bold", false) %>
								</div>
							</div>
						</div>
					</div>
				</div>
<%
				////////////////////////////////////////////////////////////////////////////////////////////////////
				//
				// SLACK
				//
%>
				<div class="DS-tab-panel DS-tab-panel-active">

					<div class="DS-padding-top-16px DS-padding-bottom-0px">
						<div class="DS-text-extra-large">Slack Alerts Setup</div>
					</div>
					<div class="DS-padding-top-0px DS-padding-bottom-16px DS-border-dn">
						<div class="DS-text-italic">Set Slack's real-time messaging behavior</div>
					</div>
<%
					//
					// Enablement
					//
%>
					<div class="DS-padding-updn16px DS-border-dn">
						<div class="mdc-layout-grid__inner">
							<div class="<%= MdcTool.Layout.Cell(3, 2, 4) %> DS-grid-middle-left">
								<div class="DS-text-huge">Enablement</div>
							</div>
							<div class="<%= MdcTool.Layout.Cell(4, 3, 4) %> DS-grid-middle-left">
								<%= MdcTool.Check.Box(
									"chkSlack",
									"Activate Alerts via Slack DM",
									"Y",
									alrData.isSlkActive() ? MdcTool.Check.Status.CHECKED : null,
									""
								) %>
							</div>
							<div class="<%= MdcTool.Layout.Cell(5, 3, 4) %> DS-grid-middle-right">
								<div class="DS-text-compact" align="center">
									<div class="DS-text-gray DS-text-italic">Your SlackID on file</div>
									<div class="DS-text-fixed DS-text-bold DS-text-purple"><%= auuSlackID %></div>
								</div>
							</div>
						</div>
					</div>
<%
					//
					// Country Coverage
					//
%>
					<div class="DS-padding-updn16px DS-border-dn">
						<div class="mdc-layout-grid__inner">
							<div class="<%= MdcTool.Layout.Cell(3, 2, 4) %> DS-grid-middle-left">
								<div class="DS-text-huge">Country Coverage</div>
							</div>
							<div class="<%= MdcTool.Layout.Cell(4, 3, 4) %> DS-grid-middle-left">
								<%= MdcTool.Select.Box(
									"slkCountry",
									MdcTool.Select.Width.NORM,
									"Select Country",
									REQ.getCountriesCombo(alrData.getSlkCountry()),
									""
								) %>
							</div>
							<div class="<%= MdcTool.Layout.Cell(5, 3, 4) %> DS-grid-middle-left">
								<%= MdcTool.Check.Box(
									"chkSlackAll",
									"Enable Alerts for the whole country<br><span class=\"DS-text-small DS-text-italic\">If not checked you'll receive only alerts for requests in your area</span>",
									"Y",
									(alrData.isSlkAllCountry() ? MdcTool.Check.Status.CHECKED : null),
									""
								) %>
							</div>
						</div>
					</div>

				</div>
<%
				////////////////////////////////////////////////////////////////////////////////////////////////////
				//
				// EMAIL
				//
%>
				<div class="DS-tab-panel">

					<div class="DS-padding-top-16px DS-padding-bottom-0px">
						<div class="DS-text-extra-large">e-Mail Alerts Setup</div>
					</div>
					<div class="DS-padding-top-0px DS-padding-bottom-16px DS-border-dn">
						<div class="DS-text-italic">Set the behavior of e-Mail messages</div>
					</div>
<%
					//
					// Enablement
					//
%>
					<div class="DS-padding-updn16px DS-border-dn">
						<div class="mdc-layout-grid__inner">
							<div class="<%= MdcTool.Layout.Cell(3, 2, 4) %> DS-grid-middle-left">
								<div class="DS-text-huge">Enablement</div>
							</div>
							<div class="<%= MdcTool.Layout.Cell(4, 3, 4) %> DS-grid-middle-left">
								<%= MdcTool.Check.Box(
									"chkMail",
									"Activate Alerts via e-Mail",
									"Y",
									alrData.isEmlActive() ? MdcTool.Check.Status.CHECKED : null,
									""
								) %>
							</div>
							<div class="<%= MdcTool.Layout.Cell(5, 3, 4) %> DS-grid-middle-right">
								<div class="DS-text-compact" align="center">
									<div class="DS-text-gray DS-text-italic">Your e-Mail address on file</div>
									<div class="DS-text-italic DS-text-purple"><%= auuMailAdd %></div>
								</div>
							</div>
						</div>
					</div>
<%
					//
					// Country Coverage
					//
%>
					<div class="DS-padding-updn16px DS-border-dn">
						<div class="mdc-layout-grid__inner">
							<div class="<%= MdcTool.Layout.Cell(3, 2, 4) %> DS-grid-middle-left">
								<div class="DS-text-huge">Country Coverage</div>
							</div>
							<div class="<%= MdcTool.Layout.Cell(4, 3, 4) %> DS-grid-middle-left">
								<%= MdcTool.Select.Box(
									"emlCountry",
									MdcTool.Select.Width.NORM,
									"Select Country",
									REQ.getCountriesCombo(alrData.getEmlCountry()),
									""
								) %>
							</div>
							<div class="<%= MdcTool.Layout.Cell(5, 3, 4) %> DS-grid-middle-left">
								<%= MdcTool.Check.Box(
									"chkMailAll",
									"Enable Alerts for the whole country<br><span class=\"DS-text-small DS-text-italic\">If not checked you'll receive only alerts for requests in your area</span>",
									"Y",
									alrData.isEmlAllCountry() ? MdcTool.Check.Status.CHECKED : null,
									""
								) %>
							</div>
						</div>
					</div>
<%
					//
					// Frequency
					//
%>
					<div class="DS-padding-updn16px DS-border-dn">
						<div class="mdc-layout-grid__inner">
							<div class="<%= MdcTool.Layout.Cell(3, 2, 4) %>">
								<div class="DS-text-huge">Send me an email when...</div>
							</div>
							<div class="<%= MdcTool.Layout.Cell(9, 6, 4) %>">
								<%= MdcTool.Check.Box("chkMailNew", "a new request is created", "Y", alrData.isEmlOnCreate() ? MdcTool.Check.Status.CHECKED : null, "") %><br>
								<%= MdcTool.Check.Box("chkMailMod", "<span class=\"DS-text-disabled\">a request is modified (todo)</span>", "Y", alrData.isEmlOnModify() ? MdcTool.Check.Status.CHECKED : null, "disabled") %><br>
								<%= MdcTool.Check.Box("chkMailEnd", "<span class=\"DS-text-disabled\">a request is rejected or closed (todo)</span>", "Y", alrData.isEmlOnClose() ? MdcTool.Check.Status.CHECKED : null, "disabled") %><br>
								<%= MdcTool.Check.Box("chkMailMyR", "<span class=\"DS-text-disabled\">a request managed by me change status (todo)</span>", "Y", alrData.isEmlOnModMine() ? MdcTool.Check.Status.CHECKED : null, "disabled") %>
							</div>
						</div>
					</div>
<%
					//
					// Rate
					//
%>
					<div class="DS-padding-updn16px DS-border-dn">
						<div class="mdc-layout-grid__inner">
							<div class="<%= MdcTool.Layout.Cell(3, 2, 4) %> DS-grid-middle-left">
								<div class="DS-text-huge">Sending Frequency</div>
							</div>
							<div class="<%= MdcTool.Layout.Cell(4, 3, 4) %> DS-grid-middle-left">
								<% String maxFreqOptLine = ""; %>
								<% for (EmailFrequency maxFreqOptEnum : EmailFrequency.values()) { %>
									<% if (!maxFreqOptEnum.equals(EmailFrequency.UNK)) { %>
										<% maxFreqOptLine += "<option " + (maxFreqOptEnum.equals(alrData.getEmlFrequency()) ? "selected" : "") + " value=\"" + maxFreqOptEnum.getCode() + "\">" + maxFreqOptEnum.getDesc() + "</option>"; %>
									<% } %>
								<% } %>
								<%= MdcTool.Select.Box(
									"mailFreq",
									MdcTool.Select.Width.NORM,
									"Messages Rate",
									maxFreqOptLine,
									"disabled"
								) %>
							</div>
							<div class="<%= MdcTool.Layout.Cell(5, 3, 4) %> DS-grid-middle-left">
								<div class="DS-text-exception DS-text-italic">This option is still under development, but...<br>given its limited utility, it probably won't be done</div>
							</div>
						</div>
					</div>

				</div>
<%
				////////////////////////////////////////////////////////////////////////////////////////////////////
				//
				// RSS FEED
				//
%>
				<div class="DS-tab-panel">

					<div class="DS-padding-top-16px DS-padding-bottom-0px">
						<div class="DS-text-extra-large">RSS Feed Alerts Setup</div>
					</div>
					<div class="DS-padding-top-0px DS-padding-bottom-16px DS-border-dn">
						<div class="DS-text-italic">Set the filters used to create your RSS feed</div>
					</div>
<%
					//
					// Enablement
					//
%>
					<div class="DS-padding-updn16px DS-border-dn">
						<div class="mdc-layout-grid__inner">
							<div class="<%= MdcTool.Layout.Cell(3, 2, 4) %> DS-grid-middle-left">
								<div class="DS-text-huge">Enablement</div>
							</div>
							<div class="<%= MdcTool.Layout.Cell(4, 3, 4) %> DS-grid-middle-left">
								<div class="DS-text-purple DS-text-italic">The RSS feed is always enabled</div>
							</div>
							<div class="<%= MdcTool.Layout.Cell(5, 3, 4) %> DS-grid-middle-right">
								<div class="DS-text-compact" align="center">
									<div class="DS-text-gray DS-text-italic">The RSS feed cannot be disabled</div>
									<div class="DS-text-gray DS-text-italic">If you don't want it, don't use it</div>
								</div>
							</div>
						</div>
					</div>
<%
					//
					// Country
					//
%>
					<div class="DS-padding-updn16px DS-border-dn">
						<div class="mdc-layout-grid__inner">
							<div class="<%= MdcTool.Layout.Cell(3, 2, 4) %> DS-grid-middle-left">
								<div class="DS-text-huge">Country Coverage</div>
							</div>
							<div class="<%= MdcTool.Layout.Cell(4, 3, 4) %> DS-grid-middle-left">
								<%= MdcTool.Select.Box(
									"rssCountry",
									MdcTool.Select.Width.NORM,
									"Select a Country",
									REQ.getCountriesCombo(alrData.getRssCountry()),
									""
								) %>
							</div>
							<div class="<%= MdcTool.Layout.Cell(5, 3, 4) %> DS-grid-middle-left">
								<%= MdcTool.Check.Box(
									"chkRssAll",
									"Enable Alerts for the whole Country<br><span class=\"DS-text-small DS-text-italic\">If not checked you'll receive only alerts for requests in your area</span>",
									"Y",
									alrData.isRssAllCountry() ? MdcTool.Check.Status.CHECKED : null,
									""
								) %>
							</div>
						</div>
					</div>
<%
					//
					// Statuses
					//
%>
					<div class="DS-padding-updn16px DS-border-dn">
						<div class="mdc-layout-grid__inner">
							<div class="<%= MdcTool.Layout.Cell(3, 2, 4) %>">
								<div class="DS-text-huge">Included Statuses</div>
							</div>
							<div class="<%= MdcTool.Layout.Cell(9, 6, 4) %>">
								<div class="mdc-layout-grid__inner DS-grid-gap-0px">
									<% for (RequestStatus X : RequestStatus.values()) { %>
										<% if (!X.equals(RequestStatus.UNKN)) { %>
										<div class="<%= MdcTool.Layout.Cell(12, 8, 4) %>">
											<%= MdcTool.Check.Box(
												CHKBOX_STATUSES_PREFIX.concat(X.toString()),
												RequestStatus.getColorizedSpan(X) + "&nbsp;Enable alerts in <b>" + X.getDescr() + "</b> status",
												"Y",
												alrData.getRssActStatuses().contains(X.toString()) ? MdcTool.Check.Status.CHECKED : null,
												""
											) %>						
										</div>
										<% } %>
									<% } %>
								</div>
							</div>
							<div class="<%= MdcTool.Layout.Cell(3, 2, 4) %>"></div>
							<div class="<%= MdcTool.Layout.Cell(9, 6, 4) %>">
								<% String maxArrOptLine = ""; %>
								<% int maxArrOpt[] = { 10, 20, 50, 100, 1000 }; %>
								<% for (int maxArrOptValue : maxArrOpt) { %>
									<% maxArrOptLine += "<option " + (alrData.getRssMaxEntries() == maxArrOptValue ? "selected" : "") + " value=\"" + maxArrOptValue + "\">Show no more than " + maxArrOptValue + " entries</option>"; %>
								<% } %>
								<%= MdcTool.Select.Box(
									"rssMax",
									MdcTool.Select.Width.NORM,
									"Max entries",
									maxArrOptLine,
									""
								) %>
							</div>
						</div>
					</div>
<%
					//
					// RSS Link
					//
%>
					<div class="DS-padding-updn16px DS-border-dn">
						<div class="mdc-layout-grid__inner">
							<div class="<%= MdcTool.Layout.Cell(3, 2, 4) %> DS-grid-middle-left">
								<div class="DS-text-huge">RSS Feed Link</div>
							</div>
							<div class="<%= MdcTool.Layout.Cell(6, 4, 4) %> DS-grid-middle-left">
								<a class="DS-text-bold DS-text-italic" href="<%= alrData.getRssFeedLink() %>" target="_blank"><%= alrData.getRssFeedLink() %></a>
							</div>
							<div class="<%= MdcTool.Layout.Cell(3, 2, 4) %> DS-grid-middle-right">
								<div class="DS-text-exception DS-text-italic">Save to update RSS feed link</div>
							</div>
						</div>
					</div>

				</div>
<%
				////////////////////////////////////////////////////////////////////////////////////////////////////
				//
				// FOOTER
				//
%>
				<div class="DS-padding-updn16px">
					<div class="mdc-layout-grid__inner">
						<div class="<%= MdcTool.Layout.Cell(6, 4, 2) %>" align="left">
							<%= MdcTool.Button.BackTextIcon("Back", "../home/") %>
						</div>
						<div class="<%= MdcTool.Layout.Cell(6, 4, 2) %>" align="right">
							<%= MdcTool.Button.SubmitTextIconClass(
								"save",
								"&nbsp;Save",
								null,
								"DS-text-lime",
								null,
								""
							) %>
						</div>
					</div>
				</div>

				</form>

			</div>
<%
		} else if (Action.equals("update")) {

			try {

				// Check consistency

				boolean isSlackActive = EnvTool.getStr(request, "chkSlack", "N").equals("Y");
				boolean isEmailActive = EnvTool.getStr(request, "chkMail", "N").equals("Y");

				if (isSlackActive && auuSlackID.equals(""))
					throw new Exception("Unable to turn on Slack alerts, no SlackID found in your profile");

				if (isSlackActive && !SlackMsg.isSlackIdValid(auuSlackID))
					throw new Exception("Unable to turn on Slack alerts, malformed SlackID found in your profile: <b>" + auuSlackID + "</b>");

				if (isEmailActive && auuMailAdd.equals(""))
					throw new Exception("Unable to activate e-Mail alerts, no address found in your profile");

				if (isEmailActive && !Mail.isAddressValid(auuMailAdd))
					throw new Exception("Unable to activate e-Mail alerts, malformed address found in your profile: <b>" + auuMailAdd + "</b>");

				// Get current data

				alrData = ALR.Read(SysTool.getCurrentUser(request));

				//
				// Slack
				//

				alrData.setSlkActive(isSlackActive);
				alrData.setSlkCountry(EnvTool.getStr(request, "slkCountry", ""));
				alrData.setSlkAllCountry(EnvTool.getStr(request, "chkSlackAll", "N").equals("Y"));

				//
				// Email
				//

				alrData.setEmlActive(isEmailActive);
				alrData.setEmlCountry(EnvTool.getStr(request, "emlCountry", ""));
				alrData.setEmlAllCountry(EnvTool.getStr(request, "chkMailAll", "N").equals("Y"));
				alrData.setEmlOnCreate(EnvTool.getStr(request, "chkMailNew", "N").equals("Y"));
				alrData.setEmlOnModify(EnvTool.getStr(request, "chkMailMod", "N").equals("Y"));
				alrData.setEmlOnClose(EnvTool.getStr(request, "chkMailEnd", "N").equals("Y"));
				alrData.setEmlOnModMine(EnvTool.getStr(request, "chkMailMyR", "N").equals("Y"));
				alrData.setEmlFrequency(EmailFrequency.RTM); // TODO: Email Frequency is fixed at EmailFrequency.RTM

				//
				// RSS Feed
				//

				alrData.setRssCountry(EnvTool.getStr(request, "rssCountry", ""));
				alrData.setRssAllCountry(EnvTool.getStr(request, "chkRssAll", "N").equals("Y"));
				alrData.setRssMaxEntries(EnvTool.getInt(request, "rssMax", Alert.getRssMaxEntriesDefault()));
				alrData.setRssFeedLink(AppCfg.getServerHomeUrl() + "/servlet/RssFeed?c=" + alrData.getRssCountry() + "&u=" + SysTool.getCurrentUser(request));

				// RSS Status Checkboxes

				String tmpStatuses = "";

				for (RequestStatus X : RequestStatus.values())
					if (EnvTool.getStr(request, CHKBOX_STATUSES_PREFIX + X.getValue(), "N").equals("Y"))
						tmpStatuses += ((tmpStatuses.equals("") ? "" : SysTool.getDelimiter()) + X.getValue());

				while (tmpStatuses.contains(SysTool.getDelimiter() + SysTool.getDelimiter()))
					tmpStatuses = tmpStatuses.replace(SysTool.getDelimiter() + SysTool.getDelimiter(), SysTool.getDelimiter());
				
				if (tmpStatuses.startsWith(SysTool.getDelimiter()))
					tmpStatuses = tmpStatuses.substring(1).trim();

				alrData.setRssActStatuses(tmpStatuses);

				//
				// Update
				//

				ALR.Update(SysTool.getCurrentUser(request), alrData);

				MSG.setSnackText("Configuration updated");

			} catch (Exception e) {
				MSG.setSlideText("Update Error", e.getMessage());
			}

			RedirectTo = "?";

		} else
			throw new Exception("Unknown Action: '" + Action + "'");

	} catch (Exception e) {
%>
		<div class="<%= MdcTool.Layout.Cell(12, 8, 4) %>">
			<div class="mdc-card <%= MdcTool.Elevation.Light() %>">
				<div class="DS-padding-24px">
					<div class="DS-text-huge DS-text-exception">Internal Error</div>
					<div class="DS-text-exception"><%= e.toString() %></div>
				</div>
			</div>
		</div>
<%
	}

	if (DB != null)
		DB.destroy();
%>
	</div> <!-- /mdc-layout-grid__inner -->
	</div> <!-- /mdc-layout-grid -->

	<jsp:include page="../_common/footer.jsp">
		<jsp:param name="RedirectTo" value="<%= RedirectTo %>"/>
	</jsp:include>

</body>
</html>
