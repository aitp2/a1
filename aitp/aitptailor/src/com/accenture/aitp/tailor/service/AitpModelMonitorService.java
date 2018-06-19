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
package com.accenture.aitp.tailor.service;

import com.accenture.aitp.tailor.data.ModelMonitoredInfo;

import de.hybris.platform.core.model.ItemModel;

/**
 *
 */
public interface AitpModelMonitorService
{
	 void publish(final ItemModel object);

	 ModelMonitoredInfo consume();
	 
	 void invalidateUrls(ModelMonitoredInfo context);
}
