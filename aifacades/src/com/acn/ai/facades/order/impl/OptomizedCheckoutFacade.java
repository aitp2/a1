/**
 *
 */
package com.acn.ai.facades.order.impl;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.service.OptimizeCartService;

import de.hybris.platform.acceleratorfacades.order.impl.DefaultAcceleratorCheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;


/**
 * @author mingming.wang
 *
 */
public class OptomizedCheckoutFacade extends DefaultAcceleratorCheckoutFacade
{
	@Autowired
	private OptimizeCartService optimizeCartService;
	
	private Converter<OptimizedCartData, CartData> optimizeCartConverter;

	protected OptimizedCartData getOptimizedCart()
	{
		return hasCheckoutCart() ? optimizeCartService.getSessionOptimizedCart() : null;
	}
	
	@Override
	public CartData getCheckoutCart()
	{
		final OptimizedCartData cartData = getOptimizedCart();
		if (cartData != null)
		{
			return this.optimizeCartConverter.convert(cartData);
		}
		
		throw new NullPointerException("Cart can not be null");
	}
		
	
	//TODO acn
	@Override
	public List<AddressData> getSupportedDeliveryAddresses(final boolean visibleAddressesOnly)
	{
		//super.getSupportedDeliveryAddresses(visibleAddressesOnly);
		return Collections.emptyList();
		
	}
	
	//TODO acn
	@Override
	public boolean setDeliveryAddressIfAvailable()
	{
		return false;
	}
	
	@Override
	public boolean hasValidCart()
	{
		final OptimizedCartData optimizeCartData = getOptimizedCart();
		if(optimizeCartData == null)
		{
			return false;
		}
		else
		{
			return optimizeCartData.getEntries() != null && !optimizeCartData.getEntries().isEmpty();
		}
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
