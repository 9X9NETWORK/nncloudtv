package com.nncloudtv.mock.service;

import com.nncloudtv.model.Mso;
import com.nncloudtv.service.MsoManager;

public class MockMsoManager extends MsoManager {
    
    public MockMsoManager() {
        
        super(new MockMsoConfigManager());
    }
    
    @Override
    public long addMsoVisitCounter(boolean readOnly) {
        
        return 1001;
    }
    
    @Override
    public String[] getBrandInfoCache(Mso mso, String os) {
        // TODO Auto-generated method stub
        return super.getBrandInfoCache(mso, os);
    }
    
    @Override
    public Mso findNNMso() {
        
        return new Mso(Mso.NAME_9X9, "mock 9x9 mso", "mso@9x9.tv", Mso.TYPE_NN);
    }
    
    @Override
    public Mso findByName(String name) {
        
        if (name == Mso.NAME_9X9) {
            
            return findNNMso();
        }
        
        return null;
    }

}
