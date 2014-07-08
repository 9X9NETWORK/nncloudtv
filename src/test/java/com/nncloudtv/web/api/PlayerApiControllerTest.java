package com.nncloudtv.web.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.FacebookLib;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MsoManager.class,FacebookLib.class})
public class PlayerApiControllerTest {
    
    protected static final Logger log = Logger.getLogger(PlayerApiControllerTest.class.getName());
    
    private PlayerApiController playerAPI;
    
    private MockHttpServletRequest req;
    @Mock private MsoManager mockMsoMngr;
    @Mock private MsoConfigManager mockConfigMngr;
    
    @Before
    public void setUp() {
        
        CacheFactory.isEnabled = false;
        
        req = new MockHttpServletRequest();
        
        playerAPI = new PlayerApiController(mockMsoMngr);
        FacebookLib.setConfigMngr(mockConfigMngr);
    }
    
    @After
    public void tearDown() {
        req = null;
        mockMsoMngr = null;
        mockConfigMngr = null;
        
        playerAPI = null;
    }
    
    @Test
    public void testFbLogin() {
        
        // input arguments
        final String referrer = "http://www.mock.com/signin";
        req.setRequestURI("/playerAPI/fbLogin");
        req.addHeader(ApiContext.HEADER_REFERRER, referrer);
        
        // mock data
        final Long msoId = (long) 1;
        Mso mso = new Mso("9x9", "intro", "contactEmail", Mso.TYPE_NN);
        mso.setId(msoId);
        
        // stubs
        when(mockMsoMngr.getByNameFromCache(anyString())).thenReturn(mso);
        
        PowerMockito.mockStatic(MsoManager.class);
        when(MsoManager.isNNMso((Mso) anyObject())).thenReturn(true);
        
        when(mockMsoMngr.findOneByName(anyString())).thenReturn(mso);
        
        PowerMockito.mockStatic(FacebookLib.class);
        when(FacebookLib.getDialogOAuthPath(anyString(), anyString(), (Mso) anyObject())).thenCallRealMethod();
        
        // execute
        String result = playerAPI.fbLogin(req);
        
        // verify
        verify(mockMsoMngr).getByNameFromCache(anyString());
        
        PowerMockito.verifyStatic();
        MsoManager.isNNMso(mso);
        
        verify(mockMsoMngr).findOneByName(null);
        
        final String protocol = "http://";
        final String domain = "www.localhost"; // if wrong here, look ApiContext implement
        final String path = "/fb/login";
        PowerMockito.verifyStatic();
        FacebookLib.getDialogOAuthPath(referrer, protocol + domain + path, mso);
        
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
