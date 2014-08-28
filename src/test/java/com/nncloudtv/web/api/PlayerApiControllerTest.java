package com.nncloudtv.web.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.GetFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.nncloudtv.dao.MsoConfigDao;
import com.nncloudtv.dao.MsoDao;
import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.FacebookLib;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.service.NnStatusMsg;
import com.nncloudtv.service.NnUserManager;
import com.nncloudtv.service.PlayerApiService;
import com.nncloudtv.support.NnTestUtil;
import com.nncloudtv.web.json.player.ApiStatus;
import com.nncloudtv.wrapper.NNFWrapper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MsoManager.class,FacebookLib.class,CacheFactory.class,PlayerApiController.class})
public class PlayerApiControllerTest {
    
    protected static final Logger log = Logger.getLogger(PlayerApiControllerTest.class.getName());
    
    private PlayerApiController playerAPI;
    
    private MockHttpServletRequest req;
    private MockHttpServletResponse resp;
    
    @Before
    public void setUp() {
        req = new MockHttpServletRequest();
        resp = new MockHttpServletResponse(); 
        playerAPI = new PlayerApiController();
    }
    
    @After
    public void tearDown() {
        
        req = null;
        resp = null;
        playerAPI = null;
        
        NNFWrapper.empty();
    }
    
    private void setUpMemCacheMock(MemcachedClient cache) {
        
        CacheFactory.isEnabled = true;
        CacheFactory.isRunning = true;
        
        PowerMockito.spy(CacheFactory.class);
        try {
            PowerMockito.doReturn(cache).when(CacheFactory.class, "getSharedClient");
            PowerMockito.doReturn(cache).when(CacheFactory.class, "getClient");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // default return null for any kind of key
        GetFuture<Object> future = Mockito.mock(GetFuture.class);
        when(cache.asyncGet(anyString())).thenReturn(future);
        try {
            when(future.get(anyInt(), (TimeUnit) anyObject())).thenReturn(null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
    
    private void recordMemoryCacheGet(MemcachedClient cache, String key, Object returnObj) {
        
        GetFuture<Object> future = Mockito.mock(GetFuture.class);
        when(cache.asyncGet(key)).thenReturn(future);
        try {
            when(future.get(anyInt(), (TimeUnit) anyObject())).thenReturn(returnObj);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testBrandInfoWithVersionParameter() {
        
        // input
        String brandName = Mso.NAME_9X9;
        String os = "web";
        //String version = "40";
        //req.setParameter("v", version);
        req.setParameter("format", "text");
        req.setParameter("lang", "zh");
        req.setParameter("os", os);
        req.setParameter("mso", brandName);
        
        // mock object
        MemcachedClient cache = Mockito.mock(MemcachedClient.class);
        MsoConfigManager configMngr = Mockito.mock(MsoConfigManager.class);
        NnUserManager userMngr = Mockito.spy(new NnUserManager());
        
        NNFWrapper.setConfigMngr(configMngr);
        NNFWrapper.setUserMngr(userMngr);
        
        PlayerApiService playerApiService = PowerMockito.spy(new PlayerApiService());
        
        // mock data
        Mso mso = NnTestUtil.getNnMso();
        
        MsoConfig minVersion = new MsoConfig();
        minVersion.setMsoId(mso.getId());
        minVersion.setItem(MsoConfig.API_MINIMAL);
        minVersion.setValue("40");
        
        // stubs
        // only mso=9x9 available from cache and database
        setUpMemCacheMock(cache);
        String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
        recordMemoryCacheGet(cache, cacheKey, mso);
        
        doReturn("zh").when(userMngr).findLocaleByHttpRequest(req); // must stub
        
        when(configMngr.findByItem(MsoConfig.API_MINIMAL)).thenReturn(minVersion);
        
        // inject playerApiService in local new
        try {
            PowerMockito.whenNew(PlayerApiService.class).withNoArguments().thenReturn(playerApiService);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // execute & verify
        // missing 'v' in request parameter but has 'v' setting in database with v=40
        Object actual = playerAPI.brandInfo(brandName, os, null, null, req, resp);
        String expected = (String)playerApiService.response(playerApiService.assembleMsgs(NnStatusCode.API_FORCE_UPGRADE, null));

        assertEquals("missing 'v' setting will evalute to v=31, with database hold v=40 then " +
                "should response for 'api force upgrade'.", expected, actual);
        
        // with v=32 set in request parameter and has 'v' setting in database with v=40
        String version = "32";
        req.setParameter("v", version);
        actual = playerAPI.brandInfo(brandName, os, version, null, req, resp);
        assertEquals("when 'v' set to v=32, with database hold v=40 then " +
                "should response for 'api force upgrade'.", expected, actual);
        
        // missing 'v' in request parameter and has not 'v' setting in database
        when(configMngr.findByItem(MsoConfig.API_MINIMAL)).thenReturn(null);
        req.removeParameter("v");
        actual = playerAPI.brandInfo(brandName, os, null, null, req, resp);
        
        ArgumentCaptor<Object> captureActual = ArgumentCaptor.forClass(Object.class);
        verify(playerApiService, atLeastOnce()).response(captureActual.capture());
        String actual2 = (String) captureActual.getValue();
        
        expected = NnStatusCode.SUCCESS + "\t" +
                NnStatusMsg.getPlayerMsgText(NnStatusCode.SUCCESS, null) + "\n";
        assertTrue("missing 'v' in request parameter and without 'v' setting in database " +
                "should always see as success operation.", actual2.startsWith(expected));
        
        // with v=40 set in request parameter and has 'v' setting in database with v=32
        minVersion.setValue("32");
        when(configMngr.findByItem(MsoConfig.API_MINIMAL)).thenReturn(minVersion);
        version = "40";
        req.setParameter("v", version);
        actual = playerAPI.brandInfo(brandName, os, version, null, req, resp);
        
        captureActual = ArgumentCaptor.forClass(Object.class);
        verify(playerApiService, atLeastOnce()).response(captureActual.capture());
        actual2 = (String) captureActual.getValue();
        
        assertTrue("when 'v' set to v=40, with database hold v=32 then " +
                "should return as success operation.", actual2.startsWith(expected));
    }
    
    @Test
    public void testBrandInfoWithLocaleResponse() {
        
        // input
        String brandName = Mso.NAME_9X9;
        String os = "web";
        String version = "40";
        req.setParameter("v", version);
        req.setParameter("format", "text");
        req.setParameter("lang", "zh");
        req.setParameter("os", os);
        req.setParameter("mso", brandName);
        
        // mock data
        Mso mso = NnTestUtil.getNnMso();
        
        // mock object
        MemcachedClient cache = Mockito.mock(MemcachedClient.class);
        MsoConfigManager configMngr = Mockito.mock(MsoConfigManager.class);
        
        NNFWrapper.setConfigMngr(configMngr);
        
        PlayerApiService playerApiService = PowerMockito.spy(new PlayerApiService());
        
        // stubs
        // only mso=9x9 available from cache and database
        setUpMemCacheMock(cache);
        String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
        recordMemoryCacheGet(cache, cacheKey, mso);
        
        // inject playerApiService in local new
        try {
            PowerMockito.whenNew(PlayerApiService.class).withNoArguments().thenReturn(playerApiService);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // execute & verify
        // ip in tw
        req.setRemoteAddr("114.32.175.163"); // this ip locate in tw
        Object actual = playerAPI.brandInfo(brandName, os, version, null, req, resp);
        
        ArgumentCaptor<Object> captureActual = ArgumentCaptor.forClass(Object.class);
        verify(playerApiService, atLeastOnce()).response(captureActual.capture());
        String actual2 = (String) captureActual.getValue();
        String expected = "locale" + "\t" + "zh" + "\n";
        assertTrue("ip locate in tw should set locale=zh in response.", actual2.contains(expected));
        
        // ip in us
        req.setRemoteAddr("136.18.5.120"); // this ip locate in us
        actual = playerAPI.brandInfo(brandName, os, version, null, req, resp);
        
        verify(playerApiService, atLeastOnce()).response(captureActual.capture());
        actual2 = (String) captureActual.getValue();
        expected = "locale" + "\t" + "en" + "\n";
        assertTrue("ip not locate in tw should set locale=en in response.", actual2.contains(expected));
    }
    
    @Test
    public void testBrandInfoWithFormatParameter() {
        
        // input
        String brandName = Mso.NAME_9X9;
        String os = "web";
        String version = "40";
        req.setParameter("v", version);
        req.setParameter("format", "text");
        req.setParameter("lang", "zh");
        req.setParameter("os", os);
        req.setParameter("mso", brandName);
        
        // mock data
        Mso mso = NnTestUtil.getNnMso();
        
        // mock object
        MemcachedClient cache = Mockito.mock(MemcachedClient.class);
        NnUserManager userMngr = Mockito.spy(new NnUserManager());
        MsoConfigManager configMngr = Mockito.mock(MsoConfigManager.class);
        
        NNFWrapper.setUserMngr(userMngr);
        NNFWrapper.setConfigMngr(configMngr);
        
        PlayerApiService playerApiService = PowerMockito.spy(new PlayerApiService());
        
        // stubs
        // only mso=9x9 available from cache and database
        setUpMemCacheMock(cache);
        String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
        recordMemoryCacheGet(cache, cacheKey, mso);
        
        doReturn("zh").when(userMngr).findLocaleByHttpRequest(req); // must stub
        
        // inject playerApiService in local new
        try {
            PowerMockito.whenNew(PlayerApiService.class).withNoArguments().thenReturn(playerApiService);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // execute & verify
        // format=xyz, not exist format
        req.setParameter("format", "xyz");
        Object actual = playerAPI.brandInfo(brandName, os, version, null, req, resp);
        
        ArgumentCaptor<Object> captureActual = ArgumentCaptor.forClass(Object.class);
        verify(playerApiService, atLeastOnce()).response(captureActual.capture());
        Object actual2 = captureActual.getValue();
        assertTrue("parameter format with not exist format should return text format response as default.",
                actual2 instanceof String);
        
        // not provide format
        req.removeParameter("format");
        actual = playerAPI.brandInfo(brandName, os, version, null, req, resp);
        
        verify(playerApiService, atLeastOnce()).response(captureActual.capture());
        actual2 = captureActual.getValue();
        assertTrue("parameter format not provide should return text format response as default.",
                actual2 instanceof String);
    }
    
    @Test
    public void testFbLogin() {
        
        CacheFactory.isEnabled = false;
        MsoManager mockMsoMngr = Mockito.mock(MsoManager.class);
        NNFWrapper.setMsoMngr(mockMsoMngr);
        
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
