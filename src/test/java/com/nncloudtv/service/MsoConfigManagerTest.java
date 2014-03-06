package com.nncloudtv.service;

import org.junit.Assert;
import org.junit.Test;

public class MsoConfigManagerTest {
    
    @Test
    public void testGetServerDomain() {
        
        String serverDomain = MsoConfigManager.getServerDomain();
        String check = MsoConfigManager.getProperty("facebook.properties", "server_domain");
        
        Assert.assertEquals(check, serverDomain);
    }
}
