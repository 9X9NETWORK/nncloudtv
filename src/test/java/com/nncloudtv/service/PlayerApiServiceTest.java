package com.nncloudtv.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.anyShort;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import junit.framework.Assert; The old method (of Junit 3),
// see http://stackoverflow.com/questions/291003/differences-between-2-junit-assert-classes

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.GetFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import com.nncloudtv.wrapper.NNFWrapper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BasicValidator.class,CacheFactory.class})
public class PlayerApiServiceTest {

    protected static final Logger log = Logger.getLogger(PlayerApiServiceTest.class.getName());
    
    private PlayerApiService service;
    
    private MockHttpServletRequest req;
    private MockHttpServletResponse resp;
    @Mock private NnUserManager mockUserMngr;
    @Mock private MsoManager mockMsoMngr;
    @Mock private NnUserProfileManager mockProfileMngr;

    @Before
    public void setUp() {
        
        CacheFactory.isEnabled = false;
        
        req = new MockHttpServletRequest();
        resp = new MockHttpServletResponse();
        
        //req.addHeader(ApiContext.HEADER_USER_AGENT, MockHttpServletRequest.class.getName());
        //HttpSession session = req.getSession();
        //session.setMaxInactiveInterval(60);
        service = Mockito.spy(new PlayerApiService());
        
        //NNFWrapper.setUserMngr(mockUserMngr);
        //NNFWrapper.setMsoMngr(mockMsoMngr);
        //NNFWrapper.setProfileMngr(mockProfileMngr);
        
        System.out.println("@Before - setUp");
    }
    
    @After
    public void tearDown() {
        req = null;
        resp = null;
        mockUserMngr = null;
        mockMsoMngr = null;
        mockProfileMngr = null;
        
        service = null;
        
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
    public void testBrandInfo() {
        
        NNFWrapper.setMsoMngr(mockMsoMngr);
        
        // input arguments
        String os = null;
        
        // mock data
        String locale = "zh";
        Object brandInfo = "brandInfo";
        Object result = "result";
        
        // mock object
        
        
        // stubs
        //when(service.findLocaleByHttpRequest((MockHttpServletRequest) anyObject())).thenReturn(locale);
        doReturn(locale).when(service).findLocaleByHttpRequest((MockHttpServletRequest) anyObject());
        when(mockMsoMngr.getBrandInfo((MockHttpServletRequest) anyObject(), (Mso) anyObject(),
                anyString(), anyShort(), anyString(), anyLong(), anyString(), anyString())).thenReturn(brandInfo);
        when(service.assembleMsgs(anyInt(), anyObject())).thenReturn(result);
        
        // execute
        Object actual = service.brandInfo(os, req);
        
        // verify
        verify(service).findLocaleByHttpRequest(req);
        verify(mockMsoMngr).getBrandInfo((MockHttpServletRequest) anyObject(), (Mso) anyObject(),
                anyString(), anyShort(), anyString(), anyLong(), anyString(), anyString());
        verify(service).assembleMsgs(NnStatusCode.SUCCESS, brandInfo);
        assertEquals(result, actual);
    }
    
    @Test
    public void testBrandInfoNotExistMso() {
        
        // input
        String brandName = "notExist";
        String os = "web";
        req.setParameter("v", "40");
        req.setParameter("format", "text");
        req.setParameter("lang", "zh");
        req.setParameter("os", os);
        req.setParameter("mso", brandName);
        req.setServerName("localhost:8080");
        req.setRequestURI("/playerAPI/brandInfo");
        
        // mock object
        MemcachedClient cache = Mockito.mock(MemcachedClient.class);
        NnUserManager userMngr = Mockito.spy(new NnUserManager());
        MsoDao msoDao = Mockito.spy(new MsoDao());
        MsoConfigManager configMngr = Mockito.spy(new MsoConfigManager());
        
        NNFWrapper.setUserMngr(userMngr);
        NNFWrapper.setMsoDao(msoDao);
        NNFWrapper.setConfigMngr(configMngr);
        
        // mock data
        Mso mso = NnTestUtil.getNnMso();
        
        String brandInfo = "";
        brandInfo += PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
        brandInfo += PlayerApiService.assembleKeyValue("name", mso.getName());
        
        // stubs
        // only mso=9x9 available from cache
        setUpMemCacheMock(cache);
        String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
        recordMemoryCacheGet(cache, cacheKey, mso);
        cacheKey = "mso(" + brandName + ")";
        recordMemoryCacheGet(cache, cacheKey, null);
        doReturn(null).when(msoDao).findByName(brandName); // must stub
        doReturn(null).when(configMngr).findByItem(MsoConfig.API_MINIMAL); // must stub
        doReturn(null).when(configMngr).findByItem(MsoConfig.RO); // must stub
        doReturn("zh").when(userMngr).findLocaleByHttpRequest(req); // must stub
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
    public void testBrandInfoNotProvideMso() {
        
        // input
        String os = "web";
        req.setParameter("v", "40");
        req.setParameter("format", "text");
        req.setParameter("lang", "zh");
        req.setParameter("os", os);
        req.setServerName("localhost:8080");
        req.setRequestURI("/playerAPI/brandInfo");
        
        // mock object
        MemcachedClient cache = Mockito.mock(MemcachedClient.class);
        NnUserManager userMngr = Mockito.spy(new NnUserManager());
        MsoConfigManager configMngr = Mockito.spy(new MsoConfigManager());
        
        NNFWrapper.setUserMngr(userMngr);
        NNFWrapper.setConfigMngr(configMngr);
        
        // mock data
        Mso mso = NnTestUtil.getNnMso();
        
        String brandInfo = "";
        brandInfo += PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
        brandInfo += PlayerApiService.assembleKeyValue("name", mso.getName());
        
        // stubs
        // only mso=9x9 available from cache
        setUpMemCacheMock(cache);
        String cacheKey = "mso(" + Mso.NAME_9X9 + ")";
        recordMemoryCacheGet(cache, cacheKey, mso);
        doReturn(null).when(configMngr).findByItem(MsoConfig.API_MINIMAL); // must stub
        doReturn(null).when(configMngr).findByItem(MsoConfig.RO); // must stub
        doReturn("zh").when(userMngr).findLocaleByHttpRequest(req); // must stub
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
    public void testBrandInfoExistMso() {
        
        // input
        String brandName = "cts";
        String os = "web";
        req.setParameter("v", "40");
        req.setParameter("format", "text");
        req.setParameter("lang", "zh");
        req.setParameter("os", os);
        req.setParameter("mso", brandName);
        req.setServerName("localhost:8080");
        req.setRequestURI("/playerAPI/brandInfo");
        
        // mock object
        MemcachedClient cache = Mockito.mock(MemcachedClient.class);
        NnUserManager userMngr = Mockito.spy(new NnUserManager());
        MsoConfigManager configMngr = Mockito.spy(new MsoConfigManager());
        
        NNFWrapper.setUserMngr(userMngr);
        NNFWrapper.setConfigMngr(configMngr);
        
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
        setUpMemCacheMock(cache);
        String cacheKey = "mso(" + brandName + ")";
        recordMemoryCacheGet(cache, cacheKey, mso);
        doReturn(null).when(configMngr).findByItem(MsoConfig.API_MINIMAL); // must stub
        doReturn(null).when(configMngr).findByItem(MsoConfig.RO); // must stub
        doReturn("zh").when(userMngr).findLocaleByHttpRequest(req); // must stub
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
    public void testBrandInfoExistMsoFromRoot() {
        
        // input
        String brandName = "cts";
        String os = "web";
        req.setParameter("v", "40");
        req.setParameter("format", "text");
        req.setParameter("lang", "zh");
        req.setParameter("os", os);
        req.setServerName(brandName + ".flipr.tv");
        req.setRequestURI("/playerAPI/brandInfo");
        
        // mock object
        MemcachedClient cache = Mockito.mock(MemcachedClient.class);
        NnUserManager userMngr = Mockito.spy(new NnUserManager());
        MsoDao msoDao = Mockito.spy(new MsoDao());
        MsoConfigManager configMngr = Mockito.spy(new MsoConfigManager());
        
        NNFWrapper.setUserMngr(userMngr);
        NNFWrapper.setMsoDao(msoDao);
        NNFWrapper.setConfigMngr(configMngr);
        
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
        doReturn(null).when(configMngr).findByItem(MsoConfig.API_MINIMAL); // must stub
        doReturn(null).when(configMngr).findByItem(MsoConfig.RO); // must stub
        doReturn("zh").when(userMngr).findLocaleByHttpRequest(req); // must stub
        // brandInfo from cache
        setUpMemCacheMock(cache);
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
    public void testSetProfile() {
        
        NNFWrapper.setUserMngr(mockUserMngr);
        NNFWrapper.setMsoMngr(mockMsoMngr);
        NNFWrapper.setProfileMngr(mockProfileMngr);
        
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
        when(mockMsoMngr.getByNameFromCache(anyString())).thenReturn(mso);
        doReturn(0).when(service).checkApiMinimal();
        doReturn(NnStatusCode.SUCCESS).when(service).checkRO();
        
        when(mockUserMngr.findByToken(anyString(), anyLong())).thenReturn(user);
        when(mockProfileMngr.findByUser((NnUser) anyObject())).thenReturn(null);
        when(mockProfileMngr.save((NnUser) anyObject(), (NnUserProfile) anyObject())).thenReturn(null);
        
        // execute
        service.prepService(req, resp);
        Object actual = service.setUserProfile(userToken, items, values, req);
        
        // verify
        verify(mockMsoMngr).getByNameFromCache(anyString());
        verify(service).checkApiMinimal();
        verify(service).checkRO();
        
        verify(mockUserMngr).findByToken(userToken, msoId);
        verify(mockProfileMngr).findByUser(user);
        
        ArgumentCaptor<NnUserProfile> arg = ArgumentCaptor.forClass(NnUserProfile.class);
        verify(mockProfileMngr).save(eq(user), arg.capture());
        NnUserProfile profile = arg.getValue();
        assertEquals(msoId, (Long) profile.getMsoId());
        assertEquals(userId, (Long) profile.getUserId());
        assertEquals(name, profile.getName());
        assertEquals(phone, profile.getPhoneNumber());
        
        assertNotNull(actual);
    }
    
    @Test
    public void testLogin() {
        
        NNFWrapper.setUserMngr(mockUserMngr);
        NNFWrapper.setMsoMngr(mockMsoMngr);
        
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
        when(mockMsoMngr.getByNameFromCache(anyString())).thenReturn(mso);
        doReturn(0).when(service).checkApiMinimal();
        doReturn(NnStatusCode.SUCCESS).when(service).checkRO();
        
        PowerMockito.mockStatic(BasicValidator.class);
        when(BasicValidator.validateRequired((String[]) anyObject())).thenReturn(true);
        
        when(mockUserMngr.findAuthenticatedUser(anyString(), anyString(), anyLong(),
                (HttpServletRequest) anyObject())).thenReturn(user);
        // Spy object call one time for when() seems OK, but over two times got trouble (don't know why),
        // call doXXXX() family method first then when() method second solve it.
        doReturn(userInfo).when(service).prepareUserInfo((NnUser) anyObject(), (NnGuest) anyObject(),
                (HttpServletRequest) anyObject(), anyBoolean());
        when(mockUserMngr.save((NnUser) anyObject())).thenReturn(user);
        doNothing().when(service).setUserCookie((HttpServletResponse) anyObject(), anyString(), anyString());
        when(service.assembleMsgs(anyInt(), (String[]) anyObject())).thenReturn(result);
        
        // execute
        service.prepService(req, resp);
        Object actual = service.login(email, password, req, resp);
        
        // verify
        verify(mockMsoMngr).getByNameFromCache(anyString());
        verify(service).checkApiMinimal();
        verify(service).checkRO();
        
        PowerMockito.verifyStatic();
        BasicValidator.validateRequired(new String[] {email, password});
        
        verify(mockUserMngr).findAuthenticatedUser(email, password, msoId, req);
        verify(service).prepareUserInfo(user, null, req, true);
        verify(mockUserMngr).save(user);
        verify(service).setUserCookie(resp, CookieHelper.USER, token);
        
        String[] raw = {(String) userInfo};
        verify(service).assembleMsgs(NnStatusCode.SUCCESS, raw);
        
        assertEquals(result, actual);
    }
    
}