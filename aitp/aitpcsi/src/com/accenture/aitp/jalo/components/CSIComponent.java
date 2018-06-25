package com.accenture.aitp.jalo.components;

import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.type.ComposedType;

import org.apache.log4j.Logger;


public class CSIComponent extends GeneratedCSIComponent
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(CSIComponent.class.getName());

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.hybris.platform.cms2.jalo.contents.components.GeneratedAbstractCMSComponent#isContainer(de.hybris.platform.jalo
	 * .SessionContext)
	 */
	@Override
	public Boolean isContainer(final SessionContext arg0)
	{
		// XXX Auto-generated method stub
		return Boolean.FALSE;
	}

}
