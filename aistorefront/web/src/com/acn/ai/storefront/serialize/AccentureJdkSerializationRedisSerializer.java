package com.acn.ai.storefront.serialize;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;


/**
 * @author mingming.wang
 *
 */
public class AccentureJdkSerializationRedisSerializer implements RedisSerializer<Object>
{

	private final Converter<Object, byte[]> serializer = new SerializingConverter(new DefaultAccentureSerializer());
	private final Converter<byte[], Object> deserializer = new DeserializingConverter(new DefaultAccentureDeserializer());

	@Override
	public Object deserialize(final byte[] bytes)
	{

		if (bytes == null || bytes.length == 0)
		{
			return null;
		}
		try
		{
			return this.deserializer.convert(bytes);
		}
		catch (final Exception ex)
		{
			throw new SerializationException("Cannot deserialize", ex);
		}
	}

	@Override
	public byte[] serialize(final Object object)
	{
		if (object == null)
		{
			return new byte[0];
		}
		try
		{
			return this.serializer.convert(object);
		}
		catch (final Exception ex)
		{
			throw new SerializationException("Cannot serialize", ex);
		}
	}
}
