////////////////////////////////////////////////////////////////////////////////////////////////////
//
// ClientEndpnt.java - WebSocket Client EndPoint
//
// First Release: Jul/2024 by Fulvio Mondini (https://danisoft.software/)
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.websocket;

import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

@ClientEndpoint
public class EndPointClient {

	private Session _Session = null;
	private Logger logger = new Logger(this.getClass());

	/**
	 * Constructor
	 */
	public EndPointClient(URI endpointURI) throws Exception {
		WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
		this._Session = webSocketContainer.connectToServer(this, endpointURI);
	}

	@OnOpen
	public void onOpen(Session session) {
		this.logger.Debug("onOpen('" + session.getId() + "')");
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		this.logger.Debug("onMessage('" + session.getId() + "') - Incoming Message: '" + message + "'");
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		int code = closeReason.getCloseCode().getCode();
		this.logger.Debug("onClose('" + session.getId() + "') [" + code + "] " + ExitCode.getEnumByCode(code).getDesc());
	}
	
	@OnError
	public void onError(Session session, Throwable throwable) {
		this.logger.Error("onError('" + session.getId() + "') - " + throwable.toString());
	}

	/**
	 * Send payload
	 * @throws Exception
	 */
	public void Send(String payload) throws Exception {
		this.logger.Debug("Send('" + this._Session.getId() + "') " + payload);
		this._Session.getBasicRemote().sendText(payload);
	}

	/**
	 * Close session
	 * @throws Exception
	 */
	public void Close() throws Exception {
		this._Session.close();
		while (this._Session.isOpen())
			Thread.sleep(250);
	}

}
