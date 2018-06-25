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
package com.accenture.aitp.cart.jalo;

import de.hybris.platform.core.Registry;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.security.JaloSecurityException;
import de.hybris.platform.promotions.jalo.PromotionResult;
import de.hybris.platform.servicelayer.internal.jalo.order.InMemoryCart;

import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;


/**
 *
 */
public class AitpPromotionCart extends InMemoryCart
{

	@Override
	public Object getAttribute(final SessionContext ctx, final String qualifier)
			throws JaloInvalidParameterException, JaloSecurityException
	{
		Object retval = null;
		if ("allPromotionResults".equals(qualifier))
		{
			final RedisTemplate redisTemplate = (RedisTemplate) Registry.getApplicationContext().getBean("redisTemplate");

			retval = redisTemplate.execute(new RedisCallback<Set<PromotionResult>>()
			{
				@Override
				public Set<PromotionResult> doInRedis(final RedisConnection redisConnection) throws DataAccessException
				{
					redisConnection.select(2);
					final byte[] result = redisConnection.get(redisTemplate.getKeySerializer().serialize(getCode()));
					return (Set<PromotionResult>) redisTemplate.getValueSerializer().deserialize(result);
				}

			});
		}
		else
		{
			retval = super.getAttribute(ctx, qualifier);
		}
		return retval;
	}
}
