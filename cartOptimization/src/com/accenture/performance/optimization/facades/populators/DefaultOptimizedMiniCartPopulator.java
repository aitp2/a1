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
package com.accenture.performance.optimization.facades.populators;

import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import org.springframework.util.Assert;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;


/**
 *
 */
public class DefaultOptimizedMiniCartPopulator<T extends CartData> extends AbstractOptomizedCartPopulator<OptimizedCartData, T>
{


	@Override
	public void populate(final OptimizedCartData source, final T target) throws ConversionException
	{
		Assert.notNull(target, "Parameter target cannot be null.");

		if (source == null)
		{
			target.setTotalPrice(createZeroPrice());
			target.setDeliveryCost(null);
			target.setSubTotal(createZeroPrice());
			target.setTotalItems(Integer.valueOf(0));
			target.setTotalUnitCount(Integer.valueOf(0));
		}
		else
		{
			addCommon(source, target);
			addTotals(source, target);

			target.setTotalUnitCount(calcTotalUnitCount(source));
		}
	}



}
