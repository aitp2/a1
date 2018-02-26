package com.accenture.performance.optimization.ruleengineservices.strategy;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.data.AbstractOptimizedRuleBasedPromotionActionData;
import com.accenture.performance.optimization.data.OptimizedPromotionActionParameterData;
import com.accenture.performance.optimization.data.OptimizedPromotionResultData;
import com.accenture.performance.optimization.data.OptimizedRuleBasedPotentialPromotionMessageAction;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.ruleengineservices.service.OptimizePromotionActionService;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPotentialPromotionMessageActionModel;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.DisplayMessageRAO;

public class DefaultOptimizePotentialPromotionMessageActionStrategy	extends AbstractOptimizeRuleActionStrategy<AbstractOptimizedRuleBasedPromotionActionData> {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultOptimizePotentialPromotionMessageActionStrategy.class);
	
	private OptimizePromotionActionService promotionActionService;
	
	@Override
	public List<OptimizedPromotionResultData> apply(AbstractRuleActionRAO action) {
		if (!(action instanceof DisplayMessageRAO)) {
			LOG.error("cannot apply {}, action is not of type DisplayMessageRAO",
					(Object) this.getClass().getSimpleName());
			return Collections.emptyList();
		}
		
		OptimizedPromotionResultData promoResult = this.getPromotionActionService().createPromotionResultData(action);
		
		if (promoResult == null) {
			LOG.error("cannot apply {}, promotionResult could not be created.",
					(Object) this.getClass().getSimpleName());
			return Collections.emptyList();
		}
		
		OptimizedCartData order = promoResult.getCart();
		if (order == null) {
			
			return Collections.emptyList();
		}
		
		OptimizedRuleBasedPotentialPromotionMessageAction actionModel = (OptimizedRuleBasedPotentialPromotionMessageAction) this.createPromotionAction(promoResult, action);
		this.handleActionMetadata(action, actionModel);
		this.supplementMessageActionModelWithParameters((DisplayMessageRAO) action, actionModel);
		////this.getModelService().saveAll(new Object[]{promoResult, actionModel});
		
		return Collections.singletonList(promoResult);
	}
	
	protected void supplementMessageActionModelWithParameters(DisplayMessageRAO action, OptimizedRuleBasedPotentialPromotionMessageAction actionModel) 
	{
		if (MapUtils.isNotEmpty((Map<String,Object>) action.getParameters())) 
		{
			Map<String,Object> params = action.getParameters();
			actionModel.setParameters
			(
					params.entrySet()
					.stream()
					.map(this::convertToActionParameterModel)
					.collect( Collectors.toList() ) 
			);
		}
	}

	protected OptimizedPromotionActionParameterData convertToActionParameterModel(Map.Entry<String, Object> actionParameterEntry) 
	{
		OptimizedPromotionActionParameterData actionParameterData = new OptimizedPromotionActionParameterData(); 
		actionParameterData.setUuid(actionParameterEntry.getKey());
		actionParameterData.setValue(actionParameterEntry.getValue());
		return actionParameterData;
	}

	//TODO acn
	public void undo(ItemModel item) {
		if (item instanceof RuleBasedPotentialPromotionMessageActionModel) {
			LOG.error("TODO: no implement of undo!");
		}
	}

	/**
	 * @return the promotionActionService
	 */
	@Override
	public OptimizePromotionActionService getPromotionActionService() {
		return promotionActionService;
	}

	/**
	 * @param promotionActionService the promotionActionService to set
	 */
	public void setPromotionActionService(OptimizePromotionActionService promotionActionService) {
		super.setPromotionActionService(promotionActionService);
		this.promotionActionService = promotionActionService;
	}


}
