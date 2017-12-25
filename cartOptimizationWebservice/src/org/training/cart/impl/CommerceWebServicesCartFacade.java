/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package org.training.cart.impl;

import com.accenture.performance.optimization.facades.impl.DefaultOptimizeCartFacade;

import de.hybris.platform.commercefacades.order.impl.DefaultCartFacade;
import de.hybris.platform.core.model.order.CartModel;


/**
 * Extension of {@link DefaultCartFacade} for commercewebservices.
 */
public class CommerceWebServicesCartFacade extends DefaultOptimizeCartFacade
{

	/**
	 * Checks if given card belongs to anonymous user.
	 *
	 * @param cartGuid
	 *           GUID of the cart.
	 * @return <tt>true</tt> if the cart belongs to anonymous user.
	 */
	public boolean isAnonymousUserCart(final String cartGuid)
	{
		final CartModel cart = getCommerceCartService().getCartForGuidAndSiteAndUser(cartGuid,
				getBaseSiteService().getCurrentBaseSite(), getUserService().getAnonymousUser());
		return cart != null;
	}

	/**
	 * Checks if given card belongs to current user.
	 *
	 * @param cartGuid
	 *           GUID of the cart.
	 * @return <tt>true</tt> if the cart belongs to current user.
	 */
	public boolean isCurrentUserCart(final String cartGuid)
	{
		final CartModel cart = getCommerceCartService().getCartForGuidAndSiteAndUser(cartGuid,
				getBaseSiteService().getCurrentBaseSite(), getUserService().getCurrentUser());
		return cart != null;
	}
}
