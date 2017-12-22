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

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;


/**
 *
 */
public interface OptimizeModelDealService
{

	public void removePersistentCart(final String cartGuid, final String userid);

	public OptimizedCartData restoreOrCreateCurrentCartData();

	public OptimizedCartData getCartDataForGuidAndSiteAndUser(final String cartguid, final BaseSiteModel currentBaseSite,
			final String userid);

	public OptimizedCartData getCartDataForCodeAndSiteAndUser(final String cartguid, final BaseSiteModel currentBaseSite,
			final String userid);

	public void removeCurrentSessionCart(OptimizedCartData cartData);

	void persistCart(final OptimizedCartData cart);

	public OptimizedCartData createSessionCart();

	public OptimizedCartData getSessionCart(String cartGuid);
}
