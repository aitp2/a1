/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package org.training.v2.controller;

import de.hybris.platform.commercefacades.product.data.PromotionData;
import de.hybris.platform.commercefacades.promotion.CommercePromotionFacade;
import de.hybris.platform.commercefacades.promotion.PromotionOption;
import de.hybris.platform.commercewebservicescommons.dto.product.PromotionListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.product.PromotionWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.RequestParameterException;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import org.training.constants.YcommercewebservicesConstants;
import org.training.product.data.PromotionDataList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Main Controller for Promotions
 *
 * @pathparam code Promotion identifier (code)
 */
@Controller
@RequestMapping(value = "/{baseSiteId}/promotions")
@CacheControl(directive = CacheControlDirective.PUBLIC, maxAge = 300)
public class PromotionsController extends BaseController
{
	private static final String ORDER_PROMOTION = "order";
	private static final String PRODUCT_PROMOTION = "product";
	private static final String ALL_PROMOTIONS = "all";
	private static final Set<PromotionOption> OPTIONS;

	static
	{
		String promotionOptions = "";

		for (final PromotionOption option : PromotionOption.values())
		{
			promotionOptions = promotionOptions + option.toString() + " ";
		}
		promotionOptions = promotionOptions.trim().replace(" ", YcommercewebservicesConstants.OPTIONS_SEPARATOR);
		OPTIONS = extractOptions(promotionOptions);
	}

	@Resource(name = "commercePromotionFacade")
	private CommercePromotionFacade commercePromotionFacade;

	/**
	 * Returns promotions defined for a current base site. Requests pertaining to promotions have been developed for the
	 * previous version of promotions and vouchers and therefore some of them are currently not compatible with the new
	 * promotion engine.
	 *
	 * @queryparam type Defines what type of promotions should be returned. Values supported for that parameter are:
	 *             <ul>
	 *             <li>all: All available promotions are returned</li>
	 *             <li>product: Only product promotions are returned</li>
	 *             <li>order: Only order promotions are returned</li>
	 *             </ul>
	 * @queryparam promotionGroup Only promotions from this group are returned
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @return List of promotions
	 * @throws RequestParameterException
	 *            When value of 'type' parameter is incorrect
	 * @security Permitted only for trusted client
	 */
	@Secured("ROLE_TRUSTED_CLIENT")
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@Cacheable(value = "promotionCache", key = "T(de.hybris.platform.commercewebservicescommons.cache.CommerceCacheKeyGenerator).generateKey(false,true,'getPromotions',#type,#promotionGroup,#fields)")
	public PromotionListWsDTO getPromotions(@RequestParam final String type,
			@RequestParam(required = false) final String promotionGroup, @RequestParam(defaultValue = "BASIC") final String fields)
					throws RequestParameterException //NOSONAR
	{
		validateTypeParameter(type);

		final PromotionDataList promotionDataList = new PromotionDataList();
		promotionDataList.setPromotions(getPromotionList(type, promotionGroup));
		return getDataMapper().map(promotionDataList, PromotionListWsDTO.class, fields);
	}

	/**
	 * Returns details of a single promotion specified by a promotion code. Requests pertaining to promotions have been
	 * developed for the previous version of promotions and vouchers and therefore some of them are currently not
	 * compatible with the new promotion engine.
	 *
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @return Promotion details
	 * @security Permitted only for trusted client
	 */
	@Secured("ROLE_TRUSTED_CLIENT")
	@RequestMapping(value = "/{code}", method = RequestMethod.GET)
	@Cacheable(value = "promotionCache", key = "T(de.hybris.platform.commercewebservicescommons.cache.CommerceCacheKeyGenerator).generateKey(false,true,'getPromotions',#code,#fields)")
	@ResponseBody
	public PromotionWsDTO getPromotionByCode(@PathVariable final String code,
			@RequestParam(defaultValue = "BASIC") final String fields)
	{
		final PromotionData promotionData = commercePromotionFacade.getPromotion(code, OPTIONS);
		return getDataMapper().map(promotionData, PromotionWsDTO.class, fields);
	}

	protected void validateTypeParameter(final String type) throws RequestParameterException //NOSONAR
	{
		if (!ORDER_PROMOTION.equals(type) && !PRODUCT_PROMOTION.equals(type) && !ALL_PROMOTIONS.equals(type))
		{
			throw new RequestParameterException(
					"Parameter type=" + sanitize(type)
							+ " is not supported. Permitted values for this parameter are : 'order', 'product' or 'all'",
					RequestParameterException.INVALID, "type");
		}
	}

	protected List<PromotionData> getPromotionList(final String type, final String promotionGroup)
	{
		if (promotionGroup == null || promotionGroup.isEmpty())
		{
			return getPromotionList(type);
		}

		List<PromotionData> promotions = null;
		if (ORDER_PROMOTION.equals(type))
		{
			promotions = getCommercePromotionFacade().getOrderPromotions(promotionGroup);
		}
		else if (PRODUCT_PROMOTION.equals(type))
		{
			promotions = getCommercePromotionFacade().getProductPromotions(promotionGroup);
		}
		else if (ALL_PROMOTIONS.equals(type))
		{
			promotions = getCommercePromotionFacade().getProductPromotions(promotionGroup);
			promotions.addAll(getCommercePromotionFacade().getOrderPromotions(promotionGroup));
		}
		return promotions;

	}

	protected List<PromotionData> getPromotionList(final String type)
	{
		List<PromotionData> promotions = null;
		if (ORDER_PROMOTION.equals(type))
		{
			promotions = getCommercePromotionFacade().getOrderPromotions();
		}
		else if (PRODUCT_PROMOTION.equals(type))
		{
			promotions = getCommercePromotionFacade().getProductPromotions();
		}
		else if (ALL_PROMOTIONS.equals(type))
		{
			promotions = getCommercePromotionFacade().getProductPromotions();
			promotions.addAll(getCommercePromotionFacade().getOrderPromotions());
		}
		return promotions;
	}

	protected static Set<PromotionOption> extractOptions(final String options)
	{
		final String[] optionsStrings = options.split(YcommercewebservicesConstants.OPTIONS_SEPARATOR);

		final Set<PromotionOption> opts = new HashSet<PromotionOption>();
		for (final String option : optionsStrings)
		{
			opts.add(PromotionOption.valueOf(option));
		}
		return opts;
	}

	public CommercePromotionFacade getCommercePromotionFacade()
	{
		return commercePromotionFacade;
	}
}
