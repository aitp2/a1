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
package com.accenture.performance.optimization.facades.impl;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commercefacades.order.data.AddToCartParams;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.CartRestorationData;
import de.hybris.platform.commercefacades.order.impl.DefaultCartFacade;
import de.hybris.platform.commerceservices.order.CommerceCartMergingException;
import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.CommerceCartRestoration;
import de.hybris.platform.commerceservices.order.CommerceCartRestorationException;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.accenture.performance.optimization.facades.OptimizedCartFacade;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.service.OptimizeCartService;


/**
 *
 */
public class DefaultOptimizeCartFacade extends DefaultCartFacade implements OptimizedCartFacade
{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultOptimizeCartFacade.class);

	@Autowired
	private OptimizeCartService optimizeCartService;

	@Override
	public OptimizedCartData getSessionCartData()
	{

		final OptimizedCartData cartData = optimizeCartService.getSessionOptimizedCart();

		return cartData;
	}

	@Override
	public String getSessionCartGuid()
	{
		String sessionCartGuid = null;
		if (hasSessionCart())
		{
			sessionCartGuid = optimizeCartService.getSessionOptimizedCart().getGuid();
		}
		return sessionCartGuid;
	}

	@Override
	public boolean hasSessionCart()
	{
		return optimizeCartService.hasSessionCart();
	}


	@Override
	public CartModificationData addToCart(final String code, final long quantity) throws CommerceCartModificationException
	{
		final AddToCartParams params = new AddToCartParams();
		params.setProductCode(code);
		params.setQuantity(quantity);
		return addToCart(params);
	}

	@Override
	public CartModificationData addToCart(final String code, final long quantity, final String storeId)
			throws CommerceCartModificationException
	{
		final AddToCartParams params = new AddToCartParams();
		params.setProductCode(code);
		params.setQuantity(quantity);
		params.setStoreId(storeId);

		return addToCart(params);
	}

	@Override
	public CartModificationData addToCart(final AddToCartParams addToCartParams) throws CommerceCartModificationException
	{
		//final CommerceCartParameter parameter = getCommerceCartParameterConverter().convert(addToCartParams);
		final CommerceCartModification modification = optimizeCartService.addToCart(addToCartParams);
		//super.addToCart(addToCartParams);
		return getCartModificationConverter().convert(modification);
	}

	@Override
	public CartModificationData updateCartEntry(final long entryNumber, final long quantity)
			throws CommerceCartModificationException
	{
		final AddToCartParams dto = new AddToCartParams();
		dto.setQuantity(quantity);
		final CommerceCartParameter parameter = getCommerceCartParameterConverter().convert(dto);
		parameter.setEnableHooks(true);
		parameter.setEntryNumber(entryNumber);
		final CommerceCartModification modification = optimizeCartService.updateQuantityForCartEntry(parameter);
		return getCartModificationConverter().convert(modification);
	}

	@Override
	public CartRestorationData restoreAnonymousCartAndMerge(final String fromAnonymousCartGuid, final String toUserCartGuid)
			throws CommerceCartRestorationException, CommerceCartMergingException
	{
		final BaseSiteModel currentBaseSite = getBaseSiteService().getCurrentBaseSite();
		final OptimizedCartData fromCart = optimizeCartService.getCartForGuidAndSiteAndUser(fromAnonymousCartGuid,
				currentBaseSite.getUid(), getUserService().getAnonymousUser().getUid());

		final OptimizedCartData toCart = optimizeCartService.getCartForGuidAndSiteAndUser(toUserCartGuid, currentBaseSite.getUid(),
				getUserService().getCurrentUser().getUid());

		if (toCart == null)
		{
			throw new CommerceCartRestorationException("Cart cannot be null");
		}

		if (fromCart == null)
		{
			return restoreSavedCart(toUserCartGuid);
		}

		final CommerceCartParameter parameter = new CommerceCartParameter();
		parameter.setEnableHooks(true);
		//parameter.setCart(toCart);
		parameter.setOptimizeCart(toCart);

		final CommerceCartRestoration restoration = getCommerceCartService().restoreCart(parameter);
		//parameter.setCart(getCartService().getSessionCart());
		parameter.setOptimizeCart(optimizeCartService.getSessionOptimizedCart());

		//commerceCartService.mergeCarts(fromCart, parameter.getCart(), restoration.getModifications());

		final CommerceCartRestoration commerceCartRestoration = getCommerceCartService().restoreCart(parameter);

		commerceCartRestoration.setModifications(restoration.getModifications());

		getCartService().changeCurrentCartUser(getUserService().getCurrentUser());
		return getCartRestorationConverter().convert(commerceCartRestoration);
	}

}
