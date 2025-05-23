////////////////////////////////////////////////////////////////////////////////////////////////////
//
// GetScriptConfig.java
//
// Retrieve config data
//
// First Release: Jan/2023 by Fulvio Mondini (https://danisoft.software/)
//       Revised: Mar/2025 Ported to Waze dslib.jar
//                         Changed to @WebServlet style
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import net.danisoft.dslib.Database;
import net.danisoft.dslib.EnvTool;
import net.danisoft.dslib.FmtTool;
import net.danisoft.dslib.Mail;
import net.danisoft.wtlib.auth.User;

@WebServlet(description = "Retrieve config data", urlPatterns = { "/api/cfg/get" })

public class GetScriptConfig extends HttpServlet {

	private static final long serialVersionUID = FmtTool.getSerialVersionUID();

	private static final String BUG_REPORT_URL = "https://github.com/fmondini/wazetools-ureq/issues";

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/javascript; charset=UTF-8");

		JSONObject jConfig = new JSONObject();

		String UserName = EnvTool.getStr(request, "username", "[UNKNOWN]");
		String Wme_Mail = EnvTool.getStr(request, "wme_mail", "[UNKNOWN]");
		String Language = EnvTool.getStr(request, "language", "[UNKNOWN]");
		String CallBack = EnvTool.getStr(request, "callback", "");

		boolean isBrowserITA = Language.toUpperCase().startsWith("IT");

		try {

			//
			// ABOUT
			//

			JSONObject jAbout = new JSONObject();

			jAbout.put("lblBugsRpt", "Report bugs and enhancement <a href=\"" + BUG_REPORT_URL + "\" target=\"_blank\">here</a>");

			jConfig.put("About", jAbout);

			//
			// MAIN FORM
			//

			JSONObject jMainForm = new JSONObject();

			// Comments

			jMainForm.put("lblCommentsHead", isBrowserITA
				? "Motivo della richiesta / commenti per chi sblocca"
				: "Reason for the request / comments for the unlocker"
			);

			jMainForm.put("lblCommentsBody", isBrowserITA
				? "Spiega il motivo della richiesta. Aggiungi le informazioni che ci possono aiutare nello sblocco."
				: "Explain why you are asking for an unlock. Add any information that might be useful for unlocking."
			);

			// Radio boxes

			jMainForm.put("rbxSolveText", isBrowserITA
				? "Chiedo che il problema sia risolto da un editor di alto livello"
				: "Please resolve this issue for me"
			);

			jMainForm.put("rbxUnlockText", isBrowserITA
				? "Richiedo il solo sblocco perch&eacute; voglio risolvere io"
				: "I want to solve this issue myself (unlock only)"
			);

			// Bottom buttons

			jMainForm.put("btnCancelText", isBrowserITA ? "Annulla" : "Cancel");
			jMainForm.put("btnSubmitText", isBrowserITA ? "Invia" : "Send");

			// Wait Messages

			jMainForm.put("lblCreating", isBrowserITA ? "Creazione Richiesta" : "Creating Request");
			jMainForm.put("lblPleaseWait", isBrowserITA ? "Attendere un attimo..." : "Please wait just a moment...");

			// Bug report

			jMainForm.put("lblBugReport", isBrowserITA
				? "Per errori e suggerimenti <a href=\"" + BUG_REPORT_URL + "\" target=\"_blank\">clicca qui</a>"
				: "Report bugs and enhancement <a href=\"" + BUG_REPORT_URL + "\" target=\"_blank\">here</a>"
			);

			jConfig.put("MainForm", jMainForm);

			//
			// ERRORS
			//

			JSONObject jErrors = new JSONObject();

			jErrors.put("lblHal9000", "I'm sorry, Dave... I'm afraid I can't do that.");

			jErrors.put("lblNoSelObj", isBrowserITA
				? "<b>Nessun segmento / place / camera selezionato</b><br>" +
        			"Seleziona un oggetto da sbloccare e riprova"
        		: "<b>No Segments / Venues / Cameras Selected</b><br>" +
        			"Please select one or more objects to unlock"
			);

			jConfig.put("Errors", jErrors);

			//
			// RESULTS
			//

			JSONObject jResults = new JSONObject();
			
			jResults.put("lblHeadOK", isBrowserITA ? "Richiesta Creata" : "Request Created");
			jResults.put("lblHeadKO", isBrowserITA ? "Richiesta NON Creata" : "Request NOT Created");

			jResults.put("lblBodyOK", isBrowserITA
				? "La tua richiesta &egrave; stata inviata correttamente"
				: "Your request was created successfully"
			);

			jConfig.put("Results", jResults);

			//
			// USER DATA
			//

			Database DB = null;
			String usrMail = "";
			String usrSlackID = "";

			try {
				
				DB = new Database();
				User USR = new User(DB.getConnection());
				User.Data usrData = USR.Read(UserName);
				
				usrMail = usrData.getMail();
				usrSlackID = usrData.getSlackID();

				// Force WME Mail if no mail on file

				if (!Mail.isAddressValid(usrMail)) {
					usrData.setMail(Wme_Mail);
					USR.Update(UserName, usrData);
					usrMail = Wme_Mail;
				}

			} catch (Exception ee) { }

			if (DB != null)
				DB.destroy();

			JSONObject jUser = new JSONObject();

			jUser.put("Mail", usrMail);
			jUser.put("SlackID", usrSlackID);

			jConfig.put("User", jUser);

			//
			// DONE
			//

			jConfig.put("rc", HttpServletResponse.SC_OK);

		} catch (Exception e) {

			jConfig = new JSONObject();
			jConfig.put("rc", HttpServletResponse.SC_NOT_ACCEPTABLE);
			jConfig.put("error", e.toString());
		}

		response.getOutputStream().println(CallBack + "(" + jConfig.toString() + ")");
	}
}
