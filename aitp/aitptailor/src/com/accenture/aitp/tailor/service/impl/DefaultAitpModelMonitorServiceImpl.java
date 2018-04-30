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
package com.accenture.aitp.tailor.service.impl;

import de.hybris.platform.core.model.ItemModel;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.accenture.aitp.tailor.data.ModelMonitoredInfo;
import com.accenture.aitp.tailor.monitor.AitpModelMonitor;
import com.accenture.aitp.tailor.service.AitpModelMonitorService;
import com.accenture.aitp.tailor.strategy.AitpModelMonitorQueueStrateg;

/**
 *
 */
public class DefaultAitpModelMonitorServiceImpl implements AitpModelMonitorService
{
	private List<AitpModelMonitor> aitpModelMonitorList;
	private AitpModelMonitorQueueStrateg aitpModelMonitorQueueStrateg;

	@Override
	public void put(final ItemModel object)
	{
		if (CollectionUtils.isEmpty(getAitpModelMonitorList()))
		{
			return;
		}

		for (final AitpModelMonitor monitor : aitpModelMonitorList)
		{
			if (monitor.accept(object))
			{
				monitor.put(object);
				return;
			}
		}

	}

	@Override
	public ModelMonitoredInfo take()
	{
		//TODO a1 refactor
		return getAitpModelMonitorQueueStrateg().take();
	}

	/**
	 * @return the aitpModelMonitorList
	 */
	public List<AitpModelMonitor> getAitpModelMonitorList()
	{
		return aitpModelMonitorList;
	}

	/**
	 * @param aitpModelMonitorList
	 *           the aitpModelMonitorList to set
	 */
	public void setAitpModelMonitorList(final List<AitpModelMonitor> aitpModelMonitorList)
	{
		this.aitpModelMonitorList = aitpModelMonitorList;
	}

	/**
	 * @return the aitpModelMonitorQueueStrateg
	 */
	public AitpModelMonitorQueueStrateg getAitpModelMonitorQueueStrateg()
	{
		return aitpModelMonitorQueueStrateg;
	}

	/**
	 * @param aitpModelMonitorQueueStrateg
	 *           the aitpModelMonitorQueueStrateg to set
	 */
	public void setAitpModelMonitorQueueStrateg(final AitpModelMonitorQueueStrateg aitpModelMonitorQueueStrateg)
	{
		this.aitpModelMonitorQueueStrateg = aitpModelMonitorQueueStrateg;
	}

}
