////////////////////////////////////////////////////////////////////////////////////////////////////
//
// File: /_common/request_actions.js
//
// Functions needed to process a request - Used by both lastreq.jsp and map.jsp
//
////////////////////////////////////////////////////////////////////////////////////////////////////

	//
	// Common Worker Function
	//

	function _reqAct_OpenAjaxDlg(ReqID, Caller, DialogURL) {

		$.ajax({

			async: true,
			cache: false,
			type: 'POST',
			dataType: 'text',
			url: DialogURL,
			data: { ReqID: ReqID, Caller: Caller },

			beforeSend: function() {
				$('#divAjaxWait').show();
			},

			success: function(data) {
				ShowDialog_AJAX(data);
			},

			error: function(jqXHR, textStatus, errorThrown) {
				ShowDialog_AJAX(
					'<div class="CellPadding_9px" align="center">' +
						'<div class="DS-text-title DS-text-exception">An error has occurred</div>' +
						jqXHR.responseText +
					'</div>'
				);
				console.error('_reqAct_OpenAjaxDlg() Error: jqXHR: %o', jqXHR);
				console.error('_reqAct_OpenAjaxDlg() Error: textStatus: %o', textStatus);
				console.error('_reqAct_OpenAjaxDlg() Error: errorThrown: %o', errorThrown);
			},

			complete: function() {
				$('#divAjaxWait').hide();
			}
		});
	}

	//
	// REAL callable functions
	//

	function ShowRequestDetails	(ReqID, Caller) {	_reqAct_OpenAjaxDlg(ReqID, Caller, '../unlock/_dlg_details.jsp');	}	// Show Details and Action Buttons
	function InfoRequest		(ReqID, Caller) {	_reqAct_OpenAjaxDlg(ReqID, Caller, '../unlock/_dlg_info.jsp');		}	// Post an info request
	function ShowHistory		(ReqID, Caller) {	_reqAct_OpenAjaxDlg(ReqID, Caller, '../unlock/_dlg_history.jsp');	}	// Show request history
	function RejectRequest		(ReqID, Caller) {	_reqAct_OpenAjaxDlg(ReqID, Caller, '../unlock/_dlg_reject.jsp');	}	// Reject a request
	function SolveRequest		(ReqID, Caller) {	_reqAct_OpenAjaxDlg(ReqID, Caller, '../unlock/_dlg_solved.jsp');	}	// Set a request as solved

////////////////////////////////////////////////////////////////////////////////////////////////////
//
// End of file: /_common/request_actions.js
//
////////////////////////////////////////////////////////////////////////////////////////////////////
