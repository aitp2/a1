/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture l.jin
 *  @date: Feb 23, 2017
 */
package com.acn.ai.util.client;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.acn.ai.util.httpclient.HttpSSLClient;


/**
 *
 */
public class AiSSLClient
{
	private static final Logger LOG = Logger.getLogger(AiSSLClient.class);
	
	public String doPost(final String url, final String jsonParam, final String charset)
	{
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		try
		{
			httpClient = new HttpSSLClient();
			httpPost = new HttpPost(url);
			httpPost.setHeader("Content-Type", "application/json");
			/*
			 * //璁剧疆鍙傛暟 List<NameValuePair> list = new ArrayList<NameValuePair>(); Iterator iterator =
			 * map.entrySet().iterator(); while(iterator.hasNext()){ Entry<String,String> elem = (Entry<String, String>)
			 * iterator.next(); list.add(new BasicNameValuePair(elem.getKey(),elem.getValue())); }
			 */
			//final JSONObject json = new JSONObject(map);
			final StringEntity entity = new StringEntity(jsonParam, "utf-8");
			entity.setContentEncoding("UTF-8");
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			final HttpResponse response = httpClient.execute(httpPost);
			if (response != null)
			{
				final HttpEntity resEntity = response.getEntity();
				if (resEntity != null)
				{
					result = EntityUtils.toString(resEntity, charset);
				}
			}
		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	public String doPost(final String url, final String jsonParam, final String charset, final String token)
	{
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		try
		{
			httpClient = new HttpSSLClient();
			httpPost = new HttpPost(url);
			httpPost.setHeader("Content-Type", "application/json");
			/*
			 * //设置参数 List<NameValuePair> list = new ArrayList<NameValuePair>(); Iterator iterator =
			 * map.entrySet().iterator(); while(iterator.hasNext()){ Entry<String,String> elem = (Entry<String, String>)
			 * iterator.next(); list.add(new BasicNameValuePair(elem.getKey(),elem.getValue())); }
			 */
			//final JSONObject json = new JSONObject(map);
			final StringEntity entity = new StringEntity(jsonParam, "utf-8");
			entity.setContentEncoding("UTF-8");
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			httpPost.addHeader("Authorization", "Bearer " + token);
			final HttpResponse response = httpClient.execute(httpPost);
			if (response != null)
			{
				final HttpEntity resEntity = response.getEntity();
				if (resEntity != null)
				{
					result = EntityUtils.toString(resEntity, charset);
				}
			}
		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	public String doPurge(final String url, final String charset) throws KeyManagementException, NoSuchAlgorithmException, ClientProtocolException, IOException {
		// 采用绕过验证的方式处理https请求
		SSLContext sslcontext = createIgnoreVerifySSL();
		
		final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, NoopHostnameVerifier.INSTANCE);

		// 设置协议http和https对应的处理socket链接工厂的对象
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", sslsf)
				.build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

		// 创建自定义的httpclient对象
		CloseableHttpClient httpclient = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.setConnectionManager(connManager)
				.build();
		String resultString = "";
		CloseableHttpResponse response = null;
		try {
			final HttpPurge httpPurge = new HttpPurge(url);
			response = httpclient.execute(httpPurge);
			LOG.info("status:" + response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				resultString = EntityUtils.toString(response.getEntity(), charset);
			}
		}  finally {
			try {
				if (response != null) {
					response.close();
				}
				httpclient.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return resultString;
	}
	
	public String doGet(final String url, final String charset)
	{
		final CloseableHttpClient httpclient = HttpClients.createDefault();
		String resultString = "";
		CloseableHttpResponse response = null;
		try
		{
			final URIBuilder builder = new URIBuilder(url);
			final URI uri = builder.build();
			final HttpGet httpGet = new HttpGet(uri);
			response = httpclient.execute(httpGet);
			// 鍒ゆ柇杩斿洖鐘舵�佹槸鍚︿负200
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
			{
				resultString = EntityUtils.toString(response.getEntity(), charset);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (response != null)
				{
					response.close();
				}
				httpclient.close();
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
		return resultString;
	}
	
	/** 
	 * 绕过验证 
	 *   
	 * @return 
	 * @throws NoSuchAlgorithmException  
	 * @throws KeyManagementException  
	 */  
	public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {  
	    SSLContext sc = SSLContext.getInstance("SSLv3");  
	  
	    // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法  
	    X509TrustManager trustManager = new X509TrustManager() {  
	        @Override  
	        public void checkClientTrusted(  
	                java.security.cert.X509Certificate[] paramArrayOfX509Certificate,  
	                String paramString) throws CertificateException {  
	        }  
	  
	        @Override  
	        public void checkServerTrusted(  
	                java.security.cert.X509Certificate[] paramArrayOfX509Certificate,  
	                String paramString) throws CertificateException {  
	        }  
	  
	        @Override  
	        public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
	            return null;  
	        }  
	    };  
	  
	    sc.init(null, new TrustManager[] { trustManager }, null);  
	    return sc;  
	}  
}
