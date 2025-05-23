////////////////////////////////////////////////////////////////////////////////////////////////////
//
// UserMsg.java
//
// Mail/Slack Interface to interact to the user
//
// First Release: Jan/2023 by Fulvio Mondini (https://danisoft.software/)
//       Revised: Feb/2024 Moved to V3
//       Revised: Mar/2025 Ported to Waze dslib.jar
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.ureq;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import net.danisoft.dslib.Database;
import net.danisoft.dslib.FmtTool;
import net.danisoft.dslib.LogTool;
import net.danisoft.dslib.Mail;
import net.danisoft.dslib.MsgTool;
import net.danisoft.dslib.QrcTool;
import net.danisoft.dslib.SlackMsg;
import net.danisoft.dslib.SysTool;
import net.danisoft.wtlib.auth.WazerContacts;
import net.danisoft.wazetools.AppCfg;

/**
 * Mail/Slack interface to interact with the user
 */
public class UserMsg {

	private static final String WINDOG_FAKE_TARGET_CONTACT = "fmondini"; // Force to this contact on windog devel

	/**
	 * Get Slack HeadImage URL
	 */
	private static String getSlackHeadImageUrl(RequestStatus requestStatus) {
		return("https://ureq.waze.tools/images/slack-" + requestStatus.getValue().toLowerCase() + "-head.png");
	}

	/**
	 * Send a message to a user
	 */
	public boolean Send(HttpServletRequest Request, WazerContacts senderContact, WazerContacts targetContact, Request.Data reqData, String editorMsg) {

		boolean rc = false;

		if (SysTool.isWindog())
			targetContact = new WazerContacts(WINDOG_FAKE_TARGET_CONTACT);

		if (!targetContact.isEmpty()) {

			MsgTool MSG = new MsgTool(Request.getSession());

			String reqNo = "#" + FmtTool.fmtZeroPad(reqData.getID(), 5);

			if (targetContact.getMethod().equals(WazerContacts.Method.SLACK)) {

				////////////////////////////////////////////////////////////////////////////////////////////////////
				//
				// SLACK messages
				//

				SlackMsg SLM = new SlackMsg();

				JSONArray jaBlocks = new JSONArray();

				// Slack Header

				String SlackMsgHead = "Error creating subject, contact Waze.Tools Suite SysOp";

				if (reqData.isStatusOpen()) SlackMsgHead = "Your request has been received";														else
				if (reqData.isStatusWork()) SlackMsgHead = "An expert editor is processing your request";											else
				if (reqData.isStatusInfo()) SlackMsgHead = "We need more info to resolve your request";												else
				if (reqData.isStatusRjct()) SlackMsgHead = "Sorry, your " + (reqData.isResolve() ? "UPDATE" : "UNLOCK") + " request was REJECTED";	else
				if (reqData.isStatusRchk()) SlackMsgHead = "Your " + (reqData.isResolve() ? "UPDATE" : "UNLOCK") + " Request has been solved";		else
				if (reqData.isStatusDone()) SlackMsgHead = "Your " + (reqData.isResolve() ? "UPDATE" : "UNLOCK") + " Request has been solved";

				jaBlocks.put(SLM.DM_BlockCreate_SECTION(getSlackHeadImageUrl(reqData.getStatus()), SlackMsgHead));

				// Slack Abstract

				String SlackMsgAbst = "Error creating message abstract, contact Waze.Tools Suite SysOp";

				if (reqData.isStatusOpen()) SlackMsgAbst = "Your request has been recorded as " + reqNo;											else
				if (reqData.isStatusWork()) SlackMsgAbst = "An expert editor (" + reqData.getManagedBy() + ") is processing your request " + reqNo;	else
				if (reqData.isStatusInfo()) SlackMsgAbst = "Unfortunately, we do not have enough information to resolve your request " + reqNo;		else
				if (reqData.isStatusRjct()) SlackMsgAbst = "We are sorry to inform you that *your request " + reqNo + " has been rejected*";		else
				if (reqData.isStatusRchk()) SlackMsgAbst = "We are glad to inform you that *your request " + reqNo + " has been solved*";			else
				if (reqData.isStatusDone()) SlackMsgAbst = "We are glad to inform you that *your request " + reqNo + " has been solved*";

				jaBlocks.put(SLM.DM_BlockCreate_SECTION(SlackMsgAbst));

				// Slack Body

				String SlackMsgBody = (
					"\n" +
					"`DateTime` " + FmtTool.fmtDateTime(reqData.getTimestamp()) + "\n" +
					"`Location` " + reqData.getLocation() + " - *Lock " + reqData.getLock() + "*\n" +
					"`WME Link` <" + reqData.CreatePermalink() + "|Click here to open the WME Editor>\n" +
					"\n" +
					"```" + reqData.getMotivation() + "```"
				);

				jaBlocks.put(
					SLM.DM_BlockCreate_SECTION(
						SlackMsgBody,
						AppCfg.getServerHomeUrl() + "/api/req/getQrCode?uuid=" + reqData.getUUID(),
						"QR Code to check your request status"
					)
				);

				// Sender references

				if (senderContact != null) {

					String SlackMsgSRef = (
						(editorMsg.trim().equals("")
							? "_For more info contact the editor in charge of this request (see below)_"
							: "*Please read " + reqData.getManagedBy() + " comments*:\n\n```" + editorMsg + "```"
						)
					);

					jaBlocks.put(
						SLM.DM_BlockCreate_SECTION(SlackMsgSRef)
					);
				}

				// Slack footer

				jaBlocks.put(SLM.DM_BlockCreate_DIVIDER());

				jaBlocks.put(
					SLM.DM_BlockCreate_SECTION(
						"*Scan the QR Code to check your request status.*\n" +
						"_If you can't scan the QR Code_ <" + AppCfg.getServerHomeUrl() + "/status/by_uuid.jsp?uuid=" + reqData.getUUID() + "|*click here*>."
					)
				);

				if (senderContact != null) {
					jaBlocks.put(
						SLM.DM_BlockCreate_FOOTER(senderContact.getId())
					);
				}

				// Send message

				try {

					JSONObject jResult = SLM.DirectMessage(targetContact.getId(), jaBlocks);

					if (jResult.getInt("rc") != HttpServletResponse.SC_OK)
						throw new Exception(jResult.getString("error"));

					MSG.setSnackText("Slack DM sent to " + targetContact.getId());
					
					rc = true;

				} catch (Exception e) {

					System.err.println("UserMsg.SendInfoRequest(): " + e.toString());
					MSG.setSlideText("Direct Message Error", e.getMessage());
				}

			} else if (targetContact.getMethod().equals(WazerContacts.Method.MAIL)) {

				////////////////////////////////////////////////////////////////////////////////////////////////////
				//
				// MAIL messages
				//

				try {

					Mail MAIL = new Mail(Request);
					QrcTool QRC = new QrcTool(AppCfg.getServerHomeUrl() + "/status/by_uuid.jsp?uuid=" + reqData.getUUID(), 200, 200, 10);

					// Mail recipient

					if (SysTool.isWindog())
						targetContact = new WazerContacts(WINDOG_FAKE_TARGET_CONTACT);

					MAIL.setRecipient(targetContact.getId());

					// Mail subject

					String MailSubj = "[UREQ ERROR] Error creating mail subject, contact Waze.Tools Suite SysOp (REQ.REQ_Status: '" + reqData.getStatus().getValue() + "')";

					if (reqData.isStatusOpen()) MailSubj = "[UREQ #" + reqData.getID() + "] Your request has been received";													else
					if (reqData.isStatusWork()) MailSubj = "[UREQ #" + reqData.getID() + "] An expert editor is processing your request";										else
					if (reqData.isStatusInfo()) MailSubj = "[UREQ #" + reqData.getID() + "] We need more info to resolve your request";											else
					if (reqData.isStatusRjct()) MailSubj = "[UREQ #" + reqData.getID() + "] Sorry, your UPDATE/UNLOCK request was REJECTED";									else
					if (reqData.isStatusRchk()) MailSubj = "[UREQ #" + reqData.getID() + "] Your " + (reqData.isResolve() ? "UPDATE" : "UNLOCK") + " Request has been solved";	else
					if (reqData.isStatusDone()) MailSubj = "[UREQ #" + reqData.getID() + "] Your " + (reqData.isResolve() ? "UPDATE" : "UNLOCK") + " Request has been solved";

					MAIL.setHtmlTitle(MailSubj);
					MAIL.setSubject(MailSubj);

					// Mail body

					String SnippetAbst = "<p style=\"color:red\">[UREQ ERROR] Error creating mail abstract, contact Waze.Tools Suite SysOp (REQ.REQ_Status: '" + reqData.getStatus().getValue() + "')</p>";

					if (reqData.isStatusOpen()) SnippetAbst = "Your request has been recorded as " + reqNo;																			else
					if (reqData.isStatusWork()) SnippetAbst = "An expert editor is processing your request " + reqNo;																else
					if (reqData.isStatusInfo()) SnippetAbst = "unfortunately we do not have enough information to resolve your request " + reqNo + " summarized below";				else
					if (reqData.isStatusRjct()) SnippetAbst = "we are sorry to inform you that <span style=\"color:#ff0000\">your request " + reqNo + " has been rejected</span>";	else
					if (reqData.isStatusRchk()) SnippetAbst = "we are glad to inform you that <span style=\"color:#009900\">your request " + reqNo + " has been solved</span>";		else
					if (reqData.isStatusDone()) SnippetAbst = "we are glad to inform you that <span style=\"color:#009900\">your request " + reqNo + " has been solved</span>";

					String MailBody = (
						"<p>Dear " + reqData.getUser() + ", " + SnippetAbst + ".</p>" +
						"<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">" +
							"<tr><td><b>Date/Time</b></td><td>" + FmtTool.fmtDateTime(reqData.getTimestamp()) + "</td><td RowSpan=\"5\"><a href=\"" + AppCfg.getServerHomeUrl() + "/status/by_uuid.jsp?uuid=" + reqData.getUUID() + "\">" + QRC.getImgTag() + "</a></td></tr>" +
							"<tr><td><b>Location</b></td><td>" + reqData.getLocation() + "</td></tr>" +
							"<tr><td><b>Reason</b></td><td>" + reqData.getMotivation() + "</td></tr>" +
							"<tr><td><b>WME Link</b></td><td><a href=\"" + reqData.CreatePermalink() + "\">Click here to open in WME</a></td></tr>" +
							"<tr><td><b>Status</b></td><td><i>Click or scan the QRCode to check status</i> -></td></tr>" +
						"<table>"
					);

					MAIL.addHtmlBody(MailBody);

					String MailSenderContact =
						"<p><span style=\"color:#006666\">" + (
							editorMsg.trim().equals("")
								? "For more info contact the editor in charge of this request."
								: "<big><b><u>Comments:</u></b></big><br><br>" + editorMsg.replace("\n", "<br><br>")
							) +
						"</span></p>"
					;

					MAIL.addHtmlBody(MailSenderContact);

					// Mail footer

					String MailFooter = "";

					if (!reqData.getManagedBy().trim().equals("")) {

						MailFooter =
							"<p style=\"color:#555555\"><i><b>NOTE:</b><br>" +
							"This request was handled by <b>" + reqData.getManagedBy() + "</b>. Contact him on Slack or via forum for more information.</i></p>"
						;

						MAIL.addHtmlBody(MailFooter);
					}

					Database DB = new Database();
					LogTool LOG = new LogTool(DB.getConnection());

					if (MAIL.Send()) {
						MSG.setSnackText("Mail sent to " + targetContact.getId());
						LOG.Info(Request, LogTool.Category.MAIL, "Mail sent to '" + MAIL.getRecipient() + "' with subject '" + MAIL.getSubject() + "'");
						rc = true;
					} else {
						LOG.Error(Request, LogTool.Category.MAIL, "Error sending mail to '" + MAIL.getRecipient() + "' with subject '" + MAIL.getSubject() + "': " + MAIL.getLastError());
						throw new Exception("Mail Error: " + MAIL.getLastError());
					}

					if (DB != null)
						DB.destroy();

				} catch (Exception e) {

					System.err.println("UserMsg.SendInfoRequest(): " + e.toString());
					MSG.setSlideText("Mail Error for " + targetContact.getId(), e.getMessage());
				}
			}
		}

		return(rc);
	}

}
