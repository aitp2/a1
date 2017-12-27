/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture yong.c.sun
 *  @date: Dec 22, 2016
 */
package com.acn.ai.util.string;

import java.util.Calendar;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;


/**
 *
 */
public class AiStringUtils
{
	public final static String PASSWORD_CHAR_ALL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890\\~`!@#$%^&*()-_=+:;<>?,./{}|'\"/[/]+";
	public final static String NUM_CHAR_ALL = "1234567890";
	public final static String LETTER_CHAR_ALL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public final static String SPECIAL_CHAR_ALL = "\\~`!@#$%^&*()-_=+:;<>?,./{}|'\"/[/]+";

	public static String getRandomString(final int length, final String patter)
	{
		final Random random = new Random();
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; ++i)
		{
			final int number = random.nextInt(patter.length());

			sb.append(patter.charAt(number));
		}
		return sb.toString();
	}

	public static String getRandomPassword(final int length)
	{
		String result = StringUtils.EMPTY;
		while (true)
		{
			result = getRandomString(length, PASSWORD_CHAR_ALL);
			if (matchTypeCount(result) >= 2)
			{
				return result;
			}
		}
	}

	//	public static String getRandomPasswordByRole(final PasswordRole passwordRole)
	//	{
	//		final String numpart = getRandomString(3, NUM_CHAR_ALL);
	//		final String letterpart = getRandomString(3, LETTER_CHAR_ALL);
	//		final String specialpart = getRandomString(3, SPECIAL_CHAR_ALL);
	//		String result = StringUtils.EMPTY;
	//		if (PasswordRole.BOSS.equals(passwordRole))
	//		{
	//			result = numpart + letterpart + specialpart;
	//		}
	//		else if (PasswordRole.FINANCE.equals(passwordRole))
	//		{
	//			result = numpart + letterpart;
	//		}
	//		else if (PasswordRole.MANAGER.equals(passwordRole))
	//		{
	//			result = letterpart + specialpart;
	//		}
	//		else if (PasswordRole.CLERK.equals(passwordRole))
	//		{
	//			result = numpart + specialpart;
	//		}
	//		return result;
	//	}

	public static int matchTypeCount(final String result)
	{
		int account = 0;
		if (result.matches(".*[a-z]{1,}.*"))
		{
			account++;
		}
		if (result.matches(".*[A-Z]{1,}.*"))
		{
			account++;
		}
		if (result.matches(".*\\d{1,}.*"))
		{
			account++;
		}
		if (result.matches(".*[\\~`!@#$%^&*()-_=+:;<>?,./{}|'\"/[/]+]{1,}.*"))
		{
			account++;
		}
		return account;
	}

	public static String trim(final String str)
	{
		final StringBuffer st = new StringBuffer(str.length());
		for (int i = 0; i < str.length(); i++)
		{
			if (str.charAt(i) != ' ')
			{
				st.append(str.charAt(i));
			}
		}
		return st.toString();
	}

	public static boolean isNumeric(final String str)
	{
		final Pattern pattern = Pattern.compile("-?[0-9]*.?[0-9]*");
		return pattern.matcher(str).matches();
	}

	public synchronized static String getRandomSerialNum()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(Calendar.getInstance().getTimeInMillis());
		for (int i = 0; i < 4; i++)
		{
			final char ch = NUM_CHAR_ALL.charAt(new Random().nextInt(10));
			sb.append(ch);
		}
		return sb.toString();
	}
}
