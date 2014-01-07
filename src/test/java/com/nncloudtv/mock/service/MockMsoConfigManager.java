package com.nncloudtv.mock.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.service.MsoConfigManager;

public class MockMsoConfigManager extends MsoConfigManager {
    
    protected static final Logger log = Logger.getLogger(MockMsoConfigManager.class.getName());
    
    @Override
    public boolean isQueueEnabled(boolean cacheReset) {
        
        return true;
    }
    
    @Override
    public boolean isInReadonlyMode(boolean cacheReset) {
        
        return false;
    }
    
    @Override
    public List<MsoConfig> findByMso(Mso mso) {
        
        return new ArrayList<MsoConfig>();
    }
    
    @Override
    public MsoConfig findByMsoAndItem(Mso mso, String item) {
        
        if (mso.getName() == Mso.NAME_9X9 && item == MsoConfig.FAVICON_URL) {
            
            MsoConfig config = new MsoConfig();
            config.setItem(MsoConfig.FAVICON_URL);
            config.setValue("http://www.mock.com/favicon.ico");
            log.info("mock favicon url = " + config.getValue());
            
            return config;
        }
        
        return null;
    }
    
}
