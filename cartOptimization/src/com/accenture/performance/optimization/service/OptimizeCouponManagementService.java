package com.accenture.performance.optimization.service;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;

import de.hybris.platform.couponservices.service.data.CouponResponse;
import de.hybris.platform.couponservices.services.CouponManagementService;

public interface OptimizeCouponManagementService extends CouponManagementService 
{
	public boolean redeem(String couponCode, OptimizedCartData pptimizedCartData);
	public CouponResponse verifyCouponCode(String couponCode, OptimizedCartData abstractOrder);
}
