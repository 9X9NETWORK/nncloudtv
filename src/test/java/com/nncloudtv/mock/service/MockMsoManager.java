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
    public Mso findNNMso() {
        
        Mso nnMso = new Mso(Mso.NAME_9X9, "mock 9x9 mso", "mso@9x9.tv", Mso.TYPE_NN);
        nnMso.setId(1);
        
        return nnMso;
    }
    
    @Override
    public Mso findByName(String name) {
        
        if (name == Mso.NAME_9X9) {
            
            return findNNMso();
        }
        
        return null;
    }
    
    @Override
    public Mso findOneByName(String name) {
        
        return findNNMso();
    }

    @Override
    public Mso getByNameFromCache(String name) {
        
        return findByName(name);
    }
    
}
