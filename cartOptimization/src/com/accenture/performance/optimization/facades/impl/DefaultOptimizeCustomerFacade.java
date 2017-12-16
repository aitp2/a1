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

import de.hybris.platform.commercefacades.customer.impl.DefaultCustomerFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.order.exceptions.CalculationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.service.OptimizeCartService;
import com.accenture.performance.optimization.service.OptimizeCommerceCartService;


/**
 *
 */
public class DefaultOptimizeCustomerFacade extends DefaultCustomerFacade
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultOptimizeCartFacade.class);

	private OptimizeCartService optimizeCartService;

	private OptimizeCommerceCartService optimizeCommerceCartService;


	@Override
	public void loginSuccess()
	{

		if (getOptimizeCartService().hasSessionCart())
		{
			final OptimizedCartData optimizedCartData = getOptimizeCartService().getSessionOptimizedCart();

			try
			{
				getOptimizeCommerceCartService().calculateCart(optimizedCartData, true);
			}
			catch (final CalculationException ex)
			{
				LOG.error("Failed to recalculate order [" + optimizedCartData.getCode() + "]", ex);
			}
		}
		//login user not need to create empty cart
		//else
		//{
		//create new optimized cart
		//	getOptimizeCartService().getSessionOptimizedCart();

		//}

		final CustomerData userData = getCurrentCustomer();

		// First thing to do is to try to change the user on the session cart
		if (getCartService().hasSessionCart())
		{
			getCartService().changeCurrentCartUser(getCurrentUser());
		}

		// Update the session currency (which might change the cart currency)
		//		if (!updateSessionCurrency(userData.getCurrency(), getStoreSessionFacade().getDefaultCurrency()))
		//		{
		//			// Update the user
		//			getUserFacade().syncSessionCurrency();
		//		}

		// Update the user
		getUserFacade().syncSessionLanguage();

		//	super.loginSuccess();
	}

	/**
	 * @return the optimizeCartService
	 */
	public OptimizeCartService getOptimizeCartService()
	{
		return optimizeCartService;
	}

	/**
	 * @param optimizeCartService
	 *           the optimizeCartService to set
	 */
	public void setOptimizeCartService(final OptimizeCartService optimizeCartService)
	{
		this.optimizeCartService = optimizeCartService;
	}

	/**
	 * @return the optimizeCommerceCartService
	 */
	public OptimizeCommerceCartService getOptimizeCommerceCartService()
	{
		return optimizeCommerceCartService;
	}

	/**
	 * @param optimizeCommerceCartService
	 *           the optimizeCommerceCartService to set
	 */
	public void setOptimizeCommerceCartService(final OptimizeCommerceCartService optimizeCommerceCartService)
	{
		this.optimizeCommerceCartService = optimizeCommerceCartService;
	}

}
