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
 * Specific exception that is thrown when delivery address is not supported for the current session cart
 * 
 *
 * 
 */
public class UnsupportedDeliveryAddressException extends Exception
{

	private final String addressId;

	/**
	 * @param id
	 */
	public UnsupportedDeliveryAddressException(final String id)
	{
		super("Address [" + id + "] is not supported for the current cart");
		this.addressId = id;
	}

	/**
	 * @return the addressId
	 */
	public String getAddressId()
	{
		return addressId;
	}
}
