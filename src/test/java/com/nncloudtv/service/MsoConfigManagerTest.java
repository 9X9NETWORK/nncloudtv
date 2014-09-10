package com.nncloudtv.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.nncloudtv.support.NnTestAll;

@Category(NnTestAll.class)
public class MsoConfigManagerTest {
    
    @Test
    public void testGetServerDomain() {
        
        String serverDomain = MsoConfigManager.getServerDomain();
        String check = MsoConfigManager.getProperty("facebook.properties", "server_domain");
        
        assertEquals(check, serverDomain);
    }
}