ACC.silentorderpost = {

	spinner: $("<img src='" + ACC.config.commonResourcePath + "/images/spinner.gif' />"),
	
	sendRequest:function(callback,dataSend){
		var options = {
			    type : 'GET',
			    url : ACC.config.contextPath + "/auth/getToken",
			    dataType:"json",
			    async:false,
			    error : function(request) {
			    	console.log('something wrong');
			    	console.log(request);
			    },
			    success : function(result) {
			    	callback(result,dataSend);
			    }
		};
		
		$.ajax(options);
	},
	
	bindUseDeliveryAddress: function ()
	{
		$('#useDeliveryAddress').on('change', function ()
		{
			if ($('#useDeliveryAddress').is(":checked"))
			{
				var options = {'countryIsoCode': $('#useDeliveryAddressData').data('countryisocode'), 'useDeliveryAddress': true};
				ACC.silentorderpost.enableAddressForm();
				ACC.silentorderpost.displayCreditCardAddressForm(options, ACC.silentorderpost.useDeliveryAddressSelected);
				ACC.silentorderpost.disableAddressForm();
			}
			else
			{
				ACC.silentorderpost.clearAddressForm();
				ACC.silentorderpost.enableAddressForm();
			}
		});

		if ($('#useDeliveryAddress').is(":checked"))
		{
			var options = {'countryIsoCode': $('#useDeliveryAddressData').data('countryisocode'), 'useDeliveryAddress': true};
			ACC.silentorderpost.enableAddressForm();
			ACC.silentorderpost.displayCreditCardAddressForm(options, ACC.silentorderpost.useDeliveryAddressSelected);
			ACC.silentorderpost.disableAddressForm();
		}
	},

	bindSavedPaymentEntryButton:function(result,dataSend)
	{
		$(".saved-payment-entry button").click(function(){
			console.log('cli')
			var data={
				'selectedpaymentmethodid':$(this).attr('attr-selectedpaymentmethodid')
			};
			
			ACC.silentorderpost.sendRequest(ACC.silentorderpost.selectPayment,data);
		});
	},
	
	selectPayment:function(result,dataSend){
		var options = {
				type : 'PUT',
				url : "/cartOptimizationWebservice/v2/"+ACC.config.siteId+"/users/current/carts/"+result.cartUid+"/paymentdetails?paymentDetailsId="+dataSend.selectedpaymentmethodid,
				headers : {
					Authorization : "Bearer " + result.token
				},
				async : false,
				error : function(request) {
					console.log("something wrong for selectDeliveryAddress");
					console.log(request);
				},
				success : function(data) {
					console.log('data:'+data);
					window.location = ACC.config.contextPath+'/'+ACC.config.language+'/checkout/multi/summary/view';
				}
			};
			
			$.ajax(options);
	},
	
	bindSubmitSilentOrderPostForm: function ()
	{
		$('.submit_silentOrderPostForm').click(function ()
		{
			
			ACC.common.blockFormAndShowProcessingMessage($(this));
			$('.billingAddressForm').filter(":hidden").remove();
			ACC.silentorderpost.enableAddressForm();
			
			var data = {};
			ACC.silentorderpost.sendRequest(ACC.silentorderpost.createPayment,data);
			//$('#silentOrderPostForm').submit();
		});
	},
	
	createPayment:function(result,dataSend){
		var options = {
				type : 'POST',
				url : "/cartOptimizationWebservice/v2/"+ACC.config.siteId+"/users/current/carts/"+result.cartUid+"/paymentdetails",
				
				headers : {
					Authorization : "Bearer " + result.token
				},
				data : {
					"accountHolderName" : $("input[name='card_nameOnCard']").val(),
					"cardNumber" : $("input[name='card_accountNumber']").val(),
					"cardType" : $("select[name='card_cardType'] option:selected").val(),
					"expiryMonth" : $("select[name='card_expirationMonth'] option:selected").val(),
					"expiryYear" : $("select[name='card_expirationYear'] option:selected").val(),
					//"issueNumber" : $("input[name='card_cvNumber']").val(),
					
					//"startMonth" : $("input[name='townCity']").val(),
					//"startYear" : $("input[name='postcode']").val(),
					//"subscriptionId" : $("select[name='countryIso'] option:selected").val(),
					//"defaultPaymentInfo" : $("select[name='regionIso'] option:selected").val(),
					
					"saved" : $("input[name='savePaymentInfo']").is(":checked"),
					
					//"id" : defaultAddressFlag,
					"billingAddress.titleCode" : $("select[name='billTo_titleCode'] option:selected").val(),//billTo_titleCode
					"billingAddress.firstName" : $("input[name='billTo_firstName']").val(),//billTo_firstName
					"billingAddress.lastName" : $("input[name='billTo_lastName']").val(),
					"billingAddress.line1" : $("input[name='billTo_street1']").val(),//billTo_street1
					"billingAddress.line2" : $("input[name='billTo_street2']").val(),//
					"billingAddress.town" :  $("input[name='billTo_city']").val(),//billTo_city
					"billingAddress.postalCode" : $("input[name='billTo_postalCode']").val(),//billTo_postalCode
					"billingAddress.country.isocode" : $("select[name='billTo_country'] option:selected").val(),//billTo_country
					"billingAddress.phone" : $("input[name='billTo_phoneNumber']").val()
					//"region.isocode" : defaultAddressFlag,
					//"defaultAddress":
						
				},
				async : false,
				error : function(request) {
					console.log("something wrong for addDeliveryAddress");
				},
				success : function(data) {
					console.log(data);
					window.location = ACC.config.contextPath+'/'+ACC.config.language+'/checkout/multi/summary/view';
				}
			};
			
			$.ajax(options);
	},
	
	submitSilentOrderPostForm:function(){
		var options = {
				type : 'Get',
				url : "/cartOptimizationWebservice/v2/"+ACC.config.siteId+"/users/current/carts/"+result.cartUid+"/payment/sop/request",
				
				headers : {
					Authorization : "Bearer " + result.token
				},
				async : false,
				error : function(request) {
					console.log("something wrong for selectDeliveryAddress");
					console.log(request);
				},
				success : function(data) {
					console.log('data:'+data);
					window.location = ACC.config.contextPath+'/'+ACC.config.language+'/checkout/multi/payment-method/add';
				}
		};
			
		$.ajax(options);
	},

	bindCycleFocusEvent: function ()
	{
		$('#lastInTheForm').blur(function ()
		{
			$('#silentOrderPostForm [tabindex$="10"]').focus();
		})
	},

	isEmpty: function (obj)
	{
		if (typeof obj == 'undefined' || obj === null || obj === '') return true;
		return false;
	},

	disableAddressForm: function ()
	{
		$('input[id^="address\\."]').prop('disabled', true);
		$('select[id^="address\\."]').prop('disabled', true);
	},

	enableAddressForm: function ()
	{
		$('input[id^="address\\."]').prop('disabled', false);
		$('select[id^="address\\."]').prop('disabled', false);
	},

	clearAddressForm: function ()
	{
		$('input[id^="address\\."]').val("");
		$('select[id^="address\\."]').val("");
	},

	useDeliveryAddressSelected: function ()
	{
		if ($('#useDeliveryAddress').is(":checked"))
		{
			$('#address\\.country').val($('#useDeliveryAddressData').data('countryisocode'));
			ACC.silentorderpost.disableAddressForm();
		}
		else
		{
			ACC.silentorderpost.clearAddressForm();
			ACC.silentorderpost.enableAddressForm();
		}
	},
	
	

	bindCreditCardAddressForm: function ()
	{
		$('#billingCountrySelector :input').on("change", function ()
		{
			var countrySelection = $(this).val();
			var options = {
				'countryIsoCode': countrySelection,
				'useDeliveryAddress': false
			};
			ACC.silentorderpost.displayCreditCardAddressForm(options);
		})
	},

	displayCreditCardAddressForm: function (options, callback)
	{
		$.ajax({ 
			url: ACC.config.encodedContextPath + '/checkout/multi/sop/billingaddressform',
			async: true,
			data: options,
			dataType: "html",
			beforeSend: function ()
			{
				$('#billingAddressForm').html(ACC.silentorderpost.spinner);
			}
		}).done(function (data)
				{
					$("#billingAddressForm").html(data);
					if (typeof callback == 'function')
					{
						callback.call();
					}
				});
	}
}

$(document).ready(function ()
{
	with (ACC.silentorderpost)
	{
		bindUseDeliveryAddress()
		bindSubmitSilentOrderPostForm();
		bindCreditCardAddressForm();
		bindSavedPaymentEntryButton();
	}

	// check the checkbox
	$("#useDeliveryAddress").click();
});
