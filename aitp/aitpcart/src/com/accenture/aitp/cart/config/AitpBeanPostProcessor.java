/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2018 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package com.accenture.aitp.cart.config;

import de.hybris.platform.servicelayer.web.SessionFilter;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.web.filter.GenericFilterBean;


/**
 *
 */
public class AitpBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware
{

	private static final Logger LOGGER = Logger.getLogger(AitpBeanPostProcessor.class);

	private ConfigurableListableBeanFactory beanFactory;

	private final String addBeanName = "storeSessionCartFilter";
	private final Class afterClass = SessionFilter.class;

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException
	{

		if ("defaultStorefrontTenantDefaultFilterChainList".equals(beanName)
				&& bean.getClass().isAssignableFrom(ListFactoryBean.class))
		{
			final ListFactoryBean listFactoryBean = (ListFactoryBean) bean;
			try
			{

				final List<Object> listBeans = listFactoryBean.getObject();

				if (LOGGER.isDebugEnabled())
				{
					listBeans.stream().forEach(filterBean -> LOGGER.info("list bean class:[" + filterBean.getClass() + "]"));
				}

				final GenericFilterBean storeSessionCartFilter = (GenericFilterBean) beanFactory.getBean(addBeanName);
				LOGGER.info("storeSessionCartFilter bean class:[" + storeSessionCartFilter.getClass() + "]");
				final int index = getIndexByClass(listBeans, afterClass);
				LOGGER.info("storeSessionCartFilter has replace index:[" + (index + 1) + "]");
				listBeans.add(index + 1, storeSessionCartFilter);
				listFactoryBean.setSourceList(listBeans);
				return listFactoryBean;

			}
			catch (final Exception e)
			{
				throw new BeanCreationException("aitp Process ", e);
			}
		}
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException
	{
		return bean;
	}

	protected int getIndexByClass(final List listBeans, final Class classze)
	{
		for (int i = 0; i < listBeans.size(); i++)
		{
			if (listBeans.get(i).getClass().isAssignableFrom(classze))
			{
				return i;
			}
		}
		return listBeans.size();
	}

	@Override
	public void setBeanFactory(final BeanFactory beanFactory) throws BeansException
	{
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;

	}

}
