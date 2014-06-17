package com.nncloudtv.service;

import java.util.Date;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.clearcommerce.ccxclientapi.CcApiBadKeyException;
import com.clearcommerce.ccxclientapi.CcApiDocument;
import com.clearcommerce.ccxclientapi.CcApiRecord;
import com.nncloudtv.dao.BillingProfileDao;
import com.nncloudtv.lib.ClearCommerceLib;
import com.nncloudtv.model.BillingProfile;
import com.nncloudtv.web.json.cms.CreditCard;

@Service
public class BillingProfileManager {
    
    protected static final Logger log = Logger.getLogger(BillingProfileManager.class.getName());
    
    protected BillingProfileDao dao;
    
    public BillingProfileManager() {
        
        dao = new BillingProfileDao();
    }
    
    public BillingProfile save(BillingProfile profile) {
        
        if (profile == null) return null;
        
        Date now = new Date();
        
        if (profile.getCreateDate() == null) {
            
            profile.setCreateDate(now);
        }
        profile.setUpdateDate(now);
        
        return dao.save(profile);
    }
    
    public BillingProfile findById(String idStr) {
        
        if (idStr == null) return null;
        
        long profileId = 0;
        try {
            profileId = Long.valueOf(idStr);
            
        } catch(NumberFormatException e) {
        }
        
        return dao.findById(profileId);
    }
    
    public BillingProfile updateAuthInfo(BillingProfile profile, CreditCard creditCard, CcApiDocument ccResult) {
        
        String txnStatus = null;
        
        if (ccResult == null) {
            log.warning("ccResult is null");
            return null;
        }
        CcApiRecord ccOverview = ClearCommerceLib.getOverview(ccResult);
        if (ccOverview == null) {
            log.warning("ccOverview is null");
            return null;
        }
        try {
            txnStatus = ccOverview.getFieldString("TransactionStatus");
            if (txnStatus == null) {
                log.warning("txnStatus is null");
                return null;
            }
        } catch (CcApiBadKeyException e) {
            log.warning("TransactionStatus is empty");
            return null;
        }
        if (txnStatus.equals("A")) {
            profile.setCardStatus(BillingProfile.AUTHED);
            profile.setCardHolderName(creditCard.getCardHolderName());
            profile.setCardRemainDigits(creditCard.getCardNumber().substring(creditCard.getCardNumber().length() - 4));
            save(profile);
        }
        
        return profile;
    }
}
