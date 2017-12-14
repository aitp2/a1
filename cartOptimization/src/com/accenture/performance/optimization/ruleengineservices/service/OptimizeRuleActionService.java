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

import de.hybris.platform.ruleengineservices.action.RuleActionService;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;

import java.util.List;


/**
 *
 */
public interface OptimizeRuleActionService extends RuleActionService
{
	public List<Object> applyAllActionsForData(final RuleEngineResultRAO ruleEngineResultRAO);
}
