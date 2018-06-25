/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2018 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package com.accenture.aitp.cart.strategy.impl;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.order.Cart;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

import org.apache.commons.lang3.StringUtils;

import com.accenture.aitp.cart.strategy.CartKeyGenerateStrategy;


/**
 *
 */
public class CartKeyGenerateStrategyImpl implements CartKeyGenerateStrategy
{

	private static final String PRE_SESSION_CART = "redis_cart_";

	private ModelService modelService;
	private UserService userService;

	@Override
	public String generateCartKey(final Cart cart)
	{
		if (null == cart)
		{
			return null;
		}
		final CartModel cartModel = modelService.get(cart);
		return generateCartKey(cartModel);
	}

	@Override
	public String generateCartKey(final CartModel cartModel)
	{
		return PRE_SESSION_CART
				+ (userService.isAnonymousUser(cartModel.getUser()) ? cartModel.getGuid() : cartModel.getUser().getPk());
	}

	@Override
	public String generateCartKey(final UserModel user)
	{
		if (null == user)
		{
			return null;
		}
		return PRE_SESSION_CART + (userService.isAnonymousUser(user) ? null : user.getPk());
	}

	@Override
	public String generateCartKey(final String guid)
	{
		if (StringUtils.isEmpty(guid))
		{
			return null;
		}
		return PRE_SESSION_CART + guid;
	}


	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @param userService
	 *           the userService to set
	 */
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}



}
