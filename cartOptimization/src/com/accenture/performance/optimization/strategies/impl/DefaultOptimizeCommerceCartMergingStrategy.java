package com.accenture.performance.optimization.strategies.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.commerceservices.order.CommerceCartMergingException;
import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.order.impl.DefaultCommerceCartMergingStrategy;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.product.UnitService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;
import com.accenture.performance.optimization.service.OptimizeCartService;
import com.accenture.performance.optimization.service.OptimizeModelDealService;
import com.accenture.performance.optimization.strategies.OptimizeCommerceCartMergingStrategy;


public class DefaultOptimizeCommerceCartMergingStrategy extends DefaultCommerceCartMergingStrategy
		implements OptimizeCommerceCartMergingStrategy
{
	private OptimizeCartService cartService;
	private UnitService unitService;
	private OptimizeModelDealService optimizeModelDealService;
	private ProductService productService;

	@Override
	public void mergeCarts(final OptimizedCartData fromCart, final OptimizedCartData toCart,
			final List<CommerceCartModification> modifications) throws CommerceCartMergingException
	{
		final UserModel currentUser = getUserService().getCurrentUser();

		if (currentUser == null || getUserService().isAnonymousUser(currentUser))
		{
			throw new AccessDeniedException("Only logged user can merge carts!");
		}

		validateParameterNotNull(fromCart, "fromCart can not be null");
		validateParameterNotNull(toCart, "toCart can not be null");

		if (!getBaseSiteService().getCurrentBaseSite().getUid().equals(fromCart.getBaseSite()))
		{
			throw new CommerceCartMergingException(String.format("Current site %s is not equal to cart %s site %s",
					getBaseSiteService().getCurrentBaseSite(), fromCart, fromCart.getBaseSite()));
		}

		if (!getBaseSiteService().getCurrentBaseSite().getUid().equals(toCart.getBaseSite()))
		{
			throw new CommerceCartMergingException(String.format("Current site %s is not equal to cart %s site %s",
					getBaseSiteService().getCurrentBaseSite(), toCart, toCart.getBaseSite()));
		}

		if (fromCart.getGuid().equals(toCart.getGuid()))
		{
			throw new CommerceCartMergingException("Cannot merge cart to itself!");
		}


		final List<CommerceCartParameter> parameterList = new ArrayList<>();
		for (final OptimizedCartEntryData entry : fromCart.getEntries())
		{
			final CommerceCartParameter newCartParameter = new CommerceCartParameter();
			newCartParameter.setEnableHooks(true);
			newCartParameter.setOptimizeCart(toCart);
			newCartParameter.setProduct(productService.getProductForCode(entry.getProductCode()));
			newCartParameter.setProductCode(entry.getProductCode());
			//		newCartParameter.setPointOfService(pointOfServiceModel);
			newCartParameter.setQuantity(entry.getQuantity() == null ? 0l : entry.getQuantity().longValue());
			newCartParameter.setUnit(unitService.getUnitForCode(entry.getUnit()));
			newCartParameter.setCreateNewEntry(false);

			parameterList.add(newCartParameter);
		}
		mergeModificationsToList(cartService.addToCart(parameterList), modifications);

		//		toCart.setCalculated(Boolean.FALSE);
		//TODO payment transactions - to clear or not to clear...

		//		getModelService().save(toCart);
		optimizeModelDealService.removePersistentCart(fromCart.getGuid(), fromCart.getUserId());
	}

	@Override
	protected void mergeModificationToList(final CommerceCartModification modificationToAdd,
			final List<CommerceCartModification> toModificationList)
	{
		if (modificationToAdd.getEntryData() != null)
		{
			for (final CommerceCartModification finalModification : toModificationList)
			{
				if (finalModification.getEntryData() == null)
				{
					continue;
				}
				if (finalModification.getEntryData().equals(modificationToAdd.getEntryData()))
				{
					finalModification.setQuantity(finalModification.getQuantity() + modificationToAdd.getQuantity());
					finalModification.setQuantityAdded(finalModification.getQuantityAdded() + modificationToAdd.getQuantityAdded());
					finalModification
							.setStatusCode(mergeStatusCodes(modificationToAdd.getStatusCode(), finalModification.getStatusCode()));
					return;
				}
			}
		}

		toModificationList.add(modificationToAdd);
	}

	@Override
	public ProductService getProductService()
	{
		return productService;
	}

	@Override
	public void setProductService(final ProductService productService)
	{
		this.productService = productService;
	}

	public OptimizeModelDealService getOptimizeModelDealService()
	{
		return optimizeModelDealService;
	}

	public void setOptimizeModelDealService(final OptimizeModelDealService optimizeModelDealService)
	{
		this.optimizeModelDealService = optimizeModelDealService;
	}

	public UnitService getUnitService()
	{
		return unitService;
	}

	public void setUnitService(final UnitService unitService)
	{
		this.unitService = unitService;
	}

	@Override
	public OptimizeCartService getCartService()
	{
		return cartService;
	}

	public void setCartService(final OptimizeCartService cartService)
	{
		this.cartService = cartService;
	}


}
