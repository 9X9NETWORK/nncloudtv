package com.nncloudtv.lib;

import java.util.logging.Logger;

import com.nncloudtv.dao.MsoDao;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.service.NnChannelManager;

public class NNF {
    
    protected static final Logger log = Logger.getLogger(NNF.class.getName());
    
    protected static NnChannelManager channelMngr = null;
    protected static MsoConfigManager configMngr  = null;
    protected static MsoManager       msoMngr     = null;
    
    protected static MsoDao           msoDao      = null;
    
    public static MsoDao getMsoDao() {
        
        if (msoDao == null) {
            
            log.info("create msoDao");
            msoDao = new MsoDao();
        }
        
        return msoDao;
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
    
}
