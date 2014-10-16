package com.nncloudtv.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
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
import com.nncloudtv.lib.NNF;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.NnGuest;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserProfile;
import com.nncloudtv.support.NnTestAll;
import com.nncloudtv.support.NnTestImportant;
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
    
    private static String pair(String key, String value) {
        return PlayerApiService.assembleKeyValue(key, value);
    }
    
    @RunWith(PowerMockRunner.class)
    @PrepareForTest({CacheFactory.class})
    @Category(NnTestImportant.class)
    public static class testBrandInfo extends PlayerApiServiceTest {
        
        private Map<String, Object> memCache;
        private MsoConfigManager configMngr;
        
        private String brandInfo9x9;
        private Mso mso9x9;
        private String defaultOS;
        
        @Before
        public void setUp2() {
            
            memCache = new HashMap<String, Object>();
            NnTestUtil.initMockMemcache(memCache);
            
            NnUserManager userMngr = Mockito.spy(new NnUserManager());
            NNFWrapper.setUserMngr(userMngr);
            
            configMngr = NNF.getConfigMngr();
            
            NnTestUtil.emptyTable(Mso.class);
            NnTestUtil.emptyTable(MsoConfig.class);
            
            // must stub, call network access
            doReturn("zh").when(userMngr).findLocaleByHttpRequest(req);
            
            // default cache for mso=9x9
            mso9x9 = NnTestUtil.getNnMso();
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            CacheFactory.set(cacheKey, mso9x9);
            
            // default cache for brandInfo=9x9, at os="web" format="text"
            brandInfo9x9 = "";
            brandInfo9x9 += PlayerApiService.assembleKeyValue("key", String.valueOf(mso9x9.getId()));
            brandInfo9x9 += PlayerApiService.assembleKeyValue("name", mso9x9.getName());
            defaultOS = PlayerService.OS_WEB;
            cacheKey = CacheFactory.getBrandInfoKey(mso9x9, defaultOS, PlayerApiService.FORMAT_PLAIN);
            CacheFactory.set(cacheKey, brandInfo9x9);
            
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
            
            memCache = null;
            configMngr = null;
            
            brandInfo9x9 = null;
            mso9x9 = null;
            defaultOS = null;
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
            CacheFactory.set(cacheKey, mso);
            // brandInfo=cts from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, defaultOS, PlayerApiService.FORMAT_PLAIN);
            CacheFactory.set(cacheKey, brandInfo);
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(defaultOS, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            assertTrue("Provide exist mso should return as its brand info.", ((String) actual).contains(brandInfo));
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
            CacheFactory.set(cacheKey, brandInfo);
            
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
            configMngr.save(mso, adConfig);
            
            String adInfo = "";
            String tab = "\t";
            String br = "\n";
            adInfo += "1" + tab + "0" + tab + "ad hello world!!" + tab + "http://s3.aws.amazon.com/creative_video0.mp4" + br;
            adInfo += "2" + tab + "1" + tab + "vastAd hello world!!" + tab + "http://rec.scupio.com/recweb/vast.aspx?" + br;
            
            // stubs
            // only mso=9x9 available from cache, overwrite original one, becuz need mso equality check at later stub
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            CacheFactory.set(cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_ANDROID, PlayerApiService.FORMAT_PLAIN);
            CacheFactory.set(cacheKey, brandInfo);
            // adInfo from cache
            cacheKey = CacheFactory.getAdInfoKey(mso, PlayerApiService.FORMAT_PLAIN);
            CacheFactory.set(cacheKey, adInfo);
            
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
            configMngr.save(mso, adConfig);
            
            String adInfo = "";
            String tab = "\t";
            String br = "\n";
            adInfo += "1" + tab + "0" + tab + "ad hello world!!" + tab + "http://s3.aws.amazon.com/creative_video0.mp4" + br;
            adInfo += "2" + tab + "1" + tab + "vastAd hello world!!" + tab + "http://rec.scupio.com/recweb/vast.aspx?" + br;
            
            // stubs
            // only mso=9x9 available from cache, overwrite original one, becuz need mso equality check at later stub
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            CacheFactory.set(cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_ANDROID, PlayerApiService.FORMAT_PLAIN);
            CacheFactory.set(cacheKey, brandInfo);
            // adInfo from cache
            cacheKey = CacheFactory.getAdInfoKey(mso, PlayerApiService.FORMAT_PLAIN);
            CacheFactory.set(cacheKey, adInfo);
            
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
            configMngr.save(mso, adConfig);
            
            String adInfo = "";
            String tab = "\t";
            String br = "\n";
            adInfo += "1" + tab + "0" + tab + "ad hello world!!" + tab + "http://s3.aws.amazon.com/creative_video0.mp4" + br;
            adInfo += "2" + tab + "1" + tab + "vastAd hello world!!" + tab + "http://rec.scupio.com/recweb/vast.aspx?" + br;
            
            // stubs
            // only mso=9x9 available from cache, overwrite original one, becuz need mso equality check at later stub
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            CacheFactory.set(cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_IOS, PlayerApiService.FORMAT_PLAIN);
            CacheFactory.set(cacheKey, brandInfo);
            // adInfo from cache
            cacheKey = CacheFactory.getAdInfoKey(mso, PlayerApiService.FORMAT_PLAIN);
            CacheFactory.set(cacheKey, adInfo);
            
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
            configMngr.save(mso, adConfig);
            
            String adInfo = "";
            String tab = "\t";
            String br = "\n";
            adInfo += "1" + tab + "0" + tab + "ad hello world!!" + tab + "http://s3.aws.amazon.com/creative_video0.mp4" + br;
            adInfo += "2" + tab + "1" + tab + "vastAd hello world!!" + tab + "http://rec.scupio.com/recweb/vast.aspx?" + br;
            
            // stubs
            // only mso=9x9 available from cache, overwrite original one, becuz need mso equality check at later stub
            String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
            CacheFactory.set(cacheKey, mso);
            // brandInfo from cache
            cacheKey = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_IOS, PlayerApiService.FORMAT_PLAIN);
            CacheFactory.set(cacheKey, brandInfo);
            // adInfo from cache
            cacheKey = CacheFactory.getAdInfoKey(mso, PlayerApiService.FORMAT_PLAIN);
            CacheFactory.set(cacheKey, adInfo);
            
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
    @Category(NnTestImportant.class)
    public static class testBrandInfoWithoutCache extends PlayerApiServiceTest {
        
        private Mso defaultMso;
        private String defaultOS;
        
        // common pair means OS independent Key-Value pair exist in response format=text
        private static final Map<String, String> commonPair;
        // pair that in OS=iOS can exist but not mean only exist in OS=iOS
        private static final Map<String, String> iosPair;
        // pair that in OS=Android can exist but not mean only exist in OS=Android
        private static final Map<String, String> androidPair;
        // pair that in OS=web can exist but not mean only exist in OS=web
        private static final Map<String, String> webPair;
        // the key store config's item name, the value store mock value to be verify
        private static final Map<String, String> items;
        static {
            Map<String, String> commonMap = new HashMap<String, String>();
            Map<String, String> iosMap = new HashMap<String, String>();
            Map<String, String> androidMap = new HashMap<String, String>();
            Map<String, String> webMap = new HashMap<String, String>();
            
            commonMap.put(MsoConfig.DEBUG,                  "c" + MsoConfig.DEBUG);
            commonMap.put(MsoConfig.FBTOKEN,                "c" + MsoConfig.FBTOKEN);
            commonMap.put(MsoConfig.RO,                     "c" + MsoConfig.RO);
            commonMap.put(MsoConfig.FORCE_UPGRADE,          "c" + MsoConfig.FORCE_UPGRADE);
            commonMap.put(MsoConfig.UPGRADE_MSG,            "c" + MsoConfig.UPGRADE_MSG);
            commonMap.put(MsoConfig.VIDEO,                  "c" + MsoConfig.VIDEO);
            commonMap.put(MsoConfig.ABOUT_US,               "c" + MsoConfig.ABOUT_US);
            commonMap.put(MsoConfig.SOCIAL_FEEDS,           "c" + MsoConfig.SOCIAL_FEEDS);
            commonMap.put(MsoConfig.SUPPORTED_REGION,       "c" + MsoConfig.SUPPORTED_REGION);
            commonMap.put(MsoConfig.FACEBOOK_CLIENTID,      "c" + MsoConfig.FACEBOOK_CLIENTID);
            commonMap.put(MsoConfig.CHROMECAST_ID,          "c" + MsoConfig.CHROMECAST_ID);
            commonMap.put(MsoConfig.SOCIAL_FEEDS_SERVER,    "c" + MsoConfig.SOCIAL_FEEDS_SERVER);
            commonMap.put(MsoConfig.SEARCH,                 "c" + MsoConfig.SEARCH);
            
            // these mock value are shared setting in different OS
            String mockShackDiscover = "on"; // "on" is only valid value
            String mockHomePage = "myhome.com";
            String mockNotificationSound = "bling";
            String mockSignupEnforce = "enforce";
            String mockAudioBackground = "on";
            
            iosMap.put(MsoConfig.SHAKE_DISCOVER,                mockShackDiscover);
            iosMap.put("flurry",                                "i" + "flurry");
            iosMap.put("ad",                                    "i" + "on");
            iosMap.put("ad-type",                               "i" + "ad-type"); // depend on "ad" exist or not
            iosMap.put("admob-key",                             "i" + "admob-key");
            iosMap.put(MsoConfig.AUDIO_BACKGROUND,              mockAudioBackground);
            iosMap.put(MsoConfig.HOMEPAGE,                      mockHomePage);
            iosMap.put(MsoConfig.NOTIFICATION_SOUND_VIBRATION,  mockNotificationSound);
            iosMap.put(MsoConfig.SIGNUP_ENFORCE,                mockSignupEnforce);
            iosMap.put("ga",                                    "i" + "ga");
            
            androidMap.put(MsoConfig.GCM_SENDER_ID,                 "a" + MsoConfig.GCM_SENDER_ID);
            androidMap.put(MsoConfig.SHAKE_DISCOVER,                mockShackDiscover);
            androidMap.put("flurry",                                "a" + "flurry");
            androidMap.put("ad",                                    "a" + "on");
            androidMap.put("admob-key",                             "a" + "admob-key");
            androidMap.put(MsoConfig.AUDIO_BACKGROUND,              mockAudioBackground);
            androidMap.put("youtube",                               "a" + "youtube");
            androidMap.put(MsoConfig.HOMEPAGE,                      mockHomePage);
            androidMap.put(MsoConfig.NOTIFICATION_SOUND_VIBRATION,  mockNotificationSound);
            androidMap.put(MsoConfig.SIGNUP_ENFORCE,                mockSignupEnforce);
            androidMap.put("ga",                                    "a" + "ga");
            
            webMap.put("ga", "w" + "ga");
            
            commonPair = Collections.unmodifiableMap(commonMap);
            iosPair = Collections.unmodifiableMap(iosMap);
            androidPair = Collections.unmodifiableMap(androidMap);
            webPair = Collections.unmodifiableMap(webMap);
            
            Map<String, String> itemMap = new HashMap<String, String>();
            
            itemMap.putAll(commonPair);
            itemMap.put(MsoConfig.SHAKE_DISCOVER,   mockShackDiscover);
            itemMap.put(MsoConfig.AUDIO_BACKGROUND, mockAudioBackground);
            itemMap.put(MsoConfig.HOMEPAGE,         mockHomePage);
            itemMap.put(MsoConfig.NOTIFICATION_SOUND_VIBRATION, mockNotificationSound);
            itemMap.put(MsoConfig.SIGNUP_ENFORCE,   mockSignupEnforce);
            
            itemMap.put(MsoConfig.GOOGLE_ANALYTICS_IOS, iosPair.get("ga"));
            itemMap.put(MsoConfig.FLURRY_ANALYTICS_IOS, iosPair.get("flurry"));
            itemMap.put(MsoConfig.AD_IOS,               iosPair.get("ad"));
            itemMap.put(MsoConfig.AD_IOS_TYPE,          iosPair.get("ad-type"));
            itemMap.put(MsoConfig.ADMOBKEY_IOS,         iosPair.get("admob-key"));
            
            itemMap.put(MsoConfig.GOOGLE_ANALYTICS_ANDROID, androidPair.get("ga"));
            itemMap.put(MsoConfig.FLURRY_ANALYTICS_ANDROID, androidPair.get("flurry"));
            itemMap.put(MsoConfig.AD_ANDROID,               androidPair.get("ad"));
            itemMap.put(MsoConfig.ADMOBKEY_ANDROID,         androidPair.get("admob-key"));
            itemMap.put(MsoConfig.YOUTUBE_ID_ANDROID,       androidPair.get("youtube"));
            itemMap.put(MsoConfig.GCM_SENDER_ID,            androidPair.get(MsoConfig.GCM_SENDER_ID));
            
            itemMap.put(MsoConfig.GOOGLE_ANALYTICS_WEB, webPair.get("ga"));
            
            items = Collections.unmodifiableMap(itemMap);
        }
        private static final Map<String, String> commonPairDefault;
        private static final Map<String, String> webPairDefault;
        private static final Map<String, String> iosPairDefault;
        private static final Map<String, String> androidPairDefault;
        static {
            Map<String, String> commonDefaultMap = new HashMap<String, String>();
            Map<String, String> webDefaultMap = new HashMap<String, String>();
            Map<String, String> iosDefaultMap = new HashMap<String, String>();
            Map<String, String> androidDefaultMap = new HashMap<String, String>();
            
            commonDefaultMap.put(MsoConfig.SUPPORTED_REGION, "en US;zh 台灣");
            commonDefaultMap.put(MsoConfig.FACEBOOK_CLIENTID, "361253423962738");
            commonDefaultMap.put(MsoConfig.CHROMECAST_ID, "DBB1992C");
            commonDefaultMap.put(MsoConfig.SEARCH, "all");
            
            webDefaultMap.put("ga", "UA-47454448-1");
            
            iosDefaultMap.put("ga", "UA-47454448-3");
            iosDefaultMap.put("flurry", "J6GPGNMBR7GRDJVSCCN8");
            iosDefaultMap.put("ad", "off");
            iosDefaultMap.put(MsoConfig.AUDIO_BACKGROUND, "off");
            iosDefaultMap.put("homepage", "portal");
            iosDefaultMap.put(MsoConfig.NOTIFICATION_SOUND_VIBRATION, "sound off;vibration off");
            iosDefaultMap.put(MsoConfig.SIGNUP_ENFORCE, "never");
            
            androidDefaultMap.put("ga", "UA-47454448-2");
            androidDefaultMap.put("flurry", "CJGQT59JKHN4MWBQFXZN");
            androidDefaultMap.put("ad", "off");
            androidDefaultMap.put(MsoConfig.AUDIO_BACKGROUND, "off");
            androidDefaultMap.put("youtube",
                    "AI39si5HrNx2gxiCnGFlICK4Bz0YPYzGDBdJHfZQnf-fClL2i7H_A6Fxz6arDBriAMmnUayBoxs963QLxfo-5dLCO9PCX-DTrA");
            androidDefaultMap.put("homepage", "portal");
            androidDefaultMap.put(MsoConfig.NOTIFICATION_SOUND_VIBRATION, "sound off;vibration off");
            androidDefaultMap.put(MsoConfig.SIGNUP_ENFORCE, "never");
            
            commonPairDefault = Collections.unmodifiableMap(commonDefaultMap);
            webPairDefault = Collections.unmodifiableMap(webDefaultMap);
            iosPairDefault = Collections.unmodifiableMap(iosDefaultMap);
            androidPairDefault = Collections.unmodifiableMap(androidDefaultMap);
        }
        
        @Before
        public void setUp2() {
            
            CacheFactory.isEnabled = false;
            CacheFactory.isRunning = false;
            
            NnTestUtil.emptyTable(Mso.class);
            NnTestUtil.emptyTable(MsoConfig.class);
            
            NnUserManager userMngr = Mockito.spy(new NnUserManager());
            NNFWrapper.setUserMngr(userMngr);
            
            // must stub, call network access
            doReturn("zh").when(userMngr).findLocaleByHttpRequest(req);
            
            String brandName = "cts";
            defaultMso = NnTestUtil.getNnMso();
            defaultMso.setName(brandName);
            defaultMso.setType(Mso.TYPE_MSO);
            
            MsoDao msoDao = NNF.getMsoDao();
            defaultMso = msoDao.save(defaultMso);
            
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
            
            defaultMso = null;
            defaultOS = null;
        }
        
        // all config available from database
        private void makeAllConfigAvailable() {
            
            MsoConfigManager configMngr = NNF.getConfigMngr();
            
            for (String itemName : items.keySet()) {
                
                MsoConfig config = new MsoConfig();
                config.setMsoId(defaultMso.getId());
                config.setItem(itemName);
                config.setValue(items.get(itemName));
                configMngr.save(defaultMso, config);
            }
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
        public void configKeyValueWithWeb() { // os=web & all config exist in database
            
            // exist list
            Map<String, String> exist = new HashMap<String, String>();
            exist.putAll(commonPair);
            exist.putAll(webPair);
            
            // can't exist list
            Set<String> notExist = new HashSet<String>();
            notExist.addAll(commonPair.keySet());
            notExist.addAll(webPair.keySet());
            notExist.addAll(iosPair.keySet());
            notExist.addAll(androidPair.keySet());
            notExist.removeAll(exist.keySet());
            
            // input
            String os = PlayerService.OS_WEB;
            req.setParameter("os", os);
            
            // stubs
            makeAllConfigAvailable();
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            
            String respText = (String) actual;
            for (String key : exist.keySet()) {
                assertTrue("Missing '" + key + "' pair.", respText.contains(pair(key, exist.get(key))));
            }
            for (String key : notExist) {
                assertFalse("'" + key + "' pair can't appear in response.", respText.contains(key + "\t"));
            }
        }
        
        @Test
        public void configKeyValueDefaultWithWeb() { // os=web & nothing config exist in database
            
            // exist list (has default)
            Map<String, String> exist = new HashMap<String, String>();
            exist.putAll(commonPairDefault);
            exist.putAll(webPairDefault);
            
            // can't exist list
            Set<String> notExist = new HashSet<String>();
            notExist.addAll(commonPair.keySet());
            notExist.addAll(webPair.keySet());
            notExist.addAll(iosPair.keySet());
            notExist.addAll(androidPair.keySet());
            notExist.removeAll(exist.keySet());
            
            // input
            String os = PlayerService.OS_WEB;
            req.setParameter("os", os);
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            
            String respText = (String) actual;
            for (String key : exist.keySet()) {
                assertTrue("Missing '" + key + "' pair.", respText.contains(pair(key, exist.get(key))));
            }
            for (String key : notExist) {
                assertFalse("'" + key + "' pair can't appear in response.", respText.contains(key + "\t"));
            }
        }
        
        @Test
        public void configKeyValueWithIos() { // os=ios & all config exist in database
            
            // exist list
            Map<String, String> exist = new HashMap<String, String>();
            exist.putAll(commonPair);
            exist.putAll(iosPair);
            
            // can't exist list
            Set<String> notExist = new HashSet<String>();
            notExist.addAll(commonPair.keySet());
            notExist.addAll(webPair.keySet());
            notExist.addAll(iosPair.keySet());
            notExist.addAll(androidPair.keySet());
            notExist.removeAll(exist.keySet());
            
            // input
            String os = PlayerService.OS_IOS;
            req.setParameter("os", os);
            
            // stubs
            makeAllConfigAvailable();
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            
            String respText = (String) actual;
            for (String key : exist.keySet()) {
                assertTrue("Missing '" + key + "' pair.", respText.contains(pair(key, exist.get(key))));
            }
            for (String key : notExist) {
                assertFalse("'" + key + "' pair can't appear in response.", respText.contains(key + "\t"));
            }
        }
        
        @Test
        public void configKeyValueDefaultWithIos() { // os=ios & nothing config exist in database
            
            // exist list (has default)
            Map<String, String> exist = new HashMap<String, String>();
            exist.putAll(commonPairDefault);
            exist.putAll(iosPairDefault);
            
            // can't exist list
            Set<String> notExist = new HashSet<String>();
            notExist.addAll(commonPair.keySet());
            notExist.addAll(webPair.keySet());
            notExist.addAll(iosPair.keySet());
            notExist.addAll(androidPair.keySet());
            notExist.removeAll(exist.keySet());
            
            // input
            String os = PlayerService.OS_IOS;
            req.setParameter("os", os);
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            
            String respText = (String) actual;
            for (String key : exist.keySet()) {
                assertTrue("Missing '" + key + "' pair.", respText.contains(pair(key, exist.get(key))));
            }
            for (String key : notExist) {
                assertFalse("'" + key + "' pair can't appear in response.", respText.contains(key + "\t"));
            }
        }
        
        @Test
        public void configKeyValueWithAndroid() { // os=android & all config exist in database
            
            // exist list
            Map<String, String> exist = new HashMap<String, String>();
            exist.putAll(commonPair);
            exist.putAll(androidPair);
            
            // can't exist list
            Set<String> notExist = new HashSet<String>();
            notExist.addAll(commonPair.keySet());
            notExist.addAll(webPair.keySet());
            notExist.addAll(iosPair.keySet());
            notExist.addAll(androidPair.keySet());
            notExist.removeAll(exist.keySet());
            
            // input
            String os = PlayerService.OS_ANDROID;
            req.setParameter("os", os);
            
            // stubs
            makeAllConfigAvailable();
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            
            String respText = (String) actual;
            for (String key : exist.keySet()) {
                assertTrue("Missing '" + key + "' pair.", respText.contains(pair(key, exist.get(key))));
            }
            for (String key : notExist) {
                assertFalse("'" + key + "' pair can't appear in response.", respText.contains(key + "\t"));
            }
        }
        
        @Test
        public void configKeyValueDefaultWithAndroid() { // os=android & nothing config exist in database
            
            // exist list (has default)
            Map<String, String> exist = new HashMap<String, String>();
            exist.putAll(commonPairDefault);
            exist.putAll(androidPairDefault);
            
            // can't exist list
            Set<String> notExist = new HashSet<String>();
            notExist.addAll(commonPair.keySet());
            notExist.addAll(webPair.keySet());
            notExist.addAll(iosPair.keySet());
            notExist.addAll(androidPair.keySet());
            notExist.removeAll(exist.keySet());
            
            // input
            String os = PlayerService.OS_ANDROID;
            req.setParameter("os", os);
            
            // execute
            service.prepService(req, resp, true);
            Object actual = service.brandInfo(os, req);
            
            // verify
            assertTrue("parameter format=text should return text format response.", actual instanceof String);
            
            String respText = (String) actual;
            for (String key : exist.keySet()) {
                assertTrue("Missing '" + key + "' pair.", respText.contains(pair(key, exist.get(key))));
            }
            for (String key : notExist) {
                assertFalse("'" + key + "' pair can't appear in response.", respText.contains(key + "\t"));
            }
        }
        
        // TODO third part 'ad' section test case
        
    }
    
    @RunWith(MockitoJUnitRunner.class)
    @Category(NnTestAll.class)
    public static class testSetProfile extends PlayerApiServiceTest {
        
        @Test
        public void normal() {
            
            CacheFactory.isEnabled = false;
            
            MsoManager msoMngr = Mockito.mock(MsoManager.class);
            NnUserManager userMngr = Mockito.mock(NnUserManager.class);
            NnUserProfileManager profileMngr = Mockito.mock(NnUserProfileManager.class);
            MsoConfigManager configMngr = Mockito.spy(new MsoConfigManager());
            NNFWrapper.setUserMngr(userMngr);
            NNFWrapper.setMsoMngr(msoMngr);
            NNFWrapper.setProfileMngr(profileMngr);
            NNFWrapper.setConfigMngr(configMngr);
            
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
            doReturn(null).when(configMngr).getByMsoAndItem((Mso) anyObject(), eq(MsoConfig.APP_EXPIRE));
            doReturn(null).when(configMngr).getByMsoAndItem((Mso) anyObject(), eq(MsoConfig.APP_VERSION_EXPIRE));
            
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
    @Category(NnTestAll.class)
    public static class testLogin extends PlayerApiServiceTest {
        
        @Test
        public void normal() {
            
            CacheFactory.isEnabled = false;
            
            MsoManager msoMngr = Mockito.mock(MsoManager.class);
            NnUserManager userMngr = Mockito.mock(NnUserManager.class);
            MsoConfigManager configMngr = Mockito.spy(new MsoConfigManager());
            NNFWrapper.setUserMngr(userMngr);
            NNFWrapper.setMsoMngr(msoMngr);
            NNFWrapper.setConfigMngr(configMngr);
            
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
            doReturn(null).when(configMngr).getByMsoAndItem((Mso) anyObject(), eq(MsoConfig.APP_EXPIRE));
            doReturn(null).when(configMngr).getByMsoAndItem((Mso) anyObject(), eq(MsoConfig.APP_VERSION_EXPIRE));
            
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
    
    @RunWith(PowerMockRunner.class)
    @PrepareForTest({CacheFactory.class})
    @Category(NnTestImportant.class)
    public static class testPrepService extends PlayerApiServiceTest {
        
        private Map<String, Object> memCache;
        private Mso defaultMso;
        
        @Before
        public void setUp2() {
            
            // replace mock one, mock hide field that we want to verify
            service = new PlayerApiService();
            
            NnTestUtil.emptyTable(Mso.class);
            NnTestUtil.emptyTable(MsoConfig.class);
            
            memCache = new HashMap<String, Object>();
            NnTestUtil.initMockMemcache(memCache);
            
            defaultMso = NnTestUtil.getNnMso();
            MsoDao msoDao = NNF.getMsoDao();
            defaultMso = msoDao.save(defaultMso);
        }
        
        @After
        public void tearDown2() {
            
            memCache = null;
            defaultMso = null;
        }
        
        private Object getPrivateField(String field) {
            
            try {
                Field format = service.getClass().getDeclaredField(field);
                format.setAccessible(true);
                return format.get(service);
                
            } catch (SecurityException e) {
                fail(e.toString());
            } catch (NoSuchFieldException e) {
                fail(e.toString());
            } catch (IllegalArgumentException e) {
                fail(e.toString());
            } catch (IllegalAccessException e) {
                fail(e.toString());
            }
            
            return null;
        }
        
        @Test
        public void notProvideVersionNorDatabaseSetting() {
            
            req.removeParameter("v");
            
            int status = service.prepService(req, resp);
            
            assertEquals("missing 'v' in request parameter and without 'v' setting in database " +
                    "should always see as success operation.", NnStatusCode.SUCCESS, status);
        }
        
        @Test
        public void notProvideVersionButDatabaseSettingHigherThanDefault() {
            
            req.removeParameter("v");
            
            MsoConfig minVersion = new MsoConfig();
            minVersion.setMsoId(defaultMso.getId());
            minVersion.setItem(MsoConfig.API_MINIMAL);
            minVersion.setValue("40");
            NNF.getConfigMngr().save(defaultMso, minVersion);
            
            int status = service.prepService(req, resp);
            
            assertEquals("missing 'v' setting will evalute to v=31, with database hold v=40 then " +
                    "should response for 'api force upgrade'.", NnStatusCode.API_FORCE_UPGRADE, status);
        }
        
        @Test
        public void provideVersionHigherThanDatabaseSetting() {
            
            req.setParameter("v", "40");
            
            MsoConfig minVersion = new MsoConfig();
            minVersion.setMsoId(defaultMso.getId());
            minVersion.setItem(MsoConfig.API_MINIMAL);
            minVersion.setValue("32");
            NNF.getConfigMngr().save(defaultMso, minVersion);
            
            int status = service.prepService(req, resp);
            
            assertEquals("when 'v' set to v=40, with database hold v=32 then " +
                    "should return as success operation.", NnStatusCode.SUCCESS, status);
        }
        
        @Test
        public void provideVersionLowerThanDatabaseSetting() {
            
            req.setParameter("v", "32");
            
            MsoConfig minVersion = new MsoConfig();
            minVersion.setMsoId(defaultMso.getId());
            minVersion.setItem(MsoConfig.API_MINIMAL);
            minVersion.setValue("40");
            NNF.getConfigMngr().save(defaultMso, minVersion);
            
            int status = service.prepService(req, resp);
            
            assertEquals("when 'v' set to v=32, with database hold v=40 then " +
                    "should response for 'api force upgrade'.", NnStatusCode.API_FORCE_UPGRADE, status);
        }
        
        @Test
        public void provideUnknownFormat() {
            
            req.setParameter("format", "xyz");
            
            service.prepService(req, resp);
            
            short format = (Short) getPrivateField("format");
            assertEquals("parameter format=xyz(unknown format) should see as default format=text to access.",
                    PlayerApiService.FORMAT_PLAIN, format);
        }
        
        @Test
        public void notProvideFormat() {
            
            req.removeParameter("format");
            
            service.prepService(req, resp);
            
            short format = (Short) getPrivateField("format");
            assertEquals("parameter format not provide should see as default format=text to access.",
                    PlayerApiService.FORMAT_PLAIN, format);
        }
        
        @Test
        public void notExistMso() {
            
            req.setParameter("mso", "notExist");
            
            service.prepService(req, resp);
            
            Mso actual = (Mso) getPrivateField("mso");
            if (actual == null || actual.getName() != Mso.NAME_9X9) {
                fail("Not exist mso should see as default(mso=9x9) to access.");
            }
        }
        
        @Test
        public void notProvideMso() {
            
            req.removeParameter("mso");
            
            service.prepService(req, resp);
            
            Mso actual = (Mso) getPrivateField("mso");
            if (actual == null || actual.getName() != Mso.NAME_9X9) {
                fail("Not provide mso should see as default(mso=9x9) to access.");
            }
        }
        
        @Test
        public void existMsoFromRoot() {
            
            String brandName = "cts";
            req.removeParameter("mso");
            req.setServerName(brandName + ".flipr.tv");
            req.setRequestURI("/playerAPI/brandInfo");
            
            Mso mso = NnTestUtil.getNnMso();
            mso.setName(brandName);
            mso.setType(Mso.TYPE_MSO);
            
            // mso=cts available from database
            NNF.getMsoDao().save(mso);
            
            service.prepService(req, resp);
            
            Mso actual = (Mso) getPrivateField("mso");
            if (actual == null || actual.getName() != brandName) {
                fail("Provide exist mso from root domain should set it to further access.");
            }
        }
    }
}