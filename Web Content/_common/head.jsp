<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="org.json.*"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wazetools.*"
%><%
	request.setCharacterEncoding("UTF-8");

	response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
	response.setHeader("Pragma", "no-cache"); // HTTP 1.0
	response.setDateHeader("Expires", 0); // Proxies

	final String reqTitle = EnvTool.getStr(request, "PAGE_Title", "");
	final String reqKeywords = EnvTool.getStr(request, "PAGE_Keywords", "");
	final String reqDescription = EnvTool.getStr(request, "PAGE_Description", "");

	SiteCfg SCFG = new SiteCfg();
%>
	<!--
		So you're curious... Gotcha!
		Don't take this code as an example, it was written by a cow.
	-->

	<!--
		[HEAD] START
	-->

	<title><%= reqTitle %></title>

	<meta charset="UTF-8" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta http-equiv="X-UA-Compatible" content="IE=edge" />
	<meta name="viewport" content="width=device-width, initial-scale=1.0" />
	<meta name="keywords" content="<%= reqKeywords %>" />
	<meta name="author" content="<%= SysTool.getDevName() %> (<%= SysTool.getDevLink() %>)" />
	<meta name="generator" content="<%= SysTool.getIdeName() %> (<%= SysTool.getIdeLink() %>)" />
	<meta name="description" content="<%= reqDescription %>" />
	<meta name="copyright" content="<%= SysTool.getCopyright() %>" />

	<link rel="icon" type="image/png" href="../favicon.png">

	<link rel="stylesheet" href="../_common/material-components-web.min.css?v=<%= AppCfg.getAppVersion() /* Prevent Caching */ %>">
	<link rel="stylesheet" href="../_common/mdc-default.css?v=<%= AppCfg.getAppVersion() /* Prevent Caching */ %>">
	<link rel="stylesheet" href="../_common/_local_stylesheet.css?v=<%= AppCfg.getAppVersion() /* Prevent Caching */ %>">

	<script id="_head_jsp_jquery" src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
	<script id="_head_jsp_jquery_ui" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.14.1/jquery-ui.min.js"></script>
	<script id="_head_jsp_mdc_web" src="../_common/material-components-web.min.js?v=<%= AppCfg.getAppVersion() /* Prevent Caching */ %>"></script>
<%
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// BROWSER LANGUAGE
	//
%>
	<script id="_head_jsp_browser_lang">

		/**
		 * Check italian browser
		 */
		function isItalian() {
			return ((window.navigator.userLanguage || window.navigator.language).toUpperCase().startsWith('IT'));
		}

		</script>
<%
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// COOKIES
	//
%>
	<script id="_head_jsp_cookies">
<%
	if (SCFG.getGlobals().isCookies()) {
%>
		const cookieExpireDays = 365;

		/**
		 * GET cookie by name
		 */
		function getCookie(cookieName) {

			var theCookie = ' ' + document.cookie;
			var ind = theCookie.indexOf(' ' + cookieName + '=');

			if (ind == -1)
				ind = theCookie.indexOf(';' + cookieName + '=');

			if (ind == -1 || cookieName == '')
				return '';

			var ind1 = theCookie.indexOf(';', ind + 1);

			if (ind1 == -1)
				ind1 = theCookie.length;

			return unescape(theCookie.substring(ind + cookieName.length + 2, ind1));
		}

		/**
		 * SET cookie by name and value
		 */
		function setCookie(cookieName, cookieValue) {

			var today = new Date();
			var expire = new Date();

			expire.setTime(today.getTime() + 3600000 * 24 * cookieExpireDays);
			document.cookie = cookieName + '=' + escape(cookieValue) + ';expires=' + expire.toGMTString();
		}

		/**
		 * DEL cookie by name
		 */
		function delCookie(cookieName, cookiePath) {

			document.cookie = cookieName + '=; Path=' + cookiePath + '; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
		}
<%
	} else
		out.println("\n		// [COOKIES] Hidden by design");
%>
	</script>
<%
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// THROBBERS
	//
%>
	<script id="_head_jsp_throbber">

		/**
		 * Set Throbber in DIV
		 *
		 * @param throbberDivName (string, mandatory) Destination DIV name
		 * @param throbberSize (int, optional) Size in pixel (if omitted -> 24)
		 * @param throbberColor (string, optional) Throbber Color (if omitted -> 'DarkSlateGray')
		 */
		function ThrobberStart(throbberDivName, throbberSize, throbberColor) {

			if (typeof throbberSize === undefined)
				throbberSize = 24;

			if (typeof throbberColor === undefined)
				throbberColor = 'DarkSlateGray';

			$('#' + throbberDivName).html(
				'<svg xmlns="http://www.w3.org/2000/svg" width="' + throbberSize + '" height="' + throbberSize + '" viewBox="0 0 24 24">' +
					'<g stroke="' + throbberColor + '">' +
						'<circle cx="12" cy="12" r="9.5" fill="none" stroke-linecap="round" stroke-width="3">' +
							'<animate attributeName="stroke-dasharray" calcMode="spline" dur="2.25s" keySplines="0.42,0,0.58,1;0.42,0,0.58,1;0.42,0,0.58,1" keyTimes="0;0.475;0.95;1" repeatCount="indefinite" values="0 150;42 150;42 150;42 150"/>' +
							'<animate attributeName="stroke-dashoffset" calcMode="spline" dur="2.25s" keySplines="0.42,0,0.58,1;0.42,0,0.58,1;0.42,0,0.58,1" keyTimes="0;0.475;0.95;1" repeatCount="indefinite" values="0;-16;-59;-59"/>' +
						'</circle>' +
						'<animateTransform attributeName="transform" dur="3s" repeatCount="indefinite" type="rotate" values="0 12 12;360 12 12"/>' +
					'</g>' +
				'</svg>'
			);
		}

		/**
		 * Delete Throbber from DIV
		 *
		 * @param throbberDivName (string, mandatory) Destination DIV name
		 */
		function ThrobberStop(throbberDivName) {
			$('#' + throbberDivName).html('');
		}

	</script>
<%
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// PLEASE WAIT POPUP
	//
%>
	<script id="_head_jsp_please_wait">

		/**
		 * Show/Hide Please Wait PopUp DIV
		 *
		 * @param message (string) The message, or null to dismiss popup
		 */
		function PleaseWaitPopup(message) {

			const WAIT_DIV_BACK_ID = 'DSLIB_WAIT_BACK_DIV_ID';
			const WAIT_DIV_BODY_ID = 'DSLIB_WAIT_BODY_DIV_ID';
			const WAIT_DIV_TEXT_ID = 'DSLIB_WAIT_TEXT_DIV_ID';

			if (message != null) {

				if ($('#' + WAIT_DIV_BACK_ID).length == 0) {
					$('body').append('<div id="' + WAIT_DIV_BACK_ID + '" style="position:absolute; top:0px; left:0px; height:100%; width:100%; background-color:black; opacity:0.5;"></div>');
					$('#' + WAIT_DIV_BACK_ID).css('z-index', 20998);
				}

				if ($('#' + WAIT_DIV_BODY_ID).length == 0) {
					$('body').append('<table id="' + WAIT_DIV_BODY_ID + '" style="position:absolute; top:50%; left:50%; margin-top:-75px; margin-left:-125px;" width="250px" height="150px" bgcolor="#eeeeee"><tr><td align="center" valign="middle"><img src="../images/ajax-loader.gif"><div id="' + WAIT_DIV_TEXT_ID + '" class="DS-padding-top-8px DS-text-large"></div></td></tr></table>');
					$('#' + WAIT_DIV_BODY_ID).css('z-index', 20999);
				}

				$('#' + WAIT_DIV_TEXT_ID).html(message);
				$('#' + WAIT_DIV_BODY_ID).show();
				$('#' + WAIT_DIV_BACK_ID).show();

			} else {

				$('#' + WAIT_DIV_BODY_ID).hide();
				$('#' + WAIT_DIV_BACK_ID).hide();
			}
		}

	</script>
<%
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// DIALOGS (INCLUDE)
	//

	if (SCFG.getGlobals().isDialogs()) {

		MsgTool MSG = new MsgTool(session);

		JSONObject jObj;

		try {

			//
			// INFOBOX
			//

			jObj = new JSONObject(MdcTool.Dialog.Create(MdcTool.Dialog.Type.INFO, MSG.getInfoHead(), MSG.getInfoBody(), "OK", null));

			if (!jObj.isEmpty()) {
%>
				<%= jObj.getString("DivContent") %>
				<script id="_head_jsp_dlg_info">new mdc.dialog.MDCDialog(document.querySelector('#<%= jObj.getString("DivID") %>')).open();</script>
<%
			}

			//
			// ALERTBOX
			//

			jObj = new JSONObject(MdcTool.Dialog.Create(MdcTool.Dialog.Type.ALERT, MSG.getAlertHead(), MSG.getAlertBody(), "OK", null));

			if (!jObj.isEmpty()) {
%>
				<%= jObj.getString("DivContent") %>
				<script id="_head_jsp_dlg_alert">new mdc.dialog.MDCDialog(document.querySelector('#<%= jObj.getString("DivID") %>')).open();</script>
<%
			}

			//
			// SNACKBAR
			//

			jObj = new JSONObject(MdcTool.SnackBar.Create(MSG.getSnackText()));

			if (!jObj.isEmpty()) {
%>
				<%= jObj.getString("DivContent") %>
				<script id="_head_jsp_snack_bar">new mdc.snackbar.MDCSnackbar(document.getElementById('<%= jObj.getString("DivID") %>')).open();</script>
<%			
			}

			//
			// SLIDE BOX
			//

			jObj = new JSONObject(MdcTool.SlideDown.Create(MSG.getSlideHead(), MSG.getSlideBody()));

			if (!jObj.isEmpty()) {
%>
				<!--
					[START] SlideDown Treatment
				-->

				<%= jObj.getString("DivContent") %>

				<script id="_head_jsp_slidedown">

					var msgSlideDiv = $('#<%= jObj.getString("DivID") %>');

					$(msgSlideDiv).hide();

					const winWidth = $(window).width();
					const divWidth = $(msgSlideDiv).width();
					const divTop = 25;
					const divLeft = ((winWidth - divWidth) / 2);

					$(msgSlideDiv).css('position', 'absolute');
					$(msgSlideDiv).css('zIndex', '999');
					$(msgSlideDiv).css('top', divTop + 'px');
					$(msgSlideDiv).css('left', divLeft + 'px');

					$(msgSlideDiv).slideDown(500, function() {
						$(msgSlideDiv).delay(5000).slideUp(300);
					});

				</script>

				<!--
					[-END-] SlideDown Treatment
				-->
<%			
			}

			//
			// JAVASCRIPT PART
			//

			String _DIALOGS_AllDivs = ""; // Used in footer.jsp
%>
			<script id="_head_jsp_dialogs">

				var dialogAjax = null;
				var dialogOk = null;
				var dialogYesNo = null;
				var dialogErrorExt = null;
<%
				//
				// DIALOG (AJAX STYLE)
				//

				jObj = new JSONObject(MdcTool.Dialog.Create(MdcTool.Dialog.Type.AJAX, null, "FILLER", null, null));

				_DIALOGS_AllDivs += ("		" + jObj.getString("DivContent") + "\n");
%>
				/**
				 * NO-BUTTONS FOR AJAX POPUPS
				 *
				 * Show a vanilla message to be used with AJAX
				 * @param BodyText (string) the HTML content
				 * @author fmondini[at]danisoft[dot]net
				 */
				function ShowDialog_AJAX(BodyText) {

					const DialogMainID = '<%= jObj.getString("DivID") %>';

					$('#' + DialogMainID + '_BodyID').html(BodyText);

					dialogAjax = new mdc.dialog.MDCDialog(document.querySelector('#' + DialogMainID));
					dialogAjax.open();
				}

				function DismissDialog_AJAX() {
					try { dialogAjax.close(); } catch (err) { console.error('[ERROR] [dialogs.jsp] %o : %o', err.name, err.message); }
				}
<%
				//
				// DIALOG (DIALOG STYLE)
				//

				jObj = new JSONObject(MdcTool.Dialog.Create(MdcTool.Dialog.Type.DIALOG, "FILLER", "FILLER", "FILLER", null));

				_DIALOGS_AllDivs += ("		" + jObj.getString("DivContent") + "\n");
%>
				/**
				 * OK ONLY
				 *
				 * Show a message with a dismiss button
				 * @param HeadText (string) the HTML title
				 * @param BodyText (string) the HTML content
				 * @param ButtText (string) the dismiss button text (ButtText... LOL)
				 * @author fmondini[at]danisoft[dot]net
				 */
				function ShowDialog_OK(HeadText, BodyText, ButtText) {

					const DialogMainID = '<%= jObj.getString("DivID") %>';

					$('#' + DialogMainID + '_HeadID').html(HeadText);
					$('#' + DialogMainID + '_BodyID').html(BodyText);
					$('#' + DialogMainID + '_Btn_KO').html(ButtText);

					dialogOk = new mdc.dialog.MDCDialog(document.querySelector('#' + DialogMainID));
					dialogOk.open();
				}

				function DismissDialog_OK() {
					try { dialogOk.close(); } catch (err) { console.error('[ERROR] [dialogs.jsp] %o : %o', err.name, err.message); }
				}
<%
				//
				// DIALOG (QUESTION STYLE)
				//

				jObj = new JSONObject(MdcTool.Dialog.Create(MdcTool.Dialog.Type.QUESTION, "FILLER", "FILLER", "FILLER", "FILLER"));

				_DIALOGS_AllDivs += ("		" + jObj.getString("DivContent") + "\n");
%>
				/**
				 * YES/NO DIALOG
				 *
				 * Show a message with an OK and a dismiss button
				 * @param HeadText (string) the HTML title
				 * @param BodyText (string) the HTML content
				 * @param BtnText_OK (string) the OK button text
				 * @param RedirUrl_OK (string) the URL on OK click. Use "" to avoid redirect
				 * @param BtnText_KO (string) the dismiss button text
				 * @param RedirUrl_KO (string) the URL on dismiss click. Use "" to avoid redirect
				 * @author fmondini[at]danisoft[dot]net
				 */
				function ShowDialog_YesNo(HeadText, BodyText, BtnText_OK, RedirUrl_OK, BtnText_KO, RedirUrl_KO) {

					const DialogMainID = '<%= jObj.getString("DivID") %>';

					$('#' + DialogMainID + '_HeadID').html(HeadText);
					$('#' + DialogMainID + '_BodyID').html(BodyText);
					$('#' + DialogMainID + '_Btn_KO').html(BtnText_KO);
					$('#' + DialogMainID + '_Btn_OK').html(BtnText_OK);

					document.getElementById(DialogMainID + '_Btn_KO').addEventListener("click", function() {
						if (RedirUrl_KO != '')
							window.location.href = RedirUrl_KO;
					});

					document.getElementById(DialogMainID + '_Btn_OK').addEventListener("click", function() {
						if (RedirUrl_OK != '')
							window.location.href = RedirUrl_OK;
					});

					dialogYesNo = new mdc.dialog.MDCDialog(document.querySelector('#' + DialogMainID));
					dialogYesNo.open();
				}

				function DismissDialog_YesNo() {
					try { dialogYesNo.close(); } catch (err) { console.error('[ERROR] [dialogs.jsp] %o : %o', err.name, err.message); }
				}
<%
				//
				// DIALOG (ALERT STYLE)
				//

				jObj = new JSONObject(MdcTool.Dialog.Create(MdcTool.Dialog.Type.ALERT, "FILLER", "FILLER", "FILLER", null));

				_DIALOGS_AllDivs += ("		" + jObj.getString("DivContent") + "\n");
%>
				/**
				 * SIMPLE ERROR (using ShowDialog_ErrorExt)
				 *
				 * Show a default error message calling ShowDialog_ErrorExt(HeadText, BodyText, NextUrl)
				 * @param BodyText (string) the HTML content
				 * @author fmondini[at]danisoft[dot]net
				 */
				function ShowDialog_Error(BodyText) {
					ShowDialog_ErrorExt('Internal Error', BodyText, '');
				}

				function DismissDialog_Error() {
					DismissDialog_ErrorExt();
				}

				/**
				 * EXTENDED ERROR
				 *
				 * Show an error message with a dismiss "OK" button
				 * @param HeadText (string) the HTML title
				 * @param BodyText (string) the HTML content
				 * @param NextUrl (string) Where to go after button click, or '' to dismiss only
				 * @author fmondini[at]danisoft[dot]net
				 */
				function ShowDialog_ErrorExt(HeadText, BodyText, NextUrl) {

					const DialogMainID = '<%= jObj.getString("DivID") %>';

					$('#' + DialogMainID + '_HeadID').html(HeadText);
					$('#' + DialogMainID + '_BodyID').html(BodyText);
					$('#' + DialogMainID + '_Btn_KO').html('OK');

					document.getElementById(DialogMainID + '_Btn_KO').addEventListener("click", function() {
						if (NextUrl != '')
							window.location.href = NextUrl;
					});

					dialogErrorExt = new mdc.dialog.MDCDialog(document.querySelector('#' + DialogMainID));
					dialogErrorExt.open();
				}

				function DismissDialog_ErrorExt() {
					try { dialogErrorExt.close(); } catch (err) { console.error('[ERROR] [dialogs.jsp] %o : %o', err.name, err.message); }
				}

			</script>
<%
			//
			// Store all ALL DIVs in session to be printed in footer.jsp
			//

			session.setAttribute("DIALOGS_AllDivs", _DIALOGS_AllDivs);
	
		} catch (Exception e) {

			String errMsg =
				"\n" +
				"++\n" +
				"+++ head.jsp dialog error: " + e.toString() + "\n" +
				"++\n" +
				"\n"
			;

			out.print(errMsg);
			System.err.print(errMsg);
		}
	
	}
%>
	<!--
		[HEAD] -END-
	-->
