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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acn.ai.util.exception.AiJsonConverterException;
import com.acn.ai.util.helper.HttpHelper;
import com.acn.ai.util.httpclient.HttpProtocolHandler;
import com.acn.ai.util.httpclient.HttpRequest;
import com.acn.ai.util.httpclient.HttpResponse;
import com.acn.ai.util.httpclient.HttpResultType;
import com.acn.ai.util.json.JacksonHelper;


/**
 * @author kevin modified by charles
 *
 */
abstract public class AiRestfulClient
{

	/** The Constant LOG. */
	private final static Logger LOG = LoggerFactory.getLogger(AiRestfulClient.class);

	private HttpProtocolHandler httpProtocolHandler;
	private ConfigurationService configurationService;
	/** The default connection timeout. */
	protected int defaultConnectionTimeout = 8000;
	protected int defaultSoTimeout = 30000;

	protected <T> T getResponse(final Map<String, String> urlVariables, final Class<T> beanType)
	{
		return getResponse(urlVariables, beanType, HttpRequest.METHOD_GET);
	}

	protected <T> T getResponse(final Map<String, String> urlVariables, final Class<T> beanType, final String method)
	{
		final HttpRequest request = new HttpRequest(HttpResultType.STRING);
		request.setCharset("UTF-8");
		request.setUrl(getRequestUrl());
		request.setMethod(method);
		return getResponse(urlVariables, beanType, method, request);
	}

	protected <T> T getResponse(final Map<String, String> urlVariables, final Class<T> beanType, final String method,
			final HttpRequest request)
	{
		HttpResponse response = null;
		try
		{
			if (HttpRequest.METHOD_GET.equals(method))
			{
				request.setQueryString(HttpHelper.generateQueryString(urlVariables, "UTF-8"));
			}
			else
			{
				request.setParameters(JacksonHelper.generatNameValuePair(urlVariables));
			}
			final long startTime = System.currentTimeMillis();
			response = httpProtocolHandler.execute(request);
			final long endTime = System.currentTimeMillis();
			if (LOG.isDebugEnabled())
			{
				LOG.debug("finished to {} request. consume time: {} ms", beanType.getSimpleName(), Long.valueOf(endTime - startTime));
			}
			if (HttpStatus.SC_OK == response.getStatusCode())
			{
				LOG.info("Rest response:[{}]", response.getStringResult());
				return parseJSON(response.getStringResult(), beanType);
			}
			else
			{
				LOG.error("WS call fail: {}, response status code is {}", urlVariables, String.valueOf(response.getStatusCode()));
			}
		}
		catch (final Exception e)
		{
			LOG.error("WS call exception: {}, {}", urlVariables, e);
		}
		return null;
	}

	protected <T> T checkCreditPost(final String params, final Class<T> beanType, final int outTime)
	{
		final String strURL = getRequestUrl();
		LOG.info("(ZFWG)check credit strURL:" + strURL);
		LOG.info("(ZFWG)check credit params:" + params);
		try
		{
			final URL url = new URL(strURL);// 创建连接
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestMethod("POST"); // 设置请求方式
			connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格�?
			connection.setRequestProperty("Content-Type", "application/json"); // 设置发�?�数据的格式
			connection.setConnectTimeout(outTime);
			connection.setReadTimeout(outTime);
			connection.connect();
			final OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"); // utf-8编码
			out.append(params);
			out.flush();
			out.close();
			// 读取响应
			final InputStreamReader in = new InputStreamReader(connection.getInputStream());
			final BufferedReader br = new BufferedReader(in);
			String result = "";
			String readerLine = null;
			while ((readerLine = br.readLine()) != null)
			{
				result += readerLine;
			}
			in.close();
			LOG.info("(ZFWG)check credit:" + result);
			return parseJSON(result, beanType);
		}
		catch (final AiJsonConverterException e)
		{
			// YTODO Auto-generated catch block
			LOG.error("ZFWG call exception: {}, {}", params, e);
			e.printStackTrace();
		}
		catch (final IOException e)
		{
			LOG.error("ZFWG call fail: connect error");
			// YTODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	protected String post(final String strURL, final String params)
	{
		LOG.info("sap store strURL:" + strURL);
		LOG.info("sap store params:" + params);
		try
		{
			final URL url = new URL(strURL);// 创建连接
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestMethod("POST"); // 设置请求方式
			connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格�?
			connection.setRequestProperty("Content-Type", "application/json"); // 设置发�?�数据的格式
			connection.setConnectTimeout(defaultConnectionTimeout);
			connection.setReadTimeout(defaultSoTimeout);
			connection.connect();
			final OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"); // utf-8编码
			out.append(params);
			out.flush();
			out.close();
			// 读取响应
			final InputStreamReader in = new InputStreamReader(connection.getInputStream());
			final BufferedReader br = new BufferedReader(in);
			String result = "";
			String readerLine = null;
			while ((readerLine = br.readLine()) != null)
			{
				result += readerLine;
			}
			in.close();
			LOG.info("sap store:" + result);
			return result;
		}
		catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "error"; // 自定义错误信�?
	}

	protected <T> T getResponse(final Map<String, String> urlVariables, final Class<T> beanType, final String method,
			final int outTime)
	{
		final HttpRequest request = new HttpRequest(HttpResultType.STRING);
		request.setCharset("UTF-8");
		request.setUrl(getRequestUrl());
		request.setMethod(method);
		request.setTimeout(outTime);

		return getResponse(urlVariables, beanType, method, request);
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
