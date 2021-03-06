package com.nncloudtv.service;

import java.util.Date;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.clearcommerce.ccxclientapi.CcApiBadKeyException;
import com.clearcommerce.ccxclientapi.CcApiDocument;
import com.clearcommerce.ccxclientapi.CcApiRecord;
import com.nncloudtv.dao.BillingProfileDao;
import com.nncloudtv.lib.ClearCommerceLib;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.BillingProfile;
import com.nncloudtv.web.json.cms.CreditCard;

@Service
public class BillingProfileManager {
    
    protected static final Logger log = Logger.getLogger(BillingProfileManager.class.getName());
    
    protected BillingProfileDao dao = NNF.getBillingProfileDao();
    
    public BillingProfile save(BillingProfile profile) {
        
        if (profile == null) return null;
        
        Date now = NnDateUtil.now();
        
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
        
        return dao.findById(idStr);
    }
    
    public BillingProfile updateAuthInfo(BillingProfile profile, CreditCard creditCard, CcApiDocument ccResult) throws CcApiBadKeyException {
        
        String txnStatus = null;
        String ccRefOrderId = null;
        
        if (ccResult == null) throw new IllegalArgumentException("ccResult must not be null");
        CcApiRecord ccOverview = ClearCommerceLib.getOverview(ccResult);
        CcApiRecord ccOrderForm = ClearCommerceLib.getOrderForm(ccResult);
        txnStatus = ccOverview.getFieldString("TransactionStatus");
        String remains = creditCard.getCardNumber().substring(creditCard.getCardNumber().length() - 4);
        if (txnStatus.equals("A")) {
            
            log.info("card was accepted.");
            ccRefOrderId = ccOrderForm.getFieldString("Id");
            profile.setCcRefOrderId(ccRefOrderId);
            CcApiRecord ccTransaction = ccOrderForm.getFirstRecord("Transaction");
            profile.setCcRefTransId(ccTransaction.getFieldString("Id"));
            profile.setCardStatus(BillingProfile.AUTHED);
            profile.setCardHolderName(creditCard.getCardHolderName());
            profile.setCardRemainDigits(remains);
            log.info(String.format("billing profile updated (%s)", creditCard.getCardHolderName()));
            
            return save(profile);
            
        } else {
            
            log.info(String.format("card was declined (%s)", remains));
        }
        
        return profile;
    }
}
