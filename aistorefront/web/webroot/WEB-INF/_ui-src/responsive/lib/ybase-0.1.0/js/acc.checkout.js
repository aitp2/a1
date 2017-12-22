ACC.checkout = {

	_autoload: [
		"bindCheckO",
		"bindForms",
		"bindSavedPayments",
		"bindAddressEntryButton"
	],

	addDeliveryAddress:function(result) {
		var defaultAddressFlag = $("input[name='defaultAddress']").is(":checked");
		if(defaultAddressFlag){
			//
		}else{
			defaultAddressFlag = false;
		}
		
		var options = {
			type : 'POST',
			url : "/cartOptimizationWebservice/v2/"+ACC.config.siteId+"/users/current/carts/"+result.cartUid+"/addresses/deliveries",
			
			headers : {
				Authorization : "Bearer " + result.token
			},
			data : {
				"id" : $("input[name='addressId']").val(),
				"titleCode" : $("select[name='titleCode'] option:selected").val(),
				"firstName" : $("input[name='firstName']").val(),
				"lastName" : $("input[name='lastName']").val(),
				"line1" : $("input[name='line1']").val(),
				"line2" : $("input[name='line2']").val(),
				"town" : $("input[name='townCity']").val(),
				"postalCode" : $("input[name='postcode']").val(),
				"country.isocode" : $("select[name='countryIso'] option:selected").val(),
				"region.isocode" : $("select[name='regionIso'] option:selected").val(),
				"defaultAddress" : defaultAddressFlag
					
			},
			dataType : "json",
			async : false,
			error : function(request) {
				console.log("something wrong for addDeliveryAddress");
			},
			success : function(data) {
				console.log(data);
				window.location = ACC.config.contextPath+'/'+ACC.config.siteId+'/'+ACC.config.language+'/checkout/multi/delivery-method/choose';
			}
		};
		
		$.ajax(options);
	},

	selectDeliveryAddress:function(result,dataSend){
		var options = {
				type : 'PUT',
				url : "/cartOptimizationWebservice/v2/"+ACC.config.siteId+"/users/current/carts/"+result.cartUid+"/addresses/delivery?addressId="+dataSend.addressId,
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
					window.location = ACC.config.contextPath+'/'+ACC.config.siteId+'/'+ACC.config.language+'/checkout/multi/delivery-method/choose';
				}
			};
			
			$.ajax(options);
	},
	
	selectDeliveryMethod:function(result,dataSend){
		var options = {
				type : 'PUT',
				url : "/cartOptimizationWebservice/v2/"+ACC.config.siteId+"/users/current/carts/"+result.cartUid+"/deliverymode?deliveryModeId="+dataSend.deliveryModeCode,
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
					window.location = ACC.config.contextPath+'/'+ACC.config.siteId+'/'+ACC.config.language+'/checkout/multi/payment-method/add';
				}
			};
			
			$.ajax(options);
	},
	
	bindForms:function(){

		$(document).on("click","#addressSubmit",function(e){
			e.preventDefault();
			var options = {
				    type : 'GET',
				    url : ACC.config.contextPath + "/auth/getToken",
				    dataType:"json",
				    async:false,
				    error : function(request) {
				    	console.log('something wrong');
				    },
				    success : function(result) {
				    	ACC.checkout.addDeliveryAddress(result);
				    }
			};
			$.ajax(options);
		})
		
		$(document).on("click","#deliveryMethodSubmit",function(e){
			e.preventDefault();
			
			var deliveryModeCode = $("select[name='delivery_method'] option:selected").val();
			var data = {'deliveryModeCode':deliveryModeCode};
				
			ACC.checkout.sendRequest(ACC.checkout.selectDeliveryMethod,data);
		})

	},

	bindAddressEntryButton:function(){
		$('.addressEntry button').click(function(){
			var addressId = $(this).attr('attr-addressID');
			var data = {'addressId':addressId};
			
			ACC.checkout.sendRequest(ACC.checkout.selectDeliveryAddress,data);
		});
		
	},
	
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
	
	bindSavedPayments:function(){
		$(document).on("click",".js-saved-payments",function(e){
			e.preventDefault();

			var title = $("#savedpaymentstitle").html();

			$.colorbox({
				href: "#savedpaymentsbody",
				inline:true,
				maxWidth:"100%",
				opacity:0.7,
				//width:"320px",
				title: title,
				close:'<span class="glyphicon glyphicon-remove"></span>',
				onComplete: function(){
				}
			});
		})
	},

	bindCheckO: function ()
	{
		var cartEntriesError = false;
		
		// Alternative checkout flows options
		$('.doFlowSelectedChange').change(function ()
		{
			if ('multistep-pci' == $('#selectAltCheckoutFlow').val())
			{
				$('#selectPciOption').show();
			}
			else
			{
				$('#selectPciOption').hide();

			}
		});



		$('.js-continue-shopping-button').click(function ()
		{
			var checkoutUrl = $(this).data("continueShoppingUrl");
			window.location = checkoutUrl;
		});
		
		$('.js-create-quote-button').click(function ()
		{
			$(this).prop("disabled", true);
			var createQuoteUrl = $(this).data("createQuoteUrl");
			window.location = createQuoteUrl;
		});

		
		$('.expressCheckoutButton').click(function()
				{
					document.getElementById("expressCheckoutCheckbox").checked = true;
		});
		
		$(document).on("input",".confirmGuestEmail,.guestEmail",function(){
			  
			  var orginalEmail = $(".guestEmail").val();
			  var confirmationEmail = $(".confirmGuestEmail").val();
			  
			  if(orginalEmail === confirmationEmail){
			    $(".guestCheckoutBtn").removeAttr("disabled");
			  }else{
			     $(".guestCheckoutBtn").attr("disabled","disabled");
			  }
		});
		
		$('.js-continue-checkout-button').click(function ()
		{
			var checkoutUrl = $(this).data("checkoutUrl");
			
			cartEntriesError = ACC.pickupinstore.validatePickupinStoreCartEntires();
			if (!cartEntriesError)
			{
				var expressCheckoutObject = $('.express-checkout-checkbox');
				if(expressCheckoutObject.is(":checked"))
				{
					window.location = expressCheckoutObject.data("expressCheckoutUrl");
				}
				else
				{
					var flow = $('#selectAltCheckoutFlow').val();
					if ( flow == undefined || flow == '' || flow == 'select-checkout')
					{
						// No alternate flow specified, fallback to default behaviour
						window.location = checkoutUrl;
					}
					else
					{
						// Fix multistep-pci flow
						if ('multistep-pci' == flow)
						{
						flow = 'multistep';
						}
						var pci = $('#selectPciOption').val();

						// Build up the redirect URL
						var redirectUrl = checkoutUrl + '/select-flow?flow=' + flow + '&pci=' + pci;
						window.location = redirectUrl;
					}
				}
			}
			return false;
		});

	}

};
