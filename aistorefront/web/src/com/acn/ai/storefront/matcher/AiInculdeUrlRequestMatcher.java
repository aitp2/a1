package com.acn.ai.storefront.matcher;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.PathMatcher;

public class AiInculdeUrlRequestMatcher implements RequestMatcher {
	
	private Set<String> includeUrlSet;
	private PathMatcher pathMatcher;
	
	@Override
	public boolean matches(HttpServletRequest request) {
		for (final String excludeUrl : getIncludeUrlSet())
		{
			if (getPathMatcher().match(excludeUrl, request.getServletPath()))
			{
				// Found an exclude pattern
				return true;
			}
		}
		
		return false;
	}

	/**
	 * @return the includeUrlSet
	 */
	public Set<String> getIncludeUrlSet() {
		return includeUrlSet;
	}

	/**
	 * @param includeUrlSet the includeUrlSet to set
	 */
	@Required
	public void setIncludeUrlSet(Set<String> includeUrlSet) {
		this.includeUrlSet = includeUrlSet;
	}

	/**
	 * @return the pathMatcher
	 */
	public PathMatcher getPathMatcher() {
		return pathMatcher;
	}

	/**
	 * @param pathMatcher the pathMatcher to set
	 */
	public void setPathMatcher(PathMatcher pathMatcher) {
		this.pathMatcher = pathMatcher;
	}

}
