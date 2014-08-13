package com.nncloudtv.web.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;

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
import com.nncloudtv.model.Mso;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.wrapper.NNFWrapper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MsoManager.class,FacebookLib.class})
public class PlayerApiControllerTest {
    
    protected static final Logger log = Logger.getLogger(PlayerApiControllerTest.class.getName());
    
    private PlayerApiController playerAPI;
    
    private MockHttpServletRequest req;
    @Mock private MsoManager mockMsoMngr;
    
    @Before
    public void setUp() {
        
        CacheFactory.isEnabled = false;
        
        req = new MockHttpServletRequest();
        
        playerAPI = new PlayerApiController();
        
        NNFWrapper.setMsoMngr(mockMsoMngr);
    }
    
    @After
    public void tearDown() {
        req = null;
        mockMsoMngr = null;
        
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
        
        final String dialogOAuthPath = "dialogOAuthPath";
        
        // stubs
        when(mockMsoMngr.getByNameFromCache(anyString())).thenReturn(mso);
        
        PowerMockito.mockStatic(MsoManager.class);
        when(MsoManager.isNNMso((Mso) anyObject())).thenReturn(true);
        
        when(mockMsoMngr.findOneByName(anyString())).thenReturn(mso);
        
        PowerMockito.mockStatic(FacebookLib.class);
        when(FacebookLib.getDialogOAuthPath(anyString(), anyString(), (Mso) anyObject())).thenReturn(dialogOAuthPath);
        
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
        
        assertEquals("redirect:" + dialogOAuthPath, result);
    }
}