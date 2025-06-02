////////////////////////////////////////////////////////////////////////////////////////////////////
//
// Request.java
//
// DB Interface for the requests table
//
// First Release: Jan/2013 by Fulvio Mondini (https://danisoft.software/)
//       Revised: Jan/2024 Moved to V3
//       Revised: Feb/2024 CRUD operations changed to ReqObject
//       Revised: Mar/2025 Ported to Waze dslib.jar
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.ureq;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import net.danisoft.dslib.FmtTool;
import net.danisoft.dslib.LogTool;
import net.danisoft.dslib.Mail;
import net.danisoft.dslib.SlackMsg;
import net.danisoft.dslib.SysTool;
import net.danisoft.wtlib.auth.GeoIso;
import net.danisoft.wtlib.auth.User;
import net.danisoft.wtlib.auth.WazerConfig;
import net.danisoft.wtlib.auth.WazerContacts;
import net.danisoft.wazetools.AppCfg;

/**
 * DB Interface for the requests table
 */
public class Request {

	private static final String	TBL_NAME = "UREQ_requests";
	private static final String	DEFAULT_UUID = "00000000-0000-0000-0000-000000000000";
	private static final String	UREQ_SCRIPT_UUID = "d323e155-f555-4006-944f-f8757fa59bb2";

	private static final int	MAX_REQUESTS_TO_SHOW = 1000;
	private static final String MY_AREAS_ONLY_OBJECTNAME = "chkMyAreaOnly";
	private static final String	MY_AREAS_ONLY_COOKIENAME = "ureq-my-area-only";
	private static final String MY_QUEUE_ONLY_OBJECTNAME = "chkMyQueueOnly";
	private static final String	MY_QUEUE_ONLY_COOKIENAME = "ureq-my-queue-only";
	private static final String LAST_STATUSES_COOKIEMASK = "ureq-lastreq-status-{}"; // Substitute "{}" with the status name

	// Getters
	public static String getTblName()				{ return TBL_NAME;					}
	public static String getDefaultUuid()			{ return DEFAULT_UUID;				}
	public static String getUreqScriptUUID()		{ return UREQ_SCRIPT_UUID;			}
	public static int getMaxReqToShow()				{ return MAX_REQUESTS_TO_SHOW;		}
	public static String getMyAreasOnlyObjectName()	{ return MY_AREAS_ONLY_OBJECTNAME;	}
	public static String getMyAreasOnlyCookieName()	{ return MY_AREAS_ONLY_COOKIENAME;	}
	public static String getMyQueueOnlyObjectName()	{ return MY_QUEUE_ONLY_OBJECTNAME;	}
	public static String getMyQueueOnlyCookieName()	{ return MY_QUEUE_ONLY_COOKIENAME;	}
	public static String getLastStatusCookieMask()	{ return LAST_STATUSES_COOKIEMASK;	}

	Connection cn;

	/**
	 * Constructor
	 */
	public Request(Connection conn) {
		this.cn = conn;
	}

	/**
	 * Request Data
	 */
	public class Data {

		// Fields
		private int				_ID;			// `REQ_ID` int NOT NULL AUTO_INCREMENT,
		private Timestamp		_Timestamp;		// `REQ_Timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
		private String			_JsVersion;		// `REQ_JsVersion` varchar(16) NOT NULL DEFAULT '',
		private String			_Environment;	// `REQ_Environment` varchar(16) NOT NULL DEFAULT 'row',
		private String			_Country;		// `REQ_Country` char(2) NOT NULL DEFAULT 'IT',
		private String			_User;			// `REQ_User` varchar(32) NOT NULL DEFAULT '',
		private String			_UserMail;		// `REQ_UserMail` varchar(48) NOT NULL DEFAULT '',
		private int				_UserRank;		// `REQ_UserRank` int NOT NULL DEFAULT '1',
		private int				_Lock;			// `REQ_Lock` int NOT NULL DEFAULT '1',
		private double			_Lat;			// `REQ_Lat` double(9,5) NOT NULL DEFAULT '0.00000',
		private double			_Lon;			// `REQ_Lon` double(9,5) NOT NULL DEFAULT '0.00000',
		private int				_Zoom;			// `REQ_Zoom` int NOT NULL DEFAULT '7',
		private String			_Motivation;	// `REQ_Motivation` varchar(2048) NOT NULL DEFAULT '',
		private String			_Location;		// `REQ_Location` varchar(255) NOT NULL DEFAULT '',
		private String			_Segments;		// `REQ_Segments` varchar(2048) NOT NULL DEFAULT '',
		private String			_Venues;		// `REQ_Venues` varchar(255) NOT NULL DEFAULT '',
		private String			_Cameras;		// `REQ_Cameras` varchar(255) NOT NULL DEFAULT '',
		private boolean			_Resolve;		// `REQ_Resolve` enum('Y','N') NOT NULL DEFAULT 'N' COMMENT 'Y=Must be resolved by LC -- N=Unlock only',
		private RequestStatus	_Status;		// `REQ_Status` enum('OPEN','WORK','INFO','DONE','RJCT','RCHK') NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN=Request is open -- WORK=We are working on it -- INFO=More infos requested to the user -- DONE=Request completed -- RJCT=Request rejected',
		private String			_Notes;			// `REQ_Notes` varchar(2048) NOT NULL DEFAULT '',
		private String			_FollowUp;		// `REQ_FollowUp` enum('M','S','?') NOT NULL DEFAULT '?' COMMENT 'FollowUp Type: M=Mail, S=Slack, ?=Unknown (default)',
		private String			_SolvedBy;		// `REQ_SolvedBy` varchar(32) NOT NULL DEFAULT '',
		private Timestamp		_SolvedDate;	// `REQ_SolvedDate` datetime NOT NULL DEFAULT '1900-01-01 00:00:00',
		private String			_ManagedBy;		// `REQ_ManagedBy` varchar(32) NOT NULL DEFAULT '' COMMENT 'Local Champ in charge for this Unlock Request',
		private Timestamp		_LastUpdate;	// `REQ_LastUpdate` datetime NOT NULL DEFAULT '1900-01-01 00:00:00',
		private String			_UUID;			// `REQ_UUID` varchar(36) NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000' COMMENT 'UUID for QRCode Queries',

		// Getters
		public int				getID()				{ return this._ID;			}
		public Timestamp		getTimestamp()		{ return this._Timestamp;	}
		public String			getJsVersion()		{ return this._JsVersion;	}
		public String			getEnvironment()	{ return this._Environment;	}
		public String			getCountry()		{ return this._Country;		}
		public String			getUser()			{ return this._User;		}
		public String			getUserMail()		{ return this._UserMail;	}
		public int				getUserRank()		{ return this._UserRank;	}
		public int				getLock()			{ return this._Lock;		}
		public double			getLat()			{ return this._Lat;			}
		public double			getLon()			{ return this._Lon;			}
		public int				getZoom()			{ return this._Zoom;		}
		public String			getMotivation()		{ return this._Motivation;	}
		public String			getLocation()		{ return this._Location;	}
		public String			getSegments()		{ return this._Segments;	}
		public String			getVenues()			{ return this._Venues;		}
		public String			getCameras()		{ return this._Cameras;		}
		public boolean			isResolve()			{ return this._Resolve;		}
		public RequestStatus	getStatus()			{ return this._Status;		}
		public String			getNotes()			{ return this._Notes;		}
		public String			getFollowUp()		{ return this._FollowUp;	}
		public String			getSolvedBy()		{ return this._SolvedBy;	}
		public Timestamp		getSolvedDate()		{ return this._SolvedDate;	}
		public String			getManagedBy()		{ return this._ManagedBy;	}
		public Timestamp		getLastUpdate()		{ return this._LastUpdate;	}
		public String			getUUID()			{ return this._UUID;		}
		
		// Setters
		public void setID(int id)							{ this._ID = id;					}
		public void setTimestamp(Timestamp timestamp)		{ this._Timestamp = timestamp;		}
		public void setJsVersion(String jsVersion)			{ this._JsVersion = jsVersion;		}
		public void setEnvironment(String environment)		{ this._Environment = environment;	}
		public void setCountry(String country)				{ this._Country = country;			}
		public void setUser(String user)					{ this._User = user;				}
		public void setUserMail(String userMail)			{ this._UserMail = userMail;		}
		public void setUserRank(int userRank)				{ this._UserRank = userRank;		}
		public void setLock(int lock)						{ this._Lock = lock;				}
		public void setLat(double lat)						{ this._Lat = lat;					}
		public void setLon(double lon)						{ this._Lon = lon;					}
		public void setZoom(int zoom)						{ this._Zoom = zoom;				}
		public void setMotivation(String motivation)		{ this._Motivation = motivation;	}
		public void setLocation(String location)			{ this._Location = location;		}
		public void setSegments(String segments)			{ this._Segments = segments;		}
		public void setVenues(String venues)				{ this._Venues = venues;			}
		public void setCameras(String cameras)				{ this._Cameras = cameras;			}
		public void setResolve(boolean resolve)				{ this._Resolve = resolve;			}
		public void setStatus(RequestStatus status)			{ this._Status = status;			}
		public void setNotes(String notes)					{ this._Notes = notes;				}
		public void setFollowUp(String followUp)			{ this._FollowUp = followUp;		}
		public void setSolvedBy(String solvedBy)			{ this._SolvedBy = solvedBy;		}
		public void setSolvedDate(Timestamp solvedDate)		{ this._SolvedDate = solvedDate;	}
		public void setManagedBy(String managedBy)			{ this._ManagedBy = managedBy;		}
		public void setLastUpdate(Timestamp lastUpdate)		{ this._LastUpdate = lastUpdate;	}
		public void setUUID(String uuid)					{ this._UUID = uuid;				}

		// Status Switches
		public boolean isStatusOpen() { return(this._Status.equals(RequestStatus.OPEN)); }
		public boolean isStatusWork() { return(this._Status.equals(RequestStatus.WORK)); }
		public boolean isStatusInfo() { return(this._Status.equals(RequestStatus.INFO)); }
		public boolean isStatusRjct() { return(this._Status.equals(RequestStatus.RJCT)); }
		public boolean isStatusRchk() { return(this._Status.equals(RequestStatus.RCHK)); }
		public boolean isStatusDone() { return(this._Status.equals(RequestStatus.DONE)); }

		/**
		 * Constructor
		 */
		public Data() {
			super();

			this._ID			= 0;
			this._Timestamp		= FmtTool.DATEZERO;
			this._JsVersion		= "";
			this._Environment	= "";
			this._Country		= "";
			this._User			= "";
			this._UserMail		= "";
			this._UserRank		= 0;
			this._Lock			= 0;
			this._Lat			= 0.0D;
			this._Lon			= 0.0D;
			this._Zoom			= 0;
			this._Motivation	= "";
			this._Location		= "";
			this._Segments		= "";
			this._Venues		= "";
			this._Cameras		= "";
			this._Resolve		= false;
			this._Status		= RequestStatus.UNKN;
			this._Notes			= "";
			this._SolvedBy		= "";
			this._SolvedDate	= FmtTool.DATEZERO;
			this._ManagedBy		= "";
			this._LastUpdate	= FmtTool.DATEZERO;
			this._UUID			= Request.getDefaultUuid();
		}

		/**
		 * Get ManagedBy Span
		 */
		public String getManagedBySpan() {

			String rc = "";
			
			if (!getManagedBy().equals(""))
				rc = FmtTool.fmtDateTimeNoSecs(getLastUpdate()).replace(" ", "&nbsp;") + "<br><i>by&nbsp;" + getManagedBy() + "</i>";

			return(rc);
		}

		/**
		 * Get ClosedBy Span
		 */
		public String getClosedBySpan() {

			String rc = "";
			
			if (isStatusRchk() || isStatusDone())
				if (!getSolvedBy().equals(""))
					rc = FmtTool.fmtDateTimeNoSecs(getSolvedDate()).replace(" ", "&nbsp;") + "<br><i>by&nbsp;" + getSolvedBy() + "</i>";

			return(rc);
		}

		/**
		 * Get RequestRank Span
		 * @param myRank Used to change the color of the background
		 */
		public String getRequestRankSpan(int myRank) {
			return(
				"<span class=\"DS-padding-lfrg-8px DS-padding-updn-0px DS-text-fixed DS-border-full DS-border-round DS-text-bold " +
					"DS-back-" + (myRank < getLock() ? "LightPink" : "PaleGreen") + " " +
					"DS-text-" + (myRank < getLock() ? "FireBrick" : "ForestGreen") + "\" " +
					"title=\"" + (myRank < getLock() ? "The lock is too high for your rank level" : "Unlockable with your rank level") + "\">" +
					getLock() +
				"</span>"
			);
		}

		/**
		 * Generate a ResolveTime colorized span for RCHK and DONE statuses
		 * @param extendedOutput TRUE for more verbose output
		 */
		public String getElapsedTimeSpan(boolean extendedOutput) {

			String rc = "";
			long DiffMs, seconds, minutes, hours, days;

			if (isStatusRchk() || isStatusDone()) {

				if (!getSolvedBy().equals("")) {

					try {

						DiffMs = getSolvedDate().getTime() - getTimestamp().getTime();
						seconds = TimeUnit.MILLISECONDS.toSeconds(DiffMs);
						minutes = TimeUnit.MILLISECONDS.toMinutes(DiffMs); 
						hours = TimeUnit.MILLISECONDS.toHours(DiffMs); 
						days = TimeUnit.MILLISECONDS.toDays(DiffMs);

						rc = "<span>" +
							(extendedOutput
								? (
									(days > 0 ? days + "&nbsp;day" + (days > 1 ? "s" : "") + ",&nbsp;" : "") +
									(hours > 0 ? (hours % 24) + "&nbsp;hour" + (hours > 1 ? "s" : "") + ",&nbsp;" : "") +
									(minutes % 60) + "&nbsp;min&nbsp;&amp;&nbsp;" +
									(seconds % 60) + "&nbsp;sec"
								)
								: (
									(days > 0 ? days + " day(s)<br>" : "") +
									(hours > 0 ? FmtTool.fmtZeroPad(hours % 24, 2) + ":" : "") +
									FmtTool.fmtZeroPad(minutes % 60, 2) + ":" +
									FmtTool.fmtZeroPad(seconds % 60, 2)
								)
							) +
						"</span>";

					} catch (Exception e) {
						rc = "<span class=\"DS-text-exception\">[ERR]</span>";
					}
				}
			}

			return(rc);
		}

		/**
		 * Create a Permalink from the current <Request.Data>
		 */
		public String CreatePermalink() {
			return(_create_permalink("https://www.waze.com/editor/"));
		}

		/**
		 * Create a Permalink (to beta wme) from the current <Request.Data>
		 */
		public String CreatePermalinkBeta() {
			return(_create_permalink("https://beta.waze.com/editor/"));
		}

		/**
		 * Create Permalink (worker)
		 */
		private String _create_permalink(String wmeUrl) {
			
			return(wmeUrl +
				"?env=" + this.getEnvironment() +
				"&lon=" + this.getLon() +
				"&lat=" + this.getLat() +
				"&zoomLevel=" + this.getZoom() +
				(this.getSegments().equals("") ? "" : "&segments=" + this.getSegments()) +
				(this.getVenues().equals("") ? "" : "&venues=" + this.getVenues()) +
				(this.getCameras().equals("") ? "" : "&cameras=" + this.getCameras())
			);
		}

		/**
		 * Generate a Request Summary Table for Dialogs
		 */
		public String getSummaryTable(String caller) {

			WazerContacts authContacts = new WazerContacts(this.getUser());

			String Col1Descr = "";
			String Col1Value = "";

			if (!this.getSegments().equals(""))	{	Col1Descr = "Segment IDs";	Col1Value = this.getSegments().replace(",", " ");	} else
			if (!this.getVenues().equals(""))	{	Col1Descr = "Place ID";		Col1Value = this.getVenues();						} else
			if (!this.getCameras().equals(""))	{	Col1Descr = "Camera ID";	Col1Value = this.getCameras();						} else
			{
				Col1Descr = "Object IDs";
				Col1Value = "<div class=\"DS-text-italic DS-text-exception\"><b>WARNING</b>: Request without a selected segments/venue/camera</div>";
			}

			return(
				"<table class=\"TableSpacing_0px DS-full-width\">" +
					_get_summary_table_row(
						"Status",
						RequestStatus.getColorizedSpan(this.getStatus()) + "&nbsp;" + this.getStatus().getDescr(),
						null,
						(this.isStatusOpen()
							? ""
							: "<span class=\"DS-text-small DS-text-italic DS-text-gray\"><a href=\"../unlock/_actions.jsp?Action=actReopen&ReqID=" + this.getID() + "&Caller=" + caller + "\" tabindex=\"-1\">Reset the status back to <b>" + RequestStatus.OPEN.getValue() + "</b></a></span>"
						)
					) +
					_get_summary_table_row(
						"UUID",
						(this.getUUID().equals(DEFAULT_UUID)
							? "<span class=\"DS-text-italic DS-text-disabled\">Old or Manual Request, no UUID found</span>"
							: "<span class=\"DS-text-fixed-compact\">" + this.getUUID() + "</span>"
						),
						null,
						(this.getUUID().equals(DEFAULT_UUID)
							? "<span class=\"DS-text-small DS-text-disabled\">No History found</span>"
							: "<span class=\"DS-text-small DS-text-italic\"><a href=\"" + AppCfg.getServerHomeUrl() + "/status/by_uuid.jsp?uuid=" + this.getUUID() + "\" target=\"_blank\">Show History</a></span>"
						)
					) +
					_get_summary_table_row(
						"User",
						"<a href=\"" + GetUserProfileLink(this.getUser()) + "\" title=\"Open User Info\" tabindex=\"-1\" target=\"_blank\">" + this.getUser() + "</a>(" + this.getUserRank() + ")",
						_get_user_reliability(this.getUser(), "120px"),
						"<span class=\"DS-text-small DS-text-italic DS-text-" + (authContacts.isEmpty() ? "exception" : "gray") + "\">" +
							"Contact method: <b>" + authContacts.getMethod().name() + "</b></span>"
					) +
					_get_summary_table_row(
						"Created",
						FmtTool.fmtDateTime(this.getTimestamp()) + "<span class=\"DS-text-gray\"> by </span>" +
							(this.getJsVersion().startsWith("W") ? "" : "UREQ Script ") + " " + this.getJsVersion(),
						null,
						null
					) +
					_get_summary_table_row(
						"Location",
						this.getLocation(),
						null,
						null
					) +
					_get_summary_table_row(
						"Lat / Lon",
						this.getLat() + " / " + this.getLon() + " <span class=\"DS-text-gray DS-text-italic\">[ENV:" + this.getEnvironment().toUpperCase() + "]</span>",
						null,
						"<span class=\"DS-text-small DS-text-italic\">" +
							"<a href=\"https://maps.google.com/maps?q=loc:" + this.getLat() + "," + this.getLon() + "\" tabindex=\"-1\" target=\"_blank\">Google Maps</a>" +
							"&nbsp;|&nbsp;" +
							"<a href=\"" + this._get_livemap_permalink() + "\" tabindex=\"-1\" target=\"_blank\">Waze LiveMap</a>" +
							"&nbsp;|&nbsp;" +
							"<a href=\"" + this.CreatePermalink() + "\" tabindex=\"-1\" target=\"_blank\">WME</a>" +
							"</span>"
					) +
					_get_summary_table_row(
						Col1Descr,
						Col1Value,
						null,
						null
					) +
					_get_summary_table_row(
						"Lock Level",
						"Items locked at no more than <b>level " + this.getLock() + "</b>",
						null,
						null
					) +
					_get_summary_table_row(
						"Reason",
						"<div class=\"DS-text-fixed-compact\" style=\"word-break: break-all;\">" + this.getMotivation().replace("\n", "<br>") + "</div>",
						null,
						null
					) +
					_get_summary_table_row(
						"Method",
						"<table class=\"TableSpacing_0px\">" +
							"<tr>" +
								"<td class=\"DS-padding-top-2px\">" +
									"<span class=\"material-icons\" style=\"color: #" + (this.isResolve() ? "aa0000" : "999999") + "\">" +
										(this.isResolve() ? "edit_location" : "lock_open") +
									"</span>" +
								"</td>" +
								"<td class=\"DS-padding-lfrg-4px\">" + (
										this.isResolve()
										? "<b>UNLOCK & SOLVE</b> - The user asks that <b>you</b> fix the problem"
										: "<b>UNLOCK ONLY</b> - The user wants to fix the problem himself"
									) +
								"</td>" +
							"</tr>" +
						"</table>",
						null,
						null
					) +
					_get_summary_table_row(
						"Last Update",
						(this.getManagedBy().trim().equals("")
							? "<span class=\"DS-text-italic DS-text-disabled\">Original request, no updates found</span>"
							: FmtTool.fmtDateTime(this.getLastUpdate()) + " <span class=\"DS-text-disabled\">by</span> " + this.getManagedBy()
						),
						null,
						null
					) +
				"</table>"
			);
		}

		/**
		 * Create a LiveMap Permalink from this <b>Request.Data</b> request
		 */
		private String _get_livemap_permalink() {
			return("https://www.waze.com/live-map/directions?latlng=" + this.getLat() + "," + this.getLon());
		}

		/**
		 * Create a ROW for CreateSummaryTable()
		 * @param Col3 2 TD if set to null, 3 TD otherwise
		 * @param Col4 2 TD if set to null, 3 TD otherwise
		 */
		private String _get_summary_table_row(String Col1, String Col2, String Col3, String Col4) {

			String defaultPadding = "DS-padding-lfrg-8px DS-padding-updn-4px";

			return(
				"<tr class=\"DS-border-full\">" +
					"<td class=\"" + defaultPadding + " DS-back-gray DS-text-black DS-text-italic DS-border-rg\" align=\"center\" nowrap>" + Col1 + "</td>" +
					"<td class=\"DS-padding-0px\">" +
						"<table class=\"TableSpacing_0px DS-full-width\">" +
							"<tr class=\"DS-border-none\">" +
								"<td class=\"" + defaultPadding + "\" align=\"left\">" + Col2 + "</td>" +
								(Col3 == null ? "" : "<td class=\"" + defaultPadding + "\" align=\"left\">" + Col3 + "</td>") +
								(Col4 == null ? "" : "<td class=\"" + defaultPadding + "\" align=\"right\">" + Col4 + "</td>") +
							"</tr>" +
						"</table>" +
					"</td>" +
				"</tr>"
			);
		}

		/**
		 * Get a user's reliability table and script
		 */
		private String _get_user_reliability(String userName, String widthParam) {

			String rc = "";

			try {

				int totOpen = 0;
				int totInfo = 0;
				int totRjct = 0;
				int totDone = 0;

				String dateMin = "";
				String dateMax = "";

				Statement st = Request.this.cn.createStatement();
				ResultSet rs = null;

				// First Request Date

				rs = st.executeQuery(
					"SELECT REQ_Timestamp FROM " + TBL_NAME + " WHERE REQ_User = '" + userName + "' ORDER BY REQ_Timestamp ASC LIMIT 1;"
				);

				if (rs.next())
					dateMin = FmtTool.fmtDate(rs.getTimestamp("REQ_Timestamp"));

				rs.close();
				
				// Last Request Date

				rs = st.executeQuery(
					"SELECT REQ_Timestamp FROM " + TBL_NAME + " WHERE REQ_User = '" + userName + "' ORDER BY REQ_Timestamp DESC LIMIT 1;"
				);

				if (rs.next())
					dateMax = FmtTool.fmtDate(rs.getTimestamp("REQ_Timestamp"));

				rs.close();

				// Open

				rs = st.executeQuery(
					"SELECT COUNT(*) AS RecNo FROM " + TBL_NAME + " WHERE REQ_User = '" + userName + "' AND (REQ_Status = 'OPEN' OR REQ_Status = 'WORK');"
				);

				if (rs.next())
					totOpen = rs.getInt("RecNo");

				rs.close();
				
				// Info

				rs = st.executeQuery(
					"SELECT COUNT(*) AS RecNo FROM " + TBL_NAME + " WHERE REQ_User = '" + userName + "' AND (REQ_Status = 'INFO');"
				);

				if (rs.next())
					totInfo = rs.getInt("RecNo");

				rs.close();
				
				// Rejected

				rs = st.executeQuery(
					"SELECT COUNT(*) AS RecNo FROM " + TBL_NAME + " WHERE REQ_User = '" + userName + "' AND (REQ_Status = 'RJCT');"
				);

				if (rs.next())
					totRjct = rs.getInt("RecNo");

				rs.close();
				
				// Done

				rs = st.executeQuery(
					"SELECT COUNT(*) AS RecNo FROM " + TBL_NAME + " WHERE REQ_User = '" + userName + "' AND (REQ_Status = 'RCHK' OR REQ_Status = 'DONE');"
				);

				if (rs.next())
					totDone = rs.getInt("RecNo");

				rs.close();

				st.close();

				int totReqs = totOpen + totInfo + totRjct + totDone;

				int percOpen = (totOpen * 100) / totReqs;
				int percInfo = (totInfo * 100) / totReqs;
				int percRjct = (totRjct * 100) / totReqs;
				int percDone = 100 - percOpen - percInfo - percRjct;

				String dlgPercOpen = FmtTool.fmtAmount2dPerc((totOpen * 100.0D) / totReqs);
				String dlgPercInfo = FmtTool.fmtAmount2dPerc((totInfo * 100.0D) / totReqs);
				String dlgPercRjct = FmtTool.fmtAmount2dPerc((totRjct * 100.0D) / totReqs);
				String dlgPercDone = FmtTool.fmtAmount2dPerc(100.0D - ((totOpen * 100.0D) / totReqs) - ((totInfo * 100.0D) / totReqs) - ((totRjct * 100.0D) / totReqs));

				String dialogHead = userName + " Reliability";

				String dialogBody =
					"<center>" +
						"<table class=\"TableSpacing_0px\">" +
							"<tr class=\"DS-border-dn\">" +
								"<td class=\"DS-padding-lfrg-4px DS-padding-updn-4px\" align=\"center\" ColSpan=\"3\">" +
									"<div class=\"DS-text-large\">Period: <span class=\"DS-text-bold\">" + dateMin + " &rarr; " + dateMax + "</span></div>" +
								"</td>" +
							"</tr>" +
							"<tr class=\"DS-border-none\">" +
								"<td class=\"DS-text-fixed DS-text-bold DS-text-GoldenRod DS-padding-lfrg-4px DS-padding-updn-2px\" align=\"right\">" + totOpen + "</td>" +
								"<td class=\"DS-padding-lfrg-2px DS-padding-updn-4px\" align=\"left\">requests still pending</td>" +
								"<td class=\"DS-text-italic DS-text-gray DS-padding-lfrg-2px DS-padding-updn-4px\" align=\"right\">" + dlgPercOpen + "</td>" +
							"</tr>" +
							"<tr class=\"DS-border-none\">" +
								"<td class=\"DS-text-fixed DS-text-bold DS-text-green DS-padding-lfrg-4px DS-padding-updn-2px\" align=\"right\">" + totDone + "</td>" +
								"<td class=\"DS-padding-lfrg-2px DS-padding-updn-4px\" align=\"left\">requests accepted and resolved</td>" +
								"<td class=\"DS-text-italic DS-text-gray DS-padding-lfrg-2px DS-padding-updn-4px\" align=\"right\">" + dlgPercDone + "</td>" +
							"</tr>" +
							"<tr class=\"DS-border-none\">" +
								"<td class=\"DS-text-fixed DS-text-bold DS-text-blue DS-padding-lfrg-4px DS-padding-updn-2px\" align=\"right\">" + totInfo + "</td>" +
								"<td class=\"DS-padding-lfrg-2px DS-padding-updn-4px\" align=\"left\">requests awaiting response from the user</td>" +
								"<td class=\"DS-text-italic DS-text-gray DS-padding-lfrg-2px DS-padding-updn-4px\" align=\"right\">" + dlgPercInfo + "</td>" +
							"</tr>" +
							"<tr class=\"DS-border-none\">" +
								"<td class=\"DS-text-fixed DS-text-bold DS-text-darkred DS-padding-lfrg-4px DS-padding-updn-2px\" align=\"right\">" + totRjct + "</td>" +
								"<td class=\"DS-padding-lfrg-2px DS-padding-updn-4px\" align=\"left\">rejected requests</td>" +
								"<td class=\"DS-text-italic DS-text-gray DS-padding-lfrg-2px DS-padding-updn-4px\" align=\"right\">" + dlgPercRjct + "</td>" +
							"</tr>" +
						"</table>" +
					"</center>"
				;

				rc =
					"<script>" +
						"function ShowReliability(open, done, info, rjct) {" +
							"ShowDialog_OK(" +
								"'" + dialogHead + "'," +
								"'" + dialogBody + "'," +
								"'OK'" +
							");" +
						"}" +
					"</script>" +
					"<table class=\"TableSpacing_0px DS-cursor-pointer\" onClick=\"ShowReliability(" + percOpen + ", " + percDone + ", " + percInfo + ", " + percRjct + ");\" width=\"" + widthParam + "\">" +
						"<tr class=\"DS-border-full\">" +
							"<td class=\"DS-padding-lfrg-0px DS-padding-updn-4px DS-border-none DS-back-LemonChiffon\" title=\"Open: " + percOpen + "%\" width=\"" + percOpen + "%\">" + "</td>" +
							"<td class=\"DS-padding-lfrg-0px DS-padding-updn-4px DS-border-none DS-back-LimeGreen\" title=\"Done: " + percDone + "%\" width=\"" + percDone + "%\">" + "</td>" +
							"<td class=\"DS-padding-lfrg-0px DS-padding-updn-4px DS-border-none DS-back-Violet\" title=\"Wait: " + percInfo + "%\" width=\"" + percInfo + "%\">" + "</td>" +
							"<td class=\"DS-padding-lfrg-0px DS-padding-updn-4px DS-border-none DS-back-OrangeRed\" title=\"Rjct: " + percRjct + "%\" width=\"" + percRjct + "%\">" + "</td>" +
						"</tr>" +
					"</table>"
				;

			} catch (Exception e) {
				System.err.println("Request._get_user_reliability(): " + e.toString());
				rc = e.getMessage();
			}

			return(rc);
		}

	}

	/**
	 * Get the MyAreasOnly flag from cookies
	 */
	public static boolean isMyAreasOnly(Cookie[] allCookies) {

		boolean rc = false;

		try {

			for (Cookie cookie : allCookies) {
				if (cookie.getName().equals(getMyAreasOnlyCookieName()))
					rc = cookie.getValue().equalsIgnoreCase("true");
			}

		} catch (Exception e) { }

		return(rc);
	}

	/**
	 * Get the MyQueueOnly flag from cookies
	 */
	public static boolean isMyQueueOnly(Cookie[] allCookies) {

		boolean rc = false;

		try {

			for (Cookie cookie : allCookies) {
				if (cookie.getName().equals(getMyQueueOnlyCookieName()))
					rc = cookie.getValue().equalsIgnoreCase("true");
			}

		} catch (Exception e) { }

		return(rc);
	}

	/**
	 * Read a Request
	 */
	public Data Read(int ReqID) {
		return(
			_read_obj_by_id(ReqID)
		);
	}

	/**
	 * Insert a new Request
	 * @return LAST_INSERT_ID()
	 * @throws Exception
	 */
	public int Insert(Data data) throws Exception {

		int LastID = 0;

		Statement st = this.cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = st.executeQuery("SELECT * FROM " + TBL_NAME + " LIMIT 1");

		rs.moveToInsertRow();

		_update_rs_from_obj(rs, data);

		rs.insertRow();
		rs.close();

		// Last inserted ID

		rs = st.executeQuery("SELECT LAST_INSERT_ID() AS LastID");

		if (rs.next())
			LastID = rs.getInt("LastID");

		rs.close();
		st.close();
		
		return(LastID);
	}

	/**
	 * Update a Request record
	 * @throws Exception
	 */
	public void Update(int ReqID, Data data) throws Exception {

		Statement st = this.cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = st.executeQuery("SELECT * FROM " + TBL_NAME + " WHERE REQ_ID = '" + ReqID + "';");

		if (rs.next()) {

			_update_rs_from_obj(rs, data);

			rs.updateRow();

		} else
			throw new Exception("Request.Update(): ReqID " + ReqID + " NOT found");

		rs.close();
		st.close();
	}

	/**
	 * Update Notes Fields
	 * @throws Exception
	 */
	public void UpdateNotes(int ReqID, String user, String notes) throws Exception {

		Data data = _read_obj_by_id(ReqID);

		if (!data.getNotes().equals(notes)) { // Update only if changed

			data.setNotes(notes);
			data.setManagedBy(user);
			data.setLastUpdate(new Timestamp(new java.util.Date().getTime()));

			Update(ReqID, data);
		}
	}

	/**
	 * Set request status
	 * @return Previous RequestStatus (before the change)
	 * @throws Exception
	 */
	public RequestStatus UpdateStatus(int ReqID, RequestStatus status, String user, boolean UpdateSolvedByFieldToo) throws Exception {

		Data data = _read_obj_by_id(ReqID);

		RequestStatus prevRequestStatus = data.getStatus();

		data.setStatus(status);
		data.setManagedBy(user);
		data.setLastUpdate(new Timestamp(new java.util.Date().getTime()));

		if (UpdateSolvedByFieldToo) {
			data.setSolvedBy(user);
			data.setSolvedDate(new Timestamp(new java.util.Date().getTime()));
		}

		Update(ReqID, data);

		return(prevRequestStatus);
	}

	/**
	 * Return all requests for a given user id
	 * @return Vector<Request.Data> of results 
	 */
	public Vector<Data> getAll(String user) {
		return(
			_fill_req_vector("SELECT * FROM " + TBL_NAME + " WHERE REQ_User = '" + user + "' ORDER BY REQ_Timestamp DESC;")
		);
	}

	/**
	 * Return all requests for a given user id in a given portion of time
	 * @return Vector<Request.Data> of results 
	 */
	public Vector<Data> getAll(String user, String dateMin, String dateMax) {
		return(
			_fill_req_vector(
				"SELECT * FROM " + TBL_NAME + " " +
				"WHERE REQ_User = '" + user + "' AND REQ_Timestamp BETWEEN '" + dateMin + "' AND '" + dateMax + "' " +
				"ORDER BY REQ_Timestamp DESC;"
			)
		);
	}

	/**
	 * Return all requests based on the given parameters
	 * @param isoCountry ISO 2-letter code Country
	 * @param userName All country if empty string, else limited to given user's areas
	 * @return Vector<Request.Data> of results 
	 */
	@SuppressWarnings("unused")
	public Vector<Data> getAll(String isoCountry, String userName, boolean showOpen, boolean showWork, boolean showInfo, boolean showRjct, boolean showRchk, boolean showDone) {

		String WHERE = "", tmpArea = "", tmpType = "";

		// Add Country Filter

		if (isoCountry != null)
			if (!isoCountry.equals(""))
				WHERE += "(REQ_Country = '" + isoCountry + "')";

		// Add Type Filter

		if (showOpen)
			tmpType += (
				(tmpType.equals("") ? "" : " OR ") +
				"REQ_Status = '" + RequestStatus.OPEN.getValue() + "'"
			);

		if (showWork)
			tmpType += (
				(tmpType.equals("") ? "" : " OR ") +
				"REQ_Status = '" + RequestStatus.WORK.getValue() + "'"
			);

		if (showInfo)
			tmpType += (
				(tmpType.equals("") ? "" : " OR ") +
				"REQ_Status = '" + RequestStatus.INFO.getValue() + "'"
			);
/*
		if (showRjct)
			tmpType += (
				(tmpType.equals("") ? "" : " OR ") +
				"(REQ_Status = '" + RequestStatus.RJCT.getValue() + "' AND DATE_SUB(CURDATE(),INTERVAL " + getRjctMaxLimit() + " DAY) <= REQ_Timestamp)"
			);
*/		
		if (showRchk)
			tmpType += (
				(tmpType.equals("") ? "" : " OR ") +
				"REQ_Status = '" + RequestStatus.RCHK.getValue() + "'"
			);
/*
		if (showDone)
			tmpType += (
				(tmpType.equals("") ? "" : " OR ") +
				"(REQ_Status = '" + RequestStatus.DONE.getValue() + "' AND DATE_SUB(CURDATE(),INTERVAL " + getDoneMaxLimit() + " DAY) <= REQ_Timestamp)"
			);
*/
		WHERE += (tmpType.equals("") ? "" : " AND (" + tmpType + ")");

		// Add UserArea Filter

		if (!userName.equals("")) {

			int AreaCount = 0;
			Area AREA = new Area(this.cn);
			Vector<Area.Data> vecData = AREA.getAll(userName);

			for (Area.Data data : vecData) {

				tmpArea += (AreaCount == 0 ? "" : "OR ") +
					"MBRContains(" +
						"ST_GeomFromText('" + data.getArea() + "')," +
						"ST_GeomFromText(" +
							"CONCAT('POINT(', REQ_Lon, ' ', REQ_Lat, ')')" +
						")" +
					")";

				AreaCount++;
			}

			WHERE += (tmpArea.equals("") ? "" : " AND (" + tmpArea + ")");
		}

		// Query DB

		return(
			_fill_req_vector("SELECT * FROM " + TBL_NAME + " WHERE (" + WHERE + ");")
		);
	}

	/**
	 * Return the most recent requests<br>
	 * @param isoCountry: Country Prefix or "" for ALL countries
	 * @param userName All country if empty string, else limited to given user's areas
	 * @return Vector<Request.Data> of results 
	 */
	public Vector<Data> getRecent(String isoCountry, String userName, String dateMin, String dateMax) {

		String WHERE = "";

		// Country filter

		if (!isoCountry.equals(""))
			WHERE += "(REQ_Country = '" + isoCountry + "')";

		// Area filter

		if (!userName.equals("")) {

			String tmpArea = "";
			Area AREA = new Area(this.cn);
			Vector<Area.Data> vecData = AREA.getAll(userName);

			for (Area.Data data : vecData) {

				tmpArea += (tmpArea.equals("") ? "" : " OR ") +
					"MBRContains(" +
						"ST_GeomFromText('" + data.getArea() + "')," +
						"ST_GeomFromText(" +
							"CONCAT('POINT(', REQ_Lon, ' ', REQ_Lat, ')')" +
						")" +
					")";
			}

			WHERE += (tmpArea.equals("")
				? ""
				: (WHERE.equals("") ? "" : " AND ") + "(" + tmpArea + ")"
			);
		}

		if (!dateMin.equals("") && !dateMax.equals(""))
			WHERE += 
				(WHERE.equals("") ? "" : " AND ") +
				"(REQ_Timestamp BETWEEN '" + dateMin + "' AND '" + dateMax + "')";

		// Query

		String QUERY = "SELECT * FROM " + TBL_NAME + (WHERE.trim().equals("") ? "" : " WHERE " + WHERE) + " ORDER BY REQ_Timestamp DESC;";

		return(_fill_req_vector(QUERY));
	}

	/**
	 * Return the recheck queue
	 * @param User The user who solved the UR, or "ALL" for everyone
	 * @return Vector<Request.Data> of results 
	 */
	public Vector<Data> getRecheckQueue(String Country, String User) {
		return(
			_fill_req_vector(
				"SELECT * FROM " + TBL_NAME + " " +
				"WHERE " +
					"REQ_Country = '" + Country + "' AND (" +
						"REQ_Status = '" + RequestStatus.RCHK.toString() + "' OR " +
						"REQ_Status = '" + RequestStatus.WORK.toString() + "' OR " +
						"REQ_Status = '" + RequestStatus.INFO.toString() + "'" +
					")" +
					(User == null ? "" : " AND (REQ_ManagedBy = '" + User + "' OR REQ_SolvedBy = '" + User + "') ") +
				"ORDER BY REQ_Timestamp"
			)
		);
	}

	/**
	 * Return ID of the request by UUID<br>
	 * @param ReqUUID: Request UUID
	 * @return The RequestID or 0 (zero) if not found
	 */
	public int getIdByUuid(String ReqUUID) {

		int rc = 0;

		if (!ReqUUID.equals(DEFAULT_UUID)) { // Skip zero uuid

			try {

				Statement st = this.cn.createStatement();
				ResultSet rs = st.executeQuery("SELECT REQ_ID FROM " + TBL_NAME + " WHERE REQ_UUID = '" + ReqUUID + "';");

				if (rs.next())
					rc = rs.getInt("REQ_ID");

				rs.close();
				st.close();

			} catch (Exception e) { }
		}

		return(rc);
	}

	/**
	 * Count requests by seg lock level
	 * @return rs.getInt("SegLock") + SysTool.getDelimiter() + rs.getInt("SegCount")
	 */
	public Vector<String> countByLock() {

		Vector<String> vecResults = new Vector<>();

		try {

			Statement st = this.cn.createStatement();

			ResultSet rs = st.executeQuery(
				"SELECT REQ_Lock AS SegLock, COUNT(*) AS SegCount " +
				"FROM " + TBL_NAME + " " +
				"GROUP BY REQ_Lock " +
				"ORDER BY SegCount DESC;"
			);

			while (rs.next())
				vecResults.addElement(rs.getInt("SegLock") + SysTool.getDelimiter() + rs.getInt("SegCount"));

			rs.close();
			st.close();

		} catch (Exception e) {
			System.err.println("Request.countByLock(): " + e.toString());
		}

		return(vecResults);
	}

	/**
	 * Count requests by Unlocker
	 * @return rs.getString("Unlocker") + SysTool.getDelimiter() + rs.getInt("ReqCount")
	 */
	public Vector<String> countByUnlocker() {

		Vector<String> vecResults = new Vector<>();

		try {

			Statement st = this.cn.createStatement();

			ResultSet rs = st.executeQuery(
				"SELECT REQ_ManagedBy AS Unlocker, COUNT(*) AS ReqCount " +
				"FROM " + TBL_NAME + " " +
				"WHERE REQ_ManagedBy <> '' " +
				"GROUP BY REQ_ManagedBy " +
				"ORDER BY ReqCount DESC;"
			);

			while (rs.next())
				vecResults.addElement(rs.getString("Unlocker") + SysTool.getDelimiter() + rs.getInt("ReqCount"));

			rs.close();
			st.close();

		} catch (Exception e) {
			System.err.println("Request.countByUnlocker(): " + e.toString());
		}

		return(vecResults);
	}

	/**
	 * Count requests by Year
	 * @return rs.getInt("ReqYear") + SysTool.getDelimiter() + rs.getInt("ReqCount")
	 */
	public Vector<String> countByYear() {

		Vector<String> vecResults = new Vector<>();

		try {
			
			Statement st = this.cn.createStatement();

			ResultSet rs = st.executeQuery(
				"SELECT YEAR(REQ_Timestamp) AS ReqYear, COUNT(*) AS ReqCount " +
				"FROM " + TBL_NAME + " " +
				"GROUP BY YEAR(REQ_Timestamp);"
			);

			while (rs.next())
				vecResults.addElement(rs.getInt("ReqYear") + SysTool.getDelimiter() + rs.getInt("ReqCount"));

			rs.close();
			st.close();

		} catch (Exception e) {
			System.err.println("Request.countByYear(): " + e.toString());
		}

		return(vecResults);
	}

	/**
	 * Count requests in last 12 months
	 * @return rs.getString("ReqMonth") + SysTool.getDelimiter() + rs.getInt("ReqCount")
	 */
	public Vector<String> countByMonth() {

		Vector<String> vecResults = new Vector<>();

		try {

			Statement st = this.cn.createStatement();

			ResultSet rs = st.executeQuery(
				"SELECT ReqMonth, ReqCount FROM (" +
					"SELECT YEAR(REQ_Timestamp) AS iYear, MONTH(REQ_Timestamp) AS iMonth, DATE_FORMAT(REQ_Timestamp, '%b/%y') AS ReqMonth, COUNT(*) AS ReqCount " +
					"FROM " + TBL_NAME + " " +
					"GROUP BY iYear, iMonth, ReqMonth " +
					"ORDER BY iYear DESC, iMonth DESC " +
					"LIMIT 12" +
				") AS CountByMonth " +
				"ORDER BY iYear, iMonth"
			);

			while (rs.next())
				vecResults.addElement(rs.getString("ReqMonth") + SysTool.getDelimiter() + rs.getInt("ReqCount"));

			rs.close();
			st.close();

		} catch (Exception e) {
			System.err.println("Request.countByMonth(): " + e.toString());
		}

		return(vecResults);
	}

	/**
	 * Get Vector of ISO-2char active countries for a given user
	 */
	public Vector<String> getActvCtryIso2(String userName) {

		User USR = new User(this.cn);
		GeoIso GEO = new GeoIso(this.cn);

		GeoIso.Data geoData;
		User.Data usrData = USR.Read(userName);
		WazerConfig wazerConfig = usrData.getWazerConfig();
		JSONArray jaActvCtry = wazerConfig.getUreq().getActiveCountries();
		Vector<String> vecCountries = new Vector<String>();

		for (int i = 0; i < jaActvCtry.length(); i++) {

			try {
				geoData = GEO.Read(jaActvCtry.getString(i));
				vecCountries.add(geoData.getIso2());
			} catch (Exception e) {
				System.err.println("Request.getActiveCountries('" + userName + "'): " + e.toString());
			}
		}

		return(vecCountries);
	}

	/**
	 * Get Active Countries Combo
	 */
	public String getCountriesCombo(String selected) {

		String CouIso2 = "", CouIso3 = "", CouName = "", Results = "";
		
		try {

			Statement st = this.cn.createStatement();

			ResultSet rs = st.executeQuery(
				"SELECT DISTINCT " +
					"REQ_Country AS CountryIso2, " +
					"GEO_Code AS CountryIso3, " +
					"IFNULL(GEO_Name, 'Description to be fixed in a future release') AS CountryName " +
				"FROM " + TBL_NAME + " " +
					"LEFT JOIN AUTH_geo ON REQ_Country = GEO_Iso2 " +
				"WHERE LENGTH(GEO_Code) = 3 " +
				"ORDER BY REQ_Country;"
			);

			Results += "<option value=\"\"" + (selected.equals("") ? " selected" : "") + "></option>";

			while (rs.next()) {

				CouIso2 = rs.getString("CountryIso2");
				CouIso3 = rs.getString("CountryIso3");
				CouName = rs.getString("CountryName");

				Results +=
					"<option iso3=\"" + CouIso3 + "\" value=\"" + CouIso2 + "\"" + (selected.equals(CouIso2) ? " selected" : "") + ">" +
						CouName +
					"</option>"
				;
			}

			rs.close();
			st.close();

		} catch (Exception e) {
			Results = "<option value=\"\" selected>" + e.toString() + "</option>";
		}

		return(Results);
	}

	/**
	 * Create a User Profile Link from the user nick
	 */
	public String GetUserProfileLink(String user) {
		return("https://www.waze.com/user/editor/" + user);
	}

	/**
	 * Send e-mail alert
	 */
	public void SendMailAlert(HttpServletRequest request, int LastReqID) {

		final String WINDOG_FAKE_TARGET_EMAIL = "fmondini@danisoft.net"; // Force to this mail on windog

		Area AREA = new Area(this.cn);
		Alert ALR = new Alert(this.cn);
		User USR = new User(this.cn);
		
		Vector<String> usrVect = new Vector<String>();

		Data reqData;
		Alert.Data alrData;
		Area.Data auaData;

		try {

			reqData = Read(LastReqID);

			//
			// Check all users for whole country
			//

			Vector<User.Data> vecUsrData = USR.getAll();

			for (User.Data usrData : vecUsrData) {

				try {

					alrData = ALR.Read(usrData.getName());
					
					if (alrData.isEmlActive())
						if (alrData.getEmlCountry().equals(reqData.getCountry()))
							if (alrData.isEmlAllCountry())
								if (alrData.isEmlOnCreate())
									usrVect.addElement(alrData.getUser());

				} catch (Exception e) { }
			}

			//
			// Check areas for point location
			//

			try {

				Vector<Integer> vecAreaId = AREA.getByPoint(reqData.getLat(), reqData.getLon());

				for (int AreaID : vecAreaId) {

					auaData = AREA.Read(AreaID);

					try {

						alrData = ALR.Read(auaData.getUser());

						if (alrData.isEmlActive())
							if (alrData.isEmlOnCreate())
								usrVect.addElement(alrData.getUser());

					} catch (Exception e) { }
				}

			} catch (Exception e) {
				System.err.println(e.toString());
			}

			//
			// Send email
			//

			HashSet<String> hashSet = new HashSet<>(usrVect); // HashSet keeps only unique elements by default
			usrVect = new Vector<>(hashSet);
			String userMail = "";

			for (String usrName : usrVect) {

				userMail = USR.getMailByUser(usrName);

				if (!userMail.trim().equals("")) {

					try {

						Mail MAIL = new Mail(request);

						MAIL.setHtmlTitle("A new " + (reqData.isResolve() ? "resolve" : "unlock") + " request has been created");
						MAIL.setSubject("New " + (reqData.isResolve() ? "resolve" : "unlock") + " request in " + reqData.getLocation());

						MAIL.addHtmlBody("<pre>");
						MAIL.addHtmlBody("Generator: <b>" + reqData.getJsVersion() + "</b>");
						MAIL.addHtmlBody("Requester: <b>" + reqData.getUser() + "</b>(" + reqData.getUserRank() + ")");
						MAIL.addHtmlBody("Location.: <b>" + reqData.getLocation() + "</b>");
						MAIL.addHtmlBody("Segm.Lock: <b>" + reqData.getLock() + "</b>");
						MAIL.addHtmlBody("Comments.: <b>" + reqData.getMotivation() + "</b>");
						MAIL.addHtmlBody("UREQ Link: <b><a href=\"" + AppCfg.getServerHomeUrl() + "/unlock/?ReqID=" + reqData.getID() + "&setLat=" + reqData.getLat() + "&setLon=" + reqData.getLon() + "\">Click here</a></b> to open this request in UREQ");
						MAIL.addHtmlBody("</pre>");
						MAIL.addHtmlBody("You can change your UREQ alerts settings from <a href=\"" + AppCfg.getServerHomeUrl() + "/tools/alerts.jsp\">this page</a>.");

						MAIL.setRecipient(SysTool.isWindog()
							? WINDOG_FAKE_TARGET_EMAIL
							: userMail
						);

						LogTool LOG = new LogTool(this.cn);

						if (MAIL.Send()) {
							LOG.Info(request, LogTool.Category.MAIL, "Mail sent to '" + MAIL.getRecipient() + "' with subject '" + MAIL.getSubject() + "'");
						} else {
							LOG.Error(request, LogTool.Category.MAIL, "Error sending mail to '" + MAIL.getRecipient() + "' with subject '" + MAIL.getSubject() + "': " + MAIL.getLastError());
							throw new Exception("Mail Error: " + MAIL.getLastError());
						}

					} catch (Exception e) {
						// Error reading user, skip it
						System.err.println("Request.SendMailAlert(): " + e.toString());						
					}

				} else
					System.err.println("Request.SendMailAlert(): user '" + usrName + " has NO mail");
			}

		} catch (Exception e) {
			System.err.println("Request.SendMailAlert(): " + e.toString());
		}
	}

	/**
	 * Post request to a Slack channel
	 */
	public void SlackSendToChannel(int LastReqID) {

		final String CHANNEL_NAME = "ureq_alert"; // WazeTools_bot

		try {

			Data reqData = Read(LastReqID);

			String MessageText = 
				"*A new " + (reqData.isResolve() ? "resolve" : "unlock") + " request has been created by " + reqData.getUser() + "(" + reqData.getUserRank() + ")*\n" +
				"\n" +
				"`LocFilter: " + reqData.getLocation() + "`\n" +
				"`Generator: " + reqData.getJsVersion() + "`\n" +
				"`Segm.Lock: " + reqData.getLock() + "`\n" +
				"\n" +
				reqData.getMotivation() + "\n" +
				"\n" +
				"*UREQ Link*: " + AppCfg.getServerHomeUrl() + "/unlock/?ReqID=" + reqData.getID() + "&setLat=" + reqData.getLat() + "&setLon=" + reqData.getLon() + "\n"
			;

			if (SysTool.isWindog()) {

				System.out.println("[FAKE] Request.SendSlackAlert(): Not sent under windog. MessageText:\n-=[ MSG START ]=-\n" + MessageText + "\n-=[ MSG -END- ]=-");

			} else {

				SlackMsg SLM = new SlackMsg();

				JSONObject jResult = SLM.BroadcastToChannel(
					CHANNEL_NAME,
					AppCfg.getSlackBotName(),
					MessageText
				);

				if (jResult.getInt("rc") != HttpServletResponse.SC_OK)
					throw new Exception(jResult.getString("error"));
			}

		} catch (Exception e) {

			System.err.println("Request.SlackSendToChannel(): " + e.toString());
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// +++ PRIVATE +++
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Read REQ Record based on given ID
	 * @return <Request.Data> result 
	 */
	private Data _read_obj_by_id(int ReqID) {

		Data data = new Data();

		try {

			Statement st = this.cn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM " + TBL_NAME + " WHERE REQ_ID = " + ReqID + ";");

			if (rs.next())
				data = _parse_obj_from_rs(rs);

			rs.close();
			st.close();

		} catch (Exception e) {
			System.err.println("Request._read_obj_by_id(): " + e.toString());
		}

		return(data);
	}

	/**
	 * Read REQ Records based on given query
	 * @return Vector<Request.Data> of results 
	 */
	private Vector<Data> _fill_req_vector(String query) {

		Vector<Data> vecData = new Vector<Data>();

		try {

			Statement st = this.cn.createStatement();
			ResultSet rs = st.executeQuery(query);

			while (rs.next())
				vecData.add(_parse_obj_from_rs(rs));

			rs.close();
			st.close();

		} catch (Exception e) {
			System.err.println("Request._fill_req_vector(): " + e.toString());
		}

		return(vecData);
	}

	/**
	 * Parse a given ResultSet into a Request.Data object
	 * @return <Request.Data> result 
	 */
	private Data _parse_obj_from_rs(ResultSet rs) {

		Data data = new Data();

		try {

			data.setID(rs.getInt("REQ_ID"));
			data.setJsVersion(rs.getString("REQ_JsVersion"));
			data.setEnvironment(rs.getString("REQ_Environment"));
			data.setCountry(rs.getString("REQ_Country"));
			data.setUser(rs.getString("REQ_User"));
			data.setUserMail(rs.getString("REQ_UserMail"));
			data.setUserRank(rs.getInt("REQ_UserRank"));
			data.setLock(rs.getInt("REQ_Lock"));
			data.setLat(rs.getDouble("REQ_Lat"));
			data.setLon(rs.getDouble("REQ_Lon"));
			data.setZoom(rs.getInt("REQ_Zoom"));
			data.setMotivation(rs.getString("REQ_Motivation"));
			data.setLocation(rs.getString("REQ_Location"));
			data.setSegments(rs.getString("REQ_Segments"));
			data.setVenues(rs.getString("REQ_Venues"));
			data.setCameras(rs.getString("REQ_Cameras"));
			data.setResolve(rs.getString("REQ_Resolve").equals("Y"));
			data.setStatus(RequestStatus.getByValue(rs.getString("REQ_Status")));
			data.setNotes(rs.getString("REQ_Notes"));
			data.setFollowUp(rs.getString("REQ_FollowUp"));
			data.setSolvedBy(rs.getString("REQ_SolvedBy"));
			data.setManagedBy(rs.getString("REQ_ManagedBy"));
			data.setUUID(rs.getString("REQ_UUID"));

			// Special handling for dates

			try { data.setTimestamp(rs.getTimestamp("REQ_Timestamp"));		} catch (Exception e) {	data.setTimestamp(FmtTool.DATEZERO);	}
			try { data.setSolvedDate(rs.getTimestamp("REQ_SolvedDate"));	} catch (Exception e) {	data.setSolvedDate(FmtTool.DATEZERO);	}
			try { data.setLastUpdate(rs.getTimestamp("REQ_LastUpdate"));	} catch (Exception e) {	data.setLastUpdate(FmtTool.DATEZERO);	}

		} catch (Exception e) {
			System.err.println("Request._parse_obj_from_rs(): " + e.toString());
		}

		return(data);
	}
	
	/**
	 * Update a given ResultSet from a given Request.Data object
	 */
	private static void _update_rs_from_obj(ResultSet rs, Data data) {

		try {

			rs.updateString("REQ_JsVersion", data.getJsVersion());
			rs.updateString("REQ_Environment", data.getEnvironment());
			rs.updateString("REQ_Country", data.getCountry());
			rs.updateString("REQ_User", data.getUser());
			rs.updateString("REQ_UserMail", data.getUserMail());
			rs.updateInt("REQ_UserRank", data.getUserRank());
			rs.updateInt("REQ_Lock", data.getLock());
			rs.updateDouble("REQ_Lat", data.getLat());
			rs.updateDouble("REQ_Lon", data.getLon());
			rs.updateInt("REQ_Zoom", data.getZoom());
			rs.updateString("REQ_Motivation", data.getMotivation());
			rs.updateString("REQ_Location", data.getLocation().trim().equals("") ? _ibot_get_location(data.getLat(), data.getLon()) : data.getLocation());
			rs.updateString("REQ_Segments", data.getSegments());
			rs.updateString("REQ_Venues", data.getVenues());
			rs.updateString("REQ_Cameras", data.getCameras());
			rs.updateString("REQ_Resolve", data.isResolve() ? "Y" : "N");
			rs.updateString("REQ_Status", data.getStatus().getValue());
			rs.updateString("REQ_Notes", data.getNotes());
			rs.updateString("REQ_SolvedBy", data.getSolvedBy());
			rs.updateString("REQ_ManagedBy", data.getManagedBy());
			rs.updateString("REQ_UUID", data.getUUID());

			// Special handling for dates
			try { rs.updateTimestamp("REQ_SolvedDate", new Timestamp(data.getSolvedDate().getTime())); } catch (Exception e) { }
			try { rs.updateTimestamp("REQ_LastUpdate", new Timestamp(data.getLastUpdate().getTime())); } catch (Exception e) { }

		} catch (Exception e) {
			System.err.println("Request._update_rs_from_obj(): " + e.toString());
		}
	}

	/**
	 * Retrieve location from iBot based on Lat/Lon
	 * Example: https://ibot.cortinovis.cloud/fmondini/location.php?lon=9.47956&lat=45.54448&token=0iKC24-jW
	 * @return {"status":"OK","location":[{"country":"Italy","region":"Lombardia","province":"Milano","abbr":"MI","comune":"Inzago"}]}
	 * @return {"status":"Invalid token","location":[]}
	 */
	private static String _ibot_get_location(double lat, double lon) {

		final String IBOT_DATA_URL = "https://ibot.cortinovis.cloud/fmondini/location.php?lon=" + lon + "&lat=" + lat + "&token=0iKC24-jW";

		String rc = "[ERR:IN:IBOT:LOCN]";
		String strResult = "";

		try {

			String inLine;
			URL iBotURL = new URL(IBOT_DATA_URL);
			BufferedReader in = new BufferedReader(new InputStreamReader(iBotURL.openStream()));

			while ((inLine = in.readLine()) != null)
				strResult += inLine;

			in.close();

			// Process received JSON

			JSONObject jsonResult = new JSONObject(strResult);

			if (jsonResult.get("status").toString().equalsIgnoreCase("OK")) {

				JSONArray jaLocation = new JSONArray(jsonResult.get("location").toString());

				rc = "[" + jaLocation.getJSONObject(0).getString("country") + ":" + jaLocation.getJSONObject(0).getString("region") + ":" + jaLocation.getJSONObject(0).getString("province") + ":" + jaLocation.getJSONObject(0).getString("comune") + "]";

			} else
				throw new Exception();

		} catch (Exception e) {

			System.err.println("Request._ibot_get_location(): " + e.toString());
			System.err.println("Request._ibot_get_location(): strResult is '" + strResult + "'");
			System.err.println("Request._ibot_get_location(): LAT: '" + lat + "' - LON: '" + lon + "' - Location returned: '" + rc + "'");
		}

		if (SysTool.isWindog())
			System.out.println("[DEBUG] Request._ibot_get_location(): rc = '" + rc + "'");

		return(rc);
	}

}
