package com.acn.ai.util.client;

import de.hybris.platform.servicelayer.config.ConfigurationService;

import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acn.ai.util.httpclient.HttpProtocolHandler;
import com.acn.ai.util.httpclient.HttpRequest;
import com.acn.ai.util.httpclient.HttpResponse;
import com.acn.ai.util.httpclient.HttpResultType;
import com.acn.ai.util.xml.XmlJaxbUtils;


/**
 * @author charles.chao.qian
 *
 */
abstract public class AiSoapClient
{

	/** The Constant LOG. */
	private final static Logger LOG = LoggerFactory.getLogger(AiSoapClient.class);

	private HttpProtocolHandler httpProtocolHandler;
	private ConfigurationService configurationService;

	protected <T> T getResponse(final String requestStr, final Class<T> beanType)
	{
		return getResponse(requestStr, beanType, "", "", "");
	}

	protected <T> T getResponse(final String requestStr, final Class<T> beanType, final String username, final String password,
			final String contentType)
	{
		final HttpRequest request = new HttpRequest(HttpResultType.STRING);
		request.setContentType("application/xml");
		request.setCharset("utf-8");
		request.setUrl(getRequestUrl());
		HttpResponse response = null;
		try
		{
			LOG.info("Soap request:[{}]", requestStr);
			response = httpProtocolHandler.execute(request, requestStr, username, password, contentType);
			if (HttpStatus.SC_OK == response.getStatusCode())
			{
				LOG.info("Soap response:[{}]", response.getStringResult());
				return parseXML(response.getStringResult(), beanType);
			}
			else
			{
				LOG.error("WS-SOAP call fail: {}, response status code is {}", requestStr, String.valueOf(response.getStatusCode()));
			}
		}
		catch (final Exception e)
		{
			LOG.error("WS-SOAP call exception: {}, {}", requestStr, e);
		}
		return null;
	}

	abstract protected String getPropertyKey();

	protected <T> T parseXML(final String xmlStr, final Class<T> beanType) throws JAXBException
	{
		return XmlJaxbUtils.unmarshal(xmlStr, beanType);
	}

	protected String getRequestUrl()
	{
		return getConfigurationService().getConfiguration().getString(getPropertyKey());
	}

	/**
	 * @return the httpProtocolHandler
	 */
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
}
