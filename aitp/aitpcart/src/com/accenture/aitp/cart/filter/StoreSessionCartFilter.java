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

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.jalo.order.Cart;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.util.Config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;
import org.drools.core.util.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.filter.GenericFilterBean;

import com.accenture.aitp.cart.strategy.CartKeyGenerateStrategy;


/**
 *
 */
public class StoreSessionCartFilter extends GenericFilterBean
{
	private static final Logger LOGGER = Logger.getLogger(StoreSessionCartFilter.class);
	private static final String SESSION_CACHE_CART_KEY = "sessionCartCacheKey";
	private boolean touchSessionCart;
	private RedisTemplate redisTemplate;
	private CartKeyGenerateStrategy cartKeyGenerateStrategy;

	private CartService cartService;
	private ModelService modelService;
	private SessionService sessionService;

	@Override
	public void doFilter(final ServletRequest httpRequest, final ServletResponse httpResponse, final FilterChain filterChain)
			throws IOException, ServletException
	{
		if (touchSessionCart && !cartService.hasSessionCart())
		{
			initSessionCartFromCache();
		}
		try
		{
			filterChain.doFilter(httpRequest, httpResponse);
		}
		finally
		{
			if (touchSessionCart)
			{
				setSessionCartToCache();
			}
		}
	}

	protected void setSessionCartToCache()
	{
		if (cartService.hasSessionCart())
		{
			final CartModel cart = cartService.getSessionCart();
			if (null != cart)
			{
				final String cartKey = cartKeyGenerateStrategy.generateCartKey(cart);
				final long before = System.currentTimeMillis();
				redisTemplate.opsForValue().set(cartKey, modelService.getSource(cart));
				LOGGER.info("set session cart:" + cartKey + " use time:" + (System.currentTimeMillis() - before) + "ms");
				sessionService.setAttribute(SESSION_CACHE_CART_KEY, cartKey);
			}
		}
	}


	protected void initSessionCartFromCache()
	{
		final String cartKey = sessionService.getAttribute(SESSION_CACHE_CART_KEY);
		if (!StringUtils.isEmpty(cartKey))
		{
			final long before = System.currentTimeMillis();
			final Cart cart = (Cart) redisTemplate.opsForValue().get(cartKey);
			LOGGER.info("get session cart:" + cartKey + " use time:" + (System.currentTimeMillis() - before) + "ms");
			if (null != cart)
			{
				cartService.setSessionCart(modelService.get(cart));
			}
		}
	}


	@Override
	public void afterPropertiesSet() throws ServletException
	{
		super.afterPropertiesSet();
		this.touchSessionCart = Config.getBoolean("redis.session.cart.support", false);

	}


	/**
	 * @param redisTemplate
	 *           the redisTemplate to set
	 */
	public void setRedisTemplate(final RedisTemplate redisTemplate)
	{
		this.redisTemplate = redisTemplate;
	}


	/**
	 * @param cartKeyGenerateStrategy
	 *           the cartKeyGenerateStrategy to set
	 */
	public void setCartKeyGenerateStrategy(final CartKeyGenerateStrategy cartKeyGenerateStrategy)
	{
		this.cartKeyGenerateStrategy = cartKeyGenerateStrategy;
	}


	/**
	 * @param cartService
	 *           the cartService to set
	 */
	public void setCartService(final CartService cartService)
	{
		this.cartService = cartService;
	}


	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}


	/**
	 * @param sessionService
	 *           the sessionService to set
	 */
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}


}
