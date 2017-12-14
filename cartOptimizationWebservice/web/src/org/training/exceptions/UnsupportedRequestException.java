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
package org.training.exceptions;

import javax.servlet.ServletException;


/**
 * Thrown when request is not supported for current configuration.
 */
public class UnsupportedRequestException extends ServletException
{
	public UnsupportedRequestException(final String message)
	{
		super(message);
	}
}
