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
package com.accenture.performance.optimization.service.impl;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.data.OptimizedPromotionResultData;


/**
 *
 */
public class PromotionResultUtils
{
	private static final Logger LOG = LoggerFactory.getLogger(PromotionResultUtils.class);

	/**
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 *
	 */
	public static String parsePromotionToString(final List<OptimizedPromotionResultData> allPromotionResults)
	{

		final ObjectMapper mapper = new ObjectMapper();
		try
		{
			return mapper.writeValueAsString(allPromotionResults);
		}
		catch (final Exception e)
		{
			LOG.error("Promotion to String error", e);
		}
		return null;
	}

}
