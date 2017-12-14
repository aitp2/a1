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

import java.util.List;


/**
 *
 */
public class BeanD extends BeanB
{
	private List<BeanA> prop1BeanD;

	private Integer prop2BeanD;

	/**
	 * @return the prop1BeanD
	 */
	public List<BeanA> getProp1BeanD()
	{
		return prop1BeanD;
	}

	/**
	 * @param prop1BeanD
	 *           the prop1BeanD to set
	 */
	public void setProp1BeanD(final List<BeanA> prop1BeanD)
	{
		this.prop1BeanD = prop1BeanD;
	}

	/**
	 * @return the prop2BeanD
	 */
	public Integer getProp2BeanD()
	{
		return prop2BeanD;
	}

	/**
	 * @param prop2BeanD
	 *           the prop2BeanD to set
	 */
	public void setProp2BeanD(final Integer prop2BeanD)
	{
		this.prop2BeanD = prop2BeanD;
	}

}
