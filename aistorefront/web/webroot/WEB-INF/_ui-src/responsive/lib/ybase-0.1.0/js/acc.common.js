ACC.common = {
	currentCurrency: $("main").data('currencyIsoCode') || "USD",
	processingMessage: $("<img src='" + ACC.config.commonResourcePath + "/images/spinner.gif'/>"),


	blockFormAndShowProcessingMessage: function (submitButton)
	{
		var form = submitButton.parents('form:first');
		form.block({ message: ACC.common.processingMessage });
	},

	refreshScreenReaderBuffer: function ()
	{
		// changes a value in a hidden form field in order
		// to trigger a buffer update in a screen reader
		$('#accesibility_refreshScreenReaderBufferField').attr('value', new Date().getTime());
	},

	checkAuthenticationStatusBeforeAction: function (actionCallback)
	{
		$.ajax({
			url: ACC.config.authenticationStatusUrl,
			statusCode: {
				401: function () {
					location.href = ACC.config.loginUrl;
				}
			},
			success: function (data) {
				if (data == "authenticated") {
					actionCallback();
				}
			}
		});
	}
};





/* Extend jquery with a postJSON method */
jQuery.extend({
	postJSON: function (url, data, callback)
	{
		return jQuery.post(url, data, callback, "json");
	}
});

window.getAuthToken =function(){
	var resultData;
	 var options = {
			    type : 'GET',
			    url : "/auth/getToken",
			    dataType:"json",
			    async:false,
			    error : function(request) {
			    },
			    success : function(result) {
			    	resultData = result;
			    }
			  };
			  $.ajax(options);
    return resultData;
}

// add a CSRF request token to POST ajax request if its not available
$.ajaxPrefilter(function (options, originalOptions, jqXHR)
{
	// Modify options, control originalOptions, store jqXHR, etc
	if (options.type === "post" || options.type === "POST")
	{
		var noData = (typeof options.data === "undefined");
		if (noData)
		{
			options.data = "CSRFToken=" + ACC.config.CSRFToken;
		}
		else
		{
			var patt1 = /application\/json/i;
			if (options.data instanceof window.FormData)
			{
				options.data.append("CSRFToken", ACC.config.CSRFToken);
			}
			// if its a json post, then append CSRF to the header. 
			else if (patt1.test(options.contentType))
			{
				jqXHR.setRequestHeader('CSRFToken', ACC.config.CSRFToken);
			}
			else if (options.data.indexOf("CSRFToken") === -1)
			{
				options.data = options.data + "&" + "CSRFToken=" + ACC.config.CSRFToken;
			}
		}
		
	}
});
