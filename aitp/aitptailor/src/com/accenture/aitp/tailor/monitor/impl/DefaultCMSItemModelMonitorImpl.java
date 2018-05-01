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

import de.hybris.platform.cms2.model.contents.CMSItemModel;

import org.apache.log4j.Logger;

import com.accenture.aitp.tailor.data.ModelMonitoredInfo;

/**
 *
 */
public class DefaultCMSItemModelMonitorImpl extends DefaultAitpAbstractModelMonitorImpl
{
	private final static Logger LOG = Logger.getLogger(DefaultCMSItemModelMonitorImpl.class);

	@Override
	public boolean accept(final Object object)
	{
		if (object instanceof CMSItemModel)
		{
			return isOnlineVersion(((CMSItemModel) object).getCatalogVersion());
		}
		else
		{
			return false;
		}
	}

	@Override
	public void publish(final Object object)
	{
		final CMSItemModel model = (CMSItemModel) object;
		final ModelMonitoredInfo info = createModelMonitoredInfo();
		info.setPk(model.getPk());
		info.setUid(model.getUid());
		//TODO a1 get the page related
		getAitpModelMonitorQueueStrateg().put(info);

	}

	@Override
	public void consume0(final ModelMonitoredInfo info)
	{
		// YTODO Auto-generated method stub

	}

}
