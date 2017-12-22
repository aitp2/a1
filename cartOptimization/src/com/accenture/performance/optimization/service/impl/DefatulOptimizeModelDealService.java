/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2017 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package com.accenture.performance.optimization.service.impl;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.delivery.DeliveryService;
import de.hybris.platform.commerceservices.util.GuidKeyGenerator;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.storelocator.pos.PointOfServiceService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.performance.optimization.facades.data.OptimizedCartData;
import com.accenture.performance.optimization.model.OptimizedCartModel;
import com.accenture.performance.optimization.service.OptimizeModelDealService;


/**
 *
 */
public class DefatulOptimizeModelDealService implements OptimizeModelDealService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefatulOptimizeModelDealService.class);

	protected final static String SELECTCLAUSE = "SELECT {" + OptimizedCartModel.PK + "} FROM {" + OptimizedCartModel._TYPECODE
			+ "}  WHERE 1=1 ";

	protected final static String ORDERBYCLAUSE = " ORDER BY {" + OptimizedCartModel.MODIFIEDTIME + "} DESC";

	protected final static String FIND_CART_FOR_GUID_AND_USER_AND_SITE = SELECTCLAUSE + "AND  {" + OptimizedCartModel.GUID
			+ "} = ?guid" + " AND {" + OptimizedCartModel.USERID + "} = ?user " + ORDERBYCLAUSE;


	protected static final String FIND_PRODUCT_BY_CODE = "SELECT {" + ProductModel.PK + "} FROM {" + ProductModel._TYPECODE
			+ "}  WHERE 1=1 AND {" + ProductModel.CODE + "} = ?code  AND {" + ProductModel.CATALOGVERSION + "} = ?catalogVersion ";

	protected final static String FIND_CART_FOR_USER_AND_SITE = SELECTCLAUSE + " AND {" + OptimizedCartModel.USERID
			+ "} = ?user AND {" + OptimizedCartModel.SITE + "}=?site " + ORDERBYCLAUSE;

	protected final static String FIND_CART_BY_CODE = SELECTCLAUSE + " AND {" + OptimizedCartModel.CODE + "} = ?code ";

	private UserService userService;
	private CommonI18NService commonI18NService;
	private ModelService modelService;
	private KeyGenerator keyGenerator;
	private FlexibleSearchService flexibleSearchService;
	private ProductService productService;
	private PointOfServiceService pointOfServiceService;
	private BaseSiteService baseSiteService;
	private BaseStoreService baseStoreService;
	private GuidKeyGenerator guidKeyGenerator;
	private CustomerAccountService customerAccountService;
	private DeliveryService deliveryService;
	private UnitService unitService;
	private MediaService mediaService;

	@Override
	public OptimizedCartData createSessionCart()
	{
		final OptimizedCartModel cartModel = doCreationCart();
		modelService.save(cartModel);
		return recoverCart(cartModel);
	}

	protected OptimizedCartModel doCreationCart()
	{
		final OptimizedCartModel cartModel = (OptimizedCartModel) modelService.create(OptimizedCartModel.class);
		cartModel.setCode(String.valueOf(keyGenerator.generate()));
		//cartModel.setUserAccessToken(value);
		cartModel.setUserId(getUserService().getCurrentUser().getUid());
		cartModel.setCurrencyCode(commonI18NService.getCurrentCurrency().getIsocode());
		//cartModel.setNet(Boolean.valueOf(getNetGrossStrategy().isNet()));
		cartModel.setSite(getBaseSiteService().getCurrentBaseSite());
		cartModel.setStore(getBaseStoreService().getCurrentBaseStore());
		cartModel.setGuid(getGuidKeyGenerator().generate().toString());
		return cartModel;
	}

	@Override
	public OptimizedCartData restoreOrCreateCurrentCartData()
	{
		final UserModel user = getUserService().getCurrentUser();
		OptimizedCartModel cartModel = getRelatedCartModel(user.getUid(), baseSiteService.getCurrentBaseSite());
		if (cartModel == null)
		{
			cartModel = doCreationCart();
			modelService.save(cartModel);
		}
		return this.recoverCart(cartModel);
	}

	protected OptimizedCartModel getRelatedCartModel(final OptimizedCartData cartData)
	{
		if (cartData != null && cartData.getGuid() != null)
		{
			return this.getCartForGuidAndSiteAndUser(cartData.getGuid(), baseSiteService.getBaseSiteForUID(cartData.getBaseSite()), cartData.getUserId());
		}
		else
		{
			return null;
		}
	}

	protected OptimizedCartModel getRelatedCartModel(final String userid, final BaseSiteModel site)
	{
		if (!StringUtils.isEmpty(userid))
		{
			final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(FIND_CART_FOR_USER_AND_SITE);
			fQuery.addQueryParameter("user", userid);
			fQuery.addQueryParameter("site", site);
			final SearchResult<OptimizedCartModel> optimizedCartModelSearchResult = flexibleSearchService.search(fQuery);
			if (optimizedCartModelSearchResult.getResult() != null && !optimizedCartModelSearchResult.getResult().isEmpty())
			{
				return optimizedCartModelSearchResult.getResult().get(0);
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	public void persistCart(final OptimizedCartData cart)
	{
		OptimizedCartModel cartModel = getRelatedCartModel(cart);
		if (cartModel == null)
		{
			cartModel = this.getModelService().create(OptimizedCartModel.class);
			if (cart.getCode() == null)
			{
				cartModel.setCode(String.valueOf(this.getKeyGenerator().generate()));
				cart.setCode(cartModel.getCode());
			}
			else
			{
				cartModel.setCode(cart.getCode());
			}
		}

		fillCartModelOnly(cart, cartModel);

		if (cartModel.getCartDataMedia() == null)
		{
			final CatalogUnawareMediaModel media = this.modelService.create(CatalogUnawareMediaModel.class);
			media.setCode(cartModel.getCode());
			cartModel.setCartDataMedia(media);
		}

		this.getModelService().saveAll(cartModel);

		try
		{
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			final ObjectOutputStream oos = new ObjectOutputStream(bout);
			oos.writeObject(cart);
			getMediaService().setDataForMedia(cartModel.getCartDataMedia(), bout.toByteArray());
		}
		catch (final Exception e)
		{
			LOG.error(e.getMessage(), e);
		}
	}

	protected OptimizedCartData recoverCart(final OptimizedCartModel cartModel)
	{
		if (cartModel.getCartDataMedia() != null)
		{
			final byte[] alldata = this.getMediaService().getDataFromMedia(cartModel.getCartDataMedia());
			final ByteArrayInputStream bais = new ByteArrayInputStream(alldata);
			try
			{
				final ObjectInputStream ois = new ObjectInputStream(bais);
				return (OptimizedCartData) ois.readObject();
			}
			catch (final Exception e)
			{
				LOG.error(e.getMessage(), e);
			}
		}
		else
		{
			final OptimizedCartData cart = new OptimizedCartData();
			cart.setCode(cartModel.getCode());
			cart.setUserId(cartModel.getUserId());
			//			cart.setCurrencyCode(cartModel.getCurrencyCode());
			cart.setBaseSite(cartModel.getSite().getUid());
			cart.setBaseStore(cartModel.getStore().getUid());
			cart.setGuid(cartModel.getGuid());
			return cart;
		}
		return null;
	}

	protected void fillCartModelOnly(final OptimizedCartData cart, final OptimizedCartModel cartModel)
	{
		updateProperty("currencyCode", commonI18NService.getCurrentCurrency().getIsocode(), cartModel);
		updateProperty("guid", cart.getGuid(), cartModel);
		updateProperty("userId", cart.getUserId(), cartModel);
		updateProperty("site", getBaseSiteService().getBaseSiteForUID(cart.getBaseSite()), cartModel);
		updateProperty("store", getBaseStoreService().getBaseStoreForUid(cart.getBaseStore()), cartModel);
		updateProperty("totalDiscounts", cart.getTotalDiscounts(), cartModel);
		updateProperty("totalPrice", cart.getTotalPrice(), cartModel);

		// cartModel.setEntries(value);
	}

	public static void main(final String[] avg) throws Exception
	{
		//		final OptimizedCartData cart = new OptimizedCartData();
		//
		//		final OptimizedCartModel cart2 = new OptimizedCartModel();
		//		cart.setPaymentCost(Double.valueOf(1.001));
		//		cart2.setPaymentCost(Double.valueOf(1.002));
		//
		//		System.out.println(new DefatulOptimizeModelDealService().updateProperty("paymentCost", cart.getPaymentCost(), cart2));
		//		System.out.println(cart2.getPaymentCost());
		//		boolean result = true;
		//		result = result | false;
		//		System.out.println(result);
		//		result = result | true;
		//		System.out.println(result);

		//		final List<BeanD> all = new ArrayList<>();
		//		{
		//			final BeanD a = new BeanD();
		//			a.setProp1BeanA("abc");
		//			a.setProp1BeanB("bcd");
		//			a.setProp2BeanD(Integer.valueOf(1));
		//			a.setProp1BeanD(new ArrayList<>());
		//			{
		//				final BeanC b = new BeanC();
		//				b.setProp1BeanA("eee1");
		//				b.setProp1BeanB("eee2");
		//				b.setProp2BeanC(12);
		//				b.setProp1BeanC(a);
		//				a.getProp1BeanD().add(b);
		//			}
		//
		//			{
		//				final BeanC b = new BeanC();
		//				b.setProp1BeanA("eee11");
		//				b.setProp1BeanB("eee22");
		//
		//				a.getProp1BeanD().add(b);
		//			}
		//
		//			all.add(a);
		//		}
		//		{
		//			final BeanD a = new BeanD();
		//			a.setProp1BeanA("ddd");
		//			a.setProp1BeanB("ccc");
		//			a.setProp2BeanD(Integer.valueOf(1));
		//			a.setProp1BeanD(new ArrayList<>());
		//
		//			all.add(a);
		//		}
		//
		//		final ObjectMapper mapper = new ObjectMapper();
		//		final String txt = mapper.writeValueAsString(all);
		//		System.out.println(txt);

		//		final Object result = mapper.readValue(txt, List.class);
		//		System.out.println(result.getClass());
	}

	private boolean updateDeliveryAddressModel(final OptimizedCartData cart, final OptimizedCartModel cartModel)
	{
		boolean update = false;
		if (cart.getDeliveryAddress() != null)
		{
			final String newId = cart.getDeliveryAddress().getId();
			if (newId == null)
			{
				// new address
				LOG.error("the cart data address is not a saved address! cart code : " + cart.getCode());
			}
			else if (cartModel.getDeliveryAddress() == null || !newId.equals(cartModel.getDeliveryAddress().getPk().toString()))
			{
				// clone new data model && no save
				final AddressModel newAddress = this.getModelService().get(PK.parse(cart.getDeliveryAddress().getId()));
				final AddressModel toSaveAddress = this.getModelService().clone(newAddress);
				toSaveAddress.setOwner(cartModel);
				if (cartModel.getDeliveryAddress() != null)
				{
					this.getModelService().remove(cartModel.getDeliveryAddress());
				}
				cartModel.setDeliveryAddress(toSaveAddress);
				update = true;
			}
		}
		else if (cartModel.getDeliveryAddress() != null)
		{
			this.getModelService().remove(cartModel.getDeliveryAddress());
			cartModel.setDeliveryAddress(null);
			update = true;
		}
		return update;
	}

	/**
	 *
	 */
	private boolean updateProperty(final String key, final Object value, final Object cartModel)
	{
		try
		{
			final Object old = PropertyUtils.getProperty(cartModel, key);
			if (old == null)
			{
				if (value != null)
				{
					PropertyUtils.setProperty(cartModel, key, value);
					return true;
				}
			}
			else if (!old.equals(value))
			{
				PropertyUtils.setProperty(cartModel, key, value);
				return true;
			}
		}
		catch (final Exception e)
		{
			LOG.error("can't update property of " + key, e);
		}
		return false;
	}

	@Override
	public OptimizedCartData getCartDataForGuidAndSiteAndUser(final String cartguid, final BaseSiteModel currentBaseSite,
			final String currentUser)
	{
		final OptimizedCartModel model = getCartForGuidAndSiteAndUser(cartguid, currentBaseSite, currentUser);
		if (model == null)
		{
			return null;
		}
		return this.recoverCart(model);
	}

	@Override
	public OptimizedCartData getCartDataForCodeAndSiteAndUser(final String cartCode, final BaseSiteModel currentBaseSite,
			final String currentUser)
	{
		final OptimizedCartModel model = getCartForCodeAndSiteAndUser(cartCode, currentBaseSite, currentUser);
		if (model == null)
		{
			return null;
		}
		return this.recoverCart(model);
	}

	private OptimizedCartModel getCartForCodeAndSiteAndUser(final String cartCode, final BaseSiteModel currentBaseSite,
			final String currentUser)
	{

		if (StringUtils.isEmpty(cartCode))
		{
			return getRelatedCartModel(currentUser, currentBaseSite);
		}

		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(FIND_CART_BY_CODE);
		fQuery.addQueryParameter("code", cartCode);

		//fQuery.addQueryParameter("user", currentUser);
		//fQuery.addQueryParameter("site", cartData.getBaseSite());

		final SearchResult<OptimizedCartModel> optimizedCartModelSearchResult = flexibleSearchService.search(fQuery);
		if (optimizedCartModelSearchResult.getResult() != null && !optimizedCartModelSearchResult.getResult().isEmpty())
		{
			return optimizedCartModelSearchResult.getResult().get(0);
		}
		else
		{
			return null;
		}
	}


	private OptimizedCartModel getCartForGuidAndSiteAndUser(final String cartguid, final BaseSiteModel currentBaseSite,
			final String currentUser)
	{
		if (StringUtils.isEmpty(cartguid))
		{
			return getRelatedCartModel(currentUser, currentBaseSite);
		}
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(FIND_CART_FOR_GUID_AND_USER_AND_SITE);
		fQuery.addQueryParameter("guid", cartguid);
		fQuery.addQueryParameter("user", currentUser);
		//fQuery.addQueryParameter("site", cartData.getBaseSite());
		final SearchResult<OptimizedCartModel> optimizedCartModelSearchResult = flexibleSearchService.search(fQuery);
		if (optimizedCartModelSearchResult.getResult() != null && !optimizedCartModelSearchResult.getResult().isEmpty())
		{
			return optimizedCartModelSearchResult.getResult().get(0);
		}
		else
		{
			return null;
		}
	}

	@Override
	public void removePersistentCart(final String cartGuid, final String userid)
	{
		final OptimizedCartModel cartModel = this.getCartForGuidAndSiteAndUser(cartGuid, null, userid);
		if (cartModel != null)
		{
			getModelService().remove(cartModel);
		}
	}

	@Override
	public void removeCurrentSessionCart(final OptimizedCartData cartData)
	{
		removePersistentCart(cartData.getGuid(), cartData.getUserId());
	}

	@Override
	public OptimizedCartData getSessionCart(final String cartGuid)
	{
		//
		return null;
	}

	/**
	 * @return the userService
	 */
	public UserService getUserService()
	{
		return userService;
	}

	/**
	 * @param userService
	 *           the userService to set
	 */
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	/**
	 * @return the commonI18NService
	 */
	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	/**
	 * @param commonI18NService
	 *           the commonI18NService to set
	 */
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	/**
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
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
	 * @return the keyGenerator
	 */
	public KeyGenerator getKeyGenerator()
	{
		return keyGenerator;
	}

	/**
	 * @param keyGenerator
	 *           the keyGenerator to set
	 */
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}

	/**
	 * @return the productService
	 */
	public ProductService getProductService()
	{
		return productService;
	}

	/**
	 * @param productService
	 *           the productService to set
	 */
	public void setProductService(final ProductService productService)
	{
		this.productService = productService;
	}

	/**
	 * @return the pointOfServiceService
	 */
	public PointOfServiceService getPointOfServiceService()
	{
		return pointOfServiceService;
	}

	/**
	 * @param pointOfServiceService
	 *           the pointOfServiceService to set
	 */
	public void setPointOfServiceService(final PointOfServiceService pointOfServiceService)
	{
		this.pointOfServiceService = pointOfServiceService;
	}

	/**
	 * @return the baseSiteService
	 */
	public BaseSiteService getBaseSiteService()
	{
		return baseSiteService;
	}

	/**
	 * @param baseSiteService
	 *           the baseSiteService to set
	 */
	public void setBaseSiteService(final BaseSiteService baseSiteService)
	{
		this.baseSiteService = baseSiteService;
	}

	/**
	 * @return the baseStoreService
	 */
	public BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	/**
	 * @param baseStoreService
	 *           the baseStoreService to set
	 */
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

	/**
	 * @return the guidKeyGenerator
	 */
	public GuidKeyGenerator getGuidKeyGenerator()
	{
		return guidKeyGenerator;
	}

	/**
	 * @param guidKeyGenerator
	 *           the guidKeyGenerator to set
	 */
	public void setGuidKeyGenerator(final GuidKeyGenerator guidKeyGenerator)
	{
		this.guidKeyGenerator = guidKeyGenerator;
	}

	/**
	 * @return the flexibleSearchService
	 */
	public FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	/**
	 * @param flexibleSearchService
	 *           the flexibleSearchService to set
	 */
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	/**
	 * @return the customerAccountService
	 */
	public CustomerAccountService getCustomerAccountService()
	{
		return customerAccountService;
	}

	/**
	 * @param customerAccountService
	 *           the customerAccountService to set
	 */
	public void setCustomerAccountService(final CustomerAccountService customerAccountService)
	{
		this.customerAccountService = customerAccountService;
	}

	/**
	 * @return the deliveryService
	 */
	public DeliveryService getDeliveryService()
	{
		return deliveryService;
	}

	/**
	 * @param deliveryService
	 *           the deliveryService to set
	 */
	public void setDeliveryService(final DeliveryService deliveryService)
	{
		this.deliveryService = deliveryService;
	}

	/**
	 * @return the unitService
	 */
	public UnitService getUnitService()
	{
		return unitService;
	}

	/**
	 * @param unitService
	 *           the unitService to set
	 */
	public void setUnitService(final UnitService unitService)
	{
		this.unitService = unitService;
	}

	/**
	 * @return the mediaService
	 */
	public MediaService getMediaService()
	{
		return mediaService;
	}

	/**
	 * @param mediaService
	 *           the mediaService to set
	 */
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

}
