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
package com.accenture.performance.optimization.service.impl;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.promotionengineservices.model.RuleBasedOrderAdjustTotalActionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedOrderEntryAdjustActionModel;
import de.hybris.platform.promotionengineservices.promotionengine.impl.DefaultPromotionEngineService;
import de.hybris.platform.promotions.model.AbstractPromotionActionModel;
import de.hybris.platform.promotions.model.PromotionGroupModel;
import de.hybris.platform.promotions.model.PromotionOrderEntryConsumedModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.promotions.result.PromotionOrderResults;
import de.hybris.platform.ruleengine.RuleEvaluationContext;
import de.hybris.platform.ruleengine.RuleEvaluationResult;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.exception.RuleEngineRuntimeException;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;
import de.hybris.platform.ruleengineservices.enums.FactContextType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.data.AbstractOptimizedPromotionActionData;
import com.accenture.performance.optimization.data.OptimizedPromotionOrderAdjustTotalAction;
import com.accenture.performance.optimization.data.OptimizedPromotionOrderEntryAdjustAction;
import com.accenture.performance.optimization.data.OptimizedPromotionOrderEntryConsumedData;
import com.accenture.performance.optimization.data.OptimizedPromotionResultData;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;
import com.accenture.performance.optimization.ruleengineservices.result.OptimizedPromotionOrderResults;
import com.accenture.performance.optimization.ruleengineservices.service.OptimizeRuleActionService;
import com.accenture.performance.optimization.ruleengineservices.strategy.OptimizeRuleEngineContextFinderStrategy;
import com.accenture.performance.optimization.service.OptimizeCalculateService;
import com.accenture.performance.optimization.service.OptimizePromotionService;
import com.google.common.collect.Lists;


/**
 *
 */
public class DefaultOptimizationPromotionService extends DefaultPromotionEngineService implements OptimizePromotionService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultOptimizationPromotionService.class);


	@Override
	public PromotionOrderResults updatePromotions(final Collection<PromotionGroupModel> promotionGroups,
			final OptimizedCartData cartData, final Date date)
	{
		final Object perSessionLock = this.getSessionService().getOrLoadAttribute("promotionsUpdateLock", () -> {
			return new SerializableObject();
		});
		synchronized (perSessionLock)
		{
			return this.updatePromotionsNotThreadSafe(promotionGroups, cartData, date);
		}
	}

	/**
	 * @param date
	 *
	 */
	private PromotionOrderResults updatePromotionsNotThreadSafe(final Collection<PromotionGroupModel> promotionGroups,
			final OptimizedCartData order, final Date date)
	{
		this.cleanupAbstractOrder(order);

		Object actionApplicationResults;
		try
		{
			final RuleEvaluationResult rere = this.evaluate(order, promotionGroups, date);
			if (!rere.isEvaluationFailed())
			{
				if (this.getRuleActionService() instanceof OptimizeRuleActionService)
				{
					// optimized action process: OptimizedPromotionResultData
					final List applyAllActionList = ((OptimizeRuleActionService) this.getRuleActionService())
							.applyAllActionsForData(rere.getResult());
					
					actionApplicationResults = applyAllActionList
							.stream()
							.filter((item) -> {
								return item instanceof OptimizedPromotionResultData;
							})
							.map((item) -> {
								return (OptimizedPromotionResultData) item;
							})
							.collect(Collectors.toList());
					
					//TODO acn
					order.setAllPromotionResults((List<OptimizedPromotionResultData>) actionApplicationResults);
				}
				else
				{
					throw new RuntimeException("expect OptimizeRuleActionService, but now is : " + this.getRuleActionService());
				}
			}
			else
			{
				actionApplicationResults = Lists.newArrayList();
			}
		}
		catch (final RuleEngineRuntimeException ex)
		{
			LOG.error(ex.getMessage(), ex);
			actionApplicationResults = new ArrayList();
		}

		return new OptimizedPromotionOrderResults(JaloSession.getCurrentSession().getSessionContext(), null, null, order,
				(List<OptimizedPromotionResultData>) actionApplicationResults, 0.0D);
	}

	@Override
	public RuleEvaluationResult evaluate(final OptimizedCartData order, final Collection<PromotionGroupModel> promotionGroups,
			final Date date)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Promotion cart evaluation triggered for cart with code \'{}\'", order.getCode());
		}

		final ArrayList facts = new ArrayList();
		facts.add(order);
		facts.addAll(promotionGroups);
		if (Objects.nonNull(date))
		{
			facts.add(date);
		}

		try
		{
			final RuleEvaluationContext e = this.prepareContext(
					this.getFactContextFactory().createFactContext(FactContextType.PROMOTION_ORDER, facts),
					this.determineRuleEngineContext(order));
			return this.getCommerceRuleEngineService().evaluate(e);
		}
		catch (final IllegalStateException arg6)
		{
			LOG.error("Promotion rule evaluation failed", arg6);
			final RuleEvaluationResult result = new RuleEvaluationResult();
			result.setErrorMessage(arg6.getMessage());
			result.setEvaluationFailed(true);
			return result;
		}
	}

	protected AbstractRuleEngineContextModel determineRuleEngineContext(final OptimizedCartData order)
	{
		return StringUtils
				.isNotEmpty(this.getDefaultRuleEngineContextName())
						? this.getRuleEngineContextDao().findRuleEngineContextByName(this.getDefaultRuleEngineContextName())
						: (AbstractRuleEngineContextModel) ((OptimizeRuleEngineContextFinderStrategy) this
								.getRuleEngineContextFinderStrategy()).findRuleEngineContext(order, RuleType.PROMOTION)
										.orElseThrow(() -> {
											return new IllegalStateException(
													String.format("No rule engine context could be derived for order [%s]", new Object[]
											{ order }));
										});
	}

	/**
	 * confirm if need to clean the datamodel when cart bean convert to cart model and save. <br/>
	 * change invoking undo method before calculate
	 */
	protected void cleanupAbstractOrder(final OptimizedCartData cart)
	{
		cart.setGlobalDiscountValues(null);
		cart.setAllPromotionResults(null);
		final List<OptimizedCartEntryData> removeList = new ArrayList<>();
		for (final OptimizedCartEntryData entry : CollectionUtils.emptyIfNull(cart.getEntries()))
		{
			entry.setDiscountList(null);
			if (Boolean.TRUE.equals(entry.getPromomtionGiftEntry()))
			{
				removeList.add(entry);
			}
		}
		if (!removeList.isEmpty())
		{
			cart.getEntries().removeAll(removeList);
		}

		try
		{
			((OptimizeCalculateService) this.getCalculationService()).calculateTotals(cart, true);
		}
		catch (final CalculationException e)
		{
			LOG.error("before promotion calculate error", e);
		}

	}

	private static class SerializableObject implements Serializable
	{
		private SerializableObject()
		{
		}

	}

	@Override
	public void transferPromotionsToOrder(final OptimizedCartData cartModel, final OrderModel target,
			final boolean onlyTransferAppliedPromotions)
	{
		if (target.getAllPromotionResults() == null)
		{
			target.setAllPromotionResults(new HashSet<PromotionResultModel>());
		}

		final Map<Integer, AbstractOrderEntryModel> allEntries = new HashMap<>();
		for (final AbstractOrderEntryModel entry : target.getEntries())
		{
			allEntries.put(entry.getEntryNumber(), entry);
		}

		final List<OptimizedPromotionResultData> sourcePRList = cartModel.getAllPromotionResults();
		if (CollectionUtils.isNotEmpty(sourcePRList))
		{
			for (final OptimizedPromotionResultData sourcePR : sourcePRList)
			{
				final PromotionResultModel targetPromoResult = this.getModelService().create(PromotionResultModel.class);
				targetPromoResult.setOrder(target);
				target.getAllPromotionResults().add(targetPromoResult);

				targetPromoResult.setOrder(target);
				targetPromoResult.setPromotion(this.getModelService().get(PK.parse(sourcePR.getPromotionPK())));
				targetPromoResult.setRulesModuleName(sourcePR.getRulesModuleName());
				targetPromoResult.setMessageFired(sourcePR.getMessageFired());
				targetPromoResult.setRuleVersion(sourcePR.getRuleVersion());
				targetPromoResult.setModuleVersion(sourcePR.getModuleVersion());
				targetPromoResult.setCertainty(sourcePR.getCertainty());

				// action
				targetPromoResult.setActions(new HashSet<AbstractPromotionActionModel>());
				if (CollectionUtils.isNotEmpty(sourcePR.getActions()))
				{
					createActionForPromotionResult(allEntries, sourcePR, targetPromoResult);
				}

				// consumedentryModel
				targetPromoResult.setConsumedEntries(new HashSet<PromotionOrderEntryConsumedModel>());
				if (CollectionUtils.isNotEmpty(sourcePR.getConsumedEntries()))
				{
					createConsumedEntryForPromotionResult(allEntries, sourcePR, targetPromoResult);
				}
			}
		}
	}

	/**
	 *
	 */
	private void createConsumedEntryForPromotionResult(final Map<Integer, AbstractOrderEntryModel> allEntries,
			final OptimizedPromotionResultData sourcePR, final PromotionResultModel targetPromoResult)
	{
		for (final OptimizedPromotionOrderEntryConsumedData sourceConsumed : sourcePR.getConsumedEntries())
		{
			final PromotionOrderEntryConsumedModel targetConsumed = this.getModelService()
					.create(PromotionOrderEntryConsumedModel.class);

			targetConsumed.setAdjustedUnitPrice(sourceConsumed.getAdjustedUnitPrice());
			targetConsumed.setQuantity(Long.valueOf(sourceConsumed.getQuantity().intValue()));
			targetConsumed.setCode(sourceConsumed.getCode());
			targetConsumed.setOrderEntry(allEntries.get(sourceConsumed.getOrderEntry().getEntryNumber()));
			targetPromoResult.getConsumedEntries().add(targetConsumed);
		}
	}

	/**
	 *
	 */
	private void createActionForPromotionResult(final Map<Integer, AbstractOrderEntryModel> allEntries,
			final OptimizedPromotionResultData sourcePR, final PromotionResultModel targetPromoResult)
	{
		targetPromoResult.setAllPromotionActions(new HashSet<AbstractPromotionActionModel>());
		for (final AbstractOptimizedPromotionActionData sourceAction : sourcePR.getActions())
		{
			if (sourceAction instanceof OptimizedPromotionOrderAdjustTotalAction)
			{
				final OptimizedPromotionOrderAdjustTotalAction orderAdjust = (OptimizedPromotionOrderAdjustTotalAction) sourceAction;
				final RuleBasedOrderAdjustTotalActionModel targetAction = this.getModelService()
						.create(RuleBasedOrderAdjustTotalActionModel.class);
				if (orderAdjust.getAmount() != null)
				{
					targetAction.setAmount(BigDecimal.valueOf(orderAdjust.getAmount().doubleValue()));
				}
				targetAction.setGuid(orderAdjust.getGuid());
				targetAction.setRule(this.getModelService().get(PK.parse(orderAdjust.getRulePK())));
				targetAction.setMarkedApplied(orderAdjust.getMarkedApplied());
				targetAction.setStrategyId(orderAdjust.getStrategyId());

				targetAction.setPromotionResult(targetPromoResult);
				targetPromoResult.getAllPromotionActions().add(targetAction);

			}
			else if (sourceAction instanceof OptimizedPromotionOrderEntryAdjustAction)
			{
				final OptimizedPromotionOrderEntryAdjustAction entryAdjust = (OptimizedPromotionOrderEntryAdjustAction) sourceAction;
				final RuleBasedOrderEntryAdjustActionModel targetAction = this.getModelService()
						.create(RuleBasedOrderEntryAdjustActionModel.class);
				if (entryAdjust.getAmount() != null)
				{
					targetAction.setAmount(BigDecimal.valueOf(entryAdjust.getAmount().doubleValue()));
				}

				targetAction.setGuid(entryAdjust.getGuid());
				targetAction.setRule(this.getModelService().get(PK.parse(entryAdjust.getRulePK())));
				targetAction.setMarkedApplied(entryAdjust.getMarkedApplied());
				targetAction.setStrategyId(entryAdjust.getStrategyId());

				targetAction.setOrderEntryNumber(entryAdjust.getOrderEntryNumber());
				targetAction.setOrderEntryProduct(allEntries.get(entryAdjust.getOrderEntryNumber()).getProduct());
				targetAction.setOrderEntryQuantity(entryAdjust.getOrderEntryQuantity());

				targetAction.setPromotionResult(targetPromoResult);
				targetPromoResult.getAllPromotionActions().add(targetAction);
			}

		}
	}

}



