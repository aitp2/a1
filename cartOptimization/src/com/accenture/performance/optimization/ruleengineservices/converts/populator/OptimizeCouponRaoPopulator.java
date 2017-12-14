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

import de.hybris.platform.converters.Populator;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.google.common.base.Preconditions;


/**
 *
 */
public class OptimizeCouponRaoPopulator implements Populator<OptimizedCartData, CartRAO>
{

	@Override
	public void populate(final OptimizedCartData cartData, final CartRAO cartRao) throws ConversionException
	{
		Preconditions.checkNotNull(cartData, "Cart model is not expected to be NULL here");
		Preconditions.checkNotNull(cartRao, "Cart RAO is not expected to be NULL here");
		// TODO: no implement coupon now
		//Collection appliedCouponCodes = cartData.getAppliedCouponCodes();
		//		if (CollectionUtils.isNotEmpty(appliedCouponCodes))
		//		{
		//			cartRao.setCoupons(
		//					(List) appliedCouponCodes.stream().map(this::getCouponRAO).filter(Objects::nonNull).collect(Collectors.toList()));
		//		}
	}

}
