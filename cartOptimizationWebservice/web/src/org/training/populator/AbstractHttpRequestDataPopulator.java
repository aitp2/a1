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
package org.training.populator;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;


/**
 *
 * 
 */
public abstract class AbstractHttpRequestDataPopulator
{

	protected String updateStringValueFromRequest(final HttpServletRequest request, final String paramName,
			final String defaultValue)
	{
		final String requestParameterValue = getRequestParameterValue(request, paramName);
		if ("".equals(requestParameterValue))
		{
			return null;
		}
		return StringUtils.defaultIfBlank(requestParameterValue, defaultValue);
	}

	protected boolean updateBooleanValueFromRequest(final HttpServletRequest request, final String paramName,
			final boolean defaultValue)
	{
		final String booleanString = updateStringValueFromRequest(request, paramName, null);
		if (booleanString == null)
		{
			return defaultValue;
		}
		return Boolean.parseBoolean(booleanString);
	}

	protected Double updateDoubleValueFromRequest(final HttpServletRequest request, final String paramName,
			final Double defaultValue)
	{
		final String booleanString = updateStringValueFromRequest(request, paramName, null);
		if (booleanString == null)
		{
			return defaultValue;
		}
		return Double.valueOf(booleanString);
	}

	protected String getRequestParameterValue(final HttpServletRequest request, final String paramName)
	{
		return request.getParameter(paramName);
	}
}
