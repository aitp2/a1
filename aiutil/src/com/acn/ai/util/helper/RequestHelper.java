/**
 *  Copyright (c) 2016 liby Group Ltd.,
 *  All rights reserved.
 *  @author: accenture le.qi
 *  @date: Feb 20, 2017
 */
package com.acn.ai.util.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class RequestHelper
{
	private final static Logger LOG = LoggerFactory.getLogger(RequestHelper.class);

	/**
	 * 鑾峰彇鐢ㄦ埛鐪熷疄IP鍦板潃锛屼笉浣跨敤request.getRemoteAddr();鐨勫師鍥犳槸鏈夊彲鑳界敤鎴蜂娇鐢ㄤ簡浠ｇ悊杞欢鏂瑰紡閬垮厤鐪熷疄IP鍦板潃,
	 * 鍙槸锛屽鏋滈�氳繃浜嗗绾у弽鍚戜唬鐞嗙殑璇濓紝X-Forwarded-For鐨勫�煎苟涓嶆涓�涓紝鑰屾槸涓�涓睮P鍊硷紝绌剁珶鍝釜鎵嶆槸鐪熸鐨勭敤鎴风鐨勭湡瀹濱P鍛紵
	 * 绛旀鏄彇X-Forwarded-For涓涓�涓潪unknown鐨勬湁鏁圛P瀛楃涓层�� 濡傦細X-Forwarded-For锛�192.168.1.110, 192.168.1.120,
	 * 192.168.1.130,192.168.1.100 鐢ㄦ埛鐪熷疄IP涓猴細192.168.1.110聽聽
	 *
	 * @param request
	 *           聽聽聽
	 * @return 聽聽聽
	 */
	public static String getIpAddress(final HttpServletRequest request)
	{
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
		{
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
		{
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
		{
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
		{
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
		{
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	// \b 鏄崟璇嶈竟鐣�(杩炵潃鐨勪袱涓�(瀛楁瘝瀛楃 涓� 闈炲瓧姣嶅瓧绗�) 涔嬮棿鐨勯�昏緫涓婄殑闂撮殧),
	// 瀛楃涓插湪缂栬瘧鏃朵細琚浆鐮佷竴娆�,鎵�浠ユ槸 "\\b"
	// \B 鏄崟璇嶅唴閮ㄩ�昏緫闂撮殧(杩炵潃鐨勪袱涓瓧姣嶅瓧绗︿箣闂寸殑閫昏緫涓婄殑闂撮殧)
	static String phoneReg = "\\b(ip(hone|od)|android|opera m(ob|in)i" + "|windows (phone|ce)|blackberry"
			+ "|s(ymbian|eries60|amsung)|p(laybook|alm|rofile/midp" + "|laystation portable)|nokia|fennec|htc[-_]"
			+ "|mobile|up.browser|[1-4][0-9]{2}x[1-4][0-9]{2})\\b";
	static String tableReg = "\\b(ipad|tablet|(Nexus 7)|up.browser" + "|[1-4][0-9]{2}x[1-4][0-9]{2})\\b";

	//绉诲姩璁惧姝ｅ垯鍖归厤锛氭墜鏈虹銆佸钩鏉�
	static Pattern phonePat = Pattern.compile(phoneReg, Pattern.CASE_INSENSITIVE);
	static Pattern tablePat = Pattern.compile(tableReg, Pattern.CASE_INSENSITIVE);

	/**
	 * 妫�娴嬫槸鍚︽槸绉诲姩璁惧璁块棶
	 *
	 * @Title: check
	 * @Date : 2014-7-7 涓嬪崍01:29:07
	 * @param userAgent
	 *           娴忚鍣ㄦ爣璇�
	 * @return true:绉诲姩璁惧鎺ュ叆锛宖alse:pc绔帴鍏�
	 */
	public static boolean isMobileDevice(final HttpServletRequest request)
	{

		//妫�鏌ユ槸鍚﹀凡缁忚褰曡闂柟寮忥紙绉诲姩绔垨pc绔級
		try
		{
			//鑾峰彇ua锛岀敤鏉ュ垽鏂槸鍚︿负绉诲姩绔闂�
			String userAgent = request.getHeader("USER-AGENT").toLowerCase();
			if (null == userAgent)
			{
				userAgent = "";
			}
			// 鍖归厤
			final Matcher matcherPhone = phonePat.matcher(userAgent);
			final Matcher matcherTable = tablePat.matcher(userAgent);
			if (matcherPhone.find() || matcherTable.find())
			{
				// System.out.println("绉诲姩绔闂�");
				return true;
			}
		}
		catch (final Exception e)
		{
			LOG.error("RequestHelper isMobileDevice error :" + e.getMessage());
		}
		// System.out.println("pc绔闂�");
		return false;
	}


	/**
	 * 鑾峰彇鎿嶄綔绯荤粺淇℃伅
	 *
	 * @param request
	 * @return
	 */
	public static String getOsInfo(final HttpServletRequest request)
	{
		final String browserDetails = request.getHeader("User-Agent");
		final String userAgent = browserDetails;

		String os = "";

		//=================OS Info=======================
		if (userAgent.toLowerCase().indexOf("windows") >= 0)
		{
			os = "Windows";
		}
		else if (userAgent.toLowerCase().indexOf("mac") >= 0)
		{
			os = "Mac";
		}
		else if (userAgent.toLowerCase().indexOf("x11") >= 0)
		{
			os = "Unix";
		}
		else if (userAgent.toLowerCase().indexOf("android") >= 0)
		{
			os = "Android";
		}
		else if (userAgent.toLowerCase().indexOf("iphone") >= 0)
		{
			os = "IPhone";
		}
		else
		{
			os = "UnKnown, More-Info: " + userAgent;
		}

		return os;
	}

	/**
	 * 鑾峰彇娴忚鍣ㄥ強娴忚鍣ㄧ増鏈俊鎭�
	 *
	 * @param request
	 * @return
	 */
	public static String getBrowserInfo(final HttpServletRequest request)
	{
		final String browserDetails = request.getHeader("User-Agent");
		final String userAgent = browserDetails;
		final String user = userAgent.toLowerCase();

		String browser = "";

		//===============Browser===========================
		if (user.contains("edge"))
		{
			browser = (userAgent.substring(userAgent.indexOf("Edge")).split(" ")[0]).replace("/", "-");
		}
		else if (user.contains("msie"))
		{
			final String substring = userAgent.substring(userAgent.indexOf("MSIE")).split(";")[0];
			browser = substring.split(" ")[0].replace("MSIE", "IE") + "-" + substring.split(" ")[1];
		}
		else if (user.contains("safari") && user.contains("version"))
		{
			browser = (userAgent.substring(userAgent.indexOf("Safari")).split(" ")[0]).split("/")[0] + "-"
					+ (userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
		}
		else if (user.contains("opr") || user.contains("opera"))
		{
			if (user.contains("opera"))
			{
				browser = (userAgent.substring(userAgent.indexOf("Opera")).split(" ")[0]).split("/")[0] + "-"
						+ (userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
			}
			else if (user.contains("opr"))
			{
				browser = ((userAgent.substring(userAgent.indexOf("OPR")).split(" ")[0]).replace("/", "-")).replace("OPR", "Opera");
			}

		}
		else if (user.contains("chrome"))
		{
			browser = (userAgent.substring(userAgent.indexOf("Chrome")).split(" ")[0]).replace("/", "-");
		}
		else if ((user.indexOf("mozilla/7.0") > -1) || (user.indexOf("netscape6") != -1) || (user.indexOf("mozilla/4.7") != -1)
				|| (user.indexOf("mozilla/4.78") != -1) || (user.indexOf("mozilla/4.08") != -1) || (user.indexOf("mozilla/3") != -1))
		{
			browser = "Netscape-?";

		}
		else if (user.contains("firefox"))
		{
			browser = (userAgent.substring(userAgent.indexOf("Firefox")).split(" ")[0]).replace("/", "-");
		}
		else if (user.contains("rv"))
		{
			final String IEVersion = (userAgent.substring(userAgent.indexOf("rv")).split(" ")[0]).replace("rv:", "-");
			browser = "IE" + IEVersion.substring(0, IEVersion.length() - 1);
		}
		else
		{
			browser = "UnKnown, More-Info: " + userAgent;
		}

		return browser;
	}
}
