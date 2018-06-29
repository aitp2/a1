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
package com.accenture.aitp.cart.serialize.kryo;

import de.hybris.platform.core.Registry;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.servicelayer.model.ModelService;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


/**
 *
 */
public class ItemSerializer extends Serializer<Item>
{

	@Override
	public void write(final Kryo kryo, final Output output, final Item item)
	{
		output.writeLong(item.getPK().getLongValue());
	}

	@Override
	public Item read(final Kryo kryo, final Input input, final Class<? extends Item> type)
	{
		final ModelService modelService = (ModelService) Registry.getApplicationContext().getBean("modelService");
		final long pkLong = input.readLong();
		return modelService.get(Long.valueOf(pkLong), type.getSimpleName());
	}

}
