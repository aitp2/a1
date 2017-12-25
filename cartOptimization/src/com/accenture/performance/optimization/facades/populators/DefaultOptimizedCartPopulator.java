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
package com.accenture.performance.optimization.facades.populators;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;

import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commerceservices.delivery.DeliveryService;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;


/**
 *
 */
public class DefaultOptimizedCartPopulator<T extends CartData> extends AbstractOptomizedCartPopulator<OptimizedCartData, T>
{
	private DeliveryService deliveryService;
	private Converter<DeliveryModeModel, DeliveryModeData> deliveryModeConverter;

	@Override
	public void populate(final OptimizedCartData source, final T target) throws ConversionException
	{
		addCommon(source, target);
		addTotals(source, target);
		addEntries(source, target);
		addPromotions(source, target);
		//addSavedCartData(source, target);
		addEntryGroups(source, target);
		//addComments(source, target);
		target.setGuid(source.getGuid());
		target.setTotalUnitCount(calcTotalUnitCount(source));
		target.setDeliveryAddress(source.getDeliveryAddress());
		if(source.getDeliveryMode() != null)
		{
			target.setDeliveryMode(getDeliveryMode(source.getDeliveryMode()));
		}
		target.setPaymentInfo(source.getPaymentInfo());
	}

	private DeliveryModeData getDeliveryMode(final String deliveryModeCode)
	{
		DeliveryModeModel model = deliveryService.getDeliveryModeForCode(deliveryModeCode);
		if(model != null)
		{
			return deliveryModeConverter.convert(model);
		}
		else
		{
			return null;
		}
	}
	/**
	 * @return the deliveryService
	 */
	public DeliveryService getDeliveryService() {
		return deliveryService;
	}

	/**
	 * @param deliveryService the deliveryService to set
	 */
	public void setDeliveryService(DeliveryService deliveryService) {
		this.deliveryService = deliveryService;
	}

	/**
	 * @return the deliveryModeConverter
	 */
	public Converter<DeliveryModeModel, DeliveryModeData> getDeliveryModeConverter() {
		return deliveryModeConverter;
	}

	/**
	 * @param deliveryModeConverter the deliveryModeConverter to set
	 */
	public void setDeliveryModeConverter(Converter<DeliveryModeModel, DeliveryModeData> deliveryModeConverter) {
		this.deliveryModeConverter = deliveryModeConverter;
	}



}
