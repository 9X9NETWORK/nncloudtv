package com.nncloudtv.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nncloudtv.mock.lib.MockNNF;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;

public class MsoManagerTest {
    
    
    @Before
    public void setUp() {
        
        MockNNF.initAll();
    }
    
    @Test
    public void testIsVliadBrand() {
        
        Mso mockMso = new Mso(Mso.NAME_CTS, "mock cts mso", "cts@9x9.tv", Mso.TYPE_MSO);
        NnChannel channel = new NnChannel("name", "intro", "imgUrl");
        channel.setStatus(NnChannel.STATUS_SUCCESS);
        channel.setContentType(NnChannel.CONTENTTYPE_MIXED);
        channel.setPublic(true);
        channel.setSphere(LangTable.LANG_ZH);
        Assert.assertTrue("The mock mso should be a valid brand of mock channel.", MockNNF.getMsoMngr().isValidBrand(channel, mockMso));
        
    }
}
