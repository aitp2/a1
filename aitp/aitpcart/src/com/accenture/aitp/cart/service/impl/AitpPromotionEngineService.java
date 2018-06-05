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

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;


/**
 *
 */
public class AitpPromotionEngineService extends DefaultPromotionEngineService
{

	private RedisTemplate redisTemplate;
	private int promotionResultDatabase;

	@Override
	protected PromotionOrderResults updatePromotionsNotThreadSafe(final Collection<PromotionGroupModel> promotionGroups,
			final AbstractOrderModel order, final Date date)
	{
		final PromotionOrderResults promotionOrderResult = super.updatePromotionsNotThreadSafe(promotionGroups, order, date);
		getRedisTemplate().execute(new RedisCallback()
		{
			@Override
			public Object doInRedis(final RedisConnection redisConnection) throws DataAccessException
			{
				redisConnection.select(promotionResultDatabase);
				redisConnection.set(getRedisTemplate().getKeySerializer().serialize(order.getCode()),
						getRedisTemplate().getValueSerializer().serialize(promotionOrderResult.getAllResults()));
				return Boolean.TRUE;
			}

		});
		return promotionOrderResult;
	}

	/**
	 * @return the redisTemplate
	 */
	public RedisTemplate getRedisTemplate()
	{
		return redisTemplate;
	}

	/**
	 * @param redisTemplate
	 *           the redisTemplate to set
	 */
	public void setRedisTemplate(final RedisTemplate redisTemplate)
	{
		this.redisTemplate = redisTemplate;
	}

	/**
	 * @param promotionResultDatabase
	 *           the promotionResultDatabase to set
	 */
	public void setPromotionResultDatabase(final int promotionResultDatabase)
	{
		this.promotionResultDatabase = promotionResultDatabase;
	}

}
