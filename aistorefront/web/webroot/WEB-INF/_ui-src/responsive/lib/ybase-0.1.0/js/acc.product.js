ACC.product = {

    _autoload: [
      
        "enableStorePickupButton",
        "enableVariantSelectors",
        "bindFacets"
    ],


    bindFacets: function () {
        $(document).on("click", ".js-show-facets", function (e) {
            e.preventDefault();
            var selectRefinementsTitle = $(this).data("selectRefinementsTitle");
            ACC.colorbox.open(selectRefinementsTitle, {
                href: ".js-product-facet",
                inline: true,
                width: "480px",
                onComplete: function () {
                    $(document).on("click", ".js-product-facet .js-facet-name", function (e) {
                        e.preventDefault();
                        $(".js-product-facet  .js-facet").removeClass("active");
                        $(this).parents(".js-facet").addClass("active");
                        $.colorbox.resize()
                    })
                },
                onClosed: function () {
                    $(document).off("click", ".js-product-facet .js-facet-name");
                }
            });
        });
        enquire.register("screen and (min-width:" + screenSmMax + ")", function () {
            $("#cboxClose").click();
        });
    },


    enableAddToCartButton: function () {
        $('.js-enable-btn').each(function () {
            if (!($(this).hasClass('outOfStock') || $(this).hasClass('out-of-stock'))) {
                $(this).removeAttr("disabled");
            }
        });
        $("#addToCartButton").click(ACC.product.bindToAddToCartForm);
    },

    enableVariantSelectors: function () {
        $('.variant-select').removeAttr("disabled");
    },

    bindToAddToCartForm: function () {
        //var addToCartForm = $('.add_to_cart_form');
        ACC.product.showRequest;
		var result = getAuthToken();
		
        //addToCartForm.ajaxForm({
        //	beforeSubmit:,
        //	success: ACC.product.displayAddToCartPopup
        // });    
        setTimeout(function(){
        	$ajaxCallEvent  = true;
         }, 2000);
     },
     doAddToCart:function(result){
    	 var options = {
 			    type : 'POST',
 			    url : "/cartOptimizationWebservice/v2/"+result.siteId+"/users/current/carts/"+result.cartUid+"/entries",
 			   headers: {Authorization: "Bearer "+result.token },
 			   data:{
 				   "code":$(".add_to_cart_form input[name='productCodePost']").val(),
 				   "qty":$(".add_to_cart_form #qty").val(),
 				   "fields":"FULL"
 			   },
 			    dataType:"json",
 			    async:false,
 			    error : function(request) {
 			    },
 			    success : function(data) {
 			    	ACC.product.displayAddToCartPopup(data);
 			    }
 			  };
 			  $.ajax(options);
     },
     showRequest: function(arr, $form, options) {  
    	 if($ajaxCallEvent)
    		{
    		 $ajaxCallEvent = false;
    		 return true;
    		}   	
    	 return false;
 
    },

    bindToAddToCartStorePickUpForm: function () {
        var addToCartStorePickUpForm = $('#colorbox #add_to_cart_storepickup_form');
        addToCartStorePickUpForm.ajaxForm({success: ACC.product.displayAddToCartPopup});
    },

    enableStorePickupButton: function () {
        $('.js-pickup-in-store-button').removeAttr("disabled");
    },

    displayAddToCartPopup: function (cartResult) {
    	$ajaxCallEvent=true;
        //$('#addToCartLayer').remove();
        if (typeof ACC.minicart.updateMiniCartDisplay == 'function') {
            ACC.minicart.updateMiniCartDisplay();
        }
        var cartIcon = "";
        var images = cartResult.entry.product.images;
        for(var i in images){
        	if(images[i].format=='cartIcon'){
        		cartIcon = images[i].url;
        		break;
        	}
        }
        $("#addToCartLayerPop #addToCart_image").attr("href",cartResult.entry.product.url);
        $("#addToCartLayerPop #addToCart_image img").attr("src",cartIcon);
        $("#addToCartLayerPop #addToCart_product").attr("href",cartResult.entry.product.url);
        $("#addToCartLayerPop #addToCart_product").html(cartResult.entry.product.name);
        $("#addToCartLayerPop .qty span:eq(1)").html(cartResult.quantityAdded);
        $("#addToCartLayerPop .price").html(cartResult.entry.basePrice.value);
 
        var titleHeader = $('#addToCartTitle').html();
        ACC.colorbox.open(titleHeader, {
            html: $("#addToCartLayerPop").html(),
            width: "460px"
        });
        /* remove the addtocart track
        var productCode = $(".add_to_cart_form input[name='productCodePost']").val();
        var quantityField = $(".add_to_cart_form #qty").val();

        var quantity = 1;
        if (quantityField != undefined) {
            quantity = quantityField;
        }
        var cartAnalyticsData = cartResult.cartAnalyticsData;

        var cartData = {
            "cartCode": cartAnalyticsData.cartCode,
            "productCode": productCode, "quantity": quantity,
            "productPrice": cartAnalyticsData.productPostPrice,
            "productName": cartAnalyticsData.productName
        };
        ACC.track.trackAddToCart(productCode, quantity, cartData);
        */
    }
};

$(document).ready(function () {
	$ajaxCallEvent = true;
    ACC.product.enableAddToCartButton();
});