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
package com.accenture.aitp.tailor.service.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.accenture.aitp.tailor.data.ModelMonitoredInfo;
import com.accenture.aitp.tailor.network.AiSSLClient;
import com.accenture.aitp.tailor.service.AitpModelMonitorService;
import com.accenture.aitp.tailor.service.strategy.AitpInvalidateStrategy;
import com.accenture.aitp.tailor.strategy.AitpModelMonitorQueueStrategy;

/**
 *
 */
public class DefaultAitpModelMonitorServiceImpl implements AitpModelMonitorService {
	private static final Logger LOG = Logger.getLogger(DefaultAitpModelMonitorServiceImpl.class);

	private Map<String, AitpInvalidateStrategy> modelMap;
	private AitpModelMonitorQueueStrategy aitpModelMonitorQueueStrategy;
	private ConfigurationService configurationService;

	@Override
	public void publish(final ItemModel model) {
		if (this.getModelMap().containsKey(model.getItemtype())) {
			try {
				aitpModelMonitorQueueStrategy.put(createMonitoredInfo(model, this.getModelMap().get(model.getItemtype())));
			} catch (Exception e) {
				LOG.error("pulbish error : " + e.getMessage());
			}
		}
	}

	private ModelMonitoredInfo createMonitoredInfo(ItemModel model, AitpInvalidateStrategy aitpInvalidateStrategy) {
		ModelMonitoredInfo info = new ModelMonitoredInfo();
		info.setPk(model.getPk().toString());
		info.setItemType(model.getItemtype());
		aitpInvalidateStrategy.initializeContext(model, info);
		return info;
	}

	@Override
	public ModelMonitoredInfo consume() {
		return getAitpModelMonitorQueueStrategy().take();
	}

	/**
	 * @return the aitpModelMonitorQueueStrateg
	 */
	public AitpModelMonitorQueueStrategy getAitpModelMonitorQueueStrategy() {
		return aitpModelMonitorQueueStrategy;
	}

	/**
	 * @param aitpModelMonitorQueueStrateg
	 *            the aitpModelMonitorQueueStrateg to set
	 */
	public void setAitpModelMonitorQueueStrategy(final AitpModelMonitorQueueStrategy aitpModelMonitorQueueStrateg) {
		this.aitpModelMonitorQueueStrategy = aitpModelMonitorQueueStrateg;
	}

	public Map<String, AitpInvalidateStrategy> getModelMap() {
		return modelMap;
	}

	@Required
	public void setModelMap(Map<String, AitpInvalidateStrategy> modelMap) {
		this.modelMap = modelMap;
	}

	@Override
	public void invalidateUrls(ModelMonitoredInfo context) {
		String serverStr = this.getConfigurationService().getConfiguration().getString("webserver.url.list");
		if (serverStr == null) {
			LOG.error("there is no server url. ");
			return;
		}
		String[] serverArray = serverStr.split(";");

		AitpInvalidateStrategy strategy = this.getModelMap().get(context.getItemType());
		if (strategy == null) {
			LOG.warn("there is no strategy for model : " + context);
			return;
		}

		List<String> all = strategy.invalidateUrls(context);
		AiSSLClient client = new AiSSLClient();
		all.forEach(inUrl -> {
			LOG.info("will invalidate " + inUrl);
			for (int i = 0; i < serverArray.length; i++) {
				try {
					LOG.info("request: " + client.doPurge(serverArray[i].trim() + inUrl, "UTF-8"));
				} catch (Exception e) {
					LOG.error(e);
				}
			}
		});
	}

	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

}
