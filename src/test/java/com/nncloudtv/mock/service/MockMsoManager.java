package com.nncloudtv.mock.service;

import com.nncloudtv.mock.dao.MockMsoDao;
import com.nncloudtv.model.Mso;
import com.nncloudtv.service.MsoManager;

public class MockMsoManager extends MsoManager {
    
    public MockMsoManager() {
        
        super(new MockMsoDao());
    }
    
    @Override
    public long addMsoVisitCounter(boolean readOnly) {
        
        return 1001;
    }
    
    @Override
    public Mso findNNMso() {
        
        Mso nn = new Mso(Mso.NAME_9X9, "This is a mock mso.", "mock@9x9.tv", Mso.TYPE_NN);
        nn.setId(1);
        nn.setLogoUrl("http://www.mock.com/logo.png");
        nn.setTitle("Mock 9x9");
        
        return nn;
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
