////////////////////////////////////////////////////////////////////////////////////////////////////
//
// ServerEndpnt.java - WebSocket Server EndPoint for URI: "/ws"
//
// First Release: Jul/2024 by Fulvio Mondini (https://danisoft.software/)
//                Jul/2024 PING/PONG implemented
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.websocket;

import java.util.Vector;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import net.danisoft.dslib.Database;
import net.danisoft.wazetools.AppCfg;

@ServerEndpoint("/ws/{currentUser}")
public class EndPointServer {

	private static final String WS_PING_MESSAGE = "UREQPING"; // The text of the ping/pong messages MUST BE THE SAME as those used in the WebSocket
	private static final String WS_PONG_MESSAGE = "UREQPONG"; // client JavaScript file in the UREQ webapp and those in the WUREQM Monitor script

	private Logger logger = new Logger(this.getClass());

	class ActiveSession {

		private String _ID;
		private String _User;
		private Session _Session;

		public String getId() { return this._ID; }
		public String getUser() { return this._User; }
		public Session getSession() { return this._Session; }

		public void setId(String id) { this._ID = id; }
		public void setUser(String user) { this._User = user; }
		public void setSession(Session session) { this._Session = session; }
	}

	private static Vector<ActiveSession> vecActiveSessions = new Vector<ActiveSession>();

	/**
	 * Event: OPEN
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("currentUser") String currentUser) {

		// Store in Sessions Vector

		ActiveSession activeSession = new ActiveSession();
		activeSession.setId(session.getId());
		activeSession.setUser(currentUser);
		activeSession.setSession(session);
		vecActiveSessions.add(activeSession);

		this.logger.Debug("onOpen('" + session.getId() + "') - URI: '" + session.getRequestURI() + "'");
	}

	/**
	 * Event: MESSAGE
	 */
	@OnMessage
	public void onMessage(String message, Session session) throws Exception {

		Database DB = null;

		if (message.equals(WS_PING_MESSAGE)) {

			session.getBasicRemote().sendText(WS_PONG_MESSAGE);

		} else if (message.contains("\"type\":\"" + BroadcastData.VALID_TYPE_IDENTIFIER + "\"")) {

			DB = new Database();
		//	Area AREA = new Area(DB.cn);

			for (ActiveSession activeSession : vecActiveSessions) {
				if (!activeSession.getSession().getId().equals(session.getId())) {
					if (activeSession.getSession().isOpen()) {
					//	if (_can_send_alert(AREA, message)) {
							this.logger.Debug("onMessage('" + activeSession.getSession().getId() + "') - '" + message + "'");
							activeSession.getSession().getBasicRemote().sendText(message);
							this.logger.Info(
								"[" + AppCfg.getAppTag() + "] " +
								"New alert sent to session '" + activeSession.getSession().getId() + "' " +
								"[" + activeSession.getUser() + "]"
							);
						}
					// }
				}
			}

		} else {

			this.logger.Error("onMessage('" + session.getId() + "') - Message refused: '" + message + "'");
		}

		if (DB != null)
			DB.destroy();
	}

	/**
	 * Event: CLOSE
	 */
	@OnClose
	public void onClose(Session session, CloseReason closeReason) {

		// Remove from Sessions Vector

		for (int i=0; i<vecActiveSessions.size(); i++) {
			if (vecActiveSessions.get(i).getId().equals(session.getId())) {
				vecActiveSessions.remove(i);
				break;
			}
		}

		int code = closeReason.getCloseCode().getCode();
		this.logger.Debug("onClose('" + session.getId() + "') - [" + code + "] " + ExitCode.getEnumByCode(code).getDesc());
	}

	/**
	 * Event: ERROR
	 */
	@OnError
	public void onError(Session session, Throwable throwable) {

		this.logger.Error("onError('" + session.getId() + "') - " + throwable.toString());
	}

	/**
	 * Check if an alert can be sent (based on user area)
	 */
/*
	private boolean _can_send_alert(Area AREA, String message) {

		boolean rc = false, stopLoop = false;
		Area.Data auaData;
		String logMsg;

		try {

			JSONObject jData = new JSONObject(message);
			String msgUser = jData.getString("user");
			Vector<Integer> vecAreaId = AREA.getByPoint(jData.getDouble("cLat"), jData.getDouble("cLon"));

			for (int AreaID : vecAreaId) {

				auaData = AREA.Read(AreaID);
				logMsg = "_can_send_alert(): Checking user '" + auaData.getUser() + "' for area #" + AreaID + "... ";

				if (auaData.getUser().equals(msgUser)) {
					logMsg += " --> FOUND";
					rc = stopLoop = true;
				} else
					logMsg += " (skipped)";

				this.logger.Debug(logMsg);

				if (stopLoop)
					break;
			}

		} catch (Exception e) {
			this.logger.Error("_can_send_alert(): " + e.toString());
		}

		return(rc);
	}
*/

}
