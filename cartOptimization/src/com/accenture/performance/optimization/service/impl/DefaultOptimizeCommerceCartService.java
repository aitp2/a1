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

import de.hybris.platform.commerceservices.order.CommerceCartRestoration;
import de.hybris.platform.commerceservices.order.CommerceCartRestorationException;
import de.hybris.platform.commerceservices.order.impl.DefaultCommerceCartService;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.promotions.model.PromotionGroupModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.time.TimeService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;
import com.accenture.performance.optimization.service.OptimizeCartService;
import com.accenture.performance.optimization.service.OptimizeCommerceCartService;
import com.accenture.performance.optimization.service.OptimizePromotionService;


/**
 *
 */
public class DefaultOptimizeCommerceCartService extends DefaultCommerceCartService implements OptimizeCommerceCartService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultOptimizeCommerceCartService.class);

	public static final String SESSION_CART_PARAMETER_NAME = "cart";
	public static final String SESSION_OPTIMIZED_CART_PARAMETER_NAME = "optimizedcart";
	protected static final int APPEND_AS_LAST = -1;
	@Autowired
	private CalculationService calculationService;
	@Autowired
	private CommonI18NService commonI18NService;
	@Autowired
	private OptimizeCartService optimizeCartService;

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




	@Autowired
	private TimeService timeService;
	@Autowired
	private OptimizePromotionService promotionEngineService;


	@Override
	public boolean calculateCart(final OptimizedCartData optimizedCartData, final boolean enableHook)
	{

		//validateParameterNotNull(optimizedCartData, "Cart model cannot be null");
		boolean recalculated = false;

		try
		{
			optimizedCartData.setCalculated(Boolean.FALSE);
			this.beforeCalculate(optimizedCartData);
			this.calculate(optimizedCartData);
			promotionCalculate(optimizedCartData);
			//Promotion check
		}
		catch (final CalculationException calculationException)
		{
			throw new IllegalStateException(
					"Cart model " + optimizedCartData.getCode() + " was not calculated due to: " + calculationException.getMessage(),
					calculationException);
		}
		finally
		{
			this.afterCalculate(optimizedCartData);

		}
		recalculated = true;

		//if (calculateExternalTaxes)
		//{
		//	getExternalTaxesService().calculateExternalTaxes(cartModel);
		//}
		LOG.info("Calculated Cart Success. Total Price is ......" + optimizedCartData.getTotalPrice());

		getSessionService().setAttribute(SESSION_OPTIMIZED_CART_PARAMETER_NAME, optimizedCartData);
		return recalculated;
	}




	protected void beforeCalculate(final OptimizedCartData optimizedCartData)
	{
		// Empty method - extension point
	}

	protected void afterCalculate(final OptimizedCartData optimizedCartData)
	{
		// Empty method - extension point
	}

	protected Collection<PromotionGroupModel> getPromotionGroups()
	{
		final Collection<PromotionGroupModel> promotionGroupModels = new ArrayList<PromotionGroupModel>();
		if (getBaseSiteService().getCurrentBaseSite() != null
				&& getBaseSiteService().getCurrentBaseSite().getDefaultPromotionGroup() != null)
		{
			promotionGroupModels.add(getBaseSiteService().getCurrentBaseSite().getDefaultPromotionGroup());
		}
		return promotionGroupModels;
	}

	protected void promotionCalculate(final OptimizedCartData optimizedCartData)
	{
		getPromotionEngineService().updatePromotions(getPromotionGroups(), optimizedCartData, getTimeService().getCurrentTime());
	}

	protected void calculate(final OptimizedCartData optimizedCartData) throws CalculationException
	{
		calculateEntries(optimizedCartData, false);
		// -----------------------------
		// reset own values
		//final Map taxValueMap = resetAllValues(optimizedCartData);
		// -----------------------------
		// now calculate all totals
		calculateTotals(optimizedCartData, false);
	}

	public void calculateEntries(final OptimizedCartData order, final boolean forceRecalculate) throws CalculationException
	{
		double subtotal = 0.0;
		double totalPrice = 0.0;
		for (final OptimizedCartEntryData e : order.getEntries())
		{
			totalPrice = calculateOneEntries(e, e.getCalculated().booleanValue());

			//recalculateOrderEntryIfNeeded(e, forceRecalculate);
			subtotal += totalPrice;
		}
		order.setTotalPrice(Double.valueOf(subtotal));
		order.setSubtotal(Double.valueOf(subtotal));

	}

	@Override
	public double calculateOneEntries(final OptimizedCartEntryData entry, final boolean forceRecalculate)
			throws CalculationException
	{

		final int digits = this.getOptimizeCartService().getCurrentCurrencyDigit();
		final double totalPriceWithoutDiscount = commonI18NService
				.roundCurrency(entry.getBasePrice().doubleValue() * entry.getQuantity().doubleValue(), digits);
		//final double quantity = entry.getQuantity();
		/*
		 * apply discounts (will be rounded each) convert absolute discount values in case their currency doesn't match
		 * the order currency
		 */
		//YTODO : use CalculatinService methods to apply discounts
		//final List appliedDiscounts = DiscountValue.apply(quantity, totalPriceWithoutDiscount, digits,
		//		convertDiscountValues(order, entry.getDiscountValues()), curr.getIsocode());
		//entry.setDiscountValues(appliedDiscounts);
		final double totalPrice = totalPriceWithoutDiscount;
		//for (final Iterator it = appliedDiscounts.iterator(); it.hasNext();)
		//{
		//	totalPrice -= ((DiscountValue) it.next()).getAppliedValue();
		//}
		// set total price
		entry.setTotalPrice(Double.valueOf(totalPrice));
		// apply tax values too
		//YTODO : use CalculatinService methods to apply taxes
		//calculateTotalTaxValues(entry);
		entry.setCalculated(Boolean.TRUE);
		//setCalculatedStatus(entry);
		//getModelService().save(entry);
		//recalculateOrderEntryIfNeeded(e, forceRecalculate);
		return totalPrice;

	}


	public void calculateTotals(final OptimizedCartData order, final boolean recalculate)
	{

		final int digits = this.getOptimizeCartService().getCurrentCurrencyDigit();
		// subtotal
		final double subtotal = order.getSubtotal().doubleValue();
		// discounts

		//final double totalDiscounts = calculateDiscountValues(order, recalculate);
		//final double roundedTotalDiscounts = commonI18NService.roundCurrency(totalDiscounts, digits);
		//order.setTotalDiscounts(Double.valueOf(roundedTotalDiscounts));
		// set total
		final double paymentcost = order.getPaymentCost() == null ? 0d : order.getPaymentCost().doubleValue();
		final double deliveryCost = order.getDeliveryCost() == null ? 0d : order.getDeliveryCost().doubleValue();
		final double total = subtotal + paymentcost + deliveryCost;
		//		- roundedTotalDiscounts;
		final double totalRounded = commonI18NService.roundCurrency(total, digits);
		order.setTotalPrice(Double.valueOf(totalRounded));
		// taxes
		//final double totalTaxes = calculateTotalTaxValues(//
		//		order, recalculate, //
		//		digits, //
		//		getTaxCorrectionFactor(taxValueMap, subtotal, total, order), //
		//		taxValueMap);//
		//final double totalRoundedTaxes = commonI18NService.roundCurrency(totalTaxes, digits);
		//order.setTotalTax(Double.valueOf(totalRoundedTaxes));
		setCalculatedStatus(order);

		//saveOrder(order);

	}

	protected void setCalculatedStatus(final OptimizedCartData order)
	{
		order.setCalculated(Boolean.TRUE);
		final List<OptimizedCartEntryData> entries = order.getEntries();
		if (entries != null)
		{
			for (final OptimizedCartEntryData entry : entries)
			{

				entry.setCalculated(Boolean.TRUE);
			}
		}
	}



	/**
	 * @return the calculationService
	 */
	public CalculationService getCalculationService()
	{
		return calculationService;
	}


	/**
	 * @param calculationService
	 *           the calculationService to set
	 */
	public void setCalculationService(final CalculationService calculationService)
	{
		this.calculationService = calculationService;
	}

	@Override
	public CommerceCartRestoration restoreCart(final CommerceCartParameter parameters) throws CommerceCartRestorationException
	{
		return getCommerceCartRestorationStrategy().restoreCart(parameters);
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
	 * @return the timeService
	 */
	public TimeService getTimeService()
	{
		return timeService;
	}




	/**
	 * @param timeService
	 *           the timeService to set
	 */
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}




	/**
	 * @return the promotionEngineService
	 */
	public OptimizePromotionService getPromotionEngineService()
	{
		return promotionEngineService;
	}

	/**
	 * @param promotionEngineService
	 *           the promotionEngineService to set
	 */
	public void setPromotionEngineService(final OptimizePromotionService promotionEngineService)
	{
		this.promotionEngineService = promotionEngineService;
	}


}
