/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package org.training.xstream;

import java.io.Writer;

import org.springframework.oxm.xstream.XStreamMarshaller;

import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonWriter;


public class JsonXStreamMarshallerFactory extends XmlXStreamMarshallerFactory
{
	private XStreamMarshaller jsonMarshallerInstance;

	@SuppressWarnings("PMD")
	@Override
	public void afterPropertiesSet() throws Exception
	{
		jsonMarshallerInstance = getObjectInternal();
	}

	@SuppressWarnings("PMD")
	@Override
	public Object getObject() throws Exception
	{
		return jsonMarshallerInstance;
	}

	/**
	 * creates a custom json writer which swallows top most root nodes
	 */
	@Override
	protected XStreamMarshaller createMarshaller()
	{
		final XStreamMarshaller marshaller = super.createMarshaller();
		marshaller.setStreamDriver(new com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver()
		{
			@Override
			public HierarchicalStreamWriter createWriter(final Writer writer)
			{
				return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
			}
		});
		return marshaller;
	}
}
