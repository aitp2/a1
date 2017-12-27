/**
 *  Copyright (c) 2016 LIBY Group Ltd.,
 *  All rights reserved.
 *  @author: accenture zhidong.peng
 *  @date: Dec 23, 2016
 */
package com.acn.ai.util.client;

import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.io.IOException;
import java.util.Random;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;

import com.acn.ai.util.httpclient.HttpProtocolHandler;
import com.acn.ai.util.httpclient.HttpRequest;
import com.acn.ai.util.httpclient.HttpResponse;
import com.acn.ai.util.httpclient.HttpResultType;


/**
 * LibySMSClientImpl
 *
 * @author: accenture zhidong.peng
 */
public class AiSMSClient
{
	/** The http handler. */
	private HttpProtocolHandler httpProtocolHandler;

	/** The url. */
	private String url;

	/** The password. */
	private String password;

	/** The loginName. */
	private String loginName;

	/** The spCode. */
	private String spCode;

	private ConfigurationService configurationService;


	public String sendSMS(final String mobileNumber, final String message) throws HttpException, IOException
	{
		final String encoding = "GBK";
		final HttpRequest request = new HttpRequest(HttpResultType.BYTES);
		final NameValuePair[] parameters = new NameValuePair[8];

		//set parameter
		parameters[0] = createNameValuePair("SpCode", spCode);
		parameters[1] = createNameValuePair("LoginName", loginName);
		parameters[2] = createNameValuePair("Password", password);
		parameters[3] = createNameValuePair("MessageContent", message);
		parameters[4] = createNameValuePair("UserNumber", mobileNumber);
		parameters[5] = createNameValuePair("SerialNumber", getSerialNumber(20));
		//final String scheduleTime = LibyDateUtil.format(new Date(), LibyDateUtil.DATE_FORMAT_SCHEDULETIME);//df.format(Timestamp.valueOf("2013-05-21 17:30:01"));
		parameters[6] = createNameValuePair("ScheduleTime", "");//棰勭害鍙戦�佹椂闂�
		parameters[7] = createNameValuePair("f", "1");
		request.setCharset(encoding);
		request.setParameters(parameters);
		request.setUrl(url);
		request.setMethod(HttpRequest.METHOD_POST);
		final HttpResponse response = httpProtocolHandler.execute(request);
		return new String(response.getByteResult(), encoding);
	}

	/**
	 * create NameValuePair
	 */
	public NameValuePair createNameValuePair(final String name, final String value)
	{
		final NameValuePair parameter = new NameValuePair();
		parameter.setName(name);
		parameter.setValue(value);
		return parameter;
	}

	/**
	 * get SerialNumber
	 */
	public static String getSerialNumber(final int len)
	{
		final Random random = new Random();
		String str = String.valueOf(System.currentTimeMillis());
		if (str.length() == len)
		{
			return str;
		}
		else if (str.length() > len)
		{
			return str.substring(0, len);
		}
		else
		{
			for (int i = str.length(); i < len; i++)
			{
				str += random.nextInt(9);
			}
			return str;
		}
	}

	/**
	 * @return the url
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * @param url
	 *           the url to set
	 */
	public void setUrl(final String url)
	{
		this.url = url;
	}

	/**
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @param password
	 *           the password to set
	 */
	public void setPassword(final String password)
	{
		this.password = password;
	}

	/**
	 * @return the loginName
	 */
	public String getLoginName()
	{
		return loginName;
	}

	/**
	 * @param loginName
	 *           the loginName to set
	 */
	public void setLoginName(final String loginName)
	{
		this.loginName = loginName;
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
	 * @return the spCode
	 */
	public String getSpCode()
	{
		return spCode;
	}

	/**
	 * @param spCode
	 *           the spCode to set
	 */
	public void setSpCode(final String spCode)
	{
		this.spCode = spCode;
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
