/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2016 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package com.acn.ai.util.client;

import de.hybris.platform.servicelayer.config.ConfigurationService;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acn.ai.util.exception.AiJsonConverterException;
import com.acn.ai.util.httpclient.HttpProtocolHandler;
import com.acn.ai.util.json.JacksonHelper;


/**
 * @author kevin modified by charles
 *
 */
abstract public class AiWSDLClient
{

	/** The Constant LOG. */
	private final static Logger LOG = LoggerFactory.getLogger(AiWSDLClient.class);

	private HttpProtocolHandler httpProtocolHandler;
	private ConfigurationService configurationService;
	/** The default connection timeout. */
	protected int defaultConnectionTimeout = 8000;
	protected int defaultSoTimeout = 30000;

	protected String wsdlClient(final String strURL, final String method, final String params)
	{
		LOG.info("wsdlClient strURL:" + strURL);
		LOG.info("wsdlClient method:" + method);
		LOG.info("wsdlClient params:" + params);
		try
		{
			final JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
			final Client client = dcf.createClient(strURL);
			//sayHello 涓烘帴鍙ｄ腑瀹氫箟鐨勬柟娉曞悕绉�   寮犱笁涓轰紶閫掔殑鍙傛暟   杩斿洖涓�涓狾bject鏁扮粍
			final Object[] objects = client.invoke(method, params);
			//杈撳嚭璋冪敤缁撴灉
			final String result = objects[0].toString();
			LOG.info("wsdlClient result:" + result);
			return result;
		}
		catch (final Exception e)
		{
			LOG.error("wsdlClient error", e);
		}
		return null;
	}

	protected <T> T parseJSON(final String jsonStr, final Class<T> beanType) throws AiJsonConverterException
	{
		return JacksonHelper.fromJSON(jsonStr, beanType);
	}

	abstract protected String getPropertyKey();

	protected String getRequestUrl()
	{
		return getConfigurationService().getConfiguration().getString(getPropertyKey());
	}

	public HttpProtocolHandler getHttpProtocolHandler()
	{
		return httpProtocolHandler;
	}

	/**
	 * @param httpProtocolHandler
	 *           the httpProtocolHandler to set
	 */
	public void setHttpProtocolHandler(final HttpProtocolHandler httpProtocolHandler)
	{
		this.httpProtocolHandler = httpProtocolHandler;
	}

	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	/**
	 * @param configurationService
	 *           the configurationService to set
	 */
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	/**
	 * @return the defaultConnectionTimeout
	 */
	public int getDefaultConnectionTimeout()
	{
		return defaultConnectionTimeout;
	}

	/**
	 * @param defaultConnectionTimeout
	 *           the defaultConnectionTimeout to set
	 */
	public void setDefaultConnectionTimeout(final int defaultConnectionTimeout)
	{
		this.defaultConnectionTimeout = defaultConnectionTimeout;
	}

	/**
	 * @return the defaultSoTimeout
	 */
	public int getDefaultSoTimeout()
	{
		return defaultSoTimeout;
	}

	/**
	 * @param defaultSoTimeout
	 *           the defaultSoTimeout to set
	 */
	public void setDefaultSoTimeout(final int defaultSoTimeout)
	{
		this.defaultSoTimeout = defaultSoTimeout;
	}


}
