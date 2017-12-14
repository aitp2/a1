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

import de.hybris.platform.commercefacades.product.ProductExportFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductResultData;
import de.hybris.platform.commercewebservicescommons.dto.product.ProductListWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.RequestParameterException;
import org.training.constants.YcommercewebservicesConstants;
import org.training.formatters.WsDateFormatter;
import org.training.product.data.ProductDataList;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Web Services Controller to expose the functionality of the
 * {@link de.hybris.platform.commercefacades.product.ProductFacade} and SearchFacade.
 */
@Controller
@RequestMapping(value = "/{baseSiteId}/export/products")
public class ExportController extends BaseController
{
	private static final Set<ProductOption> OPTIONS;
	private static final String DEFAULT_PAGE_VALUE = "0";
	private static final String MAX_INTEGER = "2147483647";
	@Resource(name = "cwsProductExportFacade")
	private ProductExportFacade productExportFacade;
	@Resource(name = "wsDateFormatter")
	private WsDateFormatter wsDateFormatter;

	static
	{
		String productOptions = "";

		for (final ProductOption option : ProductOption.values())
		{
			productOptions = productOptions + option.toString() + " ";
		}
		productOptions = productOptions.trim().replace(" ", YcommercewebservicesConstants.OPTIONS_SEPARATOR);
		OPTIONS = extractOptions(productOptions);
	}

	protected static Set<ProductOption> extractOptions(final String options)
	{
		final String[] optionsStrings = options.split(YcommercewebservicesConstants.OPTIONS_SEPARATOR);

		final Set<ProductOption> opts = new HashSet<ProductOption>();
		for (final String option : optionsStrings)
		{
			opts.add(ProductOption.valueOf(option));
		}
		return opts;
	}

	/**
	 * Used for product export. Depending on the timestamp parameter, it can return all products or only products
	 * modified after the given time.
	 *
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @queryparam currentPage The current result page requested.
	 * @queryparam pageSize The number of results returned per page.
	 * @queryparam catalog Catalog from which get products. Must be provided along with version.
	 * @queryparam version Catalog version. Must be provided along with catalog.
	 * @queryparam timestamp When this parameter is set, only products modified after given time will be returned.This
	 *             parameter should be in RFC-8601 format.
	 * @return List of products
	 * @security Allowed only for trusted client
	 */
	@Secured("ROLE_TRUSTED_CLIENT")
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public ProductListWsDTO exportProducts(@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields,
			@RequestParam(required = false, defaultValue = DEFAULT_PAGE_VALUE) final int currentPage,
			@RequestParam(required = false, defaultValue = MAX_INTEGER) final int pageSize,
			@RequestParam(required = false) final String catalog, @RequestParam(required = false) final String version,
			@RequestParam(required = false) final String timestamp)
	{
		if (StringUtils.isEmpty(catalog) && !StringUtils.isEmpty(version))
		{
			throw new RequestParameterException("Both 'catalog' and 'version' parameters have to be provided or ignored.",
					RequestParameterException.MISSING, catalog == null ? "catalog" : "version");
		}

		if (StringUtils.isEmpty(version) && !StringUtils.isEmpty(catalog))
		{
			throw new RequestParameterException("Both 'catalog' and 'version' parameters have to be provided or ignored.",
					RequestParameterException.MISSING, catalog == null ? "catalog" : "version");
		}

		if (StringUtils.isEmpty(timestamp))
		{
			return fullExport(fields, currentPage, pageSize, catalog, version);
		}
		else
		{
			return incrementalExport(fields, currentPage, pageSize, catalog, version, timestamp);
		}
	}

	protected ProductListWsDTO incrementalExport(final String fields, final int currentPage, final int pageSize,
			final String catalog, final String version, final String timestamp)
	{
		final Date timestampDate;
		try
		{
			timestampDate = wsDateFormatter.toDate(timestamp);
		}
		catch (final IllegalArgumentException e)
		{
			throw new RequestParameterException("Wrong time format. The only accepted format is ISO-8601.",
					RequestParameterException.INVALID, "timestamp", e);
		}

		final ProductResultData modifiedProducts = productExportFacade.getOnlyModifiedProductsForOptions(catalog, version,
				timestampDate, OPTIONS, currentPage, pageSize);

		return getDataMapper().map(convertResultset(currentPage, pageSize, catalog, version, modifiedProducts),
				ProductListWsDTO.class, fields);
	}

	protected ProductListWsDTO fullExport(final String fields, final int currentPage, final int pageSize, final String catalog,
			final String version)
	{
		final ProductResultData products = productExportFacade.getAllProductsForOptions(catalog, version, OPTIONS, currentPage,
				pageSize);

		return getDataMapper().map(convertResultset(currentPage, pageSize, catalog, version, products), ProductListWsDTO.class,
				fields);
	}

	protected ProductDataList convertResultset(final int page, final int pageSize, final String catalog, final String version,
			final ProductResultData modifiedProducts)
	{
		final ProductDataList result = new ProductDataList();
		result.setProducts(modifiedProducts.getProducts());
		if (pageSize > 0)
		{
			result.setTotalPageCount((modifiedProducts.getTotalCount() % pageSize == 0) ? modifiedProducts.getTotalCount() / pageSize
					: modifiedProducts.getTotalCount() / pageSize + 1);
		}
		result.setCurrentPage(page);
		result.setTotalProductCount(modifiedProducts.getTotalCount());
		result.setCatalog(catalog);
		result.setVersion(version);
		return result;
	}
}
