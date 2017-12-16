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
package com.accenture.performance.optimization.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.model.OptimizedCartModel;


/**
 *
 */
public class DefaultRedisModelDealService extends DefatulOptimizeModelDealService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultRedisModelDealService.class);
	private RedisTemplate<String, Object> redisTemplate;

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

	@Override
	protected OptimizedCartData recoverCart(final OptimizedCartModel cartModel)
	{
		final Object obj = this.getRedisTemplate().opsForValue().get(cartModel.getGuid());
		if (obj != null)
		{
			return (OptimizedCartData) obj;
		}
		else
		{
			final OptimizedCartData cart = new OptimizedCartData();
			cart.setCode(cartModel.getCode());
			cart.setUserId(cartModel.getUserId());
			cart.setCurrencyCode(cartModel.getCurrencyCode());
			cart.setBaseSite(cartModel.getSite().getUid());
			cart.setBaseStore(cartModel.getStore().getUid());
			cart.setGuid(cartModel.getGuid());
			cart.setCurrencyCode(cartModel.getCurrencyCode());
			return cart;
		}
	}

	@Override
	public void persistCart(final OptimizedCartData cart)
	{
		OptimizedCartModel cartModel = getRelatedCartModel(cart);
		if (cartModel == null)
		{
			cartModel = this.getModelService().create(OptimizedCartModel.class);
			if (cart.getCode() == null)
			{
				cartModel.setCode(String.valueOf(this.getKeyGenerator().generate()));
				cart.setCode(cartModel.getCode());
			}
			else
			{
				cartModel.setCode(cart.getCode());
			}
		}

		fillCartModelOnly(cart, cartModel);

		this.getModelService().saveAll(cartModel);

		this.getRedisTemplate().opsForValue().set(cart.getGuid(), cart);
	}

	@Override
	public void removePersistentCart(final String cartGuid, final String userid)
	{
		super.removePersistentCart(cartGuid, userid);
		this.getRedisTemplate().delete(cartGuid);
	}

}
