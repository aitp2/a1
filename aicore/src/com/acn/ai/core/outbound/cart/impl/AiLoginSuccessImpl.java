package com.acn.ai.core.outbound.cart.impl;

import com.acn.ai.core.oauth.AiTokenService;
import com.acn.ai.core.outbound.cart.AiLoginSuccess;
import com.acn.ai.util.client.AiSSLClient;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiLoginSuccessImpl implements AiLoginSuccess
{
	private final static Logger LOG = LoggerFactory.getLogger(AiLoginSuccessImpl.class);
	
	private AiTokenService tokenService;
	private UserService userService;
	private BaseStoreService baseStoreService;
	private ConfigurationService configurationService;
	
	@Override
	public void restoreCartAndMerge(String mostRecentSavedCartGuid, String sessionCartGuid)
	{
		final UserModel currentUser = userService.getCurrentUser();
		BaseStoreModel baseStore = baseStoreService.getCurrentBaseStore();
		final String token = tokenService.getAccessToken();
		final String occ = configurationService.getConfiguration().getString("ai.occ.uri")+"/"+baseStore.getUid()+"/users/"+currentUser.getUid()+"/carts?oldCartId="+mostRecentSavedCartGuid+"&toMergeCartGuid="+sessionCartGuid;
		new AiSSLClient().doPost(occ, "", "utf-8",token);
	}

	public AiTokenService getTokenService() {
		return tokenService;
	}

	public void setTokenService(AiTokenService tokenService) {
		this.tokenService = tokenService;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public BaseStoreService getBaseStoreService() {
		return baseStoreService;
	}

	public void setBaseStoreService(BaseStoreService baseStoreService) {
		this.baseStoreService = baseStoreService;
	}
	
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

}
