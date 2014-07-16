package com.nncloudtv.lib;

import java.util.logging.Logger;

import com.nncloudtv.dao.BillingOrderDao;
import com.nncloudtv.dao.BillingPackageDao;
import com.nncloudtv.dao.BillingProfileDao;
import com.nncloudtv.dao.MsoDao;
import com.nncloudtv.dao.MsoPromotionDao;
import com.nncloudtv.dao.NnChannelDao;
import com.nncloudtv.dao.NnDeviceDao;
import com.nncloudtv.dao.NnEpisodeDao;
import com.nncloudtv.dao.NnProgramDao;
import com.nncloudtv.dao.NnUserDao;
import com.nncloudtv.dao.NnUserPrefDao;
import com.nncloudtv.dao.NnUserProfileDao;
import com.nncloudtv.dao.PoiCampaignDao;
import com.nncloudtv.dao.PoiDao;
import com.nncloudtv.dao.PoiEventDao;
import com.nncloudtv.dao.PoiPointDao;
import com.nncloudtv.dao.SysTagDao;
import com.nncloudtv.dao.SysTagDisplayDao;
import com.nncloudtv.dao.SysTagMapDao;
import com.nncloudtv.dao.YtProgramDao;
import com.nncloudtv.service.BillingOrderManager;
import com.nncloudtv.service.BillingPackageManager;
import com.nncloudtv.service.BillingProfileManager;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.service.MsoNotificationManager;
import com.nncloudtv.service.MsoPromotionManager;
import com.nncloudtv.service.NnChannelManager;
import com.nncloudtv.service.NnChannelPrefManager;
import com.nncloudtv.service.NnDeviceManager;
import com.nncloudtv.service.NnDeviceNotificationManager;
import com.nncloudtv.service.NnEpisodeManager;
import com.nncloudtv.service.NnProgramManager;
import com.nncloudtv.service.NnUserManager;
import com.nncloudtv.service.NnUserPrefManager;
import com.nncloudtv.service.NnUserProfileManager;
import com.nncloudtv.service.PoiCampaignManager;
import com.nncloudtv.service.PoiEventManager;
import com.nncloudtv.service.PoiPointManager;
import com.nncloudtv.service.SysTagDisplayManager;
import com.nncloudtv.service.SysTagManager;
import com.nncloudtv.service.SysTagMapManager;

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
    
    protected static MsoDao            msoDao            = null;
    protected static MsoPromotionDao   msoPromotionDao   = null;
    protected static BillingOrderDao   orderDao          = null;
    protected static BillingProfileDao billingProfileDao = null;
    protected static BillingPackageDao packageDao        = null;
    protected static NnEpisodeDao      episodeDao        = null;
    protected static NnChannelDao      channelDao        = null;
    protected static NnDeviceDao       deviceDao         = null;
    protected static PoiDao            poiDao            = null;
    protected static PoiPointDao       poiPointDao       = null;
    protected static PoiEventDao       poiEventDao       = null;
    protected static PoiCampaignDao    poiCampaignDao    = null;
    protected static NnProgramDao      programDao        = null;
    protected static YtProgramDao      ytProgramDao      = null;
    protected static NnUserDao         userDao           = null;
    protected static NnUserProfileDao  profileDao        = null;
    protected static NnUserPrefDao     prefDao           = null;
    protected static SysTagDao         sysTagDao         = null;
    protected static SysTagDisplayDao  displayDao        = null;
    protected static SysTagMapDao      sysTagMapDao      = null;
    
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
    
    public static PoiCampaignManager getPoiCanpaignMngr() {
        
        if (poiCampaignMngr == null) {
            
            log.info("create poiCampaignMngr");
            poiCampaignMngr = new PoiCampaignManager();
        }
        
        return poiCampaignMngr;
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
}
