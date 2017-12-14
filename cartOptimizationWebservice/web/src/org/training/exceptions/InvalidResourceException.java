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
 *
 * 
 */
public class InvalidResourceException extends ServletException
{

	private final String baseSiteId;

	/**
	 * @param baseSiteUid
	 */
	public InvalidResourceException(final String baseSiteUid)
	{
		super("Base site " + baseSiteUid + " doesn't exist");
		this.baseSiteId = baseSiteUid;
	}

	/**
	 * @return the baseSiteId
	 */
	public String getBaseSiteId()
	{
		return baseSiteId;
	}
}
