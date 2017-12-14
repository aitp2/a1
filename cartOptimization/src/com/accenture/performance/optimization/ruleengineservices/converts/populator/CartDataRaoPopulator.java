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

import de.hybris.platform.converters.Converters;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.UserRAO;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;


/**
 *
 */
public class CartDataRaoPopulator implements Populator<OptimizedCartData, CartRAO>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CartDataRaoPopulator.class);

	// TODO
	private Converter<OptimizedCartEntryData, OrderEntryRAO> entryConverter;
	private Converter<UserModel, UserRAO> userConverter;

	private FlexibleSearchService flexibleSearchService;
	private CommonI18NService commonI18NService;


	@Override
	public void populate(final OptimizedCartData source, final CartRAO target) throws ConversionException
	{
		target.setCode(source.getCode());

		// tailor : Currency : getCommonI18NService().getCurrentCurrency()
		target.setCurrencyIsoCode(getCommonI18NService().getCurrentCurrency().getIsocode());

		target.setTotal(
				Objects.isNull(source.getTotalPrice()) ? BigDecimal.ZERO : BigDecimal.valueOf(source.getTotalPrice().doubleValue()));
		target.setSubTotal(
				Objects.isNull(source.getSubtotal()) ? BigDecimal.ZERO : BigDecimal.valueOf(source.getSubtotal().doubleValue()));
		target.setDeliveryCost(Objects.isNull(source.getDeliveryCost()) ? BigDecimal.ZERO
				: BigDecimal.valueOf(source.getDeliveryCost().doubleValue()));
		target.setPaymentCost(Objects.isNull(source.getPaymentCost()) ? BigDecimal.ZERO
				: BigDecimal.valueOf(source.getPaymentCost().doubleValue()));

		if (CollectionUtils.isNotEmpty(source.getEntries()))
		{
			final List<OrderEntryRAO> list = Converters.convertAll(source.getEntries(), this.getEntryConverter());
			list.forEach((entry) -> {
				entry.setOrder(target);
			});
			target.setEntries(new LinkedHashSet(list));
		}
		else
		{
			LOGGER.warn("Order entry list is empty, skipping the conversion");
		}

		// tailor: no any discount before protmoion calculating

		this.convertAndSetUser(target, source.getUserId());

		// tailor: payment mode convert

		// from CartRaoPopulator
		target.setActions(new LinkedHashSet());
		target.setOriginalTotal(target.getTotal());
	}

	/**
	 *
	 */
	private void convertAndSetUser(final CartRAO target, final String userId)
	{
		if (StringUtils.isNotEmpty(userId))
		{
			UserModel userModel = new UserModel();
			userModel.setUid(userId);
			try
			{
				userModel = flexibleSearchService.getModelByExample(userModel);
				target.setUser(this.getUserConverter().convert(userModel));
			}
			catch (final ModelNotFoundException ex)
			{
				LOGGER.error("can't found user in cart {}", userId);
			}
		}
	}

	/**
	 * @return the entryConverter
	 */
	public Converter<OptimizedCartEntryData, OrderEntryRAO> getEntryConverter()
	{
		return entryConverter;
	}

	/**
	 * @param entryConverter
	 *           the entryConverter to set
	 */
	public void setEntryConverter(final Converter<OptimizedCartEntryData, OrderEntryRAO> entryConverter)
	{
		this.entryConverter = entryConverter;
	}

	/**
	 * @return the userConverter
	 */
	public Converter<UserModel, UserRAO> getUserConverter()
	{
		return userConverter;
	}

	/**
	 * @param userConverter
	 *           the userConverter to set
	 */
	public void setUserConverter(final Converter<UserModel, UserRAO> userConverter)
	{
		this.userConverter = userConverter;
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

}
