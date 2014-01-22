package com.nncloudtv.mock.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.web.api.ApiContext;

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
        
        MsoConfig config = new MsoConfig();
        config.setItem(item);
        
        if (mso.getName() == Mso.NAME_9X9) {
            
            if (item == MsoConfig.FAVICON_URL) {
                
                config.setValue("http://www.mock.com/favicon.ico");
                log.info("[MOCK] favicon url = " + config.getValue());
                
                return config;
            }
            
        } else if (mso.getName() == Mso.NAME_CTS) {
            
            if (item == MsoConfig.SUPPORTED_REGION) {
                
                String supportedRegion = "zh 中文";
                config.setValue(supportedRegion);
                log.info("[MOCK] supported region = " + supportedRegion);
                
                return config;
            }
            
        }
        
        return null;
    }
    
    @Override
    public MsoConfig findByItem(String item) {
        
        MsoConfig config = new MsoConfig();
        
        if (item == MsoConfig.API_MINIMAL) {
            
            config.setItem(MsoConfig.API_MINIMAL);
            config.setValue("" + ApiContext.DEFAULT_VERSION);
            log.info("[MOCK] default version = " + config.getValue());
            
            return config;
            
        } else if (item == MsoConfig.RO) {
            
            config.setItem(MsoConfig.RO);
            config.setValue("0");
            log.info("[MOCK] RO = 0");
            
            return config;
        }
        
        return null;
    }
    
    @Override
    public String getFacebookInfo(String type, Mso mso) {
        
        if (mso == null || type == null) {
            return null;
        }
        if (type == MsoConfig.FACEBOOK_CLIENTID)
            return getProperty("facebook.properties", "facebook_clientid");
        if (type == MsoConfig.FACEBOOK_APPTOKEN)
            return getProperty("facebook.properties", "facebook_apptoken");
        if (type == MsoConfig.FACEBOOK_CLIENTSECRET)
            return getProperty("facebook.properties", "facebook_client_secret");
        return null;
    }
    
}
