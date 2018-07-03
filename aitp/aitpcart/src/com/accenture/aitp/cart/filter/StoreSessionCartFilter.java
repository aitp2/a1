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
package com.accenture.aitp.cart.filter;

import de.hybris.platform.order.CartService;
import de.hybris.platform.util.Config;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.GenericFilterBean;

import com.accenture.aitp.cart.strategy.CartSerializerStrategy;

import reactor.util.CollectionUtils;


/**
 * filter serializer session cart by the cartSerializerStrategy 1. before the next fiter, init session cart from
 * serializer cart via cartSerializerStrategy 2. do filer chain 3. serializer session cart via cartSerializerStrategy
 *
 * @author mingming.wang
 */
public class StoreSessionCartFilter extends GenericFilterBean
{
	private static boolean touchSessionCart;
	private CartService cartService;
	private CartSerializerStrategy cartSerializerStrategy;

	private List<String> cartOperationUrls;
	private PathMatcher pathMatcher;

	@Override
	public void doFilter(final ServletRequest httpRequest, final ServletResponse httpResponse, final FilterChain filterChain)
			throws IOException, ServletException
	{
		if (touchSessionCart && !getCartService().hasSessionCart())
		{
			getCartSerializerStrategy().initSessionCart((HttpServletRequest) httpRequest);
		}
		try
		{
			filterChain.doFilter(httpRequest, httpResponse);
		}
		finally
		{
			if (touchSessionCart && getCartService().hasSessionCart() && isCartOperationUrl((HttpServletRequest) httpRequest))
			{
				getCartSerializerStrategy().serializerCart((HttpServletRequest) httpRequest, getCartService().getSessionCart());
			}
		}
	}

	protected boolean isCartOperationUrl(final HttpServletRequest request)
	{
		if (CollectionUtils.isEmpty(cartOperationUrls))
		{
			return true;
		}
		final String servletPath = request.getServletPath();

		for (final String input : cartOperationUrls)
		{
			if (pathMatcher.match(input, servletPath))
			{
				return true;
			}
		}
		return false;
	}


	/**
	 * @return the cartSerializerStrategy
	 */
	public CartSerializerStrategy getCartSerializerStrategy()
	{
		return cartSerializerStrategy;
	}

	/**
	 * @param cartSerializerStrategy
	 *           the cartSerializerStrategy to set
	 */
	public void setCartSerializerStrategy(final CartSerializerStrategy cartSerializerStrategy)
	{
		this.cartSerializerStrategy = cartSerializerStrategy;
	}

	/**
	 * @param cartOperationUrls
	 *           the cartOperationUrls to set
	 */
	public void setCartOperationUrls(final List<String> cartOperationUrls)
	{
		this.cartOperationUrls = cartOperationUrls;
	}

	@Override
	public void afterPropertiesSet() throws ServletException
	{
		super.afterPropertiesSet();
		StoreSessionCartFilter.touchSessionCart = Config.getBoolean("redis.session.cart.support", false);

	}



	/**
	 * @param cartService
	 *           the cartService to set
	 */
	public void setCartService(final CartService cartService)
	{
		this.cartService = cartService;
	}



	@Required
	public void setPathMatcher(final PathMatcher pathMatcher)
	{
		this.pathMatcher = pathMatcher;
	}

	/**
	 * @return the cartService
	 */
	public CartService getCartService()
	{
		return cartService;
	}


}
