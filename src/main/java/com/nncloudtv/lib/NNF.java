package com.nncloudtv.lib;

import java.util.logging.Logger;

import com.nncloudtv.dao.MsoDao;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.service.NnChannelManager;
import com.nncloudtv.service.NnChannelPrefManager;
import com.nncloudtv.service.NnUserManager;
import com.nncloudtv.service.NnUserPrefManager;
import com.nncloudtv.service.NnUserProfileManager;

public class NNF {
    
    protected static final Logger log = Logger.getLogger(NNF.class.getName());
    
    protected static NnChannelManager  channelMngr = null;
    protected static MsoConfigManager  configMngr  = null;
    protected static MsoManager        msoMngr     = null;
    protected static NnUserPrefManager prefMngr    = null;
    protected static NnUserManager     userMngr    = null;
    protected static NnUserProfileManager profileMngr = null;
    protected static NnChannelPrefManager chPrefMngr  = null;
    
    protected static MsoDao            msoDao      = null;
    
    public static MsoDao getMsoDao() {
        
        if (msoDao == null) {
            
            log.info("create msoDao");
            msoDao = new MsoDao();
        }
        
        return msoDao;
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
