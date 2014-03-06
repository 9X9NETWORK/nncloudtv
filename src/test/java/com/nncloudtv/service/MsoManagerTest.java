package com.nncloudtv.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nncloudtv.mock.dao.MockMsoDao;
import com.nncloudtv.mock.service.MockMsoConfigManager;
import com.nncloudtv.mock.service.MockNnChannelManager;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;

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
        
        msoMngr = new MsoManager(mockConfigMngr, mockMsoDao);
        
    }
    
    @Test
    public void testIsVliadBrand() {
        
        Mso mockMso = new Mso(Mso.NAME_CTS, "mock cts mso", "cts@9x9.tv", Mso.TYPE_MSO);
        NnChannel channel = new NnChannel("name", "intro", "imgUrl");
        channel.setStatus(NnChannel.STATUS_SUCCESS);
        channel.setContentType(NnChannel.CONTENTTYPE_MIXED);
        channel.setPublic(true);
        channel.setSphere(LangTable.LANG_ZH);
        Assert.assertTrue("The mock mso should be a valid brand of mock channel.", msoMngr.isValidBrand(channel, mockMso));
        
    }
}
