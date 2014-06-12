package com.nncloudtv.service;

import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.BillingProfileDao;

@Service
public class BillingProfileManager {
    
    protected static final Logger log = Logger.getLogger(BillingProfileManager.class.getName());
    
    protected BillingProfileDao dao;
    
    public BillingProfileManager() {
        
        dao = new BillingProfileDao();
    }
    
}
