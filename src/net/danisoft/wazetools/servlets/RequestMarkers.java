////////////////////////////////////////////////////////////////////////////////////////////////////
//
// RequestMarkers.java
//
// Servlet to get all request markers (with filters)
//
// First Release: May/2020 by Fulvio Mondini (https://danisoft.software/)
//       Revised: Jan/2024 Moved to V3
//       Revised: Feb/2024 Replaced pins with inline SVG icons
//       Revised: Mar/2024 Added variable stroke and color to pins
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

import org.json.JSONObject;

import net.danisoft.dslib.Database;
import net.danisoft.dslib.EnvTool;
import net.danisoft.dslib.FmtTool;
import net.danisoft.dslib.SysTool;
import net.danisoft.wazetools.ureq.Request;

@WebServlet(description = "Get all request markers (with filters)", urlPatterns = { "/servlet/RequestMarkers" })

public class RequestMarkers extends HttpServlet {

	private static final long serialVersionUID = FmtTool.getSerialVersionUID();

	// MAP_MARKER_* SVG source data are in /_ORIGINALS folder

	private static final String MAP_MARKER_POINT_UNL = "M16,0.003L16,0.003c-6.191,0-11.198,5.007-11.198,11.198c0,2.783,0.8,5.391,2.256,7.74c1.519,2.466,3.52,4.576,5.055,7.039c0.752,1.199,1.296,2.317,1.872,3.615c0.416,0.881,0.752,2.399,2.015,2.399l0,0c1.262,0,1.6-1.521,2-2.399c0.59-1.298,1.121-2.416,1.871-3.615c1.533-2.447,3.533-4.559,5.057-7.039c1.471-2.351,2.271-4.958,2.271-7.74C27.199,5.01,22.189,0.003,16,0.003z";
	private static final String MAP_MARKER_PLACE_UNL = "M32,14.398V11.2L15.999,0l-16,11.2v3.198h3.2v14.4h-3.2V32H32v-3.201h-3.198v-14.4L32,14.398L32,14.398z";
	private static final String MAP_MARKER_SPEED_UNL = "M24.889,13.334V7.111c0-0.977-0.8-1.777-1.775-1.777H1.777C0.801,5.334,0,6.134,0,7.111v17.778c0,0.977,0.801,1.777,1.777,1.777h21.334c0.978,0,1.775-0.802,1.775-1.777v-6.222L32,25.777V6.223L24.889,13.334z";
	private static final String MAP_MARKER_POINT_RES = "M16,0.003L16,0.003c-6.191,0-11.198,5.007-11.198,11.198c0,2.783,0.8,5.391,2.256,7.74c1.519,2.466,3.52,4.576,5.055,7.039c0.752,1.199,1.296,2.317,1.872,3.615c0.416,0.881,0.752,2.399,2.015,2.399l0,0c1.262,0,1.6-1.521,2-2.399c0.59-1.298,1.121-2.416,1.871-3.615c1.533-2.447,3.533-4.559,5.057-7.039c1.471-2.351,2.271-4.958,2.271-7.74C27.199,5.01,22.189,0.003,16,0.003z M25.271,2.448h2.569l0.794-2.445l0.795,2.445H32l-2.077,1.513l0.794,2.445l-2.082-1.513l-2.081,1.513l0.794-2.445L25.271,2.448z";
	private static final String MAP_MARKER_PLACE_RES = "M32,14.398V11.2L15.999,0l-16,11.2v3.198h3.2v14.4h-3.2V32H32v-3.201h-3.198v-14.4L32,14.398L32,14.398z M25.271,2.445h2.57L28.636,0l0.794,2.445H32l-2.077,1.513l0.795,2.445l-2.082-1.513l-2.082,1.513l0.795-2.445L25.271,2.445z";
	private static final String MAP_MARKER_SPEED_RES = "M24.889,13.334V7.111c0-0.977-0.8-1.777-1.775-1.777H1.777C0.801,5.334,0,6.134,0,7.111v17.778c0,0.977,0.801,1.777,1.777,1.777h21.334c0.978,0,1.775-0.802,1.775-1.777v-6.222L32,25.777V6.223L24.889,13.334z M25.271,2.445h2.57L28.636,0l0.794,2.445H32l-2.077,1.513l0.795,2.445l-2.082-1.513l-2.082,1.513l0.795-2.445L25.271,2.445z";
	private static final String MAP_MARKER_ERROR_ALL = "M-0.037,29.619h32.074L16,1.917L-0.037,29.619z M17.458,25.246h-2.917V22.33h2.917V25.246z M17.458,19.416h-2.917v-5.833h2.917V19.416z";

	private static final String MarkerPointShapeUnlVarName = "mrkPntUnl";
	private static final String MarkerPlaceShapeUnlVarName = "mrkPlcUnl";
	private static final String MarkerSpeedShapeUnlVarName = "mrkSpdUnl";
	private static final String MarkerPointShapeResVarName = "mrkPntRes";
	private static final String MarkerPlaceShapeResVarName = "mrkPlcRes";
	private static final String MarkerSpeedShapeResVarName = "mrkSpdRes";
	private static final String MarkerErrorShapeAllVarName = "mrkErrAll";

	private static final String IconPointLabelOrigVarName = "icLbOrPn";
	private static final String IconPlaceLabelOrigVarName = "icLbOrPl";
	private static final String IconSpeedLabelOrigVarName = "icLbOrSp";
	private static final String IconErrorLabelOrigVarName = "icLbOrEr";

	private static final String markerLabel1VarName = "mlb1";
	private static final String markerLabel2VarName = "mlb2";
	private static final String markerLabel3VarName = "mlb3";
	private static final String markerLabel4VarName = "mlb4";
	private static final String markerLabel5VarName = "mlb5";
	private static final String markerLabel6VarName = "mlb6";
	private static final String markerLabelXVarName = "mlbX";

	private static final String gmapMarkerVarName = "gmM";
	private static final String IconAnchorVarName = "icnAnch";
	private static final String markerAnimationVarName = "mrkAnim";
	private static final String gmapAddEventListenerVarName = "gmAel";
	private static final String ureqGetDetailsScriptVarName = "gDs";

	private static final long REQ_AGE_01_MON = (30L * 24L * 60L * 60L);
	private static final long REQ_AGE_10_DAY = (10L * 24L * 60L * 60L);
	private static final long REQ_AGE_03_DAY = ( 3L * 24L * 60L * 60L);

	/**
	 * Constructor
	 */
    public RequestMarkers() {
        super();
    }

    /**
     * POST
     */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Database DB = null;

		String AllMarkers = (
			"var ma=new Array();" +
			"var al=0;" + // al = ArrayLenght
			"const " +
				MarkerPointShapeUnlVarName + "='" + MAP_MARKER_POINT_UNL + "'," +
				MarkerPlaceShapeUnlVarName + "='" + MAP_MARKER_PLACE_UNL + "'," +
				MarkerSpeedShapeUnlVarName + "='" + MAP_MARKER_SPEED_UNL + "'," +
				MarkerPointShapeResVarName + "='" + MAP_MARKER_POINT_RES + "'," +
				MarkerPlaceShapeResVarName + "='" + MAP_MARKER_PLACE_RES + "'," +
				MarkerSpeedShapeResVarName + "='" + MAP_MARKER_SPEED_RES + "'," +
				MarkerErrorShapeAllVarName + "='" + MAP_MARKER_ERROR_ALL + "'," +
				IconAnchorVarName + "=new google.maps.Point(16,32)," +
				IconPointLabelOrigVarName + "=new google.maps.Point(16,12)," +
				IconPlaceLabelOrigVarName + "=new google.maps.Point(16,16)," +
				IconSpeedLabelOrigVarName + "=new google.maps.Point(12,16)," +
				IconErrorLabelOrigVarName + "=new google.maps.Point(9,12)," +
				markerAnimationVarName + "=google.maps.Animation.DROP," +
				markerLabel1VarName + "={text:\"1\",color:\"black\",fontSize:\"14px\"}," +
				markerLabel2VarName + "={text:\"2\",color:\"black\",fontSize:\"14px\"}," +
				markerLabel3VarName + "={text:\"3\",color:\"black\",fontSize:\"14px\"}," +
				markerLabel4VarName + "={text:\"4\",color:\"black\",fontSize:\"14px\"}," +
				markerLabel5VarName + "={text:\"5\",color:\"black\",fontSize:\"14px\"}," +
				markerLabel6VarName + "={text:\"6\",color:\"black\",fontSize:\"14px\"}," +
				markerLabelXVarName + "={text:\" \"}," + // Space needed...
				gmapAddEventListenerVarName + "=google.maps.event.addListener," +
				gmapMarkerVarName + "=google.maps.Marker," +
				ureqGetDetailsScriptVarName + "=getDetails;"
		);

		try {

			DB = new Database();
			Request REQ = new Request(DB.getConnection());

			JSONObject jCfg = new JSONObject(EnvTool.getStr(request, "jCfg", "{}"));

			//
			// Markers Array Loop
			//

			boolean CanBeShown;
			String MarkerOptions;

			Vector<Request.Data> vecReqData = REQ.getAll(
				"IT", // TODO Country Filter in REQ.getAll()
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
				// Create Markers Array
				//

				String MarkerIcon;
				String MarkerLabel;
				String MarkerPosition;
				String MarkerFillColor;
				String MarkerTitle = "UNKNOWN";

				// Defaults
				String markerIconTitle = "Bad Request";
				String markerIconScale = "1.00";
				String markerIconLabelOrigin = IconErrorLabelOrigVarName;
				String markerIconSvgConst = MarkerErrorShapeAllVarName;
				String markerStrokeWeight = "0";
				String markerStrokeColor = "black";

				if (CanBeShown) {

					if (!reqData.getSegments().trim().equals("")) {

						markerIconTitle = "Segment";
						markerIconScale = "1.00";
						markerIconLabelOrigin = IconPointLabelOrigVarName;
						markerIconSvgConst = reqData.isResolve() ? MarkerPointShapeResVarName : MarkerPointShapeUnlVarName;

					} else if (!reqData.getVenues().trim().equals("")) {

						markerIconTitle = "Place";
						markerIconScale = "0.80";
						markerIconLabelOrigin = IconPlaceLabelOrigVarName;
						markerIconSvgConst = reqData.isResolve() ? MarkerPlaceShapeResVarName : MarkerPlaceShapeUnlVarName;

					} if (!reqData.getCameras().trim().equals("")) {

						markerIconTitle = "Camera";
						markerIconScale = "1.00";
						markerIconLabelOrigin = IconSpeedLabelOrigVarName;
						markerIconSvgConst = reqData.isResolve() ? MarkerSpeedShapeResVarName : MarkerSpeedShapeUnlVarName;
					}

					// Marker Title

					MarkerTitle = "title:'" + markerIconTitle + " (by " + reqData.getUser() + ")'";

					// Marker Position

					MarkerPosition = "position:{lat:" + reqData.getLat() + ",lng:" + reqData.getLon() + "}";

					// Marker Label

					switch (reqData.getLock()) {
						case 1: MarkerLabel = markerLabel1VarName; break;
						case 2: MarkerLabel = markerLabel2VarName; break;
						case 3: MarkerLabel = markerLabel3VarName; break;
						case 4: MarkerLabel = markerLabel4VarName; break;
						case 5: MarkerLabel = markerLabel5VarName; break;
						case 6: MarkerLabel = markerLabel6VarName; break;
						default:
							MarkerLabel = markerLabelXVarName;
							break;
					}
					
					MarkerLabel = "label:" + MarkerLabel;

					// Marker Fill Color

					MarkerFillColor = (markerIconSvgConst.equals(MarkerErrorShapeAllVarName)
						? "fillColor:'#e60000'"
						: "fillColor:'#" + reqData.getStatus().getFillC() + "'"
					);

					// Marker Stroke Weight & Color

					long reqAge = (new java.util.Date().getTime() - reqData.getTimestamp().getTime()) / 1000L;

					markerStrokeWeight = (reqAge > REQ_AGE_01_MON
						? "3"
						: (reqAge > REQ_AGE_10_DAY
							? "2"
							: (reqAge > REQ_AGE_03_DAY
								? "2"
								: "1"
							)
						)
					);

					markerStrokeColor = (reqAge > REQ_AGE_01_MON
						? "DarkRed"
						: (reqAge > REQ_AGE_10_DAY
							? "DarkViolet"
							: (reqAge > REQ_AGE_03_DAY
								? "DarkOliveGreen"
								: "Black"
							)
						)
					);

					// Marker Icon

					MarkerIcon = "icon:{" +
						MarkerFillColor + "," +
						"path:" + markerIconSvgConst + "," +
						"scale:" + markerIconScale + "," +
						"strokeWeight:" + markerStrokeWeight + "," +
						"strokeColor:'" + markerStrokeColor + "'," +
						"fillOpacity:1," +
						"anchor:" + IconAnchorVarName + "," +
						"labelOrigin:" + markerIconLabelOrigin + "" +
					"}";

					// Create Marker Options

					MarkerOptions = "new " + gmapMarkerVarName + "({" +
						"map:MapObj," +
						MarkerTitle + "," +
						MarkerIcon + "," +
						MarkerLabel + "," +
						"animation:" + markerAnimationVarName + "," +
						MarkerPosition +
					"})";

					// Add Marker to Markers Array

					AllMarkers += (
						"al=ma.push(" + MarkerOptions + ");" +
						"ma[al-1].addListener('click', () => {" +
							ureqGetDetailsScriptVarName + "(" + reqData.getID() + ");" +
						"});"
					);
				}
			}

		} catch (Exception e) {
			System.err.println("RequestMarkers(): " + e.toString());
		}

		if (DB != null)
			DB.destroy();

		response.setContentType("text/javascript; charset=UTF-8");
		response.getOutputStream().print(AllMarkers);
	}

}
