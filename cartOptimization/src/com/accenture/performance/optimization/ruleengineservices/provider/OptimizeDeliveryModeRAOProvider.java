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
package com.accenture.performance.optimization.ruleengineservices.provider;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.order.daos.DeliveryModeDao;
import de.hybris.platform.ruleengineservices.calculation.DeliveryCostEvaluationStrategy;
import de.hybris.platform.ruleengineservices.rao.DeliveryModeRAO;
import de.hybris.platform.ruleengineservices.rao.providers.RAOProvider;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;


/**
 *
 */
public class OptimizeDeliveryModeRAOProvider implements RAOProvider
{
	private DeliveryModeDao deliveryModeDao;
	private DeliveryCostEvaluationStrategy deliveryCostEvaluationStrategy;
	private Converter<DeliveryModeModel, DeliveryModeRAO> deliveryModeRaoConverter;
	private CommonI18NService commonI18NService;

	@Override
	public Set<?> expandFactModel(final Object modelFact)
	{
		if (modelFact instanceof AbstractOrderModel)
		{
			final AbstractOrderModel orderModel = (AbstractOrderModel) modelFact;
			final Collection<DeliveryModeModel> availableDeliveryModes = this.getDeliveryModeDao().findAllDeliveryModes();
			if (CollectionUtils.isNotEmpty(availableDeliveryModes))
			{
				return availableDeliveryModes.stream().map((dm) -> {
					final DeliveryModeRAO deliveryModeRao = this.getDeliveryModeRaoConverter().convert(dm);
					final BigDecimal cost = this.getDeliveryCostEvaluationStrategy().evaluateCost(orderModel, dm);
					deliveryModeRao.setCost(cost);
					deliveryModeRao.setCurrencyIsoCode(orderModel.getCurrency().getIsocode());
					return deliveryModeRao;
				}).collect(Collectors.toSet());
			}
		}
		else if (modelFact instanceof OptimizedCartData)
		{
			// ADD BY JUNBIN
			final Collection<DeliveryModeModel> availableDeliveryModes = this.getDeliveryModeDao().findAllDeliveryModes();
			if (CollectionUtils.isNotEmpty(availableDeliveryModes))
			{
				return availableDeliveryModes.stream().map((dm) -> {
					final DeliveryModeRAO deliveryModeRao = this.getDeliveryModeRaoConverter().convert(dm);
					final BigDecimal cost = this.getDeliveryCostEvaluationStrategy().evaluateCost(null, dm);
					deliveryModeRao.setCost(cost);
					deliveryModeRao.setCurrencyIsoCode(getCommonI18NService().getCurrentCurrency().getIsocode());
					return deliveryModeRao;
				}).collect(Collectors.toSet());
			}
		}
		return Collections.emptySet();
	}

	/**
	 * @return the deliveryModeDao
	 */
	public DeliveryModeDao getDeliveryModeDao()
	{
		return deliveryModeDao;
	}

	/**
	 * @param deliveryModeDao
	 *           the deliveryModeDao to set
	 */
	public void setDeliveryModeDao(final DeliveryModeDao deliveryModeDao)
	{
		this.deliveryModeDao = deliveryModeDao;
	}

	/**
	 * @return the deliveryCostEvaluationStrategy
	 */
	public DeliveryCostEvaluationStrategy getDeliveryCostEvaluationStrategy()
	{
		return deliveryCostEvaluationStrategy;
	}

	/**
	 * @param deliveryCostEvaluationStrategy
	 *           the deliveryCostEvaluationStrategy to set
	 */
	public void setDeliveryCostEvaluationStrategy(final DeliveryCostEvaluationStrategy deliveryCostEvaluationStrategy)
	{
		this.deliveryCostEvaluationStrategy = deliveryCostEvaluationStrategy;
	}

	/**
	 * @return the deliveryModeRaoConverter
	 */
	public Converter<DeliveryModeModel, DeliveryModeRAO> getDeliveryModeRaoConverter()
	{
		return deliveryModeRaoConverter;
	}

	/**
	 * @param deliveryModeRaoConverter
	 *           the deliveryModeRaoConverter to set
	 */
	public void setDeliveryModeRaoConverter(final Converter<DeliveryModeModel, DeliveryModeRAO> deliveryModeRaoConverter)
	{
		this.deliveryModeRaoConverter = deliveryModeRaoConverter;
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
