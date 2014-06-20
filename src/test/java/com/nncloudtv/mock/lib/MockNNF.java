package com.nncloudtv.mock.lib;

import com.nncloudtv.dao.MsoDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.mock.dao.MockMsoDao;
import com.nncloudtv.mock.service.MockMsoConfigManager;
import com.nncloudtv.mock.service.MockMsoManager;
import com.nncloudtv.mock.service.MockNnChannelManager;
import com.nncloudtv.mock.service.MockNnUserPrefManager;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.service.NnChannelManager;
import com.nncloudtv.service.NnUserPrefManager;

public class MockNNF extends NNF {
    
    public static void initAll() {
        
        getChannelMngr();
        getConfigMngr();
        getMsoMngr();
        getPrefMngr();
        
        getMsoDao();
    }
    
    public static NnUserPrefManager getPrefMngr() {
        
        if (prefMngr == null) {
            
            log.info("create mockPrefMngr");
            prefMngr = new MockNnUserPrefManager();
        }
        
        return prefMngr;
    }
    
    public static MsoDao getMsoDao() {
        
        if (msoDao == null) {
            
            log.info("create mockMsoDao");
            msoDao = new MockMsoDao();
        }
        
        return msoDao;
    }
    
    public static MsoManager getMsoMngr() {
        
        if (msoMngr == null) {
            
            log.info("create mockMsoMngr");
            msoMngr = new MockMsoManager();
        }
        
        return msoMngr;
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
