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
 * Specific exception that is thrown when the given payment info could not be associated with the checkout cart.
 * 
 *
 * 
 */
public class InvalidPaymentInfoException extends Exception
{

	private final String paymentInfoId;

	/**
	 * @param id
	 */
	public InvalidPaymentInfoException(final String id)
	{
		super("PaymentInfo [" + id + "] is invalid for the current cart");
		this.paymentInfoId = id;
	}

	/**
	 * @return the paymentInfoId
	 */
	public String getPaymentInfoId()
	{
		return paymentInfoId;
	}

}
