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
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.accenture.aitp.tailor.data.ModelMonitoredInfo;
import com.accenture.aitp.tailor.monitor.AitpModelMonitor;
import com.accenture.aitp.tailor.parser.AitpCacheKeyParser;
import com.accenture.aitp.tailor.strategy.AitpModelMonitorQueueStrateg;

/**
 *
 */
public abstract class DefaultAitpAbstractModelMonitorImpl implements AitpModelMonitor
{
	private final static Logger LOG = Logger.getLogger(DefaultAitpAbstractModelMonitorImpl.class);

	private AitpModelMonitorQueueStrateg aitpModelMonitorQueueStrateg;
	private AitpCacheKeyParser aitpCacheKeyParser;
	private String typeCode;

	protected boolean isOnlineVersion(final CatalogVersionModel catalogVersion)
	{
		return AitpModelMonitor.ONLINE.equalsIgnoreCase(catalogVersion == null ? null : catalogVersion.getVersion());
	}

	protected boolean isAccept(final ModelMonitoredInfo info, final String typeCodeRequired)
	{
		ServicesUtil.validateParameterNotNull(typeCodeRequired, "typeCodeRequired can not be null");
		return info != null && typeCodeRequired.equalsIgnoreCase(info.getTypeCode());
	}

	protected ModelMonitoredInfo createModelMonitoredInfo()
	{
		final ModelMonitoredInfo info = new ModelMonitoredInfo();
		info.setTypeCode(getTypeCode());

		return info;
	}

	@Override
	public void consume(final ModelMonitoredInfo info)
	{
		if (isAccept(info, getTypeCode()))
		{
			LOG.info(toStringOfModelMonitoredInfo(info));//TODO a1 remove later
			LOG.info("cache key:" + getAitpCacheKeyParser().parser(info));
			consume0(info);
		}
	}

	public abstract void consume0(final ModelMonitoredInfo info);

	protected String toStringOfModelMonitoredInfo(final ModelMonitoredInfo info)
	{
		return info == null ? null
				: String.format("typeCode:%s,pk:%d,code:%s,uid:%s,url:%s", info.getTypeCode(), info.getPk().getLong(), info.getCode(),
				info.getUid(), info.getUrl());
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

	/**
	 * @return the typeCode
	 */
	public String getTypeCode()
	{
		return typeCode;
	}

	/**
	 * @param typeCode
	 *           the typeCode to set
	 */
	@Required
	public void setTypeCode(final String typeCode)
	{
		if (StringUtils.isBlank(typeCode))
		{
			throw new IllegalArgumentException("typeCode can not be blank");
		}

		this.typeCode = typeCode;
	}

	/**
	 * @return the aitpCacheKeyParser
	 */
	public AitpCacheKeyParser getAitpCacheKeyParser()
	{
		return aitpCacheKeyParser;
	}

	/**
	 * @param aitpCacheKeyParser
	 *           the aitpCacheKeyParser to set
	 */
	public void setAitpCacheKeyParser(final AitpCacheKeyParser aitpCacheKeyParser)
	{
		this.aitpCacheKeyParser = aitpCacheKeyParser;
	}

}
