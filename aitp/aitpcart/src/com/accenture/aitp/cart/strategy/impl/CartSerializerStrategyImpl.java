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
package com.accenture.aitp.cart.strategy.impl;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.order.Cart;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.impl.DefaultCartService;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.drools.core.util.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import com.accenture.aitp.cart.strategy.CartKeyGenerateStrategy;
import com.accenture.aitp.cart.strategy.CartSerializerStrategy;


/**
 *
 */
public class CartSerializerStrategyImpl implements CartSerializerStrategy
{
	private static final Logger LOGGER = Logger.getLogger(CartSerializerStrategyImpl.class);
	private static final String SESSION_CACHE_CART_KEY = "sessionCartCacheKey";

	//private static final int DEFAULT_CART_MAX_AGE = 2419200;
	//private static final int DEFAULT_ANONYMOUS_CART_MAX_AGE = 1209600;
	private RedisTemplate redisTemplate;
	private CartKeyGenerateStrategy cartKeyGenerateStrategy;
	private CartService cartService;
	private ModelService modelService;


	@Override
	public void serializerCart(final JaloSession jaloSession)
	{
		final Object object = jaloSession.getAttribute(DefaultCartService.SESSION_CART_PARAMETER_NAME);
		if (null != object)
		{
			final Cart cart = (Cart) object;
			getRedisTemplate().opsForValue().set(getCartKeyGenerateStrategy().generateCartKey(cart), cart);
		}
	}

	@Override
	public void initSessionCart(final HttpServletRequest httpRequest)
	{
		final String cartKey = (String) httpRequest.getSession().getAttribute(SESSION_CACHE_CART_KEY);
		if (!StringUtils.isEmpty(cartKey))
		{
			final long before = System.currentTimeMillis();
			final Cart cart = (Cart) getRedisTemplate().opsForValue().get(cartKey);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.info("get session cart:" + cartKey + " use time:" + (System.currentTimeMillis() - before) + "ms");
			}
			if (null != cart)
			{
				cartService.setSessionCart(modelService.get(cart));
			}
			else
			{
				httpRequest.getSession().removeAttribute(SESSION_CACHE_CART_KEY);
			}
		}
	}


	@Override
	public void serializerCart(final HttpServletRequest httpRequest, final CartModel cartModel)
	{
		if (null != cartModel)
		{
			final String cartKey = cartKeyGenerateStrategy.generateCartKey(cartModel);
			final long before = System.currentTimeMillis();
			getRedisTemplate().opsForValue().set(cartKey, modelService.getSource(cartModel));
			httpRequest.getSession().setAttribute(SESSION_CACHE_CART_KEY, cartKey);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.info("set session cart:" + cartKey + " use time:" + (System.currentTimeMillis() - before) + "ms");
			}
		}
	}

	@Override
	public void removeSerializerCart(final CartModel cart)
	{
		if (null != cart)
		{
			final String cartKey = cartKeyGenerateStrategy.generateCartKey(cart);
			getRedisTemplate().delete(cartKey);
		}

	}

	@Override
	public CartModel queryCartByUser(final UserModel user)
	{
		final Cart cart = (Cart) getRedisTemplate().opsForValue().get(getCartKeyGenerateStrategy().generateCartKey(user));
		if (null != cart)
		{
			return getModelService().get(cart);
		}
		return null;
	}

	@Override
	public CartModel queryCartByGuidForAnonymousUser(final String guid)
	{
		final Cart cart = (Cart) getRedisTemplate().opsForValue().get(getCartKeyGenerateStrategy().generateCartKey(guid));
		if (null != cart)
		{
			return getModelService().get(cart);
		}
		return null;
	}

	/**
	 * @return the redisTemplate
	 */
	public RedisTemplate getRedisTemplate()
	{
		return redisTemplate;
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
	 * @return the cartKeyGenerateStrategy
	 */
	public CartKeyGenerateStrategy getCartKeyGenerateStrategy()
	{
		return cartKeyGenerateStrategy;
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
	 * @return the cartService
	 */
	public CartService getCartService()
	{
		return cartService;
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
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
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
