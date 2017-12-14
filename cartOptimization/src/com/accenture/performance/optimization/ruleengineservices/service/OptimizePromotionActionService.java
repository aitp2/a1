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
package com.accenture.performance.optimization.ruleengineservices.service;

import de.hybris.platform.promotionengineservices.promotionengine.PromotionActionService;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;

import com.accenture.performance.optimization.data.OptimizedPromotionResultData;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;


/**
 *
 */
public interface OptimizePromotionActionService extends PromotionActionService
{
	OptimizedCartEntryData getOrderEntryData(AbstractRuleActionRAO action);

	void recalculateTotals(OptimizedCartData cartData);

	OptimizedPromotionResultData createPromotionResultData(AbstractRuleActionRAO actionRao);

	void createDiscountValue(DiscountRAO discountRao, String code, OptimizedCartData cartData);

	void createDiscountValue(DiscountRAO arg0, String arg1, OptimizedCartEntryData entry);

	OptimizedCartData getOrderData(AbstractRuleActionRAO action);

}
