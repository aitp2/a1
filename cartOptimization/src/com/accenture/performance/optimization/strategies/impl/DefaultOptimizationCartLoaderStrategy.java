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
package com.accenture.performance.optimization.strategies.impl;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.commerceservices.order.CommerceCartRestorationException;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.CartException;
import de.hybris.platform.commercewebservicescommons.strategies.impl.DefaultCartLoaderStrategy;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.servicelayer.user.UserService;

import org.springframework.security.access.AccessDeniedException;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.service.OptimizeCartService;
import com.accenture.performance.optimization.service.OptimizeModelDealService;


/**
 *
 */
public class DefaultOptimizationCartLoaderStrategy extends DefaultCartLoaderStrategy
{
	private static final String CURRENT_CART = "current";
	private static final String CART_NOT_FOUND_MESSAGE = "Cart not found.";

	private OptimizeModelDealService optimizeModelDealService;
	private UserService userService;

	/**
	 * Loads customer's cart
	 *
	 * @param cartID
	 */
	@Override
	protected void loadUserCart(final String cartID, final boolean refresh)
	{
		String requestedCartID = cartID;
		if (requestedCartID.equals(CURRENT_CART))
		{
			// current means last modified cart
			final OptimizedCartData cart = optimizeModelDealService.getCartDataForCodeAndSiteAndUser(null,
					getBaseSiteService().getCurrentBaseSite(), userService.getCurrentUser().getUid());
			if (cart == null)
			{
				throw new CartException("No cart created yet.", CartException.NOT_FOUND);
			}
			else if (!isBaseSiteValid(cart))
			{
				throw new CartException(CART_NOT_FOUND_MESSAGE, CartException.NOT_FOUND, requestedCartID);
			}
			requestedCartID = cart.getCode();
			restoreCart(cart, requestedCartID, refresh);
		}
		else
		{
			final OptimizedCartData cart = optimizeModelDealService.getCartDataForCodeAndSiteAndUser(requestedCartID,
					getBaseSiteService().getCurrentBaseSite(), userService.getCurrentUser().getUid());
			if (cart == null || !isBaseSiteValid(cart))
			{
				throw new CartException(CART_NOT_FOUND_MESSAGE, CartException.NOT_FOUND, requestedCartID);
			}
			restoreCart(cart, requestedCartID, refresh);
		}
		// code might be different because of cart expiration
		checkCartExpiration(requestedCartID, getOptimizeCartService().getSessionOptimizedCart().getCode());

	}

	/**
	 * Loads anonymous or guest cart
	 *
	 * @param cartID
	 */
	@Override
	protected void loadAnonymousCart(final String cartID, final boolean refresh)
	{
		if (cartID.equals(CURRENT_CART))
		{
			throw new AccessDeniedException("Access is denied");
		}

		final OptimizedCartData cart = optimizeModelDealService.getCartDataForGuidAndSiteAndUser(cartID,
				getBaseSiteService().getCurrentBaseSite(), userService.getCurrentUser().getUid());
		if (cart != null)
		{
			final CustomerModel cartOwner = (CustomerModel) userService.getUserForUID(cart.getUserId());
			if (userService.isAnonymousUser(cartOwner) || CustomerType.GUEST.equals(cartOwner.getType()))
			{
				if (!isBaseSiteValid(cart))
				{
					throw new CartException(CART_NOT_FOUND_MESSAGE, CartException.NOT_FOUND, cartID);
				}
				restoreCart(cart, cartID, refresh);
			}
			else
			{
				// 'access denied' presented as 'not found' for security reasons
				throw new CartException(CART_NOT_FOUND_MESSAGE, CartException.NOT_FOUND, cartID);
			}
		}
		else
		{
			throw new CartException(CART_NOT_FOUND_MESSAGE, CartException.NOT_FOUND, cartID);
		}

		// guid might be different because of cart expiration
		checkCartExpiration(cartID, getOptimizeCartService().getSessionOptimizedCart().getGuid());
	}

	protected void restoreCart(final OptimizedCartData cart, final String requestedCartId, final boolean refresh)
	{
		try
		{
			if (refresh)
			{
				final CommerceCartParameter parameter = new CommerceCartParameter();
				parameter.setEnableHooks(true);
				parameter.setOptimizeCart(cart);
				getCommerceCartService().restoreCart(parameter);
			}
			else
			{
				getOptimizeCartService().setSessionOptimizedCart(cart);
			}

			//applyCurrencyToCartAndRecalculateIfNeeded();
		}
		catch (final CommerceCartRestorationException e)
		{
			throw new CartException("Couldn't restore cart: " + e.getMessage(), CartException.INVALID, requestedCartId, e);
		}
	}

	/**
	 * Checks currently set currency and compares it with one set in cart. If not equal, sets new currency in cart and
	 * recalculates. This is similar logic to SessionContext.checkSpecialAttributes. Calling this is needed if
	 * checkSpecialAttributes was called before (when there was no cart in session)
	 */
	@Override
	protected void applyCurrencyToCartAndRecalculateIfNeeded()
	{
		final OptimizedCartData cart = getOptimizeCartService().getSessionOptimizedCart();
		final CurrencyModel currentCurrency = getCommerceCommonI18NService().getCurrentCurrency();
		if (!cart.getCurrencyCode().equals(currentCurrency.getIsocode()))
		{
			cart.setCurrencyCode(currentCurrency.getIsocode());
			getModelService().save(cart);
			try
			{
				final CommerceCartParameter parameter = new CommerceCartParameter();
				parameter.setEnableHooks(true);
				parameter.setOptimizeCart(cart);
				getCommerceCartService().recalculateCart(parameter);
			}
			catch (final CalculationException e)
			{
				throw new CartException("Couldn't recalculate cart" + e.getMessage(), CartException.CANNOT_RECALCULATE, e);
			}
		}
	}


	/**
	 * Checks if base site set in the cart is the same as one set in baseSiteService. It prevents mixing requests for
	 * multiple sites in one session
	 */
	protected boolean isBaseSiteValid(final OptimizedCartData cart)
	{
		if (cart != null)
		{
			final BaseSiteModel baseSiteFromCart = getBaseSiteService().getBaseSiteForUID(cart.getBaseSite());
			final BaseSiteModel baseSiteFromService = getBaseSiteService().getCurrentBaseSite();

			if (baseSiteFromCart != null && baseSiteFromService != null && baseSiteFromCart.equals(baseSiteFromService))
			{
				return true;
			}
		}
		return false;
	}

	private OptimizeCartService getOptimizeCartService()
	{
		return (OptimizeCartService) super.getCartService();
	}

	/**
	 * @return the optimizeModelDealService
	 */
	public OptimizeModelDealService getOptimizeModelDealService()
	{
		return optimizeModelDealService;
	}

	/**
	 * @param optimizeModelDealService
	 *           the optimizeModelDealService to set
	 */
	public void setOptimizeModelDealService(final OptimizeModelDealService optimizeModelDealService)
	{
		this.optimizeModelDealService = optimizeModelDealService;
	}

	/**
	 * @return the userService
	 */
	public UserService getUserService()
	{
		return userService;
	}

	/**
	 * @param userService
	 *           the userService to set
	 */
	@Override
	public void setUserService(final UserService userService)
	{
		super.setUserService(userService);
		this.userService = userService;
	}

}
