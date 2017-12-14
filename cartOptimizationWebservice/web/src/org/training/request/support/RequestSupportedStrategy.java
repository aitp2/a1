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
package org.training.request.support;

import org.training.exceptions.UnsupportedRequestException;


/**
 * Interface for checking if request is supported in current configuration (e.g. for current base store, for payment
 * provider)
 * 
 */
public interface RequestSupportedStrategy
{
	/**
	 * Method check if request is supported
	 * 
	 * @param requestId
	 *           request identifier (e.g. request path)
	 * @return true if request is supported<br/>
	 *         false if request is not supported
	 * 
	 */
	boolean isRequestSupported(String requestId);

	/**
	 * Method check if request is supported and throws exception if not
	 * 
	 * @param requestId
	 *           request identifier (e.g. request path)
	 * @throws UnsupportedRequestException
	 *            when request is not supported
	 */
	void checkIfRequestSupported(String requestId) throws UnsupportedRequestException;
}
