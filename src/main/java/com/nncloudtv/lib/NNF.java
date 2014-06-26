package com.nncloudtv.lib;

import java.util.logging.Logger;

import com.nncloudtv.dao.BillingOrderDao;
import com.nncloudtv.dao.BillingPackageDao;
import com.nncloudtv.dao.BillingProfileDao;
import com.nncloudtv.dao.MsoDao;
import com.nncloudtv.dao.NnChannelDao;
import com.nncloudtv.dao.NnDeviceDao;
import com.nncloudtv.dao.NnEpisodeDao;
import com.nncloudtv.service.BillingOrderManager;
import com.nncloudtv.service.BillingPackageManager;
import com.nncloudtv.service.BillingProfileManager;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.service.NnChannelManager;
import com.nncloudtv.service.NnChannelPrefManager;
import com.nncloudtv.service.NnDeviceManager;
import com.nncloudtv.service.NnDeviceNotificationManager;
import com.nncloudtv.service.NnEpisodeManager;
import com.nncloudtv.service.NnProgramManager;
import com.nncloudtv.service.NnUserManager;
import com.nncloudtv.service.NnUserPrefManager;
import com.nncloudtv.service.NnUserProfileManager;

public class NNF {
    
    protected static final Logger log = Logger.getLogger(NNF.class.getName());
    
    protected static NnChannelManager      channelMngr        = null;
    protected static MsoConfigManager      configMngr         = null;
    protected static MsoManager            msoMngr            = null;
    protected static NnUserPrefManager     prefMngr           = null;
    protected static NnUserManager         userMngr           = null;
    protected static NnUserProfileManager  profileMngr        = null;
    protected static NnChannelPrefManager  chPrefMngr         = null;
    protected static BillingProfileManager billingProfileMngr = null;
    protected static BillingOrderManager   orderMngr          = null;
    protected static BillingPackageManager packageMngr        = null;
    protected static NnEpisodeManager      episodeMngr        = null;
    protected static NnProgramManager      programMngr        = null;
    protected static NnDeviceManager       deviceMngr         = null;
    protected static NnDeviceNotificationManager deviceNotiMngr = null;
    
    protected static MsoDao                msoDao             = null;
    protected static BillingOrderDao       orderDao           = null;
    protected static BillingProfileDao     billingProfileDao  = null;
    protected static BillingPackageDao     packageDao         = null;
    protected static NnEpisodeDao          episodeDao         = null;
    protected static NnChannelDao          channelDao         = null;
    protected static NnDeviceDao           deviceDao          = null;
    
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
}
