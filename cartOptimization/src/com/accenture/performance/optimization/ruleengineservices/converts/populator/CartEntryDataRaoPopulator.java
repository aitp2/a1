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
package com.accenture.performance.optimization.ruleengineservices.converts.populator;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.ProductRAO;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.math.BigDecimal;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;


/**
 *
 */
public class CartEntryDataRaoPopulator implements Populator<OptimizedCartEntryData, OrderEntryRAO>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CartEntryDataRaoPopulator.class);

	private FlexibleSearchService flexibleSearchService;
	private CommonI18NService commonI18NService;

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

	@Override
	public void populate(final OptimizedCartEntryData source, final OrderEntryRAO target) throws ConversionException
	{
		if (Objects.nonNull(source.getProductCode()))
		{
			// tailor: simple convert productid only
			final ProductRAO rao = new ProductRAO();
			rao.setCode(source.getProductCode());
			target.setProduct(rao);
		}

		if (Objects.nonNull(source.getQuantity()))
		{
			target.setQuantity(source.getQuantity().intValue());
		}

		final Double basePrice = source.getBasePrice();
		if (Objects.nonNull(basePrice))
		{
			target.setBasePrice(BigDecimal.valueOf(basePrice.doubleValue()));
			// tailor : Currency : getCommonI18NService().getCurrentCurrency()
			target.setCurrencyIsoCode(getCommonI18NService().getCurrentCurrency().getIsocode());
		}

		if (Objects.nonNull(source.getEntryNumber()))
		{
			target.setEntryNumber(source.getEntryNumber());
		}
	}

	/**
	 * @return the flexibleSearchService
	 */
	public FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	/**
	 * @param flexibleSearchService
	 *           the flexibleSearchService to set
	 */
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

}
