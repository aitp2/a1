package com.accenture.aitp.tailor.service.strategy;

import java.util.List;

import com.accenture.aitp.tailor.data.ModelMonitoredInfo;

import de.hybris.platform.core.model.ItemModel;

public interface AitpInvalidateStrategy {
	void initializeContext(ItemModel model, ModelMonitoredInfo context);
	List<String> invalidateUrls(ModelMonitoredInfo context);
}
