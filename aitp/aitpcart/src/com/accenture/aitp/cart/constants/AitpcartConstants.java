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
package com.accenture.aitp.cart.constants;

/**
 * Global class for all Aitpcart constants. You can add global constants for your extension into this class.
 */
public final class AitpcartConstants extends GeneratedAitpcartConstants
{
	public static final String EXTENSIONNAME = "aitpcart";

	private AitpcartConstants()
	{
		//empty to avoid instantiating this constant class
	}

	// implement here constants used by this extension

    public static final String PLATFORM_LOGO_CODE = "aitpcartPlatformLogo";
    
    public static final String AITP_CART_SWTICH_ON = "true";
    
    public static final String AITP_CART_SWTICH_OFF = "false";
    
    public static final String AITP_CART_TAX_SWTICH = "aitp.cart.tax.swtich";
    
    public static final String AITP_CART_PAYMENTCOST_SWTICH = "aitp.cart.paymentcost.swtich";
}
