package com.nncloudtv.service;

import java.util.Date;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.BillingProfileDao;
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
    
    public void updateCreditCardInfo(BillingProfile profile, CreditCard creditCard, short status) {
        
        profile.setCardStatus(status);
        profile.setCardHolderName(creditCard.getCardHolderName());
        profile.setCardRemainDigits(creditCard.getCardNumber().substring(creditCard.getCardHolderName().length() - 4));
        save(profile);
    }
}
