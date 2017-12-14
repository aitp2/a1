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

import de.hybris.platform.core.model.c2l.CurrencyModel;

import javax.servlet.ServletException;


/**
 *
 *
 */
public class UnsupportedCurrencyException extends ServletException
{

	private final CurrencyModel currency;

	/**
	 * @param currencyToSet
	 */
	public UnsupportedCurrencyException(final CurrencyModel currencyToSet)
	{
		super("Currency " + currencyToSet + " is not supported by the current base store");
		this.currency = currencyToSet;
	}

	public UnsupportedCurrencyException(final CurrencyModel currencyToSet, final Throwable rootCouse)
	{
		super("Currency " + currencyToSet + " is not supported by the current base store", rootCouse);
		this.currency = currencyToSet;
	}

	/**
	 * @param msg
	 */
	public UnsupportedCurrencyException(final String msg)
	{
		super(msg);
		currency = null;
	}

	public UnsupportedCurrencyException(final String msg, final Throwable rootCouse)
	{
		super(msg, rootCouse);
		currency = null;
	}

	/**
	 * @return the currency
	 */
	public CurrencyModel getCurrency()
	{
		return currency;
	}
}
