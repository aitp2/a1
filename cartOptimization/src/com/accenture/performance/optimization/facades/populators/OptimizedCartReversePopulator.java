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

import de.hybris.platform.commerceservices.delivery.DeliveryService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.services.BaseStoreService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.model.OptimizedCartModel;


/**
 *
 */
public class OptimizedCartReversePopulator implements Populator<OptimizedCartData, OptimizedCartModel>
{
	private static final Logger LOG = LoggerFactory.getLogger(OptimizedCartReversePopulator.class);

	private BaseSiteService baseSiteService;

	private BaseStoreService baseStoreService;

	private ModelService modelService;

	private UnitService unitService;

	private DeliveryService deliveryService;

	/*
	 * From data to model
	 */
	@Override
	public void populate(final OptimizedCartData source, final OptimizedCartModel target)
	{
		target.setCode(source.getCode());
		target.setUserId(source.getUserId());
		target.setSite(getBaseSiteService().getBaseSiteForUID(source.getBaseSite()));
		target.setStore(getBaseStoreService().getBaseStoreForUid(source.getBaseStore()));
		target.setGuid(source.getGuid());
		target.setTotalDiscounts(source.getTotalDiscounts());
		target.setTotalPrice(source.getTotalPrice());
		target.setTotalTax(source.getTotalTax());
		target.setSubtotal(source.getSubtotal());
		target.setDeliveryCost(source.getDeliveryCost());
		target.setCurrencyCode(source.getCurrencyCode());
		if(source.getDeliveryMode() != null)
		{
			target.setDeliveryMode(getDeliveryService().getDeliveryModeForCode(source.getDeliveryMode()));
		}
		target.setPaymentCost(source.getPaymentCost());
		target.setNet(Boolean.valueOf(source.isNet()));


		//		//	getModelService().removeAll(target.getEntries());
		//		if (source.getEntries() != null || !source.getEntries().isEmpty())
		//		{
		//			final List<OptimizedCartEntryModel> entryDataList = new ArrayList<OptimizedCartEntryModel>(target.getEntries());
		//			for (final OptimizedCartEntryData entryData : source.getEntries())
		//			{
		//				boolean isExisted = false;
		//				for (final OptimizedCartEntryModel entryModel : target.getEntries())
		//				{
		//					if (entryData.getProductCode().equals(entryModel.getProductCode())
		//							&& entryData.getQuantity().intValue() == entryModel.getQuantity().intValue())
		//					{
		//						isExisted = true;
		//						break;
		//					}
		//					if (entryData.getProductCode().equals(entryModel.getProductCode()))
		//					{
		//						entryModel.setQuantity(entryData.getQuantity());
		//						getModelService().save(entryModel);
		//						isExisted = true;
		//						break;
		//					}
		//				}
		//				if (isExisted == false)
		//				{
		//					final OptimizedCartEntryModel newEntryModel = getModelService().create(OptimizedCartEntryModel.class);
		//					newEntryModel.setEntryNumber(entryData.getEntryNumber());
		//					newEntryModel.setEntryNumberId(new Date());
		//					newEntryModel.setProductCode(entryData.getProductCode());
		//					newEntryModel.setQuantity(entryData.getQuantity());
		//					newEntryModel.setMaxOrderQuantity(entryData.getMaxOrderQuantity());
		//					newEntryModel.setDiscountValues(entryData.getDiscountValues());
		//					newEntryModel.setProductName(entryData.getProductName());
		//					newEntryModel.setProductDescription(entryData.getProductDescription());
		//					newEntryModel.setTotalPrice(entryData.getTotalPrice());
		//					newEntryModel.setUnit(getUnitService().getUnitForCode(entryData.getUnit()));
		//					getModelService().save(newEntryModel);
		//					//newEntryModel.setIsGift(entryData.getis);
		//					entryDataList.add(newEntryModel);
		//
		//				}
		//			}
		//			target.setEntries(entryDataList);

		//	}

	}

	/**
	 * @return the baseSiteService
	 */
	public BaseSiteService getBaseSiteService()
	{
		return baseSiteService;
	}

	/**
	 * @param baseSiteService
	 *           the baseSiteService to set
	 */
	public void setBaseSiteService(final BaseSiteService baseSiteService)
	{
		this.baseSiteService = baseSiteService;
	}

	/**
	 * @return the baseStoreService
	 */
	public BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	/**
	 * @param baseStoreService
	 *           the baseStoreService to set
	 */
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

	/**
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @return the unitService
	 */
	public UnitService getUnitService()
	{
		return unitService;
	}

	/**
	 * @param unitService
	 *           the unitService to set
	 */
	public void setUnitService(final UnitService unitService)
	{
		this.unitService = unitService;
	}

	/**
	 * @return the deliveryService
	 */
	public DeliveryService getDeliveryService()
	{
		return deliveryService;
	}

	/**
	 * @param deliveryService
	 *           the deliveryService to set
	 */
	public void setDeliveryService(final DeliveryService deliveryService)
	{
		this.deliveryService = deliveryService;
	}



}
