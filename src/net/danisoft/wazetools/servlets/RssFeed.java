////////////////////////////////////////////////////////////////////////////////////////////////////
//
// RssFeed.java
//
// Servlet to generate a RSS feed
//
// First Release: Mar/2018 by Fulvio Mondini (https://danisoft.software/)
//       Revised: Mar/2025 Ported to Waze dslib.jar
//                         Changed to @WebServlet style
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.servlets;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.danisoft.dslib.Database;
import net.danisoft.dslib.EnvTool;
import net.danisoft.dslib.FmtTool;
import net.danisoft.dslib.SysTool;
import net.danisoft.wazetools.AppCfg;
import net.danisoft.wazetools.ureq.Alert;
import net.danisoft.wazetools.ureq.Area;
import net.danisoft.wazetools.ureq.Request;
import net.danisoft.wazetools.ureq.RequestStatus;

@WebServlet(description = "Generate a RSS feed", urlPatterns = { "/servlet/RssFeed" })

public class RssFeed extends HttpServlet {

	private static final long serialVersionUID = FmtTool.getSerialVersionUID();

	private Database	_Database;
	private Document	_Document;
	private Element		_ChanElement;

	// Getters
	public Database	getDatabase()		{ return this._Database;	}
	public Document	getDocument()		{ return this._Document;	}
	public Element	getChanElement()	{ return this._ChanElement;	}

	// Setters
	public void setDatabase(Database d)		{ this._Database = d;		}
	public void setDocument(Document d)		{ this._Document = d;		}
	public void setChanElement(Element e)	{ this._ChanElement = e;	}

	/**
	 * Entry point
	 */
	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/xml; charset=UTF-8");
		
		ServletOutputStream out = response.getOutputStream();

		String reqUser = EnvTool.getStr(request, "u", "");
		String reqCountry = EnvTool.getStr(request, "c", "");

		try {

			setDatabase(new Database());
			Request REQ = new Request(getDatabase().getConnection());
			Alert ALR = new Alert(getDatabase().getConnection());
			
			Alert.Data alrData = ALR.new Data();

			try {
				alrData = ALR.Read(reqUser);
			} catch (Exception e) {
				throw new Exception("Cannot find filter parameters for user: '" + reqUser + "'");
			}

			//
			// HEADER
			//

			setDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());

			Element elem_rss = _new_element("rss");
			elem_rss.setAttribute("xmlns:atom", "http://www.w3.org/2005/Atom");
			elem_rss.setAttribute("version", "2.0");

			setChanElement(_new_element("channel"));

			getChanElement().appendChild(_new_element("title", "[" + AppCfg.getAppTag() + "] Latest Requests"));
			getChanElement().appendChild(_new_element("link", AppCfg.getServerHomeUrl()));
			getChanElement().appendChild(_new_element("description", "[" + AppCfg.getAppTag() + "] Latest Update Requests"));
			getChanElement().appendChild(_new_element("language", "en-us"));
			getChanElement().appendChild(_new_element("pubDate", FmtTool.fmtDateTimeRssStyle()));
			getChanElement().appendChild(_new_element("lastBuildDate", FmtTool.fmtDateTimeRssStyle()));
			getChanElement().appendChild(_new_element("docs", "https://www.rssboard.org/rss-specification"));
			getChanElement().appendChild(_new_element("generator", AppCfg.getAppName() + " " + AppCfg.getAppVersion()));
			getChanElement().appendChild(_new_element("managingEditor", "Wazer fmondini (dev[at]waze[dot]tools)"));
			getChanElement().appendChild(_new_element("webMaster", "Wazer fmondini (dev[at]waze[dot]tools)"));

			Element elem_atom_link = _new_element("atom:link");
			elem_atom_link.setAttribute("href", "https://ureq.waze.tools/servlet/RssFeed?c=" + reqCountry + "&amp;u=" + reqUser);
			elem_atom_link.setAttribute("rel", "self");
			elem_atom_link.setAttribute("type", "application/rss+xml");
			getChanElement().appendChild(elem_atom_link);

			Element elem_image = _new_element("image");
			elem_image.appendChild(_new_element("title", "[" + AppCfg.getAppTag() + "] Latest Requests"));
			elem_image.appendChild(_new_element("url", /* CHANNEL_IMAGE */ AppCfg.getServerHomeUrl() + "/images/waze.png"));
			elem_image.appendChild(_new_element("link", AppCfg.getServerHomeUrl()));
			getChanElement().appendChild(elem_image);

			//
			// DETAILS
			//

			String QUERY = _create_query(reqCountry, reqUser, alrData);

			Vector<Integer> Results = new Vector<>();

			Statement st = getDatabase().getConnection().createStatement();
			ResultSet rs = st.executeQuery(QUERY);

			while(rs.next())
				Results.addElement(rs.getInt("REQ_ID"));

			rs.close();
			st.close();

			// Create Feed

			String Description;
			Element elem_item, elem_guid;

			for (int reqId : Results) {

				Request.Data reqData = REQ.Read(reqId);

				Description =
					"<tt><b>Req.Status:</b></tt> " + "[" + reqData.getStatus().getValue() + "] " + reqData.getStatus().getDescr() + "<br>" +
					"<tt><b>UnlockType:</b></tt> " + (reqData.isResolve() ? "[S] To Be Solved" : "[U] Unlock Only") + "<br>" +
					"<tt><b>Location..:</b></tt> " + _str_encode(reqData.getLocation()) + "<br>" +
					"<tt><b>Lock Level:</b></tt> " + reqData.getLock() + "<br>" +
					"<tt><b>Created at:</b></tt> " + FmtTool.fmtDateTime(reqData.getTimestamp()) + "<br>" +
					"<tt><b>Created by:</b></tt> " + reqData.getUser() + "(" + reqData.getUserRank() + ")" +
					(reqData.getMotivation().trim().equals("")
						? ""
						: ("<br><hr><b>User Comments:</b><br>" + _str_encode(reqData.getMotivation()).replace("\r", "").replace("\n", "<br>") + "<br><hr>")
					)
				;

				elem_item = _new_element("item");

				try {
					
					elem_item.appendChild(_new_element_cdata("title", _str_encode(("L" + reqData.getLock() + "/" + reqData.getStatus().getValue() + "/" + (reqData.isResolve() ? "S" : "U")) + " " + (reqData.getLocation().trim().equals("") ? ("[" + reqData.getLat() + ", " + reqData.getLon() + "]") : reqData.getLocation()))));
					elem_item.appendChild(_new_element("link", AppCfg.getServerHomeUrl() + "/unlock/index.jsp?ReqID=" + reqData.getID() + "&amp;setLat=" + reqData.getLat() + "&amp;setLon=" + reqData.getLon()));
					elem_item.appendChild(_new_element_cdata("description", Description));
					elem_item.appendChild(_new_element("pubDate", FmtTool.fmtDateTimeRssStyle(reqData.getTimestamp())));
					elem_item.appendChild(_new_element("category", _str_encode(reqData.getStatus().getDescr())));

					elem_guid = _new_element("guid", "UREQ_" + reqData.getID());
					elem_guid.setAttribute("isPermaLink", "false");
					elem_item.appendChild(elem_guid);

				} catch (Exception ed) {
					System.err.println("Loop: " + ed.toString());
					elem_item = _new_element("item");
					elem_item.appendChild(_new_element("title", "FEED ERROR"));
					elem_item.appendChild(_new_element_cdata("description", ed.toString()));
					elem_item.appendChild(_new_element("link", ""));
					elem_item.appendChild(_new_element("pubDate", FmtTool.fmtDateTimeRssStyle()));
					elem_item.appendChild(_new_element("category", ""));
				}

				getChanElement().appendChild(elem_item);
			}

			elem_rss.appendChild(getChanElement());
			getDocument().appendChild(elem_rss);

		} catch (Exception e) {

			System.err.println("Details: " + e.toString());
		}

		//
		// Generate Xml
		//

		try {

			DOMSource domSource = new DOMSource(getDocument());
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); // or ISO-8859-1
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

			// Create

			StringWriter sw = new StringWriter();
			StreamResult streamResult = new StreamResult(sw);
			transformer.transform(domSource, streamResult);

			out.println(sw.getBuffer().toString());
		
		} catch (Exception e) {
			
			System.err.println("Generation: " + e.toString());
		}		
		
		if (getDatabase() != null)
			getDatabase().destroy();
	}

	/**
	 * Create QUERY statement
	 */
	private String _create_query(String country, String user, Alert.Data alrData) {

		String WHERE = "WHERE ";

		// Country filter

		WHERE += "(REQ_Country = '" + country + "')";

		// Filter by area

		if (!alrData.isRssAllCountry()) {

			int AreaCount = 0;
			Area AREA = new Area(getDatabase().getConnection());
			Vector<Area.Data> vecAuaData = AREA.getAll(user);

			for (Area.Data auaData : vecAuaData) {

				WHERE += " " + (AreaCount == 0 ? "AND (" : "OR ") +
					"MBRContains(" +
						"ST_GeomFromText('" + auaData.getArea() + "')," +
						"ST_GeomFromText(" +
							"CONCAT('POINT(', REQ_Lon, ' ', REQ_Lat, ')')" +
						")" +
					")"
				;

				AreaCount++;
			}

			WHERE += ")";
		}

		// Status Filter

		WHERE += " AND (";

		if (!alrData.getRssActStatuses().equals("")) {

			String Statuses[] = alrData.getRssActStatuses().split(SysTool.getDelimiter());

			for (int i=0; i<Statuses.length; i++)
				WHERE += (i == 0 ? "" : " OR ") + "REQ_Status = '" + Statuses[i] + "'";

		} else
			WHERE += "REQ_Status = '" + RequestStatus.UNKN.getValue() + "'";

		WHERE += ")";

		// Query

		String QUERY =
			"SELECT REQ_ID " +
			"FROM " + Request.getTblName() + " " +
			WHERE + " " +
			"ORDER BY REQ_Timestamp DESC " +
			"LIMIT " + alrData.getRssMaxEntries()
		;

		return(QUERY);
	}

	/**
	 * Encode a string
	 */
	private static String _str_encode(String str) throws Exception {

		return(
			StringEscapeUtils.escapeJava(StringEscapeUtils.escapeHtml4(str))
		);
	}

	/**
	 * Create an element
	 */
	private Element _new_element(String name) {

		return(
			getDocument().createElement(name)
		);
	}

	/**
	 * Create an element with a value
	 */
	private Element _new_element(String name, String value) {

		Element elem = getDocument().createElement(name);
		elem.appendChild(getDocument().createTextNode(value));
		return(elem);
	}

	/**
	 * Create an element with a value
	 */
	private Element _new_element_cdata(String name, String value) {

		Element elem = getDocument().createElement(name);
		elem.appendChild(getDocument().createCDATASection(value));
		return(elem);
	}

}
