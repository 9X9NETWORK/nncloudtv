package com.nncloudtv.mock.service;

import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserProfile;
import com.nncloudtv.service.NnUserProfileManager;

public class MockNnUserProfileManager extends NnUserProfileManager {
    
    @Override
    public NnUserProfile save(NnUser user, NnUserProfile profile) {
        
        return profile;
    }
    
    @Override
    public NnUserProfile findByUser(NnUser user) {
        
        return null;
    }
    
}
