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
package org.training.strategies.impl;


import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import org.training.strategies.OrderCodeIdentificationStrategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link org.training.strategies.OrderCodeIdentificationStrategy}.
 */
public class DefaultOrderCodeIdentificationStrategy implements OrderCodeIdentificationStrategy
{
	private String idPattern;

	/**
	 * Checks if given string is GUID
	 *
	 * @param potentialId
	 *           - string to check
	 * @return result
	 */
	@Override
	public boolean isID(final String potentialId)
	{
		validateParameterNotNull(potentialId, "identifier must not be null");
		if (potentialId == null || potentialId.isEmpty())
		{
			return false;
		}

		final Pattern pattern = Pattern.compile(this.idPattern);
		final Matcher matcher = pattern.matcher(potentialId);
		return matcher.find();
	}

	@Required
	public void setIdPattern(final String idPattern)
	{
		this.idPattern = idPattern;
	}
}
