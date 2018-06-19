/**
 *
 */
package com.accenture.aitp.controllers;

import de.hybris.platform.acceleratorstorefrontcommons.controllers.cms.AbstractCMSComponentController;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.AttributeNotSupportedException;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.accenture.aitp.model.components.CSIComponentModel;


/**
 * @author junbin.liu
 *
 */
@Controller("CSIComponentController")
@RequestMapping(value = "/view/CSIComponentController")
public class CSIComponentController extends AbstractCMSComponentController<CSIComponentModel>
{
	@Resource(name = "modelService")
	private ModelService modelService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.hybris.platform.acceleratorstorefrontcommons.controllers.cms.AbstractCMSComponentController#fillModel(javax.
	 * servlet.http.HttpServletRequest, org.springframework.ui.Model,
	 * de.hybris.platform.cms2.model.contents.components.AbstractCMSComponentModel)
	 */
	@Override
	protected void fillModel(final HttpServletRequest request, final Model model, final CSIComponentModel component)
	{
		for (final String property : getCmsComponentService().getReadableEditorProperties(component))
		{
			try
			{
				final Object value = modelService.getAttributeValue(component, property);
				model.addAttribute(property, value);
			}
			catch (final AttributeNotSupportedException ignore)
			{
				// ignore
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.hybris.platform.acceleratorstorefrontcommons.controllers.cms.AbstractCMSComponentController#getView(de.hybris.
	 * platform.cms2.model.contents.components.AbstractCMSComponentModel)
	 */
	@Override
	protected String getView(final CSIComponentModel component)
	{
		return configurationService.getConfiguration().getString("aiptcsicomponent.view.url");
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @param configurationService
	 *           the configurationService to set
	 */
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}
}
