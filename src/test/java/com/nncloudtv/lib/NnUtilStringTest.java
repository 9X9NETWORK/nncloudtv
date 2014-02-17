package com.nncloudtv.lib;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.mock.service.MockMsoManager;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.web.api.ApiContext;


public class NnUtilStringTest {
    
    protected static final Logger log = Logger.getLogger(NnUtilStringTest.class.getName());
    
    MockHttpServletRequest req;
    private ApiContext context;
    
    private final String chId = "28087";
    private final String epId = "e49675";
    
    @Before
    public void setUp() {
        
        req = new MockHttpServletRequest();
        context = new ApiContext(new MockHttpServletRequest(), new MockMsoManager());
    }
    
    @Test
    public void testUrlencode() {
        
        String expected = "%5B%20SPA%20SE%20%5D";
        String input = "[ SPA SE ]";
        String output = NnStringUtil.urlencode(input);
        log.info("urlencod('" + input + "') = '" + output + "'");
        
        Assert.assertEquals("Spaces should be replaced by '%20'.", expected, output);
    }
    
    @Test
    public void testGetSharingUrl() {
        
        Assert.assertEquals("standard sharing URL is not matched.",
                            "http://" + MsoConfigManager.getServerDomain() + "/view/p" + chId + "/" + epId,
                            NnStringUtil.getSharingUrl(false, context, chId, epId));
        
        Assert.assertEquals("flipr URL is not matched.",
                "flipr://" + MsoConfigManager.getServerDomain() + "/view/p" + chId + "/" + epId,
                NnStringUtil.getSharingUrl(true, context, chId, epId));
        
    }
}