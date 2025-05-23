////////////////////////////////////////////////////////////////////////////////////////////////////
//
// History.java
//
// DB Interface for the requests history table
//
// First Release: January 2023 by Fulvio Mondini (https://danisoft.software/)
//       Revised: Jan/2024 Moved to V3
//       Revised: Feb/2024 Converted to <ReqObject> mode
//       Revised: Mar/2025 Ported to Waze dslib.jar
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.ureq;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Vector;

import net.danisoft.dslib.FmtTool;

/**
 * DB Interface for the requests history table
 */
public class History {

	private final static String TBL_NAME = "UREQ_history";

	private Connection cn;

	/**
	 * Constructor
	 */
	public History(Connection conn) {
		this.cn = conn;
	}

	/**
	 * History Data
	 */
	public class Data {

		// Fields
		private int				_ID;		// `URH_ID` int NOT NULL AUTO_INCREMENT
		private int				_UreqID;	// `URH_UreqID` int NOT NULL DEFAULT '0' COMMENT 'UREQ Reference ID'
		private Timestamp		_Timestamp;	// `URH_Timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation DateTime'
		private RequestStatus	_Action;	// `URH_Action` char(4) NOT NULL DEFAULT 'UNKN' COMMENT 'Action - See REQ_Status'
		private String			_Editor;	// `URH_Editor` varchar(32) NOT NULL DEFAULT '' COMMENT 'Who did the operation'
		private String			_Comments;	// `URH_Comments` varchar(2048) NOT NULL DEFAULT '' COMMENT 'Notes / Any message sent'

		// Getters
		public int				getID()			{ return this._ID;			}
		public int				getUreqID()		{ return this._UreqID;		}
		public Timestamp		getTimestamp()	{ return this._Timestamp;	}
		public RequestStatus	getAction()		{ return this._Action;		}
		public String			getEditor()		{ return this._Editor;		}
		public String			getComments()	{ return this._Comments;	}

		// Setters
		public void setID(int id)						{ this._ID = id;				}
		public void setUreqID(int ureqID)				{ this._UreqID = ureqID;		}
		public void setTimestamp(Timestamp timestamp)	{ this._Timestamp = timestamp;	}
		public void setAction(RequestStatus action)		{ this._Action = action;		}
		public void setEditor(String editor)			{ this._Editor = editor;		}
		public void setComments(String comments)		{ this._Comments = comments;	}

		/**
		 * Constructor
		 */
		public Data() {
			super();

			this._ID		= 0;
			this._UreqID	= 0;
			this._Timestamp	= FmtTool.DATEZERO;
			this._Action	= RequestStatus.UNKN;
			this._Editor	= "";
			this._Comments	= "";
		}
	}

	/**
	 * Read a History Request
	 */
	public Data Read(int UrhID) {
		return(
			_read_obj_by_id(UrhID)
		);
	}

	/**
	 * Get History IDs for a given ReqID<br>
	 * - Order: <tt>URH_Timestamp ASC</tt>
	 */
	public Vector<Data> getAll(int ReqID) {
		return(
			_fill_req_vector("SELECT * FROM " + TBL_NAME + " WHERE URH_UreqID = '" + ReqID + "' ORDER BY URH_Timestamp;")
		);
	}

	/**
	 * Insert a new History Request
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

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// +++ PRIVATE +++
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Parse a given ResultSet into a History.Data object
	 * @return <History.Data> result 
	 */
	private Data _parse_obj_from_rs(ResultSet rs) {

		Data data = new Data();

		try {

			data.setID(rs.getInt("URH_ID"));
			data.setUreqID(rs.getInt("URH_UreqID"));
			data.setAction(RequestStatus.getByValue(rs.getString("URH_Action")));
			data.setEditor(rs.getString("URH_Editor"));
			data.setComments(rs.getString("URH_Comments"));

			// Special handling for dates
			try { data.setTimestamp(rs.getTimestamp("URH_Timestamp")); } catch (Exception e) { }

		} catch (Exception e) {
			System.err.println("History._parse_obj_from_rs(): " + e.toString());
		}

		return(data);
	}

	/**
	 * Update a given ResultSet from a given History.Data object
	 */
	private static void _update_rs_from_obj(ResultSet rs, Data data) {

		try {

			rs.updateInt("URH_UreqID", data.getUreqID());
			rs.updateString("URH_Action", data.getAction().getValue());		
			rs.updateString("URH_Editor", data.getEditor());
			rs.updateString("URH_Comments", data.getComments());

		} catch (Exception e) {
			System.err.println("History._update_rs_from_obj(): " + e.toString());
		}
	}

	/**
	 * Read URH Record based on given UrhID
	 * @return <History.Data> result 
	 */
	private Data _read_obj_by_id(int UrhID) {

		Data data = new Data();

		try {

			Statement st = this.cn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM " + TBL_NAME + " WHERE URH_ID = " + UrhID + ";");

			if (rs.next())
				data = _parse_obj_from_rs(rs);

			rs.close();
			st.close();

		} catch (Exception e) {
			System.err.println("History._read_obj_by_id(): " + e.toString());
		}

		return(data);
	}

	/**
	 * Read URH Records based on given query
	 * @return Vector<History.Data> of results 
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
			System.err.println("History._fill_req_vector(): " + e.toString());
		}

		return(vecData);
	}

}
