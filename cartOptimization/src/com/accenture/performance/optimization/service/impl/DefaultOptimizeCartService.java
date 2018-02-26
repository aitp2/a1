/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2017 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package com.accenture.performance.optimization.service.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;
import com.accenture.performance.optimization.model.OptimizedCartModel;
import com.accenture.performance.optimization.service.OptimizeCartService;
import com.accenture.performance.optimization.service.OptimizeModelDealService;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.commercefacades.order.data.AddToCartParams;
import de.hybris.platform.commerceservices.constants.CommerceServicesConstants;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.order.CommerceCartMergingException;
import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.CommerceCartModificationStatus;
import de.hybris.platform.commerceservices.order.hook.CommerceAddToCartMethodHook;
import de.hybris.platform.commerceservices.order.hook.CommerceUpdateCartEntryHook;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.JaloObjectNoLongerValidException;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.order.impl.DefaultCartService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.session.SessionService.SessionAttributeLoader;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.storelocator.pos.PointOfServiceService;


/**
 *
 */
public class DefaultOptimizeCartService extends DefaultCartService implements OptimizeCartService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultOptimizeCartService.class);
	private static final int APPEND_AS_LAST = -1;
	
	@Autowired
	private OptimizeCartFactory cartFactory;
	private SessionService sessionService;

	public static final String SESSION_CART_PARAMETER_NAME = "cart";
	public static final String SESSION_OPTIMIZED_CART_PARAMETER_NAME = "optimizedcart";

	private ProductService productService;
	private List<CommerceAddToCartMethodHook> commerceAddToCartMethodHooks;
	private List<CommerceUpdateCartEntryHook> commerceUpdateCartEntryHooks;
	private ConfigurationService configurationService;
	private BaseSiteService baseSiteService;
	private PointOfServiceService pointOfServiceService;
	@Autowired
	private DefaultOptimizeCommerceCartService defaultOptimizeCommerceCartService;

	private OptimizeModelDealService optimizeModelDealService;
	private CommonI18NService commonI18NService;

	@Autowired
	private UserService userService;
	@Autowired
	private BaseStoreService baseStoreService;
	@Autowired
	private CatalogVersionService catalogVersionService;

	private CustomerAccountService customerAccountService;

	/**
	 * @return the customerAccountService
	 */
	public CustomerAccountService getCustomerAccountService()
	{
		return customerAccountService;
	}

	/**
	 * @param customerAccountService
	 *           the customerAccountService to set
	 */
	public void setCustomerAccountService(final CustomerAccountService customerAccountService)
	{
		this.customerAccountService = customerAccountService;
	}

	@Override
	public CommerceCartParameter validateCartParameter(final AddToCartParams parameters) throws CommerceCartModificationException
	{
		final CommerceCartParameter commerceCartParameter = new CommerceCartParameter();
		if (parameters.getQuantity() < 1)
		{
			throw new CommerceCartModificationException("Quantity must not be less than one");
		}
		//check productForCode
		final ProductModel productModel = getProductService().getProductForCode(parameters.getProductCode());
		//final ProductModel productModel = this.getProductForCode(parameters.getProductCode());
		if (productModel == null)
		{
			throw new CommerceCartModificationException("Could not found Product");
		}
		else
		{
			if (productModel.getVariantType() != null)
			{
				throw new CommerceCartModificationException("Choose a variant instead of the base product");
			}
		}

		if (StringUtils.isNotEmpty(parameters.getStoreId()))
		{
			final PointOfServiceModel pointOfServiceModel = getPointOfServiceService()
					.getPointOfServiceForName(parameters.getStoreId());
			commerceCartParameter.setPointOfService(pointOfServiceModel);
		}
		commerceCartParameter.setQuantity(parameters.getQuantity());
		commerceCartParameter.setCreateNewEntry(false);
		commerceCartParameter.setEntryGroupNumbers(parameters.getEntryGroupNumbers());
		commerceCartParameter.setEnableHooks(true);
		commerceCartParameter.setBaseSite(getBaseSiteService().getCurrentBaseSite());
		commerceCartParameter.setProduct(productModel);
		commerceCartParameter.setUnit(productModel.getUnit());
		commerceCartParameter.setBasePrice(productModel.getEurope1Prices().stream().findFirst().get().getPrice());
		commerceCartParameter.setProductCode(productModel.getCode());
		return commerceCartParameter;
	}

	@Override
	public OptimizedCartData getSessionOptimizedCart()
	{
		try
		{
			return internalGetSessionOptimizedCart();
		}
		catch (final JaloObjectNoLongerValidException ex)
		{
			if (LOG.isInfoEnabled())
			{
				LOG.info("Session Cart no longer valid. Removing from session. getSessionCart will create a new cart. "
						+ ex.getMessage());
			}
			return internalGetSessionOptimizedCart();
		}
	}

	@Override
	public int getCurrentCurrencyDigit()
	{
		final CurrencyModel curr = commonI18NService.getBaseCurrency();
		return curr.getDigits().intValue();
	}


	protected OptimizedCartData internalGetSessionOptimizedCart()
	{
		final String cartId = getSessionService().getOrLoadAttribute(SESSION_OPTIMIZED_CART_PARAMETER_NAME,
				new SessionAttributeLoader<String>()
				{
					@Override
					public String load()
					{
						final OptimizedCartData cartData = cartFactory.createSessionCart();
						return cartData.getGuid();
					}
				});

		return optimizeModelDealService.getSessionCart(cartId);
	}

	@Override
	public boolean hasSessionCart()
	{
		try
		{
			boolean flag = getSessionService().getAttribute(SESSION_OPTIMIZED_CART_PARAMETER_NAME) != null  ;
			if(flag) 
			{
				if(internalGetSessionOptimizedCart() != null)
				{
					return flag;
				}
				else
				{
					throw new JaloObjectNoLongerValidException(null,null,"Session Cart no longer valid.",0);
				}
			}
			else
			{
				return flag;
			}
			
		}
		catch (final JaloObjectNoLongerValidException ex)
		{
			if (LOG.isInfoEnabled())
			{
				LOG.info("Session Cart no longer valid. Removing from session. hasSessionCart will return false. " + ex.getMessage());
			}
			getSessionService().removeAttribute(SESSION_CART_PARAMETER_NAME);
			getSessionService().removeAttribute(SESSION_OPTIMIZED_CART_PARAMETER_NAME);
			return false;
		}
	}

	@Override
	public void changeCurrentCartUser(final UserModel user)
	{
		validateParameterNotNull(user, "user must not be null!");
		if (hasSessionCart())
		{
			final OptimizedCartData sessionCart = getSessionOptimizedCart();
			sessionCart.setUserId(user.getUid());
			//		optimizeModelDealService.removePersistentCart(sessionCart.getGuid(), user.getUid());
			setSessionOptimizedCart(sessionCart);
		}
	}



	@Override
	public void setSessionOptimizedCart(final OptimizedCartData cartData)
	{
		if (cartData == null)
		{
			removeSessionCart();
		}
		else
		{
			getSessionService().setAttribute(SESSION_OPTIMIZED_CART_PARAMETER_NAME, cartData.getGuid());
			optimizeModelDealService.persistCart(cartData);
		}
		
	}
	
	@Override
	public void removeSessionCart()
	{
		if (hasSessionCart())
		{
//			final CartModel sessionCart = getSessionCart();
//			getModelService().remove(sessionCart);
//			getSessionService().removeAttribute(SESSION_CART_PARAMETER_NAME);
			
			getSessionService().removeAttribute(SESSION_OPTIMIZED_CART_PARAMETER_NAME);
		}
	}

	@Override
	public CommerceCartModification addToCart(final AddToCartParams parameter) throws CommerceCartModificationException
	{
		final CommerceCartParameter cartParameterData = beforeAddToCart(parameter);

		final CommerceCartModification modification = doAddToCart(cartParameterData);
		final OptimizedCartData cartData = getSessionOptimizedCart();

		try
		{
			defaultOptimizeCommerceCartService.calculateCart(cartData, true);
		}
		catch (final Exception ex)
		{
			LOG.error("calculate failed ..." + ex.getMessage(), ex);
		}
		final long newNumber = normalizeEntryNumbers(cartData, parameter.getProductCode());
		cartParameterData.setEntryNumber(newNumber);
		cartParameterData.setOptimizeCart(cartData);
		
		afterAddToCart(cartParameterData, modification);
		// Here the entry is fully populated, so we can search for a similar one and merge.
		//mergeEntry(modification, cartParameterData);
		return modification;

	}

	/**
	 * Do add to cart.
	 *
	 * @param parameter
	 *           the parameter
	 * @return the commerce cart modification
	 * @throws CommerceCartModificationException
	 *            the commerce cart modification exception
	 */
	@Override
	public CommerceCartModification doAddToCart(final CommerceCartParameter parameter) throws CommerceCartModificationException
	{
		CommerceCartModification modification = null;
		final ProductModel productModel = parameter.getProduct();
		final String productCode = productModel.getCode();
		final long quantityToAdd = parameter.getQuantity();
		//final PointOfServiceModel deliveryPointOfService = parameter.getPointOfService();
		final OptimizedCartData cartData = parameter.getOptimizeCart() != null ? parameter.getOptimizeCart()
				: this.getSessionOptimizedCart();
		// So now work out what the maximum allowed to be added is (note that this may be negative!)
		//		if (cartData == null)
		//		{
		//			cartData = this.getSessionOptimizedCart();
		//		}
		final long actualAllowedQuantityChange = getAllowedCartAdjustmentForProduct(cartData, productCode, quantityToAdd,
				productModel.getMaxOrderQuantity());
		//final Integer maxOrderQuantity = productModel.getMaxOrderQuantity();
		//final long cartLevel = checkCartLevel(cartData, productCode);
		//final long cartLevelAfterQuantityChange = actualAllowedQuantityChange + cartLevel;

		if (actualAllowedQuantityChange > 0)
		{
			// We are allowed to add items to the cart
			final OptimizedCartEntryData entryData = addCartEntry(parameter, cartData, actualAllowedQuantityChange);
			setSessionOptimizedCart(cartData);
			final String statusCode = CommerceCartModificationStatus.SUCCESS;
			//getStatusCodeAllowedQuantityChange(actualAllowedQuantityChange, maxOrderQuantity, quantityToAdd, cartLevelAfterQuantityChange);

			modification = createAddToCartResp(parameter, statusCode, entryData, actualAllowedQuantityChange);
		}
		else
		{
			// Not allowed to add any quantity, or maybe even asked to reduce the quantity
			// Do nothing!
			final String status = CommerceCartModificationStatus.NO_STOCK;
			//getStatusCodeForNotAllowedQuantityChange(maxOrderQuantity, maxOrderQuantity);
			final OptimizedCartEntryData entryData = new OptimizedCartEntryData();
			entryData.setBasePrice(Double.valueOf(0));
			entryData.setTotalPrice(Double.valueOf(0));
			entryData.setProductCode(productCode);
			modification = createAddToCartResp(parameter, status, entryData, 0);

		}
		return modification;

	}

	@Override
	public boolean isValidDeliveryAddress(final OptimizedCartData cartModel, final AddressModel addressModel)
	{
		if (addressModel != null)
		{
			final UserModel user = getUserService().getUserForUID(cartModel.getUserId());
			final List<AddressModel> supportedAddresses = getCustomerAccountService().getAllAddressEntries((CustomerModel) user);
			return supportedAddresses != null && supportedAddresses.contains(addressModel);
		}
		else
		{
			return true;
		}
	}

	protected CommerceCartParameter beforeAddToCart(final AddToCartParams parameters) throws CommerceCartModificationException
	{
		// Empty method - extension point
		return validateCartParameter(parameters);
	}

	protected OptimizedCartEntryData addCartEntry(final CommerceCartParameter parameter, final OptimizedCartData cartData,
			final long actualAllowedQuantityChange) throws CommerceCartModificationException
	{
		// search for present entries for this product if needed
		final String productCode = parameter.getProduct().getCode();
		final long qty = actualAllowedQuantityChange;
		final String unit = parameter.getUnit().getCode();
		OptimizedCartEntryData ret = getEntriesForProduct(cartData, productCode);
		if (ret == null)
		{
			ret = new OptimizedCartEntryData();
			ret.setQuantity(Long.valueOf(qty));
			ret.setProductCode(productCode);
			ret.setUnit(unit);
			final Double basePrice = parameter.getProduct().getEurope1Prices().stream().findFirst().get().getPrice();
			ret.setBasePrice(basePrice);
			ret.setEntryNumber(new Integer(cartData.getEntries() == null ? 0 : cartData.getEntries().size()));
			ret.setCartData(cartData);
			List<OptimizedCartEntryData> entryList = new ArrayList<OptimizedCartEntryData>();

			if (cartData.getEntries() != null)
			{
				entryList = cartData.getEntries();
			}
			try
			{
				defaultOptimizeCommerceCartService.calculateOneEntries(ret, false);
			}
			catch (final CalculationException ex)
			{
				LOG.info("[Calculate entries failed. The entryNumber is{0}, the error message is {1}", ret.getEntryNumber(),
						ex.getMessage());
			}
			entryList.add(ret);
			cartData.setEntries(entryList);
		}
		else
		{
			ret.setQuantity(Long.valueOf(ret.getQuantity().longValue() + qty));
		}
		cartData.setCalculated(Boolean.FALSE);
		parameter.setOptimizeCart(cartData);
		return ret;

	}

	protected void afterAddToCart(final CommerceCartParameter parameter, final CommerceCartModification result)
			throws CommerceCartModificationException
	{
		if (getCommerceAddToCartMethodHooks() != null && (parameter.isEnableHooks()
				&& getConfigurationService().getConfiguration().getBoolean(CommerceServicesConstants.ADDTOCARTHOOK_ENABLED)))
		{
			for (final CommerceAddToCartMethodHook commerceAddToCartMethodHook : getCommerceAddToCartMethodHooks())
			{
				commerceAddToCartMethodHook.afterAddToCart(parameter, result);

			}
		}
		optimizeModelDealService.persistCart(parameter.getOptimizeCart());

	}

	/**
	 * Work out the allowed quantity adjustment for a product in the cart given a requested quantity change.
	 *
	 * @param productCode
	 *           the productCode in the cart
	 * @param quantityToAdd
	 *           the amount to increase the quantity of the product in the cart, may be negative if the request is to
	 *           reduce the quantity
	 * @return the allowed adjustment
	 */
	protected long getAllowedCartAdjustmentForProduct(final OptimizedCartData optimizedCartData, final String productCode,
			final long quantityToAdd, final Integer productMaxOrderQuantity)
	{
		final long cartLevel = checkCartLevel(optimizedCartData, productCode);
		final long stockLevel = getAvailableStockLevel(productCode);

		// How many will we have in our cart if we add quantity
		final long newTotalQuantity = cartLevel + quantityToAdd;

		// Now limit that to the total available in stock
		final long newTotalQuantityAfterStockLimit = Math.min(newTotalQuantity, stockLevel);

		// So now work out what the maximum allowed to be added is (note that
		// this may be negative!)
		final Integer maxOrderQuantity = productMaxOrderQuantity == null ? new Integer("9999") : productMaxOrderQuantity;

		//if (maxOrderQuantity != null)
		//	{
		final long newTotalQuantityAfterProductMaxOrder = Math.min(newTotalQuantityAfterStockLimit, maxOrderQuantity.longValue());
		return newTotalQuantityAfterProductMaxOrder - cartLevel;
		//	}
		//	return newTotalQuantityAfterStockLimit - cartLevel;
	}

	protected CommerceCartModification createAddToCartResp(final CommerceCartParameter parameter, final String status,
			final OptimizedCartEntryData entryData, final long quantityAdded)
	{
		final long quantityToAdd = parameter.getQuantity();
		final CommerceCartModification modification = new CommerceCartModification();
		modification.setStatusCode(status);
		modification.setQuantityAdded(quantityAdded);
		modification.setQuantity(quantityToAdd);
		modification.setEntryData(entryData);
		return modification;
	}

	protected OptimizedCartEntryData getEntriesForProduct(final OptimizedCartData optimizedCartData, final String productCode)
	{
		if (optimizedCartData.getEntries() != null)
		{
			for (final OptimizedCartEntryData e : optimizedCartData.getEntries())
			{
				// Ensure that order entry is not a 'give away', and has same units
				//	if (e.getProductCode().equals(productCode) && Boolean.FALSE.equals(e.getGiveAway()) && usedUnit.equals(e.getUnit()))
				if (e.getProductCode().equals(productCode))
				{
					return e;
				}
			}
		}
		return null;
	}

	protected long normalizeEntryNumbers(final OptimizedCartData cartData, final String code)
	{
		long newNumber = -1;
		final List<OptimizedCartEntryData> entries = cartData.getEntries();
		Collections.sort(entries, new BeanComparator("entryNumber", new ComparableComparator()));
		for (int i = 0; i < entries.size(); i++)
		{
			entries.get(i).setEntryNumber(Integer.valueOf(i));
			if (entries.get(i).getProductCode().equals(code))
			{
				newNumber = new Long("" + i + "").longValue();
			}
		}
		cartData.setEntries(entries);
		return newNumber;
	}

	protected long checkCartLevel(final OptimizedCartData cartData, final String productCode)
	{
		long cartLevel = 0;
		final OptimizedCartEntryData cartEntryData = getEntriesForProduct(cartData, productCode);
		if (cartEntryData != null)
		{
			cartLevel += cartEntryData.getQuantity().longValue();
		}
		return cartLevel;
	}

	protected long getAvailableStockLevel(final String productCode)
	{
		return 9999l;
	}

	@Override
	public CommerceCartModification updateQuantityForCartEntry(final CommerceCartParameter parameters)
			throws CommerceCartModificationException
	{
		//beforeUpdateCartEntry(parameters);
		//final CartModel cartModel = parameters.getCart();

		final long newQuantity = parameters.getQuantity();
		final long entryNumber = parameters.getEntryNumber();
		final OptimizedCartData cartData = getSessionOptimizedCart();
		final OptimizedCartEntryData cartEntryData = cartData.getEntries().stream()
				.filter(s -> s.getEntryNumber().equals(Integer.valueOf(String.valueOf(entryNumber)))).findFirst().get();
		final String productCode = cartEntryData.getProductCode();
		parameters.setProductCode(productCode);

		//final ProductModel productModel = cartparameters.getProduct();

		validateParameterNotNull(cartData, "Cart model cannot be null");
		CommerceCartModification modification;

		final OptimizedCartEntryData entryToUpdate = getEntryForNumber(cartData, (int) entryNumber);
		//validateEntryBeforeModification(newQuantity, entryToUpdate);
		final Integer maxOrderQuantity = entryToUpdate.getMaxOrderQuantity() == null ? new Integer("999")
				: entryToUpdate.getMaxOrderQuantity();
		// Work out how many we want to add (could be negative if we are
		// removing items)
		final long quantityToAdd = newQuantity - entryToUpdate.getQuantity().longValue();
		final long actualAllowedQuantityChange = getAllowedCartAdjustmentForProduct(cartData, productCode, quantityToAdd, null);
		modification = modifyEntry(cartData, entryToUpdate, actualAllowedQuantityChange, newQuantity, maxOrderQuantity);
		parameters.setOptimizeCart(cartData);
		afterUpdateCartEntry(parameters, modification);
		return modification;

	}

	protected OptimizedCartEntryData getEntryForNumber(final OptimizedCartData order, final int number)
	{
		final List<OptimizedCartEntryData> entries = order.getEntries();
		if (entries != null && !entries.isEmpty())
		{
			final Integer requestedEntryNumber = Integer.valueOf(number);
			for (final OptimizedCartEntryData entry : entries)
			{
				if (entry != null && requestedEntryNumber.equals(entry.getEntryNumber()))
				{
					return entry;
				}
			}
		}
		return null;
	}

	protected CommerceCartModification modifyEntry(final OptimizedCartData cartData, final OptimizedCartEntryData entryToUpdate,
			final long actualAllowedQuantityChange, final long newQuantity, final Integer maxOrderQuantity)
	{
		// Now work out how many that leaves us with on this entry
		final long entryNewQuantity = entryToUpdate.getQuantity().longValue() + actualAllowedQuantityChange;

		// Adjust the entry quantity to the new value
		entryToUpdate.setQuantity(Long.valueOf(entryNewQuantity));
		LOG.info("========entryNewQuantity=======" + entryNewQuantity);
		try
		{
			defaultOptimizeCommerceCartService.calculateCart(cartData,true);
		}
		catch (final IllegalStateException ex)
		{
			LOG.info("[Calculate entries failed. The entryNumber is{0}, the error message is {1}", entryToUpdate.getEntryNumber(),
					ex.getMessage());
		}

		// Return the modification data
		final CommerceCartModification modification = new CommerceCartModification();
		modification.setQuantityAdded(actualAllowedQuantityChange);
		modification.setEntryData(entryToUpdate);
		modification.setQuantity(entryNewQuantity);

		if (maxOrderQuantity != null && entryNewQuantity == maxOrderQuantity.longValue())
		{
			modification.setStatusCode(CommerceCartModificationStatus.MAX_ORDER_QUANTITY_EXCEEDED);
		}
		else if (newQuantity == entryNewQuantity)
		{
			modification.setStatusCode(CommerceCartModificationStatus.SUCCESS);
		}
		else
		{
			modification.setStatusCode(CommerceCartModificationStatus.LOW_STOCK);
		}

		return modification;

	}

	protected void afterUpdateCartEntry(final CommerceCartParameter parameter, final CommerceCartModification result)
	{
		if (getCommerceUpdateCartEntryHooks() != null && parameter.isEnableHooks() && getConfigurationService().getConfiguration()
				.getBoolean(CommerceServicesConstants.UPDATECARTENTRYHOOK_ENABLED, true))
		{
			for (final CommerceUpdateCartEntryHook commerceUpdateCartEntryHook : getCommerceUpdateCartEntryHooks())
			{
				commerceUpdateCartEntryHook.afterUpdateCartEntry(parameter, result);
			}
		}
		optimizeModelDealService.persistCart(parameter.getOptimizeCart());
	}

	@Override
	public OptimizedCartData getCartForGuidAndSiteAndUser(final String cartguid, final BaseSiteModel currentBaseSite,
			final String currentUser)
	{
		return optimizeModelDealService.getCartDataForGuidAndSiteAndUser(cartguid, currentBaseSite, currentUser);
	}

	@Override
	public void changeSessionCartCurrency(final CurrencyModel currency)
	{
		validateParameterNotNull(currency, "currency must not be null!");
		if (hasSessionCart())
		{
			//final CartModel sessionCart = this.getSessionOptimizedCart();
			//sessionCart.setCurrency(currency);
			//getModelService().save(sessionCart);
		}

	}

	@Override
	public List<OptimizedCartModel> getCartsForSiteAndUser(final BaseSiteModel currentBaseSite, final UserModel currentUser)
	{
		return optimizeModelDealService.getCartsDataForSiteAndUser(currentBaseSite, currentUser);
	}

	@Override
	public List<CommerceCartModification> addToCart(final List<CommerceCartParameter> parameterList)
			throws CommerceCartMergingException
	{
		final List<CommerceCartModification> modifications = new ArrayList<>();
		try
		{
			if (!CollectionUtils.isEmpty(parameterList))
			{
				// add items to cart
				final Map<CommerceCartParameter, CommerceCartModification> paramModificationMap = new HashMap<>();
				for (final CommerceCartParameter parameter : parameterList)
				{
					final CommerceCartModification modification = doAddToCart(parameter);
					paramModificationMap.put(parameter, modification);
					modifications.add(modification);
				}

				// calculate the cart. Using recalculateCart() to force calculate the entries
				//		getCommerceCartCalculationStrategy().recalculateCart(parameterList.get(0));

				// after add to cart
				for (final Entry<CommerceCartParameter, CommerceCartModification> entry : paramModificationMap.entrySet())
				{
					final CommerceCartParameter storedParameter = entry.getKey();
					final CommerceCartModification storedModification = entry.getValue();
					afterAddToCart(storedParameter, storedModification);
					//		mergeEntry(storedModification, storedParameter);
				}
			}
		}
		catch (final CommerceCartModificationException e)
		{
			throw new CommerceCartMergingException(e.getMessage(), e);
		}
		return modifications;
	}

	@Override
	public OptimizedCartEntryData addNewEntry(OptimizedCartData order, ProductModel product, long qty, UnitModel unit,
			int number, boolean addToPresent) {
		validateParameterNotNullStandardMessage("product", product);
		validateParameterNotNullStandardMessage("order", order);
		checkQuantity(qty, number);
		UnitModel usedUnit = unit;
		if (usedUnit == null)
		{
			LOG.debug("No unit passed, trying to get product unit");
			usedUnit = product.getUnit();
			validateParameterNotNullStandardMessage("usedUnit", usedUnit);
		}

		OptimizedCartEntryData ret = getAbstractOrderEntryModel(order, product, qty, number, addToPresent, usedUnit);

		if (ret == null)
		{
			ret = new OptimizedCartEntryData();
			ret.setQuantity(Long.valueOf(qty));
			ret.setProductCode(product.getCode());
			ret.setUnit(usedUnit.getCode());
			addEntryAtPosition(order, ret, number);
			ret.setCartData(order);
		}
		order.setCalculated(Boolean.FALSE);
		return ret;
	}
	
	protected void checkQuantity(final long qty, final int number)
	{
		if (qty <= 0)
		{
			throw new IllegalArgumentException("Quantity must be a positive non-zero value");
		}
		if (number < APPEND_AS_LAST)
		{
			throw new IllegalArgumentException("Number must be greater or equal -1");
		}
	}
	
	protected OptimizedCartEntryData getAbstractOrderEntryModel(final OptimizedCartData order, final ProductModel product, final long qty,
			final int number, final boolean addToPresent, final UnitModel usedUnit)
	{
		OptimizedCartEntryData ret = null;
		// search for present entries for this product if needed
		if (addToPresent)
		{
			OptimizedCartEntryData entryData = getEntriesForProduct(order, product.getCode());
			if(entryData != null)
			{
				/*
				 * Check if the entrymodel has Point of service and if "Yes" then compare the entry number as we might have
				 * multiple POS for same product and update should happen for right entry model with right POS. Else if the
				 * POS is null and since we always pass -1 from DefaultCommerceCartService, we pass -1 only for addnew for
				 * POS, which means it's a shipping mode entry.
				 *
				 * Ensure that order entry is not a 'give away', and has same units
				 */
				if ((isPOSNullAndAppendAsLast(entryData, number) || isPOSNotNullAndHasEqualEntryNumber(entryData, number))
						&& isNotGiveAwayAndHasEqualUnit(entryData, usedUnit))
				{
					entryData.setQuantity(Long.valueOf(entryData.getQuantity().longValue() + qty));
					ret = entryData;
				}
			}
		}
		return ret;
	}
	
	protected boolean isPOSNotNullAndHasEqualEntryNumber(final OptimizedCartEntryData cartEntry, final int number)
	{
		return cartEntry.getDeliveryPointOfService() != null && number == cartEntry.getEntryNumber().intValue();
	}

	protected boolean isPOSNullAndAppendAsLast(final OptimizedCartEntryData cartEntry, final int number)
	{
		return cartEntry.getDeliveryPointOfService() == null && number == APPEND_AS_LAST;
	}

	protected boolean isNotGiveAwayAndHasEqualUnit(final OptimizedCartEntryData cartEntry, final UnitModel usedUnit)
	{
		return !Boolean.TRUE.equals(cartEntry.getPromomtionGiftEntry()) && usedUnit.equals(cartEntry.getUnit());
	}
	
	
	protected int addEntryAtPosition(final OptimizedCartData order, final OptimizedCartEntryData entry, final int requested)
	{
		int ret = requested;
		final List<OptimizedCartEntryData> all = order.getEntries();
		/*
		 * just append ?
		 */
		final int lastIndex = all.isEmpty() ? 0 : all.size() - 1;
		final int lastIndexEntryNumberValue = all.isEmpty() ? 0 : (all.get(lastIndex)).getEntryNumber().intValue();

		if (requested < 0)
		{

			ret = all.isEmpty() ? 0 : lastIndexEntryNumberValue + 1;
		}
		/*
		 * need shuffling other entries
		 */
		else
		{
			boolean foundEntryWithNumber = false;
			for (int i = 0, s = all.size(); i < s; i++) //NOPMD
			{
				final OptimizedCartEntryData currentEntry = all.get(i);
				final int enr = currentEntry.getEntryNumber().intValue();
				// other entry got this number -> we need to shift numbers now
				if (foundEntryWithNumber)
				{
					currentEntry.setEntryNumber(Integer.valueOf(enr + 1));
				}
				// found it now?
				else if (enr == requested)
				{
					foundEntryWithNumber = true;
					currentEntry.setEntryNumber(Integer.valueOf(currentEntry.getEntryNumber().intValue() + 1));
				}
				// no other entry got this number -> dont need to shift other entry numbers
				else if (enr > requested)
				{
					break;
				}
			}
		}
		entry.setEntryNumber(Integer.valueOf(ret));

		final List<OptimizedCartEntryData> newEntries = new ArrayList<OptimizedCartEntryData>(all);
		newEntries.add(entry);
		Collections.sort(newEntries, new Comparator<OptimizedCartEntryData>()
		{
			@Override
			public int compare(final OptimizedCartEntryData order1, final OptimizedCartEntryData order2)
			{
				return order1.getEntryNumber().compareTo(order2.getEntryNumber());
			}
		});

		order.setEntries(newEntries);
		return ret;
	}

	/**
	 * @return the sessionService
	 */
	@Override
	public SessionService getSessionService()
	{
		return sessionService;
	}


	/**
	 * @param sessionService
	 *           the sessionService to set
	 */
	@Override
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	/**
	 * @return the productService
	 */
	public ProductService getProductService1()
	{
		return productService;
	}

	/**
	 * @param productService
	 *           the productService to set
	 */
	public void setProductService(final ProductService productService)
	{
		this.productService = productService;
	}

	/**
	 * @return the commerceAddToCartMethodHooks
	 */
	public List<CommerceAddToCartMethodHook> getCommerceAddToCartMethodHooks()
	{
		return commerceAddToCartMethodHooks;
	}

	/**
	 * @param commerceAddToCartMethodHooks
	 *           the commerceAddToCartMethodHooks to set
	 */
	public void setCommerceAddToCartMethodHooks(final List<CommerceAddToCartMethodHook> commerceAddToCartMethodHooks)
	{
		this.commerceAddToCartMethodHooks = commerceAddToCartMethodHooks;
	}

	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	/**
	 * @param configurationService
	 *           the configurationService to set
	 */
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	/**
	 * @return the baseSiteService
	 */
	public BaseSiteService getBaseSiteService1()
	{
		return baseSiteService;
	}


	/**
	 * @param baseSiteService
	 *           the baseSiteService to set
	 */
	public void setBaseSiteService(final BaseSiteService baseSiteService)
	{
		this.baseSiteService = baseSiteService;
	}


	/**
	 * @return the pointOfServiceService
	 */
	public PointOfServiceService getPointOfServiceService1()
	{
		return pointOfServiceService;
	}


	/**
	 * @param pointOfServiceService
	 *           the pointOfServiceService to set
	 */
	public void setPointOfServiceService(final PointOfServiceService pointOfServiceService)
	{
		this.pointOfServiceService = pointOfServiceService;
	}


	/**
	 * @return the commerceUpdateCartEntryHooks
	 */
	public List<CommerceUpdateCartEntryHook> getCommerceUpdateCartEntryHooks()
	{
		return commerceUpdateCartEntryHooks;
	}


	/**
	 * @param commerceUpdateCartEntryHooks
	 *           the commerceUpdateCartEntryHooks to set
	 */
	public void setCommerceUpdateCartEntryHooks(final List<CommerceUpdateCartEntryHook> commerceUpdateCartEntryHooks)
	{
		this.commerceUpdateCartEntryHooks = commerceUpdateCartEntryHooks;
	}


	/**
	 * @return the cartFactory
	 */
	public OptimizeCartFactory getCartFactory()
	{
		return cartFactory;
	}


	/**
	 * @param cartFactory
	 *           the cartFactory to set
	 */
	public void setCartFactory(final OptimizeCartFactory cartFactory)
	{
		this.cartFactory = cartFactory;
	}


	/**
	 * @return the defaultOptimizeCommerceCartService
	 */
	public DefaultOptimizeCommerceCartService getDefaultOptimizeCommerceCartService()
	{
		return defaultOptimizeCommerceCartService;
	}


	/**
	 * @param defaultOptimizeCommerceCartService
	 *           the defaultOptimizeCommerceCartService to set
	 */
	public void setDefaultOptimizeCommerceCartService(final DefaultOptimizeCommerceCartService defaultOptimizeCommerceCartService)
	{
		this.defaultOptimizeCommerceCartService = defaultOptimizeCommerceCartService;
	}

	/**
	 * @return the userService
	 */
	public UserService getUserService()
	{
		return userService;
	}


	/**
	 * @param userService
	 *           the userService to set
	 */
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}


	/**
	 * @return the productService
	 */
	public ProductService getProductService()
	{
		return productService;
	}


	/**
	 * @return the baseSiteService
	 */
	public BaseSiteService getBaseSiteService()
	{
		return baseSiteService;
	}


	/**
	 * @return the pointOfServiceService
	 */
	public PointOfServiceService getPointOfServiceService()
	{
		return pointOfServiceService;
	}


	/**
	 * @return the optimizeModelDealService
	 */
	public OptimizeModelDealService getOptimizeModelDealService()
	{
		return optimizeModelDealService;
	}


	/**
	 * @param optimizeModelDealService
	 *           the optimizeModelDealService to set
	 */
	public void setOptimizeModelDealService(final OptimizeModelDealService optimizeModelDealService)
	{
		this.optimizeModelDealService = optimizeModelDealService;
	}

	/**
	 * @return the commonI18NService
	 */
	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	/**
	 * @param commonI18NService
	 *           the commonI18NService to set
	 */
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

}
