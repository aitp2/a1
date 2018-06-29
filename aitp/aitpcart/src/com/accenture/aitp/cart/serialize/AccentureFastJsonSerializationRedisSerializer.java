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

import java.nio.charset.Charset;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;


/**
 *
 */
public class AccentureFastJsonSerializationRedisSerializer<T> implements RedisSerializer<T>
{

	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	private final Class<T> clazz;

	public AccentureFastJsonSerializationRedisSerializer()
	{
		super();
		this.clazz = (Class<T>) Object.class;
	}

	public AccentureFastJsonSerializationRedisSerializer(final Class<T> clazz)
	{
		super();
		this.clazz = clazz;
	}

	@Override
	public byte[] serialize(final T t) throws SerializationException
	{
		if (t == null)
		{
			return new byte[0];
		}
		return JSON.toJSONString(t, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
	}

	@Override
	public T deserialize(final byte[] bytes) throws SerializationException
	{
		if (bytes == null || bytes.length <= 0)
		{
			return null;
		}
		final String str = new String(bytes, DEFAULT_CHARSET);
		return JSON.parseObject(str, clazz);
	}

}

