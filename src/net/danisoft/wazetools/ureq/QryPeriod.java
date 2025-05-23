////////////////////////////////////////////////////////////////////////////////////////////////////
//
// QryPeriod.java
//
// UREQ Query Period
//
// First Release: ???/???? by Fulvio Mondini (https://danisoft.software/)
//       Revised: Mar/2025 Ported to Waze dslib.jar
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.ureq;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import net.danisoft.dslib.FmtTool;

/**
 * UREQ Query Period
 */
public enum QryPeriod {

	THIS_D	("Today"),
	LAST_D	("Yesterday"),
	THIS_M	("This Month"),
	LAST_M	("Last Month"),
	THIS_Y	("This Year"),
	LAST_Y	("Last Year"),
	ALLTIM	("-=[ ALL ]=-");

	private final String _Descr;

	/**
	 * Constructor
	 */
	QryPeriod(String descr) {
		this._Descr = descr;
	}

	public String getDescr() { return this._Descr; }
	public static String getCookieNamePeriod() { return("ureq-qry-period"); }
	public static String getCookieNamePerMin() { return("ureq-qry-permin"); }
	public static String getCookieNamePerMax() { return("ureq-qry-permax"); }

	/**
	 * GET Enum by QryPeriod.toString()
	 */
	public static QryPeriod getEnum(String toString) {

		QryPeriod rc = THIS_D;

		for (QryPeriod X : QryPeriod.values())
			if (X.toString().equals(toString))
				rc = X;

		return(rc);
	}

	/**
	 * Create Combo
	 * @param cookieName The Cookie name that contains the default to use
	 */
	public static String getCombo(HttpServletRequest request, String cookieName) {

		String codedValue, rc = "";

		QryPeriod qryPeriod = QryPeriod.THIS_D;

		try {

			for (Cookie cookie : request.getCookies())
				if (cookie.getName().equals(cookieName))
					qryPeriod = QryPeriod.getEnum(cookie.getValue());

		} catch (Exception e) {	}

		for (QryPeriod X : QryPeriod.values()) {

			codedValue = X.toString() +
				"|" + FmtTool.fmtDateTimeSqlStyle(X.getDateMin()) +
				"|" + FmtTool.fmtDateTimeSqlStyle(X.getDateMax()
			);

			rc +=
				"<option value=\"" + codedValue + "\"" + (X.equals(qryPeriod) ? " selected" : "") + ">" +
					X.getDescr() +
				"</option>"
			;
		}

		return(rc);
	}

	/**
	 * Create Combo
	 * @param Default The QryPeriod.toString() to select
	 */
	public static String getCombo(String Default) {

		String codedValue, rc = "";

		for (QryPeriod X : QryPeriod.values()) {

			codedValue = X.toString() +
				"|" + FmtTool.fmtDateTimeSqlStyle(X.getDateMin()) +
				"|" + FmtTool.fmtDateTimeSqlStyle(X.getDateMax()
			);

			rc +=
				"<option value=\"" + codedValue + "\"" + (X.toString().equals(Default) ? " selected" : "") + ">" +
					X.getDescr() +
				"</option>"
			;
		}

		return(rc);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// DATE / TIME RANGES
	//

	private static Date getThisDMin() { return(FmtTool.scnDateTimeSqlStyle(LocalDate.now().toString() + " 00:00:00")); }
	private static Date getThisDMax() { return(FmtTool.scnDateTimeSqlStyle(LocalDate.now().toString() + " 23:59:59")); }

	private static Date getLastDMin() { return(FmtTool.scnDateTimeSqlStyle(LocalDate.now().minusDays(1).toString() + " 00:00:00")); }
	private static Date getLastDMax() { return(FmtTool.scnDateTimeSqlStyle(LocalDate.now().minusDays(1).toString() + " 23:59:59")); }

	private static Date getThisMMin() { return(FmtTool.scnDateTimeSqlStyle(YearMonth.now().atDay(1).toString() + " 00:00:00")); }
	private static Date getThisMMax() { return(FmtTool.scnDateTimeSqlStyle(YearMonth.now().atEndOfMonth().toString() + " 23:59:59")); }

	private static Date getLastMMin() { return(FmtTool.scnDateTimeSqlStyle(YearMonth.now().minusMonths(1).atDay(1).toString() + " 00:00:00")); }
	private static Date getLastMMax() { return(FmtTool.scnDateTimeSqlStyle(YearMonth.now().minusMonths(1).atEndOfMonth().toString() + " 23:59:59")); }

	private static Date getThisYMin() { return(FmtTool.scnDateTimeSqlStyle(Year.now().atMonth(1).atDay(1).toString() + " 00:00:00")); }
	private static Date getThisYMax() { return(FmtTool.scnDateTimeSqlStyle(Year.now().atMonth(12).atDay(31).toString() + " 23:59:59")); }

	private static Date getLastYMin() { return(FmtTool.scnDateTimeSqlStyle(Year.now().minusYears(1).atMonth(1).atDay(1).toString() + " 00:00:00")); }
	private static Date getLastYMax() { return(FmtTool.scnDateTimeSqlStyle(Year.now().minusYears(1).atMonth(12).atDay(31).toString() + " 23:59:59")); }

	/**
	 * Get the MIN Date for this QryPeriod
	 */
	public Date getDateMin() {

		Date rc = FmtTool.DATEZERO;

		if (this.equals(THIS_D)) { rc = getThisDMin(); } else
		if (this.equals(LAST_D)) { rc = getLastDMin(); } else
		if (this.equals(THIS_M)) { rc = getThisMMin(); } else
		if (this.equals(LAST_M)) { rc = getLastMMin(); } else
		if (this.equals(THIS_Y)) { rc = getThisYMin(); } else
		if (this.equals(LAST_Y)) { rc = getLastYMin(); }

		return(rc);
	}

	/**
	 * Get the MAX Date for this QryPeriod
	 */
	public Date getDateMax() {

		Date rc = FmtTool.DATEMAXV;

		if (this.equals(THIS_D)) { rc = getThisDMax(); } else
		if (this.equals(LAST_D)) { rc = getLastDMax(); } else
		if (this.equals(THIS_M)) { rc = getThisMMax(); } else
		if (this.equals(LAST_M)) { rc = getLastMMax(); } else
		if (this.equals(THIS_Y)) { rc = getThisYMax(); } else
		if (this.equals(LAST_Y)) { rc = getLastYMax(); }

		return(rc);
	}

}
