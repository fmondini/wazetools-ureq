////////////////////////////////////////////////////////////////////////////////////////////////////
//
// GetReqQRCode.java
//
// Servlet to generate a QR Code for a given Request UUID
//
// First Release: Jan/2023 by Fulvio Mondini (https://danisoft.software/)
//       Revised: Mar/2025 Ported to Waze dslib.jar
//                         Changed to @WebServlet style
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.danisoft.dslib.EnvTool;
import net.danisoft.dslib.FmtTool;
import net.danisoft.dslib.QrcTool;
import net.danisoft.wazetools.AppCfg;

@WebServlet(description = "Generate a QR Code for a given Request UUID", urlPatterns = { "/api/req/getQrCode" })

public class GetReqQRCode extends HttpServlet {

	private static final long serialVersionUID = FmtTool.getSerialVersionUID();

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		try {

			byte[] imageBytes;
			
			try {
				
				String ReqUUID = EnvTool.getStr(request, "uuid", "");

				if (ReqUUID.equals(""))
					throw new Exception("Bad UUID");

				QrcTool QRC = new QrcTool(AppCfg.getServerHomeUrl() + "/status/by_uuid.jsp?uuid=" + ReqUUID, 200, 200, 10);

				imageBytes = QRC.getBytes();

			} catch (Exception ee) {

				File pngError = new File(AppCfg.getServerRootPath() + "/images/req-qrcode-error.png");
				FileInputStream fis = new FileInputStream(pngError);
				imageBytes = new byte[(int)pngError.length()];
				fis.read(imageBytes);
				fis.close();
			}

			response.setContentType("image/png");
			response.setContentLength(imageBytes.length);
			response.getOutputStream().write(imageBytes);

		} catch (Exception e) {

			System.err.println(e.toString());
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, e.toString());
		}
	}

}
