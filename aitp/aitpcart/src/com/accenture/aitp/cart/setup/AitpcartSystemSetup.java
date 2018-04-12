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
package com.accenture.aitp.cart.setup;

import static com.accenture.aitp.cart.constants.AitpcartConstants.PLATFORM_LOGO_CODE;

import de.hybris.platform.core.initialization.SystemSetup;

import java.io.InputStream;

import com.accenture.aitp.cart.constants.AitpcartConstants;
import com.accenture.aitp.cart.service.AitpcartService;


@SystemSetup(extension = AitpcartConstants.EXTENSIONNAME)
public class AitpcartSystemSetup
{
	private final AitpcartService aitpcartService;

	public AitpcartSystemSetup(final AitpcartService aitpcartService)
	{
		this.aitpcartService = aitpcartService;
	}

	@SystemSetup(process = SystemSetup.Process.INIT, type = SystemSetup.Type.ESSENTIAL)
	public void createEssentialData()
	{
		aitpcartService.createLogo(PLATFORM_LOGO_CODE);
	}

	private InputStream getImageStream()
	{
		return AitpcartSystemSetup.class.getResourceAsStream("/aitpcart/sap-hybris-platform.png");
	}
}
