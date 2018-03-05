package com.accenture.performance.optimization.service;

import de.hybris.platform.commerceservices.delivery.DeliveryService;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.util.PriceValue;

public interface OptimizeDeliveyService extends DeliveryService 
{
	public PriceValue getOptimizeDeliveryCostForDeliveryModeAndAbstractOrder(final DeliveryModeModel deliveryMode, final AbstractOrderModel cart);
}
