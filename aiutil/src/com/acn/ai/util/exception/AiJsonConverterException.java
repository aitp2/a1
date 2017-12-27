/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture zhidong.peng
 *  @date: Dec 13, 2016
 */
package com.acn.ai.util.exception;

/**
 * this exception will throw by json converter error.
 *
 */
public class AiJsonConverterException extends Exception
{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new Liby json converter exception.
	 */
	public AiJsonConverterException()
	{
	}

	/**
	 * Instantiates a new Liby json converter exception.
	 *
	 * @param paramString
	 *           the param string
	 */
	public AiJsonConverterException(final String paramString)
	{
		super(paramString);
	}

	/**
	 * Instantiates a new Liby json converter exception.
	 *
	 * @param paramString
	 *           the param string
	 * @param paramThrowable
	 *           the param throwable
	 */
	public AiJsonConverterException(final String paramString, final Throwable paramThrowable)
	{
		super(paramString, paramThrowable);
	}

	/**
	 * Instantiates a new Liby json converter exception.
	 *
	 * @param paramThrowable
	 *           the param throwable
	 */
	public AiJsonConverterException(final Throwable paramThrowable)
	{
		super(paramThrowable);
	}

	/**
	 * Instantiates a new Liby json converter exception.
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
	protected AiJsonConverterException(final String paramString, final Throwable paramThrowable, final boolean paramBoolean1,
			final boolean paramBoolean2)
	{
		super(paramString, paramThrowable, paramBoolean1, paramBoolean2);
	}

}
