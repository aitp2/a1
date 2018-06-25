/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2018 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package com.accenture.aitp.cart.factory.impl;

import de.hybris.platform.servicelayer.web.session.HybrisSpringSessionRepositoryFactory;
import de.hybris.platform.util.Config;

import org.springframework.core.serializer.Deserializer;
import org.springframework.session.SessionRepository;


/**
 *
 */
public class DefaultAitpSpringSessionRepositoryFactory implements HybrisSpringSessionRepositoryFactory
{
	private SessionRepository sessionRepository;

	private HybrisSpringSessionRepositoryFactory hybrisSessionRepositoryFactory;

	private static final String SAVED_SESSION_REDIS = "redisSession";

	@Override
	public SessionRepository createRepository(final Deserializer deSerializer, final String extension, final String contextRoot)
	{
		final String redisSession = Config.getParameter("spring.session." + extension + ".save");
		if (SAVED_SESSION_REDIS.equals(redisSession))
		{
			return getSessionRepository();
		}
		return hybrisSessionRepositoryFactory.createRepository(deSerializer, extension, contextRoot);
	}

	/**
	 * @return the sessionRepository
	 */
	public SessionRepository getSessionRepository()
	{
		return sessionRepository;
	}

	/**
	 * @param sessionRepository
	 *           the sessionRepository to set
	 */
	public void setSessionRepository(final SessionRepository sessionRepository)
	{
		this.sessionRepository = sessionRepository;
	}

	/**
	 * @param hybrisSessionRepositoryFactory
	 *           the hybrisSessionRepositoryFactory to set
	 */
	public void setHybrisSessionRepositoryFactory(final HybrisSpringSessionRepositoryFactory hybrisSessionRepositoryFactory)
	{
		this.hybrisSessionRepositoryFactory = hybrisSessionRepositoryFactory;
	}


}
