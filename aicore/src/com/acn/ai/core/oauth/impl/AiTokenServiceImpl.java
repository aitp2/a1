/**
 *
 */
package com.acn.ai.core.oauth.impl;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import com.acn.ai.core.oauth.AiTokenService;


/**
 * @author mingming.wang
 *
 */
public class AiTokenServiceImpl implements AiTokenService
{

	private static final Logger LOG = Logger.getLogger(AiTokenServiceImpl.class);
	private ConfigurationService configurationService;
	private AuthorizationServerTokenServices tokenServices;
	private ClientDetailsService clientDetailsService;
	private UserService userService;
	private UserDetailsService userDetailsService;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.acn.ai.core.oauth.AiTokenService#getAuthToken()
	 */
	@Override
	public String getAccessToken()
	{
		final UserModel currentUser = userService.getCurrentUser();
		final String clientId = configurationService.getConfiguration().getString("token.client.id");
		final Map<String, String> parameters = new HashMap();
		parameters.put("client_id", clientId);
		parameters.put("client_secret", configurationService.getConfiguration().getString("token.client.password"));
		if (userService.isAnonymousUser(currentUser))
		{
			parameters.put("grant_type", "client_credentials");
		}
		else
		{
			parameters.put("grant_type", "password");
			parameters.put("username", currentUser.getUid());
		}


		try
		{
			final OAuth2RequestFactory oAuth2RequestFactory = new DefaultOAuth2RequestFactory(clientDetailsService);
			final ClientDetails authenticatedClient = clientDetailsService.loadClientByClientId(clientId);
			final TokenRequest tokenRequest = oAuth2RequestFactory.createTokenRequest(parameters, authenticatedClient);
			final OAuth2Request storedOAuth2Request = oAuth2RequestFactory.createOAuth2Request(authenticatedClient, tokenRequest);
			final UserDetails userDetails = this.retrieveUser(currentUser.getUid());
			final Authentication userAuth = new UsernamePasswordAuthenticationToken(currentUser.getUid(), null,
					userDetails.getAuthorities());
			((AbstractAuthenticationToken) userAuth).setDetails(parameters);
			final OAuth2Authentication auth2Authentication = new OAuth2Authentication(storedOAuth2Request, userAuth);
			final OAuth2AccessToken token = tokenServices.createAccessToken(auth2Authentication);


			return token.getValue();

		}
		catch (final ClientRegistrationException | UsernameNotFoundException | AuthenticationServiceException arg5)
		{
			LOG.error("Bad credentials: " + currentUser.getUid(), arg5);
		}
		LOG.warn("User logon failure.");
		return "";
	}



	protected final UserDetails retrieveUser(final String username) throws AuthenticationException
	{
		UserDetails loadedUser;
		try
		{
			loadedUser = this.userDetailsService.loadUserByUsername(username);

		}
		catch (final DataAccessException arg3)
		{

			throw new AuthenticationServiceException(arg3.getMessage(), arg3);
		}

		if (loadedUser == null)
		{

			throw new AuthenticationServiceException("UserDetailsService returned null, which is an interface contract violation");
		}
		else
		{
			return loadedUser;
		}

	}



	/**
	 * @param configurationService
	 *           the configurationService to set
	 */
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}



	/**
	 * @param tokenServices
	 *           the tokenServices to set
	 */
	public void setTokenServices(final AuthorizationServerTokenServices tokenServices)
	{
		this.tokenServices = tokenServices;
	}



	/**
	 * @param clientDetailsService
	 *           the clientDetailsService to set
	 */
	public void setClientDetailsService(final ClientDetailsService clientDetailsService)
	{
		this.clientDetailsService = clientDetailsService;
	}



	/**
	 * @param userService
	 *           the userService to set
	 */
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}



	/**
	 * @param userDetailsService
	 *           the userDetailsService to set
	 */
	public void setUserDetailsService(final UserDetailsService userDetailsService)
	{
		this.userDetailsService = userDetailsService;
	}




}
