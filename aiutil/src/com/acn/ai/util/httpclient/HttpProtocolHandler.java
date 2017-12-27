/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture zhidong.peng
 *  @date: 13 Dec 2016
 */
package com.acn.ai.util.httpclient;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.httpclient.HttpException;


/**
 * packaging HttpClient.
 */
public interface HttpProtocolHandler
{

	/** The Constant DEFAULT_CHARSET. */
	public final static String DEFAULT_CHARSET = "UTF-8";

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
	public HttpResponse execute(final HttpRequest request, final String body, final String username, final String password,
			String contentType) throws URISyntaxException, IOException;


	/**
	 * Mainly serve the request body as json or XML request.
	 *
	 * @param request
	 *           request
	 * @return HttpResponse
	 * @throws HttpException
	 *            the http exception
	 * @throws IOException
	 *            Signals that an I/O exception has occurred.
	 */
	public HttpResponse execute(final HttpRequest request) throws HttpException, IOException;

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
	public HttpResponse execute(final HttpRequest request, final String strParaFileName, final String strFilePath)
			throws HttpException, IOException;

	/**
	 * run request by credential.
	 *
	 * @param request
	 *           the request
	 * @param username
	 *           the username
	 * @param password
	 *           the password
	 * @return HttpResponse
	 * @throws HttpException
	 *            the http exception
	 * @throws IOException
	 *            Signals that an I/O exception has occurred.
	 * @throws URISyntaxException
	 *            the URI syntax exception
	 */
	public HttpResponse executeByCredential(final HttpRequest request, final String username, final String password)
			throws HttpException, IOException, URISyntaxException;

	/**
	 * Execute.
	 *
	 * @param request
	 *           the request
	 * @param body
	 *           the body
	 * @param username
	 *           the username
	 * @param password
	 *           the password
	 * @param contentType
	 *           the content type
	 * @param scheme
	 *           the scheme
	 * @return HttpResponse
	 * @throws IOException
	 *            Signals that an I/O exception has occurred.
	 */
	HttpResponse execute(HttpRequest request, String body, String username, String password, String contentType, String scheme)
			throws IOException;
}
