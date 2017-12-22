package com.accenture.performance.optimization.payment;

import com.accenture.performance.optimization.service.OptimizeCartService;

import de.hybris.platform.acceleratorservices.payment.strategies.impl.DefaultClientReferenceLookupStrategy;

public class DefaultOptimizationClientReferenceLookupStrategy extends DefaultClientReferenceLookupStrategy {
	private static final String DEFAULT_CLIENTREF_ID = "Default_Client_Ref";
	
	private OptimizeCartService cartService;
	
	@Override
	public String lookupClientReferenceId()
	{
		if (getCartService().hasSessionCart())
		{
			return getCartService().getSessionOptimizedCart().getGuid();
		}
		return DEFAULT_CLIENTREF_ID;
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

}
