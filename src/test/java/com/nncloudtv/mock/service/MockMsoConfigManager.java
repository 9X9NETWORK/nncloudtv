package com.nncloudtv.mock.service;

import java.util.ArrayList;
import java.util.List;

import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.service.MsoConfigManager;

public class MockMsoConfigManager extends MsoConfigManager {
    
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
    
}
