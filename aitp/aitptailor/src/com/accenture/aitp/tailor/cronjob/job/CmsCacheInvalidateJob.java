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
package com.accenture.aitp.tailor.cronjob.job;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

import org.apache.log4j.Logger;

import com.accenture.aitp.tailor.data.ModelMonitoredInfo;
import com.accenture.aitp.tailor.service.AitpModelMonitorService;

/**
 *
 */
public class CmsCacheInvalidateJob extends AbstractJobPerformable<CronJobModel>
{
	private final static Logger LOG = Logger.getLogger(CmsCacheInvalidateJob.class);
	private AitpModelMonitorService aitpModelMonitorService;

	/**
	 * @return the aitpModelMonitorService
	 */
	public AitpModelMonitorService getAitpModelMonitorService()
	{
		return aitpModelMonitorService;
	}

	/**
	 * @param aitpModelMonitorService
	 *           the aitpModelMonitorService to set
	 */
	public void setAitpModelMonitorService(final AitpModelMonitorService aitpModelMonitorService)
	{
		this.aitpModelMonitorService = aitpModelMonitorService;
	}

	@Override
	public PerformResult perform(final CronJobModel crobJob)
	{
		LOG.info("start invalidate url checking");
		
		ModelMonitoredInfo info = getAitpModelMonitorService().consume();
		while(info != null) {
			LOG.info("=====> invalidate url checking for : " + info);
			getAitpModelMonitorService().invalidateUrls(info);
			info = getAitpModelMonitorService().consume();
		}
		LOG.info("end invalidate url checking");
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

}
