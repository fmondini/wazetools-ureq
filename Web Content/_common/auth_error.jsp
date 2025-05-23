<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="net.danisoft.dslib.*"
%>
<!doctype html>
<html>
<body>
<%
	new MsgTool(session).setSlideText(
		"LogIn Error / Errore di Accesso",
		"<div class=\"mdc-layout-grid__inner\">" +
			"<div class=\"" + MdcTool.Layout.Cell(6, 4, 2) + " DS-border-rg\">" +
				"<div class=\"DS-card-body\">" +
					"<div class=\"DS-text-fixed DS-text-exception DS-text-bold\">[EN]</div>" +
				"</div>" +
				"<div class=\"DS-card-none\">" +
					"<div>The credentials provided are invalid. <b>Please make sure you have the correct credentials to log in here</b>, otherwise access will be denied.</div>" +
				"</div>" +
			"</div>" +
			"<div class=\"" + MdcTool.Layout.Cell(6, 4, 2) + "\">" +
				"<div class=\"DS-card-body\">" +
					"<div class=\"DS-text-fixed DS-text-exception DS-text-bold\">[IT]</div>" +
				"</div>" +
				"<div class=\"DS-card-none\">" +
					"<div>Le credenziali fornite non sono valide. <b>Assicurati di fornire nome utente e password corretti</b>, altrimenti non sar&agrave; possibile accedere.</div>" +
				"</div>" +
			"</div>" +
		"</div>"
	);

	response.sendRedirect("../home/");
%>
</body>
</html>
