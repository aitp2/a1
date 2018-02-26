package com.accenture.performance.optimization.facades.populators;

import org.springframework.util.Assert;

import com.accenture.performance.optimization.data.OptimizedPromotionOrderEntryConsumedData;

import de.hybris.platform.commercefacades.order.data.PromotionOrderEntryConsumedData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class DefaultOptimizedPromotionOrderEntryConsumedPopulator implements Populator<OptimizedPromotionOrderEntryConsumedData, PromotionOrderEntryConsumedData> {

	@Override
	public void populate(OptimizedPromotionOrderEntryConsumedData source, PromotionOrderEntryConsumedData target)
			throws ConversionException {
		Assert.notNull(source, "Parameter source cannot be null.");
		Assert.notNull(target, "Parameter target cannot be null.");

		target.setCode(source.getCode());
		target.setAdjustedUnitPrice(source.getAdjustedUnitPrice());
		
		if (source.getOrderEntry() != null)
		{
			target.setOrderEntryNumber(source.getOrderEntry().getEntryNumber());
		}
		
		target.setQuantity(Long.valueOf(source.getQuantity().longValue()));
		
	}

}
