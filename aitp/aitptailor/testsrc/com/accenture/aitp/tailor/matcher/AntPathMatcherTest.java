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
package com.accenture.aitp.tailor.matcher;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.AntPathMatcher;


/**
 *
 */
public class AntPathMatcherTest
{

	private AntPathMatcher antPathMatcher;

	@Before
	public void setup()
	{
		this.antPathMatcher = new AntPathMatcher();
	}

	@Test
	public void test()
	{
		Assert.assertTrue(this.antPathMatcher.match("/**/account/**", "/account/address"));
	}

	@Test
	public void test2()
	{
		Assert.assertFalse(this.antPathMatcher.match("**/account/**", "/account/address"));
	}

}
