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
package com.accenture.aitp.tailor.strategy.impl;

import java.util.concurrent.LinkedTransferQueue;

import org.apache.log4j.Logger;

import com.accenture.aitp.tailor.data.ModelMonitoredInfo;
import com.accenture.aitp.tailor.strategy.AitpModelMonitorQueueStrategy;

/**
 *
 */
public class DefaultAitpModelMonitorQueueStrategyImpl implements AitpModelMonitorQueueStrategy
{
	private final static Logger LOG = Logger.getLogger(DefaultAitpModelMonitorQueueStrategyImpl.class);
	private final LinkedTransferQueue<ModelMonitoredInfo> queue = new LinkedTransferQueue<>();

	@Override
	public void put(final ModelMonitoredInfo info)
	{
		queue.put(info);
	}

	@Override
	public ModelMonitoredInfo take()
	{
		try
		{
			return queue.take();
		}
		catch (final InterruptedException e)
		{
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

}
