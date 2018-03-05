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
package com.accenture.performance.optimization.facades.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.accenture.performance.optimization.facades.OptimizeCheckoutFacade;
import com.accenture.performance.optimization.facades.OptimizedCartFacade;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;
import com.accenture.performance.optimization.model.OptimizedCartModel;
import com.accenture.performance.optimization.service.OptimizeCartService;
import com.accenture.performance.optimization.service.OptimizeCommerceCartService;
import com.accenture.performance.optimization.service.OptimizeCommerceCheckoutService;
import com.accenture.performance.optimization.service.OptimizeDeliveyService;
import com.accenture.performance.optimization.service.OptimizeModelDealService;

import de.hybris.platform.acceleratorfacades.order.impl.DefaultAcceleratorCheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.order.data.ZoneDeliveryModeData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.enums.SalesApplication;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.deliveryzone.jalo.ZoneDeliveryMode;
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeModel;
import de.hybris.platform.jalo.order.delivery.DeliveryMode;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.payment.dto.BillingInfo;
import de.hybris.platform.payment.dto.CardInfo;
import de.hybris.platform.payment.dto.CardType;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.util.PriceValue;


/**
 *
 */
public class DefaultOptimizeCheckoutFacade extends DefaultAcceleratorCheckoutFacade implements OptimizeCheckoutFacade
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultOptimizeCheckoutFacade.class);
	private SessionService sessionService;

	@Autowired
	private OptimizeCommerceCheckoutService optimizeCommerceCheckoutService;
	@Autowired
	private OptimizeModelDealService optimizeModelDealService;

	@Autowired
	private OptimizeCommerceCartService optimizeCommerceCartService;

	@Autowired
	private OptimizeCartService optimizeCartService;

	@Autowired
	private Converter<AddressModel, AddressData> addressConverter;
	@Autowired
	private Converter<OptimizedCartData, OptimizedCartModel> cartReverseConverter;

	@Autowired
	private Converter<OptimizedCartData, CartData> optimizeCartConverter;

	@Autowired
	private BaseSiteService baseSiteService;
	
	private OptimizeDeliveyService optimizeDeliveryService;

	protected OptimizedCartData getOptimizedCart()
	{
		return hasCheckoutCart() ? optimizeCartService.getSessionOptimizedCart() : null;
	}
	
	//TODO acn
	@Override
	public void prepareCartForCheckout()
	{
		//
	}
	
	@Override
	public List<? extends DeliveryModeData> getSupportedDeliveryModes() 
	{
		final List<DeliveryModeData> result = new ArrayList<DeliveryModeData>();
		
		final OptimizedCartData optimizedCartData = optimizeCartService.getSessionOptimizedCart();
		OptimizedCartModel optimizedCartModel = cartReverseConverter.convert(optimizedCartData);
		
		if (optimizedCartModel != null)
		{
			AbstractOrderModel abstractOrder = new AbstractOrderModel();
			abstractOrder.setEntries(createOrderEntriesFromOptimizedCart(optimizedCartData));
			abstractOrder.setCurrency( getCommonI18NService().getCurrency(optimizedCartModel.getCurrencyCode()));
			abstractOrder.setStore(optimizedCartModel.getStore());
			
			AddressModel address = optimizedCartModel.getDeliveryAddress();
			abstractOrder.setDeliveryAddress(address);
			abstractOrder.setNet(optimizedCartModel.getNet());
			
			for (final DeliveryModeModel deliveryModeModel : getDeliveryService().getSupportedDeliveryModeListForOrder(abstractOrder))
			{
				result.add(convert(deliveryModeModel));
			}
		}
		return result;
	}
	
	protected List<AbstractOrderEntryModel> createOrderEntriesFromOptimizedCart(final OptimizedCartData optimizedCartData)
	{
		List<AbstractOrderEntryModel> orderEntryList = new ArrayList<>();
		if(optimizedCartData.getEntries() != null)
		{
			for(OptimizedCartEntryData entry:optimizedCartData.getEntries())
			{
				AbstractOrderEntryModel orderEntryModel = new AbstractOrderEntryModel();
				if( entry.getDeliveryPointOfService() != null)
				{
					orderEntryModel.setDeliveryPointOfService(new PointOfServiceModel());
				}
				orderEntryList.add(orderEntryModel);
				
			}
		}
		return orderEntryList;
	}
	
	@Override
	public boolean hasPickUpItems()
	{
		return hasItemsMatchingPredicateACN(e -> e.getDeliveryPointOfService() != null);
	}

	@Override
	public boolean hasShippingItems()
	{
		return hasItemsMatchingPredicateACN(e -> e.getDeliveryPointOfService() == null);
	}

	protected boolean hasItemsMatchingPredicateACN(final Predicate<OrderEntryData> predicate)
	{
		final CartData cart = this.optimizeCartConverter.convert(this.optimizeCartService.getSessionOptimizedCart());
		if (cart != null && CollectionUtils.isNotEmpty(cart.getEntries()))
		{
			for (final OrderEntryData entry : cart.getEntries())
			{
				if (predicate.test(entry))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public CartData getCheckoutCart()
	{
		final OptimizedCartData cartData = optimizeCartService.getSessionOptimizedCart();
		if (cartData != null)
		{
			return this.optimizeCartConverter.convert(cartData);
		}

		throw new NullPointerException("Cart can not be null");
	}
	
	// TODO acn
	@Override
	public boolean setDeliveryAddressIfAvailable() {
		return false;
	}
		
	@Override
	public List<AddressData> getSupportedDeliveryAddresses(final boolean visibleAddressesOnly) {
		final OptimizedCartData pptimizedCartData = getOptimizedCart();
		if(null == pptimizedCartData)
		{
			return Collections.emptyList();
		}
		else
		{
			CartModel cartModel = new CartModel();
			cartModel.setUser(getUserService().getUserForUID(pptimizedCartData.getUserId()));
			return getAddressConverter().convertAll(getDeliveryService().getSupportedDeliveryAddressesForOrder(cartModel, visibleAddressesOnly));
		}
		
	}
		
	// TODO acn
	@Override
	public boolean setDeliveryModeIfAvailable() {
		return false;
	}
		
	@Override
	protected DeliveryModeData convert(final DeliveryModeModel deliveryModeModel) {
		if (deliveryModeModel instanceof ZoneDeliveryModeModel) {
			final ZoneDeliveryModeModel zoneDeliveryModeModel = (ZoneDeliveryModeModel) deliveryModeModel;
			final OptimizedCartData cart = optimizeCartService.getSessionOptimizedCart();
			if (cart != null) {
				final ZoneDeliveryModeData zoneDeliveryModeData = getZoneDeliveryModeConverter().convert(zoneDeliveryModeModel);
				final PriceValue deliveryCost = getDeliveryCostForDeliveryModeAndAbstractOrder(deliveryModeModel,cart);
				if (deliveryCost != null) {
					zoneDeliveryModeData.setDeliveryCost(getPriceDataFactory().create(PriceDataType.BUY,
							BigDecimal.valueOf(deliveryCost.getValue()), deliveryCost.getCurrencyIso()));
				}

				return zoneDeliveryModeData;
			}
			return null;
		}
		return getDeliveryModeConverter().convert(deliveryModeModel);
	}
	
	public PriceValue getDeliveryCostForDeliveryModeAndAbstractOrder(final DeliveryModeModel deliveryMode, final OptimizedCartData cart)
	{
		validateParameterNotNull(deliveryMode, "deliveryMode model cannot be null");
		validateParameterNotNull(cart, "abstractOrder model cannot be null");

		final DeliveryMode deliveryModeSource = getModelService().getSource(deliveryMode);
		if(! (deliveryModeSource instanceof ZoneDeliveryMode) )
		{
			return new PriceValue(cart.getCurrencyCode(), 0.0, cart.isNet());
		}
		
		AbstractOrderModel abstractOrder = new CartModel();
		
		AddressModel deliverAddress = getDeliveryAddressModelForCode(cart.getDeliveryAddress().getId());
		abstractOrder.setDeliveryAddress(deliverAddress);

		CurrencyModel currency = getCommonI18NService().getCurrency(cart.getCurrencyCode());
		abstractOrder.setCurrency(currency);
		
		abstractOrder.setNet(Boolean.valueOf(cart.isNet()));
		abstractOrder.setSubtotal(cart.getSubtotal());
		
		return optimizeDeliveryService.getOptimizeDeliveryCostForDeliveryModeAndAbstractOrder(deliveryMode, abstractOrder);
	}

	//TODO acn
	@Override
	public boolean authorizePayment(final String securityCode)
	{
		return true;
//		final CartModel cartModel = getCart();
//		final CreditCardPaymentInfoModel creditCardPaymentInfoModel = cartModel == null ? null
//				: (CreditCardPaymentInfoModel) cartModel.getPaymentInfo();
//		if (checkIfCurrentUserIsTheCartUser() && creditCardPaymentInfoModel != null
//				&& StringUtils.isNotBlank(creditCardPaymentInfoModel.getSubscriptionId()))
//		{
//			final CommerceCheckoutParameter parameter = createCommerceCheckoutParameter(cartModel, true);
//			parameter.setSecurityCode(securityCode);
//			parameter.setPaymentProvider(getPaymentProvider());
//			final PaymentTransactionEntryModel paymentTransactionEntryModel = getCommerceCheckoutService()
//					.authorizePayment(parameter);
//
//			return paymentTransactionEntryModel != null
//					&& (TransactionStatus.ACCEPTED.name().equals(paymentTransactionEntryModel.getTransactionStatus())
//							|| TransactionStatus.REVIEW.name().equals(paymentTransactionEntryModel.getTransactionStatus()));
//		}
//		return false;
	}
	@Override
	public boolean hasValidCart() {
		final OptimizedCartData optimizeCartData = getOptimizedCart();
		if (optimizeCartData == null) {
			return false;
		} else {
			return optimizeCartData.getEntries() != null && !optimizeCartData.getEntries().isEmpty();
		}
	}
	
	@Override
	public OrderData placeOrder() throws InvalidCartException
	{
		final OptimizedCartData cartData = optimizeCartService.getSessionOptimizedCart();
		if (cartData != null)
		{
			beforePlaceOrder(cartData);
			final CommerceCheckoutParameter parameter = new CommerceCheckoutParameter();
			parameter.setEnableHooks(true);
			parameter.setOptimizeCart(cartData);
			parameter.setSalesApplication(SalesApplication.WEB);
			final OrderModel orderModel = optimizeCommerceCheckoutService.placeOrder(parameter).getOrder();
			afterPlaceOrder(orderModel, cartData);
			if (orderModel != null)
			{
				LOG.info("The Order Code is ......" + orderModel.getCode());
				return getOrderConverter().convert(orderModel);
			}
		}
		return null;
	}

	@Override
	public OrderData placeOmsOrder(final String cartGuid) throws InvalidCartException
	{
		// TODO : set current user and get cartdata from redis

		final OptimizedCartData cartData = optimizeCartService.getSessionOptimizedCart();

		if (cartData != null)
		{
			beforePlaceOrder(cartData);
			final CommerceCheckoutParameter parameter = new CommerceCheckoutParameter();
			parameter.setEnableHooks(true);
			parameter.setOptimizeCart(cartData);
			parameter.setSalesApplication(SalesApplication.WEB);
			final OrderModel orderModel = optimizeCommerceCheckoutService.placeOrder(parameter).getOrder();
			afterPlaceOrder(orderModel, cartData);
			if (orderModel != null)
			{
				LOG.info("The Order Code is ......" + orderModel.getCode());
				return getOrderConverter().convert(orderModel);
			}
		}
		return null;
	}

	/**
	 * Clear CartData in the session and model in database
	 */
	protected void beforePlaceOrder(final OptimizedCartData cartData) //NOSONAR
	{
		if (cartData != null)
		{
			LOG.info("Start place order.");
		}
	}

	/**
	 * Clear CartData in the session and model in database
	 */
	protected void afterPlaceOrder(final OrderModel orderModel, final OptimizedCartData cartData) //NOSONAR
	{
		if (orderModel != null)
		{
			optimizeModelDealService.removeCurrentSessionCart(cartData);
			
		}
	}

	/**
	 * set Delivery mode to CartData and CartModel
	 */
	@Override
	public boolean setDeliveryMode(final String deliveryModeCode)
	{
		validateParameterNotNullStandardMessage("deliveryModeCode", deliveryModeCode);

		final OptimizedCartData cartData = optimizeCartService.getSessionOptimizedCart();
		//will add isSupportedDeliveryMode(deliveryModeCode, cartModel) check in futhure
		if (cartData != null)
		{
			final DeliveryModeModel deliveryModeModel = getDeliveryService().getDeliveryModeForCode(deliveryModeCode);
			if (deliveryModeModel != null)
			{
				cartData.setDeliveryMode(deliveryModeModel.getCode());
				cartData.setCalculated(Boolean.FALSE);
				optimizeCartService.setSessionOptimizedCart(cartData);
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 */
	@Override
	public boolean setDeliveryAddress(final AddressData addressData)
	{

		final OptimizedCartData cartData = getOptimizedCartFacade().getSessionCartData();
		if (cartData != null)
		{
			AddressModel addressModel = null;

			if (addressData != null)
			{
				if (addressData.getId() == null)
				{
					addressModel = getModelService().create(AddressModel.class);
					getAddressReversePopulator().populate(addressData, addressModel);
					//addressModel.setOwner(cartModel);
				}
				else
				{
					addressModel = getDeliveryAddressModelForCode(addressData.getId());
					if (addressModel == null)
					{
						addressModel = getModelService().create(AddressModel.class);
						getAddressReversePopulator().populate(addressData, addressModel);
					}
				}

			}
			if (addressModel == null)
			{
				addressModel = getModelService().create(AddressModel.class);
				getAddressReversePopulator().populate(addressData, addressModel);
			}

			final CommerceCheckoutParameter parameter = new CommerceCheckoutParameter();
			parameter.setAddress(addressModel);
			parameter.setIsDeliveryAddress(false);
			parameter.setOptimizeCart(cartData);
			storeDeliveryAddress(parameter);
			return true;
		}
		return false;
	}

	/**
	 * Save Delivery Address to cartData and cartModel
	 */
	public boolean storeDeliveryAddress(final CommerceCheckoutParameter parameter)
	{
		final OptimizedCartData cartData = parameter.getOptimizeCart();
		final AddressModel addressModel = parameter.getAddress();
		//final boolean flagAsDeliveryAddress = parameter.isIsDeliveryAddress();
		final AddressData addressData = addressConverter.convert(addressModel);
		if (this.getOptimizeCartService().isValidDeliveryAddress(cartData, addressModel))
		{
			cartData.setDeliveryAddress(addressData);
			optimizeCartService.setSessionOptimizedCart(cartData);
			return true;
		}
		return false;
	}

	@Override
	public boolean setPaymentDetails(final String paymentInfoId)
	{
		validateParameterNotNullStandardMessage("paymentInfoId", paymentInfoId);

		if (checkIfCurrentUserIsTheCartUser() && StringUtils.isNotBlank(paymentInfoId))
		{
			final CustomerModel currentUserForCheckout = getCurrentUserForCheckout();
			final CreditCardPaymentInfoModel ccPaymentInfoModel = getCustomerAccountService()
					.getCreditCardPaymentInfoForCode(currentUserForCheckout, paymentInfoId);
			final OptimizedCartData cartData = optimizeCartService.getSessionOptimizedCart();
			if (ccPaymentInfoModel != null)
			{
				final CommerceCheckoutParameter parameter = new CommerceCheckoutParameter();
				parameter.setOptimizeCart(cartData);
				parameter.setEnableHooks(true);
				parameter.setPaymentInfo(ccPaymentInfoModel);
				return getCommerceCheckoutService().setPaymentInfo(parameter);
			}
			LOG.warn(String.format(
					"Did not find CreditCardPaymentInfoModel for user: %s, cart: %s &  paymentInfoId: %s. PaymentInfo Will not get set.",
					currentUserForCheckout, cartData, paymentInfoId));
		}
		return false;
	}

	@Override
	public OptimizedCartData loadCart(final String cartId)
	{
		final OptimizedCartData cartdata = optimizeCartService.getSessionOptimizedCart();
		if (cartId.equals(cartdata.getGuid()))
		{
			if (cartdata.getEntries() != null)
			{
				try
				{
					optimizeCommerceCartService.calculateCart(cartdata, true);
				}
				catch (final CalculationException ex)
				{
					LOG.info("Calculate cart error......" + ex.getMessage());
				}
			}
		}
		return cartdata;

	}


	public void validateCheckoutCartInfo(final String oldCartId, String evaluatedToMergeCartGuid) throws Exception
	{
		if (getUserService().isAnonymousUser(getUserService().getCurrentUser()))
		{
			throw new Exception("Anonymous user is not allowed to copy cart!");
		}

		if (!isCartAnonymous(oldCartId))
		{
			throw new Exception("Cart is not anonymous could not restore with cartid{" + oldCartId + "}");
		}

		if (StringUtils.isEmpty(evaluatedToMergeCartGuid))
		{
			evaluatedToMergeCartGuid = getOptimizedCartFacade().getSessionCartData().getGuid();
		}
		else
		{
			if (!isUserCart(evaluatedToMergeCartGuid))
			{
				//throw new Exception("Cart is not current user's cart", CartException.CANNOT_RESTORE, evaluatedToMergeCartGuid);
				throw new Exception("Cart is not anonymous could not restore with cartid{" + evaluatedToMergeCartGuid + "}");
			}
		}
	}

	protected boolean isUserCart(final String toMergeCartGuid)
	{

		final OptimizedCartData cart = optimizeModelDealService.getCartDataForGuidAndSiteAndUser(toMergeCartGuid,
				getBaseSiteService().getCurrentBaseSite(), getUserService().getCurrentUser().getUid());
		return cart != null;
	}

	protected boolean isCartAnonymous(final String cartGuid)
	{
		final OptimizedCartData cart = optimizeModelDealService.getCartDataForGuidAndSiteAndUser(cartGuid,
				getBaseSiteService().getCurrentBaseSite(), getUserService().getAnonymousUser().getUid());
		return cart != null;
	}

	@Override
	public CCPaymentInfoData createPaymentSubscription(final CCPaymentInfoData paymentInfoData)
	{
		validateParameterNotNullStandardMessage("paymentInfoData", paymentInfoData);
		final AddressData billingAddressData = paymentInfoData.getBillingAddress();
		validateParameterNotNullStandardMessage("billingAddress", billingAddressData);

		if (checkIfCurrentUserIsTheCartUser())
		{
			final CardInfo cardInfo = new CardInfo();
			cardInfo.setCardHolderFullName(paymentInfoData.getAccountHolderName());
			cardInfo.setCardNumber(paymentInfoData.getCardNumber());
			final CardType cardType = getCommerceCardTypeService().getCardTypeForCode(paymentInfoData.getCardType());
			cardInfo.setCardType(cardType == null ? null : cardType.getCode());
			cardInfo.setExpirationMonth(Integer.valueOf(paymentInfoData.getExpiryMonth()));
			cardInfo.setExpirationYear(Integer.valueOf(paymentInfoData.getExpiryYear()));
			cardInfo.setIssueNumber(paymentInfoData.getIssueNumber());

			final BillingInfo billingInfo = new BillingInfo();
			billingInfo.setCity(billingAddressData.getTown());
			billingInfo.setCountry(billingAddressData.getCountry() == null ? null : billingAddressData.getCountry().getIsocode());
			billingInfo.setFirstName(billingAddressData.getFirstName());
			billingInfo.setLastName(billingAddressData.getLastName());
			billingInfo.setEmail(billingAddressData.getEmail());
			billingInfo.setPhoneNumber(billingAddressData.getPhone());
			billingInfo.setPostalCode(billingAddressData.getPostalCode());
			billingInfo.setStreet1(billingAddressData.getLine1());
			billingInfo.setStreet2(billingAddressData.getLine2());

			final CreditCardPaymentInfoModel ccPaymentInfoModel = getCustomerAccountService().createPaymentSubscription(
					getCurrentUserForCheckout(), cardInfo, billingInfo, billingAddressData.getTitleCode(), getPaymentProvider(),
					paymentInfoData.isSaved());
			return ccPaymentInfoModel == null ? null : getCreditCardPaymentInfoConverter().convert(ccPaymentInfoModel);
		}
		return null;
	}


	@Override
	protected boolean checkIfCurrentUserIsTheCartUser()
	{
		final OptimizedCartData cartModel = optimizeCartService.getSessionOptimizedCart();
		return cartModel == null ? false : cartModel.getUserId().equals(getCurrentUserForCheckout().getUid());
	}

	@Override
	protected AddressModel getDeliveryAddressModelForCode(final String code)
	{
		Assert.notNull(code, "Parameter code cannot be null.");
		final OptimizedCartData cartModel = optimizeCartService.getSessionOptimizedCart();
		if (cartModel != null)
		{
			final CustomerModel customer = (CustomerModel) getUserService().getUserForUID(cartModel.getUserId());

			for (final AddressModel address : getSupportedDeliveryAddressesForOrder(customer, false))
			{
				if (code.equals(address.getPk().toString()))
				{
					return address;
				}
			}
		}
		return null;
	}


	public List<AddressModel> getSupportedDeliveryAddressesForOrder(final CustomerModel customer,
			final boolean visibleAddressesOnly)
	{
		final List<AddressModel> addresses = new ArrayList<AddressModel>();
		addresses.addAll(customer.getAddresses());

		if (!addresses.isEmpty())
		{
			final List<CountryModel> deliveryCountries = getDeliveryService().getDeliveryCountriesForOrder(null);

			final List<AddressModel> result = new ArrayList<AddressModel>();

			// Filter for delivery addresses
			for (final AddressModel address : addresses)
			{
				if (address.getCountry() != null)
				{
					// Filter out invalid addresses for the site
					final boolean validForSite = deliveryCountries != null && deliveryCountries.contains(address.getCountry());
					if (validForSite)
					{
						result.add(address);
					}
				}
			}

			return result;
		}
		return Collections.emptyList();
	}

	/**
	 * @return the sessionService
	 */
	public SessionService getSessionService()
	{
		return sessionService;
	}

	/**
	 * @param sessionService
	 *           the sessionService to set
	 */
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
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
	 * @return the optimizeCommerceCartService
	 */
	public OptimizeCommerceCartService getOptimizeCommerceCartService()
	{
		return optimizeCommerceCartService;
	}

	/**
	 * @param optimizeCommerceCartService
	 *           the optimizeCommerceCartService to set
	 */
	public void setOptimizeCommerceCartService(final OptimizeCommerceCartService optimizeCommerceCartService)
	{
		this.optimizeCommerceCartService = optimizeCommerceCartService;
	}

	/**
	 * @return the optimizeCartService
	 */
	public OptimizeCartService getOptimizeCartService()
	{
		return optimizeCartService;
	}

	/**
	 * @param optimizeCartService
	 *           the optimizeCartService to set
	 */
	public void setOptimizeCartService(final OptimizeCartService optimizeCartService)
	{
		this.optimizeCartService = optimizeCartService;
	}

	/**
	 * @return the optimizedCartFacade
	 */
	public OptimizedCartFacade getOptimizedCartFacade()
	{
		return (OptimizedCartFacade) super.getCartFacade();
	}



	/**
	 * @return the addressConverter
	 */
	@Override
	public Converter<AddressModel, AddressData> getAddressConverter()
	{
		return addressConverter;
	}

	/**
	 * @param addressConverter
	 *           the addressConverter to set
	 */
	@Override
	public void setAddressConverter(final Converter<AddressModel, AddressData> addressConverter)
	{
		this.addressConverter = addressConverter;
	}

	/**
	 * @return the cartReverseConverter
	 */
	public Converter<OptimizedCartData, OptimizedCartModel> getCartReverseConverter()
	{
		return cartReverseConverter;
	}

	/**
	 * @param cartReverseConverter
	 *           the cartReverseConverter to set
	 */
	public void setCartReverseConverter(final Converter<OptimizedCartData, OptimizedCartModel> cartReverseConverter)
	{
		this.cartReverseConverter = cartReverseConverter;
	}

	/**
	 * @return the optimizeCommerceCheckoutService
	 */
	public OptimizeCommerceCheckoutService getOptimizeCommerceCheckoutService()
	{
		return optimizeCommerceCheckoutService;
	}

	/**
	 * @param optimizeCommerceCheckoutService
	 *           the optimizeCommerceCheckoutService to set
	 */
	public void setOptimizeCommerceCheckoutService(final OptimizeCommerceCheckoutService optimizeCommerceCheckoutService)
	{
		this.optimizeCommerceCheckoutService = optimizeCommerceCheckoutService;
	}

	/**
	 * @return the baseSiteService
	 */
	public BaseSiteService getBaseSiteService()
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
	 * @return the optimizeDeliveryService
	 */
	public OptimizeDeliveyService getOptimizeDeliveryService() {
		return optimizeDeliveryService;
	}

	/**
	 * @param optimizeDeliveryService the optimizeDeliveryService to set
	 */
	public void setOptimizeDeliveryService(OptimizeDeliveyService optimizeDeliveryService) {
		super.setDeliveryService(optimizeDeliveryService);
		this.optimizeDeliveryService = optimizeDeliveryService;
	}
}
