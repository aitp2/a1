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
package com.accenture.aitp.cart.serialize;

import de.hybris.platform.jalo.JaloSession;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.accenture.aitp.cart.strategy.CartSerializerStrategy;


/**
 *
 */
public class AitpRedisSerializer implements RedisSerializer<Object>
{
	private final ThreadLocal<Boolean> serializer = new ThreadLocal<Boolean>()
	{
		@Override
		protected Boolean initialValue()
		{
			return Boolean.TRUE;
		}
	};
	private CartSerializerStrategy cartSerializerStrategy;
	private RedisSerializer redisSerializer;

	@Override
	public Object deserialize(final byte[] bytes) throws SerializationException
	{
		return getRedisSerializer().deserialize(bytes);
	}

	@Override
	public byte[] serialize(final Object object) throws SerializationException
	{
		if (object instanceof JaloSession && null == serializer.get())
		{
			serializer.set(Boolean.TRUE);
			cartSerializerStrategy.serializerSessionCart((JaloSession) object);
		}
		return getRedisSerializer().serialize(object);
	}

	/**
	 * @return the redisSerializer
	 */
	public RedisSerializer getRedisSerializer()
	{
		return redisSerializer;
	}

	/**
	 * @param redisSerializer
	 *           the redisSerializer to set
	 */
	public void setRedisSerializer(final RedisSerializer redisSerializer)
	{
		this.redisSerializer = redisSerializer;
	}

	/**
	 * @param cartSerializerStrategy
	 *           the cartSerializerStrategy to set
	 */
	public void setCartSerializerStrategy(final CartSerializerStrategy cartSerializerStrategy)
	{
		this.cartSerializerStrategy = cartSerializerStrategy;
	}
}
