/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture zhidong.peng
 *  @date: 13 Dec 2016
 */
package com.acn.ai.util.httpclient.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.util.IdleConnectionTimeoutThread;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.acn.ai.util.exception.AiHystrixTimeOutException;
import com.acn.ai.util.exception.AiTaskRunnerException;
import com.acn.ai.util.helper.HttpHelper;
import com.acn.ai.util.httpclient.HttpProtocolHandler;
import com.acn.ai.util.httpclient.HttpRequest;
import com.acn.ai.util.httpclient.HttpResponse;
import com.acn.ai.util.httpclient.HttpResultType;
import com.acn.ai.util.httpclient.auth.WSSEAuthScheme;
import com.acn.ai.util.hystrix.AbstractHystrixWSCommand;
import com.acn.ai.util.hystrix.AiHystrixConfig;

import net.sourceforge.pmd.util.StringUtil;


/**
 * packaging HttpClient.
 */
public final class HttpProtocolHandlerImpl implements HttpProtocolHandler
{

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(HttpProtocolHandlerImpl.class);

	/** The Constant DEFAULT_CHARSET. */
	public final static String DEFAULT_CHARSET = "UTF-8";

	/** The Constant CONTENT_LENGTH. */
	public final static String CONTENT_LENGTH = "Content-Length";

	/** The Constant CONTENT_TYPE. */
	public final static String CONTENT_TYPE = "Content-Type";

	/** The default connection timeout. */
	private int defaultConnectionTimeout = 8000;

	/** The default so timeout. */
	private int defaultSoTimeout = 30000;

	/** The default idle conn timeout. */
	private int defaultIdleConnTimeout = 60000;

	/** The default max conn per host. */
	private int defaultMaxConnPerHost = 30;

	/** The default max total conn. */
	private int defaultMaxTotalConn = 80;

	/** The default http connection manager timeout. */
	private long defaultHttpConnectionManagerTimeout = 3L * 1000;

	private int defaultThreadPoolCoreSize = 50;

	/** The connection manager. */
	private HttpConnectionManager connectionManager;


	/**
	 * Instantiates a new http protocol handler impl.
	 */
	public HttpProtocolHandlerImpl()
	{
		connectionManager = new MultiThreadedHttpConnectionManager();
		connectionManager.getParams().setDefaultMaxConnectionsPerHost(defaultMaxConnPerHost);
		connectionManager.getParams().setMaxTotalConnections(defaultMaxTotalConn);


		final IdleConnectionTimeoutThread ict = new IdleConnectionTimeoutThread();
		ict.addConnectionManager(connectionManager);
		ict.setConnectionTimeout(defaultIdleConnTimeout);

		ict.start();
	}

	/**
	 * Mainly serve the request body as json or XML request.
	 *
	 * @param request
	 *           request
	 * @param body
	 *           request body
	 * @param username
	 *           BasicAuth user name
	 * @param password
	 *           BasicAuth password
	 * @param contentType
	 *           request content type
	 * @param scheme
	 *           the scheme
	 * @return HttpResponse
	 * @throws IOException
	 *            Signals that an I/O exception has occurred.
	 */
	@Override
	public HttpResponse execute(final HttpRequest request, final String body, final String username, final String password,
			String contentType, final String scheme) throws IOException
	{
		final HttpClient httpclient = getHttpClient(request);

		String charset = request.getCharset();
		charset = charset == null ? DEFAULT_CHARSET : charset;
		request.setCharset(charset);

		HttpMethodBase httpMethodBase;
		if (HttpRequest.METHOD_POST.equals(request.getMethod()))
		{
			httpMethodBase = new PostMethod(request.getUrl());
		}
		else if (HttpRequest.METHOD_PUT.equals(request.getMethod()))
		{
			httpMethodBase = new PutMethod(request.getUrl());
		}
		else
		{
			httpMethodBase = new GetMethod(request.getUrl());
		}

		if (!StringUtils.isNotBlank(contentType))
		{
			contentType = request.getContentType();
		}
		httpMethodBase.setDoAuthentication(true);
		httpMethodBase.addRequestHeader("Content-Type", contentType);
		// init query string for method
		addQueryStringForMethod(request, httpMethodBase);
		// init body string for method
		addRequestEntityForEntityEnclosingMethod(request, httpMethodBase, body);

		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password))
		{
			if (WSSEAuthScheme.SCHEME_NAME.equals(scheme))
			{
				httpMethodBase.addRequestHeader(WSSEAuthScheme.SCHEME_NAME,
						new WSSEAuthScheme().authenticate(new UsernamePasswordCredentials(username, password), "UTF-8"));
			}
			else
			{
				httpMethodBase.addRequestHeader("User-Agent", "Mozilla/4.0");
				httpMethodBase.addRequestHeader("Accept-Encoding", "gzip,deflate");

				final Credentials defaultcreds = new UsernamePasswordCredentials(username, password);

				try
				{
					final URI uri = new URI(request.getUrl());
					httpclient.getState().setCredentials(new AuthScope(uri.getHost(), uri.getPort(), AuthScope.ANY_REALM),
							defaultcreds);
				}
				catch (final URISyntaxException e)
				{
					throw new IOException(e);
				}
			}
		}

		return outputResponse(httpclient, httpMethodBase, request);
	}

	/**
	 * Mainly serve the request body as json or XML request.
	 *
	 * @param request
	 *           request
	 * @param body
	 *           request body
	 * @param username
	 *           BasicAuth user name
	 * @param password
	 *           BasicAuth password
	 * @param contentType
	 *           request content type
	 * @return HttpResponse
	 * @throws URISyntaxException
	 *            the URI syntax exception
	 * @throws IOException
	 *            Signals that an I/O exception has occurred.
	 */
	@Override
	public HttpResponse execute(final HttpRequest request, final String body, final String username, final String password,
			final String contentType) throws URISyntaxException, IOException
	{
		return execute(request, body, username, password, contentType, "");
	}

	/**
	 * init query string for http method.
	 *
	 * @author mingming.wang
	 * @param request
	 *           the request
	 * @param httpMethodBase
	 *           the http method base
	 */
	protected void addQueryStringForMethod(final HttpRequest request, final HttpMethod httpMethodBase)
	{
		if (request.getParameters() != null)
		{
			httpMethodBase.setQueryString(request.getParameters());
		}

		if (request.getQueryString() != null)
		{
			httpMethodBase.setQueryString(request.getQueryString());
		}
	}

	/**
	 * init enclosing string body if request method instanceof EntityEnclosingMethod.
	 *
	 * @author mingming
	 * @param request
	 *           the request
	 * @param httpMethodBase
	 *           the http method base
	 * @param body
	 *           the body
	 * @throws UnsupportedEncodingException
	 *            the unsupported encoding exception
	 */
	protected void addRequestEntityForEntityEnclosingMethod(final HttpRequest request, final HttpMethodBase httpMethodBase,
			final String body) throws UnsupportedEncodingException
	{
		if (httpMethodBase instanceof EntityEnclosingMethod && StringUtils.isNotEmpty(body))
		{
			final EntityEnclosingMethod entityEnclosingMethod = (EntityEnclosingMethod) httpMethodBase;
			final String charset = request.getCharset() == null ? DEFAULT_CHARSET : request.getCharset();
			final RequestEntity requestEntity = new StringRequestEntity(body, request.getContentType(), charset);

			entityEnclosingMethod.setRequestEntity(requestEntity);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.liby.b2b.util.httpclient.HttpProtocolHandler#execute(com.liby.b2b.util.httpclient.HttpRequest)
	 */
	@Override
	public HttpResponse execute(final HttpRequest request) throws HttpException, IOException
	{
		return this.execute(request, "", "");
	}

	/**
	 * run http request.
	 *
	 * @param request
	 *           the request
	 * @param strParaFileName
	 *           file Name
	 * @param strFilePath
	 *           file Path
	 * @return HttpResponse
	 * @throws HttpException
	 *            , IOException
	 * @throws IOException
	 *            Signals that an I/O exception has occurred.
	 */
	@Override
	public HttpResponse execute(final HttpRequest request, final String strParaFileName, final String strFilePath)
			throws HttpException, IOException
	{

		final HttpClient httpclient = getHttpClient(request);

		String charset = request.getCharset();
		charset = charset == null ? DEFAULT_CHARSET : charset;
		HttpMethod method = null;

		//get and no file upload
		if (HttpRequest.METHOD_GET.equals(request.getMethod()))
		{
			method = new GetMethod(request.getUrl());
			method.getParams().setCredentialCharset(charset);
			//add by james.shi 20160928
			if (StringUtils.isNotEmpty(request.getQueryString()))
			{
				method.setQueryString(request.getQueryString());
			}
		}
		else if ("".equals(strParaFileName) && "".equals(strFilePath))
		{
			//post and no file upload
			method = new PostMethod(request.getUrl());
			if ((request.getParameters() == null || request.getParameters().length <= 0)
					&& StringUtil.isNotEmpty(request.getParametersStr()))
			{
				((PostMethod) method).setRequestEntity(new StringRequestEntity(request.getParametersStr(),
						"application/x-www-form-urlencoded; text/html; charset=" + charset, charset));
			}
			else
			{
				((PostMethod) method).addParameters(request.getParameters());
			}
			method.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; text/html; charset=" + charset);
		}
		else
		{
			//post and have file to upload
			method = new PostMethod(request.getUrl());
			final List<Part> parts = new ArrayList<Part>();
			for (int i = 0; i < request.getParameters().length; i++)
			{
				parts.add(new StringPart(request.getParameters()[i].getName(), request.getParameters()[i].getValue(), charset));
			}
			parts.add(new FilePart(strParaFileName, new FilePartSource(new File(strFilePath))));

			((PostMethod) method).setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[0]), new HttpMethodParams()));
		}

		method.addRequestHeader("User-Agent", "Mozilla/4.0");

		//add by james.shi for add parameter to headers
		final Map<String, String> headers = request.getHeaders();
		if (null != headers && headers.keySet() != null)
		{
			for (final String key : headers.keySet())
			{
				method.addRequestHeader(key, headers.get(key));
			}
		}

		return outputResponse(httpclient, method, request);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.liby.b2b.util.httpclient.HttpProtocolHandler#executeByCredential(com.liby.b2b.util.httpclient.HttpRequest,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public HttpResponse executeByCredential(final HttpRequest request, final String username, final String password)
			throws HttpException, IOException, URISyntaxException
	{
		final HttpClient httpclient = getHttpClient(request);
		String charset = request.getCharset();
		charset = charset == null ? DEFAULT_CHARSET : charset;
		HttpMethod method = null;
		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password))
		{
			httpclient.getParams().setAuthenticationPreemptive(true);
			final Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
			final URI uri = new URI(request.getUrl());
			httpclient.getState().setCredentials(new AuthScope(uri.getHost(), uri.getPort(), AuthScope.ANY_REALM), defaultcreds);
		}
		//get and no file upload
		if (HttpRequest.METHOD_GET.equals(request.getMethod()))
		{
			method = new GetMethod(request.getUrl());
			method.getParams().setCredentialCharset(charset);
			method.setQueryString(request.getQueryString());
		}
		else
		{
			//post and no file upload
			method = new PostMethod(request.getUrl());
			if ((request.getParameters() == null || request.getParameters().length <= 0)
					&& StringUtil.isNotEmpty(request.getParametersStr()))
			{
				((PostMethod) method).setRequestEntity(new StringRequestEntity(request.getParametersStr(),
						"application/x-www-form-urlencoded; text/html; charset=" + charset, charset));
			}
			else
			{
				((PostMethod) method).addParameters(request.getParameters());
			}
			method.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; text/html; charset=" + charset);
		}

		method.addRequestHeader("User-Agent", "Mozilla/4.0");
		return outputResponse(httpclient, method, request);
	}

	/**
	 * get HttpClient.
	 *
	 * @param request
	 *           the request
	 * @return HttpClient
	 */
	private HttpClient getHttpClient(final HttpRequest request)
	{
		final HttpClient httpclient = new HttpClient(connectionManager);

		int connectionTimeout = defaultConnectionTimeout;
		if (request.getConnectionTimeout() > 0)
		{
			connectionTimeout = request.getConnectionTimeout();
		}
		httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(connectionTimeout);

		int soTimeout = defaultSoTimeout;
		if (request.getTimeout() > 0)
		{
			soTimeout = request.getTimeout();
		}
		httpclient.getHttpConnectionManager().getParams().setSoTimeout(soTimeout);

		httpclient.getParams().setConnectionManagerTimeout(defaultHttpConnectionManagerTimeout);

		return httpclient;
	}

	/**
	 * output response that have changed the charset and.
	 *
	 * @param httpclient
	 *           the httpclient
	 * @param method
	 *           the method
	 * @param request
	 *           the request
	 * @return HttpResponse
	 * @throws IOException
	 *            Signals that an I/O exception has occurred.
	 */
	private HttpResponse outputResponse(final HttpClient httpclient, final HttpMethod method, final HttpRequest request)
			throws IOException
	{
		final AiHystrixConfig config = new AiHystrixConfig();
		config.setGroupKey(getKeyFromUrl(request.getUrl()));
		config.setThreadPoolKey(getKeyFromUrl(request.getUrl()));
		final int connectionTimeout = request.getConnectionTimeout();
		if (connectionTimeout > 0)
		{
			config.setThreadTimeout(connectionTimeout);
		}
		else
		{
			config.setThreadTimeout(defaultConnectionTimeout);
		}
		config.setThreadPoolCoreSize(defaultThreadPoolCoreSize);
		final HystrixHttpClientCommand command = new HystrixHttpClientCommand(config, httpclient, method, request);
		return command.execute();
	}

	/**
	 * Gets the key from url.
	 *
	 * @param url
	 *           the url
	 * @return the key from url
	 */
	protected String getKeyFromUrl(final String url)
	{
		String key = null;
		if (null != url)
		{
			if (url.contains("?"))
			{
				key = url.substring(0, url.indexOf("?"));
			}
			else
			{
				key = url;
			}
		}
		return key;
	}

	/**
	 * To string.
	 *
	 * @param nameValues
	 *           the name values
	 * @return the string
	 */
	protected String toString(final NameValuePair[] nameValues)
	{
		if (nameValues == null || nameValues.length == 0)
		{
			return "null";
		}

		final StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < nameValues.length; i++)
		{
			final NameValuePair nameValue = nameValues[i];

			if (i == 0)
			{
				stringBuilder.append(nameValue.getName()).append("=").append(nameValue.getValue());
			}
			else
			{
				stringBuilder.append("&" + nameValue.getName() + "=" + nameValue.getValue());
			}
		}

		return stringBuilder.toString();
	}


	/**
	 * Gets the default connection timeout.
	 *
	 * @return the defaultConnectionTimeout
	 */
	public int getDefaultConnectionTimeout()
	{
		return defaultConnectionTimeout;
	}


	/**
	 * Sets the default connection timeout.
	 *
	 * @param defaultConnectionTimeout
	 *           the defaultConnectionTimeout to set
	 */
	public void setDefaultConnectionTimeout(final int defaultConnectionTimeout)
	{
		this.defaultConnectionTimeout = defaultConnectionTimeout;
	}


	/**
	 * Gets the default so timeout.
	 *
	 * @return the defaultSoTimeout
	 */
	public int getDefaultSoTimeout()
	{
		return defaultSoTimeout;
	}


	/**
	 * Sets the default so timeout.
	 *
	 * @param defaultSoTimeout
	 *           the defaultSoTimeout to set
	 */
	public void setDefaultSoTimeout(final int defaultSoTimeout)
	{
		this.defaultSoTimeout = defaultSoTimeout;
	}


	/**
	 * Gets the default idle conn timeout.
	 *
	 * @return the defaultIdleConnTimeout
	 */
	public int getDefaultIdleConnTimeout()
	{
		return defaultIdleConnTimeout;
	}


	/**
	 * Sets the default idle conn timeout.
	 *
	 * @param defaultIdleConnTimeout
	 *           the defaultIdleConnTimeout to set
	 */
	public void setDefaultIdleConnTimeout(final int defaultIdleConnTimeout)
	{
		this.defaultIdleConnTimeout = defaultIdleConnTimeout;
	}


	/**
	 * Gets the default max conn per host.
	 *
	 * @return the defaultMaxConnPerHost
	 */
	public int getDefaultMaxConnPerHost()
	{
		return defaultMaxConnPerHost;
	}


	/**
	 * Sets the default max conn per host.
	 *
	 * @param defaultMaxConnPerHost
	 *           the defaultMaxConnPerHost to set
	 */
	public void setDefaultMaxConnPerHost(final int defaultMaxConnPerHost)
	{
		this.defaultMaxConnPerHost = defaultMaxConnPerHost;
	}


	/**
	 * Gets the default max total conn.
	 *
	 * @return the defaultMaxTotalConn
	 */
	public int getDefaultMaxTotalConn()
	{
		return defaultMaxTotalConn;
	}


	/**
	 * Sets the default max total conn.
	 *
	 * @param defaultMaxTotalConn
	 *           the defaultMaxTotalConn to set
	 */
	public void setDefaultMaxTotalConn(final int defaultMaxTotalConn)
	{
		this.defaultMaxTotalConn = defaultMaxTotalConn;
	}


	/**
	 * Gets the default http connection manager timeout.
	 *
	 * @return the defaultHttpConnectionManagerTimeout
	 */
	public long getDefaultHttpConnectionManagerTimeout()
	{
		return defaultHttpConnectionManagerTimeout;
	}


	/**
	 * Sets the default http connection manager timeout.
	 *
	 * @param defaultHttpConnectionManagerTimeout
	 *           the defaultHttpConnectionManagerTimeout to set
	 */
	public void setDefaultHttpConnectionManagerTimeout(final long defaultHttpConnectionManagerTimeout)
	{
		this.defaultHttpConnectionManagerTimeout = defaultHttpConnectionManagerTimeout;
	}

	/**
	 * Gets the connection manager.
	 *
	 * @return the connectionManager
	 */
	public HttpConnectionManager getConnectionManager()
	{
		return connectionManager;
	}

	/**
	 * Sets the connection manager.
	 *
	 * @param connectionManager
	 *           the connectionManager to set
	 */
	public void setConnectionManager(final HttpConnectionManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}

	/**
	 * The Class HystrixHttpClientCommand.
	 */
	class HystrixHttpClientCommand extends AbstractHystrixWSCommand<HttpResponse>
	{

		/** The httpclient. */
		private final HttpClient httpclient;

		/** The method. */
		private final HttpMethod method;

		/** The request. */
		private final HttpRequest request;

		/**
		 * Set the configuration for Hystrix Command.
		 *
		 * @param config
		 *           configuration
		 * @param httpclient
		 *           the httpclient
		 * @param method
		 *           the method
		 * @param request
		 *           the request
		 */
		public HystrixHttpClientCommand(final AiHystrixConfig config, final HttpClient httpclient, final HttpMethod method,
				final HttpRequest request)
		{
			super(config);
			this.httpclient = httpclient;
			this.method = method;
			this.request = request;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.netflix.hystrix.HystrixCommand#getFallback()
		 */
		@Override
		protected HttpResponse getFallback()
		{
			throw new AiHystrixTimeOutException("httpClient timeout!");
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.netflix.hystrix.HystrixCommand#run()
		 */
		@Override
		protected HttpResponse run() throws AiTaskRunnerException
		{
			final HttpResponse response = new HttpResponse();
			InputStream is = null;
			ByteArrayOutputStream bos = null;
			try
			{

				httpclient.executeMethod(method);

				is = method.getResponseBodyAsStream();
				bos = new ByteArrayOutputStream();

				final byte[] b = new byte[2048];
				int len = 0;
				while ((len = is.read(b)) != -1)
				{
					bos.write(b, 0, len);
				}
				if (null != method.getResponseHeader(CONTENT_TYPE))
				{
					response.setContentType((method.getResponseHeader(CONTENT_TYPE).getValue()));
				}
				if (HttpResultType.STRING.equals(request.getResultType()))
				{
					final String charset = HttpHelper.getCharset(response.getContentType());
					if (charset == null)
					{
						response.setStringResult(new String(bos.toByteArray(), request.getCharset()));
					}
					else
					{
						response.setStringResult(new String(bos.toByteArray(), charset));
					}
				}
				else if (HttpResultType.BYTES.equals(request.getResultType()))
				{
					response.setByteResult(bos.toByteArray());
				}
				if (null != method.getResponseHeader(CONTENT_LENGTH))
				{
					response.setContentLength((Long.parseLong(method.getResponseHeader(CONTENT_LENGTH).getValue())));
				}
				response.setResponseHeaders(method.getResponseHeaders());
				response.setStatusCode(method.getStatusCode());
			}
			catch (final UnknownHostException ex)
			{
				LOG.error("catch error:", ex);
				throw new AiTaskRunnerException(ex);
			}
			catch (final IOException ex)
			{

				LOG.error("catch error:", ex);
				throw new AiTaskRunnerException(ex);
			}
			catch (final Exception ex)
			{
				LOG.error("catch error:", ex);
				throw ex;
			}
			finally
			{
				if (bos != null)
				{
					try
					{
						bos.close();
					}
					catch (final IOException e)
					{
						LOG.error("catch error:", e);
					}
				}
				if (is != null)
				{
					try
					{
						is.close();
					}
					catch (final IOException e)
					{
						LOG.error("catch error:", e);
					}
				}
				method.releaseConnection();
			}
			return response;
		}
	}

	/**
	 * @return the defaultThreadPoolCoreSize
	 */
	public int getDefaultThreadPoolCoreSize()
	{
		return defaultThreadPoolCoreSize;
	}

	/**
	 * @param defaultThreadPoolCoreSize
	 *           the defaultThreadPoolCoreSize to set
	 */
	public void setDefaultThreadPoolCoreSize(final int defaultThreadPoolCoreSize)
	{
		this.defaultThreadPoolCoreSize = defaultThreadPoolCoreSize;
	}

}
