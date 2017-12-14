/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2017 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package com.accenture.performance.optimization.ruleengineservices.service;

/**
 *
 */
public class PromtionOrderNotFoundExcetpion extends RuntimeException
{
	private String ordercode;

	public PromtionOrderNotFoundExcetpion(final String message, final String ordercode)
	{
		super(message);
		this.ordercode = ordercode;
	}

	/**
	 * @return the ordercode
	 */
	public String getOrdercode()
	{
		return ordercode;
	}

	/**
	 * @param ordercode
	 *           the ordercode to set
	 */
	public void setOrdercode(final String ordercode)
	{
		this.ordercode = ordercode;
	}
}
