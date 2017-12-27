/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture zhidong.peng
 *  @date: 13 Dec 2016
 */
package com.acn.ai.util.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;


/**
 * super class of Hystrix Command for configuration.
 *
 * @param <T>
 *           excuted result object
 */
public abstract class AbstractHystrixWSCommand<T> extends HystrixCommand<T>
{

	/**
	 * Set the configuration for Hystrix Command.
	 *
	 * @param config
	 *           configuration
	 */
	public AbstractHystrixWSCommand(final AiHystrixConfig config)
	{

		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(config.getGroupKey()))
				.andCommandPropertiesDefaults(
						HystrixCommandProperties.Setter().withExecutionIsolationThreadTimeoutInMilliseconds(config.getThreadTimeout()))
				.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(config.getThreadPoolKey()))
				.andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(config.getThreadPoolCoreSize())));

	}

}
