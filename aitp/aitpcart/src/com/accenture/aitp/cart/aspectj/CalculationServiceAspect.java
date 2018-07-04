package com.accenture.aitp.cart.aspectj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.accenture.aitp.cart.constants.AitpcartConstants;

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.order.strategies.calculation.FindDeliveryCostStrategy;
import de.hybris.platform.order.strategies.calculation.FindPaymentCostStrategy;
import de.hybris.platform.order.strategies.calculation.FindTaxValuesStrategy;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.util.PriceValue;
import de.hybris.platform.util.TaxValue;

@Aspect
public class CalculationServiceAspect 
{
	private static Logger LOGGER = Logger.getLogger(CalculationServiceAspect.class);
	
	private List<FindTaxValuesStrategy> findTaxesStrategies;
	private ConfigurationService configurationService;
	
	@Around("execution(* de.hybris.platform.order.strategies.calculation.impl.*.findTaxValues(..))")
	public Object findTaxValues(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		final Object[] args = joinPoint.getArgs();
		if (args.length == 1)
		{
			if (AitpcartConstants.AITP_CART_SWTICH_OFF.equalsIgnoreCase(getConfigurationService().getConfiguration().getString(AitpcartConstants.AITP_CART_TAX_SWTICH)))
			{
				LOGGER.debug("AITP_CART_TAX_SWTICH CLOSE TAX !");
				return Collections.<TaxValue> emptyList();
			}
		}
		return joinPoint.proceed();
	}
	
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public List<FindTaxValuesStrategy> getFindTaxesStrategies() {
		return findTaxesStrategies;
	}

	public void setFindTaxesStrategies(List<FindTaxValuesStrategy> findTaxesStrategies) {
		this.findTaxesStrategies = findTaxesStrategies;
	}

}
