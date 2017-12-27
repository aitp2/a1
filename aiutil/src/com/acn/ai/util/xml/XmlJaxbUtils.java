/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture yong.c.sun
 *  @date: Dec 28, 2016
 */
package com.acn.ai.util.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


/**
 *
 */
public class XmlJaxbUtils<T>
{

	public static <T> String marshal(final T t) throws JAXBException
	{
		final StringWriter writer = new StringWriter();
		final JAXBContext jc = JAXBContext.newInstance(t.getClass());
		final Marshaller u = jc.createMarshaller();
		u.marshal(t, writer);
		return writer.toString();
	}

	public static <T> T unmarshal(final String xmlString, final Class<T> clazz) throws JAXBException
	{
		T resp = null;
		final ByteArrayInputStream stream = new ByteArrayInputStream(xmlString.getBytes());
		final JAXBContext jc = JAXBContext.newInstance(clazz);
		final Unmarshaller u = jc.createUnmarshaller();
		resp = (T) u.unmarshal(stream);
		try
		{
			stream.close();
		}
		catch (final IOException e)
		{
			// YTODO Auto-generated catch block
			e.printStackTrace();
		}
		return resp;
	}
}
