<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wtlib.auth.*"
	import="net.danisoft.wazetools.ureq.*"
%>
<%!
	Database DB = null;

	/**
	 * Send messages
	 */
	private void _send_message(int ReqID, HttpServletRequest Request, Request.Data reqData, WazerContacts senderContact, WazerContacts targetContact, String customMsg) throws Exception {

		History URH = new History(this.DB.getConnection());
		UserMsg USM = new UserMsg();
		History.Data urhData = URH.new Data();
		
		urhData.setUreqID(ReqID);
		urhData.setAction(reqData.getStatus());
		urhData.setEditor(SysTool.getCurrentUser(Request));
		
		urhData.setComments(
			USM.Send(Request, senderContact, targetContact, reqData, customMsg)
				? "A '" + reqData.getStatus().getDescr() + "' message was sent to " + reqData.getUser() + " (" + targetContact.getId() + ") via " + (targetContact.getMethod().equals(WazerContacts.Method.SLACK) ? "Slack" : "e-Mail")
				: "No message sent to " + reqData.getUser() + ": Doesn't have a contact"
		);

		URH.Insert(urhData);
	}

	/**
	 * Get the NextHop (where to return) based on caller and DON'T reopen details dialog
	 */
	private static String _get_next_hop(String caller) {
		return(
			_get_next_hop(caller, 0)
		);
	}

	/**
	 * Get the NextHop (where to return) based on caller
	 * @param ReqID Set to 0 (zero) if do not want to reopen details dialog
	 */
	private static String _get_next_hop(String caller, int ReqID) {

		return(caller.equals("LAST")
			? "../tools/lastreq.jsp"
			: (caller.equals("RCHK")
				? "../tools/recheck.jsp"
				: (ReqID == 0
					? "../unlock/"
					: "../unlock/?ReqID=" + ReqID
				)
			)
		);
	}
%>
<%
	String NextHop = _get_next_hop(""); // Default

	String Action = EnvTool.getStr(request, "Action", "");
	String Caller = EnvTool.getStr(request, "Caller", "");
	int ReqID = EnvTool.getInt(request, "ReqID", 0);

	try {

		this.DB = new Database();
		Request REQ = new Request(this.DB.getConnection());
		History URH = new History(this.DB.getConnection());

		if (Action.equals("actUpdateNotes")) {

			////////////////////////////////////////////////////////////////////////////////////////////////////
			//
			// Update request notes
			//

			REQ.UpdateNotes(
				ReqID,
				SysTool.getCurrentUser(request),
				java.net.URLDecoder.decode(EnvTool.getStr(request, "NewNotesValue", ""), "UTF-8")
			);

			NextHop = _get_next_hop(Caller);

		} else if (Action.equals("actReopen")) {

			////////////////////////////////////////////////////////////////////////////////////////////////////
			//
			// Reopen a request
			//

			RequestStatus previousStatus = REQ.UpdateStatus(ReqID, RequestStatus.OPEN, SysTool.getCurrentUser(request), false);

			History.Data urhData = URH.new Data();

			urhData.setUreqID(ReqID);
			urhData.setAction(RequestStatus.OPEN);
			urhData.setEditor(SysTool.getCurrentUser(request));
			urhData.setComments("Status manually forced from " + previousStatus.getValue() + " to " + RequestStatus.OPEN.getValue());

			URH.Insert(urhData);

			NextHop = _get_next_hop(Caller, ReqID);

		} else if (Action.equals("actPutInWork")) {

			////////////////////////////////////////////////////////////////////////////////////////////////////
			//
			// Put in WORK and edit in WME
			//

			REQ.UpdateStatus(ReqID, RequestStatus.WORK, SysTool.getCurrentUser(request), false);

			History.Data urhData = URH.new Data();

			urhData.setUreqID(ReqID);
			urhData.setAction(RequestStatus.WORK);
			urhData.setEditor(SysTool.getCurrentUser(request));
			urhData.setComments("Unlocking operations started");

			URH.Insert(urhData);

			// Send message

/* 2024-04-14 DEPRECATED (annoying)

			Request.Data reqData = REQ.Read(ReqID);

			_send_message(
				ReqID,
				request,
				reqData,
				new Contacts(SysTool.getCurrentUser(request)),
				new Contacts(reqData.getUser()),
				""
			);
*/
			NextHop = _get_next_hop(Caller, ReqID);

		} else if (Action.equals("actRequestInfo")) {

			////////////////////////////////////////////////////////////////////////////////////////////////////
			//
			// Send an info request
			//

			String customMsg = EnvTool.getStr(request, "customMsg", "");
			
			REQ.UpdateStatus(ReqID, RequestStatus.INFO, SysTool.getCurrentUser(request), false);

			History.Data urhData = URH.new Data();

			urhData.setUreqID(ReqID);
			urhData.setAction(RequestStatus.INFO);
			urhData.setEditor(SysTool.getCurrentUser(request));
			urhData.setComments(customMsg);

			URH.Insert(urhData);

			// Send message
			
			Request.Data reqData = REQ.Read(ReqID);

			_send_message(
				ReqID,
				request,
				reqData,
				new WazerContacts(SysTool.getCurrentUser(request)),
				new WazerContacts(reqData.getUser()),
				customMsg
			);

			NextHop = _get_next_hop(Caller, ReqID);

		} else if (Action.equals("actReject")) {

			////////////////////////////////////////////////////////////////////////////////////////////////////
			//
			// Reject a request and send a message
			//

			String customMsg = EnvTool.getStr(request, "customMsg", "");

			REQ.UpdateStatus(ReqID, RequestStatus.RJCT, SysTool.getCurrentUser(request), true);

			History.Data urhData = URH.new Data();

			urhData.setUreqID(ReqID);
			urhData.setAction(RequestStatus.RJCT);
			urhData.setEditor(SysTool.getCurrentUser(request));
			urhData.setComments(customMsg);

			URH.Insert(urhData);

			// Send message

			Request.Data reqData = REQ.Read(ReqID);

			_send_message(
				ReqID,
				request,
				reqData,
				new WazerContacts(SysTool.getCurrentUser(request)),
				new WazerContacts(reqData.getUser()),
				customMsg
			);

			NextHop = _get_next_hop(Caller);

		} else if (Action.equals("actSolve")) {

			////////////////////////////////////////////////////////////////////////////////////////////////////
			//
			// Set a request as solved and send a message
			//

			String customMsg = EnvTool.getStr(request, "customMsg", "");

			RequestStatus newStatus = EnvTool.getStr(request, "chkRecheck", "").trim().equals("")
				? RequestStatus.DONE
				: RequestStatus.RCHK
			;

			RequestStatus previousStatus = REQ.UpdateStatus(ReqID, newStatus, SysTool.getCurrentUser(request), true);

			History.Data urhData = URH.new Data();

			urhData.setUreqID(ReqID);
			urhData.setAction(newStatus);
			urhData.setEditor(SysTool.getCurrentUser(request));
			urhData.setComments(customMsg);

			URH.Insert(urhData);

			// Send message

			if (!previousStatus.equals(RequestStatus.RCHK)) { // Skip msg if req was already fixed

				Request.Data reqData = REQ.Read(ReqID);

				_send_message(
					ReqID,
					request,
					reqData,
					new WazerContacts(SysTool.getCurrentUser(request)),
					new WazerContacts(reqData.getUser()),
					customMsg
				);
			}

			NextHop = _get_next_hop(Caller);

		} else
			throw new Exception("Unknown Action: '" + Action + "'");

	} catch (Exception e) {

		new MsgTool(session).setAlertText("Internal Error", e.getMessage());
	}

	if (this.DB != null)
		this.DB.destroy();

	response.sendRedirect(NextHop);
%>
