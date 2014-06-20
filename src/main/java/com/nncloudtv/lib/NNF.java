package com.nncloudtv.lib;

import java.util.logging.Logger;

import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.NnChannelManager;

public class NNF {
    
    protected static final Logger log = Logger.getLogger(NNF.class.getName());
    
    protected static NnChannelManager channelMngr = null;
    protected static MsoConfigManager configMngr = null;
    
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
