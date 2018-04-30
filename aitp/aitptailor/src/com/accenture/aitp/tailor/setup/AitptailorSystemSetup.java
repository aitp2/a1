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
package com.accenture.aitp.tailor.setup;

import static com.accenture.aitp.tailor.constants.AitptailorConstants.PLATFORM_LOGO_CODE;

import de.hybris.platform.core.initialization.SystemSetup;

import java.io.InputStream;

import com.accenture.aitp.tailor.constants.AitptailorConstants;
import com.accenture.aitp.tailor.service.AitptailorService;


@SystemSetup(extension = AitptailorConstants.EXTENSIONNAME)
public class AitptailorSystemSetup
{
	private final AitptailorService aitptailorService;

	public AitptailorSystemSetup(final AitptailorService aitptailorService)
	{
		this.aitptailorService = aitptailorService;
	}

	@SystemSetup(process = SystemSetup.Process.INIT, type = SystemSetup.Type.ESSENTIAL)
	public void createEssentialData()
	{
		aitptailorService.createLogo(PLATFORM_LOGO_CODE);
	}

	private InputStream getImageStream()
	{
		return AitptailorSystemSetup.class.getResourceAsStream("/aitptailor/sap-hybris-platform.png");
	}

}
