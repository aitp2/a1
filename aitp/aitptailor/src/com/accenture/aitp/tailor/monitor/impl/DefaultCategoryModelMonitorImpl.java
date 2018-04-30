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
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.ItemModel;

import com.accenture.aitp.tailor.data.ModelMonitoredInfo;
import com.accenture.aitp.tailor.strategy.AitpModelMonitorQueueStrateg;

/**
 *
 */
public class DefaultCategoryModelMonitorImpl extends DefaultAitpAbstractModelMonitorImpl
{
	private AitpModelMonitorQueueStrateg aitpModelMonitorQueueStrateg;

	@Override
	public boolean accept(final ItemModel object)
	{
		if (object instanceof ClassificationClassModel)
		{
			return false;
		}
		else if (object instanceof CategoryModel)
		{
			return isOnlineVersion(((CategoryModel) object).getCatalogVersion());
		}
		else
		{
			return false;
		}

	}


	@Override
	public void put(final ItemModel object)
	{
		final CategoryModel model = (CategoryModel) object;
		final ModelMonitoredInfo info = new ModelMonitoredInfo();
		info.setPk(model.getPk());
		info.setTypeCode(CategoryModel._TYPECODE);
		info.setCode(model.getCode());
		getAitpModelMonitorQueueStrateg().put(info);

	}

	@Override
	public Object take()
	{
		return getAitpModelMonitorQueueStrateg().take();
	}

	/**
	 * @return the aitpModelMonitorQueueStrateg
	 */
	@Override
	public AitpModelMonitorQueueStrateg getAitpModelMonitorQueueStrateg()
	{
		return aitpModelMonitorQueueStrateg;
	}

	/**
	 * @param aitpModelMonitorQueueStrateg
	 *           the aitpModelMonitorQueueStrateg to set
	 */
	@Override
	public void setAitpModelMonitorQueueStrateg(final AitpModelMonitorQueueStrateg aitpModelMonitorQueueStrateg)
	{
		this.aitpModelMonitorQueueStrateg = aitpModelMonitorQueueStrateg;
	}

}
