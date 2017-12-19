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

import de.hybris.platform.commercefacades.order.converters.populator.CartModificationPopulator;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;


/**
 *
 */
public class DefaultOptinizedCartModification extends CartModificationPopulator
{

	private Converter<OptimizedCartEntryData, OrderEntryData> optimizedOrderEntryConverter;

	@Override
	public void populate(final CommerceCartModification source, final CartModificationData target)
	{
		super.populate(source, target);
		if (source.getEntryData() != null)
		{
			target.setEntry(getOptimizedOrderEntryConverter().convert(source.getEntryData()));
			if (source.getEntryData().getCartData() != null)
			{
				target.setCartCode(source.getEntryData().getCartData().getCode());
			}
		}
	}

	/**
	 * @return the optimizedOrderEntryConverter
	 */
	public Converter<OptimizedCartEntryData, OrderEntryData> getOptimizedOrderEntryConverter()
	{
		return optimizedOrderEntryConverter;
	}

	/**
	 * @param optimizedOrderEntryConverter
	 *           the optimizedOrderEntryConverter to set
	 */
	public void setOptimizedOrderEntryConverter(
			final Converter<OptimizedCartEntryData, OrderEntryData> optimizedOrderEntryConverter)
	{
		this.optimizedOrderEntryConverter = optimizedOrderEntryConverter;
	}



}
