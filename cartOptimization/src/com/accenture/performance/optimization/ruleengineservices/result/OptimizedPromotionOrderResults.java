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

import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.promotions.jalo.PromotionResult;
import de.hybris.platform.promotions.result.PromotionOrderResults;

import java.util.List;

import com.accenture.performance.optimization.data.OptimizedPromotionResultData;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;


/**
 * append two attribute to save optimized cart data
 */
public class OptimizedPromotionOrderResults extends PromotionOrderResults
{
	private final List<OptimizedPromotionResultData> optimizedPromotionResults;
	private final OptimizedCartData optimizedCart;

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
