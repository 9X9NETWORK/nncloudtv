package com.nncloudtv.service;

import java.util.logging.Logger;

import org.springframework.stereotype.Service;

@Service
public class BillingService {
    
    protected static final Logger log = Logger.getLogger(BillingService.class.getName());
    
    protected BillingOrderManager orderMngr;
    protected BillingProfileManager profileMngr;
    protected BillingPackageManager packageMngr;
    
    public BillingService() {
        
        orderMngr = new BillingOrderManager();
        profileMngr = new BillingProfileManager();
        packageMngr = new BillingPackageManager();
    }
    
}
