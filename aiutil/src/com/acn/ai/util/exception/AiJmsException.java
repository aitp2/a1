/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture zhidong.peng
 *  @date: Dec 13, 2016
 */
package com.acn.ai.util.exception;

/**
 *
 */
public class AiJmsException extends Exception
{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new liby jms exception.
	 */
	public AiJmsException()
	{
	}

	/**
	 * Instantiates a new liby jms exception.
	 *
	 * @param paramString
	 *           the param string
	 */
	public AiJmsException(final String paramString)
	{
		super(paramString);
	}

	/**
	 * Instantiates a new liby jms exception.
	 *
	 * @param paramString
	 *           the param string
	 * @param paramThrowable
	 *           the param throwable
	 */
	public AiJmsException(final String paramString, final Throwable paramThrowable)
	{
		super(paramString, paramThrowable);
	}

	/**
	 * Instantiates a new liby jms exception.
	 *
	 * @param paramThrowable
	 *           the param throwable
	 */
	public AiJmsException(final Throwable paramThrowable)
	{
		super(paramThrowable);
	}

	/**
	 * Instantiates a new liby jms exception.
	 *
	 * @param paramString
	 *           the param string
	 * @param paramThrowable
	 *           the param throwable
	 * @param paramBoolean1
	 *           the param boolean 1
	 * @param paramBoolean2
	 *           the param boolean 2
	 */
	protected AiJmsException(final String paramString, final Throwable paramThrowable, final boolean paramBoolean1,
			final boolean paramBoolean2)
	{
		super(paramString, paramThrowable, paramBoolean1, paramBoolean2);
	}
}
