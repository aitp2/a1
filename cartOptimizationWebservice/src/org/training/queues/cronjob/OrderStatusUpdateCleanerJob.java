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
package org.training.queues.cronjob;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.training.model.expressupdate.cron.OrderStatusUpdateCleanerCronJobModel;
import org.training.queues.impl.OrderStatusUpdateQueue;

import java.util.Date;


/**
 * A Cron Job for cleaning up {@link org.training.queues.impl.OrderStatusUpdateQueue}.
 */
public class OrderStatusUpdateCleanerJob extends AbstractJobPerformable<OrderStatusUpdateCleanerCronJobModel>
{
	private OrderStatusUpdateQueue orderStatusUpdateQueue;

	@Override
	public PerformResult perform(final OrderStatusUpdateCleanerCronJobModel cronJob)
	{
		final Date timestamp = new Date(System.currentTimeMillis() - (cronJob.getQueueTimeLimit().intValue() * 60 * 1000));
		getOrderStatusUpdateQueue().removeItems(timestamp);
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	protected OrderStatusUpdateQueue getOrderStatusUpdateQueue()
	{
		return orderStatusUpdateQueue;
	}

	public void setOrderStatusUpdateQueue(final OrderStatusUpdateQueue orderStatusUpdateQueue)
	{
		this.orderStatusUpdateQueue = orderStatusUpdateQueue;
	}
}
