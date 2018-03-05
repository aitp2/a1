package com.accenture.performance.optimization.service.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.accenture.performance.optimization.service.OptimizeDeliveyService;

import de.hybris.platform.commerceservices.delivery.impl.DefaultDeliveryService;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.Tenant;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.deliveryzone.constants.GeneratedZoneDeliveryModeConstants;
import de.hybris.platform.deliveryzone.jalo.ZoneDeliveryMode;
import de.hybris.platform.deliveryzone.jalo.ZoneDeliveryModeValue;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.c2l.C2LManager;
import de.hybris.platform.jalo.c2l.Country;
import de.hybris.platform.jalo.c2l.Currency;
import de.hybris.platform.jalo.flexiblesearch.FlexibleSearch;
import de.hybris.platform.jalo.order.delivery.DeliveryMode;
import de.hybris.platform.jalo.order.delivery.JaloDeliveryModeException;
import de.hybris.platform.jalo.user.Address;
import de.hybris.platform.util.PriceValue;

public class DefaultOptimizeDeliveryService extends DefaultDeliveryService implements OptimizeDeliveyService{
	private final static Logger LOG = Logger.getLogger(DefaultOptimizeDeliveryService.class);
	
	@Override
	public List<DeliveryModeModel> getSupportedDeliveryModeListForOrder(AbstractOrderModel abstractOrder)
	{
		validateParameterNotNull(abstractOrder, "abstractOrder model cannot be null");
		final List<DeliveryModeModel> deliveryModes = getDeliveryModeLookupStrategy().getSelectableDeliveryModesForOrder(
				abstractOrder);
		sortDeliveryModes(deliveryModes, abstractOrder);
		return deliveryModes;
	}
	
	//TODO acn
	@Override
	protected void sortDeliveryModes(final List<DeliveryModeModel> deliveryModeModels, final AbstractOrderModel abstractOrder)
	{
		//
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.jalo.order.delivery.DeliveryMode#getCost(SessionContext ctx, AbstractOrder order) and 
	 * @see de.hybris.platform.deliveryzone.jalo.ZoneDeliveryMode#getCost(SessionContext ctx, AbstractOrder order) and
	 * @see de.hybris.platform.commerceservices.jalo.PickUpDeliveryMode
	 */
	@Override
	public PriceValue getOptimizeDeliveryCostForDeliveryModeAndAbstractOrder(final DeliveryModeModel deliveryMode, final AbstractOrderModel cart)
	{
		validateParameterNotNull(deliveryMode, "deliveryMode model cannot be null");
		validateParameterNotNull(cart, "abstractOrder model cannot be null");

		final DeliveryMode deliveryModeSource = getModelService().getSource(deliveryMode);
		if( !(deliveryModeSource instanceof ZoneDeliveryMode) )
		{
			return new PriceValue(cart.getCurrency().getIsocode(), 0.0, cart.getNet().booleanValue());
		}
		
		AddressModel deliverAddress = cart.getDeliveryAddress();
		Address addr = (Address)getModelService().getSource(deliverAddress);

		JaloDeliveryModeException jaloDeliveryModeExcep = null;
		if (addr == null) {
			jaloDeliveryModeExcep = new JaloDeliveryModeException("getCost(): delivery address was NULL in order " + (Object) cart, 0);
			LOG.error(jaloDeliveryModeExcep.getMessage(), jaloDeliveryModeExcep);
		}
		Country country = addr.getCountry();
		if (country == null) {
			jaloDeliveryModeExcep = new JaloDeliveryModeException(
					"getCost(): country of delivery address " + (Object) addr + " was NULL in order " + (Object) cart,
					0);
			LOG.error(jaloDeliveryModeExcep.getMessage(), jaloDeliveryModeExcep);
		}
		
		CurrencyModel currency = cart.getCurrency();
		Currency curr = (Currency)getModelService().getSource(currency);
		if (curr == null) {
			jaloDeliveryModeExcep =  new JaloDeliveryModeException("getCost(): currency was NULL in order " + (Object) cart, 0);
			LOG.error(jaloDeliveryModeExcep.getMessage(), jaloDeliveryModeExcep);
		}
		String propName = "delivery.zone.price";//this.getPropertyName();
		if (propName == null) {
			jaloDeliveryModeExcep =  new JaloDeliveryModeException("missing propertyname in deliverymode " + (Object) ((Object) deliveryModeSource), 0);
			LOG.error(jaloDeliveryModeExcep.getMessage(), jaloDeliveryModeExcep);
		}
		double amount = cart.getSubtotal().doubleValue();//this.getCalculationBaseValue(ctx, order, propName);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("me", (Object) deliveryModeSource);
		params.put("curr", (Object) curr);
		params.put("country", (Object) country);
		params.put("amount", new Double(amount));
		String query = "SELECT {v." + "pk".intern() + "} " + "FROM {" + GeneratedZoneDeliveryModeConstants.TC.ZONEDELIVERYMODEVALUE
				+ " AS v " + "JOIN " + GeneratedZoneDeliveryModeConstants.Relations.ZONECOUNTRYRELATION + " AS z2cRel "
				+ "ON {v." + "zone" + "}={z2cRel." + "source" + "} } " + "WHERE " + "{v." + "deliveryMode"
				+ "} = ?me AND " + "{v." + "currency" + "} = ?curr AND " + "{v." + "minimum" + "} <= ?amount AND "
				+ "{z2cRel." + "target" + "} = ?country " + "ORDER BY {v." + "minimum" + "} DESC ";
		
		SessionContext ctx = JaloSession.getCurrentSession((Tenant) Registry.getCurrentTenantNoFallback()).getSessionContext();
		List values = FlexibleSearch.getInstance().search(ctx, query, params, ZoneDeliveryModeValue.class).getResult();
		if (values.isEmpty() && !curr.isBase().booleanValue() && C2LManager.getInstance().getBaseCurrency() != null) {
			params.put("curr", (Object) C2LManager.getInstance().getBaseCurrency());
			values = FlexibleSearch.getInstance().search(ctx, query, params, ZoneDeliveryModeValue.class).getResult();
		}
		if (values.isEmpty()) {
			jaloDeliveryModeExcep =  new JaloDeliveryModeException("no delivery price defined for mode " + (Object) ((Object) deliveryModeSource)
					+ ", country " + (Object) country + ", currency " + (Object) curr + " and amount " + amount, 0);
			LOG.error(jaloDeliveryModeExcep.getMessage(), jaloDeliveryModeExcep);
		}
		
		ZoneDeliveryModeValue bestMatch = (ZoneDeliveryModeValue) values.get(0);
		Currency myCurr = bestMatch.getCurrency();
		
		ZoneDeliveryMode zonDeliveryMode = (ZoneDeliveryMode) deliveryModeSource;
		if (!curr.equals((Object) myCurr) && myCurr != null) {
			return new PriceValue(curr.getIsoCode(), myCurr.convertAndRound(curr, bestMatch.getValueAsPrimitive()),
					zonDeliveryMode.isNetAsPrimitive(ctx));
		}
		return new PriceValue(curr.getIsoCode(), bestMatch.getValueAsPrimitive(), zonDeliveryMode.isNetAsPrimitive(ctx));
	}

	
}
