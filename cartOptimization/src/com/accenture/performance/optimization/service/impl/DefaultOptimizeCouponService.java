package com.accenture.performance.optimization.service.impl;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.ruleengineservices.result.OptimizedPromotionOrderResults;
import com.accenture.performance.optimization.service.OptimizeCouponManagementService;
import com.accenture.performance.optimization.service.OptimizeCouponService;
import com.accenture.performance.optimization.service.OptimizeModelDealService;

import de.hybris.platform.couponservices.CouponServiceException;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.service.data.CouponResponse;
import de.hybris.platform.couponservices.services.impl.DefaultCouponService;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.promotions.result.PromotionOrderResults;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

public class DefaultOptimizeCouponService extends DefaultCouponService implements OptimizeCouponService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultOptimizeCouponService.class);
	
	private OptimizeCalculateServiceImpl calculationService;
	private DefaultOptimizationPromotionService promotionsService;
	private OptimizeCouponManagementService couponManagementService;
	private OptimizeModelDealService optimizeModelDealService;
	private TimeService timeService;
	
	@Override
	public CouponResponse redeemCoupon(String couponCode, OptimizedCartData cart) 
	{
		ServicesUtil.validateParameterNotNullStandardMessage((String) "couponCode", (Object) couponCode);
		ServicesUtil.validateParameterNotNullStandardMessage((String) "cart", (Object) cart);
		String clearedCouponCode = this.clearCouponCode(couponCode);
		CouponResponse response = this.assertCouponCodeInOrder(clearedCouponCode, cart);
		
		if (BooleanUtils.isTrue((Boolean) response.getSuccess())) 
		{
			this.redeemCouponCode(cart, clearedCouponCode, response);
		}
		
		return response;
	}
	
	protected void redeemCouponCode(OptimizedCartData cart, String clearedCouponCode, CouponResponse response) 
	{
		try 
		{
			if (this.getCouponManagementService().redeem(clearedCouponCode, cart)) 
			{
				LinkedHashSet<String> codes = new LinkedHashSet<String>();
				if ( CollectionUtils.isNotEmpty( cart.getAppliedCouponCodes() ) ) 
				{
					codes.addAll(cart.getAppliedCouponCodes());
				}
				codes.add(clearedCouponCode);
				cart.setAppliedCouponCodes(codes);
				optimizeModelDealService.persistCart(cart);
				////this.getModelService().save((Object) cart);
				
				PromotionOrderResults result = this.recalculateOrder(cart);
				if(result != null && result instanceof OptimizedPromotionOrderResults)
				{
					final OptimizedCartData cartResult = ((OptimizedPromotionOrderResults)result).getOptimizedCart();
					LOG.info("GlobalDiscountValues is empty:"+CollectionUtils.isEmpty(cartResult.getGlobalDiscountValues()));
					////optimizeModelDealService.persistCart();
				}
			}
		} 
		catch (CouponServiceException ex) 
		{
			LOG.debug(ex.getMessage(), (Throwable) ex);
			response.setSuccess(Boolean.FALSE);
			response.setMessage(ex.getMessage());
		}
	}

	protected PromotionOrderResults recalculateOrder(OptimizedCartData optimizedCartData) 
	{
		try 
		{
			this.getCalculationService().calculateTotals(optimizedCartData, true);
			return this.getPromotionsService().updatePromotions(getPromotionGroups(), optimizedCartData, getTimeService().getCurrentTime());
		} 
		catch (CalculationException e) 
		{
			LOG.error("Error re-calculating the order", (Throwable) e);
			throw new CouponServiceException("coupon.order.recalculation.error");
		}
	}
	
	protected CouponResponse assertCouponCodeInOrder(String couponCode, OptimizedCartData order) 
	{
		CouponResponse response = new CouponResponse();
		response.setSuccess(Boolean.TRUE);
		if (this.containsCouponCode(couponCode, order)) {
			response.setMessage("coupon.already.exists.cart");
			response.setSuccess(Boolean.FALSE);
		}
		return response;
	}
	
	protected boolean containsCouponCode(String couponCode, OptimizedCartData order) 
	{
		Optional<AbstractCouponModel> couponModel;
		if ( CollectionUtils.isNotEmpty(order.getAppliedCouponCodes() )
				&& (couponModel = this.getCouponManagementService().getCouponForCode(couponCode)).isPresent()) 
		{
			return order.getAppliedCouponCodes().stream()
					.anyMatch(this.checkMatch( (AbstractCouponModel)couponModel.get(), couponCode));
		}
		return false;
	}
	

	@Override
	public void releaseCouponCode(String couponCode, OptimizedCartData optimizedCartData) 
	{
		ServicesUtil.validateParameterNotNullStandardMessage((String) "couponCode", couponCode);
		ServicesUtil.validateParameterNotNullStandardMessage((String) "order", optimizedCartData);
		this.getCouponManagementService().releaseCouponCode(couponCode);
		this.removeCouponAndTriggerCalculation(couponCode, optimizedCartData);
	}
	
	protected void removeCouponAndTriggerCalculation(String couponCode, OptimizedCartData order)
			throws CouponServiceException {
		Collection<String> couponCodes = order.getAppliedCouponCodes();
		if (CollectionUtils.isNotEmpty( couponCodes) && this.containsCouponCode(couponCode, order)) {
			Set<String> couponCodesFiltered = couponCodes
					.stream()
					.filter(couponCodeItme -> !couponCodeItme.equals(couponCode))
					.collect(Collectors.toSet());
			
			order.setAppliedCouponCodes(couponCodesFiltered);
			order.setCalculated(Boolean.FALSE);
			
			optimizeModelDealService.persistCart(order);
			////this.getModelService().save((Object) order);
			this.recalculateOrder(order);
		}
	}


	@Override
	public CouponResponse verifyCouponCode(String couponCode, OptimizedCartData optimizedCartData) 
	{
		ServicesUtil.validateParameterNotNullStandardMessage((String) "couponCode", couponCode);
		ServicesUtil.validateParameterNotNullStandardMessage((String) "order", optimizedCartData);
		
		return this.getCouponManagementService().verifyCouponCode(couponCode, optimizedCartData);
	}

	/**
	 * @return the calculationService
	 */
	@Override
	public OptimizeCalculateServiceImpl getCalculationService() {
		return calculationService;
	}

	/**
	 * @param calculationService the calculationService to set
	 */
	public void setCalculationService(OptimizeCalculateServiceImpl calculationService) {
		super.setCalculationService(calculationService);
		this.calculationService = calculationService;
	}

	/**
	 * @return the promotionsService
	 */
	@Override
	public DefaultOptimizationPromotionService getPromotionsService() {
		return promotionsService;
	}

	/**
	 * @param promotionsService the promotionsService to set
	 */
	public void setPromotionsService(DefaultOptimizationPromotionService promotionsService) {
		super.setPromotionsService(promotionsService);
		this.promotionsService = promotionsService;
	}

	/**
	 * @return the timeService
	 */
	public TimeService getTimeService() {
		return timeService;
	}

	/**
	 * @param timeService the timeService to set
	 */
	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}

	/**
	 * @return the couponManagementService
	 */
	@Override
	public OptimizeCouponManagementService getCouponManagementService() {
		return couponManagementService;
	}

	/**
	 * @param couponManagementService the couponManagementService to set
	 */
	public void setCouponManagementService(OptimizeCouponManagementService couponManagementService) {
		super.setCouponManagementService(couponManagementService);
		this.couponManagementService = couponManagementService;
	}

	/**
	 * @return the optimizeModelDealService
	 */
	public OptimizeModelDealService getOptimizeModelDealService() {
		return optimizeModelDealService;
	}

	/**
	 * @param optimizeModelDealService the optimizeModelDealService to set
	 */
	public void setOptimizeModelDealService(OptimizeModelDealService optimizeModelDealService) {
		this.optimizeModelDealService = optimizeModelDealService;
	}

}
