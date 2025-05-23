////////////////////////////////////////////////////////////////////////////////////////////////////
//
// Alert.java
//
// DB Interface for the alerts table
//
// First Release: Mar/2018 by Fulvio Mondini (fmondini[at]danisoft.net)
//       Revised: Jan/2024 Moved to V3
//       Revised: Feb/2024 Changed to ReqObject CRUD operations
//       Revised: Mar/2025 Ported to Waze dslib.jar
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.ureq;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import net.danisoft.wtlib.ureq.EmailFrequency;

/**
 * DB Interface for the alerts table
 */
public class Alert {

	private final static String TBL_NAME = "UREQ_alerts";

	private final static int RSS_MAX_ENTRIES_DEFAULT = 10;

	public static String	getTblName()				{ return TBL_NAME;					}
	public static int		getRssMaxEntriesDefault()	{ return RSS_MAX_ENTRIES_DEFAULT;	}

	private Connection cn;

	/**
	 * Constructor
	 */
	public Alert(Connection conn) {
		this.cn = conn;
	}

	/**
	 * Alert Data
	 */
	public class Data {

		// Fields

		// Owner
		private String			_User;				// `ALR_User` varchar(32) NOT NULL DEFAULT ''
		// RSS Feed
		private String			_RSS_Country;		// `ALR_RSS_Country` char(2) NOT NULL DEFAULT 'IT'
		private boolean			_RSS_AllCountry;	// `ALR_RSS_AllCountry` enum('Y','N') NOT NULL DEFAULT 'N'
		private String			_RSS_ActStatuses;	// `ALR_RSS_ActStatuses` varchar(32) NOT NULL DEFAULT '' COMMENT '"Pipe" separated list of statuses'
		private int				_RSS_MaxEntries;	// `ALR_RSS_MaxEntries` int NOT NULL DEFAULT '10'
		private String			_RSS_FeedLink;		// `ALR_RSS_FeedLink` varchar(255) NOT NULL DEFAULT ''
		// Email
		private boolean			_EML_isActive;		// `ALR_EML_isActive` enum('Y','N') NOT NULL DEFAULT 'N'
		private String			_EML_Country;		// `ALR_EML_Country` char(2) NOT NULL DEFAULT 'IT'
		private boolean			_EML_AllCountry;	// `ALR_EML_AllCountry` enum('Y','N') NOT NULL DEFAULT 'N'
		private boolean			_EML_OnCreate;		// `ALR_EML_onCreate` enum('Y','N') NOT NULL DEFAULT 'N' COMMENT 'Send email when a new request is created'
		private boolean			_EML_OnModify;		// `ALR_EML_onModify` enum('Y','N') NOT NULL DEFAULT 'N' COMMENT 'Send email when a request is modified'
		private boolean			_EML_OnClose;		// `ALR_EML_onClose` enum('Y','N') NOT NULL DEFAULT 'N' COMMENT 'Send email when a request is rejected or closed'
		private boolean			_EML_OnModMine;		// `ALR_EML_onModMine` enum('Y','N') NOT NULL DEFAULT 'N' COMMENT 'Send email when a request managed by me change status'
		private EmailFrequency	_EML_Frequency;		// `ALR_EML_Frequency` enum('R','H','D') NOT NULL DEFAULT 'D' COMMENT '"R"=Real Time -- "H"=One per hour -- "D"=One per day'
		// RecheckQueue
	//	private int				_EML_RchkDays;		// `ALR_EML_RchkDays` int NOT NULL DEFAULT '0'
	//	private boolean			_CHK_RchkOnlyMe;	// `ALR_CHK_RchkOnlyMe` enum('Y','N') NOT NULL DEFAULT 'Y'
		// Slack
		private boolean			_SLK_isActive;		// `ALR_SLK_isActive` enum('Y','N') NOT NULL DEFAULT 'N'
		private String			_SLK_Country;		// `ALR_SLK_Country` char(2) NOT NULL DEFAULT 'IT'
		private boolean			_SLK_AllCountry;	// `ALR_SLK_AllCountry` enum('Y','N') NOT NULL DEFAULT 'N'

		// Getters
		public String			getUser()			{ return this._User;			}
		public String			getRssCountry()		{ return this._RSS_Country;		}
		public boolean			isRssAllCountry()	{ return this._RSS_AllCountry;	}
		public String			getRssActStatuses()	{ return this._RSS_ActStatuses;	}
		public int				getRssMaxEntries()	{ return this._RSS_MaxEntries;	}
		public String			getRssFeedLink()	{ return this._RSS_FeedLink;	}
		public boolean			isEmlActive()		{ return this._EML_isActive;	}
		public String			getEmlCountry()		{ return this._EML_Country;		}
		public boolean			isEmlAllCountry()	{ return this._EML_AllCountry;	}
		public boolean			isEmlOnCreate()		{ return this._EML_OnCreate;	}
		public boolean			isEmlOnModify()		{ return this._EML_OnModify;	}
		public boolean			isEmlOnClose()		{ return this._EML_OnClose;		}
		public boolean			isEmlOnModMine()	{ return this._EML_OnModMine;	}
		public EmailFrequency	getEmlFrequency()	{ return this._EML_Frequency;	}
	//	public int				getEmlRchkDays()	{ return this._EML_RchkDays;	}
	//	public boolean			isChkRchkOnlyMe()	{ return this._CHK_RchkOnlyMe;	}
		public boolean			isSlkActive()		{ return this._SLK_isActive;	}
		public String			getSlkCountry()		{ return this._SLK_Country;		}
		public boolean			isSlkAllCountry()	{ return this._SLK_AllCountry;	}

		// Setters
		public void setUser(String user)							{ this._User = user;						}
		public void setRssCountry(String rssCountry)				{ this._RSS_Country = rssCountry;			}
		public void setRssAllCountry(boolean rssAllCountry)			{ this._RSS_AllCountry = rssAllCountry;		}
		public void setRssActStatuses(String rssActStatuses)		{ this._RSS_ActStatuses = rssActStatuses;	}
		public void setRssMaxEntries(int rssMaxEntries)				{ this._RSS_MaxEntries = rssMaxEntries;		}
		public void setRssFeedLink(String rssFeedLink)				{ this._RSS_FeedLink = rssFeedLink;			}
		public void setEmlActive(boolean emlIsActive)				{ this._EML_isActive = emlIsActive;			}
		public void setEmlCountry(String emlCountry)				{ this._EML_Country = emlCountry;			}
		public void setEmlAllCountry(boolean emlAllCountry)			{ this._EML_AllCountry = emlAllCountry;		}
		public void setEmlOnCreate(boolean emlOnCreate)				{ this._EML_OnCreate = emlOnCreate;			}
		public void setEmlOnModify(boolean emlOnModify)				{ this._EML_OnModify = emlOnModify;			}
		public void setEmlOnClose(boolean emlOnClose)				{ this._EML_OnClose = emlOnClose;			}
		public void setEmlOnModMine(boolean emlOnModMine)			{ this._EML_OnModMine = emlOnModMine;		}
		public void setEmlFrequency(EmailFrequency emlFrequency)	{ this._EML_Frequency = emlFrequency;		}
	//	public void setEmlRchkDays(int emlRchkDays)					{ this._EML_RchkDays = emlRchkDays;			}
	//	public void setChkRchkOnlyMe(boolean chkRchkOnlyMe)			{ this._CHK_RchkOnlyMe = chkRchkOnlyMe;		}
		public void setSlkActive(boolean slkIsActive)				{ this._SLK_isActive = slkIsActive;			}
		public void setSlkCountry(String slkCountry)				{ this._SLK_Country = slkCountry;			}
		public void setSlkAllCountry(boolean slkAllCountry)			{ this._SLK_AllCountry = slkAllCountry;		}

		/**
		 * Constructor
		 */
		public Data() {
			super();

			this._User				= "";
			this._RSS_Country		= "";
			this._RSS_AllCountry	= false;
			this._RSS_ActStatuses	= "";
			this._RSS_MaxEntries	= Alert.getRssMaxEntriesDefault();
			this._RSS_FeedLink		= "";
			this._EML_isActive		= false;
			this._EML_Country		= "";
			this._EML_AllCountry	= false;
			this._EML_OnCreate		= false;
			this._EML_OnModify		= false;
			this._EML_OnClose		= false;
			this._EML_OnModMine		= false;
			this._EML_Frequency		= EmailFrequency.DAY;
	//		this._EML_RchkDays		= 0;
	//		this._CHK_RchkOnlyMe	= true;
			this._SLK_isActive		= false;
			this._SLK_Country		= "";
			this._SLK_AllCountry	= false;
		}
	}

	/**
	 * Read an Alert
	 */
	public Data Read(String UserID) {
		return(
			_read_obj_by_user(UserID)
		);
	}

	/**
	 * Insert a new Alert
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
	 * Update an Alert record
	 * @throws Exception
	 */
	public void Update(String UserID, Data data) throws Exception {

		Statement st = this.cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = st.executeQuery("SELECT * FROM " + TBL_NAME + " WHERE ALR_User = '" + UserID + "';");

		if (rs.next()) {

			_update_rs_from_obj(rs, data);
			rs.updateRow();

		} else
			throw new Exception("Alert.Update(): UserID " + UserID + " NOT found");

		rs.close();
		st.close();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// +++ PRIVATE +++
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Parse a given ResultSet into a AlrObject object
	 * @return <Alert.Data> result 
	 */
	private Data _parse_obj_from_rs(ResultSet rs) {

		Data data = new Data();

		try {
			
			// Owner
			data.setUser(rs.getString("ALR_User"));
			// RSS Feed
			data.setRssCountry(rs.getString("ALR_RSS_Country"));
			data.setRssAllCountry(rs.getString("ALR_RSS_AllCountry").equals("Y"));
			data.setRssActStatuses(rs.getString("ALR_RSS_ActStatuses"));
			data.setRssMaxEntries(rs.getInt("ALR_RSS_MaxEntries"));
			data.setRssFeedLink(rs.getString("ALR_RSS_FeedLink"));
			// Email
			data.setEmlActive(rs.getString("ALR_EML_isActive").equals("Y"));
			data.setEmlCountry(rs.getString("ALR_EML_Country"));
			data.setEmlAllCountry(rs.getString("ALR_EML_AllCountry").equals("Y"));
			data.setEmlOnCreate(rs.getString("ALR_EML_onCreate").equals("Y"));
			data.setEmlOnModify(rs.getString("ALR_EML_onModify").equals("Y"));
			data.setEmlOnClose(rs.getString("ALR_EML_onClose").equals("Y"));
			data.setEmlOnModMine(rs.getString("ALR_EML_onModMine").equals("Y"));
			data.setEmlFrequency(EmailFrequency.getByCode(rs.getString("ALR_EML_Frequency")));
			// RecheckQueue
	//		alrData.setEmlRchkDays(rs.getInt("ALR_EML_RchkDays"));
	//		alrData.setChkRchkOnlyMe(rs.getString("ALR_CHK_RchkOnlyMe").equals("Y"));
			// Slack
			data.setSlkActive(rs.getString("ALR_SLK_isActive").equals("Y"));
			data.setSlkCountry(rs.getString("ALR_SLK_Country"));
			data.setSlkAllCountry(rs.getString("ALR_SLK_AllCountry").equals("Y"));

		} catch (Exception e) {
			System.err.println("Alert._parse_obj_from_rs(): " + e.toString());
		}

		return(data);
	}

	/**
	 * Update a given ResultSet from a given Alert.Data object
	 */
	private static void _update_rs_from_obj(ResultSet rs, Data data) {

		try {
			
			// Owner
			rs.updateString("ALR_User", data.getUser());
			// RSS Feed
			rs.updateString("ALR_RSS_Country", data.getRssCountry());
			rs.updateString("ALR_RSS_AllCountry", data.isRssAllCountry() ? "Y" : "N");
			rs.updateString("ALR_RSS_ActStatuses", data.getRssActStatuses());
			rs.updateInt("ALR_RSS_MaxEntries", data.getRssMaxEntries());
			rs.updateString("ALR_RSS_FeedLink", data.getRssFeedLink());
			// Email
			rs.updateString("ALR_EML_isActive", data.isEmlActive() ? "Y" : "N");
			rs.updateString("ALR_EML_Country", data.getEmlCountry());
			rs.updateString("ALR_EML_AllCountry", data.isEmlAllCountry() ? "Y" : "N");
			rs.updateString("ALR_EML_onCreate", data.isEmlOnCreate() ? "Y" : "N");
			rs.updateString("ALR_EML_onModify", data.isEmlOnModify() ? "Y" : "N");
			rs.updateString("ALR_EML_onClose", data.isEmlOnClose() ? "Y" : "N");
			rs.updateString("ALR_EML_onModMine", data.isEmlOnModMine() ? "Y" : "N");
			rs.updateString("ALR_EML_Frequency", data.getEmlFrequency().getCode());
			// RecheckQueue
	//		rs.updateString("ALR_CHK_RchkOnlyMe", alrData.isChkRchkOnlyMe() ? "Y" : "N");
	//		rs.updateInt("ALR_EML_RchkDays", alrData.getEmlRchkDays());
			// Slack
			rs.updateString("ALR_SLK_isActive", data.isSlkActive() ? "Y" : "N");
			rs.updateString("ALR_SLK_Country", data.getSlkCountry());
			rs.updateString("ALR_SLK_AllCountry", data.isSlkAllCountry() ? "Y" : "N");

		} catch (Exception e) {
			System.err.println("Alert._update_rs_from_obj(): " + e.toString());
		}
	}

	/**
	 * Read ALR Record based on given UserID
	 * @return <Alert.Data> result 
	 */
	private Data _read_obj_by_user(String UserID) {

		Data data = new Data();

		try {
			
			Statement st = this.cn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM " + TBL_NAME + " WHERE ALR_User = '" + UserID + "';");

			if (rs.next())
				data = _parse_obj_from_rs(rs);

			rs.close();
			st.close();

		} catch (Exception e) {
			System.err.println("Alert._read_obj_by_user(): " + e.toString());
		}

		return(data);
	}

}
