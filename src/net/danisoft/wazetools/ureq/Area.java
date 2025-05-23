////////////////////////////////////////////////////////////////////////////////////////////////////
//
// Area.java
//
// DB Interface for the areas table
//
// First Release: Feb/2018 by Fulvio Mondini (fmondini[at]danisoft.net)
//       Revised: Jan/2024 Moved to V3
//       Revised: Feb/2024 Changed to ReqObject CRUD operations
//       Revised: Mar/2025 Ported to Waze dslib.jar
//
////////////////////////////////////////////////////////////////////////////////////////////////////

package net.danisoft.wazetools.ureq;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import net.danisoft.dslib.FmtTool;
import net.danisoft.dslib.SysTool;

/**
 * DB Interface for the UREQ areas table
 */
public class Area {

	private final static String TBL_NAME = "AUTH_areas";

	private Connection cn;

	/**
	 * Constructor
	 */
	public Area(Connection conn) {
		this.cn = conn;
	}

	/**
	 * Area Data
	 */
	public class Data {

		// Fields
		private int		_ID;	// `AUA_ID` int NOT NULL AUTO_INCREMENT
		private String	_User;	// `AUA_User` varchar(32) NOT NULL DEFAULT ''
		private String	_Area;	// `AUA_Area` polygon DEFAULT NULL

		// Getters
		public int		getID()		{ return this._ID;		}
		public String	getUser()	{ return this._User;	}
		public String	getArea()	{ return this._Area;	}

		// Setters
		public void setID(int id)			{ this._ID = id;		}
		public void setUser(String user)	{ this._User = user;	}
		public void setArea(String area)	{ this._Area = area;	}

		/**
		 * Constructor
		 */
		public Data() {
			super();

			this._ID	= 0;
			this._User	= "";
			this._Area	= "";
		}
	}

	/**
	 * Read a Request
	 */
	public Data Read(int AreaID) {
		return(
			_read_obj_by_id(AreaID)
		);
	}

	/**
	 * Return all areas for a given user id
	 * @return Vector<Area.Data> of results 
	 */
	public Vector<Data> getAll(String UserID) {
		return(
			_fill_req_vector(
				"SELECT AUA_ID, AUA_User, ST_AsText(AUA_Area) AS UserArea FROM " + TBL_NAME + " WHERE AUA_User = '" + UserID + "';"
			)
		);
	}

	/**
	 * Get Vector of all area IDs containing a specific point
	 */
	public Vector<Integer> getByPoint(double lat, double lon) {

		String QUERY = "";
		Vector<Integer> Results = new Vector<>();

		try {

			QUERY =
				"SELECT AUA_ID " +
				"FROM " + TBL_NAME + " " +
				"WHERE MBRContains(" +
					"AUA_Area, " +
					"POINT(" + Double.toString(lon) + ", " + Double.toString(lat) + ")" +
				");";

			Statement st = this.cn.createStatement();
			ResultSet rs = st.executeQuery(QUERY);

			while (rs.next())
				Results.addElement(rs.getInt("AUA_ID"));

			rs.close();
			st.close();

		} catch (Exception e) {
			System.err.println("Area.getByPoint(): " + e.toString());
		}

		return(Results);
	}

	/**
	 * Create array of UserArea Coords
	 * @note To create a polygon via javascript
	 * @return a lot of "{lat: 25.774, lng: -80.190}," in a single string
	 */
	public Vector<String> getUserAreaCoords(String UserID) {

		Vector<String> Results = new Vector<>();

		try {

			int i, p;
			String tmpStr, UsrArea = "", UsrPoly[] = null, UsrVertex[] = null, UsrPoints[] = null;
			Vector<Data> vecAuaData = getAll(UserID);

			for (Data data : vecAuaData) {

				UsrArea = data.getArea()
				    .replace("POLYGON((", "")
				    .replace("))", "")
				    .replace("),(", SysTool.getDelimiter());

				UsrPoly = UsrArea.split(SysTool.getDelimiter());
				
				for (p=0; p<UsrPoly.length; p++) {
					
					UsrVertex = UsrPoly[p].split(",");

					tmpStr = "";

					for (i = 0; i < UsrVertex.length; i++) {

						UsrPoints = UsrVertex[i].split(" ");

						tmpStr += (tmpStr.equals("") ? "" : ",") + "{" +
							"lng:" + FmtTool.Round(Double.parseDouble(UsrPoints[0]), 5) + "," +
							"lat:" + FmtTool.Round(Double.parseDouble(UsrPoints[1]), 5) +
						"}";
					}

					Results.addElement(tmpStr);
				}
			}

		} catch (Exception e) {
			System.err.println("Area.getUserAreaCoords(): " + e.toString());
		}

		return(Results);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// +++ PRIVATE +++
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Read AUA Record based on given AreaID
	 * @return <Area.Data> result 
	 */
	private Data _read_obj_by_id(int AreaID) {

		Data data = new Data();

		try {

			Statement st = this.cn.createStatement();

			ResultSet rs = st.executeQuery(
				"SELECT AUA_ID, AUA_User, ST_AsText(AUA_Area) AS UserArea " +
				"FROM " + TBL_NAME + " " +
				"WHERE AUA_ID = '" + AreaID + "'"
			);

			if (rs.next())
				data = _parse_obj_from_rs(rs);

			rs.close();
			st.close();

		} catch (Exception e) {
			System.err.println("Area._read_obj_by_id(): " + e.toString());
		}

		return(data);
	}

	/**
	 * Parse a given ResultSet into a AuaObject object
	 * @return <Area.Data> result 
	 */
	private Data _parse_obj_from_rs(ResultSet rs) {

		Data data = new Data();

		try {

			data.setID(rs.getInt("AUA_ID"));
			data.setUser(rs.getString("AUA_User"));
			data.setArea(rs.getString("UserArea"));

		} catch (Exception e) {
			System.err.println("Area._parse_obj_from_rs(): " + e.toString());
		}

		return(data);
	}

	/**
	 * Read AUA Records based on given query
	 * @return Vector<Area.Data> of results 
	 */
	private Vector<Data> _fill_req_vector(String query) {

		Vector<Data> vecData = new Vector<Data>();

		try {

			Statement st = this.cn.createStatement();
			ResultSet rs = st.executeQuery(query);

			while (rs.next())
				vecData.add(_parse_obj_from_rs(rs));

			rs.close();
			st.close();

		} catch (Exception e) {
			System.err.println("Area._fill_req_vector(): " + e.toString());
		}

		return(vecData);
	}

}
