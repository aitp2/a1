/**
 *
 */
package com.acn.ai.storefront.controllers.pages;

import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractPageController;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.accenture.performance.optimization.facades.OptimizedCartFacade;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.acn.ai.core.oauth.AiTokenService;


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

	@RequestMapping(value = "/getToken", method = RequestMethod.GET)
	public @ResponseBody Map<String, String> getAccessToken() throws CMSItemNotFoundException
	{
		final Map<String, String> result = new HashMap();
		final UserModel userModel = userService.getCurrentUser();
		LOG.info("get token for:" + userModel.getUid());
		final String token = tokenService.getAccessToken();
		final OptimizedCartData cart = cartFacade.getSessionCartData();
		result.put("token", token);
		result.put("siteId", baseSiteService.getCurrentBaseSite().getUid());
		result.put("cartUid", userService.isAnonymousUser(userModel) ? cart.getGuid() : cart.getCode());

		return result;
	}
}
