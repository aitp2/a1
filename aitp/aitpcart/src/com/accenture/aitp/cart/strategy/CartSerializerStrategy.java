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
import de.hybris.platform.jalo.JaloSession;

import javax.servlet.http.HttpServletRequest;


/**
 * @desc cart serializer and derializer stratrgy
 * @author mingming.wang
 */
public interface CartSerializerStrategy
{

	void serializerCart(JaloSession jaloSession);

	void serializerCart(HttpServletRequest httpRequest, CartModel cart);

	void removeSerializerCart(CartModel cart);

	void initSessionCart(HttpServletRequest httpRequest);

	CartModel queryCartByUser(UserModel user);

	CartModel queryCartByGuidForAnonymousUser(String guid);
}
