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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.data.AbstractOptimizedRuleBasedPromotionActionData;
import com.accenture.performance.optimization.data.OptimizedPromotionOrderEntryAdjustAction;
import com.accenture.performance.optimization.data.OptimizedPromotionResultData;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;
import com.accenture.performance.optimization.ruleengineservices.service.OptimizePromotionActionService;
import com.accenture.performance.optimization.service.OptimizeCalculateService;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.ruleengineservices.action.RuleActionStrategy;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;


/**
 *
 */
public class OptimizeOrderEntryAdjustActionStrategy
		extends AbstractOptimizeRuleActionStrategy<AbstractOptimizedRuleBasedPromotionActionData>
{
	private static final Logger LOG = LoggerFactory.getLogger(OptimizeOrderEntryAdjustActionStrategy.class);
	private RuleActionStrategy oldStrategy;

	@Override
	public List<OptimizedPromotionResultData> apply(final AbstractRuleActionRAO action)
	{

		//TODO: check if go to old strategy invoking
		if (!(action instanceof DiscountRAO))
		{
			LOG.error("cannot apply {}, action is not of type DiscountRAO", this.getClass().getSimpleName());
			return Collections.emptyList();
		}
		else
		{
			final OptimizedCartEntryData entry = ((OptimizePromotionActionService) this.getPromotionActionService())
					.getOrderEntryData(action);
			if (entry == null)
			{
				LOG.error("cannot apply {}, orderEntry could not be found.", this.getClass().getSimpleName());
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
					final OptimizedCartData order = entry.getCartData();
					if (order == null)
					{
						LOG.error("cannot apply {}, order does not exist for order entry", this.getClass().getSimpleName());
						return Collections.emptyList();
					}
					else
					{
						final DiscountRAO discountRao = (DiscountRAO) action;
						final BigDecimal discountAmount = discountRao.getValue();

						this.adjustDiscountRaoValue(entry, discountRao, discountAmount);
						final OptimizedPromotionOrderEntryAdjustAction actionData = this.createOrderEntryAdjustAction(promoResult,
								action, entry, discountAmount);
						this.handleActionMetadata(action, actionData);
						((OptimizePromotionActionService) this.getPromotionActionService()).createDiscountValue(discountRao,
								actionData.getGuid(), entry);

						this.recalculateIfNeeded(order);
						return Collections.singletonList(promoResult);
					}
				}
			}
		}
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

	/**
	 *
	 */
	private OptimizedPromotionOrderEntryAdjustAction createOrderEntryAdjustAction(final OptimizedPromotionResultData promoResult,
			final AbstractRuleActionRAO action, final OptimizedCartEntryData entry, final BigDecimal discountAmount)
	{
		// new instance of OptimizedPromotionOrderEntryAdjustAction
		final OptimizedPromotionOrderEntryAdjustAction actionData = (OptimizedPromotionOrderEntryAdjustAction) this
				.createPromotionAction(promoResult, action);
		actionData.setAmount(Double.valueOf(discountAmount.doubleValue()));
		actionData.setOrderEntryNumber(entry.getEntryNumber());
		actionData.setOrderEntryProduct(entry.getProductCode());
		actionData.setOrderEntryQuantity(Long.valueOf(this.getConsumedQuantity(promoResult)));
		return actionData;
	}

	/**
	 *
	 */
	private long getConsumedQuantity(final OptimizedPromotionResultData promoResult)
	{
		long consumedQuantity = 0L;
		if (CollectionUtils.isNotEmpty(promoResult.getConsumedEntries()))
		{
			consumedQuantity = promoResult.getConsumedEntries().stream().mapToLong((consumedEntry) -> {
				return consumedEntry.getQuantity().longValue();
			}).sum();
		}
		return consumedQuantity;
	}

	/**
	 *
	 */
	private void adjustDiscountRaoValue(final OptimizedCartEntryData entry, final DiscountRAO discountRao,
			final BigDecimal discountAmount)
	{
		if (!StringUtils.isEmpty(discountRao.getCurrencyIsoCode()) && discountRao.getAppliedToQuantity() > 0L
				|| discountRao.isPerUnit())
		{
			final BigDecimal amount = discountAmount.multiply(BigDecimal.valueOf(discountRao.getAppliedToQuantity()))
					.divide(BigDecimal.valueOf(entry.getQuantity().longValue()), 5, 4);
			discountRao.setValue(amount);
		}
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

}
