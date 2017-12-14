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

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.converters.SingleValueConverter;


/**
 * Converter which unescapes and abbreviates a given string value.
 */
public class StringValueConverter implements SingleValueConverter
{
	private static final int LIMIT_NO_DEFINED = Integer.MAX_VALUE;

	private int limit = LIMIT_NO_DEFINED;


	public void setLimit(final int limit)
	{
		this.limit = limit;
	}

	@Override
	public boolean canConvert(final Class type)
	{
		return type == String.class;
	}

	@Override
	public String toString(final Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		if (obj instanceof String)
		{
			String stringValue = (String) obj;
			if (limit != LIMIT_NO_DEFINED)
			{
				stringValue = StringUtils.abbreviate(stringValue, limit);
			}
			return stringValue.replaceAll("\\<.*?\\>", "");
		}
		return obj.toString();
	}

	@Override
	public Object fromString(final String str)
	{
		return null;
	}

}
