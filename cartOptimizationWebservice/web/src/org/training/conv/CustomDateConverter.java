/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package org.training.conv;

import org.training.formatters.WsDateFormatter;

import java.util.Date;

import com.thoughtworks.xstream.converters.SingleValueConverter;


/**
 * Converter for a specific date format.
 */
public class CustomDateConverter implements SingleValueConverter
{
	private WsDateFormatter wsDateFormatter;

	public void setWsDateFormatter(final WsDateFormatter wsDateFormatter)
	{
		this.wsDateFormatter = wsDateFormatter;
	}

	@Override
	public boolean canConvert(final Class type)
	{
		return type == Date.class;
	}

	@Override
	public String toString(final Object obj)
	{
		return wsDateFormatter.toString((Date) obj);

	}

	@Override
	public Object fromString(final String str)
	{
		return wsDateFormatter.toDate(str);
	}
}
