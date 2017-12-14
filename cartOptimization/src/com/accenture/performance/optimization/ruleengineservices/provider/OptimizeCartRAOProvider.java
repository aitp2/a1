/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2017 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package com.accenture.performance.optimization.ruleengineservices.provider;

import de.hybris.platform.ruleengineservices.calculation.RuleEngineCalculationService;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.PaymentModeRAO;
import de.hybris.platform.ruleengineservices.rao.ProductRAO;
import de.hybris.platform.ruleengineservices.rao.UserRAO;
import de.hybris.platform.ruleengineservices.rao.providers.RAOFactsExtractor;
import de.hybris.platform.ruleengineservices.rao.providers.impl.AbstractExpandedRAOProvider;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;


/**
 *
 */
public class OptimizeCartRAOProvider extends AbstractExpandedRAOProvider<OptimizedCartData, CartRAO>
{
	public static final String INCLUDE_CART = "INCLUDE_CART";
	public static final String EXPAND_ENTRIES = "EXPAND_ENTRIES";
	public static final String EXPAND_PRODUCTS = "EXPAND_PRODUCTS";
	public static final String EXPAND_DISCOUNTS = "EXPAND_DISCOUNTS";
	public static final String AVAILABLE_DELIVERY_MODES = "EXPAND_AVAILABLE_DELIVERY_MODES";
	public static final String EXPAND_CATEGORIES = "EXPAND_CATEGORIES";
	public static final String EXPAND_USERS = "EXPAND_USERS";
	public static final String EXPAND_PAYMENT_MODE = "EXPAND_PAYMENT_MODE";
	private Converter<OptimizedCartData, CartRAO> cartRaoConverter;
	private RuleEngineCalculationService ruleEngineCalculationService;

	public OptimizeCartRAOProvider()
	{
		this.validOptions = Arrays.asList(new String[]
		{ "INCLUDE_CART", "EXPAND_ENTRIES", "EXPAND_PRODUCTS", "EXPAND_CATEGORIES", "EXPAND_USERS", "EXPAND_PAYMENT_MODE",
				"EXPAND_AVAILABLE_DELIVERY_MODES", "EXPAND_DISCOUNTS" });
		this.defaultOptions = Arrays.asList(new String[]
		{ "INCLUDE_CART", "EXPAND_ENTRIES", "EXPAND_PRODUCTS", "EXPAND_CATEGORIES", "EXPAND_USERS", "EXPAND_PAYMENT_MODE",
				"EXPAND_AVAILABLE_DELIVERY_MODES" });
		this.minOptions = Arrays.asList(new String[]
		{ "INCLUDE_CART" });
	}

	@Override
	protected CartRAO createRAO(final OptimizedCartData cart)
	{
		final CartRAO rao = this.getCartRaoConverter().convert(cart);
		this.getRuleEngineCalculationService().calculateTotals(rao);
		return rao;
	}

	@Override
	protected Set<Object> expandRAO(final CartRAO cart, final Collection<String> options)
	{
		final LinkedHashSet facts = new LinkedHashSet();
		facts.addAll(super.expandRAO(cart, options));
		final Iterator arg4 = options.iterator();

		while (arg4.hasNext())
		{
			final String option = (String) arg4.next();
			final Set entries = cart.getEntries();
			switch (option.hashCode())
			{
				case -1998299017:
					if (option.equals("INCLUDE_CART"))
					{
						facts.add(cart);
					}
					break;
				case -1831506583:
					if (option.equals("EXPAND_PRODUCTS"))
					{
						this.addProducts(facts, entries);
					}
					break;
				case -1735987197:
					if (option.equals("EXPAND_USERS"))
					{
						this.addUserGroups(facts, cart.getUser());
					}
					break;
				case -858518047:
					if (option.equals("EXPAND_PAYMENT_MODE"))
					{
						this.addPaymentMode(facts, cart.getPaymentMode());
					}
					break;
				case -11528243:
					if (option.equals("EXPAND_DISCOUNTS"))
					{
						facts.addAll(cart.getDiscountValues());
					}
					break;
				case 402256097:
					if (option.equals("EXPAND_CATEGORIES"))
					{
						this.addProductCategories(facts, entries);
					}
					break;
				case 1014124491:
					if (option.equals("EXPAND_ENTRIES"))
					{
						this.addEntries(facts, entries);
					}
			}
		}

		return facts;
	}

	protected void addProductCategories(final Set<Object> facts, final Set<OrderEntryRAO> entries)
	{
		if (CollectionUtils.isNotEmpty(entries))
		{
			final Iterator arg3 = entries.iterator();

			while (arg3.hasNext())
			{
				final OrderEntryRAO orderEntry = (OrderEntryRAO) arg3.next();
				final ProductRAO product = orderEntry.getProduct();
				if (Objects.nonNull(product) && CollectionUtils.isNotEmpty(product.getCategories()))
				{
					facts.addAll(product.getCategories());
				}
			}
		}

	}

	protected void addProducts(final Set<Object> facts, final Set<OrderEntryRAO> entries)
	{
		if (CollectionUtils.isNotEmpty(entries))
		{
			entries.forEach((orderEntry) -> {
				facts.add(orderEntry.getProduct());
			});
		}

	}

	protected void addUserGroups(final Set<Object> facts, final UserRAO userRAO)
	{
		if (Objects.nonNull(userRAO))
		{
			facts.add(userRAO);
			final Set groups = userRAO.getGroups();
			if (CollectionUtils.isNotEmpty(groups))
			{
				facts.addAll(groups);
			}
		}

	}

	protected void addEntries(final Set<Object> facts, final Set<OrderEntryRAO> entries)
	{
		if (CollectionUtils.isNotEmpty(entries))
		{
			facts.addAll(entries);
		}

	}

	protected void addPaymentMode(final Set<Object> facts, final PaymentModeRAO paymentModeRAO)
	{
		if (Objects.nonNull(paymentModeRAO))
		{
			facts.add(paymentModeRAO);
		}

	}

	protected Predicate<RAOFactsExtractor> isEnabled(final Collection<String> options)
	{
		return (e) -> {
			return StringUtils.isNotEmpty(e.getTriggeringOption()) && options.contains(e.getTriggeringOption());
		};
	}

	/**
	 * @return the cartRaoConverter
	 */
	public Converter<OptimizedCartData, CartRAO> getCartRaoConverter()
	{
		return cartRaoConverter;
	}

	/**
	 * @param cartRaoConverter
	 *           the cartRaoConverter to set
	 */
	public void setCartRaoConverter(final Converter<OptimizedCartData, CartRAO> cartRaoConverter)
	{
		this.cartRaoConverter = cartRaoConverter;
	}

	/**
	 * @return the ruleEngineCalculationService
	 */
	public RuleEngineCalculationService getRuleEngineCalculationService()
	{
		return ruleEngineCalculationService;
	}

	/**
	 * @param ruleEngineCalculationService
	 *           the ruleEngineCalculationService to set
	 */
	public void setRuleEngineCalculationService(final RuleEngineCalculationService ruleEngineCalculationService)
	{
		this.ruleEngineCalculationService = ruleEngineCalculationService;
	}

}


