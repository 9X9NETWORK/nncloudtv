package com.nncloudtv.service;

import static org.junit.Assert.*;

import org.junit.Test;

public class MsoConfigManagerTest {
    
    @Test
    public void testGetServerDomain() {
        
        String serverDomain = MsoConfigManager.getServerDomain();
        String check = MsoConfigManager.getProperty("facebook.properties", "server_domain");
        
        assertEquals(check, serverDomain);
    }
}
