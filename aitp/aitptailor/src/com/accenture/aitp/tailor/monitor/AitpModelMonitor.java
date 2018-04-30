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
package com.accenture.aitp.tailor.monitor;

import de.hybris.platform.core.model.ItemModel;

/**
 *
 */
public interface AitpModelMonitor
{
	public final static String ONLINE = "Online";

	/**
	 * note: care about the item under catalog version(online accept,or not)
	 */
	public boolean accept(final ItemModel object);

	public void put(final ItemModel object);

	public Object take();
}
