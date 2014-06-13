package com.nncloudtv.lib;

import java.util.logging.Logger;

import com.clearcommerce.ccxclientapi.CcApiDocument;
import com.nncloudtv.model.BillingOrder;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.web.json.cms.CreditCard;

public class ClearCommerceLib {
    
    protected static final Logger log = Logger.getLogger(ClearCommerceLib.class.getName());
    
    public static CcApiDocument verifyCreditCardNumber(CreditCard creditCard) {
        
        String ccClientId = MsoConfigManager.getCCClientId();
        String ccBillingGateway = MsoConfigManager.getCCBillingGayeway();
        
        log.info("clearcommerce clientId = " + ccClientId + ", gateway = " + ccBillingGateway);
        
        if (ccClientId == null || ccBillingGateway == null) return null;
        
        // Qoo
        
        
        
        
        
        
        return null;
    }
    
    public static CcApiDocument preAuth(BillingOrder order) {
        
        // Qoo
        
        
        
        
        return null;
    }
    
    
}
