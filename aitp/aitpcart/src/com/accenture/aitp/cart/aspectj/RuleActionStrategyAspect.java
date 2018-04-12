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

import de.hybris.platform.promotionengineservices.action.impl.AbstractRuleActionStrategy;
import de.hybris.platform.promotionengineservices.model.AbstractRuleBasedPromotionActionModel;
import de.hybris.platform.promotions.model.AbstractPromotionActionModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;


/**
 *
 */
@Aspect
public class RuleActionStrategyAspect
{
	@Around("execution(* de.hybris.platform.promotionengineservices.action.impl.*.createPromotionAction(..))")
	public Object createAspect(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		if (joinPoint.getArgs().length == 2 && joinPoint.getTarget() instanceof AbstractRuleActionStrategy)
		{
			final AbstractRuleBasedPromotionActionModel result = (AbstractRuleBasedPromotionActionModel) joinPoint.proceed();

			final Collection<AbstractPromotionActionModel> actionList = new ArrayList(result.getPromotionResult().getActions());
			actionList.add(result);
			result.getPromotionResult().setActions(actionList);
			return result;
		}
		return joinPoint.proceed();
	}

	protected static String createActionUUID()
	{
		return "Action[" + UUID.randomUUID().toString() + "]";
	}
}
