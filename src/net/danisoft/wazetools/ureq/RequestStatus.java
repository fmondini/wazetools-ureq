////////////////////////////////////////////////////////////////////////////////////////////////////
//
// RequestStatus.java
//
// UREQ Request Status
//
// First Release: ???/???? by Fulvio Mondini (https://danisoft.software/)
//       Revised: Mar/2025 Ported to Waze dslib.jar
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.ureq;

/**
 * UREQ Request Status
 */
public enum RequestStatus {

	UNKN ("UNKN", "ff0000", "000000", "[Unknown]"),
	OPEN ("OPEN", "fff497", "000000", "Waiting in queue"),
	WORK ("WORK", "99ddff", "000000", "Under review"),
	INFO ("INFO", "e6b3ff", "000000", "Waiting more info"),
	RJCT ("RJCT", "ffaaaa", "000000", "Rejected"),
	RCHK ("RCHK", "97ec7d", "000000", "Done w/recheck"),
	DONE ("DONE", "006600", "ffffff", "Completed");	// "Done, max " + Request.RESOLVED_REQ_MAX_AGE_IN_DAYS + " days");

	private final String Value;
	private final String FillC;
	private final String CharC;
	private final String Descr;

	/**
	 * Constructor
	 */
	RequestStatus(String Value, String FillC, String CharC, String Descr) {
		this.Value = Value;
		this.FillC = FillC;
		this.CharC = CharC;
		this.Descr = Descr;
    }

	public String getValue()  { return(this.Value);  }
	public String getDescr()  { return(this.Descr);  }
	public String getFillC()  { return(this.FillC);  }
	public String getCharC()  { return(this.CharC);  }

	/**
	 * Get Colorized Span
	 */
	public static String getColorizedSpan(RequestStatus Status) {

		return (
			"<span class=\"DS-padding-lfrg-4px DS-border-full DS-border-round DS-text-fixed\" style=\"color:#" + Status.getCharC() + "; background-color:#" + Status.getFillC() + ";\">" +
				Status.getValue() +
			"</span>"
		);
	}

	/**
	 * Get FillColor
	 */
	public static String getFillC(String Value) {

		String rc = "777777";

		for (RequestStatus X : RequestStatus.values())
			if (X.toString().equals(Value))
				rc = X.getFillC();

		return(rc);
	}

	/**
	 * Get CharColor
	 */
	public static String getCharC(String Value) {

		String rc = "000000";

		for (RequestStatus X : RequestStatus.values())
			if (X.toString().equals(Value))
				rc = X.getCharC();

		return(rc);
	}

	/**
	 * Get Description
	 */
	public static String getDescr(String Value) {

		String rc = "[UNKNOWN]";

		for (RequestStatus X : RequestStatus.values())
			if (X.toString().equals(Value))
				rc = X.getDescr();

		return(rc);
	}

	/**
	 * Get Object by Value
	 */
	public static RequestStatus getByValue(String Value) {

		RequestStatus rc = UNKN;

		for (RequestStatus X : RequestStatus.values())
			if (X.getValue().equals(Value))
				rc = X;

		return(rc);
	}

}
