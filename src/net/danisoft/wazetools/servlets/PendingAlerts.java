////////////////////////////////////////////////////////////////////////////////////////////////////
//
// PendingAlerts.java
//
// Servlet to generate mail for older requests not closed
//
// First Release: Aug/2022 by Fulvio Mondini (https://danisoft.software/)
//       Revised: Jan/2024 Moved to V3
//       Revised: Mar/2025 Ported to Waze dslib.jar
//                         Changed to @WebServlet style
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.servlets;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.danisoft.dslib.Database;
import net.danisoft.dslib.FmtTool;
import net.danisoft.dslib.LogTool;
import net.danisoft.dslib.Mail;
import net.danisoft.wazetools.AppCfg;
import net.danisoft.wazetools.ureq.Request;
import net.danisoft.wazetools.ureq.RequestStatus;

@WebServlet(description = "Generate mail for older requests not closed", urlPatterns = { "/servlet/PendingAlerts" })

public class PendingAlerts extends HttpServlet {

	private static final long serialVersionUID = FmtTool.getSerialVersionUID();

	private static final int WAIT_TIME_IN_DAYS = 15;

	private Database _Database;
	public Database getDatabase()		{ return this._Database;	}
	public void setDatabase(Database d)	{ this._Database = d;		}

	/**
	 * Users Class
	 */
	class ReqUsers {

		private String _User;
		private String _Mail;

		// Getters
		public String getUser() { return this._User; }
		public String getMail() { return this._Mail; }

		// Setters
		public void setUser(String user) { this._User = user; }
		public void setMail(String mail) { this._Mail = mail; }

		/**
		 * Constructor
		 */
		public ReqUsers() {
			setUser("");
			setMail("");
		}
	}

	/**
	 * Request Lines Class
	 */
	class ReqLines {

		private int		_ID;			// [0] reqObject.getID()
		private String	_StatusIcon;	// [1] "<span style=\"color:#" + reqObject.getStatus().getCharC() + "; background-color:#" + reqObject.getStatus().getFillC() + ";\">&nbsp;" + reqObject.getStatus().getValue() + "&nbsp;</span>"
		private String	_StatusDesc;	// [2] reqObject.getStatus().getDescr()
		private String	_DateTime;		// [3] FmtTool.fmtDateTime(reqObject.getTimestamp())
		private int		_DaysOld;		// [4] DaysOld
		private String	_UserName;		// [5] reqObject.getUser()
		private int		_UserRank;		// [6] reqObject.getUserRank()
		private String	_UserMail;		// [7] (reqObject.getUserMail().trim().equals("") ? "[no email on file]" : reqObject.getUserMail())
		private String	_Location;		// [8] reqObject.getLocation()
		private String	_UreqLink;		// [9] AppCfg.getServerHomeUrl() + "/unlock/?ReqID=" + reqObject.getID() + "&setLat=" + reqObject.getLat() + "&setLon=" + reqObject.getLon()

		// Getters
		public int		getID()			{ return this._ID;			}
		public String	getStatusIcon()	{ return this._StatusIcon;	}
		public String	getStatusDesc()	{ return this._StatusDesc;	}
		public String	getDateTime()	{ return this._DateTime;	}
		public int		getDaysOld()	{ return this._DaysOld;		}
		public String	getUserName()	{ return this._UserName;	}
		public int		getUserRank()	{ return this._UserRank;	}
		public String	getUserMail()	{ return this._UserMail;	}
		public String	getLocation()	{ return this._Location;	}
		public String	getUreqLink()	{ return this._UreqLink;	}

		// Setters
		public void setID(int i)			{ this._ID = i;			}
		public void setStatusIcon(String s)	{ this._StatusIcon = s;	}
		public void setStatusDesc(String s)	{ this._StatusDesc = s;	}
		public void setDateTime(String s)	{ this._DateTime = s;	}
		public void setDaysOld(int i)		{ this._DaysOld = i;	}
		public void setUserName(String s)	{ this._UserName = s;	}
		public void setUserRank(int i)		{ this._UserRank = i;	}
		public void setUserMail(String s)	{ this._UserMail = s;	}
		public void setLocation(String s)	{ this._Location = s;	}
		public void setUreqLink(String s)	{ this._UreqLink = s;	}

		/**
		 * Constructor
		 */
		public ReqLines() {
			setID(0);
			setStatusIcon("");
			setStatusDesc("");
			setDateTime("");
			setDaysOld(0);
			setUserName("");
			setUserRank(0);
			setUserMail("");
			setLocation("");
			setUreqLink("");
		}
	}

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		Vector<ReqLines> vecLines;
		Vector<ReqUsers> vecUsers;

		try {

			setDatabase(new Database());

			vecUsers = getUsers();

			for (ReqUsers reqUsers : vecUsers) {

				vecLines = getLines(reqUsers.getUser());

				if (vecLines.size() > 0)
					sendMail(request, reqUsers.getMail(), vecLines);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (getDatabase() != null)
			getDatabase().destroy();
	}

	/**
	 * Get list of involved users
	 */
	private Vector<ReqUsers> getUsers() throws Exception {

		Vector<ReqUsers> vecResult = new Vector<ReqUsers>();

		Statement st = getDatabase().getConnection().createStatement();

		ResultSet rs = st.executeQuery(
			"SELECT DISTINCT REQ_ManagedBy, USR_Mail " +
			"FROM " + Request.getTblName() + " " +
				"LEFT JOIN AUTH_users ON REQ_ManagedBy = USR_Name " +
			"WHERE " +
				"REQ_Status = '" + RequestStatus.WORK.getValue() + "' OR " +
				"REQ_Status = '" + RequestStatus.RCHK.getValue() + "' " +
			"ORDER BY REQ_ManagedBy;"
		);

		while (rs.next()) {
			ReqUsers reqUsers = new ReqUsers();
			reqUsers.setUser(rs.getString("REQ_ManagedBy"));
			reqUsers.setMail(rs.getString("USR_Mail"));
			vecResult.add(reqUsers);
		}

		rs.close();
		st.close();

		return(vecResult);
	}

	/**
	 * Get Detail Lines
	 */
	private Vector<ReqLines> getLines(String ManagedBy) throws Exception {

		int DaysOld;
		Request.Data reqData;
		Vector<ReqLines> vecResult = new Vector<ReqLines>();

		Request REQ = new Request(getDatabase().getConnection());

		Statement st = getDatabase().getConnection().createStatement();

		ResultSet rs = st.executeQuery(
			"SELECT REQ_ID, DATEDIFF(NOW(), REQ_Timestamp) AS DaysOld " +
			"FROM " + Request.getTblName() + " " +
			"WHERE " +
				"REQ_ManagedBy = '" + ManagedBy + "' AND " +
				"(REQ_Status = '" + RequestStatus.WORK.getValue() + "' OR REQ_Status = '" + RequestStatus.RCHK.getValue() + "') " +
			"ORDER BY REQ_Timestamp;"
		);

		while (rs.next()) {

			DaysOld = rs.getInt("DaysOld");

			if (DaysOld >= WAIT_TIME_IN_DAYS) {

				reqData = REQ.Read(rs.getInt("REQ_ID"));

				ReqLines reqLines = new ReqLines();

				reqLines.setID(reqData.getID());
				reqLines.setStatusIcon("<span style=\"color:#" + reqData.getStatus().getCharC() + "; background-color:#" + reqData.getStatus().getFillC() + ";\">&nbsp;" + reqData.getStatus().getValue() + "&nbsp;</span>");
				reqLines.setStatusDesc(reqData.getStatus().getDescr());
				reqLines.setDateTime(FmtTool.fmtDateTime(reqData.getTimestamp()));
				reqLines.setDaysOld(DaysOld);
				reqLines.setUserName(reqData.getUser());
				reqLines.setUserRank(reqData.getUserRank());
				reqLines.setUserMail((reqData.getUserMail().trim().equals("") ? "[no email on file]" : reqData.getUserMail()));
				reqLines.setLocation(reqData.getLocation());
				reqLines.setUreqLink(AppCfg.getServerHomeUrl() + "/unlock/?ReqID=" + reqData.getID() + "&setLat=" + reqData.getLat() + "&setLon=" + reqData.getLon());

				vecResult.add(reqLines);
			}
		}

		rs.close();
		st.close();

		return(vecResult);
	}

	/**
	 * Send email
	 */
	private void sendMail(HttpServletRequest request, String mailAddress, Vector<ReqLines> vecLines) {

		Mail MAIL = new Mail(request);

		MAIL.setRecipient(mailAddress);
		MAIL.setSubject("There are still " + vecLines.size() + " user requests handled by you to close/complete");
		MAIL.setHtmlTitle("We found " + vecLines.size() + " user requests managed by you that are still open");

		MAIL.addHtmlBody("<p>You received this email because you are listed as the last editor who handled one or more user requests <b>not yet closed</b> and <b>older than " + WAIT_TIME_IN_DAYS + " days</b></p>");
		MAIL.addHtmlBody("<p>Here are the request(s) that require your attention:</p>");

		for (ReqLines reqLine : vecLines) {

			MAIL.addHtmlBody("<div><b>User Request #" + reqLine.getID() + "</b></div>");
			MAIL.addHtmlBody("<pre>");
			MAIL.addHtmlBody("Status....: <b>" + reqLine.getStatusIcon() + "</b> (" + reqLine.getStatusDesc() + ")");
			MAIL.addHtmlBody("Date/Time.: " + reqLine.getDateTime() + " (<span style=\"color:red\"><b>" + reqLine.getDaysOld() + "</b> days ago</span>)");
			MAIL.addHtmlBody("User......: " + reqLine.getUserName() + "(" + reqLine.getUserRank() + ") - " + reqLine.getUserMail());
			MAIL.addHtmlBody("Location..: " + reqLine.getLocation());
			MAIL.addHtmlBody("UREQ Link.: " + reqLine.getUreqLink());
			MAIL.addHtmlBody("</pre>");
		}

		MAIL.addHtmlBody("<p>Please don't forget to close/complete any pending requests you manage.</p>");
		MAIL.addHtmlBody("<p><i>Thanks for the amazing work you do to keep the map up to date.<br>");
		MAIL.addHtmlBody("Sincerely, the tireless-and-always-working UREQ robot.</i></p>");

		LogTool LOG = new LogTool(getDatabase().getConnection());

		if (MAIL.Send()) {
			LOG.Info(request, LogTool.Category.MAIL, "Mail sent to '" + MAIL.getRecipient() + "' with subject '" + MAIL.getSubject() + "'");
		} else {
			LOG.Error(request, LogTool.Category.MAIL, "Error sending mail to '" + MAIL.getRecipient() + "' with subject '" + MAIL.getSubject() + "': " + MAIL.getLastError());
		}
	}

}
