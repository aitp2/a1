package com.accenture.performance.optimization.strategies.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import org.apache.log4j.Logger;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.service.OptimizeCommerceCartService;

import de.hybris.platform.commerceservices.order.impl.DefaultCommerceDeliveryModeStrategy;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.order.exceptions.CalculationException;

public class DefaultOptimizeCommerceDeliveryModeStrategy extends DefaultCommerceDeliveryModeStrategy {
	
	private Logger LOG = Logger.getLogger(DefaultOptimizeCommerceDeliveryModeStrategy.class);
	private OptimizeCommerceCartService optimizeCommerceCartService;;
	
	@Override
	public boolean setDeliveryMode(final CommerceCheckoutParameter parameter)
	{
		final DeliveryModeModel deliveryModeModel = parameter.getDeliveryMode();
		final OptimizedCartData cartModel = parameter.getOptimizeCart();

		validateParameterNotNull(cartModel, "Cart model cannot be null");
		validateParameterNotNull(deliveryModeModel, "Delivery mode model cannot be null");

		cartModel.setDeliveryMode(deliveryModeModel.getCode());
		
		final CommerceCartParameter commerceCartParameter = new CommerceCartParameter();
		commerceCartParameter.setEnableHooks(true);
		commerceCartParameter.setOptimizeCart(cartModel);
		
		try {
			optimizeCommerceCartService.recalculateCart(commerceCartParameter);
		} catch (CalculationException e) {
			LOG.error(e.getMessage(), e);
			return false;
		}
		
		return true;
	}

	/**
	 * @return the optimizeCommerceCartService
	 */
	public OptimizeCommerceCartService getOptimizeCommerceCartService() {
		return optimizeCommerceCartService;
	}

	/**
	 * @param optimizeCommerceCartService the optimizeCommerceCartService to set
	 */
	public void setOptimizeCommerceCartService(OptimizeCommerceCartService optimizeCommerceCartService) {
		this.optimizeCommerceCartService = optimizeCommerceCartService;
	}
}
