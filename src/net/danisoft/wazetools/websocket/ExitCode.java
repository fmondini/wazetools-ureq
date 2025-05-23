////////////////////////////////////////////////////////////////////////////////////////////////////
//
// ExitCode.java
//
// WebSocket Exit Codes
//
// First Release: Jan/2023 by Fulvio Mondini (https://danisoft.software/)
//       Revised: Jan/2024 Moved to V3
//       Revised: Mar/2025 Ported to Waze dslib.jar
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.websocket;

/**
 * WebSocket Exit Codes
 */
public enum ExitCode {

	WS_CLOSE_STATUS_NOSTATUS					(0),
	WS_CLOSE_STATUS_NORMAL						(1000),
	WS_CLOSE_STATUS_GOINGAWAY					(1001),
	WS_CLOSE_STATUS_PROTOCOL_ERR				(1002),
	WS_CLOSE_STATUS_UNACCEPTABLE_OPCODE			(1003),
	WS_CLOSE_STATUS_RESERVED					(1004),
	WS_CLOSE_STATUS_NO_STATUS					(1005),
	WS_CLOSE_STATUS_ABNORMAL_CLOSE				(1006),
	WS_CLOSE_STATUS_INVALID_PAYLOAD				(1007),
	WS_CLOSE_STATUS_POLICY_VIOLATION			(1008),
	WS_CLOSE_STATUS_MESSAGE_TOO_LARGE			(1009),
	WS_CLOSE_STATUS_EXTENSION_REQUIRED			(1010),
	WS_CLOSE_STATUS_UNEXPECTED_CONDITION		(1011),
	WS_CLOSE_STATUS_TLS_FAILURE					(1015),
	WS_CLOSE_STATUS_CLIENT_TRANSACTION_DONE		(2000),
	WS_CLOSE_STATUS_UNKNOWN						(9999);

	private final int _Code;

	ExitCode(int code) {
		this._Code = code;
	}

	public int getCode()	{ return(this._Code);		}
	public String getDesc()	{ return(this.toString());	}

	/**
	 * Get SocketExitCode enum by code
	 */
	public static ExitCode getEnumByCode(int code) {

		ExitCode rc = WS_CLOSE_STATUS_UNKNOWN;

		for (ExitCode exitCode : ExitCode.values())
			if (exitCode.getCode() == code)
				rc = exitCode;

		return(rc);
	}

}
