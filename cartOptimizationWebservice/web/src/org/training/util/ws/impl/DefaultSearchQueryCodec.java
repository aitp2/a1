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
package org.training.util.ws.impl;

import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchQueryData;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchQueryTermData;
import org.training.util.ws.SearchQueryCodec;

import java.util.ArrayList;
import java.util.List;


/**
 */
public class DefaultSearchQueryCodec implements SearchQueryCodec<SolrSearchQueryData>
{
	@Override
	public SolrSearchQueryData decodeQuery(final String queryString)
	{
		final SolrSearchQueryData searchQuery = new SolrSearchQueryData();
		final List<SolrSearchQueryTermData> filters = new ArrayList<SolrSearchQueryTermData>();

		if (queryString == null)
		{
			return searchQuery;
		}

		final String[] parts = queryString.split(":");
		
		if (parts.length > 0)
		{
			searchQuery.setFreeTextSearch(parts[0]);
			if (parts.length > 1)
			{
				searchQuery.setSort(parts[1]);
			}
		}

		for (int i = 2; i < parts.length; i = i + 2)
		{
			final SolrSearchQueryTermData term = new SolrSearchQueryTermData();
			term.setKey(parts[i]);
			term.setValue(parts[i + 1]);
			filters.add(term);
		}
		searchQuery.setFilterTerms(filters);

		return searchQuery;
	}

	@Override
	public String encodeQuery(final SolrSearchQueryData searchQueryData)
	{
		if (searchQueryData == null)
		{
			return null;
		}

		final StringBuilder builder = new StringBuilder();
		builder.append((searchQueryData.getFreeTextSearch() == null) ? "" : searchQueryData.getFreeTextSearch());


		if (searchQueryData.getSort() != null
				|| (searchQueryData.getFilterTerms() != null && !searchQueryData.getFilterTerms().isEmpty()))
		{
			builder.append(":");
			builder.append((searchQueryData.getSort() == null) ? "" : searchQueryData.getSort());
		}

		final List<SolrSearchQueryTermData> terms = searchQueryData.getFilterTerms();
		if (terms != null && !terms.isEmpty())
		{
			for (final SolrSearchQueryTermData term : searchQueryData.getFilterTerms())
			{
				builder.append(":");
				builder.append(term.getKey());
				builder.append(":");
				builder.append(term.getValue());
			}
		}

		//URLEncode?
		return builder.toString();
	}
}
