/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture zhidong.peng
 *  @date: 13 Dec 2016
 */
package com.acn.ai.util.exception;

/**
 * TaskRunnerException to avoid retry.
 */
public class AiTaskRunnerException extends Exception
{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new liby task runner exception.
	 */
	public AiTaskRunnerException()
	{
	}

	/**
	 * Instantiates a new liby task runner exception.
	 *
	 * @param paramString
	 *           the param string
	 */
	public AiTaskRunnerException(final String paramString)
	{
		super(paramString);
	}

	/**
	 * Instantiates a new liby task runner exception.
	 *
	 * @param var1
	 *           the var 1
	 */
	public AiTaskRunnerException(final Throwable var1)
	{
		super(var1);
	}

}
