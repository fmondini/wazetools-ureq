////////////////////////////////////////////////////////////////////////////////////////////////////
//
// ClientSender.java - WebSocket Payload Sender - UREQ version
//
// First Release: Jul/2024 by Fulvio Mondini (https://danisoft.software/)
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.websocket;

import org.json.JSONObject;

import net.danisoft.dslib.Database;
import net.danisoft.wazetools.AppCfg;
import net.danisoft.wazetools.ureq.Request;

public class BroadcastData {

	public static final String VALID_TYPE_IDENTIFIER = "NewUreq";

	private Logger logger = new Logger(this.getClass());

	/**
	 * Data Class - Payload data to be sent via WebSocket
	 */
	public class Data {

	//	----------------------		------------------------------------------------		----------------------------------------------------------
	//	FIELDS						GETTERS													SETTERS
	//	----------------------		------------------------------------------------		----------------------------------------------------------
		private String	_Type;		public String	getType() { return this._Type; }		public void	setType(String type)	{ this._Type = type; }
		private int		_RqID;		public int		getRqID() { return this._RqID; }		public void	setRqID(int rqId)		{ this._RqID = rqId; }
		private String	_Uuid;		public String	getUuid() { return this._Uuid; }		public void	setUuid(String uuid)	{ this._Uuid = uuid; }
		private String	_User;		public String	getUser() { return this._User; }		public void	setUser(String user)	{ this._User = user; }
		private int		_Rank;		public int		getRank() { return this._Rank; }		public void	setRank(int rank)		{ this._Rank = rank; }	
		private String	_Locn;		public String	getLocn() { return this._Locn; }		public void	setLocn(String locn)	{ this._Locn = locn; }
		private int		_Lock;		public int		getLock() { return this._Lock; }		public void	setLock(int lock)		{ this._Lock = lock; }
		private boolean	_Solv;		public boolean	getSolv() { return this._Solv; }		public void	setSolv(boolean solv)	{ this._Solv = solv; }
		private double	_CLat;		public double	getCLat() { return this._CLat; }		public void	setCLat(double cLat)	{ this._CLat = cLat; }
		private double	_CLon;		public double	getCLon() { return this._CLon; }		public void	setCLon(double cLon)	{ this._CLon = cLon; }

		/**
		 * Constructor
		 */
		public Data() {
			super();

			this._Type = VALID_TYPE_IDENTIFIER;
			this._RqID = 0;
			this._Uuid = "";
			this._User = "";
			this._Rank = 0;
			this._Locn = "";
			this._Lock = 0;
			this._Solv = false;
			this._CLat = 0.0D;
			this._CLon = 0.0D;
		}

		/**
		 * Convert this Data object to a JSONObject String
		 */
		public String toJsonString() {

			JSONObject jMessage = new JSONObject();

			jMessage.put("type", this._Type);
			jMessage.put("rqId", this._RqID);
			jMessage.put("uuid", this._Uuid);
			jMessage.put("user", this._User);
			jMessage.put("rank", this._Rank);
			jMessage.put("locn", this._Locn);
			jMessage.put("lock", this._Lock);
			jMessage.put("solv", this._Solv);
			jMessage.put("cLat", this._CLat);
			jMessage.put("cLon", this._CLon);

			return(jMessage.toString());
		}

	} // End of Data Class

	/**
	 * Constructor - Broadcast data via WebSocket
	 */
	public BroadcastData(String UserID, int ReqID) {
		super();

		this.logger.Debug("Constructor('" + UserID + "', " + ReqID + ")");

		Database DB = null;

		try {

			if (UserID.trim().equals("") || ReqID == 0)
				throw new Exception("Bad Parameters");

			//
			// Read UREQ data
			//

			DB = new Database();
			Request REQ = new Request(DB.getConnection());

			Request.Data reqData = REQ.Read(ReqID);

			if (reqData.getID() != ReqID)
				throw new Exception("Bad ID -  ReqID: " + ReqID + " - reqData.getID(): " + reqData.getID());

			//
			// Create Payload
			//

			Data payload = new Data();

			payload.setRqID(reqData.getID());
			payload.setUuid(reqData.getUUID());
			payload.setUser(reqData.getUser());
			payload.setRank(reqData.getUserRank());
			payload.setLocn(reqData.getLocation());
			payload.setLock(reqData.getLock());
			payload.setSolv(reqData.isResolve());
			payload.setCLat(reqData.getLat());
			payload.setCLon(reqData.getLon());

			this.logger.Info(
				"[" + AppCfg.getAppTag() + "] " +
				"New " + (reqData.isResolve() ? "RESOLVE" : "UNLOCK") + "(" + reqData.getLock() + ") request #" + reqData.getID() + " received " +
				"from " + reqData.getUser() + "(" + reqData.getUserRank() + ")"
			);

			//
			// Send data to WebSocket server
			//
			
			EndPointClient endPointClient = new EndPointClient(AppCfg.getServerEndpointURI(UserID));

			endPointClient.Send(payload.toJsonString());
			endPointClient.Close();
			
		} catch (Exception e) {
			this.logger.Error(e.toString());
		}

		if (DB != null)
			DB.destroy();
	}
	
}
