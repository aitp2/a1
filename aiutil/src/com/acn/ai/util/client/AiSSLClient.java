/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture l.jin
 *  @date: Feb 23, 2017
 */
package com.acn.ai.util.client;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.acn.ai.util.httpclient.HttpSSLClient;


/**
 *
 */
public class AiSSLClient
{
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
}
