package com.acn.ai.facades.order.impl;

import java.util.function.Predicate;

import org.apache.commons.collections.CollectionUtils;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.service.OptimizeCartService;

import de.hybris.platform.acceleratorfacades.flow.impl.SessionOverrideCheckoutFlowFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.servicelayer.dto.converter.Converter;

public class ACNSessionOverrideCheckoutFlowFacade extends SessionOverrideCheckoutFlowFacade {
	
	private OptimizeCartService optimizeCartService;
	private Converter<OptimizedCartData, CartData> optimizeCartConverter;
	
	@Override
	public boolean hasValidCart()
	{
		final CartData cartData = optimizeCartConverter.convert(optimizeCartService.getSessionOptimizedCart()) ;
		return cartData.getEntries() != null && !cartData.getEntries().isEmpty();
	}

	@Override
	public CartData getCheckoutCart()
	{
		return optimizeCartConverter.convert(optimizeCartService.getSessionOptimizedCart()) ;
	}
	
	@Override
	public boolean hasNoDeliveryAddress()
	{
		final CartData cartData = getCheckoutCart();
		return hasShippingItems() && (cartData == null || cartData.getDeliveryAddress() == null);
	}
	
	@Override
	public boolean hasShippingItems()
	{
		return hasItemsMatchingPredicateACN(e -> e.getDeliveryPointOfService() == null);
	}
	
	@Override
	public boolean hasPickUpItems()
	{
		return hasItemsMatchingPredicateACN(e -> e.getDeliveryPointOfService() != null);
	}

	protected boolean hasItemsMatchingPredicateACN(final Predicate<OrderEntryData> predicate)
	{
		final CartData cart = getCheckoutCart();
		if (cart != null && CollectionUtils.isNotEmpty(cart.getEntries()))
		{
			for (final OrderEntryData entry : cart.getEntries())
			{
				if (predicate.test(entry))
				{
					return true;
				}
			}
		}
		return false;
	}

	
	/**
	 * @return the optimizeCartService
	 */
	public OptimizeCartService getOptimizeCartService() {
		return optimizeCartService;
	}

	/**
	 * @param optimizeCartService the optimizeCartService to set
	 */
	public void setOptimizeCartService(OptimizeCartService optimizeCartService) {
		this.optimizeCartService = optimizeCartService;
	}

	/**
	 * @return the optimizeCartConverter
	 */
	public Converter<OptimizedCartData, CartData> getOptimizeCartConverter() {
		return optimizeCartConverter;
	}

	/**
	 * @param optimizeCartConverter the optimizeCartConverter to set
	 */
	public void setOptimizeCartConverter(Converter<OptimizedCartData, CartData> optimizeCartConverter) {
		this.optimizeCartConverter = optimizeCartConverter;
	}
}
