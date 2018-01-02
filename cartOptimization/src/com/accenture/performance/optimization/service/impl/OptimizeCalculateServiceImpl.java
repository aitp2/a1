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

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.order.impl.DefaultCalculationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.util.DiscountValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;
import com.accenture.performance.optimization.service.OptimizeCalculateService;


/**
 *
 */
public class OptimizeCalculateServiceImpl extends DefaultCalculationService implements OptimizeCalculateService
{

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
	@Override
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		super.setCommonI18NService(commonI18NService);
		this.commonI18NService = commonI18NService;
	}

	@Override
	public void calculateTotals(final OptimizedCartData order, final boolean recalculate) throws CalculationException
	{
		calculateSubtotal(order, recalculate);

		//if (recalculate || orderRequiresCalculationStrategy.requiresCalculation(order))
		if (recalculate)
		{
			final CurrencyModel curr = this.getCommonI18NService().getCurrentCurrency();
			final int digits = curr.getDigits().intValue();
			// subtotal
			final double subtotal = order.getSubtotal().doubleValue();
			// discounts
			final double totalDiscounts = calculateDiscountValues(order, recalculate);
			final double roundedTotalDiscounts = commonI18NService.roundCurrency(totalDiscounts, digits);
			order.setTotalDiscounts(Double.valueOf(roundedTotalDiscounts));
			// set total
			final double total = subtotal + getDoublePrice(order.getPaymentCost()) + getDoublePrice(order.getDeliveryCost())
					- roundedTotalDiscounts;
			final double totalRounded = commonI18NService.roundCurrency(total, digits);
			order.setTotalPrice(Double.valueOf(totalRounded));
			// taxes
			//			final double totalTaxes = calculateTotalTaxValues(//
			//					order, recalculate, //
			//					digits, //
			//					getTaxCorrectionFactor(taxValueMap, subtotal, total, order), //
			//					taxValueMap);//
			//			final double totalRoundedTaxes = commonI18NService.roundCurrency(totalTaxes, digits);
			//order.setTotalTax(Double.valueOf(totalRoundedTaxes));
			setCalculatedStatus(order);
			//			saveOrder(order);
		}
	}

	protected void calculateSubtotal(final OptimizedCartData order, final boolean recalculate)
	{
		//if (recalculate || orderRequiresCalculationStrategy.requiresCalculation(order))
		if (recalculate)
		{
			double subtotal = 0.0;
			// entry grouping via map { tax code -> Double }
			final List<OptimizedCartEntryData> entries = order.getEntries();
			//			final Map<TaxValue, Map<Set<TaxValue>, Double>> taxValueMap = new LinkedHashMap<TaxValue, Map<Set<TaxValue>, Double>>(
			//					entries.size() * 2);

			for (final OptimizedCartEntryData entry : entries)
			{
				if(!Boolean.TRUE.equals(entry.getPromomtionGiftEntry()))
				{
					calculateEntryTotals(entry, recalculate);
					final double entryTotal = entry.getTotalPrice().doubleValue();
					subtotal += entryTotal;
					// use un-applied version of tax values!!!
					//				final Collection<TaxValue> allTaxValues = entry.getTaxValues();
					//				final Set<TaxValue> relativeTaxGroupKey = getUnappliedRelativeTaxValues(allTaxValues);
					//				for (final TaxValue taxValue : allTaxValues)
					//				{
					//					if (taxValue.isAbsolute())
					//					{
					//						addAbsoluteEntryTaxValue(entry.getQuantity().longValue(), taxValue.unapply(), taxValueMap);
					//					}
					//					else
					//					{
					//						addRelativeEntryTaxValue(entryTotal, taxValue.unapply(), relativeTaxGroupKey, taxValueMap);
					//					}
					//				}
				}
				
			}
			// store subtotal
			subtotal = commonI18NService.roundCurrency(subtotal,
					this.getCommonI18NService().getCurrentCurrency().getDigits().intValue());
			order.setSubtotal(Double.valueOf(subtotal));
			//			return taxValueMap;
		}
		//		return Collections.EMPTY_MAP;
	}

	@Override
	public void calculateEntryTotals(final OptimizedCartEntryData entry, final boolean recalculate)
	{
		if (recalculate)
		{
			final OptimizedCartData order = entry.getCartData();
			final CurrencyModel curr = this.getCommonI18NService().getCurrentCurrency();
			final int digits = curr.getDigits().intValue();
			final double totalPriceWithoutDiscount = commonI18NService
					.roundCurrency(entry.getBasePrice().doubleValue() * entry.getQuantity().longValue(), digits);
			final double quantity = entry.getQuantity().doubleValue();
			/*
			 * apply discounts (will be rounded each) convert absolute discount values in case their currency doesn't match
			 * the order currency
			 */
			//YTODO : use CalculatinService methods to apply discounts
			final List appliedDiscounts = DiscountValue.apply(quantity, totalPriceWithoutDiscount, digits,
					convertDiscountValues(order, entry.getDiscountList()), curr.getIsocode());
			entry.setDiscountList(appliedDiscounts);
			double totalPrice = totalPriceWithoutDiscount;
			for (final Iterator it = appliedDiscounts.iterator(); it.hasNext();)
			{
				totalPrice -= ((DiscountValue) it.next()).getAppliedValue();
			}
			// set total price
			entry.setTotalPrice(Double.valueOf(totalPrice));
			// apply tax values too
			//YTODO : use CalculatinService methods to apply taxes
			//			calculateTotalTaxValues(entry);
			setCalculatedStatus(entry);
			//			getModelService().save(entry);

		}
	}

	protected void setCalculatedStatus(final OptimizedCartEntryData entry)
	{
		entry.setCalculated(Boolean.TRUE);
	}

	/**
	 *
	 */
	private double getDoublePrice(final Double value)
	{
		return value == null ? 0 : value.doubleValue();
	}

	/**
	 *
	 */
	private void setCalculatedStatus(final OptimizedCartData order)
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
	 *
	 */
	private double calculateDiscountValues(final OptimizedCartData order, final boolean recalculate)
	{
		//if (recalculate || orderRequiresCalculationStrategy.requiresCalculation(order))
		if (recalculate)
		{
			final List<DiscountValue> discountValues = order.getGlobalDiscountValues();
			if (discountValues != null && !discountValues.isEmpty())
			{
				// clean discount value list -- do we still need it?
				//				removeAllGlobalDiscountValues();
				//
				final CurrencyModel curr = this.getCommonI18NService().getCurrentCurrency();
				final String iso = curr.getIsocode();

				final int digits = curr.getDigits().intValue();
				final double discountablePrice = order.getSubtotal().doubleValue();
				// TODO : isDiscountsIncludeDeliveryCost isDiscountsIncludePaymentCost
				//						+ (order.isDiscountsIncludeDeliveryCost() ? order.getDeliveryCost().doubleValue() : 0.0)
				//						+ (order.isDiscountsIncludePaymentCost() ? order.getPaymentCost().doubleValue() : 0.0);

				/*
				 * apply discounts to this order's total
				 */
				final List appliedDiscounts = DiscountValue.apply(1.0, discountablePrice, digits,
						convertDiscountValues(order, discountValues), iso);
				// store discount values
				order.setGlobalDiscountValues(appliedDiscounts);
				return DiscountValue.sumAppliedValues(appliedDiscounts);
			}
			return 0.0;
		}
		else
		{
			return DiscountValue.sumAppliedValues(order.getGlobalDiscountValues());
		}
	}

	/**
	 *
	 */
	private List convertDiscountValues(final OptimizedCartData order, final List<DiscountValue> dvs)
	{
		if (dvs == null)
		{
			return Collections.EMPTY_LIST;
		}
		if (dvs.isEmpty())
		{
			return dvs;
		}

		final CurrencyModel curr = this.getCommonI18NService().getCurrentCurrency();
		final String iso = curr.getIsocode();
		final List tmp = new ArrayList(dvs);
		/*
		 * convert absolute discount values to order currency is needed
		 */
		final Map<String, CurrencyModel> currencyMap = new HashMap<String, CurrencyModel>(); // just don't search twice for an isocode
		for (int i = 0; i < tmp.size(); i++)
		{
			final DiscountValue discountValue = (DiscountValue) tmp.get(i);
			if (discountValue.isAbsolute() && !iso.equals(discountValue.getCurrencyIsoCode()))
			{
				// get currency
				CurrencyModel dCurr = currencyMap.get(discountValue.getCurrencyIsoCode());
				if (dCurr == null)
				{
					currencyMap.put(discountValue.getCurrencyIsoCode(),
							dCurr = commonI18NService.getCurrency(discountValue.getCurrencyIsoCode()));
				}
				// replace old value in temp list
				tmp.set(i,
						new DiscountValue(discountValue.getCode(),
								commonI18NService.convertAndRoundCurrency(dCurr.getConversion().doubleValue(),
										curr.getConversion().doubleValue(), curr.getDigits().intValue(), discountValue.getValue()),
								true, iso));
			}
		}
		return tmp;
	}

}
