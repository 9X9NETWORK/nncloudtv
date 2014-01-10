package com.nncloudtv.service;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.mock.service.MockMsoConfigManager;
import com.nncloudtv.mock.service.MockMsoManager;
import com.nncloudtv.mock.service.MockNnChannelManager;
import com.nncloudtv.mock.service.MockNnUserManager;
import com.nncloudtv.mock.service.MockNnUserPrefManager;
import com.nncloudtv.mock.service.MockNnUserProfileManager;
import com.nncloudtv.web.api.ApiContext;

public class PlayerApiServiceTest {

    protected static final Logger log = Logger.getLogger(PlayerApiServiceTest.class.getName());
    
    private PlayerApiService service;
	
    private MockHttpServletRequest req;
    private MockHttpServletResponse resp;    
    private MockNnUserManager mockUserMngr;    
    private MockMsoManager mockMsoMngr;
    private MockNnChannelManager mockChMngr;
    private MockMsoConfigManager mockConfigMngr;
    private MockNnUserPrefManager mockPrefMngr;
    private MockNnUserProfileManager mockProfileMngr;

    @Before
    public void setUp() {
        
        CacheFactory.isEnabled = false;
        
        req = new MockHttpServletRequest();
        resp = new MockHttpServletResponse();
        mockUserMngr = new MockNnUserManager();
        mockMsoMngr = new MockMsoManager();
        mockChMngr = new MockNnChannelManager();
        mockConfigMngr = new MockMsoConfigManager();
        mockPrefMngr = new MockNnUserPrefManager();
        mockProfileMngr = new MockNnUserProfileManager();
        
        req.addHeader(ApiContext.HEADER_USER_AGENT, MockHttpServletRequest.class.getName());
        HttpSession session = req.getSession();
        session.setMaxInactiveInterval(60);
        service = new PlayerApiService(mockUserMngr, mockMsoMngr, mockChMngr, mockConfigMngr, mockPrefMngr, mockProfileMngr);
        service.prepService(req);
        System.out.println("@Before - setUp");
    }
	 	 	
	@Test
	public void testBrandInfo() {
	    
	    String result = service.brandInfo(req);
	    
		Assert.assertTrue(result.contains("SUCCESS")); 
	}

	@Test
	public void testSetProfile() {
	    
	    String result = service.setUserProfile("mock-user-token-xxoo", "name,phone", "MockUser,7777777", req);
	    
	    Assert.assertTrue(result.contains("SUCCESS")); 
	}
	
	@Test
	public void testLogin() {
		String email = "a@a.com";
		String password = "123456";
		String loginStr = service.login(email, password, req, resp);
		Assert.assertTrue(loginStr.contains("SUCCESS")); 
	}

    @Test
    public void testQuickLogin() {
        String email = "a@a.com";
        String password = "123456";        
        String userInfo = service.login(email, password, req, resp);
        System.out.println(userInfo);
        userInfo = "";
        Pattern pattern = Pattern.compile(".*sphere\t((en|zh)).*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(userInfo);
        if (matcher.matches()) {
            System.out.println(matcher.group(1));
        } else {
            System.out.println("not Valid");
        }
    }
	
}
