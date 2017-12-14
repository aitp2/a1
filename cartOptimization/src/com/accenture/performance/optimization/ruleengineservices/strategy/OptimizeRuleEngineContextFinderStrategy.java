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

import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;
import de.hybris.platform.ruleengine.strategies.impl.DefaultRuleEngineContextFinderStrategy;

import java.util.Collection;
import java.util.Optional;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;


/**
 *
 */
public class OptimizeRuleEngineContextFinderStrategy extends DefaultRuleEngineContextFinderStrategy
{
	// assumption: no catalog in orderdata
	public <T extends AbstractRuleEngineContextModel, O extends OptimizedCartData> Optional<T> findRuleEngineContext(final O order,
			final RuleType ruleType)
	{
		// assumption: no catalog on order data
		//		Collection catalogVersions = this.getCatalogVersionsForProducts(this.getProductsForOrderData(order));
		//		if (CollectionUtils.isEmpty(catalogVersions))
		//		{
		final Collection catalogVersions = this.getAvailableCatalogVersions();
		//		}

		return this.getRuleEngineContextForCatalogVersions(catalogVersions, ruleType);
	}

}
