package com.acn.ai.storefront.filters;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.accenture.performance.optimization.facades.OptimizedCartFacade;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.acn.ai.core.oauth.AiTokenService;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;

public class OccTokenFilter extends OncePerRequestFilter {
	private AiTokenService tokenService;
	
	@Resource(name = "userService")
	private UserService userService;
	
	@Resource(name = "cartFacade")
	private OptimizedCartFacade cartFacade;
	
	@Resource(name = "baseSiteService")
	private BaseSiteService baseSiteService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException 
	{
		final String token = tokenService.getAccessToken();
		
		final UserModel userModel = userService.getCurrentUser();
		final OptimizedCartData cart = cartFacade.getSessionCartData();
		final String siteID = baseSiteService.getCurrentBaseSite().getUid();
		final String cartID = userService.isAnonymousUser(userModel) ? cart.getGuid() : cart.getCode();
		
		Cookie[] cookies = request.getCookies();
		Cookie occToken = null;
		Cookie siteIDCookie = null;
		Cookie cartIDCookie = null;
		
		if(cookies != null)
		{
			for(Cookie item:cookies)
			{
				switch (item.getName()) 
				{
				case "ai-occ-token":
					occToken = item;
					break;
				case "siteId":
					siteIDCookie = item;
					break;
				case "cartUid":
					cartIDCookie = item;
					break;
				default:
				}
				
			}
		}
		
		final int sessionMaxInactiveInterval = request.getSession().getMaxInactiveInterval();
		
		if(occToken == null)
		{
			occToken = new Cookie("ai-occ-token",token);
			occToken.setPath("/");
			occToken.setMaxAge(sessionMaxInactiveInterval);
			response.addCookie(occToken);
		}
		
		if(siteIDCookie == null)
		{
			siteIDCookie = new Cookie("siteId",siteID);
			siteIDCookie.setPath("/");
			occToken.setMaxAge(sessionMaxInactiveInterval);
			response.addCookie(siteIDCookie);
			
		}
		
		if(cartIDCookie == null)
		{
			cartIDCookie = new Cookie("cartUid",cartID);
			cartIDCookie.setPath("/");
			occToken.setMaxAge(sessionMaxInactiveInterval);
			response.addCookie(cartIDCookie);
		}
		
		
		
		
		filterChain.doFilter(request, response);
	}

	/**
	 * @return the tokenService
	 */
	public AiTokenService getTokenService() {
		return tokenService;
	}

	/**
	 * @param tokenService the tokenService to set
	 */
	public void setTokenService(AiTokenService tokenService) {
		this.tokenService = tokenService;
	}

	/**
	 * @return the userService
	 */
	public UserService getUserService() {
		return userService;
	}

	/**
	 * @param userService the userService to set
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	/**
	 * @return the cartFacade
	 */
	public OptimizedCartFacade getCartFacade() {
		return cartFacade;
	}

	/**
	 * @param cartFacade the cartFacade to set
	 */
	public void setCartFacade(OptimizedCartFacade cartFacade) {
		this.cartFacade = cartFacade;
	}

	/**
	 * @return the baseSiteService
	 */
	public BaseSiteService getBaseSiteService() {
		return baseSiteService;
	}

	/**
	 * @param baseSiteService the baseSiteService to set
	 */
	public void setBaseSiteService(BaseSiteService baseSiteService) {
		this.baseSiteService = baseSiteService;
	}

}
