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
package com.accenture.performance.optimization.facades.populators;

import de.hybris.platform.commercefacades.order.EntryGroupData;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.PriceDataFactory;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.product.data.PromotionResultData;
import de.hybris.platform.commerceservices.constants.CommerceServicesConstants;
import de.hybris.platform.commerceservices.enums.DiscountType;
import de.hybris.platform.converters.Converters;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.GroupType;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotions.jalo.PromotionResult;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.promotions.result.PromotionOrderResults;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.util.DiscountValue;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.accenture.performance.optimization.data.OptimizedPromotionResultData;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;
import com.accenture.performance.optimization.ruleengineservices.result.OptimizedPromotionOrderResults;
import com.google.common.math.DoubleMath;


/**
 *
 */
public abstract class AbstractOptomizedCartPopulator<SOURCE extends OptimizedCartData, TARGET extends AbstractOrderData>
		implements Populator<SOURCE, TARGET>
{
	private static final Logger LOG = Logger.getLogger(AbstractOptomizedCartPopulator.class);
	private static final double EPSILON = 0.01d;

	private CommonI18NService commonI18NService;
	private PriceDataFactory priceDataFactory;
	private Converter<OptimizedCartEntryData, OrderEntryData> optimizedCartEntryConverter;
	private Converter<OptimizedPromotionResultData,PromotionResultData> optimizedPromotionResultConverter;
	private final Map<String, PriceData> priceData = new HashMap<String, PriceData>();

	protected Map<String, PriceData> getPriceData()
	{
		return priceData;
	}

	protected void addEntries(final OptimizedCartData source, final AbstractOrderData prototype)
	{
		prototype.setEntries(getOptimizedCartEntryConverter().convertAll(source.getEntries()));
	}

	protected void addEntryGroups(final OptimizedCartData source, final AbstractOrderData target)
	{
		if (CollectionUtils.isNotEmpty(target.getEntries()))
		{
			final List<EntryGroupData> entryGroupList = new ArrayList<>();
			final EntryGroupData entryGroupData = new EntryGroupData();
			entryGroupData.setGroupNumber(Integer.valueOf(10001));
			entryGroupData.setGroupType(GroupType.valueOf("P"));
			entryGroupData.setOrderEntries(target.getEntries());
			entryGroupList.add(entryGroupData);
			target.setRootGroups(entryGroupList);
		}
	}


	/*
	 * Adds applied and potential promotions.
	 */
	protected void addPromotions(final OptimizedCartData source, final AbstractOrderData prototype)
	{
		addPromotions(source, source.getAllPromotionResults(), prototype);
	}
	
	protected void addPromotions(final OptimizedCartData source, final List<OptimizedPromotionResultData> promoOrderResults,
			final AbstractOrderData prototype)
	{
		final double quoteDiscountsAmount = getQuoteDiscountsAmount(source);
		prototype.setQuoteDiscounts(createPrice(source, Double.valueOf(quoteDiscountsAmount)));

		final Pair<DiscountType, Double> quoteDiscountsTypeAndRate = getQuoteDiscountsTypeAndRate(source);
		prototype.setQuoteDiscountsType(quoteDiscountsTypeAndRate.getKey().getCode());
		prototype.setQuoteDiscountsRate(quoteDiscountsTypeAndRate.getValue());

		if (promoOrderResults != null)
		{
			final double productsDiscountsAmount = getProductsDiscountsAmount(source);
			final double orderDiscountsAmount = getOrderDiscountsAmount(source);

			prototype.setProductDiscounts(createPrice(source, Double.valueOf(productsDiscountsAmount)));
			prototype.setOrderDiscounts(createPrice(source, Double.valueOf(orderDiscountsAmount)));
			prototype.setTotalDiscounts(createPrice(source, Double.valueOf(productsDiscountsAmount + orderDiscountsAmount)));
			prototype.setTotalDiscountsWithQuoteDiscounts(createPrice(source,
					Double.valueOf(productsDiscountsAmount + orderDiscountsAmount + quoteDiscountsAmount)));
			
			OptimizedPromotionOrderResults optPromoOrderResults = new OptimizedPromotionOrderResults(null, null, null, source, promoOrderResults,0.0D);
			prototype.setAppliedOrderPromotions( Converters.convertAll( optPromoOrderResults.getOptimizedAppliedOrderPromotions(), getOptimizedPromotionResultConverter()) );
			prototype.setAppliedProductPromotions(Converters.convertAll(optPromoOrderResults.getOptimizedAppliedProductPromotions(),getOptimizedPromotionResultConverter()));
		}
		
	}
	

	protected double getProductsDiscountsAmount(final OptimizedCartData source)
	{
		double discounts = 0.0d;

		final List<OptimizedCartEntryData> entries = source.getEntries();
		if (entries != null)
		{
			for (final OptimizedCartEntryData entry : entries)
			{
				final List<DiscountValue> discountValues = entry.getDiscountList();
				if (discountValues != null)
				{
					for (final DiscountValue dValue : discountValues)
					{
						discounts += dValue.getAppliedValue();
					}
				}
			}
		}
		return discounts;
	}

	protected double getOrderDiscountsAmount(final AbstractOrderModel source)
	{
		double discounts = 0.0d;
		final List<DiscountValue> discountList = source.getGlobalDiscountValues(); // discounts on the cart itself
		if (discountList != null && !discountList.isEmpty())
		{
			for (final DiscountValue discount : discountList)
			{
				final double value = discount.getAppliedValue();
				if (DoubleMath.fuzzyCompare(value, 0, EPSILON) > 0
						&& !CommerceServicesConstants.QUOTE_DISCOUNT_CODE.equals(discount.getCode()))
				{
					discounts += value;
				}
			}
		}
		return discounts;
	}

	protected double getQuoteDiscountsAmount(final OptimizedCartData source)
	{
		double discounts = 0.0d;
		final List<DiscountValue> discountList = source.getGlobalDiscountValues(); // discounts on the cart itself
		if (discountList != null && !discountList.isEmpty())
		{
			for (final DiscountValue discount : discountList)
			{
				final double value = discount.getAppliedValue();
				if (DoubleMath.fuzzyCompare(value, 0, EPSILON) > 0
						&& CommerceServicesConstants.QUOTE_DISCOUNT_CODE.equals(discount.getCode()))
				{
					discounts += value;
				}
			}
		}
		return discounts;
	}

	protected Pair<DiscountType, Double> getQuoteDiscountsTypeAndRate(final OptimizedCartData source)
	{
		double discounts = 0.0d;
		DiscountType discountType = DiscountType.PERCENT;
		final List<DiscountValue> discountList = source.getGlobalDiscountValues(); // discounts on the cart itself
		if (discountList != null && !discountList.isEmpty())
		{
			for (final DiscountValue discount : discountList)
			{
				final double value = discount.getAppliedValue();
				if (DoubleMath.fuzzyCompare(value, 0, EPSILON) > 0
						&& CommerceServicesConstants.QUOTE_DISCOUNT_CODE.equals(discount.getCode()))
				{
					// for now there is only one quote discount entry
					discounts = discount.getValue();
					if (discount.isAsTargetPrice())
					{
						discountType = DiscountType.TARGET;
					}
					else if (discount.isAbsolute())
					{
						discountType = DiscountType.ABSOLUTE;
					}
					break;
				}
			}
		}
		return Pair.of(discountType, Double.valueOf(discounts));
	}


	protected void addTotals(final OptimizedCartData source, final AbstractOrderData prototype)
	{
		final double orderDiscountsAmount = getOrderDiscountsAmount(source);
		//final double quoteDiscountsAmount = getQuoteDiscountsAmount(source);

		prototype.setTotalPrice(createPrice(source, source.getTotalPrice()));
		prototype.setTotalTax(createPrice(source, source.getTotalTax()));
		final double subTotal = source.getSubtotal().doubleValue() - orderDiscountsAmount;
		final PriceData subTotalPriceData = createPrice(source, Double.valueOf(subTotal));
		prototype.setSubTotal(subTotalPriceData);
		prototype.setSubTotalWithoutQuoteDiscounts(createPrice(source, Double.valueOf(subTotal)));
		prototype.setDeliveryCost(source.getDeliveryMode() != null ? createPrice(source, source.getDeliveryCost()) : null);
		//prototype.setTotalPriceWithTax((createPrice(source, calcTotalWithTax(source))));
	}

	protected PriceData createPrice(final OptimizedCartData source, final Double val)
	{
		if (source == null)
		{
			throw new IllegalArgumentException("source order must not be null");
		}

		final CurrencyModel currency = commonI18NService.getCurrency(source.getCurrencyCode());
		if (currency == null)
		{
			throw new IllegalArgumentException("source order currency must not be null");
		}

		// Get double value, handle null as zero
		final double priceValue = val != null ? val.doubleValue() : 0d;

		return getPriceDataFactory().create(PriceDataType.BUY, BigDecimal.valueOf(priceValue), currency);
	}

	protected PriceData createZeroPrice()
	{
		final String key = getCommonI18NService().getCurrentCurrency().getIsocode();
		if (getPriceData().containsKey(key))
		{
			return getPriceData().get(key);
		}
		else
		{
			final PriceData priceData = getPriceDataFactory().create(PriceDataType.BUY, BigDecimal.ZERO,
					getCommonI18NService().getCurrentCurrency());
			getPriceData().put(key, priceData);
			return priceData;
		}
	}

	protected double getOrderDiscountsAmount(final OptimizedCartData source)
	{
		double discounts = 0.0d;
		final List<DiscountValue> discountList = source.getGlobalDiscountValues(); // discounts on the cart itself
		if (discountList != null && !discountList.isEmpty())
		{
			for (final DiscountValue discount : discountList)
			{
				final double value = discount.getAppliedValue();
				if (DoubleMath.fuzzyCompare(value, 0, EPSILON) > 0
						&& !CommerceServicesConstants.QUOTE_DISCOUNT_CODE.equals(discount.getCode()))
				{
					discounts += value;
				}
			}
		}
		return discounts;
	}

	protected void addCommon(final OptimizedCartData source, final AbstractOrderData prototype)
	{
		prototype.setCode(source.getCode());
		//prototype.setName(source.getName());
		//prototype.setDescription(source.getDescription());
		//prototype.setExpirationTime(source.getExpirationTime());
		prototype.setSite(source.getBaseSite());

		prototype.setStore(source.getBaseStore());

		//prototype.setNet(Boolean.TRUE.equals(source.getNet()));
		prototype.setGuid(source.getGuid());
		prototype.setCalculated(Boolean.TRUE.equals(source.getCalculated()));
		if (!CollectionUtils.isEmpty(source.getEntries()))
		{
			prototype.setTotalItems(calcTotalItems(source));
			prototype.setTotalUnitCount(calcTotalUnitCount(source));
		}
	}

	protected Integer calcTotalItems(final OptimizedCartData source)
	{
		return Integer.valueOf(source.getEntries().size());
	}

	protected Integer calcTotalUnitCount(final OptimizedCartData source)
	{
		int totalUnitCount = 0;
		for (final OptimizedCartEntryData orderEntryModel : source.getEntries())
		{
			totalUnitCount += orderEntryModel.getQuantity().intValue();
		}
		return Integer.valueOf(totalUnitCount);
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
	 * @return the priceDataFactory
	 */
	public PriceDataFactory getPriceDataFactory()
	{
		return priceDataFactory;
	}

	/**
	 * @param priceDataFactory
	 *           the priceDataFactory to set
	 */
	public void setPriceDataFactory(final PriceDataFactory priceDataFactory)
	{
		this.priceDataFactory = priceDataFactory;
	}

	/**
	 * @return the optimizedCartEntryConverter
	 */
	public Converter<OptimizedCartEntryData, OrderEntryData> getOptimizedCartEntryConverter()
	{
		return optimizedCartEntryConverter;
	}

	/**
	 * @param optimizedCartEntryConverter
	 *           the optimizedCartEntryConverter to set
	 */
	public void setOptimizedCartEntryConverter(final Converter<OptimizedCartEntryData, OrderEntryData> optimizedCartEntryConverter)
	{
		this.optimizedCartEntryConverter = optimizedCartEntryConverter;
	}

	/**
	 * @return the optimizedPromotionResultConverter
	 */
	public Converter<OptimizedPromotionResultData, PromotionResultData> getOptimizedPromotionResultConverter() {
		return optimizedPromotionResultConverter;
	}

	/**
	 * @param optimizedPromotionResultConverter the optimizedPromotionResultConverter to set
	 */
	public void setOptimizedPromotionResultConverter(
			Converter<OptimizedPromotionResultData, PromotionResultData> optimizedPromotionResultConverter) {
		this.optimizedPromotionResultConverter = optimizedPromotionResultConverter;
	}

}
