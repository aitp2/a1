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
package com.accenture.aitp.cart.jalo;

import de.hybris.platform.commerceservices.jalo.CommerceJaloSession;
import de.hybris.platform.core.Registry;
import de.hybris.platform.jalo.JaloItemNotFoundException;
import de.hybris.platform.jalo.order.Cart;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;

import com.accenture.aitp.cart.strategy.CartKeyGenerateStrategy;


/**
 *
 */
public class AitpCommerceJaloSession extends CommerceJaloSession
{
	private static final Logger LOG = Logger.getLogger(AitpCommerceJaloSession.class);
	private transient volatile Cart _cart;
	private volatile String cartKey;
	/*
	 * @Override public Object setAttribute(final String name, final Object value) { return
	 * this.getSessionContext().setAttribute(name, value); }
	 *
	 * @Override public Object getAttribute(final String name) { return this.getSessionContext().getAttribute(name); }
	 *
	 * @Override public Object removeAttribute(final String name) { return
	 * this.getSessionContext().removeAttribute(name); }
	 *
	 */

	@Override
	protected Cart getAttachedCart()
	{
		if (this._cart != null)
		{
			return _cart;
		}
		else if (this.cartKey == null)
		{
			return null;
		}
		else
		{
			try
			{

				final RedisTemplate redisTemplate = (RedisTemplate) Registry.getApplicationContext().getBean("redisTemplate");
				final long beforeInvokeTime = System.currentTimeMillis();
				this._cart = (Cart) redisTemplate.opsForValue().get(this.cartKey);
				LOG.info(Thread.currentThread().getName() + "--get session cart use time "
						+ (System.currentTimeMillis() - beforeInvokeTime) + " ms");
				return this._cart;
			}
			catch (final JaloItemNotFoundException arg0)
			{
				return null;
			}
		}
	}

	@Override
	public void removeCart()
	{
		Cart tmpCart = this.getAttachedCart();
		if (tmpCart != null)
		{
			synchronized (this)
			{
				tmpCart = this.getAttachedCart();
				if (tmpCart != null)
				{
					try
					{
						final RedisTemplate redisTemplate = (RedisTemplate) Registry.getApplicationContext().getBean("redisTemplate");
						redisTemplate.delete(this.cartKey);

					}
					catch (final Exception e)
					{
						LOG.error("Couldn\'t remove cart" + tmpCart.getPK(), e);
					}
					finally
					{
						this.setAttachedCart((Cart) null);
					}
				}
			}
		}
	}

	@Override
	protected void setAttachedCart(final Cart cart)
	{

		final CartKeyGenerateStrategy cartKeyGenerateStrategy = (CartKeyGenerateStrategy) Registry.getApplicationContext()
				.getBean("cartKeyGenerateStrategy");
		this._cart = cart;
		this.cartKey = cartKeyGenerateStrategy.generateCartKey(cart);

	}
}
