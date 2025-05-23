////////////////////////////////////////////////////////////////////////////////////////////////////
//
// UserAreaPaths.java
//
// Servlet to get all area paths for a given user
//
// First Release: May/2020 by Fulvio Mondini (https://danisoft.software/)
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

import net.danisoft.dslib.Database;
import net.danisoft.dslib.EnvTool;
import net.danisoft.dslib.FmtTool;
import net.danisoft.dslib.SysTool;
import net.danisoft.wazetools.ureq.Area;

@WebServlet(description = "Get all area paths for a given user", urlPatterns = { "/servlet/UserAreaPaths" })

public class UserAreaPaths extends HttpServlet {

	private static final long serialVersionUID = FmtTool.getSerialVersionUID();

    public UserAreaPaths() {
        super();
    }

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String AllPaths = "";
		String mapObjName = EnvTool.getStr(request, "mapObjName", "");
		String UserName = EnvTool.getStr(request, "user", SysTool.getCurrentUser(request));

		Database DB = null;

		try {

			DB = new Database();
			Area AUA = new Area(DB.getConnection());

			Vector<String> vecUsrAreas = AUA.getUserAreaCoords(UserName);

			for (String usrArea : vecUsrAreas) {

				AllPaths +=
					"new google.maps.Polygon({" +
						"map:" + mapObjName + "," +
						"clickable:false," +
						"fillColor:'#FFFFFF'," +
						"fillOpacity:0.0," +
						"strokeColor:'#005279'," +
						"strokeOpacity:0.25," +
						"strokeWeight:10," +
						"paths:[" + usrArea + "]" +
					"});"
				;
			}

		} catch (Exception e) {

			AllPaths = "";
			System.err.println("UserAreaPaths(): " + e.toString());
		}

		if (DB != null)
			DB.destroy();

		response.setContentType("text/javascript; charset=UTF-8");
		response.getOutputStream().print(AllPaths);
	}

}
