package com.nncloudtv.mock.service;

import java.util.ArrayList;
import java.util.List;

import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserPref;
import com.nncloudtv.service.NnUserPrefManager;

public class MockNnUserPrefManager extends NnUserPrefManager {

    @Override
    public List<NnUserPref> findByUser(NnUser user) {
        
        return new ArrayList<NnUserPref>();
    }
    
}
