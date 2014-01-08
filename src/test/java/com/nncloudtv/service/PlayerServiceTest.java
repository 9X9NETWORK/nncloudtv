package com.nncloudtv.service;

import java.util.Map;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.nncloudtv.mock.service.MockMsoConfigManager;
import com.nncloudtv.mock.service.MockMsoManager;
import com.nncloudtv.mock.service.MockNnUserManager;
import com.nncloudtv.model.Mso;
import com.nncloudtv.web.api.ApiContext;

public class PlayerServiceTest {
    
    protected static final Logger log = Logger.getLogger(PlayerServiceTest.class.getName());
    
    private PlayerService service;
    
    private MockHttpServletRequest req;
    private MockHttpServletResponse resp;
    private MockMsoConfigManager mockConfigMngr;
    private MockNnUserManager mockUserMngr;
    private MockMsoManager mockMsoMngr;
    
    @Before
    public void setUp() {
        
        req = new MockHttpServletRequest();
        resp = new MockHttpServletResponse();
        mockConfigMngr = new MockMsoConfigManager();
        mockUserMngr = new MockNnUserManager();
        mockMsoMngr = new MockMsoManager();
        
        service = new PlayerService(mockUserMngr, mockConfigMngr, mockMsoMngr);
    }
    
    @Test
    public void testPrepareBrand() {
        
        Model mockModel = new ExtendedModelMap();
        mockModel = service.prepareBrand(mockModel, Mso.NAME_9X9, resp);
        
        Map<String, Object> map = mockModel.asMap();
        log.info(map.toString());
        
        Assert.assertTrue("META_FAVICON is not null.", map.containsKey(PlayerService.META_FAVICON));
        Assert.assertFalse("META_FAVICON is not empty.", ((String)map.get(PlayerService.META_FAVICON)).isEmpty());
        
        Assert.assertTrue("META_TITLE is not null.", map.containsKey(PlayerService.META_TITLE));
        Assert.assertFalse("META_TITLE is not empty.", ((String)map.get(PlayerService.META_TITLE)).isEmpty());
        
        Assert.assertTrue("META_DESCRIPTION is not null.", map.containsKey(PlayerService.META_DESCRIPTION));
        Assert.assertFalse("META_DESCRIPTION is not empty.", ((String)map.get(PlayerService.META_DESCRIPTION)).isEmpty());
        
        Assert.assertTrue("META_IMAGE is not null.", map.containsKey(PlayerService.META_IMAGE));
        Assert.assertFalse("META_IMAGE is not empty.", ((String)map.get(PlayerService.META_IMAGE)).isEmpty());
        
    }
    
    @Test
    public void testGetTransitionModel() {
        
        Model mockModel = new ExtendedModelMap();
        req.addHeader(ApiContext.HEADER_USER_AGENT, "Mozilla/5.0 (Linux; U; Android 2.3.3; en-us; HTC_DesireS_S510e Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
        mockModel = service.getTransitionModel(mockModel, Mso.NAME_9X9, req);
        
        Map<String, Object> map = mockModel.asMap();
        log.info(map.toString());
        
        Assert.assertTrue("'name' is not null.", map.containsKey("name"));
        Assert.assertFalse("'name' is not empty.", ((String)map.get("name")).isEmpty());
        
        Assert.assertTrue("'fliprUrl' is not null.", map.containsKey("fliprUrl"));
        Assert.assertFalse("'fliprUrl' is not empty.", ((String)map.get("fliprUrl")).isEmpty());
        
        Assert.assertTrue("'reportUrl' is not null.", map.containsKey("reportUrl"));
        Assert.assertFalse("'reportUrl' is not empty.", ((String)map.get("reportUrl")).isEmpty());
        
        Assert.assertTrue("'storeUrl' is not null.", map.containsKey("storeUrl"));
        Assert.assertFalse("'storeUrl' is not empty.", ((String)map.get("storeUrl")).isEmpty());
    }
}
