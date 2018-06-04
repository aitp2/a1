/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.accenture.aitp.cart.service.order.strategies.calculation.impl;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.payment.PaymentMode;
import de.hybris.platform.order.strategies.calculation.FindPaymentCostStrategy;
import de.hybris.platform.order.strategies.calculation.impl.DefaultFindPaymentCostStrategy;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.internal.service.AbstractBusinessService;
import de.hybris.platform.util.PriceValue;
import de.hybris.platform.util.TaxValue;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.accenture.aitp.cart.constants.AitpcartConstants;


/**
 * Default implementation of {@link FindPaymentCostStrategy}.
 */
public class AitpFindPaymentCostStrategy extends DefaultFindPaymentCostStrategy
{

	private final static Logger LOG = Logger.getLogger(AitpFindPaymentCostStrategy.class);
	private ConfigurationService configurationService;
	
	@Override
	public PriceValue getPaymentCost(final AbstractOrderModel order)
	{
		try
		{
			PaymentModeModel paymentMode = order.getPaymentMode();
			if( paymentMode != null 
					&& AitpcartConstants.AITP_CART_SWTICH_ON.equalsIgnoreCase(getConfigurationService().getConfiguration().getString(AitpcartConstants.AITP_CART_PAYMENTCOST_SWTICH)))
			{
				getModelService().save(order);
	   			final AbstractOrder orderItem = getModelService().getSource(order);
	   			final PaymentMode pModeJalo = getModelService().getSource(paymentMode);
	   			return pModeJalo.getCost(orderItem);
			}
			else
			{
				return new PriceValue(order.getCurrency().getIsocode(), 0.0, order.getNet().booleanValue());
			}
		}
		catch (final Exception e)
		{
			LOG.warn("Could not find paymentCost for order [" + order.getCode() + "] due to : " + e + "... skipping!");
			return new PriceValue(order.getCurrency().getIsocode(), 0.0, order.getNet().booleanValue());
		}
	}

	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

}
