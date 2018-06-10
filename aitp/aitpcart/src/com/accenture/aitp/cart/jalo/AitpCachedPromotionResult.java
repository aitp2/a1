package com.accenture.aitp.cart.jalo;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.promotions.jalo.CachedPromotionResult;

import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;


public class AitpCachedPromotionResult extends GeneratedAitpCachedPromotionResult
{
	@SuppressWarnings("unused")
	private final static Logger LOG = Logger.getLogger(AitpCachedPromotionResult.class.getName());

	@Override
	protected Item createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes)
			throws JaloBusinessException
	{
		// business code placed here will be executed before the item is created
		// then create the item
		final Item item = super.createItem(ctx, type, allAttributes);
		// business code placed here will be executed after the item was created
		// and return the item
		return item;
	}

	@Override
	public void setOrder(final SessionContext ctx, final AbstractOrder order)
	{
		super.setOrder(ctx, order);
		try
		{
			final Set<CachedPromotionResult> prSet = (Set<CachedPromotionResult>) order
					.getAttribute(AbstractOrderModel.ALLPROMOTIONRESULTS);
			prSet.add(this);
			order.setAttribute(AbstractOrderModel.ALLPROMOTIONRESULTS, prSet);
		}
		catch (final Exception e)
		{
			LOG.error("promotion result set order exception", e);
		}

	}

	@Override
	public void removeJaloOnly() throws ConsistencyCheckException
	{
		try
		{
			final AbstractOrder order = getOrder();
			final Set<CachedPromotionResult> prSet = (Set<CachedPromotionResult>) order
					.getAttribute(AbstractOrderModel.ALLPROMOTIONRESULTS);
			final Iterator<CachedPromotionResult> it = prSet.iterator();
			CachedPromotionResult promotionResult;
			while (it.hasNext())
			{
				promotionResult = it.next();
				if (getPK().equals(promotionResult.getPK()))
				{
					it.remove();
				}
			}
			order.setAttribute(AbstractOrderModel.ALLPROMOTIONRESULTS, prSet);
		}
		catch (final Exception e)
		{
			LOG.error("promotion result set order exception", e);
		}
		super.removeJaloOnly();
	}

}
