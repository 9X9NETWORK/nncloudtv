package com.nncloudtv.lib;

import java.util.logging.Logger;

import com.nncloudtv.service.NnChannelManager;

public class NNF {
    
    protected static final Logger log = Logger.getLogger(NNF.class.getName());
    
    protected static NnChannelManager channelMngr = null;
    
    public static NnChannelManager get(Class<NnChannelManager> cls) {
        
        return getChannelMngr();
    }
    
    public static NnChannelManager getChannelMngr() {
        
        if (channelMngr == null) {
            
            log.info("create channelMngr");
            channelMngr = new NnChannelManager();
        }
        
        return channelMngr;
    }
    
}
