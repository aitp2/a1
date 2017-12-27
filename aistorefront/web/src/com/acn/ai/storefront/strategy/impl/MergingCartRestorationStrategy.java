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
package com.acn.ai.storefront.strategy.impl;

import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.strategy.impl.DefaultCartRestorationStrategy;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.acn.ai.core.outbound.cart.AiLoginSuccess;


/**
 * Strategy for cart restoration and merging.
 */
public class MergingCartRestorationStrategy extends DefaultCartRestorationStrategy
{
	private static final Logger LOG = Logger.getLogger(MergingCartRestorationStrategy.class);

	private AiLoginSuccess aiLoginSuccess;
	
	@Override
	public void restoreCart(final HttpServletRequest request)
	{
		// no need to merge if current cart has no entry
		if (!getCartFacade().hasEntries())
		{
			super.restoreCart(request);
		}
		else
		{
			final String sessionCartGuid = getCartFacade().getSessionCartGuid();
			final String mostRecentSavedCartGuid = getMostRecentSavedCartGuid(sessionCartGuid);
			if (StringUtils.isNotEmpty(mostRecentSavedCartGuid))
			{
				getSessionService().setAttribute(WebConstants.CART_RESTORATION_SHOW_MESSAGE, Boolean.TRUE);
				try
				{
//					getSessionService().setAttribute(WebConstants.CART_RESTORATION,
//							getCartFacade().restoreCartAndMerge(mostRecentSavedCartGuid, sessionCartGuid));
//					request.setAttribute(WebConstants.CART_MERGED, Boolean.TRUE);
					aiLoginSuccess.restoreCartAndMerge(mostRecentSavedCartGuid, sessionCartGuid);
				}
				catch (final Exception e)
				{
					if (LOG.isDebugEnabled())
					{
						LOG.debug(e);
					}
					getSessionService().setAttribute(WebConstants.CART_RESTORATION_ERROR_STATUS,
							WebConstants.CART_RESTORATION_ERROR_STATUS);
				}
			}
		}
	}

	public AiLoginSuccess getAiLoginSuccess() {
		return aiLoginSuccess;
	}

	public void setAiLoginSuccess(AiLoginSuccess aiLoginSuccess) {
		this.aiLoginSuccess = aiLoginSuccess;
	}

	/**
	 * Determine the most recent saved cart of a user for the site that is not the current session cart. The current
	 * session cart is already owned by the user and for the merging functionality to work correctly the most recently
	 * saved cart must be determined. getMostRecentCartGuidForUser(excludedCartsGuid) returns the cart guid which is
	 * ordered by modified time and is not the session cart.
	 *
	 * @param currentCartGuid
	 * @return most recently saved cart guid
	 */
	protected String getMostRecentSavedCartGuid(final String currentCartGuid)
	{
		return getCartFacade().getMostRecentCartGuidForUser(Arrays.asList(currentCartGuid));
	}
}
