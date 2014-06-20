package com.nncloudtv.mock.lib;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.mock.service.MockMsoConfigManager;
import com.nncloudtv.mock.service.MockNnChannelManager;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.NnChannelManager;

public class MockNNF extends NNF {
    
    public static void initAll() {
        
        getChannelMngr();
        getConfigMngr();
        
    }
    
    public static NnChannelManager getChannelMngr() {
        
        if (channelMngr == null) {
            
            log.info("create mockChannelMngr");
            channelMngr = new MockNnChannelManager();
        }
        
        return channelMngr;
    }
    
    public static MsoConfigManager getConfigMngr() {
        
        if (configMngr == null) {
            
            log.info("create mockConfigMngr");
            configMngr = new MockMsoConfigManager();
        }
        
        return configMngr;
    }
}
