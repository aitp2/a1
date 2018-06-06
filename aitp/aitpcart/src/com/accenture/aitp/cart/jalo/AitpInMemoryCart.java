package com.accenture.aitp.cart.jalo;

import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.security.JaloSecurityException;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.promotions.jalo.CachedPromotionResult;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;


public class AitpInMemoryCart extends GeneratedAitpInMemoryCart
{
	@SuppressWarnings("unused")
	private final static Logger LOG = Logger.getLogger(AitpInMemoryCart.class.getName());
	private final Set<CachedPromotionResult> promotionResults = new HashSet<>();

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
	public void setAttribute(final SessionContext ctx, final String qualifier, final Object value)
			throws JaloInvalidParameterException, JaloSecurityException, JaloBusinessException
	{
		if ("allPromotionResults".equals(qualifier))
		{
			promotionResults.clear();
			promotionResults.addAll((Set) value);
		}
		else
		{
			super.setAttribute(ctx, qualifier, value);
		}
	}

	@Override
	public Object getAttribute(final SessionContext ctx, final String qualifier)
			throws JaloInvalidParameterException, JaloSecurityException
	{
		Object retval = null;
		if ("allPromotionResults".equals(qualifier))
		{
			/*
			 * final RedisTemplate redisTemplate = (RedisTemplate)
			 * Registry.getApplicationContext().getBean("redisTemplate");
			 *
			 * retval = redisTemplate.execute(new RedisCallback<Set<PromotionResult>>() {
			 *
			 * @Override public Set<PromotionResult> doInRedis(final RedisConnection redisConnection) throws
			 * DataAccessException { redisConnection.select(2); final byte[] result =
			 * redisConnection.get(redisTemplate.getKeySerializer().serialize(getCode())); return (Set<PromotionResult>)
			 * redisTemplate.getValueSerializer().deserialize(result); }
			 *
			 * });
			 */
			final Set<CachedPromotionResult> prSet = new HashSet<>(this.promotionResults.size());
			promotionResults.stream().forEach(pr -> prSet.add(pr));
			retval = prSet;
		}
		else
		{
			retval = super.getAttribute(ctx, qualifier);
		}
		return retval;
	}



}
