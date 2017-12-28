package com.accenture.performance.optimization.facades.impl;

import de.hybris.platform.commercefacades.voucher.exceptions.VoucherOperationException;
import de.hybris.platform.couponfacades.facades.impl.DefaultCouponFacade;

public class DefaultOptimizeCouponFacadeImpl extends DefaultCouponFacade {
	
	@Override
	public void applyVoucher(String voucherCode) throws VoucherOperationException {
		//TODO acn
        super.applyVoucher(voucherCode);
    }
	
	@Override
	public void releaseVoucher(String voucherCode) throws VoucherOperationException {
		//TODO acn
        super.releaseVoucher(voucherCode);
    }
}
