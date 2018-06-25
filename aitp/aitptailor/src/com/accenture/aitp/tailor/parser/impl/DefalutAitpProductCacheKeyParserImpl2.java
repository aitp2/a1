/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2018 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package com.accenture.aitp.tailor.parser.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.accenture.aitp.tailor.data.ModelMonitoredInfo;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.url.UrlResolver;
import de.hybris.platform.core.PK;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;

/**
 *
 */
public class DefalutAitpProductCacheKeyParserImpl2 extends DefalutAitpAbstractCacheKeyParserImpl {

	private final static Logger LOG = Logger.getLogger(DefalutAitpProductCacheKeyParserImpl2.class);

	private UrlResolver<ProductData> productDataUrlResolver;
	private List<String> basePathList;
	private ConfigurationService configurationService;
	private CatalogVersionService catalogVersionService;
	private ModelService modelService;

	public UrlResolver<ProductData> getProductDataUrlResolver() {
		return productDataUrlResolver;
	}

	public void setProductDataUrlResolver(UrlResolver<ProductData> productDataUrlResolver) {
		this.productDataUrlResolver = productDataUrlResolver;
	}

	public List<String> getBasePathList() {
		return basePathList;
	}

	public void setBasePathList(List<String> basePathList) {
		this.basePathList = basePathList;
	}

	@Override
	public List<String> invalidateUrl(final ModelMonitoredInfo info) {
		String changeStr = this.getConfigurationService().getConfiguration().getString("purge.product.baseurls");
		if(StringUtils.isNotEmpty(changeStr)) {
			String[] all = changeStr.split(";");
			this.basePathList.clear();
			for(int i=0; i< all.length; i++) {
				if(StringUtils.isNotEmpty(all[i].trim())) {
					this.basePathList.add(all[i].trim());
				}
			}
		}
		
		List<CatalogVersionModel> catalogList = new ArrayList<>();
		CatalogVersionModel itemModel = this.getModelService().get(PK.parse(info.get("catalogversionpk").toString()));
		catalogList.add(itemModel);
		this.getCatalogVersionService().setSessionCatalogVersions(catalogList);
		List<String> result = new ArrayList<>();
		ProductData pdata = new ProductData();
		pdata.setCode(info.get("code").toString());
		String url = this.getProductDataUrlResolver().resolve(pdata);
		
		this.getBasePathList().forEach(tmp -> {
			String fullPath = tmp + url;
			LOG.info("resolved url : " + fullPath);
			result.add(fullPath);
		});

		return result;
	}

	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public CatalogVersionService getCatalogVersionService() {
		return catalogVersionService;
	}

	public void setCatalogVersionService(CatalogVersionService catalogVersionService) {
		this.catalogVersionService = catalogVersionService;
	}

	public ModelService getModelService() {
		return modelService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
}
