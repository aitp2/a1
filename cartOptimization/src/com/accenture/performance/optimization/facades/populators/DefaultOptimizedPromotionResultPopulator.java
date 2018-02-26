package com.accenture.performance.optimization.facades.populators;

import com.accenture.performance.optimization.data.OptimizedPromotionOrderEntryConsumedData;
import com.accenture.performance.optimization.data.OptimizedPromotionResultData;
import com.accenture.performance.optimization.ruleengineservices.service.OptimizePromotionResultService;

import de.hybris.platform.commercefacades.order.data.PromotionOrderEntryConsumedData;
import de.hybris.platform.commercefacades.product.data.PromotionResultData;
import de.hybris.platform.converters.Converters;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

public class DefaultOptimizedPromotionResultPopulator implements Populator<OptimizedPromotionResultData, PromotionResultData> {
	private OptimizePromotionResultService optimizePromotionResultService;
	private Converter<OptimizedPromotionOrderEntryConsumedData,PromotionOrderEntryConsumedData> optimizedOrderEntryConsumedConverter;
	
	@Override
	public void populate(OptimizedPromotionResultData source, PromotionResultData target) throws ConversionException {
		target.setDescription(getOptimizePromotionResultService().getDescription(source));

//		TODO ai
//		getPromotionResultService().getCouponCodesFromPromotion(source)
//				.ifPresent(couponCodes ->
//				{
//					target.setGiveAwayCouponCodes(couponCodes.stream()
//							.map(getCouponDataFacade()::getCouponDetails).filter(Optional::isPresent).map(Optional::get)
//							.collect(toList()));
//
//
//				});
		

//		target.setPromotionData(getPromotionsConverter().convert(source.getPromotion()));
		target.setConsumedEntries(Converters.convertAll(source.getConsumedEntries(), getOptimizedOrderEntryConsumedConverter()) );
		//TODO ai
		//populate other promotion result info
		
	}

	/**
	 * @return the optimizePromotionResultService
	 */
	public OptimizePromotionResultService getOptimizePromotionResultService() {
		return optimizePromotionResultService;
	}

	/**
	 * @param optimizePromotionResultService the optimizePromotionResultService to set
	 */
	public void setOptimizePromotionResultService(OptimizePromotionResultService optimizePromotionResultService) {
		this.optimizePromotionResultService = optimizePromotionResultService;
	}

	/**
	 * @return the optimizedOrderEntryConsumedConverter
	 */
	public Converter<OptimizedPromotionOrderEntryConsumedData, PromotionOrderEntryConsumedData> getOptimizedOrderEntryConsumedConverter() {
		return optimizedOrderEntryConsumedConverter;
	}

	/**
	 * @param optimizedOrderEntryConsumedConverter the optimizedOrderEntryConsumedConverter to set
	 */
	public void setOptimizedOrderEntryConsumedConverter(
			Converter<OptimizedPromotionOrderEntryConsumedData, PromotionOrderEntryConsumedData> optimizedOrderEntryConsumedConverter) {
		this.optimizedOrderEntryConsumedConverter = optimizedOrderEntryConsumedConverter;
	}
	
}
