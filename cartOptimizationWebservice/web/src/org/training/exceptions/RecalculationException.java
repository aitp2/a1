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

import de.hybris.platform.order.exceptions.CalculationException;

import javax.servlet.ServletException;


/**
 * Thrown when recalculation of cart is impossible due to any reasons.
 */
public class RecalculationException extends ServletException
{
	public RecalculationException(final CalculationException exception, final String currencyIso)
	{
		super("Cannot recalculate cart for currency: " + currencyIso, exception);
	}
}
