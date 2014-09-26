package com.nncloudtv.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.nncloudtv.dao.NnPurchaseDao;
import com.nncloudtv.lib.GooglePlayLib;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.NnPurchase;
import com.nncloudtv.model.NnUser;

@Service
public class NnPurchaseManager {
    
    protected static final Logger log = Logger.getLogger(NnPurchaseManager.class.getName());
    
    protected NnPurchaseDao dao = NNF.getPurchaseDao();
    
    public List<NnPurchase> findByUser(NnUser user) {
        
        if (user == null) {
            return null;
        }
        
        return dao.findByUserIdStr(user.getIdStr());
    }
    
    public void updatePurchase(NnPurchase purchase) {
        
        if (purchase == null) { return; }
        
        try {
            SubscriptionPurchase subscription = GooglePlayLib.getSubscriptionPurchase(purchase);
            if (subscription == null) {
                log.warning("can not get SubscriptionPurchase");
                return;
            }
            purchase.setVerified(true);
            purchase.setExpireDate(new Date(subscription.getExpiryTimeMillis()));
            if (purchase.getExpireDate().before(NnDateUtil.now())) {
                log.info("set to inactive");
                purchase.setStatus(NnPurchase.INACTIVE);
            }
            save(purchase);
            
        } catch (GeneralSecurityException e) {
            log.warning(e.getMessage());
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
    }
    
    private void save(NnPurchase purchase) {
        
        Date now = NnDateUtil.now();
        
        if (purchase.getCreateDate() == null) {
            purchase.setCreateDate(now);
        }
        purchase.setUpdateDate(now);
        
        dao.save(purchase);
    }
}
