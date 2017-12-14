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

import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.RegisterData;
import de.hybris.platform.commerceservices.constants.CommerceServicesConstants;
import de.hybris.platform.commerceservices.externaltax.ExternalTaxesService;
import de.hybris.platform.commerceservices.order.hook.CommercePlaceOrderMethodHook;
import de.hybris.platform.commerceservices.order.impl.DefaultCommerceCheckoutService;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.commerceservices.service.data.CommerceOrderResult;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.order.OrderService;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.services.BaseStoreService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jgroups.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.facades.data.OptimizedCartEntryData;
import com.accenture.performance.optimization.service.OptimizeCartService;
import com.accenture.performance.optimization.service.OptimizeCommerceCheckoutService;
import com.accenture.performance.optimization.service.OptimizeModelDealService;
import com.accenture.performance.optimization.service.OptimizePromotionService;


/**
 *
 */
public class DefaultOptimizeCommerceCheckoutService extends DefaultCommerceCheckoutService
		implements OptimizeCommerceCheckoutService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultOptimizeCommerceCheckoutService.class);

	private List<CommercePlaceOrderMethodHook> commercePlaceOrderMethodHooks;
	private ConfigurationService configurationService;
	private BaseSiteService baseSiteService;
	private BaseStoreService baseStoreService;
	private CommonI18NService commonI18NService;
	private ModelService modelService;
	private ExternalTaxesService externalTaxesService;
	private OrderService orderService;
	private CalculationService calculationService;
	private UserService userService;

	private Converter<CreditCardPaymentInfoModel, CCPaymentInfoData> creditCardPaymentInfoConverter;
	private OptimizeCartService optimizeCartService;
	private CustomerFacade customerFacade;
	private UnitService unitService;
	private KeyGenerator keyGenerator;
	private ProductService productService;
	private OptimizeModelDealService optimizeModelDealService;



	@Override
	public CommerceOrderResult placeOrder(final CommerceCheckoutParameter parameter) throws InvalidCartException
	{

		final OptimizedCartData cartData = parameter.getOptimizeCart();
		String userId = "";
		if (cartData == null)
		{
			userId = getUserService().getCurrentUser().getUid();
		}
		else
		{
			userId = cartData.getUserId();
		}
		//		final OptimizedCartData cartModel = optimizeModelDealService.getCartDataForGuidAndSiteAndUser(cartData.getGuid(),
		//				cartData.getBaseSite(), cartData.getUserId());

		final CommerceOrderResult result = new CommerceOrderResult();
		try
		{
			beforePlaceOrder(parameter);
			final CustomerModel customer = (CustomerModel) getUserService().getUserForUID(userId);
			validateParameterNotNull(customer, "Customer model cannot be null");

			final OrderModel orderModel = this.createOrderFromCart(cartData);
			if (orderModel != null)
			{
				orderModel.setSalesApplication(parameter.getSalesApplication());

				// Calculate the order now that it has been copied
				try
				{
					getCalculationService().calculateTotals(orderModel, false);
					//getExternalTaxesService().calculateExternalTaxes(orderModel);
				}
				catch (final CalculationException ex)
				{
					LOG.error("Failed to calculate order [" + orderModel + "]", ex);
				}

				getModelService().saveAll(customer, orderModel);

				result.setOrder(orderModel);

				this.beforeSubmitOrder(parameter, result);

				getOrderService().submitOrder(orderModel);
			}
			else
			{
				throw new IllegalArgumentException(String.format("Order was not properly created from cart %s", cartData.getCode()));
			}
		}
		finally
		{
			getExternalTaxesService().clearSessionTaxDocument();
		}

		this.afterPlaceOrder(parameter, result);
		return result;
	}

	protected void beforePlaceOrder(final CommerceCheckoutParameter parameter) throws InvalidCartException
	{
		if (getCommercePlaceOrderMethodHooks() != null && parameter.isEnableHooks())
		{
			for (final CommercePlaceOrderMethodHook commercePlaceOrderMethodHook : getCommercePlaceOrderMethodHooks())
			{
				commercePlaceOrderMethodHook.beforePlaceOrder(parameter);
			}
		}
	}

	protected void beforeSubmitOrder(final CommerceCheckoutParameter parameter, final CommerceOrderResult result)
			throws InvalidCartException
	{
		if (getCommercePlaceOrderMethodHooks() != null && (parameter.isEnableHooks()
				&& getConfigurationService().getConfiguration().getBoolean(CommerceServicesConstants.PLACEORDERHOOK_ENABLED, true)))
		{
			for (final CommercePlaceOrderMethodHook commercePlaceOrderMethodHook : getCommercePlaceOrderMethodHooks())
			{
				commercePlaceOrderMethodHook.beforeSubmitOrder(parameter, result);
			}
		}
	}

	protected void afterPlaceOrder(final CommerceCheckoutParameter parameter, final CommerceOrderResult result)
			throws InvalidCartException
	{


		if (getCommercePlaceOrderMethodHooks() != null && (parameter.isEnableHooks()
				&& getConfigurationService().getConfiguration().getBoolean(CommerceServicesConstants.PLACEORDERHOOK_ENABLED, true)))
		{
			for (final CommercePlaceOrderMethodHook commercePlaceOrderMethodHook : getCommercePlaceOrderMethodHooks())
			{
				commercePlaceOrderMethodHook.afterPlaceOrder(parameter, result);
			}
		}
	}

	protected OrderModel createOrderFromCart(final OptimizedCartData cartModel) throws InvalidCartException
	{
		final CustomerModel customer = prepareCustomer(cartModel.getUserId());
		final CurrencyModel currency = getCommonI18NService().getCurrentCurrency();

		OrderModel target = null;
		target = getModelService().create(OrderModel.class);
		target.setCode(generateOrderCode());
		target.setCurrency(currency);
		target.setUser(customer);
		target.setCalculated(cartModel.getCalculated());
		target.setStatus(OrderStatus.CREATED);
		target.setGuid(UUID.randomUUID().toString());
		// Reset the Date attribute for use in determining when the order was placed
		target.setDate(new Date());
		// Store the current site and store on the order
		target.setSite(getBaseSiteService().getCurrentBaseSite());
		target.setStore(getBaseStoreService().getCurrentBaseStore());
		target.setLanguage(getCommonI18NService().getCurrentLanguage());

		target.setDeliveryCost(convertNullTODouble(cartModel.getDeliveryCost()));
		target.setPaymentCost(convertNullTODouble(cartModel.getPaymentCost()));
		target.setGlobalDiscountValues(cartModel.getGlobalDiscountValues());
		target.setTotalDiscounts(cartModel.getTotalDiscounts());
		target.setSubtotal(cartModel.getSubtotal());
		target.setTotalPrice(cartModel.getTotalPrice());

		//target.setPaymentMode();
		//target.setDescription();
		//target.setAppliedCouponCodes(value);

		// clear the promotionResults that where cloned from cart PromotionService.transferPromotionsToOrder will copy them over bellow.
		//orderModel.setAllPromotionResults(Collections.<PromotionResultModel> emptySet());

		//--------------------------------------total price--------------------------------

		if (cartModel.getDeliveryMode() != null)
		{
			target.setDeliveryMode(this.getDeliveryService().getDeliveryModeForCode(cartModel.getDeliveryMode()));
		}

		//-------------------------------------------------paymentInfo---------------------------------
		if (cartModel.getPaymentInfo() != null)
		{
			//TODO : set payment info
			//			target.setPaymentInfo(cartModel.getPaymentInfo());
		}


		//-------------------------------------------------address---------------------------------

		createAddressModel(cartModel, target);

		//---------------------------------------------------entry----------------------------------

		createEntries(cartModel, target);

		// Transfer promotions to the order
		if (CollectionUtils.isNotEmpty(cartModel.getAllPromotionResults()))
		{
			((OptimizePromotionService) getPromotionsService()).transferPromotionsToOrder(cartModel, target, false);
		}

		// getCalculationService().calculate(target);
		//		getModelService().save(target);
		//		getModelService().refresh(target);
		return target;
	}

	/**
	 *
	 */
	private Double convertNullTODouble(final Double cost)
	{
		return cost == null ? new Double(0) : cost;
	}

	/**
	 *
	 */
	private void createEntries(final OptimizedCartData cartModel, final OrderModel target)
	{
		final List<OptimizedCartEntryData> entrylist = cartModel.getEntries();
		if (entrylist != null && entrylist.size() > 0)
		{
			final List<AbstractOrderEntryModel> modelEntrylist = new ArrayList<AbstractOrderEntryModel>();
			OrderEntryModel oem = null;
			for (final OptimizedCartEntryData oed : entrylist)
			{
				oem = modelService.create(OrderEntryModel.class);
				//oem.setAdjustFee(oed.g);
				if (!StringUtils.isEmpty(oed.getProductCode()))
				{
					final ProductModel product = getProductService().getProductForCode(oed.getProductCode());
					if (product == null)
					{
						throw new ConversionException("product not exist!");
					}
					//oem.setProductId(product.getCode());
					oem.setProduct(product);
					//oem.setProductProperties(oed.getProduct().);
					if (oed.getBasePrice() != null && oed.getBasePrice() != null)
					{
						oem.setBasePrice(oed.getBasePrice());
					}
					oem.setOrder(target);
					oem.setQuantity(oed.getQuantity());
					oem.setUnit(getUnitService().getUnitForCode("pieces"));
					oem.setEntryNumber(oed.getEntryNumber());
					modelEntrylist.add(oem);
					oem.setDiscountValues(oed.getDiscountList());
					//getModelService().save(oem);
					//modelToSave.add(oem);
				}
			}
			target.setEntries(modelEntrylist);
		}
	}

	/**
	 *
	 */
	private void createAddressModel(final OptimizedCartData cartModel, final OrderModel target)
	{
		final AddressData addressModel = cartModel.getDeliveryAddress();
		if (addressModel != null)
		{
			final String newId = addressModel.getId();
			if (newId == null)
			{
				LOG.error("the cart data address is not a saved address! cart code : " + cartModel.getCode());
			}
			else
			{
				// clone new data model && no save
				final AddressModel newAddress = this.getModelService().get(PK.parse(cartModel.getDeliveryAddress().getId()));
				final AddressModel toSaveAddress = this.getModelService().clone(newAddress);
				toSaveAddress.setOwner(target);
				target.setDeliveryAddress(toSaveAddress);
			}
		}
	}

	@Override
	public boolean setPaymentInfo(final CommerceCheckoutParameter parameter)
	{
		final OptimizedCartData cartData = parameter.getOptimizeCart();
		final PaymentInfoModel paymentInfoModel = parameter.getPaymentInfo();
		final CCPaymentInfoData paymentInfoData = creditCardPaymentInfoConverter
				.convert((CreditCardPaymentInfoModel) parameter.getPaymentInfo());
		validateParameterNotNull(cartData, "Cart model cannot be null");
		validateParameterNotNull(paymentInfoModel, "payment info model cannot be null");
		cartData.setPaymentInfo(paymentInfoData);
		getOptimizeCartService().setSessionCartData(cartData);
		optimizeModelDealService.persistCart(cartData);
		return true;
	}

	private CustomerModel prepareCustomer(final String uid)
	{

		final String userName = "";
		CustomerModel customer = null;
		if (!StringUtils.isEmpty(uid))
		{
			customer = (CustomerModel) getUserService().getUserForUID(uid);
		}


		if (customer == null || (StringUtils.isEmpty(uid)))
		{
			LOG.info("no existing user, now try to register a new user!");
			final RegisterData data = new RegisterData();
			data.setLogin(uid);
			data.setTitleCode("mr");
			data.setPassword("123123");
			data.setFirstName(userName);
			try
			{
				getCustomerFacade().register(data);
			}
			catch (final Exception ex)
			{
				LOG.info("CreateUser failed, The customer has existed" + uid);
			}
			customer = (CustomerModel) getUserService().getUserForUID(uid);
		}
		return customer;
	}

	private String generateOrderCode()
	{
		final Object generatedValue = getKeyGenerator().generate();
		//LOG.info("The className of keyGenerator is ......" + getKeyGenerator().getClass().getName());
		//LOG.info("The typeName of keyGenerator is ......" + getKeyGenerator().getClass().getTypeName());

		if (generatedValue instanceof String)
		{
			return (String) generatedValue;
		}
		else
		{
			return String.valueOf(generatedValue);
		}
	}




	/**
	 * @return the commercePlaceOrderMethodHooks
	 */
	public List<CommercePlaceOrderMethodHook> getCommercePlaceOrderMethodHooks()
	{
		return commercePlaceOrderMethodHooks;
	}

	/**
	 * @param commercePlaceOrderMethodHooks
	 *           the commercePlaceOrderMethodHooks to set
	 */
	public void setCommercePlaceOrderMethodHooks(final List<CommercePlaceOrderMethodHook> commercePlaceOrderMethodHooks)
	{
		this.commercePlaceOrderMethodHooks = commercePlaceOrderMethodHooks;
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
	@Override
	public BaseSiteService getBaseSiteService()
	{
		return baseSiteService;
	}

	/**
	 * @param baseSiteService
	 *           the baseSiteService to set
	 */
	@Override
	public void setBaseSiteService(final BaseSiteService baseSiteService)
	{
		this.baseSiteService = baseSiteService;
	}

	/**
	 * @return the baseStoreService
	 */
	@Override
	public BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	/**
	 * @param baseStoreService
	 *           the baseStoreService to set
	 */
	@Override
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

	/**
	 * @return the commonI18NService
	 */
	@Override
	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	/**
	 * @param commonI18NService
	 *           the commonI18NService to set
	 */
	@Override
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	/**
	 * @return the modelService
	 */
	@Override
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	@Override
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @return the externalTaxesService
	 */
	@Override
	public ExternalTaxesService getExternalTaxesService()
	{
		return externalTaxesService;
	}

	/**
	 * @param externalTaxesService
	 *           the externalTaxesService to set
	 */
	@Override
	public void setExternalTaxesService(final ExternalTaxesService externalTaxesService)
	{
		this.externalTaxesService = externalTaxesService;
	}

	/**
	 * @return the orderService
	 */
	@Override
	public OrderService getOrderService()
	{
		return orderService;
	}

	/**
	 * @param orderService
	 *           the orderService to set
	 */
	@Override
	public void setOrderService(final OrderService orderService)
	{
		this.orderService = orderService;
	}

	/**
	 * @return the calculationService
	 */
	@Override
	public CalculationService getCalculationService()
	{
		return calculationService;
	}

	/**
	 * @param calculationService
	 *           the calculationService to set
	 */
	@Override
	public void setCalculationService(final CalculationService calculationService)
	{
		this.calculationService = calculationService;
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
	 * @return the customerFacade
	 */
	public CustomerFacade getCustomerFacade()
	{
		return customerFacade;
	}

	/**
	 * @param customerFacade
	 *           the customerFacade to set
	 */
	public void setCustomerFacade(final CustomerFacade customerFacade)
	{
		this.customerFacade = customerFacade;
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
	 * @return the keyGenerator
	 */
	public KeyGenerator getKeyGenerator()
	{
		return keyGenerator;
	}

	/**
	 * @param keyGenerator
	 *           the keyGenerator to set
	 */
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}

	/**
	 * @return the productService
	 */
	public ProductService getProductService()
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
	 * @return the unitService
	 */
	public UnitService getUnitService()
	{
		return unitService;
	}

	/**
	 * @param unitService
	 *           the unitService to set
	 */
	public void setUnitService(final UnitService unitService)
	{
		this.unitService = unitService;
	}

	/**
	 * @return the creditCardPaymentInfoConverter
	 */
	public Converter<CreditCardPaymentInfoModel, CCPaymentInfoData> getCreditCardPaymentInfoConverter()
	{
		return creditCardPaymentInfoConverter;
	}

	/**
	 * @param creditCardPaymentInfoConverter
	 *           the creditCardPaymentInfoConverter to set
	 */
	public void setCreditCardPaymentInfoConverter(
			final Converter<CreditCardPaymentInfoModel, CCPaymentInfoData> creditCardPaymentInfoConverter)
	{
		this.creditCardPaymentInfoConverter = creditCardPaymentInfoConverter;
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




}
