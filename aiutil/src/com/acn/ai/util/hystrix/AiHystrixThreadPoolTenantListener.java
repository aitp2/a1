/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture zhidong.peng
 *  @date: 13 Dec 2016
 */
package com.acn.ai.util.hystrix;

import de.hybris.platform.core.Registry;
import de.hybris.platform.core.Tenant;
import de.hybris.platform.core.TenantListener;


/**
 * HystrixThreadPoolTenantListener: to close the thread pool when tenant close.
 *
 */
public class AiHystrixThreadPoolTenantListener
{

	/**
	 * Inits the.
	 */
	public void init()
	{
		Registry.registerTenantListener(new TenantListener()
		{

			@Override
			public void afterSetActivateSession(final Tenant arg0)
			{
				// YTODO Auto-generated method stub

			}

			@Override
			public void afterTenantStartUp(final Tenant arg0)
			{
				// YTODO Auto-generated method stub

			}

			@Override
			public void beforeTenantShutDown(final Tenant arg0)
			{
				// YTODO Auto-generated method stub

			}

			@Override
			public void beforeUnsetActivateSession(final Tenant arg0)
			{
				// YTODO Auto-generated method stub

			}

		});
	}
}
