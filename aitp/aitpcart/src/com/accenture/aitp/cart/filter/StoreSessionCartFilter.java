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
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.Config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.filter.GenericFilterBean;

import com.accenture.aitp.cart.strategy.CartKeyGenerateStrategy;


/**
 *
 */
public class StoreSessionCartFilter extends GenericFilterBean
{
	private boolean touchSessionCart;
	private RedisTemplate redisTemplate;
	private CartKeyGenerateStrategy cartKeyGenerateStrategy;
	private CartService cartService;
	private ModelService modelService;

	@Override
	public void doFilter(final ServletRequest httpRequest, final ServletResponse httpResponse, final FilterChain filterChain)
			throws IOException, ServletException
	{
		try
		{
			filterChain.doFilter(httpRequest, httpResponse);
		}
		finally
		{
			if (touchSessionCart && cartService.hasSessionCart())
			{
				final CartModel cart = cartService.getSessionCart();
				if (null != cart)
				{
					redisTemplate.opsForValue().set(cartKeyGenerateStrategy.generateCartKey(cart), modelService.getSource(cart));
				}
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




}
