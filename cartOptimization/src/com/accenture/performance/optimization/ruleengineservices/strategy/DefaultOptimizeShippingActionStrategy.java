package com.accenture.performance.optimization.ruleengineservices.strategy;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.math.BigDecimal;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.data.OptimizedPromotionResultData;
import com.accenture.performance.optimization.data.OptimizedRuleBasedOrderChangeDeliveryModeAction;
import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.ruleengineservices.service.OptimizePromotionActionService;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.order.daos.DeliveryModeDao;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.ShipmentRAO;
import de.hybris.platform.servicelayer.util.ServicesUtil;

public class DefaultOptimizeShippingActionStrategy extends AbstractOptimizeRuleActionStrategy<OptimizedRuleBasedOrderChangeDeliveryModeAction> 
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultOptimizeShippingActionStrategy.class);
	private DeliveryModeDao deliveryModeDao;
	
	@Override
	public List<OptimizedPromotionResultData> apply(AbstractRuleActionRAO action) {
		if (!(action instanceof ShipmentRAO)) {
			LOG.error("cannot apply {}, action is not of type ShipmentRAO, but {}",
					(Object) this.getClass().getSimpleName(), (Object) action);
			return Collections.emptyList();
		}
		ShipmentRAO changeDeliveryMethodAction = (ShipmentRAO) action;
		if (!(changeDeliveryMethodAction.getAppliedToObject() instanceof CartRAO)) {
			LOG.error("cannot apply {}, appliedToObject is not of type CartRAO, but {}",
					(Object) this.getClass().getSimpleName(), (Object) action.getAppliedToObject());
			return Collections.emptyList();
		}
		OptimizedPromotionResultData promoResult = ((OptimizePromotionActionService)this.getPromotionActionService()).createPromotionResultData(action);
		if (promoResult == null) {
			LOG.error("cannot apply {}, promotionResult could not be created.",
					(Object) this.getClass().getSimpleName());
			return Collections.emptyList();
		}
		
		OptimizedCartData order = promoResult.getCart();
		if (Objects.isNull((Object) order)) {
			LOG.error("cannot apply {}, order or cart not found: {}", (Object) this.getClass().getSimpleName(),
					(Object) order);
			/*if (this.getModelService().isNew((Object) promoResult)) {
				this.getModelService().detach((Object) promoResult);
			}*/
			return Collections.emptyList();
		}
		ShipmentRAO shipmentRAO = (ShipmentRAO) action;
		DeliveryModeModel shipmentModel = this.getDeliveryModeForCode(shipmentRAO.getMode().getCode());
		if (shipmentModel == null) {
			LOG.error("Delivery Mode for code {} not found!", (Object) shipmentRAO.getMode());
			return Collections.emptyList();
		}
		
		
		String shipmentModelToReplace = order.getDeliveryMode();
		order.setDeliveryMode(shipmentModel.getCode());
		Double deliveryCostToReplace = order.getDeliveryCost();
		order.setDeliveryCost(Double.valueOf(shipmentRAO.getMode().getCost().doubleValue()));
		OptimizedRuleBasedOrderChangeDeliveryModeAction actionModel = (OptimizedRuleBasedOrderChangeDeliveryModeAction) this
				.createPromotionAction(promoResult, action);
		
		this.handleActionMetadata(action,actionModel);
		
		actionModel.setDeliveryMode(shipmentModel.getCode());
		actionModel.setDeliveryCost(shipmentRAO.getMode().getCost());
		actionModel.setReplacedDeliveryMode(shipmentModelToReplace);
		actionModel.setReplacedDeliveryCost(BigDecimal.valueOf(deliveryCostToReplace));
		
		this.getModelService().saveAll(new Object[]{promoResult, actionModel, order});
		return Collections.singletonList(promoResult);
	}
	
	protected DeliveryModeModel getDeliveryModeForCode(String code) {
		ServicesUtil.validateParameterNotNull((Object) code, (String) "Parameter code cannot be null");
		List<DeliveryModeModel> deliveryModes = this.getDeliveryModeDao().findDeliveryModesByCode(code);
		return CollectionUtils.isNotEmpty((Collection<DeliveryModeModel>) deliveryModes) ? (DeliveryModeModel) deliveryModes.get(0) : null;
	}
	
	@Override
	public void undo(ItemModel var1) {
		// TODO acn
		LOG.error("TODO: no implement of undo!");
		
	}

	/**
	 * @return the deliveryModeDao
	 */
	public DeliveryModeDao getDeliveryModeDao() {
		return deliveryModeDao;
	}

	/**
	 * @param deliveryModeDao the deliveryModeDao to set
	 */
	public void setDeliveryModeDao(DeliveryModeDao deliveryModeDao) {
		this.deliveryModeDao = deliveryModeDao;
	}

}
