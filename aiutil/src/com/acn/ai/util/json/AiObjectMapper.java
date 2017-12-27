/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture zhidong.peng
 *  @date: Dec 13, 2016
 */
package com.acn.ai.util.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;


/**
 * The Class LibyObjectMapper.
 *
 */
public class AiObjectMapper extends ObjectMapper
{

	/**
	 * Instantiates a new Liby object mapper.
	 */
	public AiObjectMapper()
	{
		super.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>()
		{
			@Override
			public void serialize(final Object arg0, final JsonGenerator arg1, final SerializerProvider arg2)
					throws IOException, JsonProcessingException
			{
				arg1.writeString("");
			}

		});
	}
}

