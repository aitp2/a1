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

import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;


/**
 *
 */
@Aspect
public class ExtendedOrderDaoAspect
{

	private static final Logger logger = Logger.getLogger(ExtendedOrderDaoAspect.class);

	/***
	 *
	 * 代理hybris ExtendedOrderDao findOrderByCode 方法
	 *
	 * @author mingming.wang
	 * @param joinPoint
	 */
	@Around("execution(* de.hybris.platform.promotionengineservices.order.dao.impl.DefaultExtendedOrderDao.findOrderByCode(..))")
	public Object findOrderByCode(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		if (joinPoint.getArgs().length == 1)
		{
			final CartService cartService = (CartService) Registry.getApplicationContext().getBean("cartService");
			final boolean hasSessionCart = cartService.hasSessionCart();
			if (hasSessionCart)
			{
				final CartModel cartModel = cartService.getSessionCart();
				final String queryCode = joinPoint.getArgs()[0].toString();
				logger.info("ExtendedOrderDaoAspect Session cart:" + cartModel.getCode() + "query code:" + queryCode);
				if (cartModel.getCode().equals(queryCode))
				{
					return cartModel;
				}
			}
		}
		return joinPoint.proceed();
	}
}
