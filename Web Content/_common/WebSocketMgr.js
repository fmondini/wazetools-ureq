/*
	So you're curious... Gotcha!
	Don't take this code as an example, it was written by a cow.
*/

const WS_UREQ_DebugActiv = true;
const WS_UREQ_ScriptDesc = 'UREQ WebSocket Management Script';
const WS_UREQ_ScriptVers = '0.8.beta';
const WS_UREQ_ScriptDate = 'Jul/2024';
const WS_UREQ_ScriptAuth = 'fmondini [at] danisoft [dot] net';
const WS_UREQ_RServerURI = document.currentScript.dataset.server;
const WS_UREQ_DebugPrefx = 'UREQWSCK';

// The text of the ping/pong messages MUST BE THE SAME as those used in the WebSocket
// client JavaScript file in the UREQ webapp and those in the WUREQM Monitor script
const WS_UREQ_PingMsgTxt = 'UREQPING';
const WS_UREQ_PongMsgTxt = 'UREQPONG'; 

$('<script />')
	.text(
		'$(document).ready(function(){' +
			'WsInit();' +
		'});'
	)
	.appendTo($('head'))
;

/**
 * STARTUP - Create and connect the WebSocket
 */
function WsInit() {

	consoleInfo('[INFO]', WS_UREQ_ScriptDesc + ' version ' + WS_UREQ_ScriptVers);
	consoleInfo('[INFO]', '(c) Copyright ' + WS_UREQ_ScriptDate + ' by ' + WS_UREQ_ScriptAuth);

	consoleDebug('WsInit()', 'Connecting to', WS_UREQ_RServerURI);

	try {

		const wsSocket = new WebSocket(WS_UREQ_RServerURI);

		if (wsSocket != null) {

			var pingIntervalId;

			wsSocket.addEventListener('open', (event) => {
				consoleDebug('Event("' + event.type + '")', 'WebSocket State', decodeReadyState(event));
				pingIntervalId = window.setInterval(function() {
					wsSocket.send(WS_UREQ_PingMsgTxt); // Ping-Pong trick
				}, 60000); // 1 minute
			});

			wsSocket.addEventListener('message', (event) => {
				consoleDebug('Event("' + event.type + '")', 'Received', event.data);
				if (event.data != WS_UREQ_PongMsgTxt)
					ShowWsPopup(event.data);
			});

			wsSocket.addEventListener('close', (event) => {
				consoleDebug('Event("' + event.type + '")', 'WebSocket State', decodeReadyState(event));
				clearInterval(pingIntervalId);
			});

			wsSocket.addEventListener("error", (event) => {
				consoleError('Event("' + event.type + '")', 'WebSocket State', decodeReadyState(event));
			});

		} else
			throw new Error('wsSocket = new WebSocket("' + WS_UREQ_RServerURI + '") failed -> wsSocket is NULL');

	} catch (err) {
		consoleError('WsInit()', err.name, err.message);
	}
}

/**
 * PopUp the "New Request Received" message
 */
function ShowWsPopup(message) {

	try {

		const jMessage = JSON.parse(message);
		const solveType = (jMessage.solv ? 'UNLOCK & SOLVE' : 'UNLOCK ONLY');

		const wsDiv = document.createElement('div');

		$(wsDiv)
			.addClass('DS-back-pastel-blue').addClass('DS-padding-8px').addClass('DS-border-full').addClass('DS-border-round')
			.css('bottom', '15px').css('left', '15px').css('position', 'absolute')
			.html(
				'<div class="DS-text-centered">New <b>' + solveType + ' (' + jMessage.lock + ')</b> request received by <b>' + jMessage.user + '</b>(' + jMessage.rank + ')</div>' +
				'<div class="DS-text-compact DS-text-italic DS-text-centered DS-padding-updn-4px">' + jMessage.locn + '</div>' +

				'<div class="DS-text-centered">' +
					'<a id="anchorProcess" href="../unlock/?ReqID=' + jMessage.rqId + '&setLat=' + jMessage.cLat + '&setLon=' + jMessage.cLon + '" target="_blank"></a>' +
					'<input class="DS-text-italic DS-text-blue" type="button" value="Process Request" onClick="document.getElementById(\'anchorProcess\').click();">' +
				'</div>'
			)
			.appendTo($('body'))
			.hide()
			.slideToggle(500)
			.delay(10000)
			.slideToggle(500)
			.click(function () { $(this).remove(); })
			.queue(function () { $(this).remove(); })
		;

		consoleInfo('[UREQ]', 'New ' + solveType + '(' + jMessage.lock + ') request received from ' + jMessage.user + '(' + jMessage.rank + ')');

	} catch(err) {
		consoleError('ShowWsPopup()', err.name, err.message)
	}
}

/**
 * Decode Socket ReadyState
 */
function decodeReadyState(event) {

	let rc = '';

	switch (event.currentTarget.readyState) {
		case WebSocket.CONNECTING:	rc = '(0) WebSocket.CONNECTING';	break;
		case WebSocket.OPEN:		rc = '(1) WebSocket.OPEN';			break;
		case WebSocket.CLOSING:		rc = '(2) WebSocket.CLOSING';		break;
		case WebSocket.CLOSED:		rc = '(3) WebSocket.CLOSED';		break;
		default:					rc = '(' + value + ') UNKNOWN';		break;
	}

	return(rc);
}

function consoleInfo(funcName, message) {
	console.log('[%s] [i] %s: %o', WS_UREQ_DebugPrefx, funcName, message);
}

function consoleDebug(funcName, name, data) {
	if (WS_UREQ_DebugActiv)
		console.log('[%s] [d] %s: %s: %o', WS_UREQ_DebugPrefx, funcName, name, data);
}

function consoleError(funcName, errName, errMess) {
	console.error('%s:', funcName);
	console.error(' - errName: "%s"', errName);
	console.error(' - errMess: "%s"', errMess);
}
