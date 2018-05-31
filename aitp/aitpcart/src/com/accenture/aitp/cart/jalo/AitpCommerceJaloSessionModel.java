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
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.jalo.order.Cart;


/**
 *
 */
public class AitpCommerceJaloSessionModel extends CommerceJaloSession
{


	private transient volatile CartModel _cart;


	@Override
	public Object setAttribute(final String name, final Object value)
	{
		if (CART.equalsIgnoreCase(name))
		{
			if (value instanceof CartModel)
			{
				this.setCart((CartModel) value);
				return getAttachedCartModel();
			}
			return super.setAttribute(name, value);
		}
		else
		{
			return this.getSessionContext().setAttribute(name, value);
		}
	}

	@Override
	public Object getAttribute(final String name)
	{
		return CART.equalsIgnoreCase(name) ? this.getAttachedCartModel() : this.getSessionContext().getAttribute(name);
	}

	@Override
	public boolean hasCart()
	{
		return getAttachedCartModel() != null;
	}

	@Override
	public void removeCart()
	{
		_cart = null;
	}

	protected void setCart(final CartModel cart)
	{
		final CartModel old = this.getAttachedCartModel();
		if (cart != old && (cart == null || !cart.equals(old)))
		{

			this.removeCart();
			this.setAttachedCart(cart);
		}
	}



	protected void setAttachedCart(final CartModel cart)
	{
		this._cart = cart;
	}




	@Override
	public Cart getCart()
	{
		throw new RuntimeException("don't support get cart via jalo session ");
	}

	@Override
	protected Cart createCart()
	{
		throw new RuntimeException("don't support create cart via jalo session ");
	}

	protected CartModel getAttachedCartModel()
	{
		return _cart;
	}

	@Override
	protected Cart getAttachedCart()
	{

		throw new RuntimeException("don't support get AttachedCart via jalo session ");

	}

}
