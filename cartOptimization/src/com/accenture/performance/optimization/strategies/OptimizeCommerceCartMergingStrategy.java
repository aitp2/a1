package com.accenture.performance.optimization.strategies;

import de.hybris.platform.commerceservices.order.CommerceCartMergingException;
import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.order.impl.CommerceCartMergingStrategy;

import java.util.List;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;


public interface OptimizeCommerceCartMergingStrategy extends CommerceCartMergingStrategy
{
	public void mergeCarts(OptimizedCartData fromCart, OptimizedCartData optimizeCart,
			List<CommerceCartModification> modifications) throws CommerceCartMergingException;


}
