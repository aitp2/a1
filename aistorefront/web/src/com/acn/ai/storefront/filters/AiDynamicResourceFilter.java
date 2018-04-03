package com.acn.ai.storefront.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import com.acn.ai.storefront.security.ExcludeUrlRequestMatcher;

import de.hybris.platform.servicelayer.config.ConfigurationService;

public class AiDynamicResourceFilter extends GenericFilterBean
{
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
			response.addHeader("Cache-Control", configurationService.getConfiguration().getString(CACAHECONTROL, "no-cache,must-revalidate"));
		}
		
		chain.doFilter(request, response);
		
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
