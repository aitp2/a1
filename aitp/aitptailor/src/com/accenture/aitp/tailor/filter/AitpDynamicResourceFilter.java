package com.accenture.aitp.tailor.filter;

import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;


//import com.acn.ai.storefront.security.ExcludeUrlRequestMatcher;

public class AitpDynamicResourceFilter extends GenericFilterBean
{
	private final static Logger LOG = Logger.getLogger(AitpDynamicResourceFilter.class);
	private final static String CACAHECONTROL = "storefront.dynamicResourceFilter.response.header.Cache-Control";

	private RequestMatcher requestMatcher;
	private ConfigurationService configurationService;

	@Override
	public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
			throws IOException, ServletException {

		final HttpServletRequest request = (HttpServletRequest) req;
		final HttpServletResponse response = (HttpServletResponse) res;

		final boolean match = requestMatcher.matches(request);
		if (match)
		{
			response.addHeader("Cache-Control", configurationService.getConfiguration().getString(CACAHECONTROL, "private,must-revalidate"));
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
	public void setRequestMatcher(final RequestMatcher requestMatcher) {
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
	public void setConfigurationService(final ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}



}