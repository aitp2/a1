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

import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.PriceDataFactory;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.delivery.DeliveryService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.math.BigDecimal;

import org.springframework.util.Assert;

import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;


/**
 *
 */
public class DefaultOptimizedCartEntryPopulator implements Populator<OptimizedCartEntryData, OrderEntryData>
{

	private Converter<ProductModel, ProductData> productConverter;
	private PriceDataFactory priceDataFactory;
	private CommonI18NService commonI18NService;
	private ProductService productService;
	
	private DeliveryService deliveryService;
	private Converter<DeliveryModeModel, DeliveryModeData> deliveryModeConverter;


	@Override
	public void populate(final OptimizedCartEntryData source, final OrderEntryData target)
	{
		Assert.notNull(source, "Parameter source cannot be null.");
		Assert.notNull(target, "Parameter target cannot be null.");

		addCommon(source, target);
		addProduct(source, target);
		addTotals(source, target);
		
		addDeliveryMode(source, target);
		//addConfigurations(source, target);
		//addEntryGroups(source, target);
		//addComments(source, target);
	}


	
	protected void addDeliveryMode(final OptimizedCartEntryData orderEntry, final OrderEntryData entry) {
		if (orderEntry.getDeliveryMode() != null) 
		{
			entry.setDeliveryMode(getDeliveryMode(orderEntry.getDeliveryMode()));
		}

		if (orderEntry.getDeliveryPointOfService() != null) 
		{
			entry.setDeliveryPointOfService(orderEntry.getDeliveryPointOfService());
		}
	}

	private DeliveryModeData getDeliveryMode(final String deliveryModeCode) 
	{
		DeliveryModeModel model = deliveryService.getDeliveryModeForCode(deliveryModeCode);
		if (model != null) 
		{
			return deliveryModeConverter.convert(model);
		} 
		else 
		{
			return null;
		}
	}
	
	protected void addCommon(final OptimizedCartEntryData orderEntry, final OrderEntryData entry)
	{
		entry.setEntryNumber(orderEntry.getEntryNumber());
		entry.setQuantity(orderEntry.getQuantity());
		//	adjustUpdateable(entry, orderEntry);
	}




	protected void addProduct(final OptimizedCartEntryData orderEntry, final OrderEntryData entry)
	{
		entry.setProduct(getProductConverter().convert(productService.getProductForCode(orderEntry.getProductCode())));
	}

	protected void addTotals(final OptimizedCartEntryData orderEntry, final OrderEntryData entry)
	{
		if (orderEntry.getBasePrice() != null)
		{
			entry.setBasePrice(createPrice(orderEntry, orderEntry.getBasePrice()));
		}
		if (orderEntry.getTotalPrice() != null)
		{
			entry.setTotalPrice(createPrice(orderEntry, orderEntry.getTotalPrice()));
		}
	}

	protected PriceData createPrice(final OptimizedCartEntryData source, final Double val)
	{
		if (source == null)
		{
			throw new IllegalArgumentException("source order must not be null");
		}

		final CurrencyModel currency = commonI18NService.getCurrency(source.getCartData().getCurrencyCode());
		if (currency == null)
		{
			throw new IllegalArgumentException("source order currency must not be null");
		}

		// Get double value, handle null as zero
		final double priceValue = val != null ? val.doubleValue() : 0d;

		return getPriceDataFactory().create(PriceDataType.BUY, BigDecimal.valueOf(priceValue), currency);
	}

	protected PriceData createPrice(final AbstractOrderEntryModel orderEntry, final Double val)
	{
		return getPriceDataFactory().create(PriceDataType.BUY, BigDecimal.valueOf(val.doubleValue()),
				orderEntry.getOrder().getCurrency());
	}


	/**
	 * @return the productConverter
	 */
	public Converter<ProductModel, ProductData> getProductConverter()
	{
		return productConverter;
	}


	/**
	 * @param productConverter
	 *           the productConverter to set
	 */
	public void setProductConverter(final Converter<ProductModel, ProductData> productConverter)
	{
		this.productConverter = productConverter;
	}


	/**
	 * @return the priceDataFactory
	 */
	public PriceDataFactory getPriceDataFactory()
	{
		return priceDataFactory;
	}


	/**
	 * @param priceDataFactory
	 *           the priceDataFactory to set
	 */
	public void setPriceDataFactory(final PriceDataFactory priceDataFactory)
	{
		this.priceDataFactory = priceDataFactory;
	}


	/**
	 * @return the commonI18NService
	 */
	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}


	/**
	 * @param commonI18NService
	 *           the commonI18NService to set
	 */
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}


	/**
	 * @return the productService
	 */
	public ProductService getProductService()
	{
		return productService;
	}


	/**
	 * @param productService
	 *           the productService to set
	 */
	public void setProductService(final ProductService productService)
	{
		this.productService = productService;
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
