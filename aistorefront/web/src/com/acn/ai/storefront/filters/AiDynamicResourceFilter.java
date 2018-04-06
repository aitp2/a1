package com.acn.ai.storefront.filters;

import java.io.IOException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import com.acn.ai.storefront.security.ExcludeUrlRequestMatcher;

import de.hybris.platform.servicelayer.config.ConfigurationService;

public class AiDynamicResourceFilter extends GenericFilterBean
{
	private final static Logger LOG = Logger.getLogger(AiDynamicResourceFilter.class);
	private final static String CACAHECONTROL = "storefront.dynamicResourceFilter.response.header.Cache-Control";
	
	private RequestMatcher requestMatcher;
	private ConfigurationService configurationService;
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		
		final HttpServletRequest request = (HttpServletRequest) req;
		final HttpServletResponse response = (HttpServletResponse) res;
		
		boolean match = (requestMatcher instanceof ExcludeUrlRequestMatcher) ? !requestMatcher.matches(request):requestMatcher.matches(request);
		if(match)
		{
			response.addHeader("Cache-Control", configurationService.getConfiguration().getString(CACAHECONTROL, "private,must-revalidate"));
			//response.setBufferSize(1024 * 1024);
			response.setContentLength(1024*1024);
			
//			if(!isModified(request))
//			{
//				response.setStatus(304);
//			}
//			else
			{
				long flag = (new Date()).getTime();
				response.setDateHeader("Last-Modified", 0);
				response.setHeader("ETag", flag+"");
				LOG.info("set Last-Modified for RequestURL-"+request.getRequestURL()+":"+0);
				LOG.info("set ETag for RequestURL-"+request.getRequestURL()+":"+flag);
			}
		}
		
		chain.doFilter(request, response);
		
		//////
		
	}
	
	protected boolean isModified(HttpServletRequest request)
	{
		long lastModified = request.getDateHeader("If-Modified-Since");
		String etg = request.getHeader("If-None-Match");
		
		LOG.info("get lastModified for RequestURL-"+request.getRequestURL()+":"+lastModified);
		LOG.info("get ETag for RequestURL-"+request.getRequestURL()+":"+etg);
		if(lastModified < 0  && StringUtils.isEmpty(etg))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * @return the requestMatcher
	 */
	public RequestMatcher getRequestMatcher() {
		return requestMatcher;
	}

	/**
	 * @param requestMatcher the requestMatcher to set
	 */
	public void setRequestMatcher(RequestMatcher requestMatcher) {
		this.requestMatcher = requestMatcher;
	}

	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	/**
	 * @param configurationService the configurationService to set
	 */
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	

}
