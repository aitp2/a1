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
package org.training.auth;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;


/**
 *
 */
public class GuestAuthenticationToken extends AbstractAuthenticationToken
{
	private final String email;

	/**
	 * @param authorities
	 */
	public GuestAuthenticationToken(final String email, final Collection<? extends GrantedAuthority> authorities)
	{
		super(authorities);
		this.email = email;
		setAuthenticated(true); //NOSONAR
	}

	@Override
	public Object getCredentials()
	{
		return null;
	}

	@Override
	public Object getPrincipal()
	{
		return email;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!super.equals(obj))
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}

		final GuestAuthenticationToken other = (GuestAuthenticationToken) obj;
		if (email == null)
		{
			if (other.email != null)
			{
				return false;
			}
		}
		else if (!email.equals(other.email))
		{
			return false;
		}
		return true;
	}


}
