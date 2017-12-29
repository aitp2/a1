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
package com.accenture.performance.optimization.ruleengineservices.strategy;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.ruleengineservices.action.RuleActionStrategy;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.data.AbstractOptimizedRuleBasedPromotionActionData;
import com.accenture.performance.optimization.data.OptimizedPromotionOrderAdjustTotalAction;
import com.accenture.performance.optimization.data.OptimizedPromotionResultData;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.ruleengineservices.service.OptimizePromotionActionService;
import com.accenture.performance.optimization.service.OptimizeCalculateService;
import com.accenture.performance.optimization.service.OptimizeModelDealService;


/**
 *
 */
public class OptimizeOrderAdjustTotalActionStrategy
		extends AbstractOptimizeRuleActionStrategy<AbstractOptimizedRuleBasedPromotionActionData>
{
	private static final Logger LOG = LoggerFactory.getLogger(OptimizeOrderEntryAdjustActionStrategy.class);
	private RuleActionStrategy oldStrategy;
	private OptimizeModelDealService optimizeModelDealService;

	@Override
	public List<OptimizedPromotionResultData> apply(final AbstractRuleActionRAO action)
	{
		if (!(action instanceof DiscountRAO))
		{
			LOG.error("cannot apply {}, action is not of type DiscountRAO", this.getClass().getSimpleName());
			return Collections.emptyList();
		}
		else
		{
			final OptimizedPromotionResultData promoResult = ((OptimizePromotionActionService) this.getPromotionActionService())
					.createPromotionResultData(action);
			if (promoResult == null)
			{
				LOG.error("cannot apply {}, promotionResult could not be created.", this.getClass().getSimpleName());
				return Collections.emptyList();
			}
			else
			{
				final OptimizedCartData order = promoResult.getCart();
				if (order == null)
				{
					LOG.error("cannot apply {}, order not found", this.getClass().getSimpleName());
					return Collections.emptyList();
				}
				else
				{
					final DiscountRAO discountRao = (DiscountRAO) action;
					final OptimizedPromotionOrderAdjustTotalAction actionData = this.createOrderAdjustTotalAction(promoResult,
							discountRao);
					this.handleActionMetadata(action, actionData);
					((OptimizePromotionActionService) this.getPromotionActionService()).createDiscountValue(discountRao,
							actionData.getGuid(), order);
					//					this.getModelService().saveAll(new Object[]
					//					{ promoResult, actionData, order });
					this.recalculateIfNeeded(order);
					optimizeModelDealService.persistCart(order);
					
					return Collections.singletonList(promoResult);
				}
			}
		}
	}

	/**
	 *
	 */
	private OptimizedPromotionOrderAdjustTotalAction createOrderAdjustTotalAction(final OptimizedPromotionResultData promoResult,
			final DiscountRAO discountRao)
	{
		final OptimizedPromotionOrderAdjustTotalAction actionModel = (OptimizedPromotionOrderAdjustTotalAction) this
				.createPromotionAction(promoResult, discountRao);
		actionModel.setAmount(Double.valueOf(discountRao.getValue().doubleValue()));
		return actionModel;
	}

	/**
	 *
	 */
	private boolean recalculateIfNeeded(final OptimizedCartData order)
	{
		if (BooleanUtils.isTrue(this.getForceImmediateRecalculation()))
		{
			try
			{
				((OptimizeCalculateService) this.getCalculationService()).calculateTotals(order, true);
			}
			catch (final CalculationException arg2)
			{
				LOG.error(String.format("Recalculation of order with code \'%s\' failed.", new Object[]
				{ order.getCode() }), arg2);
				order.setCalculated(Boolean.FALSE);
				//this.getModelService().save(order);
				return false;
			}
		}

		return true;
	}

	@Override
	public void undo(final ItemModel arg0)
	{
		// YTODO Auto-generated method stub
		LOG.error("TODO: no implement of undo!");
	}

	/**
	 * @return the oldStrategy
	 */
	public RuleActionStrategy getOldStrategy()
	{
		return oldStrategy;
	}

	/**
	 * @param oldStrategy
	 *           the oldStrategy to set
	 */
	public void setOldStrategy(final RuleActionStrategy oldStrategy)
	{
		this.oldStrategy = oldStrategy;
	}

	/**
	 * @return the optimizeModelDealService
	 */
	public OptimizeModelDealService getOptimizeModelDealService() {
		return optimizeModelDealService;
	}

	/**
	 * @param optimizeModelDealService the optimizeModelDealService to set
	 */
	public void setOptimizeModelDealService(OptimizeModelDealService optimizeModelDealService) {
		this.optimizeModelDealService = optimizeModelDealService;
	}

}
