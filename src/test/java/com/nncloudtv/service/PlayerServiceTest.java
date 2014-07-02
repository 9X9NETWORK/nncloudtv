package com.nncloudtv.service;

import java.util.Map;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.nncloudtv.mock.lib.MockNNF;
import com.nncloudtv.model.Mso;

public class PlayerServiceTest {
    
    protected static final Logger log = Logger.getLogger(PlayerServiceTest.class.getName());
    
    private PlayerService service;
    
    private MockHttpServletResponse resp;
    
    @Before
    public void setUp() {
        
        MockNNF.initAll();
        
        resp = new MockHttpServletResponse();
        
        service = new PlayerService();
    }
    
    @Test
    public void testPrepareBrand() {
        
        Model mockModel = new ExtendedModelMap();
        mockModel = service.prepareBrand(mockModel, Mso.NAME_9X9, resp);
        
        Map<String, Object> map = mockModel.asMap();
        log.info(map.toString());
        
        Assert.assertTrue("META_FAVICON is not null.", map.containsKey(PlayerService.META_FAVICON) && map.get(PlayerService.META_FAVICON) != null);
        Assert.assertFalse("META_FAVICON is not empty.", ((String)map.get(PlayerService.META_FAVICON)).isEmpty());
        
        Assert.assertTrue("META_TITLE is not null.", map.containsKey(PlayerService.META_TITLE) && map.get(PlayerService.META_TITLE) != null);
        Assert.assertFalse("META_TITLE is not empty.", ((String)map.get(PlayerService.META_TITLE)).isEmpty());
        
        Assert.assertTrue("META_DESCRIPTION is not null.", map.containsKey(PlayerService.META_DESCRIPTION) && map.get(PlayerService.META_DESCRIPTION) != null);
        Assert.assertFalse("META_DESCRIPTION is not empty.", ((String)map.get(PlayerService.META_DESCRIPTION)).isEmpty());
        
        Assert.assertTrue("META_IMAGE is not null.", map.containsKey(PlayerService.META_THUMBNAIL) && map.get(PlayerService.META_THUMBNAIL) != null);
        Assert.assertFalse("META_IMAGE is not empty.", ((String)map.get(PlayerService.META_THUMBNAIL)).isEmpty());
        
    }
}
