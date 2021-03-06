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
import de.hybris.platform.couponservices.model.RuleBasedAddCouponActionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedOrderAddProductActionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedOrderAdjustTotalActionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedOrderChangeDeliveryModeActionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedOrderEntryAdjustActionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPotentialPromotionMessageActionModel;
import de.hybris.platform.promotions.model.CachedPromotionOrderEntryConsumedModel;
import de.hybris.platform.promotions.model.PromotionOrderEntryConsumedModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.accenture.aitp.cart.model.AitpCachedPromotionResultModel;
import com.accenture.aitp.cart.model.CacheRuleBasedOrderAddProductActionModel;
import com.accenture.aitp.cart.model.CacheRuleBasedOrderAdjustTotalActionModel;
import com.accenture.aitp.cart.model.CacheRuleBasedOrderChangeDeliveryModeActionModel;
import com.accenture.aitp.cart.model.CacheRuleBasedOrderEntryAdjustActionModel;
import com.accenture.aitp.cart.model.CacheRuleBasedPotentialPromotionMessageActionModel;
import com.accenture.aitp.cart.model.CacheRuleRuleBasedAddCouponActionModel;


/**
 *
 */
@Aspect
public class ModelServiceAspect
{
	private static final Logger logger = Logger.getLogger(ModelServiceAspect.class);

	private static Map<Class, Class> replaceClassMap;
	private static Map<Class, Class> recoverClassMap;
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
	public Object create(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		if (joinPoint.getArgs().length == 1)
		{
			final Map<Class, Class> getReplaceClassMap = getReplaceClassMap();
			final Object obj = joinPoint.getArgs()[0];
			final Class replaceClass = getReplaceClassMap.get(obj);
			if (replaceClass != null)
			{
				logger.info("super cart replace classs from [" + obj + "] to [" + replaceClass + "]");
				return joinPoint.proceed(new Object[]
				{ replaceClass });
			}
		}
		return joinPoint.proceed();
	}

	@Around("execution(* de.hybris.platform.servicelayer.internal.model.impl.*.clone(..))")
	public Object clone(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		if (joinPoint.getArgs().length == 1)
		{
			final Map<Class, Class> getReplaceClassMap = getRecoverMap();
			final Object obj = joinPoint.getArgs()[0];
			final Class replaceClass = getReplaceClassMap.get(obj.getClass());
			final ModelService modelService = (ModelService) joinPoint.getTarget();
			if (replaceClass != null)
			{
				if (logger.isDebugEnabled())
				{
					logger.info("super clone replace classs from [" + obj + "] to [" + replaceClass + "]");
				}
				return modelService.clone(obj, replaceClass);
			}
		}
		return joinPoint.proceed();
	}



	public Map<Class, Class> getRecoverMap()
	{
		if (null == ModelServiceAspect.recoverClassMap)
		{
			final Map<Class, Class> recoverMap = new HashMap<>();
			for (final Map.Entry<Class, Class> entry : getReplaceClassMap().entrySet())
			{
				recoverMap.put(entry.getValue(), entry.getKey());
			}
			ModelServiceAspect.recoverClassMap = recoverMap;
		}
		return ModelServiceAspect.recoverClassMap;
	}


	public Map<Class, Class> getReplaceClassMap()
	{
		if (null == ModelServiceAspect.replaceClassMap)
		{
			ModelServiceAspect.replaceClassMap = initReplaceMap();
		}

		return ModelServiceAspect.replaceClassMap;
	}

	/**
	 *
	 * init the replace map class , if not bean config , create a new map
	 */
	public Map<Class, Class> initReplaceMap()
	{
		try
		{

			return (Map<Class, Class>) Registry.getApplicationContext().getBean("replaceClassMap");

		}
		catch (final Exception e)
		{
			final Map<Class, Class> replaceMap = new HashMap<>();
			replaceMap.put(PromotionResultModel.class, AitpCachedPromotionResultModel.class);
			replaceMap.put(RuleBasedOrderAdjustTotalActionModel.class, CacheRuleBasedOrderAdjustTotalActionModel.class);
			replaceMap.put(RuleBasedOrderEntryAdjustActionModel.class, CacheRuleBasedOrderEntryAdjustActionModel.class);
			replaceMap.put(RuleBasedAddCouponActionModel.class, CacheRuleRuleBasedAddCouponActionModel.class);
			replaceMap.put(RuleBasedOrderChangeDeliveryModeActionModel.class,
					CacheRuleBasedOrderChangeDeliveryModeActionModel.class);
			replaceMap.put(RuleBasedOrderAddProductActionModel.class, CacheRuleBasedOrderAddProductActionModel.class);
			replaceMap.put(RuleBasedPotentialPromotionMessageActionModel.class,
					CacheRuleBasedPotentialPromotionMessageActionModel.class);
			replaceMap.put(PromotionOrderEntryConsumedModel.class, CachedPromotionOrderEntryConsumedModel.class);
			return replaceMap;
		}
	}



}
