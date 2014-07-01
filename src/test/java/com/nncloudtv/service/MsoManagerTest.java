package com.nncloudtv.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.nncloudtv.dao.MsoDao;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.NnChannel;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MsoConfigManager.class})
public class MsoManagerTest {
    
    MsoManager msoMngr;
    
    @Mock MsoConfigManager mockConfigMngr;
    @Mock NnChannelManager mockChannelMngr;
    @Mock MsoDao mockMsoDao;
    
    @Before
    public void setUp() {
        msoMngr = new MsoManager(mockConfigMngr, mockMsoDao);
    }
    
    @After
    public void tearDown() {
        mockConfigMngr = null;
        mockChannelMngr = null;
        mockMsoDao = null;
        
        msoMngr = null;
    }
    
    @Test
    public void testIsVliadBrand() {
        
        // input arguments
        final Long msoId = (long) 1;
        Mso mockMso = new Mso(Mso.NAME_CTS, "mock cts mso", "cts@9x9.tv", Mso.TYPE_MSO);
        mockMso.setId(msoId);
        
        NnChannel channel = new NnChannel("name", "intro", "imgUrl");
        channel.setStatus(NnChannel.STATUS_SUCCESS);
        channel.setContentType(NnChannel.CONTENTTYPE_MIXED);
        channel.setPublic(true);
        channel.setSphere(LangTable.LANG_ZH);
        
        // mock data
        MsoConfig supportedRegion = new MsoConfig();
        supportedRegion.setMsoId(msoId);
        supportedRegion.setItem(MsoConfig.SUPPORTED_REGION);
        supportedRegion.setValue("zh 台灣");
        
        List<String> spheres = new ArrayList<String>();
        spheres.add("zh");
        
        // stubs
        when(mockConfigMngr.findByMsoAndItem((Mso) anyObject(), anyString())).thenReturn(supportedRegion);
        
        PowerMockito.mockStatic(MsoConfigManager.class);
        when(MsoConfigManager.parseSupportedRegion(anyString())).thenReturn(spheres);
        
        // execute
        Boolean actual = msoMngr.isValidBrand(channel, mockMso);
        
        // verify
        verify(mockConfigMngr).findByMsoAndItem(mockMso, MsoConfig.SUPPORTED_REGION);
        
        PowerMockito.verifyStatic();
        MsoConfigManager.parseSupportedRegion(supportedRegion.getValue());
        
        assertTrue("The mock mso should be a valid brand of mock channel.", actual);
    }
}
