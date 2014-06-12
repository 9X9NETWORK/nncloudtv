package com.nncloudtv.service;

import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.BillingOrderDao;

@Service
public class BillingOrderManager {
    
    protected static final Logger log = Logger.getLogger(BillingOrderManager.class.getName());
    
    protected BillingOrderDao dao;
    
    public BillingOrderManager() {
        
        dao = new BillingOrderDao();
    }
    
}
