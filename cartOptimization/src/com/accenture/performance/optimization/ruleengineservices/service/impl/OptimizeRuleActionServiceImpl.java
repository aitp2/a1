/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2017 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package com.accenture.performance.optimization.ruleengineservices.service.impl;

import de.hybris.platform.promotionengineservices.promotionengine.impl.DefaultPromotionRuleActionService;
import de.hybris.platform.ruleengineservices.action.RuleActionStrategy;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.data.OptimizedPromotionResultData;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.ruleengineservices.service.OptimizePromotionActionService;
import com.accenture.performance.optimization.ruleengineservices.service.OptimizeRuleActionService;


/**
 * action service <br/>
 * apply to a list of OptimizedPromotionResultData<br/>
 */
public class OptimizeRuleActionServiceImpl extends DefaultPromotionRuleActionService implements OptimizeRuleActionService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(OptimizeRuleActionServiceImpl.class);

	@Override
	public List<Object> applyAllActionsForData(final RuleEngineResultRAO ruleEngineResultRAO)
	{
		final LinkedList actionResults = new LinkedList();
		if (ruleEngineResultRAO != null && ruleEngineResultRAO.getActions() != null)
		{
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("applyAllActions triggered for actions: [{}]",
						ruleEngineResultRAO.getActions().stream().map((action) -> {
							return action.getFiredRuleCode();
						}).collect(Collectors.joining(", ")));
			}

			final Iterator actionIter = ruleEngineResultRAO.getActions().iterator();
			while (actionIter.hasNext())
			{
				final AbstractRuleActionRAO action = (AbstractRuleActionRAO) actionIter.next();
				action.setCart(ruleEngineResultRAO.getCart());
				final RuleActionStrategy strategy = this.getRuleActionStrategy(action.getActionStrategyKey());
				if (Objects.isNull(strategy))
				{
					LOGGER.error(String.format("Strategy bean for key \'%s\' not found!", new Object[]
					{ action.getActionStrategyKey() }));
				}
				else if (!(action instanceof DiscountRAO) || ((DiscountRAO) action).getValue() == null
						|| ((DiscountRAO) action).getValue().compareTo(BigDecimal.ZERO) > 0)
				{
					final Object result = strategy.apply(action).get(0);////////
					if(result instanceof OptimizedPromotionResultData)
					{
						List<String> promoPKList = (List<String>) actionResults.stream()
						.filter((item) -> {
							return item instanceof OptimizedPromotionResultData;
						})
						.map((item) -> {
							return ((OptimizedPromotionResultData) item).getPromotionPK();
						})
						.collect(Collectors.toList());
						
						if(!promoPKList.contains(((OptimizedPromotionResultData)result).getPromotionPK())) {
							actionResults.add(result);////////
						}
					}
					else 
					{
						actionResults.add(result);////////
					}
				}
			}
			// TODO: calculate cart again.
			this.recalculateCartDataTotals(actionResults);
			return actionResults;
		}
		else
		{
			LOGGER.info("applyAllActions called for undefined action set!");
			return actionResults;
		}
	}

	protected void recalculateCartDataTotals(final List actionResults)
	{
		if (CollectionUtils.isNotEmpty(actionResults))
		{
			final OptimizedPromotionResultData promotionResult = (OptimizedPromotionResultData) actionResults.get(0);
			final OptimizedCartData order = promotionResult.getCart();
			if (order == null)
			{

				LOGGER.error("Can not recalculate totals. No order found for PromotionResult: " + promotionResult.toString());
				return;
			}
			((OptimizePromotionActionService) this.getPromotionActionService()).recalculateTotals(order);
		}
	}
}
