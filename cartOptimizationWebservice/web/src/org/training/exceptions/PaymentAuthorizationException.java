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
 * Specific exception that is thrown when the payment authorization was not accepted
 * 
 *
 * 
 */
public class PaymentAuthorizationException extends Exception
{


	public PaymentAuthorizationException()
	{
		super("Payment authorication was not successful");
	}

}
