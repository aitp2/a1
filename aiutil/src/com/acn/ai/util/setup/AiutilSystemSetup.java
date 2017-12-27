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
package com.acn.ai.util.setup;

import static com.acn.ai.util.constants.AiutilConstants.PLATFORM_LOGO_CODE;

import de.hybris.platform.core.initialization.SystemSetup;

import java.io.InputStream;

import com.acn.ai.util.constants.AiutilConstants;
import com.acn.ai.util.service.AiutilService;


@SystemSetup(extension = AiutilConstants.EXTENSIONNAME)
public class AiutilSystemSetup
{
	private final AiutilService aiutilService;

	public AiutilSystemSetup(final AiutilService aiutilService)
	{
		this.aiutilService = aiutilService;
	}

	@SystemSetup(process = SystemSetup.Process.INIT, type = SystemSetup.Type.ESSENTIAL)
	public void createEssentialData()
	{
		aiutilService.createLogo(PLATFORM_LOGO_CODE);
	}

	private InputStream getImageStream()
	{
		return AiutilSystemSetup.class.getResourceAsStream("/aiutil/sap-hybris-platform.png");
	}
}
