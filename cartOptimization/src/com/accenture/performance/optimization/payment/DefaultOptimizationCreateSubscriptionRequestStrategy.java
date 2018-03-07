package com.accenture.performance.optimization.payment;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.service.OptimizeCartService;

import de.hybris.platform.acceleratorservices.payment.cybersource.enums.SubscriptionFrequencyEnum;
import de.hybris.platform.acceleratorservices.payment.cybersource.enums.TransactionTypeEnum;
import de.hybris.platform.acceleratorservices.payment.cybersource.strategies.impl.DefaultCreateSubscriptionRequestStrategy;
import de.hybris.platform.acceleratorservices.payment.data.CreateSubscriptionRequest;
import de.hybris.platform.acceleratorservices.payment.data.CustomerShipToData;
import de.hybris.platform.acceleratorservices.payment.data.SignatureData;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

public class DefaultOptimizationCreateSubscriptionRequestStrategy extends DefaultCreateSubscriptionRequestStrategy {
	
	private OptimizeCartService cartService;
	private Converter<OptimizedCartData, CustomerShipToData> optimizCustomerShipToDataConverter;
	
	@Override
	public CreateSubscriptionRequest createSubscriptionRequest(final String siteName, final String requestUrl,
	                                                           final String responseUrl, final String merchantCallbackUrl, final CustomerModel customerModel,
	                                                           final CreditCardPaymentInfoModel cardInfo, final AddressModel paymentAddress)
	{
		final OptimizedCartData optimizedCartData = getCartService().getSessionOptimizedCart();
		if (optimizedCartData == null)
		{
			return null;
		}

		final CreateSubscriptionRequest request = new CreateSubscriptionRequest();
		//Common Data
		request.setRequestId(UUID.randomUUID().toString());
		request.setSiteName(siteName);
		request.setRequestUrl(requestUrl);

		//Version Specific Data using converters
		request.setCustomerBillToData(getCustomerBillToDataConverter().convert(paymentAddress));
		this.setEmailAddress(request.getCustomerBillToData(), customerModel);
		request.setCustomerShipToData(getOptimizCustomerShipToDataConverter().convert(optimizedCartData));
		request.setPaymentInfoData(getPaymentInfoDataConverter().convert(cardInfo));

		//In-line Version Specific Data
		request.setOrderInfoData(getRequestOrderInfoData(TransactionTypeEnum.subscription));
		request.setSignatureData(getRequestSignatureData());
		request.setSubscriptionSignatureData(getRequestSubscriptionSignatureData(SubscriptionFrequencyEnum.ON_DEMAND));
		request.setOrderPageAppearanceData(getHostedOrderPageAppearanceConfiguration());
		request.setOrderPageConfirmationData(getOrderPageConfirmationData(responseUrl, merchantCallbackUrl));

		return request;
	}
	
	@Override
	protected SignatureData getRequestSignatureData()
	{
		final SignatureData data = new SignatureData();

		final OptimizedCartData optimizedCartData = getCartService().getSessionOptimizedCart();
		if (optimizedCartData == null)
		{
			return null;
		}

		if (StringUtils.isNotEmpty(getHostedOrderPageTestCurrency()))
		{
			data.setCurrency(getHostedOrderPageTestCurrency());
		}
		else
		{
			data.setCurrency(optimizedCartData.getCurrencyCode());

		}

		data.setAmount(getSetupFeeAmount());
		data.setMerchantID(getMerchantId());
		data.setOrderPageSerialNumber(getSerialNumber());
		data.setOrderPageVersion(getHostedOrderPageVersion());
		data.setSharedSecret(getSharedSecret());

		return data;
	}
	/**
	 * @return the OptimizeCartService
	 */
	@Override
	public OptimizeCartService getCartService() {
		return cartService;
	}
	
	/**
	 * @param optimizeCartService the optimizeCartService to set
	 */
	public void setCartService(OptimizeCartService optimizeCartService) {
		super.setCartService(optimizeCartService);
		this.cartService = optimizeCartService;
	}

	/**
	 * @return the optimizCustomerShipToDataConverter
	 */
	public Converter<OptimizedCartData, CustomerShipToData> getOptimizCustomerShipToDataConverter() {
		return optimizCustomerShipToDataConverter;
	}

	/**
	 * @param optimizCustomerShipToDataConverter the optimizCustomerShipToDataConverter to set
	 */
	public void setOptimizCustomerShipToDataConverter(
			Converter<OptimizedCartData, CustomerShipToData> optimizCustomerShipToDataConverter) {
		this.optimizCustomerShipToDataConverter = optimizCustomerShipToDataConverter;
	}
}
