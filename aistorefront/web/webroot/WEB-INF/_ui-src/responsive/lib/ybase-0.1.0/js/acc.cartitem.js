ACC.cartitem = {

	_autoload: [
		"bindCartItem"
	],

	submitTriggered: false,

	bindCartItem: function ()
	{

		$('.js-execute-entry-action-button').on("click", function ()
		{
			var result = getAuthToken();
			
			var entryAction = $(this).data("entryAction");
//			var entryActionUrl =  $(this).data("entryActionUrl");
			var entryProductCode =  $(this).data("entryProductCode");
			var entryInitialQuantity =  $(this).data("entryInitialQuantity");
			var actionEntryNumbers =  $(this).data("actionEntryNumbers");
			var entryActionUrl = "/cartOptimizationWebservice/v2/"+result.siteId+"/users/current/carts/"+result.cartUid+"/entries/"+actionEntryNumbers;

			if(entryAction == 'REMOVE')
			{
				ACC.track.trackRemoveFromCart(entryProductCode, entryInitialQuantity);
			}

			var cartEntryActionForm = $("#cartEntryActionForm");
//			var entryNumbers = actionEntryNumbers.toString().split(';');
//			entryNumbers.forEach(function(entryNumber) {
//				var entryNumbersInput = $("<input>").attr("type", "hidden").attr("name", "entryNumbers").val(entryNumber);
//				cartEntryActionForm.append($(entryNumbersInput));
//			});
//			cartEntryActionForm.attr('action', entryActionUrl).submit();
			
			var options = {
	 			   type : 'DELETE',
	 			   url : entryActionUrl,
	 			   headers: {Authorization: "Bearer "+result.token },
	 			   
	 			   dataType:"json",
	 			   async:false,
	 			   error : function(request) {
	 			   },
	 			   success : function(data) {
	 			    //window.location.reload(true);
	 			   }
	 		};
			
	 		$.ajax(options);
	 		window.location.reload(true);
		
			
		});

		$('.js-update-entry-quantity-input').on("blur", function (e)
		{
			ACC.cartitem.handleUpdateQuantity(this, e);

		}).on("keyup", function (e)
		{
			return ACC.cartitem.handleKeyEvent(this, e);
		}
		).on("keydown", function (e)
		{
			return ACC.cartitem.handleKeyEvent(this, e);
		}
		);
	},

	handleKeyEvent: function (elementRef, event)
	{
		//console.log("key event (type|value): " + event.type + "|" + event.which);

		if (event.which == 13 && !ACC.cartitem.submitTriggered)
		{
			ACC.cartitem.submitTriggered = ACC.cartitem.handleUpdateQuantity(elementRef, event);
			return false;
		}
		else 
		{
			// Ignore all key events once submit was triggered
			if (ACC.cartitem.submitTriggered)
			{
				return false;
			}
		}

		return true;
	},

	handleUpdateQuantity: function (elementRef, event)
	{

		var form = $(elementRef).closest('form');

		var productCode = form.find('input[name=productCode]').val();
		var initialCartQuantity = form.find('input[name=initialQuantity]').val();
		var newCartQuantity = form.find('input[name=quantity]').val();
		var entryNumber = form.find('input[name=entryNumber]').val();

		if(initialCartQuantity != newCartQuantity)
		{
			ACC.track.trackUpdateCart(productCode, initialCartQuantity, newCartQuantity);
			var result = getAuthToken();
			//form.submit();
			 var options = {
		 			    type : 'PUT',
		 			    url : "/cartOptimizationWebservice/v2/"+result.siteId+"/users/current/carts/"+result.cartUid+"/entries/"+entryNumber,
		 			   headers: {Authorization: "Bearer "+result.token },
		 			   data:{
		 				   "qty":newCartQuantity
		 			   },
		 			    dataType:"json",
		 			    async:false,
		 			    error : function(request) {
		 			    },
		 			    success : function(data) {
		 			    	window.location.reload(true);
		 			    }
		 			  };
		 			  $.ajax(options);
			

			return true;
		}

		return false;
	}
};

