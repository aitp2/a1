/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2018 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package com.accenture.aitp.cart.jalo;

import de.hybris.platform.commerceservices.jalo.CommerceJaloSession;


/**
 *
 */
public class AitpCommerceJaloSession extends CommerceJaloSession
{

	@Override
	public Object setAttribute(final String name, final Object value)
	{
		return this.getSessionContext().setAttribute(name, value);
	}

	@Override
	public Object getAttribute(final String name)
	{
		return this.getSessionContext().getAttribute(name);
	}

	@Override
	public Object removeAttribute(final String name)
	{
		return this.getSessionContext().removeAttribute(name);
	}













}
