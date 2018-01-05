package com.accenture.performance.optimization.ruleengineservices.strategy;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.data.OptimizedPromotionResultData;
import com.accenture.performance.optimization.data.OptimizedRuleBasedOrderAddProductAction;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;
import com.accenture.performance.optimization.ruleengineservices.service.OptimizePromotionActionService;
import com.accenture.performance.optimization.service.OptimizeCartService;
import com.accenture.performance.optimization.service.OptimizeModelDealService;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.order.OrderService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.ruleengineservices.calculation.RuleEngineCalculationService;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.FreeProductRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.util.OrderUtils;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

public class DefaultOptimizeAddProductToCartActionStrategy
		extends AbstractOptimizeRuleActionStrategy<OptimizedRuleBasedOrderAddProductAction> 
{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultOptimizeAddProductToCartActionStrategy.class);
	
	private OptimizeCartService cartService;
	private OrderService orderService;
	private ProductService productService;
	private RuleEngineCalculationService ruleEngineCalculationService;
	private OrderUtils orderUtils;
	private OptimizeModelDealService optimizeModelDealService;
	
	@Override
	public List<OptimizedPromotionResultData> apply(AbstractRuleActionRAO action) 
	{
		if (!(action instanceof FreeProductRAO)) {
			LOG.error("cannot apply {}, action is not of type FreeProductRAO, but {}",
					(Object) this.getClass().getSimpleName(), (Object) action);
			return Collections.emptyList();
		}
		FreeProductRAO freeAction = (FreeProductRAO) action;
		if (!(freeAction.getAppliedToObject() instanceof CartRAO)) {
			LOG.error("cannot apply {}, appliedToObject is not of type CartRAO, but {}",
					(Object) this.getClass().getSimpleName(), (Object) action.getAppliedToObject());
			return Collections.emptyList();
		}
		if (freeAction.getAddedOrderEntry() == null || freeAction.getAddedOrderEntry().getProduct() == null
				|| freeAction.getAddedOrderEntry().getProduct().getCode() == null) {
			LOG.error("cannot apply {}, addedOrderEntry.product.code is not set.",
					(Object) this.getClass().getSimpleName());
			return Collections.emptyList();
		}
		OrderEntryRAO addedOrderEntryRao = freeAction.getAddedOrderEntry();
		ProductModel product = null;
		try {
			product = this.getProductService().getProductForCode(addedOrderEntryRao.getProduct().getCode());
		} catch (AmbiguousIdentifierException | UnknownIdentifierException e) {
			LOG.error("cannot apply {}, product for code: {} cannot be retrieved due to exception {}.",
					new Object[]{this.getClass().getSimpleName(), addedOrderEntryRao.getProduct().getCode(),
							e.getClass().getSimpleName(), e});
			return Collections.emptyList();
		}
		OptimizedPromotionResultData promoResult = ((OptimizePromotionActionService)getPromotionActionService()).createPromotionResultData(action);
		if (promoResult == null) {
			LOG.error("cannot apply {}, promotionResult could not be created.",
					(Object) this.getClass().getSimpleName());
			return Collections.emptyList();
		}
		
		final OptimizedCartData order = promoResult.getCart();
		if (Objects.isNull((Object) order)) {
			LOG.error("cannot apply {}, order or cart not found: {}", (Object) this.getClass().getSimpleName(),
					(Object) order);
			/*if (this.getModelService().isNew((Object) promoResult)) {
				this.getModelService().detach((Object) promoResult);
			}*/
			return Collections.emptyList();
		}
		OptimizedCartEntryData abstractOrderEntry = null;
		if(order instanceof OptimizedCartData)
		{
			abstractOrderEntry = this.getCartService().addNewEntry(order, product,
					(long) addedOrderEntryRao.getQuantity(), null, -1, false);
		}else
		{
			//TODO acn
//			abstractOrderEntry = this.getOrderService().addNewEntry((OrderModel) order, product,
//					(long) addedOrderEntryRao.getQuantity(), null, -1, false);
		}
				
				
		abstractOrderEntry.setPromomtionGiftEntry(Boolean.TRUE);
		addedOrderEntryRao.setEntryNumber(abstractOrderEntry.getEntryNumber());
		
		OptimizedRuleBasedOrderAddProductAction actionModel = this.createOrderAddProductAction(action,
				addedOrderEntryRao.getQuantity(), product, promoResult);
		this.handleActionMetadata(action, actionModel);
		
		////this.getModelService().saveAll(new Object[]{promoResult, actionModel, order, abstractOrderEntry});
		return Collections.singletonList(promoResult);
	}
	
	protected OptimizedRuleBasedOrderAddProductAction createOrderAddProductAction(AbstractRuleActionRAO action,
			int quantity, ProductModel product, OptimizedPromotionResultData promoResult) {
		OptimizedRuleBasedOrderAddProductAction actionModel = (OptimizedRuleBasedOrderAddProductAction) this
				.createPromotionAction(promoResult, action);
		actionModel.setProductCode(product.getCode());
		actionModel.setQuantity(Long.valueOf(quantity));
		return actionModel;
	}

	@Override
	public void undo(ItemModel var1) 
	{
		//TODO Auto-generated method stub
		LOG.error("TODO: no implement of undo!");
	}


	/**
	 * @return the orderService
	 */
	public OrderService getOrderService() {
		return orderService;
	}

	/**
	 * @param orderService the orderService to set
	 */
	public void setOrderService(OrderService orderService) {
		this.orderService = orderService;
	}

	/**
	 * @return the productService
	 */
	public ProductService getProductService() {
		return productService;
	}

	/**
	 * @param productService the productService to set
	 */
	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	/**
	 * @return the ruleEngineCalculationService
	 */
	public RuleEngineCalculationService getRuleEngineCalculationService() {
		return ruleEngineCalculationService;
	}

	/**
	 * @param ruleEngineCalculationService the ruleEngineCalculationService to set
	 */
	public void setRuleEngineCalculationService(RuleEngineCalculationService ruleEngineCalculationService) {
		this.ruleEngineCalculationService = ruleEngineCalculationService;
	}

	/**
	 * @return the orderUtils
	 */
	public OrderUtils getOrderUtils() {
		return orderUtils;
	}

	/**
	 * @param orderUtils the orderUtils to set
	 */
	public void setOrderUtils(OrderUtils orderUtils) {
		this.orderUtils = orderUtils;
	}

	/**
	 * @return the cartService
	 */
	public OptimizeCartService getCartService() {
		return cartService;
	}

	/**
	 * @param cartService the cartService to set
	 */
	public void setCartService(OptimizeCartService cartService) {
		this.cartService = cartService;
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
