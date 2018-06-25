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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import com.accenture.aitp.tailor.data.ModelMonitoredInfo;
import com.accenture.aitp.tailor.network.AiSSLClient;

/**
 *
 */
public class DefalutAitpProductCacheKeyParserImpl extends DefalutAitpAbstractCacheKeyParserImpl {
	private List<String> templates;

	@Override
	public List<String> invalidateUrl(final ModelMonitoredInfo info) {
		List<String> result = new ArrayList<>();
		VelocityEngine engine = new VelocityEngine();
		this.getTemplates().forEach(tmp -> {
			StringWriter writer = new StringWriter();
			engine.evaluate(info, writer, "", tmp);
			result.add(writer.toString());
		});

		return result;
	}

	public List<String> getTemplates() {
		return templates;
	}

	public void setTemplates(List<String> templates) {
		this.templates = templates;
	}

	public static void main(String[] avgs) {
		AiSSLClient client = new AiSSLClient();
				try {
					System.out.println(client.doGet("https://baidu.com", "UTF-8"));
				} catch (Exception e) {
					e.printStackTrace();
				}
	}
}
