package com.nncloudtv.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.support.NnTestAll;
import com.nncloudtv.wrapper.NNFWrapper;

@RunWith(MockitoJUnitRunner.class)
@Category(NnTestAll.class)
public class PlayerServiceTest {
    
    protected static final Logger log = Logger.getLogger(PlayerServiceTest.class.getName());
    
    private PlayerService service;
    
    private MockHttpServletResponse resp;
    @Mock private MsoConfigManager mockConfigMngr;
    @Mock private MsoManager mockMsoMngr;
    
    @Before
    public void setUp() {
        
        resp = new MockHttpServletResponse();
        
        service = new PlayerService();
        
        NNFWrapper.setMsoMngr(mockMsoMngr);
        NNFWrapper.setConfigMngr(mockConfigMngr);
    }
    
    @After
    public void tearDown() {
        resp = null;
        mockMsoMngr = null;
        mockConfigMngr = null;
        
        service = null;
    }
    
    @Test
    public void testPrepareBrand() {
        
        // input arguments
        Model mockModel = new ExtendedModelMap();
        final String msoName = Mso.NAME_SYS;
        
        // mock data
        final Long msoId = (long) 1;
        final String title = "title";
        final String logoUrl = "logoUrl";
        Mso mso = new Mso(msoName, "intro", "contactEmail", Mso.TYPE_MSO);
        mso.setId(msoId);
        mso.setTitle(title);
        mso.setLogoUrl(logoUrl);
        
        final String faviconUrl = "faviconUrl";
        MsoConfig config = new MsoConfig(msoId, MsoConfig.FAVICON_URL, faviconUrl);
        
        // stubs
        when(mockMsoMngr.findByName(anyString())).thenReturn(mso);
        when(mockConfigMngr.findByMsoAndItem((Mso) anyObject(), anyString())).thenReturn(config);
        
        // execute
        Model actual = service.prepareBrand(mockModel, msoName, resp);
        
        // verify
        verify(mockMsoMngr).findByName(msoName);
        verify(mockConfigMngr).findByMsoAndItem(mso, MsoConfig.FAVICON_URL);
        
        Map<String, Object> map = actual.asMap();
        log.info(map.toString());
        
        assertTrue("META_FAVICON is not null.", map.containsKey(PlayerService.META_FAVICON) && map.get(PlayerService.META_FAVICON) != null);
        assertFalse("META_FAVICON is not empty.", ((String)map.get(PlayerService.META_FAVICON)).isEmpty());
        
        assertTrue("META_TITLE is not null.", map.containsKey(PlayerService.META_TITLE) && map.get(PlayerService.META_TITLE) != null);
        assertFalse("META_TITLE is not empty.", ((String)map.get(PlayerService.META_TITLE)).isEmpty());
        
        assertTrue("META_DESCRIPTION is not null.", map.containsKey(PlayerService.META_DESCRIPTION) && map.get(PlayerService.META_DESCRIPTION) != null);
        assertFalse("META_DESCRIPTION is not empty.", ((String)map.get(PlayerService.META_DESCRIPTION)).isEmpty());
        
        assertTrue("META_IMAGE is not null.", map.containsKey(PlayerService.META_THUMBNAIL) && map.get(PlayerService.META_THUMBNAIL) != null);
        assertFalse("META_IMAGE is not empty.", ((String)map.get(PlayerService.META_THUMBNAIL)).isEmpty());
        
    }
}