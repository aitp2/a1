/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2017 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package com.accenture.performance.optimization.ruleengineservices.converts.populator;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.google.common.base.Preconditions;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.rao.CouponRAO;
import de.hybris.platform.couponservices.services.CouponService;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;


/**
 *
 */
public class OptimizeCouponRaoPopulator implements Populator<OptimizedCartData, CartRAO>
{
	private CouponService couponService;
	
	@Override
	public void populate(final OptimizedCartData cartData, final CartRAO cartRao) throws ConversionException
	{
		Preconditions.checkNotNull(cartData, "Cart model is not expected to be NULL here");
		Preconditions.checkNotNull(cartRao, "Cart RAO is not expected to be NULL here");
	
		Collection<String> appliedCouponCodes = cartData.getAppliedCouponCodes();
		if (CollectionUtils.isNotEmpty(appliedCouponCodes))
		{
			cartRao.setCoupons(
					appliedCouponCodes
					.stream()
					.map(this::getCouponRAO)
					.filter(Objects::nonNull)
					.collect(Collectors.toList())
			);
		}
	}
	
	protected CouponRAO getCouponRAO(String couponCode) {
		return this.getCouponService().getValidatedCouponForCode(couponCode)
				.map(this::toCouponRAO)
				.map(couponRao -> {
					couponRao.setCouponCode(couponCode);
					return couponRao;
				})
				.orElse(null);
	}
	
	protected CouponRAO toCouponRAO(AbstractCouponModel couponModel) {
		CouponRAO couponRao = new CouponRAO();
		couponRao.setCouponId(couponModel.getCouponId());
		return couponRao;
	}

	/**
	 * @return the couponService
	 */
	public CouponService getCouponService() {
		return couponService;
	}

	/**
	 * @param couponService the couponService to set
	 */
	public void setCouponService(CouponService couponService) {
		this.couponService = couponService;
	}

}
