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
package com.accenture.performance.optimization.facades;

import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.order.InvalidCartException;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;


/**
 *
 */
public interface OptimizeCheckoutFacade extends CheckoutFacade
{
	//Empty
	@Override
	public OrderData placeOrder() throws InvalidCartException;

	@Override
	public boolean setDeliveryMode(final String deliveryModeCode);

	@Override
	public boolean setPaymentDetails(final String paymentInfoId);

	public OptimizedCartData loadCart(String cartId);

	public void validateCheckoutCartInfo(final String oldCartId, final String evaluatedToMergeCartGuid) throws Exception;

	OrderData placeOmsOrder(final String cartGuid) throws InvalidCartException;

}
