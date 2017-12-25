package com.accenture.performance.optimization.payment.converters.populators;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;

import de.hybris.platform.acceleratorservices.payment.cybersource.converters.populators.response.AbstractResultPopulator;
import de.hybris.platform.acceleratorservices.payment.data.CustomerShipToData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class DefaultOptimizationCustomerShipToDataPopulator extends AbstractResultPopulator<OptimizedCartData, CustomerShipToData>{
	@Override
	public void populate(final OptimizedCartData source, final CustomerShipToData target) throws ConversionException
	{
		validateParameterNotNull(source, "Parameter [CartModel] source cannot be null");
		validateParameterNotNull(target, "Parameter [CustomerShipToData] target cannot be null");

		final AddressData deliveryAddress = source.getDeliveryAddress();
		if (deliveryAddress != null)
		{
			target.setShipToCity(deliveryAddress.getTown());
			target.setShipToCompany(deliveryAddress.getCompanyName());
			target.setShipToCountry(deliveryAddress.getCountry().getIsocode());
			if (deliveryAddress.getRegion() != null)
			{
				target.setShipToState(deliveryAddress.getRegion().getIsocodeShort());
			}
			target.setShipToFirstName(deliveryAddress.getFirstName());
			target.setShipToLastName(deliveryAddress.getLastName());
			target.setShipToPhoneNumber(deliveryAddress.getPhone());
			target.setShipToPostalCode(deliveryAddress.getPostalCode());
			target.setShipToStreet1(deliveryAddress.getLine1());
			target.setShipToStreet2(deliveryAddress.getLine2());
		}

		final String deliveryModeCode = source.getDeliveryMode();
		if (deliveryModeCode == null)
		{
			target.setShipToShippingMethod("none");
		}
		else
		{
			target.setShipToShippingMethod(deliveryModeCode);
		}
	}
}
