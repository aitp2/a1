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

import de.hybris.platform.cms2.jalo.site.CMSSite;
import de.hybris.platform.jalo.c2l.Currency;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.jalo.product.Unit;
import de.hybris.platform.jalo.user.Customer;
import de.hybris.platform.servicelayer.internal.jalo.order.InMemoryCartEntry;
import de.hybris.platform.servicelayer.internal.jalo.order.JaloOnlyItemHelper;
import de.hybris.platform.store.BaseStore;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.accenture.aitp.cart.jalo.AitpInMemoryCart;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.BeanSerializer;
import com.esotericsoftware.kryo.serializers.JavaSerializer;


/**
 *
 */
public class AccentureKryoSerializationRedisSerializer implements RedisSerializer<Object>
{
	private final static Logger logger = Logger.getLogger(AccentureKryoSerializationRedisSerializer.class);

	private final Map<Class<?>, Serializer> customizationMap = new HashMap<>();

	@Override
	public byte[] serialize(final Object obj) throws SerializationException
	{
		if (obj == null)
		{
			return EMPTY_ARRAY;
		}
		final Kryo kryo = kryos.get();
		final Output output = new Output(1024, -1);


		try
		{
			kryo.writeClassAndObject(output, obj);
			return output.toBytes();
		}
		finally
		{
			closeOutputStream(output);
		}


	}


	@Override
	public Object deserialize(final byte[] bytes) throws SerializationException
	{
		if (isEmpty(bytes))
		{
			return null;
		}
		final Kryo kryo = kryos.get();
		Input input = null;
		try
		{
			input = new Input(bytes);
			return kryo.readClassAndObject(input);
		}
		finally
		{
			closeInputStream(input);
		}
	}


	private static void closeOutputStream(final OutputStream output)
	{
		if (output != null)
		{
			try
			{
				output.flush();
				output.close();
			}
			catch (final Exception e)
			{
				logger.error("serialize object close outputStream exception", e);
			}
		}
	}


	private static void closeInputStream(final InputStream input)
	{
		if (input != null)
		{
			try
			{
				input.close();
			}
			catch (final Exception e)
			{
				logger.error("serialize object close inputStream exception", e);
			}
		}
	}


	private static final ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>()
	{
		@Override
		protected Kryo initialValue()
		{
			final Kryo kryo = new Kryo();
			kryo.setRegistrationRequired(false);
			kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
			// cart session
			kryo.register(JaloOnlyItemHelper.class, new BeanSerializer(kryo, JaloOnlyItemHelper.class));
			kryo.register(AitpInMemoryCart.class, new JavaSerializer());
			kryo.register(InMemoryCartEntry.class, new JavaSerializer());

			kryo.register(Unit.class, new JavaSerializer());
			kryo.register(Customer.class, new JavaSerializer());
			kryo.register(Currency.class, new JavaSerializer());
			kryo.register(BaseStore.class, new JavaSerializer());
			kryo.register(CMSSite.class, new JavaSerializer());
			kryo.register(Product.class, new JavaSerializer());

			return kryo;
		}
	};


	/**
	 * 空byte数组
	 */
	public static final byte[] EMPTY_ARRAY = new byte[0];

	public static boolean isEmpty(final byte[] data)
	{
		return (data == null || data.length == 0);
	}

}
