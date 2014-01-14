package com.nncloudtv.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nncloudtv.mock.dao.MockMsoDao;
import com.nncloudtv.mock.service.MockMsoConfigManager;
import com.nncloudtv.mock.service.MockNnChannelManager;
import com.nncloudtv.model.Mso;

public class MsoManagerTest {
    
    MsoManager msoMngr = new MsoManager();
    
    MockMsoConfigManager mockConfigMngr;
    MockNnChannelManager mockChannelMngr;
    MockMsoDao mockMsoDao;
    
    @Before
    public void setUp() {
        
        mockConfigMngr = new MockMsoConfigManager();
        mockChannelMngr = new MockNnChannelManager();
        mockMsoDao = new MockMsoDao();
        
        msoMngr = new MsoManager(mockConfigMngr, mockChannelMngr, mockMsoDao);
        
    }
    
    @Test
    public void testIsVliadBrand() {
        
        Mso mockMso = new Mso(Mso.NAME_CTS, "mock cts mso", "cts@9x9.tv", Mso.TYPE_MSO);
        
        Assert.assertTrue("The mock mso should be a valid brand of mock channel.", msoMngr.isValidBrand((long)777, mockMso));
        
    }
}
