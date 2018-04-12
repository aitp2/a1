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

import de.hybris.platform.promotionengineservices.model.RuleBasedOrderAdjustTotalActionModel;
import de.hybris.platform.promotions.jalo.CachedPromotionOrderAdjustTotalAction;
import de.hybris.platform.promotions.model.CachedPromotionResultModel;
import de.hybris.platform.promotions.model.PromotionResultModel;

import java.util.HashMap;
import java.util.Map;
import com.accenture.aitp.cart.model.CacheRuleBasedOrderAdjustTotalActionModel;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 *
 */
@Aspect
public class ModelServiceAspect
{
	private static final Logger logger = Logger.getLogger(ModelServiceAspect.class);


	@Around("execution(* de.hybris.platform.servicelayer.internal.model.impl.*.create(..))")
	public Object createAspect(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		if (joinPoint.getArgs().length == 1)
		{
			final Map<Object, Object> getReplaceClassMap = getReplaceClassMap();
			final Object obj = joinPoint.getArgs()[0];
			final Object replaceClass = getReplaceClassMap.get(obj);
			if (replaceClass != null)
			{
				logger.info("super cart replace classs from [" + obj + "] to [" + replaceClass + "]");
				return joinPoint.proceed(new Object[]
				{ replaceClass });
			}
		}
		return joinPoint.proceed();
	}


	private Map<Object, Object> getReplaceClassMap()
	{
		final Map<Object, Object> replaceMap = new HashMap<>();
		replaceMap.put(PromotionResultModel.class, CachedPromotionResultModel.class);
		replaceMap.put(PromotionResultModel.class.getSimpleName(), CachedPromotionResultModel.class.getSimpleName());
		replaceMap.put(RuleBasedOrderAdjustTotalActionModel.class, CacheRuleBasedOrderAdjustTotalActionModel.class);

		return replaceMap;
	}


}
