////////////////////////////////////////////////////////////////////////////////////////////////////
//
// Logger.java - WebSocket Logger
//
// First Release: Jul/2024 by Fulvio Mondini (https://danisoft.software/)
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.websocket;

import net.danisoft.dslib.FmtTool;

public class Logger {

	private static final boolean DEBUG_IS_ACTIVE = true;

	private String _Source;

	/**
	 * Constructor
	 */
	public Logger(Class<?> sourceClass) {
		super();
		this._Source = "[" + sourceClass.getSimpleName() + "]";
	}

	/**
	 * INFO line
	 */
	public void Info(String msg) {
		System.out.println(FmtTool.fmtDateTimeLogStyle() + " [ ] " + this._Source + " " + msg);
	}

	/**
	 * Debug line
	 */
	public void Debug(String msg) {
		if (DEBUG_IS_ACTIVE)
			System.out.println(FmtTool.fmtDateTimeLogStyle() + " [d] " + this._Source + " " + msg);
	}

	/**
	 * Error line
	 */
	public void Error(String msg) {
		System.err.println(FmtTool.fmtDateTimeLogStyle() + " [E] " + this._Source + " " + msg);
	}

}
