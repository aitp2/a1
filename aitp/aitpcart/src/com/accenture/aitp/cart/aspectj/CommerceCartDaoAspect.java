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

import de.hybris.platform.core.model.user.UserModel;

import java.util.Collections;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.accenture.aitp.cart.strategy.CartSerializerStrategy;


/**
 *
 */
@Aspect
public class CommerceCartDaoAspect
{
	private CartSerializerStrategy cartSerializerStrategy;

	private static final Logger logger = Logger.getLogger(CommerceCartDaoAspect.class);

	@Around("execution(* de.hybris.platform.commerceservices.order.dao.*.getCartsForSiteAndUser(..))")
	public Object getCartsForSiteAndUser(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		return Collections.singletonList(processHandler(joinPoint, 1));

	}

	@Around("execution(* de.hybris.platform.commerceservices.order.dao.*.getCartForGuidAndSiteAndUser(..))")
	public Object getCartForGuidAndSiteAndUser(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		final Object[] args = joinPoint.getArgs();
		if (args.length == 3)
		{
			final String guid = (String) args[0];
			if (null != guid)
			{
				return getCartSerializerStrategy().queryCartByGuidForAnonymousUser(guid);
			}
			final UserModel user = (UserModel) args[2];
			return getCartSerializerStrategy().queryCartByUser(user);
		}
		return joinPoint.proceed();
	}

	@Around("execution(* de.hybris.platform.commerceservices.order.dao.*.getCartForGuidAndSite(..))")
	public Object getCartForGuidAndSite(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		final Object[] args = joinPoint.getArgs();
		if (args.length == 2)
		{
			final String guid = (String) args[0];
			if (null != guid)
			{
				return getCartSerializerStrategy().queryCartByGuidForAnonymousUser(guid);
			}
		}
		return joinPoint.proceed();
	}

	@Around("execution(* de.hybris.platform.commerceservices.order.dao.*.getCartForCodeAndUser(..))")
	public Object getCartForCodeAndUser(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		return processHandler(joinPoint, 1);

	}

	@Around("execution(* de.hybris.platform.commerceservices.order.dao.*.getCartForSiteAndUser(..))")
	public Object getCartForSiteAndUser(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		return processHandler(joinPoint, 1);
	}


	protected Object processHandler(final ProceedingJoinPoint joinPoint, final int index) throws Throwable
	{
		final Object[] args = joinPoint.getArgs();
		if (args.length > index)
		{
			final UserModel user = (UserModel) args[1];

			return getCartSerializerStrategy().queryCartByUser(user);
		}
		return joinPoint.proceed();
	}


	/**
	 * @return the cartSerializerStrategy
	 */
	public CartSerializerStrategy getCartSerializerStrategy()
	{
		return cartSerializerStrategy;
	}

	/**
	 * @param cartSerializerStrategy
	 *           the cartSerializerStrategy to set
	 */
	public void setCartSerializerStrategy(final CartSerializerStrategy cartSerializerStrategy)
	{
		this.cartSerializerStrategy = cartSerializerStrategy;
	}


}
