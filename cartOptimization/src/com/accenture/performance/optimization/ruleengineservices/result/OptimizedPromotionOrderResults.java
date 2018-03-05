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
package com.accenture.performance.optimization.ruleengineservices.result;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.accenture.performance.optimization.data.AbstractOptimizedPromotionActionData;
import com.accenture.performance.optimization.data.OptimizedPromotionResultData;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;

import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.promotions.jalo.PromotionResult;
import de.hybris.platform.promotions.result.PromotionOrderResults;


/**
 * append two attribute to save optimized cart data
 */
public class OptimizedPromotionOrderResults extends PromotionOrderResults
{
	private final List<OptimizedPromotionResultData> optimizedPromotionResults;
	private final OptimizedCartData optimizedCart;
	private volatile List<OptimizedPromotionResultData> appliedProductPromotions;
	private volatile List<OptimizedPromotionResultData> appliedOrderPromotions;
	private volatile List<OptimizedPromotionResultData> potentialProductPromotions;
	private volatile List<OptimizedPromotionResultData> potentialOrderPromotions;

	public OptimizedPromotionOrderResults(final SessionContext ctx, final AbstractOrder order,
			final List<PromotionResult> promotionResults, final double changeFromLastResults)
	{
		super(ctx, order, promotionResults, changeFromLastResults);
		this.optimizedPromotionResults = null;
		this.optimizedCart = null;
	}

	public OptimizedPromotionOrderResults(final SessionContext ctx, final AbstractOrder order,
			final List<PromotionResult> promotionResults, final OptimizedCartData optimizedCart,
			final List<OptimizedPromotionResultData> optimizedProResults, final double changeFromLastResults)
	{
		super(ctx, order, promotionResults, changeFromLastResults);
		this.optimizedPromotionResults = optimizedProResults;
		this.optimizedCart = optimizedCart;
	}
	
	public List<OptimizedPromotionResultData> getOptimizedAppliedOrderPromotions() {
		if (this.appliedOrderPromotions == null) {
			PromotionOrderResults promotionOrderResults = this;
			synchronized (promotionOrderResults) {
				if (this.appliedOrderPromotions == null) {
					this.appliedOrderPromotions = this.getOptimizedPromotionResults(PromotionResultStatus.AppliedOnly,
							PromotionResultProducts.NoConsumedProducts);
				}
			}
		}
		return this.appliedOrderPromotions;
	}
	
	public List<OptimizedPromotionResultData> getOptimizedPotentialOrderPromotions() {
		if (this.potentialOrderPromotions == null) {
			PromotionOrderResults promotionOrderResults = this;
			synchronized (promotionOrderResults) {
				if (this.potentialOrderPromotions == null) {
					this.potentialOrderPromotions = this.getOptimizedPromotionResults(PromotionResultStatus.CouldFireOnly,
							PromotionResultProducts.NoConsumedProducts);
				}
			}
		}
		return this.potentialOrderPromotions;
	}

	
	public List<OptimizedPromotionResultData> getOptimizedAppliedProductPromotions() {
		if (this.appliedProductPromotions == null) {
			PromotionOrderResults promotionOrderResults = this;
			synchronized (promotionOrderResults) {
				if (this.appliedProductPromotions == null) {
					this.appliedProductPromotions = this.getOptimizedPromotionResults(PromotionResultStatus.AppliedOnly,
							PromotionResultProducts.RequireConsumedProducts);
				}
			}
		}
		return this.appliedProductPromotions;
	}
	
	public List<OptimizedPromotionResultData> getOptimizedPotentialProductPromotions() {
		if (this.potentialProductPromotions == null) {
			PromotionOrderResults promotionOrderResults = this;
			synchronized (promotionOrderResults) {
				if (this.potentialProductPromotions == null) {
					this.potentialProductPromotions = this.getOptimizedPromotionResults(PromotionResultStatus.CouldFireOnly,
							PromotionResultProducts.RequireConsumedProducts);
				}
			}
		}
		return this.potentialProductPromotions;
	}
	
	protected List<OptimizedPromotionResultData> getOptimizedPromotionResults(PromotionResultStatus statusFlag,
			PromotionResultProducts productsFlag) {
		LinkedList<OptimizedPromotionResultData> tmpResults = new LinkedList<OptimizedPromotionResultData>();
		if(this.optimizedPromotionResults == null)
		{
			return tmpResults;
		}
		
		for (OptimizedPromotionResultData promotionResult : this.optimizedPromotionResults) {
			boolean productsOk;
			boolean statusOk = false;
			if (statusFlag == PromotionResultStatus.Any
					|| statusFlag == PromotionResultStatus.CouldFireOnly && getCouldFire(promotionResult)) {
				statusOk = true;
			} else if ((statusFlag == PromotionResultStatus.FiredOnly || statusFlag == PromotionResultStatus.AppliedOnly
					|| statusFlag == PromotionResultStatus.FiredOrApplied) && getFired(promotionResult)) {
				if (statusFlag == PromotionResultStatus.FiredOrApplied) {
					statusOk = true;
				} else if (isApplied(promotionResult)) {
					statusOk = statusFlag == PromotionResultStatus.AppliedOnly;
				} else {
					boolean bl = statusOk = statusFlag == PromotionResultStatus.FiredOnly;
				}
			}
			if (!statusOk)
				continue;
			if (productsFlag == PromotionResultProducts.Any) {
				productsOk = true;
			} else {
				Collection consumed = promotionResult.getConsumedEntries();
				boolean hasConsumedProducts = consumed != null && !consumed.isEmpty();
				boolean bl = productsOk = productsFlag == PromotionResultProducts.RequireConsumedProducts
						&& hasConsumedProducts
						|| productsFlag == PromotionResultProducts.NoConsumedProducts && !hasConsumedProducts;
			}
			if (!productsOk)
				continue;
			tmpResults.add(promotionResult);
		}
		return Collections.unmodifiableList(tmpResults);
	}
	
	public boolean getFired(final OptimizedPromotionResultData promotionResult)
	{
		if (promotionResult.getCertainty().floatValue() >= 1.0f)
		{
			return true;
		}
		return false;
	}

	public boolean getCouldFire(final OptimizedPromotionResultData promotionResult)
	{
		final float certainty = promotionResult.getCertainty().floatValue();
		if (certainty < 1.0f)
		{
			return true;
		}
		return false;
	}

	public boolean isApplied(final OptimizedPromotionResultData promotionResult)
	{
		Collection<AbstractOptimizedPromotionActionData> actions;
		if (this.getFired(promotionResult) && (actions = promotionResult.getActions()) != null && !actions.isEmpty())
		{
			for (final AbstractOptimizedPromotionActionData action : actions)
			{
				if (action.getMarkedApplied().booleanValue())
				{
					continue;
				}
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * @return the optimizedPromotionResults
	 */
	public List<OptimizedPromotionResultData> getOptimizedPromotionResults()
	{
		return optimizedPromotionResults;
	}

	/**
	 * @return the optimizedCart
	 */
	public OptimizedCartData getOptimizedCart()
	{
		return optimizedCart;
	}
}
