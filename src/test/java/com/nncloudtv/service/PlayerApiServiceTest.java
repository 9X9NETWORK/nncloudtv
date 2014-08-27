package com.nncloudtv.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.GetFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.nncloudtv.dao.MsoDao;
import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.CookieHelper;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.NnGuest;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserProfile;
import com.nncloudtv.support.NnTestUtil;
import com.nncloudtv.validation.BasicValidator;
import com.nncloudtv.web.api.ApiContext;
import com.nncloudtv.web.api.NnStatusCode;
import com.nncloudtv.web.json.player.ApiStatus;
import com.nncloudtv.web.json.player.BrandInfo;
import com.nncloudtv.wrapper.NNFWrapper;

@RunWith(Enclosed.class)
public class PlayerApiServiceTest {

    protected static final Logger log = Logger.getLogger(PlayerApiServiceTest.class.getName());
    
    private static PlayerApiService service;
    private static MockHttpServletRequest req;
    private static MockHttpServletResponse resp;

    @Before
    public void setUp() {
        
        req = new MockHttpServletRequest();
        resp = new MockHttpServletResponse();
        
        //req.addHeader(ApiContext.HEADER_USER_AGENT, MockHttpServletRequest.class.getName());
        //HttpSession session = req.getSession();
        //session.setMaxInactiveInterval(60);
        service = Mockito.spy(new PlayerApiService());
    }
    
    @After
    public void tearDown() {
        req = null;
        resp = null;
        service = null;
        
        NNFWrapper.empty();
    }
    
    private static void setUpMemCacheMock(MemcachedClient cache) {
        
        CacheFactory.isEnabled = true;
        CacheFactory.isRunning = true;
        
        PowerMockito.spy(CacheFactory.class);
        try {
            PowerMockito.doReturn(cache).when(CacheFactory.class, "getSharedClient");
            PowerMockito.doReturn(cache).when(CacheFactory.class, "getClient");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        @SuppressWarnings("unchecked")
        GetFuture<Object> future = Mockito.mock(GetFuture.class);
        
        // default return null for any kind of key
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
    
    private static void recordMemoryCacheGet(MemcachedClient cache, String key, Object returnObj) {
        
        @SuppressWarnings("unchecked")
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
    
    private static String pair(String key, String value) {
        return PlayerApiService.assembleKeyValue(key, value);
    }
    
    @RunWith(PowerMockRunner.class)
    @PrepareForTest({CacheFactory.class})
    public static class testBrandInfo extends PlayerApiServiceTest {
        
        private MemcachedClient cache;
        private NnUserManager userMngr;
        private MsoConfigManager configMngr;
        
        private String brandInfo9x9;
        private Mso mso9x9;
        private String defaultOS;
        
        @Before
        public void setUp2() {
            
            cache = Mockito.mock(MemcachedClient.class);
            userMngr = Mockito.spy(new NnUserManager());
            configMngr = Mockito.spy(new MsoConfigManager());
            
            setUpMemCacheMock(cache);
            NNFWrapper.setUserMngr(userMngr);
            NNFWrapper.setConfigMngr(configMngr);
            
            doReturn(null).when(configMngr).findByItem(MsoConfig.API_MINIMAL); // must stub
            doReturn(null).when(configMngr).findByItem(MsoConfig.RO); // must stub
            doReturn("zh").when(userMngr).findLocaleByHttpRequest(req); // must stub
            
            // default cache for mso=9x9
            mso9x9 = NnTestUtil.getNnMso();
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso9x9);
            
            // default cache for brandInfo=9x9, at os="web" format="text"
            brandInfo9x9 = "";
            brandInfo9x9 += PlayerApiService.assembleKeyValue("key", String.valueOf(mso9x9.getId()));
            brandInfo9x9 += PlayerApiService.assembleKeyValue("name", mso9x9.getName());
            defaultOS = PlayerService.OS_WEB;
            cacheKey = CacheFactory.getBrandInfoKey(mso9x9, defaultOS, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo9x9);
            
            // default input
            req.setParameter("v", "40");
            req.setParameter("format", "text");
            req.setParameter("lang", "zh");
            req.setParameter("os", defaultOS);
            req.setParameter("mso", Mso.NAME_9X9);
            req.setServerName("localhost:8080");
            req.setRequestURI("/playerAPI/brandInfo");
        }
        
        @After
        public void tearDown2() {
            
            cache = null;
            userMngr = null;
            configMngr = null;
            
            brandInfo9x9 = null;
            mso9x9 = null;
            defaultOS = null;
        }
        
        @Test
        public void notExistMso() {
            
            // input
            String brandName = "notExist";
            req.setParameter("mso", brandName);
            
            // mock object
            MsoDao msoDao = Mockito.spy(new MsoDao());
            NNFWrapper.setMsoDao(msoDao);
            
            // stubs
            // mso=notExist unavailable from cache
            String cacheKey = "mso(" + brandName + ")";
            recordMemoryCacheGet(cache, cacheKey, null);
            // mso=notExist unavailable from database
            doReturn(null).when(msoDao).findByName(brandName); // must stub
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(defaultOS, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Not exist mso should return as default(mso=9x9) brand info.",
                    ((String) actual).contains(brandInfo9x9));
        }
        
        @Test
        public void notProvideMso() {
            
            // input
            req.removeParameter("mso");
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(defaultOS, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Not provide mso should return as default(mso=9x9) brand info.",
                    ((String) actual).contains(brandInfo9x9));
        }
        
        @Test
        public void existMso() {
            
            // input
            String brandName = "cts";
            req.setParameter("mso", brandName);
            
            // mock data
            Mso mso = NnTestUtil.getNnMso();
            mso.setId(3);
            mso.setName(brandName);
            mso.setType(Mso.TYPE_MSO);
            
            String brandInfo = "";
            brandInfo += PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
            brandInfo += PlayerApiService.assembleKeyValue("name", mso.getName());
            
            // stubs
            // mso=cts available from cache
            String cacheKey = "mso(" + brandName + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo=cts from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, defaultOS, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(defaultOS, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Provide exist mso should return as its brand info.", ((String) actual).contains(brandInfo));
        }
        
        @Test
        public void existMsoFromRoot() {
            
            // input
            String brandName = "cts";
            req.removeParameter("mso");
            req.setServerName(brandName + ".flipr.tv");
            req.setRequestURI("/playerAPI/brandInfo");
            
            // mock object
            MsoDao msoDao = Mockito.spy(new MsoDao());
            NNFWrapper.setMsoDao(msoDao);
            
            // mock data
            Mso mso = NnTestUtil.getNnMso();
            mso.setId(3);
            mso.setName(brandName);
            mso.setType(Mso.TYPE_MSO);
            
            String brandInfo = "";
            brandInfo += PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
            brandInfo += PlayerApiService.assembleKeyValue("name", mso.getName());
            
            // stubs
            // mso=cts available from database
            doReturn(mso).when(msoDao).findByName(brandName); // must stub
            // brandInfo=cts from cache
            String cacheKey = CacheFactory.getBrandInfoKey(mso, defaultOS, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(defaultOS, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Provide exist mso from root domain should return as its brand info.",
                    ((String) actual).contains(brandInfo));
        }
        
        @Test
        public void provideTextFormat() {
            
            // input
            req.setParameter("format", "text");
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(defaultOS, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Should return wanted(format=text) brand info.", ((String) actual).contains(brandInfo9x9));
        }
        
        @Test
        public void provideJsonFormat() {
            
            // input
            req.setParameter("format", "json");
            
            // mock data
            BrandInfo brandInfo = new BrandInfo();
            brandInfo.setKey(mso9x9.getId());
            brandInfo.setName(mso9x9.getName());
            
            // stubs
            // brandInfo(format=json) from cache
            String cacheKey = CacheFactory.getBrandInfoKey(mso9x9, defaultOS, PlayerApiService.FORMAT_JSON);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(defaultOS, req);
            
            // verify
            assertTrue("parameter format=json should return json format response.", actual instanceof ApiStatus);
            assertTrue("parameter format=json should return json format response.",
                    ((ApiStatus) actual).getData() instanceof BrandInfo);
            BrandInfo actualBrandInfo = (BrandInfo) ((ApiStatus) actual).getData();
            assertEquals("Should return wanted(format=json) brand info.", brandInfo.getKey(), actualBrandInfo.getKey());
            assertEquals("Should return wanted(format=json) brand info.", brandInfo.getName(), actualBrandInfo.getName());
        }
        
        @Test
        public void provideUnknownFormat() {
            
            // TODO discuz, provide unknown format parameter test case apply for all player api, need each api write one ?
            
            // input
            req.setParameter("format", "xyz");
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(defaultOS, req);
            
            // verify
            assertTrue("parameter format=xyz(unknown format) should return text format response.", actual instanceof String);
            assertTrue("Should return default(format=text) brand info.", ((String) actual).contains(brandInfo9x9));
        }
        
        @Test
        public void notProvideFormat() {
            
            // TODO discuz, not provide format parameter test case apply for all player api, need each api write one ?
            
            // input
            req.removeParameter("format");
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(defaultOS, req);
            
            // verify
            assertTrue("parameter format not provide should return text format response.", actual instanceof String);
            assertTrue("Should return default(format=text) brand info.", ((String) actual).contains(brandInfo9x9));
        }
        
        @Test
        public void osAndroid() {
            
            // input
            String os = PlayerService.OS_ANDROID;
            req.setParameter("os", os);
            
            // mock data
            Mso mso = NnTestUtil.getNnMso();
            
            String brandInfo = "";
            brandInfo += PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
            brandInfo += PlayerApiService.assembleKeyValue("title", mso.getTitle());
            
            MsoConfig adConfig = new MsoConfig();
            adConfig.setMsoId(mso.getId());
            String adKeyName = configMngr.getKeyNameByOs(PlayerService.OS_ANDROID, "ad");
            adConfig.setItem(adKeyName);
            adConfig.setValue(MsoConfig.AD_DIRECT_VIDEO);
            
            String adInfo = "";
            String tab = "\t";
            String br = "\n";
            adInfo += "1" + tab + "0" + tab + "ad hello world!!" + tab + "http://s3.aws.amazon.com/creative_video0.mp4" + br;
            adInfo += "2" + tab + "1" + tab + "vastAd hello world!!" + tab + "http://rec.scupio.com/recweb/vast.aspx?" + br;
            
            // stubs
            // only mso=9x9 available from cache, overwrite original one, becuz need mso equality check at later stub
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_ANDROID, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            doReturn(adConfig).when(configMngr).findByMsoAndItem(mso, adKeyName); // must stub, mso equality check
            
            // adInfo from cache
            cacheKey = CacheFactory.getAdInfoKey(mso, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, adInfo);
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Should return wanted(os=android) brand info.", ((String) actual).contains(brandInfo));
            assertTrue("Should contain adInfo.", ((String) actual).contains(adInfo));
        }
        
        @Test
        public void osAndroidFromUserAgent() {
            
            // input
            String os = null;
            req.removeParameter("os");
            req.addHeader(ApiContext.HEADER_USER_AGENT, "Android");
            
            // mock data
            Mso mso = NnTestUtil.getNnMso();
            
            String brandInfo = "";
            brandInfo += PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
            brandInfo += PlayerApiService.assembleKeyValue("title", mso.getTitle());
            
            MsoConfig adConfig = new MsoConfig();
            adConfig.setMsoId(mso.getId());
            String adKeyName = configMngr.getKeyNameByOs(PlayerService.OS_ANDROID, "ad");
            adConfig.setItem(adKeyName);
            adConfig.setValue(MsoConfig.AD_DIRECT_VIDEO);
            
            String adInfo = "";
            String tab = "\t";
            String br = "\n";
            adInfo += "1" + tab + "0" + tab + "ad hello world!!" + tab + "http://s3.aws.amazon.com/creative_video0.mp4" + br;
            adInfo += "2" + tab + "1" + tab + "vastAd hello world!!" + tab + "http://rec.scupio.com/recweb/vast.aspx?" + br;
            
            // stubs
            // only mso=9x9 available from cache, overwrite original one, becuz need mso equality check at later stub
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_ANDROID, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            doReturn(adConfig).when(configMngr).findByMsoAndItem(mso, adKeyName); // must stub, mso equality check
            
            // adInfo from cache
            cacheKey = CacheFactory.getAdInfoKey(mso, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, adInfo);
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Should return wanted(os=android) brand info.", ((String) actual).contains(brandInfo));
            assertTrue("Should contain adInfo.", ((String) actual).contains(adInfo));
        }
        
        @Test
        public void osIos() {
            
            // input
            String os = PlayerService.OS_IOS;
            req.setParameter("os", os);
            
            // mock data
            Mso mso = NnTestUtil.getNnMso();
            
            String brandInfo = "";
            brandInfo += PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
            brandInfo += PlayerApiService.assembleKeyValue("title", mso.getTitle());
            
            MsoConfig adConfig = new MsoConfig();
            adConfig.setMsoId(mso.getId());
            String adKeyName = configMngr.getKeyNameByOs(PlayerService.OS_IOS, "ad");
            adConfig.setItem(adKeyName);
            adConfig.setValue(MsoConfig.AD_DIRECT_VIDEO);
            
            String adInfo = "";
            String tab = "\t";
            String br = "\n";
            adInfo += "1" + tab + "0" + tab + "ad hello world!!" + tab + "http://s3.aws.amazon.com/creative_video0.mp4" + br;
            adInfo += "2" + tab + "1" + tab + "vastAd hello world!!" + tab + "http://rec.scupio.com/recweb/vast.aspx?" + br;
            
            // stubs
            // only mso=9x9 available from cache, overwrite original one, becuz need mso equality check at later stub
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_IOS, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            doReturn(adConfig).when(configMngr).findByMsoAndItem(mso, adKeyName); // must stub, mso equality check
            
            // adInfo from cache
            cacheKey = CacheFactory.getAdInfoKey(mso, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, adInfo);
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Should return wanted(os=ios) brand info.", ((String) actual).contains(brandInfo));
            assertTrue("Should contain adInfo.", ((String) actual).contains(adInfo));
        }
        
        @Test
        public void osIosFromUserAgent() {
            
            // input
            String os = null;
            req.removeParameter("os");
            req.addHeader(ApiContext.HEADER_USER_AGENT, "iPhone");
            
            // mock data
            Mso mso = NnTestUtil.getNnMso();
            
            String brandInfo = "";
            brandInfo += PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
            brandInfo += PlayerApiService.assembleKeyValue("title", mso.getTitle());
            
            MsoConfig adConfig = new MsoConfig();
            adConfig.setMsoId(mso.getId());
            String adKeyName = configMngr.getKeyNameByOs(PlayerService.OS_IOS, "ad");
            adConfig.setItem(adKeyName);
            adConfig.setValue(MsoConfig.AD_DIRECT_VIDEO);
            
            String adInfo = "";
            String tab = "\t";
            String br = "\n";
            adInfo += "1" + tab + "0" + tab + "ad hello world!!" + tab + "http://s3.aws.amazon.com/creative_video0.mp4" + br;
            adInfo += "2" + tab + "1" + tab + "vastAd hello world!!" + tab + "http://rec.scupio.com/recweb/vast.aspx?" + br;
            
            // stubs
            // only mso=9x9 available from cache, overwrite original one, becuz need mso equality check at later stub
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_IOS, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            doReturn(adConfig).when(configMngr).findByMsoAndItem(mso, adKeyName); // must stub, mso equality check
            
            // adInfo from cache
            cacheKey = CacheFactory.getAdInfoKey(mso, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, adInfo);
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Should return wanted(os=ios) brand info.", ((String) actual).contains(brandInfo));
            assertTrue("Should contain adInfo.", ((String) actual).contains(adInfo));
        }
        
        @Test
        public void osWeb() {
            
            // input
            String os = PlayerService.OS_WEB;
            req.setParameter("os", os);
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Should return as wanted(os=web) brand info.", ((String) actual).contains(brandInfo9x9));
        }
        
        @Test
        public void osNotProvide() {
            
            // input
            String os = null;
            req.removeParameter("os");
            req.addHeader(ApiContext.HEADER_USER_AGENT, "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:31.0)");
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Should return as default(os=web) brand info.", ((String) actual).contains(brandInfo9x9));
        }
        
        @Test
        public void osNotSupport() {
            
            // input
            String os = "xyz";
            req.setParameter("os", os);
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Should return as default(os=web) brand info.", ((String) actual).contains(brandInfo9x9));
        }
    }
    
    @RunWith(PowerMockRunner.class)
    @PrepareForTest({CacheFactory.class})
    public static class testBrandInfoWithoutCache extends PlayerApiServiceTest {
        
        private MsoConfigManager configMngr;
        
        private Mso defaultMso;
        private String defaultOS;
        
        @Before
        public void setUp2() {
            
            CacheFactory.isEnabled = false;
            CacheFactory.isRunning = false;
            
            NnUserManager userMngr = Mockito.spy(new NnUserManager());
            configMngr = Mockito.spy(new MsoConfigManager());
            MsoDao msoDao = Mockito.spy(new MsoDao());
            
            NNFWrapper.setUserMngr(userMngr);
            NNFWrapper.setConfigMngr(configMngr);
            NNFWrapper.setMsoDao(msoDao);
            
            doReturn(null).when(configMngr).findByItem(MsoConfig.API_MINIMAL); // must stub
            doReturn(null).when(configMngr).findByItem(MsoConfig.RO); // must stub
            doReturn("zh").when(userMngr).findLocaleByHttpRequest(req); // must stub
            
            String brandName = "cts";
            defaultMso = NnTestUtil.getNnMso();
            defaultMso.setId(3);
            defaultMso.setName(brandName);
            defaultMso.setType(Mso.TYPE_MSO);
            
            doReturn(defaultMso).when(msoDao).findByName(brandName); // must stub
            
            defaultOS = PlayerService.OS_WEB;
            // default input
            req.setParameter("v", "40");
            req.setParameter("format", "text");
            req.setParameter("lang", "zh");
            req.setParameter("os", defaultOS);
            req.setParameter("mso", brandName);
            req.setServerName("localhost:8080");
            req.setRequestURI("/playerAPI/brandInfo");
        }
        
        @After
        public void tearDown2() {
            configMngr = null;
            
            defaultMso = null;
            defaultOS = null;
        }
        
        @Test
        public void mainKeyValue() { // main pair, include mso part and others not from database
            
            // check list
            String locale = "en";
            String piwik = "http://piwik.9x9.tv/";
            String counter = "0";
            String acceptLang = "JAVA";
            
            String key = String.valueOf(defaultMso.getId());
            String name = defaultMso.getName();
            String title = defaultMso.getTitle();
            String logoUrl = defaultMso.getLogoUrl();
            String jingleUrl = defaultMso.getJingleUrl();
            String preferredLangCode = defaultMso.getLang();
            
            // stubs
            req.addHeader("Accept-Language", acceptLang);
            
            NnUserManager userMngr = Mockito.spy(new NnUserManager());
            doReturn(locale).when(userMngr).findLocaleByHttpRequest(req);
            NNFWrapper.setUserMngr(userMngr); // overwrite
            
            // make all unavoid interactions with configMngr return as null, in os=web situation
            doReturn(new ArrayList<MsoConfig>()).when(configMngr).findByMso(defaultMso);
            doReturn(null).when(configMngr).findByMsoAndItem(defaultMso, MsoConfig.SEARCH);
            String item = configMngr.getKeyNameByOs(defaultOS, "google");
            doReturn(null).when(configMngr).findByMsoAndItem(defaultMso, item);
            doReturn(null).when(configMngr).findByMsoAndItem(defaultMso, MsoConfig.AUDIO_BACKGROUND);
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(defaultOS, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            
            String respText = (String) actual;
            assertTrue("missing 'locale' pair", respText.contains(pair("locale", locale)));
            assertTrue("missing 'brandInfoCounter' pair", respText.contains(pair("brandInfoCounter", counter)));
            assertTrue("missing 'piwik' pair", respText.contains(pair("piwik", piwik)));
            assertTrue("missing 'acceptLang' pair", respText.contains(pair("acceptLang", acceptLang)));
            
            assertTrue("missing 'key' pair", respText.contains(pair("key", key)));
            assertTrue("missing 'name' pair", respText.contains(pair("name", name)));
            assertTrue("missing 'title' pair", respText.contains(pair("title", title)));
            assertTrue("missing 'logoUrl' pair", respText.contains(pair("logoUrl", logoUrl)));
            assertTrue("missing 'jingleUrl' pair", respText.contains(pair("jingleUrl", jingleUrl)));
            assertTrue("missing 'preferredLangCode' pair", respText.contains(pair("preferredLangCode", preferredLangCode)));
        }
        
        @Test
        public void configKeyValue() { // common pair (OS independent)
            
        }
        
        @Test
        public void configKeyValueDefault() { // common pair (OS independent) and has default value
            
        }
        
        @Test
        public void configKeyValueWithWeb() { // unique pair (web owned)
            
        }
        
        @Test
        public void configKeyValueDefaultWithWeb() { // unique pair (web owned) and has default value
            
        }
        
        @Test
        public void configKeyValueWithIos() { // unique pair (ios owned)
            
        }
        
        @Test
        public void configKeyValueDefaultWithIos() { // unique pair (ios owned) and has default value
            
        }
        
        @Test
        public void configKeyValueWithAndroid() { // unique pair (android owned)
            
        }
        
        @Test
        public void configKeyValueDefaultWithAndroid() { // unique pair (android owned) and has default value
            
        }
        
        // TODO special case for ga pair, set value to null in database force delete pair,
        // where original behavior is give default value when no record in database
        
        // TODO third part 'ad' section test case
    }
    
    @RunWith(MockitoJUnitRunner.class)
    public static class testSetProfile extends PlayerApiServiceTest {
        
        @Test
        public void normal() {
            
            CacheFactory.isEnabled = false;
            
            MsoManager msoMngr = Mockito.mock(MsoManager.class);
            NnUserManager userMngr = Mockito.mock(NnUserManager.class);
            NnUserProfileManager profileMngr = Mockito.mock(NnUserProfileManager.class);
            NNFWrapper.setUserMngr(userMngr);
            NNFWrapper.setMsoMngr(msoMngr);
            NNFWrapper.setProfileMngr(profileMngr);
            
            // input arguments
            final String userToken = "mock-user-token-xxoo";
            final String items = "name,phone";
            final String name = "MockUser";
            final String phone = "7777777";
            final String values = name + "," + phone;
            
            // mock data
            final Long msoId = (long) 1;
            Mso mso = new Mso("name", "intro", "contactEmail", Mso.TYPE_MSO);
            mso.setId(msoId);
            
            final Long userId = (long) 1;
            NnUser user = new NnUser("_mock_@9x9.tv", "_password_", NnUser.TYPE_USER);
            user.setId(userId);
            
            // stubs
            when(msoMngr.getByNameFromCache(anyString())).thenReturn(mso);
            doReturn(0).when(service).checkApiMinimal();
            doReturn(NnStatusCode.SUCCESS).when(service).checkRO();
            
            when(userMngr.findByToken(anyString(), anyLong())).thenReturn(user);
            when(profileMngr.findByUser((NnUser) anyObject())).thenReturn(null);
            when(profileMngr.save((NnUser) anyObject(), (NnUserProfile) anyObject())).thenReturn(null);
            
            // execute
            service.prepService(req, resp);
            Object actual = service.setUserProfile(userToken, items, values, req);
            
            // verify
            verify(msoMngr).getByNameFromCache(anyString());
            verify(service).checkApiMinimal();
            verify(service).checkRO();
            
            verify(userMngr).findByToken(userToken, msoId);
            verify(profileMngr).findByUser(user);
            
            ArgumentCaptor<NnUserProfile> arg = ArgumentCaptor.forClass(NnUserProfile.class);
            verify(profileMngr).save(eq(user), arg.capture());
            NnUserProfile profile = arg.getValue();
            assertEquals(msoId, (Long) profile.getMsoId());
            assertEquals(userId, (Long) profile.getUserId());
            assertEquals(name, profile.getName());
            assertEquals(phone, profile.getPhoneNumber());
            
            assertNotNull(actual);
        }
    }
    
    @RunWith(PowerMockRunner.class)
    @PrepareForTest({BasicValidator.class})
    public static class testLogin extends PlayerApiServiceTest {
        
        @Test
        public void normal() {
            
            CacheFactory.isEnabled = false;
            
            MsoManager msoMngr = Mockito.mock(MsoManager.class);
            NnUserManager userMngr = Mockito.mock(NnUserManager.class);
            NNFWrapper.setUserMngr(userMngr);
            NNFWrapper.setMsoMngr(msoMngr);
            
            req.addHeader(ApiContext.HEADER_USER_AGENT, MockHttpServletRequest.class.getName());
            HttpSession session = req.getSession();
            session.setMaxInactiveInterval(60);
            
            // input arguments
            final String email = "a@a.com";
            final String password = "123456";
            
            // mock data
            final Long msoId = (long) 1;
            Mso mso = new Mso("name", "intro", "contactEmail", Mso.TYPE_MSO);
            mso.setId(msoId);
            
            final Long userId = (long) 1;
            final String token = "token";
            NnUser user = new NnUser(email, password, NnUser.TYPE_USER);
            user.setId(userId);
            user.setToken(token);
            
            Object userInfo = "userInfo";
            Object result = "result";
            
            // stubs
            when(msoMngr.getByNameFromCache(anyString())).thenReturn(mso);
            doReturn(0).when(service).checkApiMinimal();
            doReturn(NnStatusCode.SUCCESS).when(service).checkRO();
            
            PowerMockito.mockStatic(BasicValidator.class);
            when(BasicValidator.validateRequired((String[]) anyObject())).thenReturn(true);
            
            when(userMngr.findAuthenticatedUser(anyString(), anyString(), anyLong(),
                    (HttpServletRequest) anyObject())).thenReturn(user);
            // Spy object call one time for when() seems OK, but over two times got trouble (don't know why),
            // call doXXXX() family method first then when() method second solve it.
            doReturn(userInfo).when(service).prepareUserInfo((NnUser) anyObject(), (NnGuest) anyObject(),
                    (HttpServletRequest) anyObject(), anyBoolean());
            when(userMngr.save((NnUser) anyObject())).thenReturn(user);
            doNothing().when(service).setUserCookie((HttpServletResponse) anyObject(), anyString(), anyString());
            when(service.assembleMsgs(anyInt(), (String[]) anyObject())).thenReturn(result);
            
            // execute
            service.prepService(req, resp);
            Object actual = service.login(email, password, req, resp);
            
            // verify
            verify(msoMngr).getByNameFromCache(anyString());
            verify(service).checkApiMinimal();
            verify(service).checkRO();
            
            PowerMockito.verifyStatic();
            BasicValidator.validateRequired(new String[] {email, password});
            
            verify(userMngr).findAuthenticatedUser(email, password, msoId, req);
            verify(service).prepareUserInfo(user, null, req, true);
            verify(userMngr).save(user);
            verify(service).setUserCookie(resp, CookieHelper.USER, token);
            
            String[] raw = {(String) userInfo};
            verify(service).assembleMsgs(NnStatusCode.SUCCESS, raw);
            
            assertEquals(result, actual);
        }
    }
}