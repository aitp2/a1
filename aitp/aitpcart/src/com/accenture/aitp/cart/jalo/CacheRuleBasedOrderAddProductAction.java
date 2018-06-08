package com.accenture.aitp.cart.jalo;

import de.hybris.platform.core.PK;
import de.hybris.platform.jalo.ConsistencyCheckException;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.JaloOnlyItem;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.security.JaloSecurityException;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.JaloGenericCreationException;
import de.hybris.platform.promotions.jalo.PromotionResult;
import de.hybris.platform.servicelayer.internal.jalo.order.JaloOnlyItemHelper;

import java.util.Date;

import org.apache.log4j.Logger;


public class CacheRuleBasedOrderAddProductAction extends GeneratedCacheRuleBasedOrderAddProductAction implements JaloOnlyItem
{
	@SuppressWarnings("unused")
	private final static Logger LOG = Logger.getLogger(CacheRuleBasedOrderAddProductAction.class.getName());
	private JaloOnlyItemHelper data;

	@Override
	protected Item createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes)
			throws JaloBusinessException
	{
		final Class cl = type.getJaloClass();

		try
		{
			final CacheRuleBasedOrderAddProductAction e = (CacheRuleBasedOrderAddProductAction) cl.newInstance();
			e.setTenant(type.getTenant());
			e.data = new JaloOnlyItemHelper((PK) allAttributes.get(PK), e, type, new Date(), (Date) null);

			return e;

		}
		catch (InstantiationException | IllegalAccessException | ClassCastException arg5)
		{

			throw new JaloGenericCreationException(
					"could not instantiate wizard class " + cl + " of type " + type.getCode() + " : " + arg5, 0);
		}
	}


	@Override
	public Boolean isMarkedApplied(final SessionContext ctx)
	{
		return (Boolean) this.data.getProperty(ctx, "markedApplied");
	}



	@Override
	public void setMarkedApplied(final SessionContext ctx, final Boolean markedApplied)
	{
		this.data.setProperty(ctx, "markedApplied", markedApplied);
	}


	@Override
	public String getGuid(final SessionContext ctx)
	{
		return (String) this.data.getProperty(ctx, "guid");
	}


	@Override
	public void setGuid(final SessionContext ctx, final String guid)
	{
		this.data.setProperty(ctx, "guid", guid);
	}


	@Override
	public PromotionResult getPromotionResult(final SessionContext ctx)
	{
		return (PromotionResult) this.data.getProperty(ctx, "promotionResult");
	}

	@Override
	public void setPromotionResult(final SessionContext ctx, final PromotionResult promotionResult)
	{
		this.data.setProperty(ctx, "promotionResult", promotionResult);
	}

	@Override
	public void removeJaloOnly() throws ConsistencyCheckException
	{
		this.data.removeJaloOnly();

	}

	@Override
	public PK providePK()
	{
		return this.data.providePK();
	}

	@Override
	public Date provideCreationTime()
	{
		return this.data.provideCreationTime();
	}

	@Override
	public Date provideModificationTime()
	{
		return this.data.provideModificationTime();
	}

	@Override
	public ComposedType provideComposedType()
	{
		return this.data.provideComposedType();
	}

	@Override
	public Object doGetAttribute(final SessionContext ctx, final String attrQualifier)
			throws JaloInvalidParameterException, JaloSecurityException
	{
		return this.data.doGetAttribute(ctx, attrQualifier);
	}

	@Override
	public void doSetAttribute(final SessionContext ctx, final String attrQualifier, final Object value)
			throws JaloInvalidParameterException, JaloSecurityException, JaloBusinessException
	{
		this.data.doSetAttribute(ctx, attrQualifier, value);

	}
}
