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
package com.accenture.performance.optimization.setup;

import static com.accenture.performance.optimization.constants.CartOptimizationConstants.PLATFORM_LOGO_CODE;

import de.hybris.platform.core.initialization.SystemSetup;

import java.io.InputStream;

import com.accenture.performance.optimization.constants.CartOptimizationConstants;
import com.accenture.performance.optimization.service.CartOptimizationService;


@SystemSetup(extension = CartOptimizationConstants.EXTENSIONNAME)
public class CartOptimizationSystemSetup
{
	private final CartOptimizationService cartOptimizationService;

	public CartOptimizationSystemSetup(final CartOptimizationService cartOptimizationService)
	{
		this.cartOptimizationService = cartOptimizationService;
	}

	@SystemSetup(process = SystemSetup.Process.INIT, type = SystemSetup.Type.ESSENTIAL)
	public void createEssentialData()
	{
		cartOptimizationService.createLogo(PLATFORM_LOGO_CODE);
	}

	private InputStream getImageStream()
	{
		return CartOptimizationSystemSetup.class.getResourceAsStream("/cartOptimization/sap-hybris-platform.png");
	}
}
