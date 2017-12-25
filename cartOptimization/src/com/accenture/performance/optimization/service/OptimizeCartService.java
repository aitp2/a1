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
import de.hybris.platform.commercefacades.order.data.AddToCartParams;
import de.hybris.platform.commerceservices.order.CommerceCartMergingException;
import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.CartService;

import java.util.List;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.model.OptimizedCartModel;


/**
 *
 */
public interface OptimizeCartService extends CartService
{
	public OptimizedCartData getSessionOptimizedCart();

	@Override
	public boolean hasSessionCart();

	public void setSessionOptimizedCart(OptimizedCartData cartData);

	public CommerceCartModification addToCart(final AddToCartParams addToCartParams) throws CommerceCartModificationException;

	public CommerceCartModification updateQuantityForCartEntry(final CommerceCartParameter parameters)
			throws CommerceCartModificationException;

	public boolean isValidDeliveryAddress(OptimizedCartData cartData, AddressModel addressModel);

	public OptimizedCartData getCartForGuidAndSiteAndUser(String cartguid, BaseSiteModel baseSite, String currentUser);

	public CommerceCartModification doAddToCart(final CommerceCartParameter parameter) throws CommerceCartModificationException;

	public int getCurrentCurrencyDigit();

	public CommerceCartParameter validateCartParameter(final AddToCartParams parameters) throws CommerceCartModificationException;

	public List<OptimizedCartModel> getCartsForSiteAndUser(BaseSiteModel currentBaseSite, UserModel currentUser);

	public List<CommerceCartModification> addToCart(List<CommerceCartParameter> parameterList) throws CommerceCartMergingException;

}
