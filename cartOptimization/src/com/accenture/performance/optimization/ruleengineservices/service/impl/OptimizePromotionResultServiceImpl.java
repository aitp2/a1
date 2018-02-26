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
package com.accenture.performance.optimization.ruleengineservices.service.impl;

import de.hybris.platform.core.PK;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.promotionengineservices.promotionengine.impl.DefaultPromotionEngineResultService;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.data.OptimizedPromotionActionParameterData;
import com.accenture.performance.optimization.data.OptimizedPromotionResultData;
import com.accenture.performance.optimization.data.OptimizedRuleBasedPotentialPromotionMessageAction;
import com.accenture.performance.optimization.ruleengineservices.service.OptimizePromotionResultService;


/**
 *
 */
public class OptimizePromotionResultServiceImpl extends DefaultPromotionEngineResultService
		implements OptimizePromotionResultService
{
	private static final Logger LOG = LoggerFactory.getLogger(OptimizePromotionResultServiceImpl.class);

	@Override
	public String getDescription(final OptimizedPromotionResultData promotionResult)
	{
		return this.getDescription(promotionResult, (Locale) null);
	}

	public String getDescription(final OptimizedPromotionResultData promotionResult, final Locale locale)
	{
		final Locale localeToUse = Objects.isNull(locale)
				? this.getCommonI18NService().getLocaleForLanguage(this.getCommonI18NService().getCurrentLanguage()) : locale;

		// only support promotion engine, no support old promtion
		final RuleBasedPromotionModel promotion = (RuleBasedPromotionModel) this.getModelService()
				.get(PK.parse(promotionResult.getPromotionPK()));

		final String messageFiredPositional = promotion.getMessageFired();
		try
		{
			if (StringUtils.isEmpty(messageFiredPositional))
			{
				return messageFiredPositional;
			}
			else
			{
				final AbstractRuleEngineRuleModel e = promotion.getRule();
				if (Objects.isNull(e))
				{
					LOG.warn("promotion {} has no corresponding rule. Cannot substitute message parameters, returning message as is.",
							promotion.getCode());
					return messageFiredPositional;
				}
				else
				{
					List<RuleParameterData> parameters = null;
					final String paramString = e.getRuleParameters();
					if (Objects.nonNull(paramString))
					{
						parameters = this.getRuleParametersService().convertParametersFromString(paramString);
					}

					if (Objects.isNull(parameters))
					{
						LOG.warn(
								"rule with code {} has no rule parameters. Cannot substitute message parameters, returning message as is.",
								e.getCode());
						return messageFiredPositional;
					}
					else
					{
												if (Objects.nonNull(promotionResult.getActions()))
												{
													final Map<String,Object> messageActionValues = promotionResult.getActions().stream().filter(action -> {
														return action instanceof OptimizedRuleBasedPotentialPromotionMessageAction;
													}).flatMap(action ->{
														OptimizedRuleBasedPotentialPromotionMessageAction messageAction = (OptimizedRuleBasedPotentialPromotionMessageAction)action;
														return messageAction.getParameters().stream();
													}).collect(Collectors.toMap(OptimizedPromotionActionParameterData::getUuid,  OptimizedPromotionActionParameterData::getValue));
													
													if (LOG.isWarnEnabled())
													{
														parameters.stream().filter((parameter) -> {
															return messageActionValues.containsKey(parameter.getUuid())
																	&& !this.getResolutionStrategies().containsKey(parameter.getType());
														}).forEach((parameter) -> {
															LOG.warn("Parameter {} has to be replaced but resolution strategy for type {} is not defined",
																	parameter.getUuid(), parameter.getType());
														});
													}
						
//													//LOG.error("TODO: get meessage logic");
//													
//													// TODO: implement the logic
//													parameters = parameters.stream().map((parameter) -> {
//														return this.replaceRuleParameterValue(promotionResult, messageActionValues, parameter);
//													}).collect(Collectors.toList());
												}

						return this.getMessageWithResolvedParameters(promotionResult, localeToUse, messageFiredPositional, parameters);
					}
				}
			}
		}
		catch (final Exception arg9)
		{
			LOG.error("error during promotion message calculation, returning empty string", arg9);
			return messageFiredPositional;
		}
	}
	
//	protected RuleParameterData replaceRuleParameterValue(final OptimizedPromotionResultData promotionResult,
//			Map<String, Object> messageActionValues, RuleParameterData parameter) {
//		return messageActionValues.containsKey(parameter.getUuid())
//				&& this.getResolutionStrategies().containsKey(parameter.getType()) ? 
//						this.getResolutionStrategies().get(parameter.getType()).getReplacedParameter(parameter,promotionResult, messageActionValues.get(parameter.getUuid()))
//						: parameter;
//	}


	protected String getMessageWithResolvedParameters(final OptimizedPromotionResultData promotionResult, final Locale locale,
			final String messageFiredPositional, final List<RuleParameterData> parameters)
	{
		final HashMap valuesMap = new HashMap();
		final Iterator resolvedMessage = parameters.iterator();

		while (resolvedMessage.hasNext())
		{
			final RuleParameterData substitorInputMessage = (RuleParameterData) resolvedMessage.next();
			if (messageFiredPositional.contains(substitorInputMessage.getUuid()))
			{
				valuesMap.put(substitorInputMessage.getUuid(),
						this.resolveParameterValue(substitorInputMessage, promotionResult, locale));
			}
		}

		final String substitorInputMessage1 = messageFiredPositional.replace("{", "${");
		final String resolvedMessage1 = (new StrSubstitutor(valuesMap)).replace(substitorInputMessage1);
		if (resolvedMessage1.contains("${"))
		{
			this.logUnresolvedPlaceholder(promotionResult, resolvedMessage1);
			return resolvedMessage1.replace("${", "{");
		}
		else
		{
			return resolvedMessage1;
		}
	}

	protected Object resolveParameterValue(final RuleParameterData parameter, final OptimizedPromotionResultData promotionResult,
			final Locale locale)
	{
		if (Objects.isNull(parameter.getValue()))
		{
			final Matcher strategy1 = LIST_PATTERN.matcher(parameter.getType());
			if (strategy1.matches())
			{
				return Collections.emptyList();
			}
			else
			{
				final Matcher mapMatcher = MAP_PATTERN.matcher(parameter.getType());
				return mapMatcher.matches() ? Collections.emptyMap() : "?";
			}
		}
		else
		{
			// TODO: message parameter handle
			//			if (MapUtils.isNotEmpty(this.getResolutionStrategies()))
			//			{
			//				final PromotionMessageParameterResolutionStrategy strategy = this.getResolutionStrategies().get(parameter.getType());
			//				if (strategy != null)
			//				{
			//					return strategy.getValue(parameter, promotionResult, locale);
			//				}
			//			}

			return parameter.getValue();
		}
	}

	protected void logUnresolvedPlaceholder(final OptimizedPromotionResultData promotionResult, final String resolvedMessage)
	{
		LOG.info("One of message placeholders cannot be filled for the \"{}\" promotion and message \"{}\"",
				promotionResult.getPromotion(), resolvedMessage);
	}

}
