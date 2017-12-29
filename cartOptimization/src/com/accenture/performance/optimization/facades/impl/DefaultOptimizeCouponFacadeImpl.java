package com.accenture.performance.optimization.facades.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.service.OptimizeCartService;
import com.accenture.performance.optimization.service.OptimizeCouponService;

import de.hybris.platform.commercefacades.voucher.data.VoucherData;
import de.hybris.platform.commercefacades.voucher.exceptions.VoucherOperationException;
import de.hybris.platform.couponfacades.facades.impl.DefaultCouponFacade;
import de.hybris.platform.couponservices.service.data.CouponResponse;
import de.hybris.platform.servicelayer.util.ServicesUtil;

public class DefaultOptimizeCouponFacadeImpl extends DefaultCouponFacade 
{
	private OptimizeCartService cartService;
	private OptimizeCouponService couponService;
	
	@Override
	public void applyVoucher(String voucherCode) throws VoucherOperationException 
	{
		ServicesUtil.validateParameterNotNullStandardMessage((String)"coupon code", (Object)voucherCode);
        CouponResponse couponResponse = this.applyIfCartExistsACN(voucherCode, (voucherCodeStr,cart) -> getCouponService().redeemCoupon(voucherCodeStr, cart) );
        
        if (BooleanUtils.isNotTrue((Boolean)couponResponse.getSuccess())) 
        {
            throw new VoucherOperationException(couponResponse.getMessage());
        }
    }
	
	protected <R> R applyIfCartExistsACN(String code, BiFunction<String, OptimizedCartData, R> orderConsumer) throws VoucherOperationException 
	{
		OptimizedCartData cart = this.getCartService().getSessionOptimizedCart();
		if (Objects.nonNull((Object) cart)) 
		{
			return orderConsumer.apply(code, cart);
		}
		
		throw new VoucherOperationException("No cart was found in session");
	}
	
	@Override
	public void releaseVoucher(String voucherCode) throws VoucherOperationException 
	{
		ServicesUtil.validateParameterNotNullStandardMessage((String)"coupon code", (Object)voucherCode);
        this.acceptIfCartExistsACN(voucherCode, (voucherCodeStr, optimizedCartData) -> getCouponService().releaseCouponCode(voucherCodeStr, optimizedCartData));

    }
	
	protected void acceptIfCartExistsACN(String code, BiConsumer<String, OptimizedCartData> orderConsumer) throws VoucherOperationException 
	{
		OptimizedCartData cart = this.getCartService().getSessionOptimizedCart();
		if (!Objects.nonNull((Object) cart)) 
		{
			throw new VoucherOperationException("No cart was found in session");
		}
		
		orderConsumer.accept(code, cart);
	}

	@Override
	public List<VoucherData> getVouchersForCart() 
	{
		return this.applyIfCartExists(this::getCouponsForOrder);
	}
	
	protected List<VoucherData> getCouponsForOrder(OptimizedCartData order) 
	{
		 Collection<String> couponCodesForOrder = order.getAppliedCouponCodes();
	        if (CollectionUtils.isNotEmpty(couponCodesForOrder)) {
	            return couponCodesForOrder
	            		.stream()
	            		.map(code -> getCouponCodeModelConverter().convert(code))
	            		.collect(Collectors.toList());
	        }
	        
	        return Collections.emptyList();
    }
	
	/**
	 * @return the cartService
	 */
	@Override
	public OptimizeCartService getCartService() 
	{
		return cartService;
	}

	/**
	 * @param cartService the cartService to set
	 */
	public void setCartService(OptimizeCartService cartService) 
	{
		super.setCartService(cartService);
		this.cartService = cartService;
	}

	/**
	 * @return the couponService
	 */
	@Override
	public OptimizeCouponService getCouponService() 
	{
		return couponService;
	}

	/**
	 * @param couponService the couponService to set
	 */
	public void setCouponService(OptimizeCouponService couponService) 
	{
		super.setCouponService(couponService);
		this.couponService = couponService;
	}
}
