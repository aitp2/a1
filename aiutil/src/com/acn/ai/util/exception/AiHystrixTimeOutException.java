/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture zhidong.peng
 *  @date: 13 Dec 2016
 */
package com.acn.ai.util.exception;

/**
 * hystrix exception
 */
public class AiHystrixTimeOutException extends RuntimeException
{

	/**
	 * Instantiates a new liby hystrix time out exception.
	 */
	public AiHystrixTimeOutException()
	{
	}

	/**
	 * Instantiates a new liby hystrix time out exception.
	 *
	 * @param var1
	 *           the var 1
	 */
	public AiHystrixTimeOutException(final String var1)
	{
		super(var1);
	}

	/**
	 * Instantiates a new liby hystrix time out exception.
	 *
	 * @param var1
	 *           the var 1
	 * @param var2
	 *           the var 2
	 */
	public AiHystrixTimeOutException(final String var1, final Throwable var2)
	{
		super(var1, var2);
	}

	/**
	 * Instantiates a new liby hystrix time out exception.
	 *
	 * @param var1
	 *           the var 1
	 */
	public AiHystrixTimeOutException(final Throwable var1)
	{
		super(var1);
	}
}
