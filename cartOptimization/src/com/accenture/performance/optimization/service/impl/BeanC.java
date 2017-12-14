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
package com.accenture.performance.optimization.service.impl;

/**
 *
 */
public class BeanC extends BeanB
{
	private BeanA prop1BeanC;

	private int prop2BeanC;

	/**
	 * @return the prop1BeanC
	 */
	public BeanA getProp1BeanC()
	{
		return prop1BeanC;
	}

	/**
	 * @param prop1BeanC
	 *           the prop1BeanC to set
	 */
	public void setProp1BeanC(final BeanA prop1BeanC)
	{
		this.prop1BeanC = prop1BeanC;
	}

	/**
	 * @return the prop2BeanC
	 */
	public int getProp2BeanC()
	{
		return prop2BeanC;
	}

	/**
	 * @param prop2BeanC the prop2BeanC to set
	 */
	public void setProp2BeanC(int prop2BeanC)
	{
		this.prop2BeanC = prop2BeanC;
	}


}
