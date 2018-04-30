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
package com.accenture.aitp.tailor.monitor.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.product.ProductModel;

import com.accenture.aitp.tailor.data.ModelMonitoredInfo;


/**
 *
 */
public class DefaultProductModelMonitorImpl extends DefaultAitpAbstractModelMonitorImpl
{

	@Override
	public boolean accept(final ItemModel object)
	{
		if( object instanceof ProductModel )
		{
			return isOnlineVersion(((ProductModel) object).getCatalogVersion());
		}
		else
		{
			return false;
		}

	}

	@Override
	public void put(final ItemModel object)
	{
		final ProductModel product = (ProductModel) object;
		final ModelMonitoredInfo info = new ModelMonitoredInfo();
		info.setTypeCode(ProductModel._TYPECODE);

		info.setPk(product.getPk());
		info.setCode(product.getCode());

		getAitpModelMonitorQueueStrateg().put(info);

	}

	@Override
	public Object take()
	{
		// YTODO Auto-generated method stub
		return null;
	}

}
