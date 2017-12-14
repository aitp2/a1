/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package org.training.v1.controller;

import de.hybris.platform.jalo.JaloSession;
import org.training.auth.data.LogoutResponse;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller("logoutControllerV1")
@RequestMapping(value = "/customers")
public class LogoutController
{
	/**
	 * Spring security logout successful redirect.
	 */
	@RequestMapping(value = "/current/logout", method = RequestMethod.POST)
	@ResponseBody
	public LogoutResponse logout(final HttpServletRequest request)
	{
		JaloSession.getCurrentSession().close();
		request.getSession().invalidate();
		final LogoutResponse logoutResponse = new LogoutResponse();
		logoutResponse.setSuccess(true);
		return logoutResponse;
	}
}
