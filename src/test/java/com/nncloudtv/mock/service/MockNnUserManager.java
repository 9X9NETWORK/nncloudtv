package com.nncloudtv.mock.service;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserProfile;
import com.nncloudtv.service.NnUserManager;
import com.nncloudtv.service.NnUserPrefManager;

public class MockNnUserManager extends NnUserManager {
    
    protected static final Logger log = Logger.getLogger(MockNnUserManager.class.getName());
    
    public MockNnUserManager() {
        
        super(new MockNnUserPrefManager());
    }
    
    @Autowired
    public MockNnUserManager(NnUserPrefManager prefMngr) {
        
        super(prefMngr);
    }
    
    @Override
    public NnUser save(NnUser user) {
        
        return user;
    }
    
    @Override
    public String findLocaleByHttpRequest(HttpServletRequest req) {
        
        return LangTable.LANG_EN;
    }
    
    @Override
    public NnUser findAuthenticatedUser(String email, String password,
            long msoId, HttpServletRequest req) {
        
        NnUser mockUser = new NnUser(email, password, NnUser.TYPE_USER);
        NnUserProfile mockProfile = new NnUserProfile(msoId, "Mock User", LangTable.LANG_EN, LangTable.LANG_EN, null);
        mockUser.setProfile(mockProfile);
        
        return mockUser;
    }
    
    @Override
    public NnUser findByToken(String token, long msoId) {
        
        return new NnUser("_mock_@9x9.tv", "_password_", NnUser.TYPE_USER);
    }
    
}
