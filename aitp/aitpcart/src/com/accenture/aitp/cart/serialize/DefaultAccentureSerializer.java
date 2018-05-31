/**
 *
 */
package com.accenture.aitp.cart.serialize;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.springframework.core.serializer.Serializer;


/**
 * @author mingming.wang
 *
 */
public class DefaultAccentureSerializer implements Serializer<Object>
{


	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.core.serializer.Serializer#serialize(java.lang.Object, java.io.OutputStream)
	 */
	@Override
	public void serialize(final Object object, final OutputStream outputStream) throws IOException
	{
		if (!(object instanceof Serializable))
		{
			throw new IllegalArgumentException(super.getClass().getSimpleName() + " requires a Serializable payload "
					+ "but received an object of type [" + object/* 41 */.getClass().getName() + "]");
		}
		final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(object);

		objectOutputStream.flush();

	}

}

