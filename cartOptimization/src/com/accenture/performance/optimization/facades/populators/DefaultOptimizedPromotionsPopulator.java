package com.accenture.performance.optimization.facades.populators;

import de.hybris.platform.commercefacades.product.converters.populator.PromotionsPopulator;
import de.hybris.platform.commercefacades.product.data.PromotionData;
import de.hybris.platform.promotions.model.AbstractPromotionModel;

public class DefaultOptimizedPromotionsPopulator extends PromotionsPopulator {
	protected void processPromotionMessages(final AbstractPromotionModel source, final PromotionData prototype)
	{
		if (getCartService().hasSessionCart())
		{
			//TODO acn
//			final CartModel cartModel = getCartService().getSessionCart();
//			if (cartModel != null)
//			{
//
//				final PromotionOrderResults promoOrderResults = getPromotionService().getPromotionResults(cartModel);
//
//				if (promoOrderResults != null)
//				{
//					prototype.setCouldFireMessages(getCouldFirePromotionsMessages(promoOrderResults, source));
//					prototype.setFiredMessages(getFiredPromotionsMessages(promoOrderResults, source));
//				}
//			}
		}
	}

}
