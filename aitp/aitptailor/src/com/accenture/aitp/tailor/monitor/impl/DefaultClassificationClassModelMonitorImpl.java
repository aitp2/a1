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

import de.hybris.platform.catalog.model.classification.ClassificationClassModel;
import de.hybris.platform.core.model.ItemModel;

import com.accenture.aitp.tailor.data.ModelMonitoredInfo;

/**
 *
 */
public class DefaultClassificationClassModelMonitorImpl extends DefaultAitpAbstractModelMonitorImpl
{

	@Override
	public boolean accept(final ItemModel object)
	{
		if (object instanceof ClassificationClassModel)
		{
			//TODO a1
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public void put(final ItemModel object)
	{
		final ClassificationClassModel model = (ClassificationClassModel) object;
		final ModelMonitoredInfo info = new ModelMonitoredInfo();
		info.setTypeCode(ClassificationClassModel._TYPECODE);

		info.setPk(model.getPk());
		info.setCode(model.getCode());
		getAitpModelMonitorQueueStrateg().put(info);
	}

	@Override
	public Object take()
	{
		// YTODO Auto-generated method stub
		return null;
	}

}
