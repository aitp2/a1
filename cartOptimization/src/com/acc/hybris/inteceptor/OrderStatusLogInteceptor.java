/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2017 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package com.acc.hybris.inteceptor;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;



/**
 *
 */
public class OrderStatusLogInteceptor implements PrepareInterceptor<OrderModel>
{
	private static final Logger LOG = Logger.getLogger(OrderStatusLogInteceptor.class);

	@Override
	public void onPrepare(final OrderModel model, final InterceptorContext ctx) throws InterceptorException
	{
		if (ctx.getModelService().isNew(model))
		{
			LOG.info("[ORD][ID:" + model.getCode() + "][STATUS:" + model.getStatus() + "]MSG::order created...::MSG");
		}
		else
		{
			final Map<String, Set<Locale>> dirtyMap = ctx.getDirtyAttributes(model);
			if (dirtyMap.containsKey(AbstractOrderModel.STATUS))
			{
				dirtyMap.get(AbstractOrderModel.STATUS);
				LOG.info("[ORD][ID:" + model.getCode() + "][STATUS:" + model.getStatus() + "]MSG::order status changed...::MSG");
			}
		}

	}


}
