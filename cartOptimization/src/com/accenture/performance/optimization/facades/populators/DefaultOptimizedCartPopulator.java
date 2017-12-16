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

import com.accenture.performance.optimization.facades.data.OptimizedCartData;


/**
 *
 */
public class DefaultOptimizedCartPopulator<T extends CartData> extends AbstractOptomizedCartPopulator<OptimizedCartData, T>
{


	@Override
	public void populate(final OptimizedCartData source, final T target) throws ConversionException
	{
		addCommon(source, target);
		addTotals(source, target);
		addEntries(source, target);
		addPromotions(source, target);
		//addSavedCartData(source, target);
		addEntryGroups(source, target);
		//addComments(source, target);
		target.setGuid(source.getGuid());
		target.setTotalUnitCount(calcTotalUnitCount(source));
	}



}
