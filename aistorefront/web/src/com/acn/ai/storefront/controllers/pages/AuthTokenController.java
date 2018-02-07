/**
 *
 */
package com.acn.ai.storefront.controllers.pages;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.accenture.performance.optimization.facades.OptimizedCartFacade;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.acn.ai.core.oauth.AiTokenService;
import com.acn.ai.storefront.security.cookie.CartRestoreCookieGenerator;

import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractPageController;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;


/**
 * @author mingming.wang
 *
 */
@Controller
@RequestMapping("/auth")
public class AuthTokenController extends AbstractPageController
{
	private static final Logger LOG = Logger.getLogger(AuthTokenController.class);
	@Resource(name = "aiTokenService")
	private AiTokenService tokenService;
	@Resource(name = "sessionService")
	private SessionService sessionService;
	@Resource(name = "userService")
	private UserService userService;
	@Resource(name = "cartFacade")
	private OptimizedCartFacade cartFacade;
	@Resource(name = "baseSiteService")
	private BaseSiteService baseSiteService;
	
	@Resource(name = "cartRestoreCookieGenerator")
	private CartRestoreCookieGenerator cartRestoreCookieGenerator;

	@RequestMapping(value = "/getToken", method = RequestMethod.GET)
	public @ResponseBody Map<String, String> getAccessToken(HttpServletRequest request, HttpServletResponse response) throws CMSItemNotFoundException
	{
		final Map<String, String> result = new HashMap();
		final UserModel userModel = userService.getCurrentUser();
		LOG.info("get token for:" + userModel.getUid());
		final String token = tokenService.getAccessToken();
		final OptimizedCartData cart = cartFacade.getSessionCartData();
		result.put("token", token);
		
		final String siteID = baseSiteService.getCurrentBaseSite().getUid();
		result.put("siteId", siteID);
		
		final String cartID =  userService.isAnonymousUser(userModel) ? cart.getGuid() : cart.getCode();
		result.put("cartUid",cartID);
		
		final int sessionMaxInactiveInterval = request.getSession().getMaxInactiveInterval();
		
		Cookie occToken = new Cookie("ai-occ-token", token);
		occToken.setPath("/");
		occToken.setMaxAge(sessionMaxInactiveInterval);
		response.addCookie(occToken);
		
		Cookie cartIDCookie = new Cookie("cartUid", cartID);
		cartIDCookie.setPath("/");
		cartIDCookie.setMaxAge(sessionMaxInactiveInterval);
		response.addCookie(cartIDCookie);
		
		Cookie siteIDCookie = new Cookie("siteId", siteID);
		siteIDCookie.setPath("/");
		siteIDCookie.setMaxAge(sessionMaxInactiveInterval);
		response.addCookie(siteIDCookie);
		
		if(userService.isAnonymousUser(userModel))
		{
			//added by wei.f.zhang
			final String restarationCartCookieName = StringUtils.deleteWhitespace(baseSiteService.getCurrentBaseSite().getUid()) + "-cart";
			Cookie restarationCartCookie = new Cookie(restarationCartCookieName,"");
			restarationCartCookie.setPath("/");
			response.addCookie(restarationCartCookie);
		}
		
		return result;
	}
}
