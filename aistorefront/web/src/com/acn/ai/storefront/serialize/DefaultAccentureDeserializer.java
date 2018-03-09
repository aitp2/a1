/**
 *
 */
package com.acn.ai.storefront.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
import org.springframework.core.NestedIOException;
import org.springframework.core.serializer.Deserializer;


/**
 * @author mingming.wang
 *
 */
public class DefaultAccentureDeserializer implements Deserializer<Object>
{
	private static final Logger logger = Logger.getLogger(DefaultAccentureDeserializer.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.core.serializer.Deserializer#deserialize(java.io.InputStream)
	 */
	@Override
	public Object deserialize(final InputStream inputStream) throws IOException
	{

		final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		try
		{
			//			logger.info("DefaultAccentureDeserializer-deserialize --AccentureJdkSerializationRedisSerializer------"
			//					+ getClass().getClassLoader());

			return objectInputStream.readObject();
		}
		catch (final ClassNotFoundException ex)
		{
			throw new NestedIOException("Failed to deserialize object type", ex);
		}
	}


}
