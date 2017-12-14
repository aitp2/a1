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

import de.hybris.platform.promotions.PromotionResultService;

import com.accenture.performance.optimization.data.OptimizedPromotionResultData;


/**
 *
 */
public interface OptimizePromotionResultService extends PromotionResultService
{
	String getDescription(OptimizedPromotionResultData arg0);
}
