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

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;
import com.accenture.performance.optimization.service.OptimizeCartService;
import com.accenture.performance.optimization.service.OptimizeCommerceCartService;
import com.accenture.performance.optimization.service.OptimizeDeliveyService;
import com.accenture.performance.optimization.service.OptimizePromotionService;

import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.order.CommerceCartRestoration;
import de.hybris.platform.commerceservices.order.CommerceCartRestorationException;
import de.hybris.platform.commerceservices.order.impl.DefaultCommerceCartService;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.promotions.model.PromotionGroupModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;
import de.hybris.platform.util.PriceValue;
import de.hybris.platform.util.TaxValue;


/**
 *
 */
public class DefaultOptimizeCommerceCartService extends DefaultCommerceCartService implements OptimizeCommerceCartService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultOptimizeCommerceCartService.class);

	protected static final int APPEND_AS_LAST = -1;
	@Autowired
	private CalculationService calculationService;
	@Autowired
	private CommonI18NService commonI18NService;
	@Autowired
	private OptimizeCartService optimizeCartService;
	
	private OptimizeDeliveyService optimizeDeliveyService;
	
	private CustomerAccountService customerAccountService;
	
	private UserService userService;
	
	private FlexibleSearchService flexibleSearchService;
	
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
	public void recalculateCart(final CommerceCartParameter parameters)
	{
		validateParameterNotNull(parameters.getOptimizeCart(), "Cart model cannot be null");
		calculateCart(parameters.getOptimizeCart(), true);
	}

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
		getOptimizeCartService().setSessionOptimizedCart(optimizedCartData);
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
		resetAllValues(optimizedCartData);
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
			if(!Boolean.TRUE.equals(e.getPromomtionGiftEntry()))
			{
				totalPrice = calculateOneEntries(e, e.getCalculated().booleanValue());

				//recalculateOrderEntryIfNeeded(e, forceRecalculate);
				subtotal += totalPrice;
			}
			
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

	/**
	 * @see de.hybris.platform.order.impl.DefaultCalculationService#resetAllValues(final AbstractOrderModel order)
	 */
	protected Map resetAllValues(final OptimizedCartData order) throws CalculationException
	{
		// -----------------------------
		// set subtotal and get tax value map
		//final Map<TaxValue, Map<Set<TaxValue>, Double>> taxValueMap = calculateSubtotal(order, false); //TODO acn later
		final Map<TaxValue, Map<Set<TaxValue>, Double>> taxValueMap = new HashMap<>();
		/*
		 * filter just relative tax values - payment and delivery prices might require conversion using taxes -> absolute
		 * taxes do not apply here TODO: ask someone for absolute taxes and how they apply to delivery cost etc. - this
		 * implementation might be wrong now
		 */
		final Collection<TaxValue> relativeTaxValues = new LinkedList<TaxValue>();
		for (final Map.Entry<TaxValue, ?> e : taxValueMap.entrySet())
		{
			final TaxValue taxValue = e.getKey();
			if (!taxValue.isAbsolute())
			{
				relativeTaxValues.add(taxValue);
			}
		}

		//PLA-10914
		final boolean setAdditionalCostsBeforeDiscounts = Config.getBoolean(
				"ordercalculation.reset.additionalcosts.before.discounts", true);
		if (setAdditionalCostsBeforeDiscounts)
		{
			resetAdditionalCosts(order, relativeTaxValues);
		}
		// -----------------------------
		// set discount values ( not applied yet ) - dont needed in model domain (?)
		//removeAllGlobalDiscountValues();
		
		//order.setGlobalDiscountValues(findGlobalDiscounts(order));//TODO acn
		// -----------------------------
		// set delivery costs - convert if net or currency is different

		if (!setAdditionalCostsBeforeDiscounts)
		{
			resetAdditionalCosts(order, relativeTaxValues);
		}

		return taxValueMap;

	}

	protected void resetAdditionalCosts(final OptimizedCartData order, final Collection<TaxValue> relativeTaxValues)
	{
		final PriceValue deliCost = getDeliveryCost(order);//see de.hybris.platform.order.strategies.calculation.impl.DefaultFindDeliveryCostStrategy#getDeliveryCost(final AbstractOrderModel order)
		double deliveryCostValue = 0.0;
		if (deliCost != null)
		{
			deliveryCostValue = convertPriceIfNecessary(deliCost, order.isNet(), getCommonI18NService().getCurrency(order.getCurrencyCode()) , relativeTaxValues).getValue();
		}
		
		order.setDeliveryCost(Double.valueOf(deliveryCostValue));
		// -----------------------------
		// set payment cost - convert if net or currency is different
		//TODO acn
//		final PriceValue payCost = findPaymentCostStrategy.getPaymentCost(order);
//		double paymentCostValue = 0.0;
//		if (payCost != null)
//		{
//			paymentCostValue = convertPriceIfNecessary(payCost, order.getNet().booleanValue(), order.getCurrency(),
//					relativeTaxValues).getValue();
//		}
//		order.setPaymentCost(Double.valueOf(paymentCostValue));
	}

	protected PriceValue getDeliveryCost(final OptimizedCartData cart)
	{
		if(StringUtils.isBlank(cart.getDeliveryMode()))
		{
			return new PriceValue(cart.getCurrencyCode(), 0.0, cart.isNet());
		}
		
		final DeliveryModeModel deliveryMode = optimizeDeliveyService.getDeliveryModeForCode(cart.getDeliveryMode());
		
		AbstractOrderModel abstractOrder = new CartModel();
		
		CustomerModel currentCustomer = (CustomerModel)userService.getCurrentUser();
		AddressModel deliverAddress = getCustomerAccountService().getAddressForCode(currentCustomer, cart.getDeliveryAddress().getId());
		abstractOrder.setDeliveryAddress(deliverAddress);

		CurrencyModel currency = getCommonI18NService().getCurrency(cart.getCurrencyCode());
		abstractOrder.setCurrency(currency);
		
		abstractOrder.setNet(Boolean.valueOf(cart.isNet()));
		abstractOrder.setSubtotal(cart.getSubtotal());
		
		return optimizeDeliveyService.getOptimizeDeliveryCostForDeliveryModeAndAbstractOrder(deliveryMode, abstractOrder);
	}

	/**
	 * converts a PriceValue object into a double matching the target currency and net/gross - state if necessary. this
	 * is the case when the given price value has a different net/gross flag or different currency.
	 *
	 * @param pv
	 *           the base price to convert
	 * @param toNet
	 *           the target net/gross state
	 * @param toCurrency
	 *           the target currency
	 * @param taxValues
	 *           the collection of tax values which apply to this price
	 *
	 * @return a new PriceValue containing the converted price or pv in case no conversion was necessary
	 */
	//YTODO: refactor to some helper class
	public PriceValue convertPriceIfNecessary(final PriceValue pv, final boolean toNet, final CurrencyModel toCurrency,	final Collection taxValues)
	{
		// net - gross - conversion
		double convertedPrice = pv.getValue();
		if (pv.isNet() != toNet)
		{
			convertedPrice = pv.getOtherPrice(taxValues).getValue();
			convertedPrice = commonI18NService.roundCurrency(convertedPrice, toCurrency.getDigits().intValue());
		}
		// currency conversion
		final String iso = pv.getCurrencyIso();
		if (iso != null && !iso.equals(toCurrency.getIsocode()))
		{
			try
			{
				final CurrencyModel basePriceCurrency = commonI18NService.getCurrency(iso);
				convertedPrice = commonI18NService.convertAndRoundCurrency(basePriceCurrency.getConversion().doubleValue(),
						toCurrency.getConversion().doubleValue(), toCurrency.getDigits().intValue(), convertedPrice);
			}
			catch (final UnknownIdentifierException e)
			{
				LOG.warn("Cannot convert from currency '" + iso + "' to currency '" + toCurrency.getIsocode() + "' since '" + iso
						+ "' doesn't exist any more - ignored");
			}
		}
		return new PriceValue(toCurrency.getIsocode(), convertedPrice, toNet);
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


	@Override
	public CommerceCartRestoration restoreCart(final CommerceCartParameter parameters) throws CommerceCartRestorationException
	{
		final OptimizedCartData cartModel = parameters.getOptimizeCart();
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

	/**
	 * @return the optimizeDeliveyService
	 */
	public OptimizeDeliveyService getOptimizeDeliveyService() {
		return optimizeDeliveyService;
	}




	/**
	 * @param optimizeDeliveyService the optimizeDeliveyService to set
	 */
	public void setOptimizeDeliveyService(OptimizeDeliveyService optimizeDeliveyService) {
		this.optimizeDeliveyService = optimizeDeliveyService;
	}


	/**
	 * @return the customerAccountService
	 */
	public CustomerAccountService getCustomerAccountService() {
		return customerAccountService;
	}




	/**
	 * @param customerAccountService the customerAccountService to set
	 */
	public void setCustomerAccountService(CustomerAccountService customerAccountService) {
		this.customerAccountService = customerAccountService;
	}




	/**
	 * @return the userService
	 */
	public UserService getUserService() {
		return userService;
	}




	/**
	 * @param userService the userService to set
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	/**
	 * @return the flexibleSearchService
	 */
	public FlexibleSearchService getFlexibleSearchService() {
		return flexibleSearchService;
	}

	/**
	 * @param flexibleSearchService the flexibleSearchService to set
	 */
	public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
		this.flexibleSearchService = flexibleSearchService;
	}


}
