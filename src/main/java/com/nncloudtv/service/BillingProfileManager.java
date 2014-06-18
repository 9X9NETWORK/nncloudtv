package com.nncloudtv.service;

import java.util.Date;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.clearcommerce.ccxclientapi.CcApiBadKeyException;
import com.clearcommerce.ccxclientapi.CcApiDocument;
import com.clearcommerce.ccxclientapi.CcApiRecord;
import com.nncloudtv.dao.BillingProfileDao;
import com.nncloudtv.lib.ClearCommerceLib;
import com.nncloudtv.lib.NnLogUtil;
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
    
    public BillingProfile findById(long profileId) {
        
        return dao.findById(profileId);
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
        String ccRefOrderId = null;
        
        if (ccResult == null) {
            log.warning("ccResult is null");
            return null;
        }
        CcApiRecord ccOverview = ClearCommerceLib.getOverview(ccResult);
        CcApiRecord ccOrderForm = ClearCommerceLib.getOrderForm(ccResult);
        if (ccOverview == null) {
            log.warning("ccOverview is null");
            return null;
        }
        if (ccOrderForm == null) {
            log.warning("ccOderForm is null");
            return null;
        }
        try {
            txnStatus = ccOverview.getFieldString("TransactionStatus");
            if (txnStatus == null) {
                log.warning("txnStatus is null");
                return null;
            }
            if (txnStatus.equals("A")) {
                
                ccRefOrderId = ccOrderForm.getFieldString("Id");
                if (ccRefOrderId == null) {
                    log.warning("ccRefOrderId is null");
                    return null;
                }
                profile.setCcRefOrderId(ccRefOrderId);
                CcApiRecord ccTransaction = ccOrderForm.getFirstRecord("Transaction");
                if (ccTransaction != null) {
                    
                    String ccRefTxnId = ccTransaction.getFieldString("Id"); 
                    if (ccRefTxnId != null) {
                        
                        profile.setCcRefTransId(ccRefTxnId);
                    }
                    
                }
                profile.setCardStatus(BillingProfile.AUTHED);
                profile.setCardHolderName(creditCard.getCardHolderName());
                profile.setCardRemainDigits(creditCard.getCardNumber().substring(creditCard.getCardNumber().length() - 4));
                save(profile);
            }
        } catch (CcApiBadKeyException e) {
            NnLogUtil.logException(e);
            return null;
        }
        
        return profile;
    }
}
