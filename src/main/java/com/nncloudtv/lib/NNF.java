package com.nncloudtv.lib;

import java.util.logging.Logger;

import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import com.nncloudtv.dao.AdPlacementDao;
import com.nncloudtv.dao.AppDao;
import com.nncloudtv.dao.BillingOrderDao;
import com.nncloudtv.dao.BillingPackageDao;
import com.nncloudtv.dao.BillingProfileDao;
import com.nncloudtv.dao.CaptchaDao;
import com.nncloudtv.dao.CounterDao;
import com.nncloudtv.dao.CounterShardDao;
import com.nncloudtv.dao.MsoConfigDao;
import com.nncloudtv.dao.MsoDao;
import com.nncloudtv.dao.MsoIpgDao;
import com.nncloudtv.dao.MsoNotificationDao;
import com.nncloudtv.dao.MsoPromotionDao;
import com.nncloudtv.dao.NnChannelDao;
import com.nncloudtv.dao.NnChannelPrefDao;
import com.nncloudtv.dao.NnContentDao;
import com.nncloudtv.dao.NnDeviceDao;
import com.nncloudtv.dao.NnDeviceNotificationDao;
import com.nncloudtv.dao.NnEpisodeDao;
import com.nncloudtv.dao.NnGuestDao;
import com.nncloudtv.dao.NnItemDao;
import com.nncloudtv.dao.NnProgramDao;
import com.nncloudtv.dao.NnPurchaseDao;
import com.nncloudtv.dao.NnUserChannelSortingDao;
import com.nncloudtv.dao.NnUserDao;
import com.nncloudtv.dao.MyLibraryDao;
import com.nncloudtv.dao.NnUserPrefDao;
import com.nncloudtv.dao.NnUserProfileDao;
import com.nncloudtv.dao.NnUserReportDao;
import com.nncloudtv.dao.NnUserShareDao;
import com.nncloudtv.dao.NnUserSubscribeDao;
import com.nncloudtv.dao.NnUserSubscribeGroupDao;
import com.nncloudtv.dao.NnUserWatchedDao;
import com.nncloudtv.dao.PoiCampaignDao;
import com.nncloudtv.dao.PoiDao;
import com.nncloudtv.dao.PoiEventDao;
import com.nncloudtv.dao.PoiPointDao;
import com.nncloudtv.dao.StoreListingDao;
import com.nncloudtv.dao.SysTagDao;
import com.nncloudtv.dao.SysTagDisplayDao;
import com.nncloudtv.dao.SysTagMapDao;
import com.nncloudtv.dao.TagDao;
import com.nncloudtv.dao.TitleCardDao;
import com.nncloudtv.dao.YtChannelDao;
import com.nncloudtv.dao.YtProgramDao;
import com.nncloudtv.service.AdPlacementManager;
import com.nncloudtv.service.BillingOrderManager;
import com.nncloudtv.service.BillingPackageManager;
import com.nncloudtv.service.BillingProfileManager;
import com.nncloudtv.service.BillingService;
import com.nncloudtv.service.CategoryService;
import com.nncloudtv.service.ContentWorkerService;
import com.nncloudtv.service.DepotService;
import com.nncloudtv.service.EmailService;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.service.MsoNotificationManager;
import com.nncloudtv.service.MsoPromotionManager;
import com.nncloudtv.service.NnChannelManager;
import com.nncloudtv.service.NnChannelPrefManager;
import com.nncloudtv.service.NnDeviceManager;
import com.nncloudtv.service.NnDeviceNotificationManager;
import com.nncloudtv.service.NnEpisodeManager;
import com.nncloudtv.service.NnItemManager;
import com.nncloudtv.service.NnProgramManager;
import com.nncloudtv.service.MyLibraryManager;
import com.nncloudtv.service.NnPurchaseManager;
import com.nncloudtv.service.NnUserManager;
import com.nncloudtv.service.NnUserPrefManager;
import com.nncloudtv.service.NnUserProfileManager;
import com.nncloudtv.service.NotificationService;
import com.nncloudtv.service.PlayerApiService;
import com.nncloudtv.service.PoiCampaignManager;
import com.nncloudtv.service.PoiEventManager;
import com.nncloudtv.service.PoiManager;
import com.nncloudtv.service.PoiPointManager;
import com.nncloudtv.service.SetService;
import com.nncloudtv.service.StoreListingManager;
import com.nncloudtv.service.SysTagDisplayManager;
import com.nncloudtv.service.SysTagManager;
import com.nncloudtv.service.SysTagMapManager;
import com.nncloudtv.service.TitleCardManager;
import com.nncloudtv.service.YtChannelManager;
import com.nncloudtv.service.YtProgramManager;

public class NNF {
    
    protected static final Logger log = Logger.getLogger(NNF.class.getName());
    
    protected static NnChannelManager       channelMngr         = null;
    protected static MsoConfigManager       configMngr          = null;
    protected static MsoManager             msoMngr             = null;
    protected static MsoNotificationManager msoNotiMngr         = null;
    protected static NnUserPrefManager      prefMngr            = null;
    protected static NnUserManager          userMngr            = null;
    protected static NnUserProfileManager   profileMngr         = null;
    protected static NnChannelPrefManager   chPrefMngr          = null;
    protected static BillingProfileManager  billingProfileMngr  = null;
    protected static BillingOrderManager    orderMngr           = null;
    protected static BillingPackageManager  packageMngr         = null;
    protected static NnEpisodeManager       episodeMngr         = null;
    protected static NnProgramManager       programMngr         = null;
    protected static NnDeviceManager        deviceMngr          = null;
    protected static NnDeviceNotificationManager deviceNotiMngr = null;
    protected static SysTagDisplayManager   displayMngr         = null;
    protected static SysTagMapManager       sysTagMapMngr       = null;
    protected static SysTagManager          sysTagMngr          = null;
    protected static MsoPromotionManager    msoPromotionMngr    = null;
    protected static PoiPointManager        poiPointMngr        = null;
    protected static PoiEventManager        poiEventMngr        = null;
    protected static PoiCampaignManager     poiCampaignMngr     = null;
    protected static PoiManager             poiMngr             = null;
    protected static AdPlacementManager     adMngr              = null;
    protected static YtProgramManager       ytProgramMngr       = null;
    protected static YtChannelManager       ytChannelMngr       = null;
    protected static StoreListingManager    storeListingMngr    = null;
    protected static MyLibraryManager       libraryMngr         = null;
    protected static NnPurchaseManager      purchaseMngr        = null;
    protected static NnItemManager          itemMngr            = null;
    protected static TitleCardManager       titleCardMngr       = null;
    
    protected static SetService        setService       = null;
    protected static CategoryService   categoryService  = null;
    protected static DepotService      depotService     = null;
    protected static EmailService      emailService     = null;
    protected static NotificationService notiService    = null;
    protected static BillingService    billingService   = null;
    protected static PlayerApiService  playerApiService = null;
    protected static ContentWorkerService workerService = null;
    
    protected static MsoDao             msoDao             = null;
    protected static MsoPromotionDao    msoPromotionDao    = null;
    protected static BillingOrderDao    orderDao           = null;
    protected static BillingProfileDao  billingProfileDao  = null;
    protected static BillingPackageDao  packageDao         = null;
    protected static NnEpisodeDao       episodeDao         = null;
    protected static NnChannelDao       channelDao         = null;
    protected static NnDeviceDao        deviceDao          = null;
    protected static PoiDao             poiDao             = null;
    protected static PoiPointDao        poiPointDao        = null;
    protected static PoiEventDao        poiEventDao        = null;
    protected static PoiCampaignDao     poiCampaignDao     = null;
    protected static NnProgramDao       programDao         = null;
    protected static YtProgramDao       ytProgramDao       = null;
    protected static NnUserDao          userDao            = null;
    protected static NnUserProfileDao   profileDao         = null;
    protected static NnUserPrefDao      prefDao            = null;
    protected static SysTagDao          sysTagDao          = null;
    protected static SysTagDisplayDao   displayDao         = null;
    protected static SysTagMapDao       sysTagMapDao       = null;
    protected static AdPlacementDao     adDao              = null;
    protected static MyLibraryDao       libraryDao         = null;
    protected static NnPurchaseDao      purchaseDao        = null;
    protected static NnItemDao          itemDao            = null;
    protected static TitleCardDao       titleCardDao       = null;
    protected static NnChannelPrefDao   chPrefDao          = null;
    protected static AppDao             appDao             = null;
    protected static CounterDao         counterDao         = null;
    protected static CounterShardDao    shardDao           = null;
    protected static CaptchaDao         captchaDao         = null;
    protected static MsoConfigDao       configDao          = null;
    protected static MsoIpgDao          msoIpgDao          = null;
    protected static MsoNotificationDao msoNotiDao         = null;
    protected static NnContentDao       contentDao         = null;
    protected static NnDeviceNotificationDao deviceNotiDao = null;
    protected static NnGuestDao         guestDao           = null;
    protected static NnUserChannelSortingDao sortingDao    = null;
    protected static NnUserReportDao    reportDao          = null;
    protected static NnUserShareDao     shareDao           = null;
    protected static NnUserSubscribeGroupDao subscribeGroupDao = null;
    protected static NnUserSubscribeDao subscribeDao       = null;
    protected static NnUserWatchedDao   watchedDao         = null;
    protected static StoreListingDao    storeListingDao    = null;
    protected static TagDao             tagDao             = null;
    protected static YtChannelDao       ytChannelDao       = null;
    
    protected static ConcurrentTaskScheduler scheduler     = null;
    
    public static ConcurrentTaskScheduler getScheduler() {
        
        if (scheduler == null) {
            
            log.info("create scheduler");
            scheduler = new ConcurrentTaskScheduler();
        }
        
        return scheduler;
    }
    
    public static NnEpisodeDao getEpisodeDao() {
        
        if (episodeDao == null) {
            
            log.info("create episodeDao");
            episodeDao = new NnEpisodeDao();
        }
        
        return episodeDao;
    }
    
    public static MsoDao getMsoDao() {
        
        if (msoDao == null) {
            
            log.info("create msoDao");
            msoDao = new MsoDao();
        }
        
        return msoDao;
    }
    
    public static NnChannelDao getChannelDao() {
        
        if (channelDao == null) {
            
            log.info("create channelDao");
            channelDao = new NnChannelDao();
        }
        
        return channelDao;
    }
    
    public static BillingOrderDao getOrderDao() {
        
        if (orderDao == null) {
            
            log.info("create orderDao");
            orderDao = new BillingOrderDao();
        }
        
        return orderDao;
    }
    
    public static BillingProfileDao getBillingProfileDao() {
        
        if (billingProfileDao == null) {
            
            log.info("create billingProfileDao");
            billingProfileDao = new BillingProfileDao();
        }
        
        return billingProfileDao;
    }
    
    public static BillingPackageDao getPackageDao() {
        
        if (packageDao == null) {
            
            log.info("create packageDao");
            packageDao = new BillingPackageDao();
        }
        
        return packageDao;
    }
    
    public static NnDeviceDao getDeviceDao() {
        
        if (deviceDao == null) {
            
            log.info("create deviceDao");
            deviceDao = new NnDeviceDao();
        }
        
        return deviceDao;
    }
    
    public static MsoPromotionDao getMsoPromotionDao() {
        
        if (msoPromotionDao == null) {
            
            log.info("create msoPromotionDao");
            msoPromotionDao = new MsoPromotionDao();
        }
        
        return msoPromotionDao;
    }
    
    public static PoiCampaignDao getPoiCampaignDao() {
        
        if (poiCampaignDao == null) {
            
            log.info("create poiCampaignDao");
            poiCampaignDao = new PoiCampaignDao();
        }
        
        return poiCampaignDao;
    }
    
    public static PoiEventDao getPoiEventDao() {
        
        if (poiEventDao == null) {
            
            log.info("create poiEventDao");
            poiEventDao = new PoiEventDao();
        }
        
        return poiEventDao;
    }
    
    public static PoiPointDao getPoiPointDao() {
        
        if (poiPointDao == null) {
            
            log.info("create poiPointDao");
            poiPointDao = new PoiPointDao();
        }
        
        return poiPointDao;
    }
    
    public static PoiDao getPoiDao() {
        
        if (poiDao == null) {
            
            log.info("create poiDao");
            poiDao = new PoiDao();
        }
        
        return poiDao;
    }
    
    public static NnProgramDao getProgramDao() {
        
        if (programDao == null) {
            
            log.info("create programDao");
            programDao = new NnProgramDao();
        }
        
        return programDao;
    }
    
    public static YtProgramDao getYtProgramDao() {
        
        if (ytProgramDao == null) {
            
            log.info("create ytProgramDao");
            ytProgramDao = new YtProgramDao();
        }
        
        return ytProgramDao;
    }
    
    public static NnUserDao getUserDao() {
        
        if (userDao == null) {
            
            log.info("create userDao");
            userDao = new NnUserDao();
        }
        
        return userDao;
    }
    
    public static NnUserProfileDao getProfileDao() {
        
        if (profileDao == null) {
            
            log.info("create profileDao");
            profileDao = new NnUserProfileDao();
        }
        
        return profileDao;
    }
    
    public static NnUserPrefDao getPrefDao() {
        
        if (prefDao == null) {
            
            log.info("create prefDao");
            prefDao = new NnUserPrefDao();
        }
        
        return prefDao;
    }
    
    public static SysTagMapDao getSysTagMapDao() {
        
        if (sysTagMapDao == null) {
            
            log.info("create sysTagMapDao");
            sysTagMapDao = new SysTagMapDao();
        }
        
        return sysTagMapDao;
    }
    
    public static SysTagDisplayDao getDisplayDao() {
        
        if (displayDao == null) {
            
            log.info("create displayDao");
            displayDao = new SysTagDisplayDao();
        }
        
        return displayDao;
    }
    
    public static SysTagDao getSysTagDao() {
        
        if (sysTagDao == null) {
            
            log.info("create sysTagDao");
            sysTagDao = new SysTagDao();
        }
        
        return sysTagDao;
    }
    
    public static AdPlacementDao getAdDao() {
        
        if (adDao == null) {
            
            log.info("create adDao");
            adDao = new AdPlacementDao();
        }
        
        return adDao;
    }
    
    public static MyLibraryDao getLibraryDao() {
        
        if (libraryDao == null) {
            
            log.info("create libraryDao");
            libraryDao = new MyLibraryDao();
        }
        
        return libraryDao;
    }
    
    public static NnPurchaseDao getPurchaseDao() {
        
        if (purchaseDao == null) {
            
            log.info("create purchaseDao");
            purchaseDao = new NnPurchaseDao();
        }
        
        return purchaseDao;
    }
    
    public static NnItemDao getItemDao() {
        
        if (itemDao == null) {
            
            log.info("create itemDao");
            itemDao = new NnItemDao();
        }
        
        return itemDao;
    }
    
    public static TitleCardDao getTitleCardDao() {
        
        if (titleCardDao == null) {
            
            log.info("create titleCardDao");
            titleCardDao = new TitleCardDao();
        }
        
        return titleCardDao;
    }
    
    public static NnChannelPrefDao getChPrefDao() {
        
        if (chPrefDao == null) {
            
            log.info("create chPrefDao");
            chPrefDao = new NnChannelPrefDao();
        }
        
        return chPrefDao;
    }
    
    public static AppDao getAppDao() {
        
        if (appDao == null) {
            
            log.info("create appDao");
            appDao = new AppDao();
        }
        
        return appDao;
    }
    
    public static CounterDao getCounterDao() {
        
        if (counterDao == null) {
            
            log.info("create counterDao");
            counterDao = new CounterDao();
        }
        
        return counterDao;
    }
    
    public static CounterShardDao getShardDao() {
        
        if (shardDao == null) {
            
            log.info("create shardDao");
            shardDao = new CounterShardDao();
        }
        
        return shardDao;
    }
    
    public static CaptchaDao getCaptchaDao() {
        
        if (captchaDao == null) {
            
            log.info("create captchaDao");
            captchaDao = new CaptchaDao();
        }
        
        return captchaDao;
    }
    
    public static MsoConfigDao getConfigDao() {
        
        if (configDao == null) {
            
            log.info("create configDao");
            configDao = new MsoConfigDao();
        }
        
        return configDao;
    }
    
    public static MsoIpgDao getMsoIpgDao() {
        
        if (msoIpgDao == null) {
            
            log.info("create msoIpgDao");
            msoIpgDao = new MsoIpgDao();
        }
        
        return msoIpgDao;
    }
    
    public static MsoNotificationDao getMsoNotiDao() {
        
        if (msoNotiDao == null) {
            
            log.info("create msoNotiDao");
            msoNotiDao = new MsoNotificationDao();
        }
        
        return msoNotiDao;
    }
    
    public static NnContentDao getContentDao() {
        
        if (contentDao == null) {
            
            log.info("create contentDao");
            contentDao = new NnContentDao();
        }
        
        return contentDao;
    }
    
    public static NnDeviceNotificationDao getDeviceNotiDao() {
        
        if (deviceNotiDao == null) {
            
            log.info("create deviceNotiDao");
            deviceNotiDao = new NnDeviceNotificationDao();
        }
        
        return deviceNotiDao;
    }
    
    public static NnGuestDao getGuestDao() {
        
        if (guestDao == null) {
            
            log.info("create guestDao");
            guestDao = new NnGuestDao();
        }
        
        return guestDao;
    }
    
    public static NnUserChannelSortingDao getSortingDao() {
        
        if (sortingDao == null) {
            
            log.info("create sortingDao");
            sortingDao = new NnUserChannelSortingDao();
        }
        
        return sortingDao;
    }
    
    public static NnUserReportDao getReportDao() {
        
        if (reportDao == null) {
            
            log.info("create reportDao");
            reportDao = new NnUserReportDao();
        }
        
        return reportDao;
    }
    
    public static NnUserShareDao getShareDao() {
        
        if (shareDao == null) {
            
            log.info("create shareDao");
            shareDao = new NnUserShareDao();
        }
        
        return shareDao;
    }
    
    public static NnUserSubscribeGroupDao getSubscribeGroupDao() {
        
        if (subscribeGroupDao == null) {
            
            log.info("create subscribeGroup");
            subscribeGroupDao = new NnUserSubscribeGroupDao();
        }
        
        return subscribeGroupDao;
    }
    
    public static NnUserSubscribeDao getSubscribeDao() {
        
        if (subscribeDao == null) {
            
            log.info("create subscribeDao");
            subscribeDao = new NnUserSubscribeDao();
        }
        
        return subscribeDao;
    }
    
    public static NnUserWatchedDao getWatchedDao() {
        
        if (watchedDao == null) {
            
            log.info("create watchedDao");
            watchedDao = new NnUserWatchedDao();
        }
        
        return watchedDao;
    }
    
    public static StoreListingDao getStoreListingDao() {
        
        if (storeListingDao == null) {
            
            log.info("create storeListingDao");
            storeListingDao = new StoreListingDao();
        }
        
        return storeListingDao;
    }
    
    public static TagDao getTagDao() {
        
        if (tagDao == null) {
            
            log.info("create tagDao");
            tagDao = new TagDao();
        }
        
        return tagDao;
    }
    
    public static YtChannelDao getYtChannelDao() {
        
        if (ytChannelDao == null) {
            
            log.info("create ytChannelDao");
            ytChannelDao = new YtChannelDao();
        }
        
        return ytChannelDao;
    }
    
    public static CategoryService getCategoryService() {
        
        if (categoryService == null) {
            
            log.info("create categoryService");
            categoryService = new CategoryService();
        }
        
        return categoryService;
    }
    
    public static SetService getSetService() {
        
        if (setService == null) {
            
            log.info("create setService");
            setService = new SetService();
        }
        
        return setService;
    }
    
    public static DepotService getDepotService() {
        
        if (depotService == null) {
            
            log.info("create depotService");
            depotService = new DepotService();
        }
        
        return depotService;
    }
    
    public static EmailService getEmailService() {
        
        if (emailService == null) {
            
            log.info("create emailService");
            emailService = new EmailService();
        }
        
        return emailService;
    }
    
    public static NotificationService getNotiService() {
        
        if (notiService == null) {
            
            log.info("create notiService");
            notiService = new NotificationService();
        }
        
        return notiService;
    }
    
    public static BillingService getBillingService() {
        
        if (billingService == null) {
            
            log.info("create billingservice");
            billingService = new BillingService();
        }
        
        return billingService;
    }
    
    public static PlayerApiService getPlayerApiService() {
        
        if (playerApiService == null) {
            
            log.info("create playerApiService");
            playerApiService = new PlayerApiService();
        }
        
        return playerApiService;
    }
    
    public static ContentWorkerService getWorkerService() {
        
        if (workerService == null) {
            
            log.info("create workerService");
            workerService = new ContentWorkerService();
        }
        
        return workerService;
    }
    
    public static StoreListingManager getStoreListingMngr() {
        
        if (storeListingMngr == null) {
            
            log.info("create storeListingMngr");
            storeListingMngr = new StoreListingManager();
        }
        
        return storeListingMngr;
    }
    
    public static YtProgramManager getYtProgramMngr() {
        
        if (ytProgramMngr == null) {
            
            log.info("create ytProgramMngr");
            ytProgramMngr = new YtProgramManager();
        }
        
        return ytProgramMngr;
    }
    
    public static AdPlacementManager getAdMngr() {
        
        if (adMngr == null) {
            
            log.info("create adMngr");
            adMngr = new AdPlacementManager();
        }
        
        return adMngr;
    }
    
    public static PoiPointManager getPoiPointMngr() {
        
        if (poiPointMngr == null) {
            
            log.info("create poiPointMngr");
            poiPointMngr = new PoiPointManager();
        }
        
        return poiPointMngr;
    }
    
    public static PoiEventManager getPoiEventMngr() {
        
        if (poiEventMngr == null) {
            
            log.info("create poiEventMngr");
            poiEventMngr = new PoiEventManager();
        }
        
        return poiEventMngr;
    }
    
    public static PoiCampaignManager getPoiCampaignMngr() {
        
        if (poiCampaignMngr == null) {
            
            log.info("create poiCampaignMngr");
            poiCampaignMngr = new PoiCampaignManager();
        }
        
        return poiCampaignMngr;
    }
    
    public static PoiManager getPoiMngr() {
        
        if (poiMngr == null) {
            
            log.info("create poiMngr");
            poiMngr = new PoiManager();
        }
        
        return poiMngr;
    }
    
    public static MsoNotificationManager getMsoNotiMngr() {
        
        if (msoNotiMngr == null) {
            
            log.info("create msoNotiMngr");
            msoNotiMngr = new MsoNotificationManager();
        }
        
        return msoNotiMngr;
    }
    
    public static SysTagDisplayManager getDisplayMngr() {
        
        if (displayMngr == null) {
            
            log.info("create displayMngr");
            displayMngr = new SysTagDisplayManager();
        }
        
        return displayMngr;
    }
    
    public static SysTagManager getSysTagMngr() {
        
        if (sysTagMngr == null) {
            
            log.info("create sysTagMngr");
            sysTagMngr = new SysTagManager();
        }
        
        return sysTagMngr;
    }
    
    public static SysTagMapManager getSysTagMapMngr() {
        
        if (sysTagMapMngr == null) {
            
            log.info("create sysTagMapMngr");
            sysTagMapMngr = new SysTagMapManager();
        }
        
        return sysTagMapMngr;
    }
    
    public static NnDeviceNotificationManager getDeviceNotiMngr() {
        
        if (deviceNotiMngr == null) {
            
            log.info("create deviceNotiMngr");
            deviceNotiMngr = new NnDeviceNotificationManager();
        }
        
        return deviceNotiMngr;
    }
    
    public static NnDeviceManager getDeviceMngr() {
        
        if (deviceMngr == null) {
            
            log.info("create deviceMngr");
            deviceMngr = new NnDeviceManager();
        }
        
        return deviceMngr;
    }
    
    public static NnProgramManager getProgramMngr() {
        
        if (programMngr == null) {
            
            log.info("create programMngr");
            programMngr = new NnProgramManager();
        }
        
        return programMngr;
    }
    
    public static NnEpisodeManager getEpisodeMngr() {
        
        if (episodeMngr == null) {
            
            log.info("create episodeMngr");
            episodeMngr = new NnEpisodeManager();
        }
        
        return episodeMngr;
    }
    
    public static BillingProfileManager getBillingProfileMngr() {
        
        if (billingProfileMngr == null) {
            
            log.info("create billingProfile");
            billingProfileMngr = new BillingProfileManager();
        }
        
        return billingProfileMngr;
    }
    
    public static BillingOrderManager getOrderMngr() {
        
        if (orderMngr == null) {
            
            log.info("crate orderMngr");
            orderMngr = new BillingOrderManager();
        }
        
        return orderMngr;
    }
    
    public static BillingPackageManager getPackageMngr() {
        
        if (packageMngr == null) {
            
            log.info("create packageMngr");
            packageMngr = new BillingPackageManager();
        }
        
        return packageMngr;
    }
    
    public static NnUserPrefManager getPrefMngr() {
        
        if (prefMngr == null) {
            
            log.info("create prefMngr");
            prefMngr = new NnUserPrefManager();
        }
        
        return prefMngr;
    }
    
    public static NnUserManager getUserMngr() {
        
        if (userMngr == null) {
            
            log.info("create userMngr");
            userMngr = new NnUserManager();
        }
        
        return userMngr;
    }
    
    public static MsoManager getMsoMngr() {
        
        if (msoMngr == null) {
            
            log.info("create msoMngr");
            msoMngr = new MsoManager();
        }
        
        return msoMngr;
    }
    
    public static MsoConfigManager getConfigMngr() {
        
        if (configMngr == null) {
            
            log.info("create configMngr");
            configMngr = new MsoConfigManager();
        }
        
        return configMngr;
    }
    
    public static NnChannelManager getChannelMngr() {
        
        if (channelMngr == null) {
            
            log.info("create channelMngr");
            channelMngr = new NnChannelManager();
        }
        
        return channelMngr;
    }
    
    public static NnUserProfileManager getProfileMngr() {
        
        if (profileMngr == null) {
            
            log.info("create profileMngr");
            profileMngr = new NnUserProfileManager();
        }
        
        return profileMngr;
    }
    
    public static NnChannelPrefManager getChPrefMngr() {
        
        if (chPrefMngr == null) {
            
            log.info("create chPrefMngr");
            chPrefMngr = new NnChannelPrefManager();
        }
        
        return chPrefMngr;
    }
    
    public static MsoPromotionManager getMsoPromotionMngr() {
        
        if (msoPromotionMngr == null) {
            
            log.info("create msoPromotionMngr");
            msoPromotionMngr = new MsoPromotionManager();
        }
        return msoPromotionMngr;
    }
    
    public static MyLibraryManager getLibraryMngr() {
        
        if (libraryMngr == null) {
            
            log.info("create libraryMngr");
            libraryMngr = new MyLibraryManager();
        }
        
        return libraryMngr;
    }
    
    public static NnPurchaseManager getPurchaseMngr() {
        
        if (purchaseMngr == null) {
            
            log.info("create purchaseMngr");
            purchaseMngr = new NnPurchaseManager();
        }
        
        return purchaseMngr;
    }
    
    public static NnItemManager getItemMngr() {
        
        if (itemMngr == null) {
            
            log.info("create itemMngr");
            itemMngr = new NnItemManager();
        }
        
        return itemMngr;
    }
    
    public static TitleCardManager getTitleCardMngr() {
        
        if (titleCardMngr == null) {
            
            log.info("create titleCardMngr");
            titleCardMngr = new TitleCardManager();
        }
        
        return titleCardMngr;
    }
    
    public static YtChannelManager getYtChannelMngr() {
        
        if (ytChannelMngr == null) {
            
            log.info("create ytChannelMngr");
            ytChannelMngr = new YtChannelManager();
        }
        
        return ytChannelMngr;
    }
}
