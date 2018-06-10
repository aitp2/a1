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

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotionengineservices.promotionengine.impl.DefaultPromotionEngineService;
import de.hybris.platform.promotions.model.PromotionGroupModel;
import de.hybris.platform.promotions.result.PromotionOrderResults;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;


/**
 *
 */
public class AitpPromotionEngineService extends DefaultPromotionEngineService
{

	//private RedisTemplate redisTemplate;

	@Override
	protected PromotionOrderResults updatePromotionsNotThreadSafe(final Collection<PromotionGroupModel> promotionGroups,
			final AbstractOrderModel order, final Date date)
	{
		final PromotionOrderResults promotionOrderResult = super.updatePromotionsNotThreadSafe(promotionGroups, order, date);

		order.setAllPromotionResults(getModelService().getAll(promotionOrderResult.getAllResults(), new HashSet()));
		getModelService().save(order);
		/*
		 * getRedisTemplate().execute(new RedisCallback() {
		 *
		 * @Override public Object doInRedis(final RedisConnection redisConnection) throws DataAccessException {
		 * redisConnection.select(promotionResultDatabase);
		 * redisConnection.set(getRedisTemplate().getKeySerializer().serialize(order.getCode()),
		 * getRedisTemplate().getValueSerializer().serialize(new HashSet<>(promotionOrderResult.getAllResults()))); return
		 * Boolean.TRUE; }
		 *
		 * });
		 */
		return promotionOrderResult;
	}

	@Override
	protected void cleanupAbstractOrder(final AbstractOrderModel cart)
	{
		super.cleanupAbstractOrder(cart);
		cart.setAllPromotionResults(new HashSet());
		getModelService().save(cart);
	}
}
