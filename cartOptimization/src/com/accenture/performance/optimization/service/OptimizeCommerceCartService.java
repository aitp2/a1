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
package com.accenture.performance.optimization.service;

import de.hybris.platform.commerceservices.order.CommerceCartService;
import de.hybris.platform.order.exceptions.CalculationException;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;


/**
 *
 */
public interface OptimizeCommerceCartService extends CommerceCartService
{
	public boolean calculateCart(final OptimizedCartData optimizedCartData, final boolean enableHook) throws CalculationException;

	public double calculateOneEntries(final OptimizedCartEntryData entry, final boolean forceRecalculate)
			throws CalculationException;

}
