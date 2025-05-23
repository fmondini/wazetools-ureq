////////////////////////////////////////////////////////////////////////////////////////////////////
//
// AddReq.java
//
// Insert a new request
//
// First Release: Jan/2023 by Fulvio Mondini (https://danisoft.software/)
//       Revised: Jan/2024 Moved to V3
//       Revised: Mar/2025 Ported to Waze dslib.jar
//                         Changed to @WebServlet style
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.api;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import net.danisoft.dslib.Database;
import net.danisoft.dslib.EnvTool;
import net.danisoft.dslib.FmtTool;
import net.danisoft.dslib.SysTool;
import net.danisoft.wtlib.auth.WazerContacts;
import net.danisoft.wazetools.ureq.History;
import net.danisoft.wazetools.ureq.Request;
import net.danisoft.wazetools.ureq.RequestStatus;
import net.danisoft.wazetools.ureq.UserMsg;
import net.danisoft.wazetools.websocket.BroadcastData;

@WebServlet(description = "Insert a new request", urlPatterns = { "/api/req/add" })

public class AddReq extends HttpServlet {

	private static final long serialVersionUID = FmtTool.getSerialVersionUID();

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/javascript; charset=UTF-8");

		Database DB = null;
		JSONObject jResult = new JSONObject();
		String CallBack = EnvTool.getStr(request, "callback", "");

		try {

			DB = new Database();

			JSONObject jPayload = new JSONObject(EnvTool.getStr(request, "payload", new JSONObject().toString()));
			JSONObject jEditor = jPayload.getJSONObject("editor");
			JSONObject jWazer = jPayload.getJSONObject("wazer");
			JSONObject jObjects = jPayload.getJSONObject("objects");
			JSONArray jaSegments = jObjects.getJSONArray("seglist");
			JSONArray jaVenues = jObjects.getJSONArray("venlist");
			JSONArray jaCameras = jObjects.getJSONArray("camlist");

			_check_script_version(
				request,
				DB.getConnection(),
				jPayload.getString("source")
			);

			Request REQ = new Request(DB.getConnection());
			History URH = new History(DB.getConnection());
			Request.Data reqData = REQ.new Data();

			int LastID = 0;
			String tmpSeg = "", tmpVen = "", tmpCam = "";

			if (jaSegments.length() > 0)
				for (int i=0; i<jaSegments.length(); i++)
					tmpSeg += ((tmpSeg.equals("") ? "" : ",") + jaSegments.getInt(i));

			if (jaVenues.length() > 0)
				for (int i=0; i<jaVenues.length(); i++)
					tmpVen += ((tmpVen.equals("") ? "" : ",") + jaVenues.getString(i));

			if (jaCameras.length() > 0)
				for (int i=0; i<jaCameras.length(); i++)
					tmpCam += ((tmpCam.equals("") ? "" : ",") + jaCameras.getInt(i));

			reqData.setJsVersion(jPayload.getString("source"));
			reqData.setEnvironment(jEditor.getString("env"));
			reqData.setCountry(jEditor.getString("country"));
			reqData.setUser(jWazer.getString("user"));
			reqData.setUserMail(jWazer.getString("mail"));
			reqData.setUserRank(jWazer.getInt("rank"));
			reqData.setLock(jObjects.getInt("lock"));
			reqData.setLat(jEditor.getDouble("lat"));
			reqData.setLon(jEditor.getDouble("lon"));
			reqData.setZoom(jEditor.getInt("zoom"));
			reqData.setMotivation(jPayload.getString("reason"));
			reqData.setLocation(jEditor.getString("address"));
			reqData.setSegments(tmpSeg);
			reqData.setVenues(tmpVen);
			reqData.setCameras(tmpCam);
			reqData.setResolve(jPayload.getBoolean("solve"));
			reqData.setStatus(RequestStatus.OPEN);
			reqData.setUUID(UUID.randomUUID().toString());

			History.Data urhData = URH.new Data();

			urhData.setAction(reqData.getStatus());
			urhData.setEditor(reqData.getUser());
			urhData.setComments(reqData.getMotivation());

			try {

				LastID = REQ.Insert(reqData);

				urhData.setUreqID(LastID);
				URH.Insert(urhData);

				jResult.put("rc", HttpServletResponse.SC_OK);
				jResult.put("lastid", LastID);

			} catch (Exception ew) {

				if (reqData.getLocation().indexOf('(') > 0)
					reqData.setLocation(reqData.getLocation().substring(0, reqData.getLocation().indexOf('(') - 1));

				LastID = REQ.Insert(reqData);

				urhData.setUreqID(LastID);
				URH.Insert(urhData);

				jResult.put("rc", HttpServletResponse.SC_OK);
				jResult.put("lastid", LastID);
			}

			DB.commit();

			REQ.SendMailAlert(request, LastID);
			REQ.SlackSendToChannel(LastID);

			// WebSocket Broadcast

			@SuppressWarnings("unused")
			BroadcastData broadcastData = new BroadcastData(SysTool.getCurrentUser(request), LastID);

			//
			// Send confirmation
			//

			reqData = REQ.Read(LastID);
			WazerContacts targetContact = new WazerContacts(reqData.getUser());

			UserMsg USM = new UserMsg();
			USM.Send(request, null, targetContact, reqData, "");

		} catch (Exception e) {

			jResult.put("rc", HttpServletResponse.SC_NOT_ACCEPTABLE);
			jResult.put("error", e.toString());
		}

		if (DB != null)
			DB.destroy();

		response.getOutputStream().println(CallBack + "(" + jResult.toString() + ")");
	}

	/**
	 * Check script version
	 * @throws an exception if different
	 */
	private static void _check_script_version(HttpServletRequest req, Connection cn, String currentVersion) throws Exception {

		final String TBL_NAME = "CODE_scripts";
		final String CODE_HOME_PAGE = "https://code.waze.tools/home/browse.jsp?ShowSID=" + Request.getUreqScriptUUID();

		Statement st = cn.createStatement();

		ResultSet rs = st.executeQuery(
			"SELECT SCR_ID, SCR_Title, SCR_Major, SCR_Minor, SCR_Build " +
			"FROM " + TBL_NAME + " " +
			"WHERE SCR_ID = '" + Request.getUreqScriptUUID() + "'"
		);

		rs.next();

		String SCR_Title = rs.getString("SCR_Title");
		String SCR_Major = rs.getString("SCR_Major");
		String SCR_Minor = rs.getString("SCR_Minor");
		String SCR_Build = rs.getString("SCR_Build");

		rs.close();
		st.close();

		String[] CurrVers = currentVersion.split("\\.");

		String zpCodeVers = String.format("%05d%05d%05d", Integer.parseInt(SCR_Major), Integer.parseInt(SCR_Minor), Integer.parseInt(SCR_Build));
		String zpUserVers = String.format("%05d%05d%05d", Integer.parseInt(CurrVers[0]), Integer.parseInt(CurrVers[1]), Integer.parseInt(CurrVers[2]));

		if (Long.parseLong(zpCodeVers) > Long.parseLong(zpUserVers)) {
			throw new Exception(
				"<br>" + // Newline after java exception type
				"<br>" + (
					SysTool.isBrowserITA(req)
						? "<b>VERSIONE INSTALLATA " + currentVersion + " OBSOLETA</b><br>"
						: "<b>YOUR SCRIPT VERSION " + currentVersion + " IS OUTDATED</b><br>"
				) +
				"<div style=\"font-size: 14px\">" + (
					SysTool.isBrowserITA(req)
						? "Aggiorna <b>" + SCR_Title + "</b> alla versione <b>" + SCR_Major + "." + SCR_Minor + "." + SCR_Build + "</b><br>" +
							"scaricandolo dal sito <a href=\"" + CODE_HOME_PAGE + "\" target=\"_blank\">Waze.Tools CODE</a>.<br>"
						: "Update your <b>" + SCR_Title + "</b> script to the <b>" + SCR_Major + "." + SCR_Minor + "." + SCR_Build + "</b> version<br>" +
							"by downloading it from the <a href=\"" + CODE_HOME_PAGE + "\" target=\"_blank\">Waze.Tools CODE</a> website.<br>"
				) +
				"<br>" + (
					SysTool.isBrowserITA(req)
						? "<i style=\"color:red\">NOTA: Non &egrave; stato possibile accettare questa richiesta.<br>" +
							"Quando avrai installato lo script corretto dovrai reinviarla.</i>"
						: "<i style=\"color:red\">Please note that this unlock request was <b>not</b> accepted.<br>" +
							"Once the correct script is installed, you will need to resubmit it</i>"
				) +
				"</div>"
			);
		}
	}

}
