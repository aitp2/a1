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
package com.acn.ai.storefront.security.impl;

import de.hybris.platform.acceleratorstorefrontcommons.security.GUIDCookieStrategy;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;

import com.accenture.performance.optimization.facades.OptimizedCartFacade;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.acn.ai.core.oauth.AiTokenService;
import com.acn.ai.storefront.interceptors.beforecontroller.RequireHardLoginBeforeControllerHandler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;
import org.springframework.web.util.CookieGenerator;


/**
 * Default implementation of {@link GUIDCookieStrategy}
 */
public class DefaultGUIDCookieStrategy implements GUIDCookieStrategy
{
	private static final Logger LOG = Logger.getLogger(DefaultGUIDCookieStrategy.class);

	@Resource(name="aiTokenService")
	private AiTokenService tokenService;
	
	@Resource(name = "userService")
	private UserService userService;
	
	@Resource(name = "cartFacade")
	private OptimizedCartFacade cartFacade;
	
	@Resource(name = "baseSiteService")
	private BaseSiteService baseSiteService;
	
	private final SecureRandom random;
	private final MessageDigest sha;

	private CookieGenerator cookieGenerator;

	public DefaultGUIDCookieStrategy() throws NoSuchAlgorithmException
	{
		random = SecureRandom.getInstance("SHA1PRNG");
		sha = MessageDigest.getInstance("SHA-1");
		Assert.notNull(random);
		Assert.notNull(sha);
	}

	@Override
	public void setCookie(final HttpServletRequest request, final HttpServletResponse response)
	{
		//// added by wei.f.zhang
		final int sessionMaxInactiveInterval = request.getSession().getMaxInactiveInterval();
		
		final String token = tokenService.getAccessToken();
		
		final UserModel userModel = userService.getCurrentUser();
		final OptimizedCartData cart = cartFacade.getSessionCartData();
		final String cartID = userService.isAnonymousUser(userModel) ? cart.getGuid() : cart.getCode();
		
		final String siteID = baseSiteService.getCurrentBaseSite().getUid();

		Cookie occToken = new Cookie("ai-occ-token", token);
		occToken.setPath("/");
		occToken.setMaxAge(sessionMaxInactiveInterval);
		response.addCookie(occToken);
		
		Cookie cartIDCookie = new Cookie("cartUid", cartID);
		cartIDCookie.setPath("/");
		cartIDCookie.setMaxAge(sessionMaxInactiveInterval);
		response.addCookie(cartIDCookie);
		
		Cookie siteIDCookie = new Cookie("siteId",siteID);
		siteIDCookie.setPath("/");
		siteIDCookie.setMaxAge(sessionMaxInactiveInterval);
		response.addCookie(siteIDCookie);
		////////////////////////////////////
		
		if (!request.isSecure())
		{
			// We must not generate the cookie for insecure requests, otherwise there is not point doing this at all
			throw new IllegalStateException("Cannot set GUIDCookie on an insecure request!");
		}

		final String guid = createGUID();
		
		getCookieGenerator().addCookie(response, guid);
		request.getSession().setAttribute(RequireHardLoginBeforeControllerHandler.SECURE_GUID_SESSION_KEY, guid);

		if (LOG.isInfoEnabled())
		{
			LOG.info("Setting guid cookie and session attribute: " + guid);
		}
	}

	@Override
	public void deleteCookie(final HttpServletRequest request, final HttpServletResponse response)
	{
		if (!request.isSecure())
		{
			LOG.error("Cannot remove secure GUIDCookie during an insecure request. I should have been called from a secure page.");
		}
		else
		{
			// Its a secure page, we can delete the cookie
			getCookieGenerator().removeCookie(response);
		}
	}

	protected String createGUID()
	{
		final String randomNum = String.valueOf(getRandom().nextInt());
		final byte[] result = getSha().digest(randomNum.getBytes());
		return String.valueOf(Hex.encodeHex(result));
	}

	protected CookieGenerator getCookieGenerator()
	{
		return cookieGenerator;
	}

	/**
	 * @param cookieGenerator
	 *           the cookieGenerator to set
	 */
	@Required
	public void setCookieGenerator(final CookieGenerator cookieGenerator)
	{
		this.cookieGenerator = cookieGenerator;
	}


	protected SecureRandom getRandom()
	{
		return random;
	}

	protected MessageDigest getSha()
	{
		return sha;
	}
}
