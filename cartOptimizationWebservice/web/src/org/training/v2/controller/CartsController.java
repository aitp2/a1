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
package org.training.v2.controller;

import de.hybris.platform.basecommerce.enums.StockLevelStatus;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.order.SaveCartFacade;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.DeliveryModesData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.PromotionResultData;
import de.hybris.platform.commercefacades.product.data.StockData;
import de.hybris.platform.commercefacades.promotion.CommercePromotionRestrictionFacade;
import de.hybris.platform.commercefacades.storelocator.data.PointOfServiceData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.voucher.VoucherFacade;
import de.hybris.platform.commercefacades.voucher.exceptions.VoucherOperationException;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.order.CommerceCartMergingException;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.CommerceCartRestorationException;
import de.hybris.platform.commerceservices.promotion.CommercePromotionRestrictionException;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.PaginationData;
import de.hybris.platform.commercewebservicescommons.dto.order.CartListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.CartModificationWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.CartWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.DeliveryModeListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.DeliveryModeWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderEntryListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderEntryWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.product.PromotionResultListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.voucher.VoucherListWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.CartAddressException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.CartEntryException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.CartException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.LowStockException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.ProductLowStockException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.RequestParameterException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.StockSystemException;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.training.cart.impl.CommerceWebServicesCartFacade;
import org.training.exceptions.InvalidPaymentInfoException;
import org.training.exceptions.NoCheckoutCartException;
import org.training.exceptions.UnsupportedDeliveryModeException;
import org.training.exceptions.UnsupportedRequestException;
import org.training.order.data.CartDataList;
import org.training.order.data.OrderEntryDataList;
import org.training.product.data.PromotionResultDataList;
import org.training.request.support.impl.PaymentProviderRequestSupportedStrategy;
import org.training.stock.CommerceStockFacade;
import org.training.voucher.data.VoucherDataList;

import com.accenture.performance.optimization.facades.impl.DefaultOptimizeCartFacade;
import com.accenture.performance.optimization.facades.impl.DefaultOptimizeCheckoutFacade;


/**
 *
 * @pathparam userId User identifier or one of the literals below :
 *            <ul>
 *            <li>'current' for currently authenticated user</li>
 *            <li>'anonymous' for anonymous user</li>
 *            </ul>
 * @pathparam cartId Cart identifier
 *            <ul>
 *            <li>cart code for logged in user</li>
 *            <li>cart guid for anonymous user</li>
 *            <li>'current' for the last modified cart</li>
 *            </ul>
 * @pathparam entryNumber Entry number. Zero-based numbering.
 * @pathparam promotionId Promotion identifier (code)
 * @pathparam voucherId Voucher identifier (code)
 * @security Anonymous user may access cart by its guid. Customer may access only own cart by its id. Trusted client or
 *           customer manager may impersonate as any user and access cart on their behalf.
 */
@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/carts")
@CacheControl(directive = CacheControlDirective.NO_CACHE)
public class CartsController extends BaseCommerceController
{
	private static final Logger LOG = Logger.getLogger(BaseCommerceController.class);
	private static final long DEFAULT_PRODUCT_QUANTITY = 1;
	@Resource(name = "commercePromotionRestrictionFacade")
	private CommercePromotionRestrictionFacade commercePromotionRestrictionFacade;
	@Resource(name = "commerceStockFacade")
	private CommerceStockFacade commerceStockFacade;
	@Resource(name = "customerFacade")
	private CustomerFacade customerFacade;
	@Resource(name = "pointOfServiceValidator")
	private Validator pointOfServiceValidator;
	@Resource(name = "orderEntryCreateValidator")
	private Validator orderEntryCreateValidator;
	@Resource(name = "orderEntryUpdateValidator")
	private Validator orderEntryUpdateValidator;
	@Resource(name = "orderEntryReplaceValidator")
	private Validator orderEntryReplaceValidator;
	@Resource(name = "greaterThanZeroValidator")
	private Validator greaterThanZeroValidator;
	@Resource(name = "paymentProviderRequestSupportedStrategy")
	private PaymentProviderRequestSupportedStrategy paymentProviderRequestSupportedStrategy;
	@Resource(name = "saveCartFacade")
	private SaveCartFacade saveCartFacade;
	@Resource(name = "voucherFacade")
	private VoucherFacade voucherFacade;

	@Resource(name="defaultOptimizeCartFacade")
	private DefaultOptimizeCartFacade optimizedCartFacade;
	
	@Autowired
	private DefaultOptimizeCheckoutFacade optimizeCheckoutFacade;

	@Autowired



	protected static CartModificationData mergeCartModificationData(final CartModificationData cmd1,
			final CartModificationData cmd2)
	{
		if ((cmd1 == null) && (cmd2 == null))
		{
			return new CartModificationData();
		}
		if (cmd1 == null)
		{
			return cmd2;
		}
		if (cmd2 == null)
		{
			return cmd1;
		}
		final CartModificationData cmd = new CartModificationData();
		cmd.setDeliveryModeChanged(Boolean
				.valueOf(Boolean.TRUE.equals(cmd1.getDeliveryModeChanged()) || Boolean.TRUE.equals(cmd2.getDeliveryModeChanged())));
		cmd.setEntry(cmd2.getEntry());
		cmd.setQuantity(cmd2.getQuantity());
		cmd.setQuantityAdded(cmd1.getQuantityAdded() + cmd2.getQuantityAdded());
		cmd.setStatusCode(cmd2.getStatusCode());
		return cmd;
	}

	protected static OrderEntryData getCartEntryForNumber(final CartData cart, final long number) throws CartEntryException //NOSONAR
	{
		final List<OrderEntryData> entries = cart.getEntries();
		if (entries != null && !entries.isEmpty())
		{
			final Integer requestedEntryNumber = Integer.valueOf((int) number);
			for (final OrderEntryData entry : entries)
			{
				if (entry != null && requestedEntryNumber.equals(entry.getEntryNumber()))
				{
					return entry;
				}
			}
		}
		throw new CartEntryException("Entry not found", CartEntryException.NOT_FOUND, String.valueOf(number));
	}

	protected static OrderEntryData getCartEntry(final CartData cart, final String productCode, final String pickupStore)
	{
		for (final OrderEntryData oed : cart.getEntries())
		{
			if (oed.getProduct().getCode().equals(productCode))
			{
				if (pickupStore == null && oed.getDeliveryPointOfService() == null)
				{
					return oed;
				}
				else if (pickupStore != null && oed.getDeliveryPointOfService() != null
						&& pickupStore.equals(oed.getDeliveryPointOfService().getName()))
				{
					return oed;
				}
			}
		}
		return null;
	}

	protected static void validateForAmbiguousPositions(final CartData currentCart, final OrderEntryData currentEntry,
			final String newPickupStore) throws CommerceCartModificationException
	{
		final OrderEntryData entryToBeModified = getCartEntry(currentCart, currentEntry.getProduct().getCode(), newPickupStore);
		if (entryToBeModified != null && !entryToBeModified.equals(currentEntry))
		{
			throw new CartEntryException(
					"Ambiguous cart entries! Entry number " + currentEntry.getEntryNumber()
							+ " after change would be the same as entry " + entryToBeModified.getEntryNumber(),
					CartEntryException.AMBIGIOUS_ENTRY, entryToBeModified.getEntryNumber().toString());
		}
	}

	/**
	 * Lists all customer carts.
	 *
	 * @formparam savedCartsOnly optional parameter. If the parameter is provided and its value is true only saved carts
	 *            are returned.
	 * @formparam currentPage optional pagination parameter in case of savedCartsOnly == true. Default value 0.
	 * @formparam pageSize optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.
	 * @formparam sort optional sort criterion in case of savedCartsOnly == true. No default value.
	 * @queryparam fields Response configuration (list of fields, which should be returned in response).
	 * @return All customer carts
	 * @security Permitted only for non-anonymous users.
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public CartListWsDTO getCarts(@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields,
			@RequestParam(required = false, defaultValue = "false") final boolean savedCartsOnly,
			@RequestParam(required = false, defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
			@RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
			@RequestParam(required = false) final String sort)
	{
		if (getUserFacade().isAnonymousUser())
		{
			throw new AccessDeniedException("Access is denied");
		}

		final CartDataList cartDataList = new CartDataList();

		final PageableData pageableData = new PageableData();
		pageableData.setCurrentPage(currentPage);
		pageableData.setPageSize(pageSize);
		pageableData.setSort(sort);
		final List<CartData> allCarts = new ArrayList<>(
				saveCartFacade.getSavedCartsForCurrentUser(pageableData, null).getResults());
		if (!savedCartsOnly)
		{
			allCarts.addAll(getCartFacade().getCartsForCurrentUser());
		}
		cartDataList.setCarts(allCarts);

		return getDataMapper().map(cartDataList, CartListWsDTO.class, fields);
	}

	/**
	 * Returns the cart with a given identifier.
	 *
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @return Details of cart and it's entries
	 */
	@RequestMapping(value = "/{cartId}", method = RequestMethod.GET)
	@ResponseBody
	public CartWsDTO getCart(@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		// CartMatchingFilter sets current cart based on cartId, so we can return cart from the session
		return getDataMapper().map(getSessionCart(), CartWsDTO.class, fields);
	}

	/**
	 * ,s a new cart or restores an anonymous cart as a user's cart (if an old Cart Id is given in the request)
	 *
	 * @formparam oldCartId Anonymous cart GUID
	 * @formparam toMergeCartGuid User's cart GUID to merge anonymous cart to
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @return Created cart data
	 * @security Cart may be created by everybody. Restoring anonymous carts is permitted only for non anonymous users.
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public CartWsDTO createCart(@RequestParam(required = false) final String oldCartId,
			@RequestParam(required = false) final String toMergeCartGuid,
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("createCart");
		}

		String evaluatedToMergeCartGuid = toMergeCartGuid;

		if (StringUtils.isNotEmpty(oldCartId))
		{
			//LOG.info("========1============");
			if (StringUtils.isEmpty(evaluatedToMergeCartGuid))
			{
				evaluatedToMergeCartGuid = optimizedCartFacade.getSessionCartData().getGuid();
			}

			try
			{
				optimizeCheckoutFacade.validateCheckoutCartInfo(oldCartId, evaluatedToMergeCartGuid);
			}
			catch (final Exception ex)
			{
				LOG.info("Create cart failed. The error is ...... " + ex.getMessage());
			}


			try
			{
				optimizedCartFacade.restoreAnonymousCartAndMerge(oldCartId, evaluatedToMergeCartGuid);
				return getDataMapper().map(optimizedCartFacade.getSessionCartData(), CartWsDTO.class, fields);
				//return new CartWsDTO();
			}
			catch (final CommerceCartMergingException e)
			{
				throw new CartException("Couldn't merge carts", CartException.CANNOT_MERGE, e);
			}
			catch (final CommerceCartRestorationException e)
			{
				throw new CartException("Couldn't restore cart", CartException.CANNOT_RESTORE, e);
			}
		}
		else
		{
			if (StringUtils.isNotEmpty(evaluatedToMergeCartGuid))
			{


				try
				{
					optimizedCartFacade.restoreSavedCart(evaluatedToMergeCartGuid);
					//return new CartWsDTO();
					return getDataMapper().map(optimizedCartFacade.getSessionCartData(), CartWsDTO.class, fields);

				}
				catch (final CommerceCartRestorationException e)
				{
					throw new CartException("Couldn't restore cart", CartException.CANNOT_RESTORE, oldCartId, e);
				}

			}


			return getDataMapper().map(optimizedCartFacade.getSessionCartData(), CartWsDTO.class, fields);

		}
	}

	protected boolean isUserCart(final String toMergeCartGuid)
	{
		if (getCartFacade() instanceof CommerceWebServicesCartFacade)
		{
			final CommerceWebServicesCartFacade commerceWebServicesCartFacade = (CommerceWebServicesCartFacade) getCartFacade();
			return commerceWebServicesCartFacade.isCurrentUserCart(toMergeCartGuid);
		}
		return true;
	}

	protected boolean isCartAnonymous(final String cartGuid)
	{
		if (getCartFacade() instanceof CommerceWebServicesCartFacade)
		{
			final CommerceWebServicesCartFacade commerceWebServicesCartFacade = (CommerceWebServicesCartFacade) getCartFacade();
			return commerceWebServicesCartFacade.isAnonymousUserCart(cartGuid);
		}
		return true;
	}

	/**
	 * Deletes a cart with a given cart id.
	 */
	@RequestMapping(value = "/{cartId}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public void deleteCart()
	{
		getCartFacade().removeSessionCart();
	}

	/**
	 * Assigns an email to the cart. This step is required to make a guest checkout.
	 *
	 * @formparam email Email of the guest user. It will be used during checkout process
	 * @throws de.hybris.platform.commerceservices.customer.DuplicateUidException
	 * @security Permitted only for client or trusted client
	 */
	@Secured(
	{ "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/email", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	public void guestLogin(@RequestParam final String email) throws DuplicateUidException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("createGuestUserForAnonymousCheckout: email=" + sanitize(email));
		}

		if (!EmailValidator.getInstance().isValid(email))
		{
			throw new RequestParameterException("Email [" + sanitize(email) + "] is not a valid e-mail address!",
					RequestParameterException.INVALID, "login");
		}

		customerFacade.createGuestUserForAnonymousCheckout(email, "guest");
	}

	/**
	 * Returns cart entries.
	 *
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @return Cart entries list
	 */
	@RequestMapping(value = "/{cartId}/entries", method = RequestMethod.GET)
	@ResponseBody
	public OrderEntryListWsDTO getCartEntries(
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getCartEntries");
		}
		final OrderEntryDataList dataList = new OrderEntryDataList();
		dataList.setOrderEntries(getSessionCart().getEntries());
		return getDataMapper().map(dataList, OrderEntryListWsDTO.class, fields);
	}

	/**
	 * Adds a product to the cart.
	 *
	 * @formparam code Code of the product to be added to cart. Product look-up is performed for the current product
	 *            catalog version.
	 * @formparam qty Quantity of product.
	 * @formparam pickupStore Name of the store where product will be picked. Set only if want to pick up from a store.
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @return Information about cart modification.
	 * @throws CommerceCartModificationException
	 *            When there are some problems with cart modification
	 * @throws WebserviceValidationException
	 *            When store given in pickupStore parameter doesn't exist
	 * @throws ProductLowStockException
	 *            When product is out of stock in store (when pickupStore parameter is filled)
	 * @throws StockSystemException
	 *            When there is no information about stock for stores (when pickupStore parameter is filled).
	 */
	@RequestMapping(value = "/{cartId}/entries", method = RequestMethod.POST)
	@ResponseBody
	public CartModificationWsDTO addCartEntry(@PathVariable final String baseSiteId,
			@RequestParam(required = true) final String code, @RequestParam(required = false, defaultValue = "1") final long qty,
			@RequestParam(required = false) final String pickupStore,
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws CommerceCartModificationException, WebserviceValidationException, ProductLowStockException, StockSystemException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("addCartEntry: " + logParam("code", code) + ", " + logParam("qty", qty) + ", "
					+ logParam("pickupStore", pickupStore));
		}

		if (StringUtils.isNotEmpty(pickupStore))
		{
			validate(pickupStore, "pickupStore", pointOfServiceValidator);
		}

		return addCartEntryInternal(baseSiteId, code, qty, pickupStore, fields);
	}

	protected CartModificationWsDTO addCartEntryInternal(final String baseSiteId, final String code, final long qty,
			final String pickupStore, final String fields) throws CommerceCartModificationException
	{
		final CartModificationData cartModificationData;
		if (StringUtils.isNotEmpty(pickupStore))
		{
			validateIfProductIsInStockInPOS(baseSiteId, code, pickupStore, null);
			cartModificationData = getCartFacade().addToCart(code, qty, pickupStore);

		}
		else
		{
			//validateIfProductIsInStockOnline(baseSiteId, code, null);
			cartModificationData = optimizedCartFacade.addToCart(code, qty);
		}
		return getDataMapper().map(cartModificationData, CartModificationWsDTO.class, fields);
	}

	/**
	 * Adds a product to the cart.
	 *
	 * @param entry
	 *           Request body parameter (DTO in xml or json format) which contains details like : product code
	 *           (product.code), quantity of product (quantity), pickup store name (deliveryPointOfService.name)
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @bodyparams entry,quantity,deliveryPointOfService.name,product.code
	 * @return Information about cart modification.
	 * @throws CommerceCartModificationException
	 *            When there are some problems with cart modification
	 * @throws WebserviceValidationException
	 *            When there is no product code value When store given in pickupStore parameter doesn't exist
	 * @throws ProductLowStockException
	 *            When product is out of stock in store (when pickupStore parameter is filled)
	 * @throws StockSystemException
	 *            When there is no information about stock for stores (when pickupStore parameter is filled).
	 */
	@RequestMapping(value = "/{cartId}/entries", method = RequestMethod.POST, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	public CartModificationWsDTO addCartEntry(@PathVariable final String baseSiteId, @RequestBody final OrderEntryWsDTO entry,
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws CommerceCartModificationException, WebserviceValidationException, ProductLowStockException, StockSystemException //NOSONAR
	{
		if (entry.getQuantity() == null)
		{
			entry.setQuantity(Long.valueOf(DEFAULT_PRODUCT_QUANTITY));
		}
		validate(entry, "entry", orderEntryCreateValidator);

		final String pickupStore = entry.getDeliveryPointOfService() == null ? null : entry.getDeliveryPointOfService().getName();
		return addCartEntryInternal(baseSiteId, entry.getProduct().getCode(), entry.getQuantity().longValue(), pickupStore, fields);
	}

	/**
	 * Returns the details of the cart entries.
	 *
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @return Cart entry data
	 * @throws CartEntryException
	 *            When entry with given number doesn't exist
	 */
	@RequestMapping(value = "/{cartId}/entries/{entryNumber}", method = RequestMethod.GET)
	@ResponseBody
	public OrderEntryWsDTO getCartEntry(@PathVariable final long entryNumber,
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getCartEntry: " + logParam("entryNumber", entryNumber)); //NOSONAR
		}
		final OrderEntryData orderEntry = getCartEntryForNumber(getSessionCart(), entryNumber);
		return getDataMapper().map(orderEntry, OrderEntryWsDTO.class, fields);
	}

	/**
	 * Updates the quantity of a single cart entry and details of the store where the cart entry will be picked.
	 * Attributes not provided in request will be defined again (set to null or default)
	 *
	 * @formparam qty Quantity of product.
	 * @formparam pickupStore Name of the store where product will be picked. Set only if want to pick up from a store.
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @return Information about cart modification
	 * @throws CartEntryException
	 *            When entry with given number doesn't exist or in case of ambiguity of cart entries
	 * @throws WebserviceValidationException
	 *            When store given in pickupStore parameter doesn't exist
	 * @throws CommerceCartModificationException
	 *            When there are some problems with cart modification
	 * @throws ProductLowStockException
	 *            When product is out of stock in store (when pickupStore parameter is filled)
	 * @throws StockSystemException
	 *            When there is no information about stock for stores (when pickupStore parameter is filled).
	 *
	 */
	@RequestMapping(value = "/{cartId}/entries/{entryNumber}", method = RequestMethod.PUT)
	@ResponseBody
	public CartModificationWsDTO setCartEntry(@PathVariable final String baseSiteId, @PathVariable final long entryNumber,
			@RequestParam(required = true) final Long qty, @RequestParam(required = false) final String pickupStore,
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws CommerceCartModificationException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setCartEntry: " + logParam("entryNumber", entryNumber) + ", " + logParam("qty", qty) + ", "
					+ logParam("pickupStore", pickupStore));
		}
		final CartData cart = getSessionCart();
		final OrderEntryData orderEntry = getCartEntryForNumber(cart, entryNumber);
		if (!StringUtils.isEmpty(pickupStore))
		{
			validate(pickupStore, "pickupStore", pointOfServiceValidator);
		}

		return updateCartEntryInternal(baseSiteId, cart, orderEntry, qty, pickupStore, fields, true);
	}

	protected CartModificationWsDTO updateCartEntryInternal(final String baseSiteId, final CartData cart,
			final OrderEntryData orderEntry, final Long qty, final String pickupStore, final String fields, final boolean putMode)
			throws CommerceCartModificationException
	{
		final long entryNumber = orderEntry.getEntryNumber().longValue();
		final String productCode = orderEntry.getProduct().getCode();
		final PointOfServiceData currentPointOfService = orderEntry.getDeliveryPointOfService();

		CartModificationData cartModificationData1 = null;
		CartModificationData cartModificationData2 = null;

		if (!StringUtils.isEmpty(pickupStore))
		{
			if (currentPointOfService == null || !currentPointOfService.getName().equals(pickupStore))
			{
				//was 'shipping mode' or store is changed
				validateForAmbiguousPositions(cart, orderEntry, pickupStore);
				validateIfProductIsInStockInPOS(baseSiteId, productCode, pickupStore, Long.valueOf(entryNumber));
				cartModificationData1 = getCartFacade().updateCartEntry(entryNumber, pickupStore);
			}
		}
		else if (putMode && currentPointOfService != null)
		{
			//was 'pickup in store', now switch to 'shipping mode'
			validateForAmbiguousPositions(cart, orderEntry, pickupStore);
			validateIfProductIsInStockOnline(baseSiteId, productCode, Long.valueOf(entryNumber));
			cartModificationData1 = getCartFacade().updateCartEntry(entryNumber, pickupStore);
		}

		if (qty != null)
		{
			cartModificationData2 = getCartFacade().updateCartEntry(entryNumber, qty.longValue());
		}

		return getDataMapper().map(mergeCartModificationData(cartModificationData1, cartModificationData2),
				CartModificationWsDTO.class, fields);
	}

	/**
	 * Updates the quantity of a single cart entry and details of the store where the cart entry will be picked.
	 * Attributes not provided in request will be defined again (set to null or default)
	 *
	 * @param entry
	 *           Request body parameter (DTO in xml or json format) which contains details like : quantity of product
	 *           (quantity), pickup store name (deliveryPointOfService.name)
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @bodyparams entry,quantity,deliveryPointOfService.name,product.code
	 * @return Information about cart modification
	 * @throws CartEntryException
	 *            When entry with given number doesn't exist or in case of ambiguity of cart entries
	 * @throws WebserviceValidationException
	 *            When store given in pickupStore parameter doesn't exist
	 * @throws CommerceCartModificationException
	 *            When there are some problems with cart modification
	 * @throws ProductLowStockException
	 *            When product is out of stock in store (when pickupStore parameter is filled)
	 * @throws StockSystemException
	 *            When there is no information about stock for stores (when pickupStore parameter is filled).
	 */
	@RequestMapping(value = "/{cartId}/entries/{entryNumber}", method = RequestMethod.PUT, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	public CartModificationWsDTO setCartEntry(@PathVariable final String baseSiteId, @PathVariable final long entryNumber,
			@RequestBody final OrderEntryWsDTO entry,
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws CommerceCartModificationException
	{
		final CartData cart = getSessionCart();
		final OrderEntryData orderEntry = getCartEntryForNumber(cart, entryNumber);
		final String pickupStore = entry.getDeliveryPointOfService() == null ? null : entry.getDeliveryPointOfService().getName();

		validateCartEntryForReplace(orderEntry, entry);

		return updateCartEntryInternal(baseSiteId, cart, orderEntry, entry.getQuantity(), pickupStore, fields, true);
	}

	protected void validateCartEntryForReplace(final OrderEntryData oryginalEntry, final OrderEntryWsDTO entry)
	{
		final String productCode = oryginalEntry.getProduct().getCode();
		final Errors errors = new BeanPropertyBindingResult(entry, "entry");
		if (entry.getProduct() != null && entry.getProduct().getCode() != null && !entry.getProduct().getCode().equals(productCode))
		{
			errors.reject("cartEntry.productCodeNotMatch");
			throw new WebserviceValidationException(errors);
		}

		validate(entry, "entry", orderEntryReplaceValidator);
	}

	/**
	 * Updates the quantity of a single cart entry and details of the store where the cart entry will be picked.
	 *
	 * @formparam qty Quantity of product.
	 * @formparam pickupStore Name of the store where product will be picked. Set only if want to pick up from a store.
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @return Information about cart modification
	 * @throws CartEntryException
	 *            When entry with given number doesn't exist or in case of ambiguity of cart entries
	 * @throws WebserviceValidationException
	 *            When store given in pickupStore parameter doesn't exist
	 * @throws CommerceCartModificationException
	 *            When there are some problems with cart modification
	 * @throws ProductLowStockException
	 *            When product is out of stock in store (when pickupStore parameter is filled)
	 * @throws StockSystemException
	 *            When there is no information about stock for stores (when pickupStore parameter is filled).
	 */
	@RequestMapping(value = "/{cartId}/entries/{entryNumber}", method = RequestMethod.PATCH)
	@ResponseBody
	public CartModificationWsDTO updateCartEntry(@PathVariable final String baseSiteId, @PathVariable final long entryNumber,
			@RequestParam(required = false) final Long qty, @RequestParam(required = false) final String pickupStore,
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws CommerceCartModificationException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("updateCartEntry: " + logParam("entryNumber", entryNumber) + ", " + logParam("qty", qty) + ", "
					+ logParam("pickupStore", pickupStore));
		}

		final CartData cart = getSessionCart();
		final OrderEntryData orderEntry = getCartEntryForNumber(cart, entryNumber);

		if (qty == null && StringUtils.isEmpty(pickupStore))
		{
			throw new RequestParameterException("At least one parameter (qty,pickupStore) should be set!",
					RequestParameterException.MISSING);
		}

		if (qty != null)
		{
			validate(qty, "quantity", greaterThanZeroValidator);
		}

		if (pickupStore != null)
		{
			validate(pickupStore, "pickupStore", pointOfServiceValidator);
		}

		return updateCartEntryInternal(baseSiteId, cart, orderEntry, qty, pickupStore, fields, false);
	}

	/**
	 * Updates the quantity of a single cart entry and details of the store where the cart entry will be picked.
	 *
	 * @param entry
	 *           Request body parameter (DTO in xml or json format) which contains details like : quantity of product
	 *           (quantity), pickup store name (deliveryPointOfService.name)
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @bodyparams entry,quantity,deliveryPointOfService.name,product.code
	 * @return Information about cart modification
	 * @throws CartEntryException
	 *            When entry with given number doesn't exist or in case of ambiguity of cart entries
	 * @throws WebserviceValidationException
	 *            When store given in pickupStore parameter doesn't exist
	 * @throws CommerceCartModificationException
	 *            When there are some problems with cart modification
	 * @throws ProductLowStockException
	 *            When product is out of stock in store (when pickupStore parameter is filled)
	 * @throws StockSystemException
	 *            When there is no information about stock for stores (when pickupStore parameter is filled).
	 */
	@RequestMapping(value = "/{cartId}/entries/{entryNumber}", method = RequestMethod.PATCH, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	public CartModificationWsDTO updateCartEntry(@PathVariable final String baseSiteId, @PathVariable final long entryNumber,
			@RequestBody final OrderEntryWsDTO entry,
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws CommerceCartModificationException
	{
		final CartData cart = getSessionCart();
		final OrderEntryData orderEntry = getCartEntryForNumber(cart, entryNumber);

		final String productCode = orderEntry.getProduct().getCode();
		final Errors errors = new BeanPropertyBindingResult(entry, "entry");
		if (entry.getProduct() != null && entry.getProduct().getCode() != null && !entry.getProduct().getCode().equals(productCode))
		{
			errors.reject("cartEntry.productCodeNotMatch");
			throw new WebserviceValidationException(errors);
		}

		if (entry.getQuantity() == null)
		{
			entry.setQuantity(orderEntry.getQuantity());
		}

		validate(entry, "entry", orderEntryUpdateValidator);

		final String pickupStore = entry.getDeliveryPointOfService() == null ? null : entry.getDeliveryPointOfService().getName();
		return updateCartEntryInternal(baseSiteId, cart, orderEntry, entry.getQuantity(), pickupStore, fields, false);
	}

	/**
	 * Deletes cart entry.
	 *
	 * @throws CartEntryException
	 *            When entry with given number doesn't exist
	 * @throws CommerceCartModificationException
	 *            When there are some problems with cart modification
	 */
	@RequestMapping(value = "/{cartId}/entries/{entryNumber}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public void removeCartEntry(@PathVariable final long entryNumber) throws CommerceCartModificationException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("removeCartEntry: " + logParam("entryNumber", entryNumber));
		}

		final CartData cart = getSessionCart();
		getCartEntryForNumber(cart, entryNumber);
		getCartFacade().updateCartEntry(entryNumber, 0);
	}

	/**
	 * Creates an address and assigns it to the cart as the delivery address.
	 *
	 * @formparam firstName Customer's first name. This parameter is required.
	 * @formparam lastName Customer's last name. This parameter is required.
	 * @formparam titleCode Customer's title code. This parameter is required. For a list of codes, see
	 *            /{baseSiteId}/titles resource
	 * @formparam country.isocode Country isocode. This parameter is required and have influence on how rest of
	 *            parameters are validated (e.g. if parameters are required : line1,line2,town,postalCode,region.isocode)
	 * @formparam line1 First part of address. If this parameter is required depends on country (usually it is required).
	 * @formparam line2 Second part of address. If this parameter is required depends on country (usually it is not
	 *            required)
	 * @formparam town Town name. If this parameter is required depends on country (usually it is required)
	 * @formparam postalCode Postal code. If this parameter is required depends on country (usually it is required)
	 * @formparam region.isocode Isocode for region. If this parameter is required depends on country.
	 *
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @return Created address
	 * @throws WebserviceValidationException
	 *            When address parameters are incorrect
	 */
	@Secured(
	//{ "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/addresses/deliveries", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public AddressWsDTO createAndSetAddress(final HttpServletRequest request,
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WebserviceValidationException, NoCheckoutCartException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("createAddress");
		}

		final AddressData addressData = createAddressInternal(request);
		//final String addressId = addressData.getId();
		//super.setCartDeliveryAddressInternal(addressId);
		return getDataMapper().map(addressData, AddressWsDTO.class, fields);
	}

	/**
	 * Creates an address and assigns it to the cart as the delivery address.
	 *
	 * @param address
	 *           Request body parameter (DTO in xml or json format) which contains details like : Customer's first
	 *           name(firstName), Customer's last name(lastName), Customer's title code(titleCode),
	 *           country(country.isocode), first part of address(line1) , second part of address(line2), town (town),
	 *           postal code(postalCode), region (region.isocode)
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @bodyparams titleCode,firstName,lastName,line1,line2,town,postalCode,country(isocode),region(isocode),
	 *             defaultAddress
	 * @return Created address
	 * @throws WebserviceValidationException
	 *            When address parameters are incorrect
	 * @security Permitted only for guests, customer managers or trusted clients. Trusted client or customer manager may
	 *           impersonate as any user and access cart on their behalf.
	 */
	@Secured(
	//{ "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/addresses/deliveries", method = RequestMethod.POST, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public AddressWsDTO createAndSetAddress(@RequestBody final AddressWsDTO address,
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WebserviceValidationException, NoCheckoutCartException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("createAddress");
		}

		validate(address, "address", getAddressDTOValidator());
		AddressData addressData = getDataMapper().map(address, AddressData.class,
				"titleCode,firstName,lastName,line1,line2,town,postalCode,country(isocode),region(isocode),defaultAddress");
		addressData = createAddressInternal(addressData);

		return getDataMapper().map(addressData, AddressWsDTO.class, fields);
	}

	/**
	 * Sets a delivery address for the cart. The address country must be placed among the delivery countries of the
	 * current base store.
	 *
	 * @formparam addressId Address identifier
	 * @throws CartAddressException
	 *            When address with given id is not valid or doesn't exists
	 * @security Permitted only for customers, guests, customer managers or trusted clients. Trusted client or customer
	 *           manager may impersonate as any user and access cart on their behalf.
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/addresses/delivery", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	public void setCartDeliveryAddress(@RequestParam(required = true) final String addressId) throws NoCheckoutCartException
	{
		super.setCartDeliveryAddressInternal(addressId);
	}

	/**
	 * Removes the delivery address from the cart.
	 *
	 * @throws CartException
	 *            When removing delivery address failed
	 * @security Permitted only for customers, guests, customer managers or trusted clients. Trusted client or customer
	 *           manager may impersonate as any user and access cart on their behalf.
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/addresses/delivery", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public void removeCartDeliveryAddress()
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("removeDeliveryAddress");
		}
		if (!getCheckoutFacade().removeDeliveryAddress())
		{
			throw new CartException("Cannot reset address!", CartException.CANNOT_RESET_ADDRESS);
		}
	}
	
	protected AddressData createAddressInternal(final HttpServletRequest request) throws WebserviceValidationException //NOSONAR
	{
		final AddressData addressData = new AddressData();
		getHttpRequestAddressDataPopulator().populate(request, addressData);

		//TODO acn
		//validate(addressData, "addressData", getAddressValidator());

		return createAddressInternal(addressData);
	}

	/**
	 * Returns the delivery mode selected for the cart.
	 *
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @return Delivery mode selected for the cart
	 * @security Permitted only for customers, guests, customer managers or trusted clients. Trusted client or customer
	 *           manager may impersonate as any user and access cart on their behalf.
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/deliverymode", method = RequestMethod.GET)
	@ResponseBody
	public DeliveryModeWsDTO getCartDeliveryMode(
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getCartDeliveryMode");
		}
		return getDataMapper().map(getSessionCart().getDeliveryMode(), DeliveryModeWsDTO.class, fields);
	}

	/**
	 * Sets the delivery mode with a given identifier for the cart.
	 *
	 * @formparam deliveryModeId Delivery mode identifier (code)
	 * @throws UnsupportedDeliveryModeException
	 *            When the delivery mode does not exist or when the delivery address is not set for the cart
	 * @security Permitted only for customers, guests, customer managers or trusted clients. Trusted client or customer
	 *           manager may impersonate as any user and access cart on their behalf.
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/deliverymode", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	public void setCartDeliveryMode(@RequestParam(required = true) final String deliveryModeId)
			throws UnsupportedDeliveryModeException
	{
		super.setCartDeliveryModeInternal(deliveryModeId);
	}

	/**
	 * Removes the delivery mode from the cart.
	 *
	 * @security Permitted only for customers, guests, customer managers or trusted clients. Trusted client or customer
	 *           manager may impersonate as any user and access cart on their behalf.
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/deliverymode", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public void removeDeliveryMode()
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("removeDeliveryMode");
		}
		if (!getCheckoutFacade().removeDeliveryMode())
		{
			throw new CartException("Cannot reset delivery mode!", CartException.CANNOT_RESET_DELIVERYMODE);
		}
	}

	/**
	 * Returns all delivery modes supported for the current base store and cart delivery address. A delivery address must
	 * be set for the cart, otherwise an empty list will be returned.
	 *
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @return All supported delivery modes
	 * @security Permitted only for customers, guests, customer managers or trusted clients. Trusted client or customer
	 *           manager may impersonate as any user and access cart on their behalf.
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/deliverymodes", method = RequestMethod.GET)
	@ResponseBody
	public DeliveryModeListWsDTO getSupportedDeliveryModes(
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getSupportedDeliveryModes");
		}
		final DeliveryModesData deliveryModesData = new DeliveryModesData();
		deliveryModesData.setDeliveryModes(getCheckoutFacade().getSupportedDeliveryModes());

		return getDataMapper().map(deliveryModesData, DeliveryModeListWsDTO.class, fields);
	}

	/**
	 * Defines details of a new credit card payment details and assigns the payment to the cart.
	 *
	 * @formparam accountHolderName Name on card. This parameter is required.
	 * @formparam cardNumber Card number. This parameter is required.
	 * @formparam cardType Card type. This parameter is required. Call GET /{baseSiteId}/cardtypes beforehand to see what
	 *            card types are supported
	 * @formparam expiryMonth Month of expiry date. This parameter is required.
	 * @formparam expiryYear Year of expiry date. This parameter is required.
	 * @formparam issueNumber
	 * @formparam startMonth
	 * @formparam startYear
	 * @formparam subscriptionId
	 * @formparam saved Parameter defines if the payment details should be saved for the customer and than could be
	 *            reused for future orders.
	 * @formparam defaultPaymentInfo Parameter defines if the payment details should be used as default for customer.
	 * @formparam billingAddress.firstName Customer's first name. This parameter is required.
	 * @formparam billingAddress.lastName Customer's last name. This parameter is required.
	 * @formparam billingAddress.titleCode Customer's title code. This parameter is required. For a list of codes, see
	 *            /{baseSiteId}/titles resource
	 * @formparam billingAddress.country.isocode Country isocode. This parameter is required and have influence on how
	 *            rest of address parameters are validated (e.g. if parameters are required :
	 *            line1,line2,town,postalCode,region.isocode)
	 * @formparam billingAddress.line1 First part of address. If this parameter is required depends on country (usually
	 *            it is required).
	 * @formparam billingAddress.line2 Second part of address. If this parameter is required depends on country (usually
	 *            it is not required)
	 * @formparam billingAddress.town Town name. If this parameter is required depends on country (usually it is
	 *            required)
	 * @formparam billingAddress.postalCode Postal code. If this parameter is required depends on country (usually it is
	 *            required)
	 * @formparam billingAddress.region.isocode Isocode for region. If this parameter is required depends on country.
	 *
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @return Created payment details
	 * @throws WebserviceValidationException
	 * @throws InvalidPaymentInfoException
	 * @throws NoCheckoutCartException
	 * @throws UnsupportedRequestException
	 * @security Permitted only for customers, guests, customer managers or trusted clients. Trusted client or customer
	 *           manager may impersonate as any user and access cart on their behalf.
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/paymentdetails", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public PaymentDetailsWsDTO addPaymentDetails(final HttpServletRequest request, //NOSONAR
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WebserviceValidationException, InvalidPaymentInfoException, NoCheckoutCartException, UnsupportedRequestException //NOSONAR
	{
		//paymentProviderRequestSupportedStrategy.checkIfRequestSupported("addPaymentDetails");
		final CCPaymentInfoData paymentInfoData = super.addPaymentDetailsInternal(request).getPaymentInfo();
		return getDataMapper().map(paymentInfoData, PaymentDetailsWsDTO.class, fields);
	}

	/**
	 * Defines details of a new credit card payment details and assigns the payment to the cart.
	 *
	 * @param paymentDetails
	 *           Request body parameter (DTO in xml or json format) which contains details like : Name on card
	 *           (accountHolderName), card number(cardNumber), card type (cardType.code), Month of expiry date
	 *           (expiryMonth), Year of expiry date (expiryYear), if payment details should be saved (saved), if if the
	 *           payment details should be used as default (defaultPaymentInfo), billing address (
	 *           billingAddress.firstName,billingAddress.lastName, billingAddress.titleCode,
	 *           billingAddress.country.isocode, billingAddress.line1, billingAddress.line2, billingAddress.town,
	 *           billingAddress.postalCode, billingAddress.region.isocode)
	 *
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @bodyparams accountHolderName,cardNumber,cardType,cardTypeData(code),expiryMonth,expiryYear,issueNumber,
	 *             startMonth, startYear
	 *             ,subscriptionId,defaultPaymentInfo,saved,billingAddress(titleCode,firstName,lastName,line1,line2
	 *             ,town,postalCode,country(isocode),region(isocode),defaultAddress)
	 * @return Created payment details
	 * @throws WebserviceValidationException
	 * @throws InvalidPaymentInfoException
	 * @throws NoCheckoutCartException
	 * @throws UnsupportedRequestException
	 * @security Permitted only for customers, guests, customer managers or trusted clients. Trusted client or customer
	 *           manager may impersonate as any user and access cart on their behalf.
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/paymentdetails", method = RequestMethod.POST, consumes =
	{ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public PaymentDetailsWsDTO addPaymentDetails(@RequestBody final PaymentDetailsWsDTO paymentDetails, //NOSONAR
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
			throws WebserviceValidationException, InvalidPaymentInfoException, NoCheckoutCartException, UnsupportedRequestException //NOSONAR
	{
		paymentProviderRequestSupportedStrategy.checkIfRequestSupported("addPaymentDetails");
		validatePayment(paymentDetails);
		final String copiedfields = "accountHolderName,cardNumber,cardType,cardTypeData(code),expiryMonth,expiryYear,issueNumber,startMonth,startYear,subscriptionId,defaultPaymentInfo,saved,"
				+ "billingAddress(titleCode,firstName,lastName,line1,line2,town,postalCode,country(isocode),region(isocode),defaultAddress)";
		CCPaymentInfoData paymentInfoData = getDataMapper().map(paymentDetails, CCPaymentInfoData.class, copiedfields);
		paymentInfoData = addPaymentDetailsInternal(paymentInfoData).getPaymentInfo();
		return getDataMapper().map(paymentInfoData, PaymentDetailsWsDTO.class, fields);
	}

	protected void validatePayment(final PaymentDetailsWsDTO paymentDetails) throws NoCheckoutCartException
	{
		if (!getCheckoutFacade().hasCheckoutCart())
		{
			throw new NoCheckoutCartException("Cannot add PaymentInfo. There was no checkout cart created yet!");
		}
		validate(paymentDetails, "paymentDetails", getPaymentDetailsDTOValidator());
	}

	/**
	 * Sets credit card payment details for the cart.
	 *
	 * @formparam paymentDetailsId Payment details identifier
	 * @throws InvalidPaymentInfoException
	 *            When payment details with given id doesn't exists or belong to another user
	 * @security Permitted only for customers, guests, customer managers or trusted clients. Trusted client or customer
	 *           manager may impersonate as any user and access cart on their behalf.
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/paymentdetails", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	public void setPaymentDetails(@RequestParam(required = true) final String paymentDetailsId) throws InvalidPaymentInfoException
	{
		super.setPaymentDetailsInternal(paymentDetailsId);
	}

	/**
	 * Return information about promotions applied on cart. Requests pertaining to promotions have been developed for the
	 * previous version of promotions and vouchers and therefore some of them are currently not compatible with the new
	 * promotion engine.
	 *
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @return Information about promotions applied on cart
	 * @security Permitted only for non anonymous users or clients. Trusted client or customer manager may impersonate as
	 *           any user and access cart on their behalf.
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/promotions", method = RequestMethod.GET)
	@ResponseBody
	public PromotionResultListWsDTO getPromotions(
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getPromotions");
		}
		final List<PromotionResultData> appliedPromotions = new ArrayList<>();
		final List<PromotionResultData> orderPromotions = getSessionCart().getAppliedOrderPromotions();
		final List<PromotionResultData> productPromotions = getSessionCart().getAppliedProductPromotions();
		appliedPromotions.addAll(orderPromotions);
		appliedPromotions.addAll(productPromotions);

		final PromotionResultDataList dataList = new PromotionResultDataList();
		dataList.setPromotions(appliedPromotions);
		return getDataMapper().map(dataList, PromotionResultListWsDTO.class, fields);
	}

	/**
	 * Return information about promotion with given id, applied on cart. Requests pertaining to promotions have been
	 * developed for the previous version of promotions and vouchers and therefore some of them are currently not
	 * compatible with the new promotion engine.
	 *
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @return Information about promotion with given id, applied on cart
	 * @security Permitted only for non anonymous users or clients. Trusted client or customer manager may impersonate as
	 *           any user and access cart on their behalf.
	 */
	@Secured(
	{ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/promotions/{promotionId}", method = RequestMethod.GET)
	@ResponseBody
	public PromotionResultListWsDTO getPromotion(@PathVariable final String promotionId,
			@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getPromotion: promotionId = " + sanitize(promotionId));
		}
		final List<PromotionResultData> appliedPromotions = new ArrayList<PromotionResultData>();
		final List<PromotionResultData> orderPromotions = getSessionCart().getAppliedOrderPromotions();
		final List<PromotionResultData> productPromotions = getSessionCart().getAppliedProductPromotions();
		for (final PromotionResultData prd : orderPromotions)
		{
			if (prd.getPromotionData().getCode().equals(promotionId))
			{
				appliedPromotions.add(prd);
			}
		}
		for (final PromotionResultData prd : productPromotions)
		{
			if (prd.getPromotionData().getCode().equals(promotionId))
			{
				appliedPromotions.add(prd);
			}
		}

		final PromotionResultDataList dataList = new PromotionResultDataList();
		dataList.setPromotions(appliedPromotions);
		return getDataMapper().map(dataList, PromotionResultListWsDTO.class, fields);
	}

	/**
	 * Enables the promotion for the order based on the promotionId defined for the cart. Requests pertaining to
	 * promotions have been developed for the previous version of promotions and vouchers and therefore some of them are
	 * currently not compatible with the new promotion engine.
	 *
	 * @formparam promotionId Promotion identifier
	 * @throws CommercePromotionRestrictionException
	 *            When there is no PromotionOrderRestriction for the promotion
	 */
	@Secured(
	{ "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/promotions", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void applyPromotion(@RequestParam(required = true) final String promotionId)
			throws CommercePromotionRestrictionException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("applyPromotion: promotionId = " + sanitize(promotionId));
		}
		commercePromotionRestrictionFacade.enablePromotionForCurrentCart(promotionId);
	}

	/**
	 * Disables the promotion for the order based on the promotionId defined for the cart. Requests pertaining to
	 * promotions have been developed for the previous version of promotions and vouchers and therefore some of them are
	 * currently not compatible with the new promotion engine.
	 *
	 * @throws CommercePromotionRestrictionException
	 *            When there is no PromotionOrderRestriction for the promotion
	 * @security Permitted only for trusted clients
	 */
	@Secured(
	{ "ROLE_TRUSTED_CLIENT" })
	@RequestMapping(value = "/{cartId}/promotions/{promotionId}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public void removePromotion(@PathVariable final String promotionId) //NOSONAR
			throws CommercePromotionRestrictionException, NoCheckoutCartException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("removePromotion: promotionId = " + sanitize(promotionId));
		}
		commercePromotionRestrictionFacade.disablePromotionForCurrentCart(promotionId);
	}

	/**
	 * Returns list of vouchers applied to the cart.
	 *
	 * @queryparam fields Response configuration (list of fields, which should be returned in response)
	 * @return List of vouchers applied to the cart.
	 * @security Permitted only for non anonymous users or clients. Trusted client or customer manager may impersonate as
	 *           any user and access cart on their behalf.
	 */
	@Secured(
	{ "ROLE_CLIENT", "ROLE_CUSTOMERGROUP", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_GUEST" })
	@RequestMapping(value = "/{cartId}/vouchers", method = RequestMethod.GET)
	@ResponseBody
	public VoucherListWsDTO getVouchers(@RequestParam(required = false, defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getVouchers");
		}
		final VoucherDataList dataList = new VoucherDataList();
		dataList.setVouchers(voucherFacade.getVouchersForCart());
		return getDataMapper().map(dataList, VoucherListWsDTO.class, fields);
	}

	/**
	 * Applies a voucher based on the voucherId defined for the cart.
	 *
	 * @formparam voucherId Voucher identifier
	 * @throws VoucherOperationException
	 *            When trying to apply a non-existent voucher or other error occurs during the voucher-application
	 *            process.
	 * @security Permitted only for non anonymous users or clients. Trusted client or customer manager may impersonate as
	 *           any user and access cart on their behalf.
	 */
	@Secured(
	{ "ROLE_CLIENT", "ROLE_CUSTOMERGROUP", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_GUEST" })
	@RequestMapping(value = "/{cartId}/vouchers", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void applyVoucherForCart(@RequestParam(required = true) final String voucherId) //NOSONAR
			throws NoCheckoutCartException, VoucherOperationException //NOSONAR
	{
		super.applyVoucherForCartInternal(voucherId);
	}

	/**
	 * Removes a voucher based on the voucherId defined for the current cart.
	 *
	 * @throws VoucherOperationException
	 *            When an error occurs during the release voucher process.
	 * @security Permitted only for non anonymous users or clients. Trusted client or customer manager may impersonate as
	 *           any user and access cart on their behalf.
	 */
	@Secured(
	{ "ROLE_CLIENT", "ROLE_CUSTOMERGROUP", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_GUEST" })
	@RequestMapping(value = "/{cartId}/vouchers/{voucherId}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public void releaseVoucherFromCart(@PathVariable final String voucherId) //NOSONAR
			throws NoCheckoutCartException, VoucherOperationException //NOSONAR
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("release voucher : voucherCode = " + sanitize(voucherId));
		}
		if (!getCheckoutFacade().hasCheckoutCart())
		{
			throw new NoCheckoutCartException("Cannot realese voucher. There was no checkout cart created yet!");
		}
		getVoucherFacade().releaseVoucher(voucherId);
	}

	protected void validateIfProductIsInStockInPOS(final String baseSiteId, final String productCode, final String storeName,
			final Long entryNumber)
	{
		if (!commerceStockFacade.isStockSystemEnabled(baseSiteId))
		{
			throw new StockSystemException("Stock system is not enabled on this site", StockSystemException.NOT_ENABLED, baseSiteId);
		}
		final StockData stock = commerceStockFacade.getStockDataForProductAndPointOfService(productCode, storeName);
		if (stock != null && stock.getStockLevelStatus().equals(StockLevelStatus.OUTOFSTOCK))
		{
			if (entryNumber != null)
			{
				throw new LowStockException("Product [" + sanitize(productCode) + "] is currently out of stock", //NOSONAR
						LowStockException.NO_STOCK, String.valueOf(entryNumber));
			}
			else
			{
				throw new ProductLowStockException("Product [" + sanitize(productCode) + "] is currently out of stock",
						LowStockException.NO_STOCK, productCode);
			}
		}
		else if (stock != null && stock.getStockLevelStatus().equals(StockLevelStatus.LOWSTOCK))
		{
			if (entryNumber != null)
			{
				throw new LowStockException("Not enough product in stock", LowStockException.LOW_STOCK, String.valueOf(entryNumber));
			}
			else
			{
				throw new ProductLowStockException("Not enough product in stock", LowStockException.LOW_STOCK, productCode);
			}
		}
	}

	protected void validateIfProductIsInStockOnline(final String baseSiteId, final String productCode, final Long entryNumber)
	{
		if (!commerceStockFacade.isStockSystemEnabled(baseSiteId))
		{
			throw new StockSystemException("Stock system is not enabled on this site", StockSystemException.NOT_ENABLED, baseSiteId);
		}
		final StockData stock = commerceStockFacade.getStockDataForProductAndBaseSite(productCode, baseSiteId);
		if (stock != null && stock.getStockLevelStatus().equals(StockLevelStatus.OUTOFSTOCK))
		{
			if (entryNumber != null)
			{
				throw new LowStockException("Product [" + sanitize(productCode) + "] cannot be shipped - out of stock online",
						LowStockException.NO_STOCK, String.valueOf(entryNumber));
			}
			else
			{
				throw new ProductLowStockException("Product [" + sanitize(productCode) + "] cannot be shipped - out of stock online",
						LowStockException.NO_STOCK, productCode);
			}
		}
	}



}
