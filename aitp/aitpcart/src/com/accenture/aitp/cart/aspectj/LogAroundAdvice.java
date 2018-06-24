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
import de.hybris.platform.servicelayer.user.UserService;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;


/**
 *
 */
public class LogAroundAdvice
{


	private static Logger LOGGER = Logger.getLogger(LogAroundAdvice.class);

	// 日志 方法 计时  方法执行时间
	public Object doAround(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		final long beforeInvokeTime = System.currentTimeMillis();
		final UserService userService = (UserService) Registry.getApplicationContext().getBean("userService");
		final String uid = userService.getCurrentUser().getUid();
		final String threadName = Thread.currentThread().getName();
		final String className = joinPoint.getTarget().getClass().getName();
		final String methodName = joinPoint.getSignature().getName();
		//LOGGER.info(uid+"--"+threadName+"--"+className+"--"+methodName+" start process");
		try
		{
			return joinPoint.proceed();
		}
		finally
		{
			final long usedTime = System.currentTimeMillis() - beforeInvokeTime;
			LOGGER.info(
					uid + "--" + threadName + "--" + className + "--" + methodName + " end process use time " + usedTime + " ms");
		}
	}

	public void doThrowing(final JoinPoint jp, final Throwable ex) throws Throwable
	{
		LOGGER.error("method " + jp.getTarget().getClass().getName() + "." + jp.getSignature().getName() + " throw exception");
		LOGGER.error(ex.getMessage());
		throw ex;
	}

}
