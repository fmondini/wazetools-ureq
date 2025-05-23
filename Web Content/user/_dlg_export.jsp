<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="java.sql.*"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wtlib.auth.*"
	import="net.danisoft.wazetools.*"
	import="net.danisoft.wazetools.ureq.*"
%>
	<div class="DS-padding-8px DS-back-gray DS-border-dn">
		<div class="DS-text-huge DS-text-bold DS-text-italic DS-text-black">Export Requests History</div>
	</div>

	<div class="DS-padding-8px">
<%
	String export_user = EnvTool.getStr(session, "export_user", "");
	String export_pass = EnvTool.getStr(session, "export_pass", "");

	Database DB = null;
	Statement st = null;
	ResultSet rs = null;

	try {

		DB = new Database();
		User USR = new User(DB.getConnection());
		Request REQ = new Request(DB.getConnection());
		User.Data usrData = USR.Read(export_user);

		if (!export_pass.equals(usrData.getPass()))
			throw new Exception("Bad authentication tokens");

		//
		// CREATE TMP TABLE
		//

		final String TMP_TBL_NAME = "UREQ_export_" + export_user;

		st = DB.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

		st.executeUpdate("DROP TABLE IF EXISTS " + TMP_TBL_NAME + ";");

		st.executeUpdate("CREATE TABLE " + TMP_TBL_NAME + " LIKE " + Request.getTblName() + ";");

		st.executeUpdate(
			"INSERT INTO " + TMP_TBL_NAME + " " +
			"SELECT * FROM " + Request.getTblName() + " " +
			"WHERE REQ_User = '" + export_user + "';"
		);

		st.executeUpdate("ALTER TABLE " + TMP_TBL_NAME + " ADD COLUMN REQ_Permalink VARCHAR(512) NOT NULL DEFAULT '' AFTER REQ_Cameras;");

		rs = st.executeQuery("SELECT * FROM " + TMP_TBL_NAME + ";");

		while (rs.next()) {

			Request.Data reqData = REQ.Read(rs.getInt("REQ_ID"));
			rs.updateString("REQ_Permalink", reqData.CreatePermalink());
			rs.updateRow();
		}

		rs.close();

		//
		// EXPORT TMP TABLE
		//

		String FieldList[][] = {
			// --------------------------------	-----------------------	------------------
			// FIELD TYPE						FIELD NAME				COLUMN DESCR
			// --------------------------------	-----------------------	------------------
			{ ExpTool.FieldType.INT.getValue(),	"REQ_ID",				"ID"			},
			{ ExpTool.FieldType.DAT.getValue(),	"REQ_Timestamp",		"Date / Time"	},
			{ ExpTool.FieldType.STR.getValue(),	"REQ_Location",			"Location"		},
			{ ExpTool.FieldType.STR.getValue(),	"REQ_Permalink",		"Permalink"		},
			{ ExpTool.FieldType.STR.getValue(),	"REQ_Resolve",			"Resolve?"		},
			{ ExpTool.FieldType.INT.getValue(),	"REQ_Lock",				"Lock"			},
			{ ExpTool.FieldType.STR.getValue(),	"REQ_Status",			"Status"		},
			{ ExpTool.FieldType.STR.getValue(),	"REQ_ManagedBy",		"Last Editor"	},
			{ ExpTool.FieldType.DAT.getValue(),	"REQ_LastUpdate",		"Last Edit"		},
			{ ExpTool.FieldType.STR.getValue(),	"REQ_SolvedBy",			"Solved By"		},
			{ ExpTool.FieldType.DAT.getValue(),	"REQ_SolvedDate",		"Close Date"	},
			{ ExpTool.FieldType.STR.getValue(),	"REQ_Motivation",		"Comments"		}
		};

		String Query = "SELECT * FROM " + TMP_TBL_NAME + " WHERE REQ_User = '" + export_user + "' ORDER BY REQ_Timestamp DESC;";

		String xlsFile = export_user + "-" + FmtTool.fmtDateTimeFileStyle() + ".xls";
		String xlsFsName = AppCfg.getServerRootPath() + "/exports/" + xlsFile;
		String xlsWebName = AppCfg.getServerHomeUrl() + "/exports/" + xlsFile;

		ExpTool EXP = new ExpTool();

		EXP.XlsExport(DB, FieldList, Query, "UREQ Data Export", xlsFsName);

		//
		// CLEAN
		//

		st.executeUpdate("DROP TABLE IF EXISTS " + TMP_TBL_NAME + ";");
		st.close();
%>
		<div class="DS-padding-16px" align="center">
			<div class="DS-text-huge DS-text-green DS-text-bold DS-text-italic">XLS file generated successfully</div>
			<div class="DS-padding-16px"><a href="<%= xlsWebName %>"><img src="../images/128x128/xls.png" title="Download Xls File"></a></div>
			<div class="DS-text-big DS-text-green DS-text-italic">Click the icon to download it</div>
		</div>
<%
	} catch (Exception e) {
%>
		<div class="DS-back-pastel-red DS-border-full">
			<div class="DS-padding-8px DS-text-exception"><%= e.toString() %></div>
		</div>
<%
	}

	if (DB != null)
		DB.destroy();
%>
	</div>

	<div class="DS-padding-8px DS-back-gray DS-border-up" align="center">
		<%= MdcTool.Dialog.BtnDismiss("btnDismiss", "Close", true, "", "") %>
	</div>
