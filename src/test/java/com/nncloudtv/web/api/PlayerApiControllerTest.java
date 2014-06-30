package com.nncloudtv.web.api;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.FacebookLib;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.mock.service.MockMsoConfigManager;
import com.nncloudtv.mock.service.MockMsoManager;

public class PlayerApiControllerTest {
    
    protected static final Logger log = Logger.getLogger(PlayerApiControllerTest.class.getName());
    
    private PlayerApiController playerAPI;
    
    private MockHttpServletRequest req;
    private MockMsoManager mockMsoMngr;
    private MockMsoConfigManager mockConfigMngr;
    
    @Before
    public void setUp() {
        
        CacheFactory.isEnabled = false;
        
        mockConfigMngr = new MockMsoConfigManager();
        mockMsoMngr = new MockMsoManager();
        req = new MockHttpServletRequest();
        
        playerAPI = new PlayerApiController(mockMsoMngr);
        FacebookLib.setConfigMngr(mockConfigMngr);
    }
    
    @Test
    public void testFbLogin() {
        
        String referrer = "http://www.mock.com/signin";
        req.setRequestURI("/playerAPI/fbLogin");
        req.addHeader(ApiContext.HEADER_REFERRER, referrer);
        String result = playerAPI.fbLogin(req);
        assertTrue(
                "The url redirection string slould start with 'redirect:'",
                result.matches("^redirect:.*$"));
        result = result.substring(9);
        
        URL url = null;
        URI uri = null;
        try {
            url = new URL(result);
            uri = new URI(result);
        } catch (MalformedURLException e) {
        } catch (URISyntaxException e) {
        }
        assertFalse("Redirection should be a valid URL.", url == null);
        assertFalse("Redirection should be a valid URI.", uri == null);
        
        List<NameValuePair> params = URLEncodedUtils.parse(uri, NnStringUtil.UTF8);
        Map<String, String> map = new HashMap<String, String>();
        for (NameValuePair nv : params) {
            map.put(nv.getName(), nv.getValue());
        }
        assertNotNull("'client_id' sould not be null.", map.get("client_id"));
        String redirectUri = map.get("redirect_uri");
        assertNotNull("'redirect_uri' should not be null.", redirectUri);
        
        uri = null;
        try {
            uri = new URI(redirectUri);
        } catch (URISyntaxException e) {
        }
        assertNotNull("'redirect_uri' should be valid.", uri);
        params = URLEncodedUtils.parse(uri, NnStringUtil.UTF8);
        map.clear();
        for (NameValuePair nv : params) {
            map.put(nv.getName(), nv.getValue());
        }
        
        assertNotNull("The parameter 'uri' of redirect_uri should not be null.", map.get("uri"));
        assertEquals("The referrer should be matched.", referrer, map.get("uri"));
    }
}
