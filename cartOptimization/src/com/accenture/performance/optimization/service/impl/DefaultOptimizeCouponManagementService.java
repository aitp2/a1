package com.accenture.performance.optimization.service.impl;

import org.apache.commons.lang.BooleanUtils;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.service.OptimizeCouponManagementService;

import de.hybris.platform.couponservices.CouponServiceException;
import de.hybris.platform.couponservices.service.data.CouponResponse;
import de.hybris.platform.couponservices.services.impl.DefaultCouponManagementService;
import de.hybris.platform.servicelayer.user.UserService;

public class DefaultOptimizeCouponManagementService extends DefaultCouponManagementService implements OptimizeCouponManagementService {
	
	private UserService userService;
	
	@Override
	public boolean redeem(String couponCode, OptimizedCartData pptimizedCartData) {
		CouponResponse response = this.verifyCouponCode(couponCode, pptimizedCartData);
		if (BooleanUtils.isTrue((Boolean) response.getSuccess())) {
			return true;
		}
		
		throw new CouponServiceException(response.getMessage());
	}
	
	@Override
	public CouponResponse verifyCouponCode(String couponCode, OptimizedCartData abstractOrder) {
		return this.validateCouponCode(couponCode, getUserService().getUserForUID(abstractOrder.getUserId()));
	}
	
	/**
	 * @return the userService
	 */
	public UserService getUserService() {
		return userService;
	}

	/**
	 * @param userService the userService to set
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}


}
