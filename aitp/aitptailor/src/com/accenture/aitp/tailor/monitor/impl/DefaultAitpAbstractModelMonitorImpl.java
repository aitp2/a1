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

import de.hybris.platform.catalog.model.CatalogVersionModel;

import com.accenture.aitp.tailor.monitor.AitpModelMonitor;
import com.accenture.aitp.tailor.strategy.AitpModelMonitorQueueStrateg;

/**
 *
 */
public abstract class DefaultAitpAbstractModelMonitorImpl implements AitpModelMonitor
{
	private AitpModelMonitorQueueStrateg aitpModelMonitorQueueStrateg;

	protected boolean isOnlineVersion(final CatalogVersionModel catalogVersion)
	{
		return AitpModelMonitor.ONLINE.equalsIgnoreCase(catalogVersion == null ? null : catalogVersion.getVersion());
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
