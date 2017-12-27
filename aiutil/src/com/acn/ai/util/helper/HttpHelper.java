/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture qianchao
 *  @date: Dec 27, 2016
 */
package com.acn.ai.util.helper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acn.ai.util.string.AiStringUtils;


/**
 * @author qianchao
 */
public class HttpHelper
{
	private final static Logger LOG = LoggerFactory.getLogger(HttpHelper.class);

	/**
	 * get the charset from content-type. if not found,return default UTF-8
	 */
	public static String getCharset(final String contentType)
	{
		if (!StringUtils.isEmpty(contentType))
		{
			final String segs[] = AiStringUtils.trim(contentType).split(";");
			for (final String seg : segs)
			{
				if (seg.indexOf("=") > 0)
				{
					final String words[] = seg.split("=");
					for (int i = 0; i < words.length; i++)
					{
						if ("charset".equalsIgnoreCase(words[i]))
						{
							if (i < (words.length - 1))
							{
								return words[i + 1];
							}
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * get the charset from content-type. if not found,return default UTF-8
	 *
	 * @throws UnsupportedEncodingException
	 */
	public static String generateQueryString(final Map<String, String> paramMap, final String enc)
			throws UnsupportedEncodingException
	{
		StringBuffer params = new StringBuffer();
		for (final String key : paramMap.keySet())
		{
			if (StringUtils.isNotBlank(key))
			{
				params.append(key);
				params.append("=");
				if (StringUtils.isNotEmpty(paramMap.get(key)))
				{
					params.append(URLEncoder.encode(paramMap.get(key), enc));
				}
				params.append("&");
			}
		}

		if (params.length() > 0)
		{
			params = params.deleteCharAt(params.length() - 1);
		}
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Rest request params:[{}]", params.toString());
		}
		return params.toString();
	}
}
