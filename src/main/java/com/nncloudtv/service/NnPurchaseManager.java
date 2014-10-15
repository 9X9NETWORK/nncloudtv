package com.nncloudtv.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.nncloudtv.dao.NnPurchaseDao;
import com.nncloudtv.exception.AppStoreFailedVerifiedException;
import com.nncloudtv.lib.AppStoreLib;
import com.nncloudtv.lib.GooglePlayLib;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
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
    
    public void verifyPurchase(NnPurchase purchase, boolean isProduction) {
        
        if (purchase == null) { return; }
        log.info("verify purchase, purchaseId = " + purchase.getId());
        NnItem item = NNF.getItemMngr().findById(purchase.getItemId());
        
        if (item.getBillingPlatform() == NnItem.GOOGLEPLAY) {
            
            try {
                
                SubscriptionPurchase subscription = GooglePlayLib.getSubscriptionPurchase(purchase);
                if (subscription == null) {
                    
                    log.warning("subscription is null");
                    purchase.setStatus(NnPurchase.INVALID);
                    save(purchase);
                    
                    return;
                }
                purchase.setExpireDate(new Date(subscription.getExpiryTimeMillis()));
                purchase.setVerified(true);
                checkExpireDate(purchase);
                
            } catch (GeneralSecurityException e) {
                
                purchase.setVerified(false);
                log.warning("GeneralSecurityException");
                log.warning(e.getMessage());
                
            } catch (IOException e) {
                
                //purchase.setVerified(false);
                log.warning("IOException");
                log.warning(e.getMessage());
                
            } finally {
                
                save(purchase);
            }
            
        } else if (item.getBillingPlatform() == NnItem.APPSTORE) {
            
            try {
                
                JSONObject receipt = AppStoreLib.getReceipt(purchase, isProduction);
                if (receipt == null) {
                    
                    log.warning("fail to get receipt");
                    return;
                }
                
                purchase.setExpireDate(new Date(receipt.getLong("expires_date")));
                String productIdRef = receipt.getString("product_id");
                // check productId
                if (item.getProductIdRef().equals(productIdRef) == false) {
                    
                    log.warning("productIdRef not match");
                    purchase.setVerified(false);
                    purchase.setStatus(NnPurchase.INVALID);
                    save(purchase);
                    
                    return;
                }
                // check bundleId
                Mso mso = NNF.getMsoMngr().findById(item.getMsoId());
                String bundleId = MsoConfigManager.getAppStoreBundleId(mso);
                if (bundleId.equals(receipt.getString("app_item_id")) == false) {
                    
                    log.warning("bundleId not matched");
                    purchase.setVerified(false);
                    purchase.setStatus(NnPurchase.INVALID);
                    save(purchase);
                    
                    return;
                }
                
                purchase.setVerified(true);
                checkExpireDate(purchase);
                
            } catch (JSONException e) {
                log.warning("JSONException");
                log.warning(e.getMessage());
                
            } catch (AppStoreFailedVerifiedException e) {
                
                log.warning("AppStoreFailedVerifiedException");
                purchase.setVerified(false);
                
            } finally {
                
                save(purchase);
            }
            
        } else {
            
            log.warning("not supported platform");
            // unknown platform - do nothing
        }
        
    }
    
    private void checkExpireDate(NnPurchase purchase) {
        
        if (purchase == null || purchase.getExpireDate() == null) { return; }
        
        if (purchase.getExpireDate().before(NnDateUtil.now())) {
            log.info("inactive");
            purchase.setStatus(NnPurchase.INACTIVE);
        } else {
            log.info("active");
            purchase.setStatus(NnPurchase.ACTIVE);
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
    
    public NnPurchase findByUserAndItem(NnUser user, NnItem item) {
        
        return dao.findOne(user.getIdStr(), item.getId());
    }
    
    // TODO: rewrite
    public boolean isPurchased(NnUser user, NnChannel channel) {
        
        if (user == null || channel == null) { return false; }
        
        List<NnPurchase> purchases = findByUser(user);
        
        for (NnPurchase purchase : purchases) {
            
            NnItem item = NNF.getItemMngr().findById(purchase.getItemId());
            
            if (item.getChannelId() == channel.getId()) {
                
                return true;
            }
        }
        
        return false;
    }
    
    public List<NnPurchase> findAllActive() {
        
        return dao.findAllActive();
    }
    
    public List<NnPurchase> findAll() {
        
        return dao.findAll();
    }
}
