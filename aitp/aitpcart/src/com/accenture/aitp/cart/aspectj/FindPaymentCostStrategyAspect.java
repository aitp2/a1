package com.accenture.aitp.cart.aspectj;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.accenture.aitp.cart.constants.AitpcartConstants;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.payment.PaymentMode;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.PriceValue;

@Aspect
public class FindPaymentCostStrategyAspect
{
	private static Logger LOGGER = Logger.getLogger(FindPaymentCostStrategyAspect.class);
	
	private ConfigurationService configurationService;
	private ModelService modelService;
	
	@Around("execution(* de.hybris.platform.order.strategies.calculation.impl.*.getPaymentCost(..))")
	public Object getPaymentCost(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		final Object[] args = joinPoint.getArgs();
		if (args.length == 1)
		{
			final AbstractOrderModel order = (AbstractOrderModel) args[0];
			try
			{
				PaymentModeModel paymentMode = order.getPaymentMode();
				if( paymentMode != null 
						&& AitpcartConstants.AITP_CART_SWTICH_ON.equalsIgnoreCase(getConfigurationService().getConfiguration().getString(AitpcartConstants.AITP_CART_PAYMENTCOST_SWTICH)))
				{
					getModelService().save(order);
		   			final AbstractOrder orderItem = getModelService().getSource(order);
		   			final PaymentMode pModeJalo = getModelService().getSource(paymentMode);
		   			return pModeJalo.getCost(orderItem);
				}
				else
				{
					return new PriceValue(order.getCurrency().getIsocode(), 0.0, order.getNet().booleanValue());
				}
			}
			catch (final Exception e)
			{
				LOGGER.warn("Could not find paymentCost for order [" + order.getCode() + "] due to : " + e + "... skipping!");
				return new PriceValue(order.getCurrency().getIsocode(), 0.0, order.getNet().booleanValue());
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

	public ModelService getModelService() {
		return modelService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}


}
