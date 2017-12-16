/**
 *
 */
package com.acn.ai.facades.order.impl;

import de.hybris.platform.acceleratorfacades.order.impl.DefaultAcceleratorCheckoutFacade;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;

import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.service.OptimizeCartService;


/**
 * @author mingming.wang
 *
 */
public class OptomizedCheckoutFacade extends DefaultAcceleratorCheckoutFacade
{
	@Autowired
	private OptimizeCartService optimizeCartService;

	protected OptimizedCartData getOptimizedCart()
	{
		return hasCheckoutCart() ? optimizeCartService.getSessionOptimizedCart() : null;
	}

	@Override
	public boolean hasPickUpItems()
	{
		return hasItemsMatchingPredicate(e -> e.getDeliveryPointOfService() != null);
	}

	@Override
	protected boolean hasItemsMatchingPredicate(final Predicate<AbstractOrderEntryModel> predicate)
	{
		/*
		 * final OptimizedCartData cart = getOptimizedCart(); if (cart != null &&
		 * CollectionUtils.isNotEmpty(cart.getEntries())) { for (final OptimizedCartEntryData entry : cart.getEntries()) {
		 * if (predicate.test(entry)) { return true; } } }
		 */
		return false;
	}

	@Override
	protected CartModel getCart()
	{
		throw new RuntimeException("not support any more");
	}

	/**
	 * @return the optimizeCartService
	 */
	public OptimizeCartService getOptimizeCartService()
	{
		return optimizeCartService;
	}

	/**
	 * @param optimizeCartService
	 *           the optimizeCartService to set
	 */
	public void setOptimizeCartService(final OptimizeCartService optimizeCartService)
	{
		this.optimizeCartService = optimizeCartService;
	}

}
