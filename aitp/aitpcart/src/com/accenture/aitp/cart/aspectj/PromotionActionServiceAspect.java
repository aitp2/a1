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

import java.util.Collections;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;


/**
 *
 */
@Aspect
public class PromotionActionServiceAspect
{

	@Pointcut("execution(protected * *.createConsumedEntries(..))")
	public void createConsumedEntries()
	{
		//
	}

	@Around("createConsumedEntries()")
	public Object createConsumedEntries(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		if (joinPoint.getArgs().length == 1)
		{
			final Object consumedEntries = joinPoint.proceed();
			if (null == consumedEntries)
			{
				return Collections.EMPTY_LIST;
			}
			return consumedEntries;

		}
		return joinPoint.proceed();
	}
}
