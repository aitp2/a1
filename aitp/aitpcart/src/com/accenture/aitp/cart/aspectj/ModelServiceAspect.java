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
import de.hybris.platform.promotions.model.CachedPromotionResultModel;
import de.hybris.platform.promotions.model.PromotionResultModel;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.accenture.aitp.cart.model.CacheRuleBasedOrderAdjustTotalActionModel;


/**
 *
 */
@Aspect
public class ModelServiceAspect
{
	private static final Logger logger = Logger.getLogger(ModelServiceAspect.class);

	/*
	 * @Around("execution(* de.hybris.platform.servicelayer.internal.model.impl.*.save*(..))") public void
	 * saveAspect(final ProceedingJoinPoint joinPoint) throws Throwable { if (joinPoint.getArgs().length > 0) { final
	 * Map<Object, Object> getReplaceClassMap = getReplaceClassMap(); for (final Object obj : joinPoint.getArgs()) { if
	 * (obj instanceof ItemModel) { final ItemModel itemModel = (ItemModel) obj; if
	 * (getReplaceClassMap.get(itemModel.getClass()) != null) { logger.info("aspect the model [" + obj +
	 * "]  igore the save action"); return; // nothing need to do } }
	 *
	 *
	 * } } joinPoint.proceed(); }
	 */


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


	/**
	 * private Map<Object, Object> getReplaceClassMap() { final Map<Object, Object> replaceMap = new HashMap<>();
	 * replaceMap.put(CartModel.class, CartModel.class); replaceMap.put(CartEntryModel.class, CartEntryModel.class);
	 *
	 * return replaceMap; }
	 */
}
