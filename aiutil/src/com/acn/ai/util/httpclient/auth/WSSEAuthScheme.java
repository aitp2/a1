/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture zhidong.peng
 *  @date: 13 Dec 2016
 */
package com.acn.ai.util.httpclient.auth;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.httpclient.auth.InvalidCredentialsException;
import org.apache.commons.httpclient.auth.MalformedChallengeException;
import org.apache.log4j.Logger;



/**
 * The Class WSSEAuthScheme.
 */
public class WSSEAuthScheme implements AuthScheme
{

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(WSSEAuthScheme.class);

	/** The Constant SCHEME_NAME. */
	public static final String SCHEME_NAME = "X-WSSE";

	/** The Constant HEXARRAY. */
	final protected static char[] HEXARRAY = "0123456789abcdef".toCharArray();


	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.httpclient.auth.AuthScheme#getSchemeName()
	 */
	@Override
	public String getSchemeName()
	{
		return SCHEME_NAME;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.httpclient.auth.AuthScheme#isConnectionBased()
	 */
	@Override
	public boolean isConnectionBased()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.httpclient.auth.AuthScheme#isComplete()
	 */
	@Override
	public boolean isComplete()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.httpclient.auth.AuthScheme#authenticate(org.apache.commons.httpclient.Credentials,
	 * java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public String authenticate(final Credentials credentials, final String paramString1, final String paramString2)
			throws AuthenticationException
	{
		LOG.trace("enter WSSEAuthScheme.authenticate(Credentials, String, String)");

		UsernamePasswordCredentials usernamepassword = null;
		try
		{
			usernamepassword = (UsernamePasswordCredentials) credentials;
		}
		catch (final ClassCastException e)
		{
			throw new InvalidCredentialsException(
					"Credentials cannot be used for basic authentication: " + credentials.getClass().getName(), e);

		}

		return authenticate(usernamepassword, "UTF-8");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.httpclient.auth.AuthScheme#authenticate(org.apache.commons.httpclient.Credentials,
	 * org.apache.commons.httpclient.HttpMethod)
	 */
	@Override
	public String authenticate(final Credentials credentials, final HttpMethod method) throws AuthenticationException
	{
		LOG.trace("enter WSSEAuthScheme.authenticate(Credentials, HttpMethod)");

		if (method == null)
		{
			throw new IllegalArgumentException("Method may not be null");
		}
		UsernamePasswordCredentials usernamepassword = null;
		try
		{
			usernamepassword = (UsernamePasswordCredentials) credentials;
		}
		catch (final ClassCastException e)
		{
			throw new InvalidCredentialsException(
					"Credentials cannot be used for basic authentication: " + credentials.getClass().getName(), e);

		}

		return authenticate(usernamepassword, method.getParams().getCredentialCharset());
	}


	/**
	 * Authenticate.
	 *
	 * @param credentials
	 *           the credentials
	 * @param charset
	 *           the charset
	 * @return the string
	 */
	public String authenticate(final UsernamePasswordCredentials credentials, final String charset)
	{
		LOG.trace("enter WSSEAuthScheme.authenticate(UsernamePasswordCredentials, String)");

		if (credentials == null)
		{
			throw new IllegalArgumentException("Credentials may not be null");
		}
		if ((charset == null) || (charset.length() == 0))
		{
			throw new IllegalArgumentException("charset may not be null or empty");
		}

		final String timestamp = getUTCTimestamp();
		final String nonce = getNonce();
		final String digest = getDigestMessage(credentials, nonce, timestamp);

		return String.format("UsernameToken Username=\"%s\", PasswordDigest=\"%s\", Nonce=\"%s\", Created=\"%s\"",
				credentials.getUserName(), digest, nonce, timestamp);
	}

	/**
	 * get UTC time.
	 *
	 * @author mingming.wang
	 * @return the UTC timestamp
	 */
	private String getUTCTimestamp()
	{
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		return sdf.format(new Date());
	}

	/**
	 * create random 16 byte string.
	 *
	 * @author mingming.wang
	 * @return the nonce
	 */
	private String getNonce()
	{
		final byte[] nonceBytes = new byte[16];
		new SecureRandom().nextBytes(nonceBytes);

		return bytesToHex(nonceBytes);
	}

	/**
	 * * generate digestvMessage by Credential, nonce and timestamp.
	 *
	 * @author mingming.wang
	 * @param credentials
	 *           the credentials
	 * @param nonce
	 *           the nonce
	 * @param timestamp
	 *           the timestamp
	 * @return the digest message
	 */
	private String getDigestMessage(final UsernamePasswordCredentials credentials, final String nonce, final String timestamp)
	{
		String digest = "";
		try
		{
			final MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
			messageDigest.reset();
			final String hashedString = String.format("%s%s%s", nonce, timestamp, credentials.getPassword());
			messageDigest.update(hashedString.getBytes("UTF-8"));
			final String sha1Sum = bytesToHex(messageDigest.digest());

			digest = DatatypeConverter.printBase64Binary(sha1Sum.getBytes("UTF-8"));
		}
		catch (final NoSuchAlgorithmException ex)
		{
			LOG.error("No SHA-1 algorithm was found!", ex);
		}
		catch (final UnsupportedEncodingException ex)
		{
			LOG.error("Cannot use UTF-8 encoding.", ex);
		}

		return digest;
	}

	/**
	 * Bytes to hex.
	 *
	 * @param bytes
	 *           the bytes
	 * @return the string
	 */
	private String bytesToHex(final byte[] bytes)
	{
		final char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++)
		{
			final int vec = bytes[j] & 0xFF;
			hexChars[j * 2] = HEXARRAY[vec >>> 4];
			hexChars[j * 2 + 1] = HEXARRAY[vec & 0x0F];
		}
		return new String(hexChars);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.httpclient.auth.AuthScheme#getID()
	 */
	@SuppressWarnings("deprecation")
	@Override
	public String getID()
	{
		return SCHEME_NAME;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.httpclient.auth.AuthScheme#getParameter(java.lang.String)
	 */
	@Override
	public String getParameter(final String paramString)
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.httpclient.auth.AuthScheme#getRealm()
	 */
	@Override
	public String getRealm()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.httpclient.auth.AuthScheme#processChallenge(java.lang.String)
	 */
	@Override
	public void processChallenge(final String paramString) throws MalformedChallengeException
	{
		// YTODO Auto-generated method stub

	}

}
