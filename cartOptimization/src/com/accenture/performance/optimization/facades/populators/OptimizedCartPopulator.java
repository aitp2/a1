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

import de.hybris.platform.converters.Populator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.model.OptimizedCartModel;


/**
 *
 */
public class OptimizedCartPopulator<T extends OptimizedCartData> implements Populator<OptimizedCartModel, T>
{
	private static final Logger LOG = LoggerFactory.getLogger(OptimizedCartPopulator.class);

	@Override
	public void populate(final OptimizedCartModel source, final T target)
	{
		target.setCode(source.getCode());
		target.setUserId(source.getUserId());
		target.setBaseSite(source.getSite().getUid());
		target.setBaseStore(source.getStore().getUid());
		target.setGuid(source.getGuid());
		target.setTotalDiscounts(source.getTotalDiscounts());
		target.setTotalPrice(source.getTotalPrice());
		target.setTotalTax(source.getTotalTax());
		target.setSubtotal(source.getSubtotal());
		target.setCalculated(Boolean.FALSE);
		target.setDeliveryCost(source.getDeliveryCost());
		target.setDeliveryMode(source.getDeliveryMode() == null ? "" : source.getDeliveryMode().getCode());
		target.setPaymentCost(source.getPaymentCost());

		//		final List<OptimizedCartEntryData> entryDataList = new ArrayList<OptimizedCartEntryData>();
		//		if (source.getEntries() != null || !source.getEntries().isEmpty())
		//		{
		//			for (final OptimizedCartEntryModel entryModel : source.getEntries())
		//			{
		//				final OptimizedCartEntryData entryData = new OptimizedCartEntryData();
		//				entryData.setEntryNumber(entryModel.getEntryNumber());
		//				entryData.setInfo(entryModel.getInfo());
		//				entryData.setProductCode(entryModel.getProductCode());
		//				entryData.setProductDescription(entryModel.getProductDescription());
		//				entryData.setProductName(entryModel.getProductName());
		//				//entryData.setUnit(entryModel.getUnit().getCode());
		//				entryData.setMaxOrderQuantity(entryModel.getMaxOrderQuantity());
		//				entryData.setQuantity(entryModel.getQuantity());
		//				entryData.setBasePrice(entryModel.getBasePrice());
		//				entryData.setCalculated(Boolean.FALSE);
		//				entryData.setEntryStatus(entryModel.getEntryStatus());
		//				entryData.setTotalPrice(entryModel.getTotalPrice());
		//				entryData.setDiscountValues(entryModel.getDiscountValues());
		//				//entryData.setDeliveryMode();
		//				entryData.setCartData(target);
		//				entryDataList.add(entryData);
		//
		//			}
		//			target.setEntries(entryDataList);
		//
		//		}
	}


}
