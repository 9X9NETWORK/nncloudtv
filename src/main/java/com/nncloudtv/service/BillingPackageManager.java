package com.nncloudtv.service;

import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.BillingPackageDao;

@Service
public class BillingPackageManager {
    
    protected static final Logger log = Logger.getLogger(BillingPackageManager.class.getName());
    
    BillingPackageDao dao;
    
    public BillingPackageManager() {
        
        dao = new BillingPackageDao();
    }
    
}
