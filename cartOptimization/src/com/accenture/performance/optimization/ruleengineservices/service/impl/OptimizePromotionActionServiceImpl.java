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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.data.AbstractOptimizedRuleBasedPromotionActionData;
import com.accenture.performance.optimization.data.OptimizedPromotionOrderEntryConsumedData;
import com.accenture.performance.optimization.data.OptimizedPromotionResultData;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;
import com.accenture.performance.optimization.ruleengineservices.service.OptimizePromotionActionService;
import com.accenture.performance.optimization.ruleengineservices.service.OptimizePromotionResultService;
import com.accenture.performance.optimization.service.OptimizeCalculateService;
import com.accenture.performance.optimization.service.OptimizeCartService;

import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.promotionengineservices.promotionengine.impl.DefaultPromotionActionService;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengineservices.rao.AbstractOrderRAO;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.DisplayMessageRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryConsumedRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.util.DiscountValue;


/**
 *
 */
public class OptimizePromotionActionServiceImpl extends DefaultPromotionActionService implements OptimizePromotionActionService
{
	private static final Logger LOG = LoggerFactory.getLogger(OptimizePromotionActionServiceImpl.class);
	private OptimizeCartService optimizeCartService;

	@Override
	public OptimizedCartEntryData getOrderEntryData(final AbstractRuleActionRAO action)
	{
		ServicesUtil.validateParameterNotNull(action, "action must not be null");
		return !(action.getAppliedToObject() instanceof OrderEntryRAO) ? null
				: this.getOrderEntryData((OrderEntryRAO) action.getAppliedToObject(),action);
	}

	protected OptimizedCartEntryData getOrderEntryData(final OrderEntryRAO orderEntryRao,final AbstractRuleActionRAO action)
	{
		ServicesUtil.validateParameterNotNull(orderEntryRao, "orderEntryRao must not be null");
		ServicesUtil.validateParameterNotNull(orderEntryRao.getEntryNumber(), "orderEntryRao.entryNumber must not be null");
		ServicesUtil.validateParameterNotNull(orderEntryRao.getProduct(), "orderEntryRao.product must not be null");
		ServicesUtil.validateParameterNotNull(orderEntryRao.getProduct().getCode(), "orderEntryRao.product.code must not be null");
		final OptimizedCartData order = action.getCart();
		if (order == null)
		{
			return null;
		}
		else
		{
			final Iterator orderEntryIter = order.getEntries().iterator();

			OptimizedCartEntryData entry;
			do
			{
				if (!orderEntryIter.hasNext())
				{
					return null;
				}

				entry = (OptimizedCartEntryData) orderEntryIter.next();
			}
			while (!orderEntryRao.getEntryNumber().equals(entry.getEntryNumber())
					|| !orderEntryRao.getProduct().getCode().equals(entry.getProductCode()));

			return entry;
		}
	}

	@Override
	public void recalculateTotals(final OptimizedCartData cartData)
	{
		try
		{
			((OptimizeCalculateService) this.getCalculationService()).calculateTotals(cartData, true);
		}
		catch (final CalculationException arg2)
		{

			if (LOG.isDebugEnabled())
			{
				LOG.debug(arg2.getMessage(), arg2);
			}

			cartData.setCalculated(Boolean.FALSE);
			
			//this.getModelService().save(order);
		}
	}

	@Override
	public void createDiscountValue(final DiscountRAO discountRao, final String code, final OptimizedCartData cartData)
	{
		final boolean isAbsoluteDiscount = discountRao.getCurrencyIsoCode() != null;
		final DiscountValue discountValue = new DiscountValue(code, discountRao.getValue().doubleValue(), isAbsoluteDiscount,
				discountRao.getCurrencyIsoCode());
		final ArrayList globalDVs = new ArrayList();
		if (cartData.getGlobalDiscountValues() != null && !cartData.getGlobalDiscountValues().isEmpty())
		{
			globalDVs.addAll(cartData.getGlobalDiscountValues());
		}
		globalDVs.add(discountValue);
		cartData.setGlobalDiscountValues(globalDVs);
		cartData.setCalculated(Boolean.FALSE);
	}

	@Override
	public void createDiscountValue(final DiscountRAO discountRao, final String code, final OptimizedCartEntryData orderEntry)
	{
		final boolean isAbsoluteDiscount = discountRao.getCurrencyIsoCode() != null;
		final DiscountValue discountValue = new DiscountValue(code, discountRao.getValue().doubleValue(), isAbsoluteDiscount,
				discountRao.getCurrencyIsoCode());

		final List globalDVs = new ArrayList();
		if (orderEntry.getDiscountList() != null && !orderEntry.getDiscountList().isEmpty())
		{
			globalDVs.addAll(orderEntry.getDiscountList());
		}
		globalDVs.add(discountValue);
		orderEntry.setDiscountList(globalDVs);
		orderEntry.setCalculated(Boolean.FALSE);
	}

	@Override
	public OptimizedPromotionResultData createPromotionResultData(final AbstractRuleActionRAO actionRao)
	{
		OptimizedCartData order = this.getOrderDataInternal(actionRao);
		if (order == null)
		{
			final OptimizedCartEntryData entry = this.getOrderEntryData(actionRao);
			if (entry != null)
			{
				order = entry.getCartData();
			}
		}

		final AbstractRuleEngineRuleModel engineRule1 = this.getRule(actionRao);
		OptimizedPromotionResultData promoResult = this.findExistingPromotionResultModel(engineRule1, order);
		if (Objects.isNull(promoResult))
		{
			promoResult = new OptimizedPromotionResultData();
			if(null == order.getAllPromotionResults())
			{
				order.setAllPromotionResults(new LinkedList<OptimizedPromotionResultData>());
			}
			order.getAllPromotionResults().add(promoResult);
		}

		promoResult.setCart(order);
		final RuleBasedPromotionModel ruleBaseP = this.getPromotion(actionRao);
		promoResult.setPromotion(ruleBaseP.getCode());
		promoResult.setPromotionPK(ruleBaseP.getPk().toString());
		promoResult.setRulesModuleName(actionRao.getModuleName());
		if (StringUtils.isEmpty(promoResult.getMessageFired()))
		{
			promoResult
					.setMessageFired(((OptimizePromotionResultService) this.getPromotionResultService()).getDescription(promoResult));
		}

		if (Objects.nonNull(engineRule1))
		{
			promoResult.setRuleVersion(engineRule1.getVersion());
			// TODO: impotant: add the module version.
			//this.setRuleModuleVersionIfApplicable(promoResult, engineRule1);
		}

		final Set<OptimizedPromotionOrderEntryConsumedData> newConsumedEntries = this.createConsumedEntryData(actionRao);
		if (CollectionUtils.isEmpty(promoResult.getConsumedEntries()))
		{
			promoResult.setConsumedEntries(newConsumedEntries);
		}
		else if (CollectionUtils.isNotEmpty(newConsumedEntries))
		{
			promoResult.getConsumedEntries().addAll(newConsumedEntries);
		}

		if (actionRao instanceof DisplayMessageRAO)
		{
			promoResult.setCertainty(Float.valueOf(0.5F));
		}
		else
		{
			promoResult.setCertainty(Float.valueOf(1.0F));
		}

		return promoResult;
	}
	
	//TODO acn
	protected OptimizedPromotionResultData findExistingPromotionResultModel(AbstractRuleEngineRuleModel rule,
			OptimizedCartData order) {
		if (rule != null && order != null) {
			List<OptimizedPromotionResultData> results = order.getAllPromotionResults();
			if(CollectionUtils.isEmpty(results)) {
				return null;
			}
			
			for (OptimizedPromotionResultData result : results) 
			{
				Collection actions = result.getActions();
				if (!actions.stream()
						.filter(a -> a instanceof AbstractOptimizedRuleBasedPromotionActionData)
						.map(a -> (AbstractOptimizedRuleBasedPromotionActionData) a)
						.anyMatch(a -> {
							AbstractOptimizedRuleBasedPromotionActionData action = (AbstractOptimizedRuleBasedPromotionActionData)a;
							return action.getRulePK() != null && rule.getPk().toString().equals(action.getRulePK());
						})
					)
				{
					continue;
				}
				
				return result;
			}//end for
		}
		return null;
	}

	/**
	 *
	 */
	private OptimizedCartData getOrderDataInternal(final AbstractRuleActionRAO actionRao)
	{
		ServicesUtil.validateParameterNotNull(actionRao, "action rao must not be null");
		AbstractOrderRAO orderRao = null;
		if (actionRao.getAppliedToObject() instanceof OrderEntryRAO)
		{
			final OrderEntryRAO orderCode = (OrderEntryRAO) actionRao.getAppliedToObject();
			orderRao = orderCode.getOrder();
		}
		else if (actionRao.getAppliedToObject() instanceof AbstractOrderRAO)
		{
			orderRao = (AbstractOrderRAO) actionRao.getAppliedToObject();
		}

		if (orderRao != null)
		{
			return actionRao.getCart();
		}

		return null;
	}

	protected Set<OptimizedPromotionOrderEntryConsumedData> createConsumedEntryData(final AbstractRuleActionRAO action)
	{
		Set<OptimizedPromotionOrderEntryConsumedData> promotionOrderEntriesConsumed = null;
		if (Objects.nonNull(action) && Objects.nonNull(action.getConsumedEntries()))
		{
			final List orderEntryConsumedRAOsForRule = action.getConsumedEntries().stream().filter((oec) -> {
				return oec.getFiredRuleCode() != null && oec.getFiredRuleCode().equals(action.getFiredRuleCode());
			}).collect(Collectors.toList());
			promotionOrderEntriesConsumed = new HashSet<>();

			OptimizedPromotionOrderEntryConsumedData promotionOrderEntryConsumed;
			for (final Iterator arg4 = orderEntryConsumedRAOsForRule.iterator(); arg4.hasNext(); promotionOrderEntriesConsumed
					.add(promotionOrderEntryConsumed))
			{
				final OrderEntryConsumedRAO orderEntryConsumedRAO = (OrderEntryConsumedRAO) arg4.next();
				promotionOrderEntryConsumed = new OptimizedPromotionOrderEntryConsumedData();
				promotionOrderEntryConsumed.setOrderEntry(this.getOrderEntryData(orderEntryConsumedRAO.getOrderEntry(),action));
				promotionOrderEntryConsumed.setQuantity(Integer.valueOf(orderEntryConsumedRAO.getQuantity()));
				if (orderEntryConsumedRAO.getAdjustedUnitPrice() != null)
				{
					promotionOrderEntryConsumed
							.setAdjustedUnitPrice(Double.valueOf(orderEntryConsumedRAO.getAdjustedUnitPrice().doubleValue()));
				}
			}
		}

		return promotionOrderEntriesConsumed;
	}

//	protected OptimizedCartData getOrderData(final AbstractOrderRAO orderRao)
//	{
//		OptimizedCartData order = null;
//		final String orderCode = orderRao.getCode();
//
//		order = this.getOptimizeCartService().getSessionOptimizedCart();
//		if (!orderCode.equals(order.getCode()))
//		{
//			LOG.error("Can\'t apply promotion since failed to find order instance by it code {}.", orderCode);
//			throw new PromtionOrderNotFoundExcetpion("can't apply promotion to order, as no found", orderCode);
//		}
//		return order;
//	}

	@Override
	public OptimizedCartData getOrderData(final AbstractRuleActionRAO action)
	{
		// YTODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the optimizeCartService
	 */
	public OptimizeCartService getOptimizeCartService()
	{
		return optimizeCartService;
	}

	/**
	 * @param optimizeCartService
	 *           the optimizeCartService to set
	 */
	public void setOptimizeCartService(final OptimizeCartService optimizeCartService)
	{
		this.optimizeCartService = optimizeCartService;
	}

}
