/**
 *  Copyright (c) 2017 LIBY Group Ltd.,
 *  All rights reserved.
 *  @author: accenture le.qi
 *  @date: Jan 13, 2017
 */
package com.acn.ai.util.httpclient;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;


//用于进行Https请求的HttpClient
public class HttpSSLClient extends DefaultHttpClient
{
	public HttpSSLClient() throws Exception
	{
		super();
		final SSLContext ctx = SSLContext.getInstance("TLS");
		final X509TrustManager tm = new X509TrustManager()
		{
			@Override
			public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException
			{
			}

			@Override
			public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException
			{
			}

			@Override
			public X509Certificate[] getAcceptedIssuers()
			{
				return null;
			}
		};
		ctx.init(null, new TrustManager[]
		{ tm }, null);
		final SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		final ClientConnectionManager ccm = this.getConnectionManager();
		final SchemeRegistry sr = ccm.getSchemeRegistry();
		sr.register(new Scheme("https", 443, ssf));
	}
}