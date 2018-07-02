/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2018 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package com.accenture.aitp.cart.service.impl;

import de.hybris.platform.promotionengineservices.promotionengine.impl.DefaultPromotionActionService;
import de.hybris.platform.promotions.model.PromotionOrderEntryConsumedModel;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;

import java.util.Collection;
import java.util.Collections;
import org.apache.commons.collections4.CollectionUtils;


/**
 *
 */
public class AitpPromotionActionService extends DefaultPromotionActionService
{
	@Override
	protected Collection<PromotionOrderEntryConsumedModel> createConsumedEntries(final AbstractRuleActionRAO action)
	{

		final Collection<PromotionOrderEntryConsumedModel> consumedEntries = super.createConsumedEntries(action);
		return CollectionUtils.isEmpty(consumedEntries) ? Collections.EMPTY_LIST : consumedEntries;
	}

}
