package com.nncloudtv.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
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
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.nncloudtv.dao.MsoDao;
import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.support.NnTestUtil;
import com.nncloudtv.web.api.ApiContext;
import com.nncloudtv.web.json.player.ApiStatus;
import com.nncloudtv.web.json.player.BrandInfo;
import com.nncloudtv.wrapper.NNFWrapper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CacheFactory.class})
public class PlayerApiServiceTest2 {

    protected static final Logger log = Logger.getLogger(PlayerApiServiceTest2.class.getName());
    
    private static PlayerApiService service;
    private static MockHttpServletRequest req;
    private static MockHttpServletResponse resp;
    
    @Before
    public void setUp() {
        
        req = new MockHttpServletRequest();
        resp = new MockHttpServletResponse();
        service = Mockito.spy(new PlayerApiService());
        
        log.info("------------------------------------------------outer setUp");
    }
    
    @After
    public void tearDown() {
        
        req = null;
        resp = null;
        service = null;
        
        NNFWrapper.empty();
        log.info("------------------------------------------------outer tearDown");
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
    
    private static void recordMemoryCacheGet(MemcachedClient cache, String key, Object returnObj) {
        
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
    
    public static class testBrandInfo extends PlayerApiServiceTest2 {
        
        private MemcachedClient cache;
        private NnUserManager userMngr;
        private MsoConfigManager configMngr;
        
        @Before
        public void setUp2() {
            log.info("------------------------------------------------inner setUp");
            
            cache = Mockito.mock(MemcachedClient.class);
            setUpMemCacheMock(cache);
            
            userMngr = Mockito.spy(new NnUserManager());
            configMngr = Mockito.spy(new MsoConfigManager());
            
            NNFWrapper.setUserMngr(userMngr);
            NNFWrapper.setConfigMngr(configMngr);
            
            doReturn(null).when(configMngr).findByItem(MsoConfig.API_MINIMAL); // must stub
            doReturn(null).when(configMngr).findByItem(MsoConfig.RO); // must stub
            doReturn("zh").when(userMngr).findLocaleByHttpRequest(req); // must stub
            
            // default input
            req.setParameter("v", "40");
            req.setParameter("format", "text");
            req.setParameter("lang", "zh");
            req.setParameter("os", "web");
            req.setParameter("mso", Mso.NAME_9X9);
            req.setServerName("localhost:8080");
            req.setRequestURI("/playerAPI/brandInfo");
        }
        
        @After
        public void tearDown2() {
            log.info("------------------------------------------------inner tearDown");
            
            cache = null;
            userMngr = null;
            configMngr = null;
        }
        
        @Test
        public void notExistMso() {
            
            // input
            String brandName = "notExist";
            String os = "web";
            req.setParameter("mso", brandName);
            
            // mock object
            MsoDao msoDao = Mockito.spy(new MsoDao());
            NNFWrapper.setMsoDao(msoDao);
            
            // mock data
            Mso mso = NnTestUtil.getNnMso();
            
            String brandInfo = "";
            brandInfo += PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
            brandInfo += PlayerApiService.assembleKeyValue("name", mso.getName());
            
            // stubs
            // only mso=9x9 available from cache
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            cacheKey = "mso(" + brandName + ")";
            recordMemoryCacheGet(cache, cacheKey, null);
            doReturn(null).when(msoDao).findByName(brandName); // must stub
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, os, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            // execute
            int status = service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Not exist mso should return as mso=9x9 brand info.", ((String) actual).contains(brandInfo));
        }
        
        @Test
        public void notProvideMso() {
            
            // input
            String os = "web";
            req.removeParameter("mso");
            
            // mock data
            Mso mso = NnTestUtil.getNnMso();
            
            String brandInfo = "";
            brandInfo += PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
            brandInfo += PlayerApiService.assembleKeyValue("name", mso.getName());
            
            // stubs
            // only mso=9x9 available from cache
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, os, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            // execute
            int status = service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Not provide mso should return as mso=9x9 brand info.", ((String) actual).contains(brandInfo));
        }
        
        @Test
        public void existMso() {
            
            // input
            String brandName = "cts";
            String os = "web";
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
            // only mso=cts available from cache
            String cacheKey = "mso(" + brandName + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, os, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            // execute
            int status = service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Provide exist mso should return as its brand info.", ((String) actual).contains(brandInfo));
        }
        
        @Test
        public void existMsoFromRoot() {
            
            // input
            String brandName = "cts";
            String os = "web";
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
            // only mso=cts available from database
            doReturn(mso).when(msoDao).findByName(brandName); // must stub
            // brandInfo from cache
            String cacheKey = CacheFactory.getBrandInfoKey(mso, os, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            // execute
            int status = service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Provide exist mso from root domain should return as its brand info.",
                    ((String) actual).contains(brandInfo));
        }
        
        @Test
        public void provideTextFormat() {
            
            // input
            String os = "web";
            req.setParameter("format", "text");
            
            // mock data
            Mso mso = NnTestUtil.getNnMso();
            
            String brandInfo = "";
            brandInfo += PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
            brandInfo += PlayerApiService.assembleKeyValue("name", mso.getName());
            
            // stubs
            // only mso=9x9 available from cache
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, os, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            // execute
            int status = service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Should return as mso=9x9 brand info.", ((String) actual).contains(brandInfo));
        }
        
        @Test
        public void provideJsonFormat() {
            
            // input
            String os = "web";
            req.setParameter("format", "json");
            
            // mock data
            Mso mso = NnTestUtil.getNnMso();
            
            BrandInfo brandInfo = new BrandInfo();
            brandInfo.setKey(mso.getId());
            brandInfo.setName(mso.getName());
            
            // stubs
            // only mso=9x9 available from cache
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, os, PlayerApiService.FORMAT_JSON);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            // execute
            int status = service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=json should return json format response.", actual instanceof ApiStatus);
            assertTrue("parameter format=json should return json format response.",
                    ((ApiStatus) actual).getData() instanceof BrandInfo);
            BrandInfo actualBrandInfo = (BrandInfo) ((ApiStatus) actual).getData();
            assertEquals("Should return as mso=9x9 brand info.", brandInfo.getKey(), actualBrandInfo.getKey());
            assertEquals("Should return as mso=9x9 brand info.", brandInfo.getName(), actualBrandInfo.getName());
        }
        
        @Test
        public void provideUnknownFormat() {
            
            // TODO discuz, provide unknown format parameter test case apply for all player api, need each api write one ?
            
            // input
            String os = "web";
            req.setParameter("format", "xyz");
            
            // mock data
            Mso mso = NnTestUtil.getNnMso();
            
            String brandInfo = "";
            brandInfo += PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
            brandInfo += PlayerApiService.assembleKeyValue("name", mso.getName());
            
            // stubs
            // only mso=9x9 available from cache
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, os, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            // execute
            int status = service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=xyz(unknown format) should return text format response.", actual instanceof String);
            assertTrue("Should return as mso=9x9 brand info.", ((String) actual).contains(brandInfo));
        }
        
        @Test
        public void notProvideFormat() {
            
            // TODO discuz, not provide format parameter test case apply for all player api, need each api write one ?
            
            // input
            String os = "web";
            req.removeParameter("format");
            
            // mock data
            Mso mso = NnTestUtil.getNnMso();
            
            String brandInfo = "";
            brandInfo += PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
            brandInfo += PlayerApiService.assembleKeyValue("name", mso.getName());
            
            // stubs
            // only mso=9x9 available from cache
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, os, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            // execute
            int status = service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format not provide should return text format response.", actual instanceof String);
            assertTrue("Should return as mso=9x9 brand info.", ((String) actual).contains(brandInfo));
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
            brandInfo += PlayerApiService.assembleKeyValue("name", mso.getName());
            
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
            // only mso=9x9 available from cache
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_ANDROID, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            doReturn(adConfig).when(configMngr).findByMsoAndItem(mso, adKeyName); // must stub
            
            // adInfo from cache
            cacheKey = CacheFactory.getAdInfoKey(mso, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, adInfo);
            
            // execute
            int status = service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Should return as provide mso's brand info.", ((String) actual).contains(brandInfo));
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
            brandInfo += PlayerApiService.assembleKeyValue("name", mso.getName());
            
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
            // only mso=9x9 available from cache
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_ANDROID, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            doReturn(adConfig).when(configMngr).findByMsoAndItem(mso, adKeyName); // must stub
            
            // adInfo from cache
            cacheKey = CacheFactory.getAdInfoKey(mso, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, adInfo);
            
            // execute
            int status = service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Should return as provide mso's brand info.", ((String) actual).contains(brandInfo));
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
            brandInfo += PlayerApiService.assembleKeyValue("name", mso.getName());
            
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
            // only mso=9x9 available from cache
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_IOS, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            doReturn(adConfig).when(configMngr).findByMsoAndItem(mso, adKeyName); // must stub
            
            // adInfo from cache
            cacheKey = CacheFactory.getAdInfoKey(mso, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, adInfo);
            
            // execute
            int status = service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Should return as provide mso's brand info.", ((String) actual).contains(brandInfo));
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
            brandInfo += PlayerApiService.assembleKeyValue("name", mso.getName());
            
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
            // only mso=9x9 available from cache
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_IOS, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            doReturn(adConfig).when(configMngr).findByMsoAndItem(mso, adKeyName); // must stub
            
            // adInfo from cache
            cacheKey = CacheFactory.getAdInfoKey(mso, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, adInfo);
            
            // execute
            int status = service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Should return as provide mso's brand info.", ((String) actual).contains(brandInfo));
            assertTrue("Should contain adInfo.", ((String) actual).contains(adInfo));
        }
        
        @Test
        public void osWeb() {
            
            // input
            String os = PlayerService.OS_WEB;
            req.setParameter("os", os);
            
            // mock data
            Mso mso = NnTestUtil.getNnMso();
            
            String brandInfo = "";
            brandInfo += PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
            brandInfo += PlayerApiService.assembleKeyValue("name", mso.getName());
            
            // stubs
            // only mso=9x9 available from cache
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_WEB, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            // execute
            int status = service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Should return as provide mso's brand info.", ((String) actual).contains(brandInfo));
        }
        
        @Test
        public void osNotProvide() {
            
            // input
            String os = null;
            req.removeParameter("os");
            req.addHeader(ApiContext.HEADER_USER_AGENT, "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:31.0)");
            
            // mock data
            Mso mso = NnTestUtil.getNnMso();
            
            String brandInfo = "";
            brandInfo += PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
            brandInfo += PlayerApiService.assembleKeyValue("name", mso.getName());
            
            // stubs
            // only mso=9x9 available from cache
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_WEB, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            // execute
            int status = service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Should return as provide mso's brand info.", ((String) actual).contains(brandInfo));
        }
        
        @Test
        public void osNotSupport() {
            
            // input
            String os = "xyz";
            req.setParameter("os", os);
            
            // mock data
            Mso mso = NnTestUtil.getNnMso();
            
            String brandInfo = "";
            brandInfo += PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
            brandInfo += PlayerApiService.assembleKeyValue("name", mso.getName());
            
            // stubs
            // only mso=9x9 available from cache
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            recordMemoryCacheGet(cache, cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_WEB, PlayerApiService.FORMAT_PLAIN);
            recordMemoryCacheGet(cache, cacheKey, brandInfo);
            
            // execute
            int status = service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Should return as provide mso's brand info.", ((String) actual).contains(brandInfo));
        }
        
    }

}
