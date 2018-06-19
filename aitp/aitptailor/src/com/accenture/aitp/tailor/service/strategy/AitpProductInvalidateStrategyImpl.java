package com.accenture.aitp.tailor.service.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.accenture.aitp.tailor.data.ModelMonitoredInfo;
import com.accenture.aitp.tailor.parser.AitpCacheKeyParser;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;

public class AitpProductInvalidateStrategyImpl implements AitpInvalidateStrategy {
	private Map<String, String> initPropMap;
	private List<AitpCacheKeyParser> parsers;
	
	private static final Logger LOG = Logger.getLogger(AitpProductInvalidateStrategyImpl.class);
	
	@Override
	public void initializeContext(ItemModel model, ModelMonitoredInfo modelContext) {
		ExpressionParser parser = new SpelExpressionParser();
		EvaluationContext evalContext = new StandardEvaluationContext(model);
		this.getInitPropMap().forEach((key, value) -> {
			Object obj = null;
			
			try {
				obj = parser.parseExpression(value).getValue(evalContext, Object.class);
			} catch (Exception e) {
				LOG.warn("can't get value from : " + value);
			}
			if(obj != null) {
				modelContext.put(key,obj);
			} else {
				LOG.warn("can't get the value for key : " + key);
			}
		});
	}

	public List<AitpCacheKeyParser> getParsers() {
		return parsers;
	}

	public void setParsers(List<AitpCacheKeyParser> parsers) {
		this.parsers = parsers;
	}

	@Override
	public List<String> invalidateUrls(ModelMonitoredInfo context) {
		List<String> allUrls = new ArrayList<>();
		this.getParsers().forEach(parser->allUrls.addAll(parser.invalidateUrl(context)));
		return allUrls;
	}

	public Map<String, String> getInitPropMap() {
		return initPropMap;
	}

	public void setInitPropMap(Map<String, String> initPropMap) {
		this.initPropMap = initPropMap;
	}

}
