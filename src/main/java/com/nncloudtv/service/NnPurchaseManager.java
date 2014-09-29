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
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.NnItem;
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
    
    public void verifyPurchase(NnPurchase purchase) {
        
        if (purchase == null) { return; }
        
        NnItem item = NNF.getItemMngr().findById(purchase.getItemId());
        
        if (item.getBillingPlatform() == NnItem.GOOGLEPLAY) {
            
            try {
                purchase.setVerified(false);
                SubscriptionPurchase subscription = GooglePlayLib.getSubscriptionPurchase(purchase);
                purchase.setExpireDate(new Date(subscription.getExpiryTimeMillis()));
                purchase.setVerified(true);
                if (purchase.getExpireDate().before(NnDateUtil.now())) {
                    purchase.setStatus(NnPurchase.INACTIVE);
                } else {
                    purchase.setStatus(NnPurchase.ACTIVE);
                }
            } catch (GeneralSecurityException e) {
                log.warning("GeneralSecurityException");
                log.warning(e.getMessage());
            } catch (IOException e) {
                log.warning("IOException");
                log.warning(e.getMessage());
            } finally {
                save(purchase);
            }
        } else {
            // unknown platform - do nothing
        }
    }
    
    public NnPurchase save(NnPurchase purchase) {
        
        Date now = NnDateUtil.now();
        
        if (purchase.getCreateDate() == null) {
            purchase.setCreateDate(now);
        }
        purchase.setUpdateDate(now);
        
        return dao.save(purchase);
    }
    
    public Object composeEachPurchase(NnPurchase purchase, NnItem item) {
        
        String[] obj = {
                String.valueOf(item.getChannelId()),
                item.getProductIdRef(),
                String.valueOf(item.getBillingPlatform()),
        };
        
        return NnStringUtil.getDelimitedStr(obj);
    }
    
    public NnPurchase findByUserAndItem(NnUser user, NnItem item) {
        
        return dao.findOne(user.getIdStr(), item.getId());
    }
}
