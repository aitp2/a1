/**
 *
 */
package com.accenture.aitp.cart.serialize;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.ConfigurableObjectInputStream;
import org.springframework.core.NestedIOException;
import org.springframework.core.serializer.Deserializer;


/**
 * @author mingming.wang
 *
 */
public class DefaultAccentureDeserializer implements Deserializer<Object>
{
	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.core.serializer.Deserializer#deserialize(java.io.InputStream)
	 */
	@Override
	public Object deserialize(final InputStream inputStream) throws IOException
	{

		try
		{

			final ConfigurableObjectInputStream objectInputStream = new ConfigurableObjectInputStream(inputStream,
					Thread.currentThread().getContextClassLoader());

			return objectInputStream.readObject();
		}
		catch (final ClassNotFoundException ex)
		{
			throw new NestedIOException("Failed to deserialize object type", ex);
		}
	}


}
