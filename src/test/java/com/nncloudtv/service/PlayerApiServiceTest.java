package com.nncloudtv.service;

import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.mock.lib.MockNNF;
import com.nncloudtv.web.api.ApiContext;

public class PlayerApiServiceTest {

    protected static final Logger log = Logger.getLogger(PlayerApiServiceTest.class.getName());
    
    private PlayerApiService service;
	
    private MockHttpServletRequest req;
    private MockHttpServletResponse resp;    

    @Before
    public void setUp() {
        
        CacheFactory.isEnabled = false;
        
        MockNNF.initAll();
        
        req = new MockHttpServletRequest();
        resp = new MockHttpServletResponse();
        
        MockNNF.initAll();
        
        req.addHeader(ApiContext.HEADER_USER_AGENT, MockHttpServletRequest.class.getName());
        HttpSession session = req.getSession();
        session.setMaxInactiveInterval(60);
        service = new PlayerApiService(null, null);
        service.prepService(req, resp);
        System.out.println("@Before - setUp");
    }
    
	@Test
	public void testBrandInfo() {
	    
	    Object result = service.brandInfo(null, req);
	    
		Assert.assertNotNull(result); 
	}

	@Test
	public void testSetProfile() {
	    
	    Object result = service.setUserProfile("mock-user-token-xxoo", "name,phone", "MockUser,7777777", req);
	    
	    Assert.assertNotNull(result); 
	}
	
	@Test
	public void testLogin() {
		String email = "a@a.com";
		String password = "123456";
		Object loginObj = service.login(email, password, req, resp);
		
		Assert.assertNotNull(loginObj); 
	}

    @Test
    public void testQuickLogin() {
        String email = "a@a.com";
        String password = "123456";        
        Object userInfo = service.login(email, password, req, resp);
        System.out.println(userInfo);
        
        Assert.assertNotNull(userInfo);
    }
	
}
