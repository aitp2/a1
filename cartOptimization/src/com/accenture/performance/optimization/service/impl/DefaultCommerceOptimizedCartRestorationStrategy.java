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
package com.accenture.performance.optimization.service.impl;

import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.CommerceCartRestoration;
import de.hybris.platform.commerceservices.order.CommerceCartRestorationException;
import de.hybris.platform.commerceservices.order.impl.DefaultCommerceCartRestorationStrategy;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;
import com.accenture.performance.optimization.service.OptimizeCartService;
import com.accenture.performance.optimization.service.OptimizeModelDealService;


/**
 *
 */
public class DefaultCommerceOptimizedCartRestorationStrategy extends DefaultCommerceCartRestorationStrategy
{
	private static final Logger LOG = Logger.getLogger(DefaultCommerceOptimizedCartRestorationStrategy.class);

	private SessionService sessionService;
	private OptimizeModelDealService optimizeModelDealService;
	//private OptimizeCommerceCartService optimizeCommerceCartService;



	@Override
	public CommerceCartRestoration restoreCart(final CommerceCartParameter parameter) throws CommerceCartRestorationException
	{
		//final CartModel cartModel = parameter.getCart();
		final OptimizedCartData cartModel = parameter.getOptimizeCart();
		final CommerceCartRestoration restoration = new CommerceCartRestoration();
		final List<CommerceCartModification> modifications = new ArrayList<>();
		if (cartModel != null)
		{
			if (getBaseSiteService().getCurrentBaseSite().getUid().equals(cartModel.getBaseSite()))
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Restoring from cart " + cartModel.getCode() + ".");
				}
				if (isCartInValidityPeriod(cartModel))
				{
					cartModel.setCalculated(Boolean.FALSE);
					//if (!cartModel.getPaymentTransactions().isEmpty())
					//{
					// clear payment transactions
					//	clearPaymentTransactionsOnCart(cartModel);
					// reset guid since its used as a merchantId for payment subscriptions and is a base id for generating PaymentTransaction.code
					// see de.hybris.platform.payment.impl.DefaultPaymentServiceImpl.authorize(DefaultPaymentServiceImpl.java:177)
					//	cartModel.setGuid(getGuidKeyGenerator().generate().toString());
					//	}

					//getModelService().save(cartModel);
					/*
					 * try { optimizeCommerceCartService.calculateCart(parameter); } catch (final IllegalStateException ex) {
					 * LOG.error("Failed to recalculate order [" + cartModel.getCode() + "]", ex); }
					 */
					//getCartService().setAttribute(cartModel);
					getOptimizeCartService().setSessionOptimizedCart(cartModel);

					if (LOG.isDebugEnabled())
					{
						LOG.debug("Cart " + cartModel.getCode() + " was found to be valid and was restored to the session.");
					}
				}
				else
				{
					try
					{
						modifications.addAll(rebuildSessionCart(parameter));
					}
					catch (final CommerceCartModificationException e)
					{
						throw new CommerceCartRestorationException(e.getMessage(), e);
					}
				}
				//getCommerceCommonI18NService().setCurrentCurrency(cartModel.get);
				//getCommerceCartService().calculateCart(parameter);
				//optimizeCommerceCartService.calculateCart(parameter);
				//getCommerceCartCalculationStrategy().calculateCart(parameter);
				getOptimizeCartService().setSessionOptimizedCart(cartModel);
			}
			else
			{
				LOG.warn(String.format("Current Site %s does not equal to cart %s Site %s", getBaseSiteService().getCurrentBaseSite(),
						cartModel, cartModel.getBaseSite()));
			}
		}
		restoration.setModifications(modifications);
		return restoration;
	}

	protected void rewriteEntriesFromCartToCart(final CommerceCartParameter parameter, final OptimizedCartData fromCartModel,
			final OptimizedCartData toCartModel, final List<CommerceCartModification> modifications)
			throws CommerceCartModificationException
	{

		for (final OptimizedCartEntryData entry : fromCartModel.getEntries())
		{
			final CommerceCartParameter newCartParameter = new CommerceCartParameter();
			parameter.setEnableHooks(true);
			newCartParameter.setOptimizeCart(toCartModel);
			//newCartParameter.setProduct(entry.getProduct());
			newCartParameter.setProductCode(entry.getProductCode());
			newCartParameter.setPointOfService(null);
			newCartParameter.setQuantity(entry.getQuantity() == null ? 1l : entry.getQuantity().longValue());
			//newCartParameter.setUnit(get);
			newCartParameter.setCreateNewEntry(false);

			final CommerceCartModification modification = getOptimizeCartService().doAddToCart(newCartParameter);
			modifications.add(modification);
		}
	}

	protected boolean isCartInValidityPeriod(final OptimizedCartData cartModel)
	{
		return new DateTime(cartModel.getCreateTime())
				.isAfter(new DateTime(getTimeService().getCurrentTime()).minusSeconds(getCartValidityPeriod()));
	}

	@Override
	protected Collection<CommerceCartModification> rebuildSessionCart(final CommerceCartParameter parameter)
			throws CommerceCartModificationException
	{
		final List<CommerceCartModification> modifications = new ArrayList<>();
		// final CartModel newCart = getCartFactory().createCart();
		final OptimizedCartData newCartData = getCartFactory().createSessionCart();
		LOG.info("===========user of new cart=========" + newCartData.getUserId());
		LOG.info("===========user of parameter cart=========" + parameter.getOptimizeCart().getUserId());

		//if (!parameter.getCart().equals(newCart))
		//{
		rewriteEntriesFromCartToCart(parameter, parameter.getOptimizeCart(), newCartData, modifications);

		newCartData.setCalculated(Boolean.FALSE);
		getOptimizeModelDealService().removeCurrentSessionCart(parameter.getOptimizeCart());
		//}
		return modifications;
	}



	/**
	 * @return the sessionService
	 */
	public SessionService getSessionService()
	{
		return sessionService;
	}

	/**
	 * @param sessionService
	 *           the sessionService to set
	 */
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	/**
	 * @return the cartFactory
	 */
	@Override
	public OptimizeCartFactory getCartFactory()
	{
		return (OptimizeCartFactory) super.getCartFactory();
	}


	/**
	 * @return the optimizeModelDealService
	 */
	public OptimizeModelDealService getOptimizeModelDealService()
	{
		return optimizeModelDealService;
	}

	/**
	 * @param optimizeModelDealService
	 *           the optimizeModelDealService to set
	 */
	public void setOptimizeModelDealService(final OptimizeModelDealService optimizeModelDealService)
	{
		this.optimizeModelDealService = optimizeModelDealService;
	}



	/**
	 * @return the optimizeCartService
	 */
	public OptimizeCartService getOptimizeCartService()
	{
		return (OptimizeCartService) super.getCartService();
	}





}
