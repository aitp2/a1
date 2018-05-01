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

import org.apache.log4j.Logger;

import com.accenture.aitp.tailor.data.ModelMonitoredInfo;

/**
 *
 */
public class DefaultClassificationClassModelMonitorImpl extends DefaultAitpAbstractModelMonitorImpl
{
	private final static Logger LOG = Logger.getLogger(DefaultClassificationClassModelMonitorImpl.class);

	@Override
	public boolean accept(final Object object)
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
	public void publish(final Object object)
	{
		final ClassificationClassModel model = (ClassificationClassModel) object;
		final ModelMonitoredInfo info = createModelMonitoredInfo();
		info.setPk(model.getPk());
		info.setCode(model.getCode());
		getAitpModelMonitorQueueStrateg().put(info);
	}

	@Override
	public void consume0(final ModelMonitoredInfo info)
	{
		// YTODO Auto-generated method stub

	}
}
