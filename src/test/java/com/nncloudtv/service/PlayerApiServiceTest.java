package com.nncloudtv.service;

import static org.junit.Assert.*;

import java.util.logging.Logger;
import javax.servlet.http.HttpSession;

//import junit.framework.Assert; The old method (of Junit 3),
// see http://stackoverflow.com/questions/291003/differences-between-2-junit-assert-classes

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
        mockPrefMngr = new MockNnUserPrefManager();
        mockUserMngr = new MockNnUserManager(mockPrefMngr);
        mockMsoMngr = new MockMsoManager();
        mockChMngr = new MockNnChannelManager();
        mockConfigMngr = new MockMsoConfigManager();
        mockProfileMngr = new MockNnUserProfileManager();
        
        req.addHeader(ApiContext.HEADER_USER_AGENT, MockHttpServletRequest.class.getName());
        HttpSession session = req.getSession();
        session.setMaxInactiveInterval(60);
        service = new PlayerApiService(mockUserMngr, mockMsoMngr, mockChMngr, mockConfigMngr, mockPrefMngr, mockProfileMngr, null, null);
        service.prepService(req, resp);
        System.out.println("@Before - setUp");
    }
	 	 	
	@Test
	public void testBrandInfo() {
	    
	    Object result = service.brandInfo(null, req);
	    
        assertNotNull(result);
	}

	@Test
	public void testSetProfile() {
	    
	    Object result = service.setUserProfile("mock-user-token-xxoo", "name,phone", "MockUser,7777777", req);
	    
        assertNotNull(result);
	}
	
	@Test
	public void testLogin() {
		String email = "a@a.com";
		String password = "123456";
		Object loginObj = service.login(email, password, req, resp);
		
        assertNotNull(loginObj);
	}

    @Test
    public void testQuickLogin() {
        String email = "a@a.com";
        String password = "123456";        
        Object userInfo = service.login(email, password, req, resp);
        System.out.println(userInfo);
        
        assertNotNull(userInfo);
    }
	
}
