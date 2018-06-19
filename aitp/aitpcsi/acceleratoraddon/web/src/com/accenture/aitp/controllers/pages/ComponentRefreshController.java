/**
 *
 */
package com.accenture.aitp.controllers.pages;

import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractPageController;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.contents.components.AbstractCMSComponentModel;
import de.hybris.platform.cms2.servicelayer.services.CMSComponentService;

import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * @author junbin.liu
 *
 */
@Controller
@RequestMapping(value = "/ajax/refresh")
public class ComponentRefreshController extends AbstractPageController
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(ComponentRefreshController.class);

	@Resource(name = "cmsComponentService")
	private CMSComponentService cmsComponentService;

	@RequestMapping(value = "/{codes:.*}", method = RequestMethod.GET)
	public String productDetail(@PathVariable("codes") final String componentCodes, final Model model,
			final HttpServletRequest request, final HttpServletResponse response)
			throws CMSItemNotFoundException, UnsupportedEncodingException
	{
		try
		{
			final AbstractCMSComponentModel component = this.getCmsComponentService().getAbstractCMSComponent(componentCodes);
			if (component != null)
			{
				model.addAttribute("feature", component);
			}
		}
		catch (final Exception e)
		{
			LOG.error(e);
			return "addon:/aitpcsi/pages/" + componentCodes;
		}

		return "addon:/aitpcsi/pages/fetchComponent";
	}

	/**
	 * @return the cmsComponentService
	 */
	public CMSComponentService getCmsComponentService()
	{
		return cmsComponentService;
	}

	/**
	 * @param cmsComponentService
	 *           the cmsComponentService to set
	 */
	public void setCmsComponentService(final CMSComponentService cmsComponentService)
	{
		this.cmsComponentService = cmsComponentService;
	}

}
