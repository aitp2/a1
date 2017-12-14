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

import de.hybris.platform.commerceservices.i18n.CommerceCommonI18NService;
import de.hybris.platform.commerceservices.order.CommerceAddToCartStrategy;
import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.CommerceCartRestoration;
import de.hybris.platform.commerceservices.order.CommerceCartRestorationException;
import de.hybris.platform.commerceservices.order.impl.DefaultCommerceCartRestorationStrategy;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.site.BaseSiteService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;
import com.accenture.performance.optimization.service.OptimizeCartService;
import com.accenture.performance.optimization.service.OptimizeCommerceCartService;
import com.accenture.performance.optimization.service.OptimizeModelDealService;


/**
 *
 */
public class DefaultCommerceOptimizedCartRestorationStrategy extends DefaultCommerceCartRestorationStrategy
{
	private static final Logger LOG = Logger.getLogger(DefaultCommerceOptimizedCartRestorationStrategy.class);

	private static final int DEFAULT_CART_VALIDITY_PERIOD = 12960000;
	private int cartValidityPeriod = DEFAULT_CART_VALIDITY_PERIOD;
	//private CartFactory cartFactory;
	private TimeService timeService;
	private KeyGenerator guidKeyGenerator;
	private BaseSiteService baseSiteService;
	private CommerceCommonI18NService commerceCommonI18NService;
	private CommerceAddToCartStrategy commerceAddToCartStrategy;

	private SessionService sessionService;
	@Autowired
	private OptimizeCartFactory cartFactory;
	private OptimizeModelDealService optimizeModelDealService;
	private OptimizeCommerceCartService optimizeCommerceCartService;
	private OptimizeCartService optimizeCartService;
	private ModelService modelService;
	public static final String SESSION_OPTIMIZED_CART_PARAMETER_NAME = "optimizedcart";



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
					try
					{
						optimizeCommerceCartService.calculateCart(parameter);
					}
					catch (final IllegalStateException ex)
					{
						LOG.error("Failed to recalculate order [" + cartModel.getCode() + "]", ex);
					}

					//getCartService().setAttribute(cartModel);
					getSessionService().setAttribute(SESSION_OPTIMIZED_CART_PARAMETER_NAME, cartModel);

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
				optimizeCommerceCartService.calculateCart(parameter);
				//getCommerceCartCalculationStrategy().calculateCart(parameter);
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

			final CommerceCartModification modification = optimizeCartService.doAddToCart(newCartParameter);
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
		final OptimizedCartData newCartData = cartFactory.createSessionCart();
		LOG.info("===========user of new cart=========" + newCartData.getUserId());
		LOG.info("===========user of parameter cart=========" + parameter.getOptimizeCart().getUserId());

		//if (!parameter.getCart().equals(newCart))
		//{
		rewriteEntriesFromCartToCart(parameter, parameter.getOptimizeCart(), newCartData, modifications);

		newCartData.setCalculated(Boolean.FALSE);
		getSessionService().setAttribute(SESSION_OPTIMIZED_CART_PARAMETER_NAME, newCartData);
		getOptimizeModelDealService().removeCurrentSessionCart(parameter.getOptimizeCart());
		//}
		return modifications;
	}



	/**
	 * @return the cartValidityPeriod
	 */
	@Override
	public int getCartValidityPeriod()
	{
		return cartValidityPeriod;
	}


	/**
	 * @param cartValidityPeriod
	 *           the cartValidityPeriod to set
	 */
	@Override
	public void setCartValidityPeriod(final int cartValidityPeriod)
	{
		this.cartValidityPeriod = cartValidityPeriod;
	}


	/**
	 * @return the timeService
	 */
	@Override
	public TimeService getTimeService()
	{
		return timeService;
	}


	/**
	 * @param timeService
	 *           the timeService to set
	 */
	@Override
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}


	/**
	 * @return the guidKeyGenerator
	 */
	@Override
	public KeyGenerator getGuidKeyGenerator()
	{
		return guidKeyGenerator;
	}


	/**
	 * @param guidKeyGenerator
	 *           the guidKeyGenerator to set
	 */
	@Override
	public void setGuidKeyGenerator(final KeyGenerator guidKeyGenerator)
	{
		this.guidKeyGenerator = guidKeyGenerator;
	}


	/**
	 * @return the baseSiteService
	 */
	@Override
	public BaseSiteService getBaseSiteService()
	{
		return baseSiteService;
	}


	/**
	 * @param baseSiteService
	 *           the baseSiteService to set
	 */
	@Override
	public void setBaseSiteService(final BaseSiteService baseSiteService)
	{
		this.baseSiteService = baseSiteService;
	}


	/**
	 * @return the commerceCommonI18NService
	 */
	@Override
	public CommerceCommonI18NService getCommerceCommonI18NService()
	{
		return commerceCommonI18NService;
	}


	/**
	 * @param commerceCommonI18NService
	 *           the commerceCommonI18NService to set
	 */
	@Override
	public void setCommerceCommonI18NService(final CommerceCommonI18NService commerceCommonI18NService)
	{
		this.commerceCommonI18NService = commerceCommonI18NService;
	}


	/**
	 * @return the commerceAddToCartStrategy
	 */
	@Override
	public CommerceAddToCartStrategy getCommerceAddToCartStrategy()
	{
		return commerceAddToCartStrategy;
	}


	/**
	 * @param commerceAddToCartStrategy
	 *           the commerceAddToCartStrategy to set
	 */
	@Override
	public void setCommerceAddToCartStrategy(final CommerceAddToCartStrategy commerceAddToCartStrategy)
	{
		this.commerceAddToCartStrategy = commerceAddToCartStrategy;
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
		return cartFactory;
	}

	/**
	 * @param cartFactory
	 *           the cartFactory to set
	 */
	public void setCartFactory(final OptimizeCartFactory cartFactory)
	{
		this.cartFactory = cartFactory;
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
	 * @return the optimizeCommerceCartService
	 */
	public OptimizeCommerceCartService getOptimizeCommerceCartService()
	{
		return optimizeCommerceCartService;
	}

	/**
	 * @param optimizeCommerceCartService
	 *           the optimizeCommerceCartService to set
	 */
	public void setOptimizeCommerceCartService(final OptimizeCommerceCartService optimizeCommerceCartService)
	{
		this.optimizeCommerceCartService = optimizeCommerceCartService;
	}

	/**
	 * @return the modelService
	 */
	@Override
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	@Override
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @return the optimizeCartService
	 */
	public OptimizeCartService getOptimizeCartService()
	{
		return optimizeCartService;
	}

	/**
	 * @param optimizeCartService
	 *           the optimizeCartService to set
	 */
	public void setOptimizeCartService(final OptimizeCartService optimizeCartService)
	{
		this.optimizeCartService = optimizeCartService;
	}




}
