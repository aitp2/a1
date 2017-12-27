/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture zhidong.peng
 *  @date: Dec 13, 2016
 */
package com.acn.ai.util.json;

import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.lang.annotation.Annotation;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.Versioned;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.util.VersionUtil;

import com.google.common.base.Preconditions;


/**
 * DimensionFieldSerializer
 *
 */
public class AiJackonAnnotationIntrospector extends JacksonAnnotationIntrospector implements Versioned
{
	private ConfigurationService configurationService;

	@Override
	public Version version()
	{
		return VersionUtil.versionFor(getClass());
	}

	@Override
	public boolean isHandled(final Annotation ann)
	{
		final Class<?> cls = ann.annotationType();
		if (AiJsonProperty.class == cls)
		{
			return true;
		}
		return super.isHandled(ann);
	}

	@Override
	public String findSerializablePropertyName(final AnnotatedField annotatedField)
	{
		return getPropertyName(annotatedField);
	}

	@Override
	public String findDeserializablePropertyName(final AnnotatedField annotatedField)
	{
		return getPropertyName(annotatedField);
	}

	private String getPropertyName(final AnnotatedField annotatedField)
	{
		final AiJsonProperty annotation = annotatedField.getAnnotation(AiJsonProperty.class);
		if (annotation != null)
		{
			final String key = annotation.key();
			Preconditions.checkArgument(StringUtils.isNotBlank(key), "key of @Dimension can not be null or empty");

			return getConfigurationService().getConfiguration().getString(key);
		}

		final JsonProperty jsonProperty = annotatedField.getAnnotation(JsonProperty.class);
		if (jsonProperty != null)
		{
			return jsonProperty.value();
		}

		return annotatedField.getName();
	}

	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService()
	{
		return this.configurationService;
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
