package com.nncloudtv.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.anyShort;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.GetFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.support.NnTestAll;
import com.nncloudtv.web.json.player.BrandInfo;
import com.nncloudtv.wrapper.NNFWrapper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MsoConfigManager.class, CacheFactory.class, MsoManager.class})
@Category(NnTestAll.class)
public class MsoManagerTest {
    
    MsoManager msoMngr;
    
    @Mock MsoConfigManager mockConfigMngr;
    
    @Before
    public void setUp() {
        
        msoMngr = new MsoManager();
        
        NNFWrapper.setConfigMngr(mockConfigMngr);
    }
    
    @After
    public void tearDown() {
        mockConfigMngr = null;
        msoMngr = null;
    }
    
    @Test
    public void testGetBrandInfo() {
        
        msoMngr = PowerMockito.spy(new MsoManager());
        
        // input arguments
        MockHttpServletRequest req = new MockHttpServletRequest();
        Mso mso = new Mso("name", "intro", "email", Mso.TYPE_NN);
        final String os = "os";
        final short format = PlayerApiService.FORMAT_JSON;
        final String locale = "tw";
        final long counter = 1;
        final String piwik = "piwik";
        final String acceptLang = "zh";
        
        // mock data
        String mockOs = PlayerService.OS_ANDROID;
        String cacheKey = "cacheKey";
        Object cached = new BrandInfo();
        
        // stubs
        try {
            PowerMockito.when(msoMngr, "checkOs", os, req).thenReturn(mockOs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        PowerMockito.mockStatic(CacheFactory.class);
        when(CacheFactory.getBrandInfoKey((Mso) anyObject(), anyString(), anyShort())).thenReturn(cacheKey);
        when(CacheFactory.get(anyString())).thenReturn(cached);
        
        // execute
        Object actual = msoMngr.getBrandInfo(req, mso, os, format, locale, counter, piwik, acceptLang);
        
        // verify
        try {
            PowerMockito.verifyPrivate(msoMngr).invoke("checkOs", os, req);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        PowerMockito.verifyStatic();
        CacheFactory.getBrandInfoKey(mso, mockOs, format);
        PowerMockito.verifyStatic();
        CacheFactory.get(cacheKey);
        
        assertEquals(cached, actual);
        
        BrandInfo jsonActual = (BrandInfo) actual;
        assertEquals(locale, jsonActual.getLocale());
        assertEquals(counter, jsonActual.getBrandInfoCounter());
        assertEquals(piwik, jsonActual.getPiwik());
        assertEquals(acceptLang, jsonActual.getAcceptLang());
    }
    
    @Test
    public void testGetBrandInfoV2() {
        
        msoMngr = PowerMockito.spy(new MsoManager());
        
        // input arguments
        MockHttpServletRequest req = new MockHttpServletRequest();
        Mso mso = new Mso("name", "intro", "email", Mso.TYPE_NN);
        final String os = "os";
        final short format = PlayerApiService.FORMAT_JSON;
        final String locale = "tw";
        final long counter = 1;
        final String piwik = "piwik";
        final String acceptLang = "zh";
        
        // mock data
        String mockOs = PlayerService.OS_ANDROID;
        //String cacheKey = "cacheKey";
        Object cached = new BrandInfo();
        
        // mock object
        MemcachedClient cache = Mockito.mock(MemcachedClient.class);
        @SuppressWarnings("unchecked")
        GetFuture<Object> future = Mockito.mock(GetFuture.class);
        
        // stubs
        try {
            PowerMockito.when(msoMngr, "checkOs", os, req).thenReturn(mockOs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //PowerMockito.mockStatic(CacheFactory.class);
        //when(CacheFactory.getBrandInfoKey((Mso) anyObject(), anyString(), anyShort())).thenReturn(cacheKey);
        //when(CacheFactory.get(anyString())).thenReturn(cached);
        
        // do not mock CacheFactory but mock MemcachedClient
        CacheFactory.isEnabled = true;
        CacheFactory.isRunning = true;
        
        PowerMockito.spy(CacheFactory.class);
        try {
            PowerMockito.doReturn(cache).when(CacheFactory.class, "getSharedClient");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        when(cache.asyncGet(anyString())).thenReturn(future);
        try {
            when(future.get(anyInt(), (TimeUnit) anyObject())).thenReturn(cached); // only one purpose is get cached object
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        // I called this duplicate code where CacheFactoryTest.testGet has covered.
        
        // execute
        Object actual = msoMngr.getBrandInfo(req, mso, os, format, locale, counter, piwik, acceptLang);
        
        // verify
        try {
            PowerMockito.verifyPrivate(msoMngr).invoke("checkOs", os, req);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //PowerMockito.verifyStatic();
        //CacheFactory.getBrandInfoKey(mso, mockOs, format);
        //CacheFactory.get(cacheKey);
        
        try {
            PowerMockito.verifyPrivate(CacheFactory.class).invoke("getSharedClient");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        verify(cache).asyncGet(anyString());
        try {
            verify(future).get(CacheFactory.ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        // I called this duplicate code where CacheFactoryTest.testGet has covered.
        
        assertEquals(cached, actual);
        
        BrandInfo jsonActual = (BrandInfo) actual;
        assertEquals(locale, jsonActual.getLocale());
        assertEquals(counter, jsonActual.getBrandInfoCounter());
        assertEquals(piwik, jsonActual.getPiwik());
        assertEquals(acceptLang, jsonActual.getAcceptLang());
    }
    
    @Test
    public void testIsVliadBrand() {
        
        // input arguments
        final Long msoId = (long) 1;
        Mso mockMso = new Mso(Mso.NAME_CTS, "mock cts mso", "cts@9x9.tv", Mso.TYPE_MSO);
        mockMso.setId(msoId);
        
        NnChannel channel = new NnChannel("name", "intro", "imgUrl");
        channel.setStatus(NnChannel.STATUS_SUCCESS);
        channel.setContentType(NnChannel.CONTENTTYPE_MIXED);
        channel.setPublic(true);
        channel.setSphere(LangTable.LANG_ZH);
        
        // mock data
        MsoConfig supportedRegion = new MsoConfig();
        supportedRegion.setMsoId(msoId);
        supportedRegion.setItem(MsoConfig.SUPPORTED_REGION);
        supportedRegion.setValue("zh 台灣");
        
        List<String> spheres = new ArrayList<String>();
        spheres.add("zh");
        
        // stubs
        when(mockConfigMngr.findByMsoAndItem((Mso) anyObject(), anyString())).thenReturn(supportedRegion);
        
        PowerMockito.mockStatic(MsoConfigManager.class);
        when(MsoConfigManager.parseSupportedRegion(anyString())).thenReturn(spheres);
        
        // execute
        Boolean actual = msoMngr.isValidBrand(channel, mockMso);
        
        // verify
        verify(mockConfigMngr).findByMsoAndItem(mockMso, MsoConfig.SUPPORTED_REGION);
        
        PowerMockito.verifyStatic();
        MsoConfigManager.parseSupportedRegion(supportedRegion.getValue());
        
        assertTrue("The mock mso should be a valid brand of mock channel.", actual);
    }
}