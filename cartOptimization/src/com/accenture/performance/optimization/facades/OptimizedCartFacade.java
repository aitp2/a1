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
package com.accenture.performance.optimization.facades;

import de.hybris.platform.commercefacades.order.CartFacade;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;


/**
 *
 */
public interface OptimizedCartFacade extends CartFacade
{
	public OptimizedCartData getSessionCartData();

	//public String getSessionCartGuid();

	//public boolean hasSessionCart();

	//public boolean hasEntries();

	//public CartModificationData updateCartEntry(final long entryNumber, final long quantity) throws CommerceCartModificationException;

}
