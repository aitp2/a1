package com.accenture.performance.optimization.service.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.util.List;

import de.hybris.platform.commerceservices.delivery.impl.DefaultDeliveryService;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;

public class DefaultOptimizeDeliveryService extends DefaultDeliveryService{
	
	@Override
	public List<DeliveryModeModel> getSupportedDeliveryModeListForOrder(AbstractOrderModel abstractOrder)
	{
		validateParameterNotNull(abstractOrder, "abstractOrder model cannot be null");
		final List<DeliveryModeModel> deliveryModes = getDeliveryModeLookupStrategy().getSelectableDeliveryModesForOrder(
				abstractOrder);
		sortDeliveryModes(deliveryModes, abstractOrder);
		return deliveryModes;
	}
	
	//TODO acn
	@Override
	protected void sortDeliveryModes(final List<DeliveryModeModel> deliveryModeModels, final AbstractOrderModel abstractOrder)
	{
		//
	}
}
