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
package com.accenture.aitp.tailor.interceptor;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;

import com.accenture.aitp.tailor.service.AitpModelMonitorService;


/**
 *
 */
public class DefaultAitpRemoveInterceptor implements RemoveInterceptor<Object>
{
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
	public void onRemove(final Object var1, final InterceptorContext var2) throws InterceptorException
	{
		// TODO a1 later

	}

}
