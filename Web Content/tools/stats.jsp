<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="java.util.*"
	import="net.danisoft.dslib.*"
	import="net.danisoft.wazetools.*"
	import="net.danisoft.wazetools.ureq.*"
%>
<%!
	private static final String PAGE_Title = "Unlock Statistics";
	private static final String PAGE_Keywords = "Waze.Tools UREQ, Waze, Tools, Unlock, Stats";
	private static final String PAGE_Description = AppCfg.getAppAbstract();
%>
<!DOCTYPE html>
<html>
<head>

	<jsp:include page="../_common/head.jsp">
		<jsp:param name="PAGE_Title" value="<%= PAGE_Title %>"/>
		<jsp:param name="PAGE_Keywords" value="<%= PAGE_Keywords %>"/>
		<jsp:param name="PAGE_Description" value="<%= PAGE_Description %>"/>
	</jsp:include>

	<script type="text/javascript" src="https://www.google.com/jsapi"></script>

</head>

<body>

	<jsp:include page="../_common/header.jsp" />

	<div class="mdc-layout-grid DS-layout-body">
	<div class="mdc-layout-grid__inner">
	<div class="<%= MdcTool.Layout.Cell(12, 8, 4) %>">

		<div class="DS-card-body">
			<div class="DS-text-title-shadow"><%= PAGE_Title %></div>
		</div>

		<div class="DS-card-body">
			<div class="mdc-layout-grid__inner">

				<div class="<%= MdcTool.Layout.Cell(6, 4, 4) %>">
					<div class="mdc-card <%= MdcTool.Elevation.Light() %>">
						<div class="DS-padding-8px DS-back-gray DS-border-dn">
							<div class="DS-text-huge">By Lock Level</div>
						</div>
						<div class="DS-padding-8px">
							<div id="chartBySegLock"></div>
						</div>
					</div>
				</div>

				<div class="<%= MdcTool.Layout.Cell(6, 4, 4) %>">
					<div class="mdc-card <%= MdcTool.Elevation.Light() %>">
						<div class="DS-padding-8px DS-back-gray DS-border-dn">
							<div class="DS-text-huge">By Unlocker</div>
						</div>
						<div class="DS-padding-8px">
							<div id="chartByUnlocker"></div>
						</div>
					</div>
				</div>

				<div class="<%= MdcTool.Layout.Cell(3, 3, 4) %>">
					<div class="mdc-card <%= MdcTool.Elevation.Light() %>">
						<div class="DS-padding-8px DS-back-gray DS-border-dn">
							<div class="DS-text-huge">By Year</div>
						</div>
						<div class="DS-padding-8px">
							<div id="chartByYear"></div>
						</div>
					</div>
				</div>
		
				<div class="<%= MdcTool.Layout.Cell(9, 5, 4) %>">
					<div class="mdc-card <%= MdcTool.Elevation.Light() %>">
						<div class="DS-padding-8px DS-back-gray DS-border-dn">
							<div class="DS-text-huge">By Month</div>
						</div>
						<div class="DS-padding-8px">
							<div id="chartByMonth"></div>
						</div>
					</div>
				</div>

			</div>
		</div>

		<div class="DS-card-foot">
			<table class="TableSpacing_0px DS-full-width">
				<tr>
					<td class="CellPadding_0px" align="left">
						<%= MdcTool.Button.BackTextIcon("Back", "../home/") %>
					</td>
					<td class="CellPadding_0px" align="right">
						<div class="DS-text-large DS-text-italic">More charts are coming... Send your requests by <a href="https://github.com/fmondini/wazetools-ureq/issues" target="_blank">creating a new entry</a> in the issue tracker</div>
					</td>
				</tr>
			</table>
		</div>

	</div> <!-- /Main 12/8/4 -->
	</div> <!-- /mdc-layout-grid__inner -->
	</div> <!-- /mdc-layout-grid -->
<%
	String RedirectTo = "";

	Database DB = new Database();
	Request REQ = new Request(DB.getConnection());
	MsgTool MSG = new MsgTool(session);

	String Tokens[] = null;
//	Enumeration<String> reqEnum = null;

	try {

		////////////////////////////////////////////////////////////////////////////////////////////////////
		//
		// GET DATA FROM DB
		//

		Vector<String> vecCount;

		//
		// By Segment Lock Level
		//

		String dataBySegLock = "['Segment Lock Level', 'Count'],";

		vecCount = REQ.countByLock();

		for (String strCount : vecCount) {
			
			Tokens = strCount.split(SysTool.getDelimiter());
			dataBySegLock += "['Lock " + Tokens[0] + "', " + Tokens[1] + "],";
		}

		dataBySegLock = dataBySegLock.substring(0, dataBySegLock.length() - 1);

		//
		// By Unlocker
		//

		String dataByUnlocker = "['Unlocker', 'Count'],";

		vecCount = REQ.countByUnlocker();

		for (String strCount : vecCount) {
	
			Tokens = strCount.split(SysTool.getDelimiter());
			dataByUnlocker += "['" + Tokens[0] + "', " + Tokens[1] + "],";
		}

		dataByUnlocker = dataByUnlocker.substring(0, dataByUnlocker.length() - 1);

		//
		// By Year
		//

		String dataByYear = "['Year', 'Count'],";

		vecCount = REQ.countByYear();

		for (String strCount : vecCount) {
	
			Tokens = strCount.split(SysTool.getDelimiter());
			dataByYear += "['" + Tokens[0] + "', " + Tokens[1] + "],";
		}

		dataByYear = dataByYear.substring(0, dataByYear.length() - 1);

		// TESTONLY dataByYear += ",['2015', 223],['2016', 308],['2017', 177],['2018', 45]";

		//
		// By Month
		//

		String dataByMonth = "['Month', 'Count'],";

		vecCount = REQ.countByMonth();

		for (String strCount : vecCount) {
	
			Tokens = strCount.split(SysTool.getDelimiter());
			dataByMonth += "['" + Tokens[0] + "', " + Tokens[1] + "],";
		}

		dataByMonth = dataByMonth.substring(0, dataByMonth.length() - 1);

		// TESTONLY dataByMonth += ",['May/2014', 145],['Jun/2014', 195],['Jul/2014', 221],['Aug/2014', 183],['Sep/2014', 112],['Oct/2014', 176],['Nov/2014', 148],['Dec/2014', 181]";
		// dataByMonth = dataByMonth.replace("/", "\\n");

		////////////////////////////////////////////////////////////////////////////////////////////////////
		//
		// DRAW CHARTS VIA GOOGLE
		//

		String CommonChartOptions =
			"fontName:'IBM Plex Sans Condensed'," +
			"fontSize: '12'" +
			"";

		String CommonPieChartOptions =
			"is3D:true," +
			"legend:{position:'right',alignment:'center',textStyle:{color:'black',fontSize:'12'}}," +
			"chartArea:{width:'100%',height:'100%'}";

		String CommonBarChartOptions =
			"vAxis:{minValue:'0'}," +
			"legend:{position:'none'}," +
			"chartArea:{width:'100%',height:'80%'}";
%>
		<script>

		//
		// Draw Charts
		//

		google.load("visualization", "1", {packages:["corechart"]});
		google.setOnLoadCallback(drawAllCharts);

		function drawAllCharts() {

			drawBySegLock();
			drawByUnlocker();
			drawByYear();
			drawByMonth();
		}

		function drawBySegLock() {

			var data = google.visualization.arrayToDataTable([<%= dataBySegLock %>]);
			var options = { <%= CommonChartOptions %>, <%= CommonPieChartOptions %> };
			var chart = new google.visualization.PieChart(document.getElementById('chartBySegLock'));

			chart.draw(data, options);
		}

		function drawByUnlocker() {

			var data = google.visualization.arrayToDataTable([<%= dataByUnlocker %>]);
			var options = { <%= CommonChartOptions %>, <%= CommonPieChartOptions %> };
			var chart = new google.visualization.PieChart(document.getElementById('chartByUnlocker'));

			chart.draw(data, options);
		}

		function drawByYear() {

			var data = google.visualization.arrayToDataTable([<%= dataByYear %>]);
			var options = {	<%= CommonChartOptions %>, <%= CommonBarChartOptions %> };
			var chart = new google.visualization.ColumnChart(document.getElementById('chartByYear'));

			chart.draw(data, options);
		}

		function drawByMonth() {

			var data = google.visualization.arrayToDataTable([<%= dataByMonth %>]);
			var options = { <%= CommonChartOptions %>, <%= CommonBarChartOptions %> };
			var chart = new google.visualization.ColumnChart(document.getElementById('chartByMonth'));

			chart.draw(data, options);
		}
	
		//
		// Redraw on resize
		//

		$(window).resize(function() {
			if (this.resizeTO)
				clearTimeout(this.resizeTO);
			this.resizeTO = setTimeout(function() {
				$(this).trigger('resizeEnd');
			}, 500);
		});

		$(window).on('resizeEnd', function() {
			drawAllCharts();
		});

		</script>
<%		
	} catch (Exception e) {

		MSG.setSlideText("Internal Error", e.toString());
		RedirectTo = "../unlock/"; // Always logged in, here
	}

	DB.destroy();
%>
	<jsp:include page="../_common/footer.jsp">
		<jsp:param name="RedirectTo" value="<%= RedirectTo %>"/>
	</jsp:include>

</body>
</html>
