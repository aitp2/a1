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
package com.accenture.aitp.cart.strategy;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.order.Cart;


/**
 *
 */
public interface CartKeyGenerateStrategy
{
	String generateCartKey(Cart cart);

	String generateCartKey(CartModel cartModel);

	String generateCartKey(UserModel user);

	String generateCartKey(String guid);

}
