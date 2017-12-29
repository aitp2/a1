package com.accenture.performance.optimization.service;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;

import de.hybris.platform.couponservices.service.data.CouponResponse;
import de.hybris.platform.couponservices.services.CouponService;

public interface OptimizeCouponService extends CouponService {
	public CouponResponse redeemCoupon(String couponCode, OptimizedCartData cart);
	
	public void releaseCouponCode(String couponCode, OptimizedCartData optimizedCartData);
	
	public CouponResponse verifyCouponCode(String couponCode, OptimizedCartData optimizedCartData);

}
