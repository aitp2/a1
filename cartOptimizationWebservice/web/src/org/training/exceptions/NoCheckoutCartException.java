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

/**
 * Thrown when an operation is performed that requires a session cart, which was not yet created.
 */
public class NoCheckoutCartException extends Exception
{

	/**
	 * @param message
	 */
	public NoCheckoutCartException(final String message)
	{
		super(message);
	}

}
