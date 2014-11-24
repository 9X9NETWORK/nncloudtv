package com.nncloudtv.lib;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
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
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.nncloudtv.support.NnTestAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CacheFactory.class})
@Category(NnTestAll.class)
public class CacheFactoryTest {
    
    protected static final Logger log = Logger.getLogger(CacheFactoryTest.class.getName());
    
    private boolean savedStateIsEnabled;
    private boolean savedStateIsRunning;
    
    @Before
    public void setUp() {
        
        savedStateIsEnabled = CacheFactory.isEnabled;
        savedStateIsRunning = CacheFactory.isRunning;
    }
    
    @After
    public void tearDown() {
        
        CacheFactory.isEnabled = savedStateIsEnabled;
        CacheFactory.isRunning = savedStateIsRunning;
    }
    
    @Test
    public void testGet() {
        
        CacheFactory.isEnabled = true;
        CacheFactory.isRunning = true;
        
        // input arguments
        final String key = "key";
        
        // mock data
        Object obj = new Object();
        
        // mock object
        MemcachedClient cache = Mockito.mock(MemcachedClient.class);
        @SuppressWarnings("unchecked")
        GetFuture<Object> future = Mockito.mock(GetFuture.class);
        
        // stubs
        PowerMockito.spy(CacheFactory.class);
        try {
            PowerMockito.doReturn(cache).when(CacheFactory.class, "getSharedClient");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        when(cache.asyncGet(key)).thenReturn(future);
        try {
            when(future.get(anyInt(), (TimeUnit) anyObject())).thenReturn(obj);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        
        // execute
        Object actual = CacheFactory.get(key);
        
        // verify
        try {
            PowerMockito.verifyPrivate(CacheFactory.class).invoke("getSharedClient");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        verify(cache).asyncGet(key);
        try {
            verify(future).get(CacheFactory.ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        
        assertEquals(obj, actual);
    }

}
