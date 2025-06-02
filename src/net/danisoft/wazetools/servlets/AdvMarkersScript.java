////////////////////////////////////////////////////////////////////////////////////////////////////
//
// AdvMarkersScript.java
//
// Servlet to generate Advanced Markers Script
//
// First Release: Aug/2022 by Fulvio Mondini (https://danisoft.software/)
//       Revised: Jan/2024 Moved to V3
//       Revised: Mar/2025 Ported to Waze dslib.jar
//                         Changed to @WebServlet style
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.servlets;

import java.io.IOException;
import java.util.Vector;

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
import net.danisoft.wazetools.ureq.Request;
import net.danisoft.wazetools.ureq.SvgMapMarker;
import net.danisoft.wtlib.auth.GeoIso;
import net.danisoft.wtlib.auth.User;
import net.danisoft.wtlib.auth.WazerConfig;

@WebServlet(description = "Generate Advanced Markers Script", urlPatterns = { "/servlet/AdvMarkersScript" })

public class AdvMarkersScript extends HttpServlet {

	private static final long serialVersionUID = FmtTool.getSerialVersionUID();

    public AdvMarkersScript() {
        super();
    }

    /**
     * POST
     */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Database DB = null;

		String AllMarkers = (
			"var mrkMark,parser=new DOMParser();"
		);

		try {

			DB = new Database();

			User USR = new User(DB.getConnection());
			GeoIso GEO = new GeoIso(DB.getConnection());
			Request REQ = new Request(DB.getConnection());

			String mapObjName = EnvTool.getStr(request, "mapObjName", "");
			JSONObject jCfg = new JSONObject(EnvTool.getStr(request, "jCfg", "{}"));

			//
			// Markers Array Loop
			//

			boolean CanBeShown;
			GeoIso.Data geoData;

			User.Data usrData = USR.Read(SysTool.getCurrentUser(request));
			WazerConfig wazerConfig = usrData.getWazerConfig();
			JSONArray jaActvCtry = wazerConfig.getUreq().getActiveCountries();

			Vector<Request.Data> vecTmpData;
			Vector<Request.Data> vecReqData = new Vector<Request.Data>();

			// Read ALL enabled countries

			for (int i = 0; i < jaActvCtry.length(); i++) {

				geoData = GEO.Read(jaActvCtry.getString(i));

				vecTmpData = REQ.getAll(
					geoData.getIso2(),
					(jCfg.getBoolean("jCfg-ShowAreaAll") // Show ALL areas flag
						? ""
						: SysTool.getCurrentUser(request)
					),
					jCfg.getBoolean("jCfg-LayerShowOpen"),
					jCfg.getBoolean("jCfg-LayerShowWork"),
					jCfg.getBoolean("jCfg-LayerShowInfo"),
					jCfg.getBoolean("jCfg-LayerShowRjct"),
					jCfg.getBoolean("jCfg-LayerShowRchk"),
					jCfg.getBoolean("jCfg-LayerShowDone")
				);

				for (Request.Data reqData : vecTmpData)
					vecReqData.add(reqData);
			}

			// Create

			for (Request.Data reqData : vecReqData) {

				// Filter Requests

				CanBeShown = false;

				if ((jCfg.getBoolean("jCfg-LayerShowOpen") && reqData.isStatusOpen()) ||
					(jCfg.getBoolean("jCfg-LayerShowWork") && reqData.isStatusWork()) ||
					(jCfg.getBoolean("jCfg-LayerShowInfo") && reqData.isStatusInfo()) ||
					(jCfg.getBoolean("jCfg-LayerShowRjct") && reqData.isStatusRjct()) ||
					(jCfg.getBoolean("jCfg-LayerShowRchk") && reqData.isStatusRchk()) ||
					(jCfg.getBoolean("jCfg-LayerShowDone") && reqData.isStatusDone())
				) {

					// Area ok, check management

					if ((jCfg.getBoolean("jCfg-ShowReqAll")) ||
						(jCfg.getBoolean("jCfg-ShowReqMine") && (reqData.getManagedBy().equals(SysTool.getCurrentUser(request)) || reqData.getSolvedBy().equals(SysTool.getCurrentUser(request)))) ||
						(jCfg.getBoolean("jCfg-ShowReqOthers") && !reqData.getManagedBy().equals(SysTool.getCurrentUser(request)) && !reqData.getSolvedBy().equals(SysTool.getCurrentUser(request))) ||
						(jCfg.getBoolean("jCfg-ShowReqNew") && reqData.getManagedBy().equals("") && reqData.getSolvedBy().equals(""))
					) {

						// Management ok, check lock

						if ((jCfg.getBoolean("jCfg-ShowLock1") && reqData.getLock() == 1) ||
							(jCfg.getBoolean("jCfg-ShowLock2") && reqData.getLock() == 2) ||
							(jCfg.getBoolean("jCfg-ShowLock3") && reqData.getLock() == 3) ||
							(jCfg.getBoolean("jCfg-ShowLock4") && reqData.getLock() == 4) ||
							(jCfg.getBoolean("jCfg-ShowLock5") && reqData.getLock() == 5) ||
							(jCfg.getBoolean("jCfg-ShowLock6") && reqData.getLock() == 6)
						) {
							CanBeShown = true;
						}
					}
				}

				//
				// Create SVG Markers
				//

				if (CanBeShown) {

					String markerMap = "map:" + mapObjName;
					String markerTitle = "title:'" + FmtTool.fmtDateTimeNoSecs(reqData.getTimestamp()) + " by " + reqData.getUser() + "(" + reqData.getUserRank() + ") - " + reqData.getStatus().getDescr() + "'";
					String markerPosition = "position:{lat:" + reqData.getLat() + ",lng:" + reqData.getLon() + "}";
					String markerContent = "content:parser.parseFromString('" + new SvgMapMarker(reqData).getSvg() + "','image/svg+xml').documentElement";

					AllMarkers += (
						"mrkMark=new google.maps.marker.AdvancedMarkerElement({" +
							markerMap + "," +
							markerTitle + "," +
							markerPosition + "," +
							markerContent +
						"});" +
						"mrkMark.addListener('click',()=>{" +
							"getDetails(" + reqData.getID() + ");" +
						"});"
					);
				}
			}

		} catch (Exception e) {
			System.err.println("AdvMarkersScript(): " + e.toString());
			AllMarkers += "alert('+++ AdvMarkersScript() ERROR +++\\n\\n" + e.toString() + "')";
		}

		if (DB != null)
			DB.destroy();

		response.setContentType("text/javascript; charset=UTF-8");
		response.getOutputStream().print(AllMarkers);
	}

}
