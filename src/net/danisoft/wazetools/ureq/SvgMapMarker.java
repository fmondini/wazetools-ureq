////////////////////////////////////////////////////////////////////////////////////////////////////
//
// SvgMapMarker.java
//
// UREQ SVG Map Markers
//
// First Release: ???/???? by Fulvio Mondini (https://danisoft.software/)
//       Revised: Mar/2025 Ported to Waze dslib.jar
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.ureq;

import java.io.StringWriter;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * UREQ SVG Map Markers
 */
public class SvgMapMarker {

	private Document		_Document;
	private Request.Data	_ReqData;
	private long			_ReqDateDiff;
	private int				_IndentSpaces;

	// Getters
	private Document		getDoc()			{ return(this._Document);		}
	private Request.Data	getReqData()		{ return(this._ReqData);		}
	private long			getReqDateDiff()	{ return(this._ReqDateDiff);	}
	private int				getIndentSpaces()	{ return(this._IndentSpaces);	}

	// Setters
	public void setDoc(Document d)			{ this._Document = d;		}
	public void setReqData(Request.Data r)	{ this._ReqData = r;		}
	public void setReqDateDiff(long l)		{ this._ReqDateDiff = l;	}
	public void setIndentSpaces(int i)		{ this._IndentSpaces = i;	}

	// Object Types
	public boolean isSegment() { return(!getReqData().getSegments().trim().equals("")); }
	public boolean isVenue()   { return(!getReqData().getVenues().trim().equals(""));   }
	public boolean isCamera()  { return(!getReqData().getCameras().trim().equals(""));  }

	// Request Age Const
	private static final long REQ_AGE_LOW = ( 3L * 24L * 60L * 60L * 1000L); // 3 days, in millis
	private static final long REQ_AGE_MID = (10L * 24L * 60L * 60L * 1000L); // 10 days, in millis
	private static final long REQ_AGE_OLD = (30L * 24L * 60L * 60L * 1000L); // 1 month, in millis

	// Request Age
	public boolean isAgeLow() { return(getReqDateDiff() < REQ_AGE_LOW); }
	public boolean isAgeMid() { return(getReqDateDiff() < REQ_AGE_MID && getReqDateDiff() > REQ_AGE_LOW); }
	public boolean isAgeOld() { return(getReqDateDiff() < REQ_AGE_OLD && getReqDateDiff() > REQ_AGE_MID); }
	public boolean isAgeMax() { return(getReqDateDiff() > REQ_AGE_OLD); }

	/**
	 * Constructor
	 */
	public SvgMapMarker(Request.Data data) throws Exception {
		super();

		setReqData(data);
		setReqDateDiff(new Date().getTime() - data.getTimestamp().getTime());
		setIndentSpaces(0); // No indent
	}

	/**
	 * Constructor (for debug)
	 */
	public SvgMapMarker(Request.Data data, int indentSpaces) throws Exception {
		super();

		setReqData(data);
		setReqDateDiff(new Date().getTime() - data.getTimestamp().getTime());
		setIndentSpaces(indentSpaces);
	}

	/**
	 * Get the SVG XML data
	 * @throws Exception
	 */
	public String getSvg() throws Exception {

		setDoc(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());

		Element elem_svg = getDoc().createElement("svg");
		elem_svg.setAttribute("version", "1.1");
		elem_svg.setAttribute("id", Integer.toString(getReqData().getID()));
		elem_svg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
		elem_svg.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
		elem_svg.setAttribute("x", "0px");
		elem_svg.setAttribute("y", "0px");
		elem_svg.setAttribute("width", "32px");
		elem_svg.setAttribute("height", "32px");
		elem_svg.setAttribute("viewBox", "0 0 32 32");
		elem_svg.setAttribute("enable-background", "new 0 0 32 32");
		elem_svg.setAttribute("xml:space", "preserve");

		getDoc().appendChild(elem_svg);

		// Select by Request Type

		String fillColor = "#" + getReqData().getStatus().getFillC();
		String lockColor = "#" + getReqData().getStatus().getCharC();
		String lockValue = Integer.toString(getReqData().getLock());
		String solvColor = getReqData().isResolve() ? "FireBrick" : "Green";
		String solvValue = getReqData().isResolve() ? "S" : "U";

		if (getReqData().isStatusDone())
			solvColor = "#" + getReqData().getStatus().getCharC(); // Force for resolved

		if (isSegment()) {
			
			elem_svg.appendChild(_new_path("White", "M18,2.305c0-1.135-0.896-2.055-2-2.055l0,0c-1.105,0-2,0.92-2,2.055v20.888c0,1.137,0.895,2.057,2,2.057l0,0c1.104,0,2-0.92,2-2.057V2.305z"));
			elem_svg.appendChild(_new_path(fillColor, "M30,10c0-8,0-8-2.738-8H4.738C2,2,2,2,2,10l0,0c0,8,0,8,2.738,8h22.524C30,18,30,18,30,9.881V10z"));
			elem_svg.appendChild(_new_text(lockColor, lockValue, "matrix(1 0 0 1 6 14.7339)"));
			elem_svg.appendChild(_new_text(solvColor, solvValue, "matrix(1 0 0 1 16.5557 14.7339)"));

		} else if (isVenue()) {

			elem_svg.appendChild(_new_path(fillColor, "M3,24.25v-2.96h2.6c0.266,0,0.481-0.2,0.481-0.45v-8.188c0-0.248-0.216-0.45-0.481-0.45H3V10.01l13-9.76l13,9.76v2.193l-2.599,0.001c-0.266,0-0.481,0.202-0.481,0.45v8.188c0,0.248,0.216,0.45,0.481,0.45H29v2.958H3z"));
			elem_svg.appendChild(_new_text(lockColor, lockValue, "matrix(1 0 0 1 7.5029 19.123)"));
			elem_svg.appendChild(_new_text(solvColor, solvValue, "matrix(1 0 0 1 15.6445 19.123)"));

		} else if (isCamera()) {

			elem_svg.appendChild(_new_path(fillColor, "M1.547,24.25c-0.715,0-1.297-0.563-1.297-1.256V5.506c0-0.693,0.583-1.256,1.297-1.256h21.681c0.714,0,1.296,0.563,1.296,1.256v6.121c0,0.199,0.117,0.379,0.313,0.455c0.063,0.025,0.129,0.037,0.194,0.037c0.132,0,0.263-0.05,0.359-0.143L31.75,5.82v16.861l-6.361-6.156c-0.099-0.099-0.228-0.144-0.36-0.144c-0.064,0-0.132,0.013-0.193,0.037c-0.189,0.076-0.313,0.256-0.313,0.455v6.121c0,0.691-0.581,1.255-1.295,1.255L1.547,24.25L1.547,24.25z"));
			elem_svg.appendChild(_new_text(lockColor, lockValue, "matrix(1 0 0 1 3.1069 19.0811)"));
			elem_svg.appendChild(_new_text(solvColor, solvValue, "matrix(1 0 0 1 13.3311 19.0811)"));

		} else {

			elem_svg.appendChild(_new_error());
		}

		_append_bottom_indicator(elem_svg);

		//
		// Generate Xml
		//

		DOMSource domSource = new DOMSource(getDoc());
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); // or ISO-8859-1

		if (getIndentSpaces() == 0) {
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
		} else {
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(getIndentSpaces()));
		}

		// Create

		StringWriter sw = new StringWriter();
		StreamResult streamResult = new StreamResult(sw);
		transformer.transform(domSource, streamResult);

		return(sw.getBuffer().toString());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// +++ PRIVATE +++
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Append Text
	 */
	private Element _new_text(String color, String text, String transform) {
		
		Element elem_t = getDoc().createElement("text");

		elem_t.setAttribute("transform", transform);
		elem_t.setAttribute("font-family", "sans-serif");
		elem_t.setAttribute("fill", color);
		elem_t.setAttribute("font-size", "14");

		elem_t.appendChild(getDoc().createTextNode(text));

		return(elem_t);
	}

	/**
	 * Append path
	 */
	private Element _new_path(String color, String data) {

		Element elem_p = getDoc().createElement("path");

		elem_p.setAttribute("fill", color);
		elem_p.setAttribute("stroke", "#000000");
		elem_p.setAttribute("stroke-width", "0.5");
		elem_p.setAttribute("stroke-linejoin", "round");
		elem_p.setAttribute("stroke-miterlimit", "10");
		elem_p.setAttribute("d", data);

		return(elem_p);
	}

	/**
	 * Append path
	 */
	private Element _new_error() {

		Element elem_g, elem_e;

		elem_g = getDoc().createElement("g");

		elem_e = getDoc().createElement("path");
		elem_e.setAttribute("fill", "#FFFFFF");
		elem_e.setAttribute("stroke", "#FF0000");
		elem_e.setAttribute("stroke-width", "3.0");
		elem_e.setAttribute("stroke-linecap", "round");
		elem_e.setAttribute("stroke-linejoin", "round");
		elem_e.setAttribute("d", "M4.895,9.456c2.511-6.132,9.517-9.069,15.649-6.559c6.132,2.51,9.068,9.515,6.56,15.647c-2.51,6.132-9.518,9.067-15.647,6.56c-6.13-2.51-9.067-9.511-6.562-15.641");

		elem_g.appendChild(elem_e);

		elem_e = getDoc().createElement("path");
		elem_e.setAttribute("fill", "#FF0000");
		elem_e.setAttribute("d", "M15.915,5.013c0,0-1.474-0.134-1.872,1.074C13.645,7.294,16.155,17,16.155,17S18.49,6.948,17.906,5.775C17.447,4.839,15.956,5.013,15.915,5.013z");

		elem_g.appendChild(elem_e);

		elem_e = getDoc().createElement("path");
		elem_e.setAttribute("fill", "#FF0000");
		elem_e.setAttribute("d", "M14.15,19.239c0.42-1.02,1.587-1.509,2.61-1.088c1.021,0.42,1.51,1.588,1.089,2.61c-0.423,1.019-1.589,1.508-2.61,1.088C14.219,21.43,13.731,20.261,14.15,19.239");

		elem_g.appendChild(elem_e);

		return(elem_g);
	}

	/**
	 * Single bottom indicator square object
	 */
	private Element _bottom_square(String backColor, String xCoord) {

		Element elem_r = getDoc().createElement("rect");

		elem_r.setAttribute("x", xCoord);
		elem_r.setAttribute("y", "28");
		elem_r.setAttribute("fill", backColor);
		elem_r.setAttribute("stroke", "#000000");
		elem_r.setAttribute("stroke-width", "0.25");
		elem_r.setAttribute("width", "8");
		elem_r.setAttribute("height", "4");

		return(elem_r);
	}

	/**
	 * Append bottom indicator
	 */
	private void _append_bottom_indicator(Element elem_svg) {

		final String COLOR_WHT = "White";
		final String COLOR_LOW = "LawnGreen";
		final String COLOR_MID = "Gold";
		final String COLOR_OLD = "Coral";
		final String COLOR_MAX = "Red";

		String backColor1, backColor2, backColor3, backColor4;

		if (isAgeLow()) { backColor1 = COLOR_LOW; backColor2 = COLOR_WHT; backColor3 = COLOR_WHT; backColor4 = COLOR_WHT; } else
		if (isAgeMid()) { backColor1 = COLOR_MID; backColor2 = COLOR_MID; backColor3 = COLOR_WHT; backColor4 = COLOR_WHT; } else
		if (isAgeOld()) { backColor1 = COLOR_OLD; backColor2 = COLOR_OLD; backColor3 = COLOR_OLD; backColor4 = COLOR_WHT; } else
						{ backColor1 = COLOR_MAX; backColor2 = COLOR_MAX; backColor3 = COLOR_MAX; backColor4 = COLOR_MAX; }

		elem_svg.appendChild(_bottom_square(backColor1, "0"));
		elem_svg.appendChild(_bottom_square(backColor2, "8"));
		elem_svg.appendChild(_bottom_square(backColor3, "16"));
		elem_svg.appendChild(_bottom_square(backColor4, "24"));
	}

}
