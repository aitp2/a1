package com.accenture.performance.optimization.listener;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.acn.ai.util.client.AiSSLClient;

import de.hybris.platform.catalog.model.synchronization.CatalogVersionSyncJobModel;
import de.hybris.platform.catalog.model.synchronization.CatalogVersionSyncScheduleMediaModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.type.TypeModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.event.events.AfterCronJobFinishedEvent;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.type.TypeService;

public class AfterCatalogVersionSyncCronJobFinishedEventListener extends AbstractEventListener<AfterCronJobFinishedEvent>{

	private final static Logger LOG = Logger.getLogger(AfterCatalogVersionSyncCronJobFinishedEventListener.class);

	private EventService eventService;
	private TypeService typeService;
	private FlexibleSearchService flexibleSearchService;
	private ConfigurationService configurationService;
	
	@Override
	protected void onEvent(AfterCronJobFinishedEvent event) {
		if(configurationService.getConfiguration().getBoolean("ai.optimize.catalogversion.sync.finished.listener.enable", true) && isCatalogVersionSyncJob(event))
		{
			LOG.info("Job code:"+event.getJob());
			LOG.info("CronJob code:"+event.getCronJob());
			LOG.info("CronJob type:"+event.getCronJobType());
			final String jobCode = event.getJob();
			if(StringUtils.isNoneBlank(jobCode))
			{
				FlexibleSearchQuery query = new FlexibleSearchQuery("SELECT {"+CatalogVersionSyncJobModel.PK+"} FROM {"+CatalogVersionSyncJobModel._TYPECODE+"} WHERE {"+CatalogVersionSyncJobModel.CODE+"} = ?code");
				query.addQueryParameter("code", jobCode);
				
				CatalogVersionSyncJobModel jobModel = flexibleSearchService.searchUnique(query);
				LOG.info("CatalogVersionSyncJob code:"+jobCode);
				LOG.info("SourceVersion:"+jobModel.getSourceVersion().getVersion()+" name:"+jobModel.getSourceVersion().getCatalog().getName()+" source catalog id:"+jobModel.getSourceVersion().getCatalog().getId());
				LOG.info("TargetVersion:"+jobModel.getTargetVersion().getVersion()+" name:"+jobModel.getTargetVersion().getCatalog().getName()+" target catalog id:"+jobModel.getTargetVersion().getCatalog().getId());
				
				//TODO check the if the catalog is product catalog or content catalog and make the cached page expired
				///
				
				//TODO acn get the items sync 
				//FlexibleSearchQuery mediaQuery = new FlexibleSearchQuery("SELECT {"+CatalogVersionSyncScheduleMediaModel.PK+"} FROM {"+CatalogVersionSyncScheduleMediaModel._TYPECODE+"} WHERE {"+CatalogVersionSyncScheduleMediaModel.CRONJOB+"} = ?cronJob");
				//mediaQuery.addQueryParameter("cronJob", value);
				final String contentURL = configurationService.getConfiguration().getString("ai.content.cache.url");
				if(StringUtils.isBlank(contentURL))
				{
					LOG.error("value for key:ai.content.cache.url can not be empty");
					return;
				}
				//String jsonParam = "";
				//new AiSSLClient().doPost(contentURL, jsonParam, "utf-8");
			}
		}
		
	}
	
	protected boolean isCatalogVersionSyncJob(AfterCronJobFinishedEvent event) {
		ComposedTypeModel eventJobType = this.typeService.getComposedTypeForCode(event.getJobType());
		ComposedTypeModel syncJob = this.typeService.getComposedTypeForClass(CatalogVersionSyncJobModel.class);
		return this.typeService.isAssignableFrom((TypeModel) syncJob, (TypeModel) eventJobType);
	}

	/**
	 * @return the eventService
	 */
	public EventService getEventService() {
		return eventService;
	}

	/**
	 * @param eventService the eventService to set
	 */
	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

	/**
	 * @return the typeService
	 */
	public TypeService getTypeService() {
		return typeService;
	}

	/**
	 * @param typeService the typeService to set
	 */
	public void setTypeService(TypeService typeService) {
		this.typeService = typeService;
	}

	/**
	 * @return the flexibleSearchService
	 */
	public FlexibleSearchService getFlexibleSearchService() {
		return flexibleSearchService;
	}

	/**
	 * @param flexibleSearchService the flexibleSearchService to set
	 */
	public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
		this.flexibleSearchService = flexibleSearchService;
	}

	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	/**
	 * @param configurationService the configurationService to set
	 */
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

}
