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
package com.accenture.performance.optimization.ruleengineservices.strategy;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.promotionengineservices.action.impl.AbstractRuleActionStrategy;
import de.hybris.platform.promotionengineservices.model.AbstractRuleBasedPromotionActionModel;
import de.hybris.platform.promotionengineservices.order.dao.ExtendedOrderDao;
import de.hybris.platform.promotionengineservices.promotionengine.PromotionActionService;
import de.hybris.platform.ruleengine.RuleActionMetadataHandler;
import de.hybris.platform.ruleengineservices.action.RuleActionStrategy;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.servicelayer.exceptions.SystemException;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Required;

import com.accenture.performance.optimization.data.AbstractOptimizedPromotionActionData;
import com.accenture.performance.optimization.data.AbstractOptimizedRuleBasedPromotionActionData;
import com.accenture.performance.optimization.data.OptimizedPromotionResultData;


/**
 *
 */
public abstract class AbstractOptimizeRuleActionStrategy<RULE_BASED_ACTION extends AbstractOptimizedRuleBasedPromotionActionData>
		implements RuleActionStrategy, BeanNameAware
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractRuleActionStrategy.class);
	private ExtendedOrderDao extendedOrderDao;
	private ModelService modelService;
	private PromotionActionService promotionActionService;
	private CalculationService calculationService;
	private Class<RULE_BASED_ACTION> promotionAction;
	private Boolean forceImmediateRecalculation;
	private String beanName;
	private Map<String, List<RuleActionMetadataHandler>> ruleActionMetadataHandlers;

	public AbstractOptimizeRuleActionStrategy()
	{
		this.forceImmediateRecalculation = Boolean.FALSE;
	}

	protected void handleActionMetadata(final AbstractRuleActionRAO action,
			final AbstractOptimizedRuleBasedPromotionActionData actionModel)
	{
		// TODO: meta data handle
		LOG.error("[TODO] handleActionMetadata unimplement");
		//		if (action.getMetadata() != null)
		//		{
		//			final Iterator arg3 = action.getMetadata().entrySet().iterator();
		//
		//			while (arg3.hasNext())
		//			{
		//				final Entry mdEntry = (Entry) arg3.next();
		//				final Iterator arg5 = this.getMetadataHandlers((String) mdEntry.getKey()).iterator();
		//
		//				while (arg5.hasNext())
		//				{
		//					final RuleActionMetadataHandler mdHandler = (RuleActionMetadataHandler) arg5.next();
		//					mdHandler.handle(actionModel, (String) mdEntry.getValue());
		//				}
		//			}
		//		}

	}

	protected RULE_BASED_ACTION createPromotionAction(final OptimizedPromotionResultData promotionResult,
			final AbstractRuleActionRAO action)
	{
		AbstractOptimizedRuleBasedPromotionActionData result;
		try
		{
			result = promotionAction.newInstance();
			result.setPromotionResult(promotionResult);
			if (promotionResult.getActions() == null)
			{
				final Collection<AbstractOptimizedPromotionActionData> actions = new ArrayList<>();
				promotionResult.setActions(actions);
			}
			promotionResult.getActions().add(result);
			result.setGuid(createActionUUID());
			result.setRulePK(this.getPromotionActionService().getRule(action).getPk().toString());
			result.setMarkedApplied(Boolean.TRUE);
			result.setStrategyId(this.getStrategyId());
			return (RULE_BASED_ACTION) result;
		}
		catch (final Exception e)
		{
			LOG.error("can not create the instance of {}", action);
		}
		return null;
	}

	protected void handleActionMetadata(final AbstractRuleActionRAO action,
			final AbstractRuleBasedPromotionActionModel actionModel)
	{
		if (action.getMetadata() != null)
		{
			final Iterator arg3 = action.getMetadata().entrySet().iterator();

			while (arg3.hasNext())
			{
				final Entry mdEntry = (Entry) arg3.next();
				final Iterator arg5 = this.getMetadataHandlers((String) mdEntry.getKey()).iterator();

				while (arg5.hasNext())
				{
					final RuleActionMetadataHandler mdHandler = (RuleActionMetadataHandler) arg5.next();
					mdHandler.handle(actionModel, (String) mdEntry.getValue());
				}
			}
		}

	}

	protected void handleUndoActionMetadata(final AbstractRuleBasedPromotionActionModel action)
	{
		if (action.getMetadataHandlers() != null)
		{
			final Iterator arg2 = action.getMetadataHandlers().iterator();

			while (arg2.hasNext())
			{
				final String mdHandlerId = (String) arg2.next();
				final Iterator arg4 = this.getMetadataHandlers(mdHandlerId).iterator();

				while (arg4.hasNext())
				{
					final RuleActionMetadataHandler mdHandler = (RuleActionMetadataHandler) arg4.next();
					mdHandler.undoHandle(action);
				}
			}
		}

	}

	protected List<RuleActionMetadataHandler> getMetadataHandlers(final String mdKey)
	{
		if (this.getRuleActionMetadataHandlers().containsKey(mdKey))
		{
			return this.getRuleActionMetadataHandlers().get(mdKey);
		}
		else
		{
			if (!"ruleCode".equals(mdKey) && !"moduleName".equals(mdKey))
			{
				LOG.error("RuleActionMetadataHandler for {} not found", mdKey);
			}

			return Collections.emptyList();
		}
	}

	//	protected AbstractOrderModel undoInternal(final RULE_BASED_ACTION action)
	//	{
	//		final OptimizedPromotionResultData promoResult = action.getPromotionResult();
	//		final OptimizedCartData order = promoResult.getCart();
	//		// TO Confirm: by junbin if we need to do this inner apply
	//		//final List modifiedItems = this.getPromotionActionService().removeDiscountValue(action.getGuid(), order);
	//		this.getModelService().remove(action);
	//		if (((Set) promoResult.getAllPromotionActions().stream().filter((promoAction) -> {
	//			return !this.getModelService().isRemoved(promoAction);
	//		}).collect(Collectors.toSet())).isEmpty())
	//		{
	//			this.getModelService().remove(promoResult);
	//		}
	//
	//		this.getModelService().saveAll(modifiedItems);
	//		return order;
	//	}

	protected static String createActionUUID()
	{
		return "Action[" + UUID.randomUUID().toString() + "]";
	}

	protected boolean recalculateIfNeeded(final AbstractOrderModel order)
	{
		if (BooleanUtils.isTrue(this.getForceImmediateRecalculation()))
		{
			try
			{
				this.getCalculationService().calculateTotals(order, true);
			}
			catch (final CalculationException arg2)
			{
				LOG.error(String.format("Recalculation of order with code \'%s\' failed.", new Object[]
				{ order.getCode() }), arg2);
				order.setCalculated(Boolean.FALSE);
				this.getModelService().save(order);
				return false;
			}
		}

		return true;
	}

	protected ExtendedOrderDao getExtendedOrderDao()
	{
		return this.extendedOrderDao;
	}

	@Required
	public void setExtendedOrderDao(final ExtendedOrderDao extendedOrderDao)
	{
		this.extendedOrderDao = extendedOrderDao;
	}

	protected ModelService getModelService()
	{
		return this.modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected Class<RULE_BASED_ACTION> getPromotionAction()
	{
		return this.promotionAction;
	}

	@Required
	public void setPromotionAction(final Class<RULE_BASED_ACTION> promotionAction)
	{
		this.promotionAction = promotionAction;
		if (promotionAction != null)
		{
			try
			{
				promotionAction.newInstance();
			}
			catch (IllegalAccessException | InstantiationException arg2)
			{
				throw new SystemException("could not instantiate class " + promotionAction.getSimpleName(), arg2);
			}
		}

	}

	protected PromotionActionService getPromotionActionService()
	{
		return this.promotionActionService;
	}

	@Required
	public void setPromotionActionService(final PromotionActionService promotionActionService)
	{
		this.promotionActionService = promotionActionService;
	}

	protected Boolean getForceImmediateRecalculation()
	{
		return this.forceImmediateRecalculation;
	}

	public void setForceImmediateRecalculation(final Boolean forceImmediateRecalculation)
	{
		this.forceImmediateRecalculation = forceImmediateRecalculation;
	}

	protected CalculationService getCalculationService()
	{
		return this.calculationService;
	}

	@Required
	public void setCalculationService(final CalculationService calculationService)
	{
		this.calculationService = calculationService;
	}

	public void setBeanName(final String beanName)
	{
		this.beanName = beanName;
	}

	public String getStrategyId()
	{
		return this.beanName;
	}

	protected Map<String, List<RuleActionMetadataHandler>> getRuleActionMetadataHandlers()
	{
		return this.ruleActionMetadataHandlers;
	}

	@Required
	public void setRuleActionMetadataHandlers(final Map<String, List<RuleActionMetadataHandler>> ruleActionMetadataHandlers)
	{
		this.ruleActionMetadataHandlers = ruleActionMetadataHandlers;
	}
}
