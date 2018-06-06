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
package com.accenture.aitp.cart.aspectj;

import de.hybris.platform.promotionengineservices.model.AbstractRuleBasedPromotionActionModel;
import de.hybris.platform.promotions.model.AbstractPromotionActionModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.ruleengine.RuleActionMetadataHandler;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;


/**
 *
 */
public class AitpRuleActionMetadataHandler implements RuleActionMetadataHandler<AbstractRuleBasedPromotionActionModel>
{

	@Override
	public void handle(final AbstractRuleBasedPromotionActionModel action, final String arg1)
	{
		final PromotionResultModel promotionResult = action.getPromotionResult();
		Collection<AbstractPromotionActionModel> actionList;
		if (CollectionUtils.isEmpty(promotionResult.getActions()))
		{
			actionList = new ArrayList();
		}
		else
		{
			actionList = new ArrayList(promotionResult.getActions());
		}
		actionList.add(action);
		promotionResult.setActions(actionList);
	}

	@Override
	public void undoHandle(final AbstractRuleBasedPromotionActionModel arg0)
	{
		//nothing need to impl

	}

}
