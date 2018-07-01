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
import de.hybris.platform.core.GenericCondition;
import de.hybris.platform.core.GenericConditionList;
import de.hybris.platform.core.GenericQuery;
import de.hybris.platform.core.GenericValueCondition;
import de.hybris.platform.core.Registry;
import de.hybris.platform.jalo.JaloItemNotFoundException;
import de.hybris.platform.jalo.SearchResult;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.order.Cart;
import de.hybris.platform.util.StandardSearchResult;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;

import com.accenture.aitp.cart.strategy.CartKeyGenerateStrategy;


/**
 *
 */
public class AitpCommerceJaloSession extends CommerceJaloSession
{
	//private static final Logger LOG = Logger.getLogger(AitpCommerceJaloSession.class);
	//private transient volatile Cart _cart;
	//private volatile String cartKey;
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
/**
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

				final RedisTemplate redisTemplate = (RedisTemplate) Registry.getApplicationContext().getBean("redisCartTemplate");
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
						final RedisTemplate redisTemplate = (RedisTemplate) Registry.getApplicationContext()
								.getBean("redisCartTemplate");
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

	}*/

	@Override
	public SearchResult search(final GenericQuery query)
	{
		if ("CartEntry".equals(query.getInitialTypeCode()))
		{
			final Object[] object = findCartAndEntryNumberInQuery(query);
			final Cart cart = (Cart) object[0];
			final Integer entryNumber = (Integer) object[1];
			if (null != cart)
			{
				final List<AbstractOrderEntry> entries = cart.getEntries().stream()
						.filter(entry -> entry.getEntryNumber().equals(entryNumber)).collect(Collectors.toList());
				return new StandardSearchResult(entries, entries.size(), -1, -1);
			}

		} //can't validation the unique code,suggest use the unique index to validation
		else if ("AbstractOrder".equals(query.getInitialTypeCode()))
		{
			return new StandardSearchResult(Collections.EMPTY_LIST, 0, -1, -1);
		}
		return super.search(query);
	}

	private Object[] findCartAndEntryNumberInQuery(final GenericQuery query)
	{
		final Object[] result = new Object[2];
		final GenericConditionList genericConditionListQ = (GenericConditionList) query.getCondition();
		final List<GenericCondition> genericConditionList = genericConditionListQ.getConditionList();
		final GenericValueCondition genericCondition = (GenericValueCondition) genericConditionList.get(0);
		final GenericValueCondition genericCondition1 = (GenericValueCondition) genericConditionList.get(1);
		if (genericCondition1.getValue() instanceof Cart)
		{
			result[0] = genericCondition1.getValue();
			result[1] = genericCondition.getValue();
		}
		else if (genericCondition.getValue() instanceof Cart)
		{
			result[1] = genericCondition1.getValue();
			result[0] = genericCondition.getValue();
		}
		return result;
	}

}
