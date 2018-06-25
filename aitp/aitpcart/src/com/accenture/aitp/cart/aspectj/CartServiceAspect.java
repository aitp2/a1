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
package com.accenture.aitp.cart.aspectj;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;


/**
 *
 */
@Aspect
public class CartServiceAspect
{

	@Around("execution(* de.hybris.platform.order.impl.*.changeCurrentCartUser(..))")
	public void changeCurrentCartUser(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		joinPoint.proceed();

		final CartService cartService = (CartService) joinPoint.getTarget();
		if (cartService.hasSessionCart())
		{
			final CartModel sessionCart = cartService.getSessionCart();
			cartService.removeSessionCart();
			cartService.setSessionCart(sessionCart);
		}

	}

}
