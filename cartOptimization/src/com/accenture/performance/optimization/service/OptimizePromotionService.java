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
package com.accenture.performance.optimization.service;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.promotions.PromotionsService;
import de.hybris.platform.promotions.model.PromotionGroupModel;
import de.hybris.platform.promotions.result.PromotionOrderResults;
import de.hybris.platform.ruleengine.RuleEvaluationResult;

import java.util.Collection;
import java.util.Date;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;


/**
 *
 */
public interface OptimizePromotionService extends PromotionsService
{
	PromotionOrderResults updatePromotions(Collection<PromotionGroupModel> promotionGroups, OptimizedCartData cartData, Date date);

	public RuleEvaluationResult evaluate(final OptimizedCartData order, final Collection<PromotionGroupModel> promotionGroups,
			final Date date);

	void transferPromotionsToOrder(OptimizedCartData cartModel, OrderModel target, boolean onlyTransferAppliedPromotions);
}
