<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
%>
<%!
	private static final String PAGE_Title = "UREQ Request Status";
	private static final String PAGE_Keywords = "";
	private static final String PAGE_Description = "";
%>
<!DOCTYPE html>
<html>
<head>

	<jsp:include page="../_common/head.jsp">
		<jsp:param name="PAGE_Title" value="<%= PAGE_Title %>"/>
		<jsp:param name="PAGE_Keywords" value="<%= PAGE_Keywords %>"/>
		<jsp:param name="PAGE_Description" value="<%= PAGE_Description %>"/>
	</jsp:include>

	<script>

		function ShowStatus(ReqUUID) {

			$.ajax({

				async: true,
				cache: false,
				type: 'GET',
				dataType: 'text',
				url: '../status/_div_status.jsp',

				data: {
					uuid: ReqUUID,
					withCloseBtn: 'N'
				},

				success: function(data) {
					$('#divHistoryContent').html(data);
				},

				error: function(jqXHR, textStatus, errorThrown) {
					ShowDialog_OK('An error has occurred', jqXHR.responseText, 'OK');
					console.error('jqXHR: %o', jqXHR);
					console.error('textStatus: %o', textStatus);
					console.error('errorThrown: %o', errorThrown);
				}
			});
		}

	</script>

</head>
<%
	String UreqUUID = EnvTool.getStr(request, "uuid", "");
%>
<body>

	<div class="DS-layout-body" align="center">
		<div id="divHistoryContent">
			<div class="DS-margin-up-16px DS-padding-32px DS-back-AliceBlue">
				<img src="../images/ajax-loader.gif">
				<div class="DS-text-large">Loading data for UUID</div>
				<div class="DS-text-fixed DS-text-bold"><%= UreqUUID %></div>
			</div>
		</div>
	</div>

	<script>
		$(document).ready(function() {
			ShowStatus('<%= UreqUUID %>');
		});
	</script>

</body>
</html>
