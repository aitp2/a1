package com.accenture.aitp.cart.service.order.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.accenture.aitp.cart.constants.AitpcartConstants;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.order.impl.DefaultCalculationService;
import de.hybris.platform.order.strategies.calculation.FindDeliveryCostStrategy;
import de.hybris.platform.order.strategies.calculation.FindPaymentCostStrategy;
import de.hybris.platform.order.strategies.calculation.FindTaxValuesStrategy;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.util.PriceValue;
import de.hybris.platform.util.TaxValue;

public class AitpCalculationService extends DefaultCalculationService
{
	private static final Logger LOG = Logger.getLogger(AitpCalculationService.class);
	private List<FindTaxValuesStrategy> findTaxesStrategies;
	private ConfigurationService configurationService;
	private FindDeliveryCostStrategy findDeliveryCostStrategy;
	private FindPaymentCostStrategy findPaymentCostStrategy;
	
	@Override
	protected Collection<TaxValue> findTaxValues(final AbstractOrderEntryModel entry) throws CalculationException
	{
		if (AitpcartConstants.AITP_CART_SWTICH_OFF.equalsIgnoreCase(getConfigurationService().getConfiguration().getString(AitpcartConstants.AITP_CART_TAX_SWTICH)))
		{
			LOG.debug("AITP_CART_TAX_SWTICH CLOSE TAX !");
			return Collections.<TaxValue> emptyList();
		}
		else if(findTaxesStrategies.isEmpty())
		{
			LOG.warn("No strategies for finding tax values could be found!");
			return Collections.<TaxValue> emptyList();
		}
		else
		{
			final List<TaxValue> result = new ArrayList<TaxValue>();
			for (final FindTaxValuesStrategy findStrategy : findTaxesStrategies)
			{
				result.addAll(findStrategy.findTaxValues(entry));
			}
			return result;
		}
	}
	
	@Override
	protected void resetAdditionalCosts(final AbstractOrderModel order, final Collection<TaxValue> relativeTaxValues)
	{
		final PriceValue deliCost = findDeliveryCostStrategy.getDeliveryCost(order);
		double deliveryCostValue = 0.0;
		if (deliCost != null)
		{
			deliveryCostValue = convertPriceIfNecessary(deliCost, order.getNet().booleanValue(), order.getCurrency(),
					relativeTaxValues).getValue();
		}
		order.setDeliveryCost(Double.valueOf(deliveryCostValue));
		// -----------------------------
		// set payment cost - convert if net or currency is different
		double paymentCostValue = 0.0;
		if(AitpcartConstants.AITP_CART_SWTICH_ON.equalsIgnoreCase(getConfigurationService().getConfiguration().getString(AitpcartConstants.AITP_CART_PAYMENTCOST_SWTICH)))
		{
			final PriceValue payCost = findPaymentCostStrategy.getPaymentCost(order);
			if (payCost != null)
			{
				paymentCostValue = convertPriceIfNecessary(payCost, order.getNet().booleanValue(), order.getCurrency(),
						relativeTaxValues).getValue();
			}
		}
		order.setPaymentCost(Double.valueOf(paymentCostValue));
	}
	
	public FindDeliveryCostStrategy getFindDeliveryCostStrategy() {
		return findDeliveryCostStrategy;
	}

	public void setFindDeliveryCostStrategy(FindDeliveryCostStrategy findDeliveryCostStrategy) {
		this.findDeliveryCostStrategy = findDeliveryCostStrategy;
	}

	public FindPaymentCostStrategy getFindPaymentCostStrategy() {
		return findPaymentCostStrategy;
	}

	public void setFindPaymentCostStrategy(FindPaymentCostStrategy findPaymentCostStrategy) {
		this.findPaymentCostStrategy = findPaymentCostStrategy;
	}

	public List<FindTaxValuesStrategy> getFindTaxesStrategies() {
		return findTaxesStrategies;
	}

	public void setFindTaxesStrategies(List<FindTaxValuesStrategy> findTaxesStrategies) {
		this.findTaxesStrategies = findTaxesStrategies;
	}

	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
